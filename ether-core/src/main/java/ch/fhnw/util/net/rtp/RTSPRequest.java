/*
 * Copyright (c) 2013 - 2016 Stefan Muller Arisona, Simon Schubiger
 * Copyright (c) 2013 - 2016 FHNW & ETH Zurich
 * All rights reserved.
 *
 * Contributions by: Filip Schramka, Samuel von Stachelski
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *  Neither the name of FHNW / ETH Zurich nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ch.fhnw.util.net.rtp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import ch.fhnw.util.Log;
import ch.fhnw.util.TextUtilities;
import ch.fhnw.util.net.NetworkUtilities;

public class RTSPRequest implements Runnable {
	private final static Log LOG = Log.create();

	final static int MJPEG_TIMEBASE = 90000;

	final static String CRLF = "\r\n";

	//----------------
	//rtsp message types
	enum REQType {
		GET,
		POST,
		OPTIONS,
		SETUP,
		PLAY,
		PAUSE,
		TEARDOWN,
		DESCRIBE,
	}

	final static Set<String> REQTypes = new HashSet<>();
	static {
		for(REQType t : REQType.values())
			REQTypes.add(t.toString());
	}

	private Socket socketRTSP; //socket used to send/receive RTSP messages
	//input and output stream filters
	private DataInputStream    in;
	private DataOutputStream   out;
	private int                localRTSPport = 0;
	private Map<String,String> request = new HashMap<>();
	private int                cSeq;
	private final String       label;
	private final InetAddress  clientIP;   //Client IP address
	private int                sessionKey = -1;
	private final RTPServer    server;

	//--------------------------------
	//Constructor
	//--------------------------------
	public RTSPRequest(RTPServer server, Socket socket) throws Exception {
		this.server        = server;
		this.label         = socket.toString();
		this.socketRTSP    = socket;
		this.localRTSPport = socket.getLocalPort();
		LOG.info(this + "New client connection");

		socket.setSendBufferSize(128 * 1024);
		socket.setTcpNoDelay(true);
		socket.setPerformancePreferences(0, 2, 1);
		socket.setTrafficClass(NetworkUtilities.IPTOS_LOWDELAY | NetworkUtilities.IPTOS_THROUGHPUT);

		//Get Client IP address
		clientIP = socketRTSP.getInetAddress();

		//Set input and output stream filters:
		in  = new DataInputStream(socketRTSP.getInputStream());
		out = new DataOutputStream(socketRTSP.getOutputStream());

		Thread t = new Thread(this, label);
		t.setDaemon(true);
		t.start();
	}

	@Override
	public void run() {
		try {
			msg_loop:
				for(;;) {
					//------------------------------------
					//Parse RTSP Request
					//------------------------------------
					REQType reqType = null;
					LOG.info(this + "Received from Client:");
					for(;;) {
						//parse request line and extract the request_type:
						String line = readLine(in);
						if(line == null) break msg_loop;
						LOG.info(line);
						if(line.length() == 0) {
							if(reqType == null) continue;
							break;
						}
						StringTokenizer tokens = new StringTokenizer(line);
						String key = tokens.nextToken();
						
						if("CSeq:".equals(key))
							cSeq = Integer.parseInt(tokens.nextToken());
						else if("Session:".equals(key))
							sessionKey= Integer.parseInt(tokens.nextToken());

						if(REQTypes.contains(key))
							reqType = REQType.valueOf(key);
						request.put(key, line.length() > key.length() ? line.substring(key.length() + 1) : "");
					}

					switch(reqType) { 
					case OPTIONS:
						send(RTSP_options());
						break; // msg_loop;
					case SETUP:
						new RTPSession(server, this);
						break;
					case PLAY:
						server.getSession(this).play(this);
						break;
					case PAUSE:
						server.getSession(this).pause(this);
						break;
					case TEARDOWN:
						server.getSession(this).teardown(this);
						break msg_loop;
					case DESCRIBE:
						send(RTSP_describe());
						break;
					case GET:
						if(get("Accept:").toLowerCase().equals("application/x-rtsp-tunnelled") && sessionCookie() != null) {
							server.addChannel(sessionCookie(), this);
							send(HTTP_ok());
							break;
						}
						send(HTTP_bad_req());
						break msg_loop;
					case POST:
						if(!(get("Content-Type:").toLowerCase().equals("application/x-rtsp-tunnelled"))) {
							send(HTTP_bad_req());
							break msg_loop;
						}
						in  = new DataInputStream(new Base64InputStream(in));
						//in = new DataInputStream(Base64.getDecoder().wrap(in));
						break;
					default:
						LOG.warning(this + "Unexpected request:" + request);
						break;
					}
				}
		in.close();
		out.close();
		} catch(Throwable t) {
			LOG.warning(toString(), t);
		}
		server.closeSession(getSessionKey());
		LOG.info(this+"Connetion closed");
	}

	private String readLine(DataInputStream in) throws IOException, InterruptedException {
		StringBuilder result = new StringBuilder();
		for(;;) {
			int ch = in.read();
			if(ch < 0)   return null;
			//System.err.print(ch < ' ' ? "<"+ch+">" : (char)ch); System.err.flush();
			if(ch == '$') {
				int channel = in.read();
				int size    = in.readChar();
				byte[] data = new byte[size];
				in.readFully(data, 0, size);
				RTPSession session = server.getSession(this);
				if(session != null)
					session.recv(channel, data);
				continue;
			}
			else if(ch == '\r') continue;
			else if(ch == '\n') return result.toString();
			else result.append((char)ch);
		}
	}

	private String sessionCookie() {
		return get("x-sessioncookie:");
	}

	void send(String msg) throws IOException {
		if(sessionCookie() != null) {
			RTSPRequest req = server.getChannel(sessionCookie());
			if(req != this) {
				req.send(msg);
				return;
			}
		}
		synchronized (out) {
			out.write(msg.getBytes());
			out.flush();
		}
		LOG.info(this + "Sent to Client:");
		LOG.info(msg);
	}

	void send(int channel, byte[] data) throws IOException {
		if(sessionCookie() != null) {
			RTSPRequest req = server.getChannel(sessionCookie());
			if(req != this) {
				req.send(channel, data);
				return;
			}
		}
		synchronized (out) {
			out.write(0x24);
			out.write(channel);
			out.writeChar(data.length);
			out.write(data);
			out.flush();
		}
	}

	// Creates a DESCRIBE response string in SDP format for current media
	private String describe() {
		// Write the body first so we can get the size later
		String content = 
				"v=0" + CRLF +
				"m=video " + 0 + " RTP/AVP " + RTPpacket.MJPEG_TYPE + CRLF +
				"a=control:streamid=" + getSessionKey() + CRLF +
				"a=rtpmap:26 JPEG/" + MJPEG_TIMEBASE + CRLF +
				"a=mimetype:string;\"video/MJPEG\"" + CRLF;

		String header = 
				"Content-Base: " +RTPServer.getContentBase(socketRTSP.getInetAddress(), localRTSPport)+ CRLF +
				"Content-Type: " + "application/sdp" + CRLF +
				"Content-Length: " + content.length() + CRLF + CRLF;

		return header + content;
	}

	private String reqTypes() {
		String result = "";
		for(REQType t : REQType.values())
			result += t.toString() + ", ";
		return result.substring(0, result.length() - 2);
	}

	//------------------------------------
	//RTSP Response
	//------------------------------------

	private String RTSP_options() {
		return 
				"RTSP/1.0 200 OK"+CRLF +
				"CSeq: "+cSeq+CRLF +
				"Public: "+reqTypes()+CRLF
				+CRLF;
	}

	private String RTSP_describe() {
		return 
				"RTSP/1.0 200 OK"+CRLF +
				"CSeq: "+cSeq+CRLF +
				describe();
	}

	private String HTTP_ok() {
		return 
				"HTTP/1.0 200 OK"+CRLF +
				"Date: "+TextUtilities.toGMTString(new Date())+CRLF +
				"Cache-Control: no-store"+CRLF+
				"Pragma: no-cache"+CRLF+
				"Connection: close"+CRLF+
				"Server: Ether-GL (Java)" +CRLF +
				"Content-Type: application/x-rtsp-tunnelled"+CRLF+
				CRLF;
	}

	private String HTTP_bad_req() {
		return 
				"HTTP/1.0 400 Bad Request"+CRLF +
				"Date: "+TextUtilities.toGMTString(new Date())+CRLF +
				"Server: Ether-GL (Java)" +CRLF +
				"Connection: close"+CRLF+
				CRLF;
	}

	public String get(String key) {
		return request.get(key);
	}

	@Override
	public String toString() {
		return "-------------------------------------------\n#" + label + ": ";
	}

	public int getSessionKey() {
		if(sessionKey < 0)
			sessionKey = (int) (Math.random() * 1000000);
		return sessionKey;
	}

	public int getCSeq() {
		return cSeq;
	}

	public InetAddress getClientIP() {
		return clientIP;
	}
}
