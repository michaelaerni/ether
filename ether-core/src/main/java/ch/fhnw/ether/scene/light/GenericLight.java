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
import ch.fhnw.util.color.RGB;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.Vec4;
import ch.fhnw.util.math.geometry.BoundingBox;

public class GenericLight implements ILight {

	public static final class LightSource {
		public enum Type {
			OFF, DIRECTIONAL_LIGHT, POINT_LIGHT, SPOT_LIGHT
		}

		private final Type type;

		private final Vec4 position;
		private final RGB ambient;
		private final RGB color;

		private final float range;

		private final Vec3 spotDirection;
		private final float spotCosCutoff;
		private final float spotExponent;


		public LightSource(Type type, Vec3 position, RGB ambient, RGB color, float range, Vec3 spotDirection, float spotCosCutoff, float spotExponent) {
			this.type = type;
			this.position = makePosition(type, position);
			this.ambient = ambient;
			this.color = color;
			this.range = range;
			this.spotDirection = spotDirection != null ? spotDirection : Vec3.Z_NEG;
			this.spotCosCutoff = spotCosCutoff;
			this.spotExponent = spotExponent;
		}

		public LightSource(LightSource source, Vec3 position) {
			this.type = source.type;
			this.position = makePosition(source.type, position);
			this.ambient = source.ambient;
			this.color = source.color;
			this.range = source.range;
			this.spotDirection = source.spotDirection;
			this.spotCosCutoff = source.spotCosCutoff;
			this.spotExponent = source.spotExponent;
		}

		private static Vec4 makePosition(Type type, Vec3 position) {
			if (type == Type.DIRECTIONAL_LIGHT) {
				position = position.normalize();
				return new Vec4(position.x, position.y, position.z, 0);
			}
			return new Vec4(position.x, position.y, position.z, 1);
		}

		public static LightSource directionalSource(Vec3 direction, RGB ambient, RGB color) {
			return new LightSource(Type.DIRECTIONAL_LIGHT, direction, ambient, color, 0, null, 0, 0);
		}

		public static LightSource pointSource(Vec3 position, RGB ambient, RGB color, float range) {
			return new LightSource(Type.POINT_LIGHT, position, ambient, color, range, null, 0, 0);
		}

		public static LightSource spotSource(Vec3 position, RGB ambient, RGB color, float range, Vec3 direction, float angle, float softness) {
			return new LightSource(Type.SPOT_LIGHT, position, ambient, color, range, direction, (float) Math.cos(Math.toRadians(angle)), 100 * softness);
		}

		public Type getType() {
			return type;
		}

		public Vec4 getPosition() {
			return position;
		}

		public RGB getAmbient() {
			return ambient;
		}

		public RGB getColor() {
			return color;
		}
		
		public float getRange() {
			return range;
		}

		public Vec3 getSpotDirection() {
			return spotDirection;
		}

		public float getSpotCosCutoff() {
			return spotCosCutoff;
		}

		public float getSpotExponent() {
			return spotExponent;
		}
	}

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
