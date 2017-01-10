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

package ch.fhnw.ether.formats.obj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import ch.fhnw.ether.formats.ModelMaterial;
import ch.fhnw.ether.formats.ModelObject;
import ch.fhnw.util.Log;
import ch.fhnw.util.TextUtilities;
import ch.fhnw.util.color.RGB;

final class MtlParser {
	private static final Log log = Log.create();
	
	private final ModelObject object;
	private final String path;
	private final String file;
	
	private ModelMaterial currentMaterial;

	public MtlParser(ModelObject object, String path, String file) {
		this.object = object;
		this.path = path;
		this.file = file;
	}

	public void parse() {
		InputStream input = this.getClass().getResourceAsStream(path + file);
		if (input == null) {
			try {
				input = new FileInputStream(new File(path + file));
			} catch (Exception e) {
				log.warning("Could not open MTL file: '" + (path + file) + "'");
				return;
			}
		}

		int line = 1;
		try (BufferedReader in = new BufferedReader(new InputStreamReader(input))) {
			for (String string = null; (string = in.readLine()) != null;) {
				parseLine(string, line);
				line++;
			}
		} catch (Exception e) {
			throw new RuntimeException("Error reading MTL file: '" + path + "' (line " + line + ")");
		}
	}

	private void parseLine(String string, int line) {
		if ("".equals(string))
			return;

		String[] words = TextUtilities.tokens(string);

		if (words.length == 0 || words[0].startsWith("#"))
			return;

		switch (words[0]) {
		case "newmtl":
			currentMaterial = new ModelMaterial(words[1]);
			object.getMaterials().put(words[1], currentMaterial);
			break;
		case "Ka":
			currentMaterial.setAmbientColor(new RGB(Float.parseFloat(words[1]), Float.parseFloat(words[2]), Float.parseFloat(words[3])));
			break;
		case "Ke":
			currentMaterial.setEmissionColor(new RGB(Float.parseFloat(words[1]), Float.parseFloat(words[2]), Float.parseFloat(words[3])));
			break;
		case "Kd":
			currentMaterial.setDiffuseColor(new RGB(Float.parseFloat(words[1]), Float.parseFloat(words[2]), Float.parseFloat(words[3])));
			break;
		case "Ks":
			currentMaterial.setSpecularColor(new RGB(Float.parseFloat(words[1]), Float.parseFloat(words[2]), Float.parseFloat(words[3])));
			break;
		case "Ns":
			currentMaterial.setShininess(Float.parseFloat(words[1]));
			break;
		case "map_Kd":
			currentMaterial.setTexture(path, words[words.length - 1]);
			break;
		case "Ni":
		case "d":
		case "illum":
			break;
		default:
			log.info("Ignoring unknown MTL key '" + words[0] + "' (line " + line + ")");
		}
	}
}