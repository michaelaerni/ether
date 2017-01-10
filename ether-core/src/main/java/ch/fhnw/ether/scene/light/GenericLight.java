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

package ch.fhnw.ether.scene.light;

import ch.fhnw.util.UpdateRequest;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.BoundingBox;

public class GenericLight implements ILight {

	private String name = "unnamed_light";

	private LightSource lightSource;
	
	private UpdateRequest update = new UpdateRequest();

	protected GenericLight(LightSource lightSource) {
		this.lightSource = lightSource;
	}

	@Override
	public final BoundingBox getBounds() {
		// TODO: return correct bounding box (whatever that is/means)
		return null;
	}

	@Override
	public final Vec3 getPosition() {
		return new Vec3(lightSource.getPosition());
	}

	@Override
	public final void setPosition(Vec3 position) {
		lightSource = new LightSource(lightSource, position);
		updateRequest();
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final void setName(String name) {
		this.name = name;
		updateRequest();
	}

	@Override
	public final LightSource getLightSource() {
		return lightSource;
	}

	@Override
	public final void setLightSource(LightSource lightSource) {
		this.lightSource = lightSource;
		updateRequest();
	}
	
	@Override
	public final void setSpotDirection(Vec3 spotDirection) {
		lightSource = new LightSource(lightSource.type, new Vec3(lightSource.position), lightSource.ambient, lightSource.color, lightSource.range, spotDirection, lightSource.spotCosCutoff, lightSource.spotExponent);
		updateRequest();
	}

	@Override
	public final UpdateRequest getUpdater() {
		return update;
	}

	// we purposely leave equals and hashcode at default (identity)
	@Override
	public final boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public final int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	protected final void updateRequest() {
		update.request();
	}
}
