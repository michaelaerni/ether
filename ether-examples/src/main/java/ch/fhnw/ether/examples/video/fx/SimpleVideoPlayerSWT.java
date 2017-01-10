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

package ch.fhnw.ether.examples.video.fx;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;

import ch.fhnw.ether.audio.IAudioRenderTarget;
import ch.fhnw.ether.audio.IAudioSource;
import ch.fhnw.ether.audio.JavaSoundTarget;
import ch.fhnw.ether.audio.fx.AudioGain;
import ch.fhnw.ether.image.IHostImage;
import ch.fhnw.ether.image.IImage.ComponentFormat;
import ch.fhnw.ether.image.IImage.ComponentType;
import ch.fhnw.ether.media.AbstractFrameSource;
import ch.fhnw.ether.media.RenderProgram;
import ch.fhnw.ether.media.Sync;
import ch.fhnw.ether.platform.Platform;
import ch.fhnw.ether.platform.SWTImageSupport;
import ch.fhnw.ether.ui.ParameterWindow;
import ch.fhnw.ether.video.ArrayVideoSource;
import ch.fhnw.ether.video.CameraInfo;
import ch.fhnw.ether.video.CameraSource;
import ch.fhnw.ether.video.IVideoRenderTarget;
import ch.fhnw.ether.video.IVideoSource;
import ch.fhnw.ether.video.ImageTarget;
import ch.fhnw.ether.video.URLVideoSource;
import ch.fhnw.ether.video.fx.AbstractVideoFX;
import ch.fhnw.util.CollectionUtilities;

public class SimpleVideoPlayerSWT {
	public static void main(String[] args) throws Exception {
		Platform.get().init();

		AbstractFrameSource source;
		try {
			try {
				source = new URLVideoSource(new URL(args[0]));
			} catch(MalformedURLException e) {
				source = new URLVideoSource(new File(args[0]).toURI().toURL());
			}
		} catch(Throwable t) {
			source =  CameraSource.create(CameraInfo.getInfos()[0]);
		}
		IVideoSource   mask     = null; 
		ImageTarget    videoOut = new ImageTarget(true);
		try {mask = new ArrayVideoSource(new URLVideoSource(new File(args[1]).toURI().toURL(), 1), Sync.ASYNC);} catch(Throwable t) {}

		List<AbstractVideoFX> fxs = CollectionUtilities.asList(
				new RGBGain(),
				new AnalogTVFX(),
				new BandPass(),
				new Convolution(),
				new FadeToColor(),
				new FakeThermoCam(),
				new MotionBlur(),
				new Posterize());

		AtomicInteger current = new AtomicInteger(0);

		if(mask != null) {
			IHostImage maskOut  = IHostImage.create(mask.getWidth(), mask.getHeight(), ComponentType.BYTE, ComponentFormat.RGB);
			ImageTarget target = new ImageTarget(maskOut);
			target.setTimebase(videoOut);
			target.useProgram(new RenderProgram<>(mask));
			fxs.add(new ChromaKey(maskOut));
			target.start();
		}

		final RenderProgram<IVideoRenderTarget> video = new RenderProgram<>((IVideoSource)source, fxs.get(current.get()));

		new ParameterWindow(parent->{
			Combo fxsUI = new Combo(parent, SWT.READ_ONLY);
			for(AbstractVideoFX fx : fxs)
				fxsUI.add(fx.toString());
			fxsUI.setLayoutData(ParameterWindow.hfill());
			fxsUI.select(0);
			fxsUI.addSelectionListener(new SelectionListener() {
				@Override public void widgetSelected(SelectionEvent e) {widgetDefaultSelected(e);}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					int newIdx = fxsUI.getSelectionIndex();
					video.replace(fxs.get(current.get()), fxs.get(newIdx));
					current.set(newIdx);
				}
			}
					);
		}, video);

		videoOut.useProgram(video);

		if(source instanceof IAudioSource) {
			RenderProgram<IAudioRenderTarget> audio = new RenderProgram<>((IAudioSource)source, new AudioGain()); 
			JavaSoundTarget audioOut = new JavaSoundTarget(((IAudioSource)source), 2 / source.getFrameRate());
			audioOut.useProgram(audio);
			videoOut.setTimebase(audioOut);
			audioOut.start();
		}

		videoOut.start();

		Shell shell = new Shell();
		shell.setText("Simple Video Player");
		shell.setSize(1024, 512);
		shell.setVisible(true);
		shell.addPaintListener(e->{
			try {
				Image image = new Image(shell.getDisplay(), SWTImageSupport.toImageData(videoOut.getImage()));
				e.gc.drawImage(image, 0, 0);
				image.dispose();
				shell.redraw();
			} catch(Throwable t) {}
		});

		Platform.get().run();
	}

}
