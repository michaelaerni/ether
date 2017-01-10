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

package ch.fhnw.ether.render;

import java.util.List;

import ch.fhnw.ether.scene.attribute.AbstractAttribute;
import ch.fhnw.ether.scene.camera.IViewCameraState;
import ch.fhnw.ether.scene.light.ILight.LightSource;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.view.IView;

/**
 * Simple rendering interface.
 *
 * @author radar
 */
public interface IRenderer {
	enum ExecutionPolicy {
		SINGLE_THREADED, DUAL_THREADED, MULTI_THREADED
	}

	interface IRenderUpdate {
		void update();
	}

	interface IRenderTargetState {
		IView getView();

		IViewCameraState getViewCameraState();

		List<LightSource> getLights();

		List<Renderable> getRenderables();
		
		boolean hasPost();
	}

	interface IRenderState {
		List<IRenderUpdate> getRenderUpdates();

		List<IRenderTargetState> getRenderStates();
	}

	final class RendererAttribute<T> extends AbstractAttribute<T> {
		public RendererAttribute(String id) {
			super(id);
		}
	}

	/**
	 * Returns execution policy of this renderer.
	 */
	ExecutionPolicy getExecutionPolicy();

	/**
	 * Returns true if renderer is ready to accept a new render state, i.e.
	 * calling submit() will not block.
	 */
	boolean ready();

	/**
	 * Called from a client (usually a render manager) to submit a render state.
	 * Depending on execution policy, submit waits until rendering is complete
	 * (single threaded) or defers rendering to other threads (dual threaded,
	 * multi threaded) if they are ready.
	 */
	void submit(IRenderState state);

	/**
	 * Create new renderable from given mesh. Usually called from render manager
	 * on scene thread.
	 */
	Renderable createRenderable(IMesh mesh);

}
