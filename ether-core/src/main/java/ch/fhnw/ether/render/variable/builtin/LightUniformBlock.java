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

package ch.fhnw.ether.render.variable.builtin;

import java.util.Collection;

import ch.fhnw.ether.render.IRenderer.RendererAttribute;
import ch.fhnw.ether.render.gl.FloatUniformBuffer;
import ch.fhnw.ether.render.variable.base.UniformBlock;
import ch.fhnw.ether.scene.camera.IViewCameraState;
import ch.fhnw.ether.scene.light.ILight.LightSource;

public final class LightUniformBlock extends UniformBlock {
	public static final RendererAttribute<Integer> ATTRIBUTE = new RendererAttribute<>("builtin.light_uniform_block");

	public static final int MAX_LIGHTS = 8;

	public static final int BLOCK_SIZE = MAX_LIGHTS * 20;

	private static final String DEFAULT_SHADER_NAME = "lightBlock";

	private static final float[] OFF_LIGHT = new float[20];

	public LightUniformBlock() {
		super(ATTRIBUTE, DEFAULT_SHADER_NAME);
	}

	public LightUniformBlock(String shaderName) {
		super(ATTRIBUTE, shaderName);
	}

	public static void loadUniforms(FloatUniformBuffer uniforms, Collection<LightSource> lights, IViewCameraState matrices) {
		uniforms.load((blockIndex, buffer) -> {
			for (LightSource light : lights) {
				float[] trss = new float[] { light.getType().ordinal(), light.getRange(), light.getSpotCosCutoff(), light.getSpotExponent() };
				buffer.put(trss);
				buffer.put(matrices.getViewMatrix().transform(light.getPosition()).toArray());
				buffer.put(light.getAmbient().toArray());
				buffer.put(0);
				buffer.put(light.getColor().toArray());
				buffer.put(0);
				buffer.put(matrices.getNormalMatrix().transform(light.getSpotDirection()).toArray());
				buffer.put(0);
			}
			for (int i = 0; i < LightUniformBlock.MAX_LIGHTS - lights.size(); ++i) {
				buffer.put(OFF_LIGHT);
			}
		});
	}
}
