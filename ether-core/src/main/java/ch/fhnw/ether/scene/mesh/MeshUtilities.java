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

package ch.fhnw.ether.scene.mesh;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.fhnw.ether.image.IGPUImage;
import ch.fhnw.ether.scene.mesh.IMesh.Flag;
import ch.fhnw.ether.scene.mesh.IMesh.Primitive;
import ch.fhnw.ether.scene.mesh.IMesh.Queue;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.IGeometryAttribute;
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial;
import ch.fhnw.ether.scene.mesh.material.ColorMaterial;
import ch.fhnw.ether.scene.mesh.material.IMaterial;
import ch.fhnw.ether.scene.mesh.material.ShadedMaterial;
import ch.fhnw.util.ArrayUtilities;
import ch.fhnw.util.FloatList;
import ch.fhnw.util.color.RGB;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Vec3;

public final class MeshUtilities {
	
	//@formatter:off

	public static final float[] UNIT_QUAD_TRIANGLES = { 
			-1, -1, 0,   1, -1, 0,    1, 1, 0,
			-1, -1, 0,   1,  1, 0,   -1, 1, 0 
	};

	public static final float[] UNIT_QUAD_NORMALS = { 
			0, 0, 1, 	0, 0, 1, 	0, 0, 1,
			0, 0, 1, 	0, 0, 1, 	0, 0, 1
	};

	public static final float[] UNIT_QUAD_TEX_COORDS = { 
			0, 0, 	1, 0, 	1, 1,
			0, 0, 	1, 1, 	0, 1 
	};
	

	public static final float[] UNIT_CUBE_TRIANGLES = {
			// bottom
			-0.5f, -0.5f, -0.5f, -0.5f, +0.5f, -0.5f, +0.5f, +0.5f, -0.5f,
			-0.5f, -0.5f, -0.5f, +0.5f, +0.5f, -0.5f, +0.5f, -0.5f, -0.5f,

			// top
			+0.5f, -0.5f, +0.5f, +0.5f, +0.5f, +0.5f, -0.5f, +0.5f, +0.5f, 
			+0.5f, -0.5f, +0.5f, -0.5f, +0.5f, +0.5f, -0.5f, -0.5f, +0.5f,

			// front
			-0.5f, -0.5f, -0.5f, +0.5f, -0.5f, -0.5f, +0.5f, -0.5f, +0.5f, 
			-0.5f, -0.5f, -0.5f, +0.5f, -0.5f, +0.5f, -0.5f, -0.5f, +0.5f,

			// back
			+0.5f, +0.5f, -0.5f, -0.5f, +0.5f, -0.5f, -0.5f, +0.5f, +0.5f, 
			+0.5f, +0.5f, -0.5f, -0.5f, +0.5f, +0.5f, +0.5f, +0.5f, +0.5f,

			// left
			-0.5f, +0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, +0.5f, 
			-0.5f, +0.5f, -0.5f, -0.5f, -0.5f, +0.5f, -0.5f, +0.5f, +0.5f,

			// right
			+0.5f, -0.5f, -0.5f, +0.5f, +0.5f, -0.5f, +0.5f, +0.5f, +0.5f, 
			+0.5f, -0.5f, -0.5f, +0.5f, +0.5f, +0.5f, +0.5f, -0.5f, +0.5f 
	};

	public static final float[] UNIT_CUBE_NORMALS = IGeometry.createNormals(UNIT_CUBE_TRIANGLES);

	public static final float[] UNIT_CUBE_EDGES = {
			// bottom
			-0.5f, -0.5f, -0.5f, -0.5f, +0.5f, -0.5f, 
			-0.5f, +0.5f, -0.5f, +0.5f, +0.5f, -0.5f,
			+0.5f, +0.5f, -0.5f, +0.5f, -0.5f, -0.5f, 
			+0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f,

			// top
			+0.5f, -0.5f, +0.5f, +0.5f, +0.5f, +0.5f, 
			+0.5f, +0.5f, +0.5f, -0.5f, +0.5f, +0.5f, 
			-0.5f, +0.5f, +0.5f, -0.5f, -0.5f, +0.5f, 
			-0.5f, -0.5f, +0.5f, +0.5f, -0.5f, +0.5f,

			// vertical
			-0.5f, -0.5f, -0.5f, -0.5f, -0.5f, +0.5f, 
			+0.5f, -0.5f, -0.5f, +0.5f, -0.5f, +0.5f, 
			+0.5f, +0.5f, -0.5f, +0.5f, +0.5f, +0.5f, 
			-0.5f, +0.5f, -0.5f, -0.5f, +0.5f, +0.5f 
	};

	public static final float[] UNIT_CUBE_POINTS = { 
			-0.5f, -0.5f, -0.5f, -0.5f, +0.5f, -0.5f, 
			+0.5f, +0.5f, -0.5f, +0.5f, -0.5f, -0.5f,
			+0.5f, -0.5f, +0.5f, +0.5f, +0.5f, +0.5f, 
			-0.5f, +0.5f, +0.5f, -0.5f, -0.5f, +0.5f
	};

	public static final float[] UNIT_CUBE_TEX_COORDS = { 
			// bottom
			0, 0, 1, 0, 1, 1,
			0, 0, 1, 1, 0, 1,
			// top
			0, 0, 1, 0, 1, 1,
			0, 0, 1, 1, 0, 1,
			// front
			0, 0, 1, 0, 1, 1,
			0, 0, 1, 1, 0, 1,
			// back
			0, 0, 1, 0, 1, 1,
			0, 0, 1, 1, 0, 1,
			// left
			0, 0, 1, 0, 1, 1,
			0, 0, 1, 1, 0, 1, 
			// right
			0, 0, 1, 0, 1, 1,
			0, 0, 1, 1, 0, 1,
	};

	public static final float[] DEFAULT_UP_NORMAL = {
		0, 0, 1
	};
	
	//@formatter:on
	
	public static IMesh createQuad() {
		return createQuad(new ColorMaterial(RGBA.WHITE), Queue.DEPTH, IMesh.NO_FLAGS);
	}

	public static IMesh createQuad(IMaterial material) {
		return createQuad(material, Queue.DEPTH, IMesh.NO_FLAGS);
	}

	public static IMesh createQuad(IMaterial material, Queue queue, Set<Flag> flags) {
		float[] v = UNIT_QUAD_TRIANGLES;
		float[] n = UNIT_QUAD_NORMALS;
		float[] m = UNIT_QUAD_TEX_COORDS;
		IGeometry g = requireTexCoords(material) ? DefaultGeometry.createVNM(v, n, m) :  DefaultGeometry.createVN(v, n);
		return new DefaultMesh(Primitive.TRIANGLES, material, g, queue, flags);
	}

	public static IMesh createCube() {
		return createCube(new ColorMaterial(RGBA.WHITE), Queue.DEPTH, IMesh.NO_FLAGS);
	}

	public static IMesh createCube(IMaterial material) {
		return createCube(material, Queue.DEPTH, IMesh.NO_FLAGS);
	}

	public static IMesh createCube(IMaterial material, Queue queue, Set<Flag> flags) {
		float[] v = UNIT_CUBE_TRIANGLES;
		float[] n = UNIT_CUBE_NORMALS;
		float[] m = UNIT_CUBE_TEX_COORDS;
		IGeometry g = requireTexCoords(material) ? DefaultGeometry.createVNM(v, n, m) :  DefaultGeometry.createVN(v, n);
		return new DefaultMesh(Primitive.TRIANGLES, material, g, queue, flags);
	}
	
	public static IMesh createDisk(int n) {
		return createDisk(new ColorMaterial(RGBA.WHITE), n);
	}

	public static IMesh createDisk(IMaterial material, int n) {
		if (requireTexCoords(material))
			throw new IllegalArgumentException("texture coordinates unsupported");
		if (n < 3)
			throw new IllegalArgumentException("n < 3");
		float[] v = new float[3 * 3 * n];
		int j = 0;
		for (int i = 0; i < n; ++i) {
			v[j++] = 0;
			v[j++] = 0;
			v[j++] = 0;
			v[j++] = 0.5f * (float)Math.cos(Math.PI * 2 * i / n);
			v[j++] = 0.5f * (float)Math.sin(Math.PI * 2 * i / n);
			v[j++] = 0;
			v[j++] = 0.5f * (float)Math.cos(Math.PI * 2 * (i + 1) / n);
			v[j++] = 0.5f * (float)Math.sin(Math.PI * 2 * (i + 1) / n);
			v[j++] = 0;
		}
		return new DefaultMesh(Primitive.TRIANGLES, material, DefaultGeometry.createVN(v, DEFAULT_UP_NORMAL));
	}
	
	public static IMesh createCylinder(int n, boolean addCaps) {
		return createCylinder(new ColorMaterial(RGBA.WHITE), n, addCaps);
	}
	
	public static IMesh createCylinder(IMaterial material, int n, boolean addCaps) {
		if (requireTexCoords(material))
			throw new IllegalArgumentException("texture coordinates unsupported");
		if (n < 3)
			throw new IllegalArgumentException("n < 3");
		float[] v = new float[6 * 3 * n + (addCaps ? 2 * 3 * 3 * n : 0)];
		int s = 0;
		int t = 6 * 3 * n;
		int b = 6 * 3 * n + 3 * 3 * n;
		for (int i = 0; i < n; ++i) {
			float px0 = 0.5f * (float)Math.cos(Math.PI * 2 * i / n);
			float py0 = 0.5f * (float)Math.sin(Math.PI * 2 * i / n);
			float px1 = 0.5f * (float)Math.cos(Math.PI * 2 * (i + 1) / n);
			float py1 = 0.5f * (float)Math.sin(Math.PI * 2 * (i + 1) / n);
			
			v[s++] = px0;
			v[s++] = py0;
			v[s++] = -0.5f;
			v[s++] = px1;
			v[s++] = py1;
			v[s++] = 0.5f;
			v[s++] = px0;
			v[s++] = py0;
			v[s++] = 0.5f;
			
			v[s++] = px0;
			v[s++] = py0;
			v[s++] = -0.5f;
			v[s++] = px1;
			v[s++] = py1;
			v[s++] = -0.5f;
			v[s++] = px1;
			v[s++] = py1;
			v[s++] = 0.5f;
			
			if (addCaps) {
				// top
				v[t++] = 0;
				v[t++] = 0;
				v[t++] = 0.5f;
				v[t++] = px0;
				v[t++] = py0;
				v[t++] = 0.5f;
				v[t++] = px1;
				v[t++] = py1;
				v[t++] = 0.5f;
				// bottom
				v[b++] = 0;
				v[b++] = 0;
				v[b++] = -0.5f;
				v[b++] = px1;
				v[b++] = py1;
				v[b++] = -0.5f;
				v[b++] = px0;
				v[b++] = py0;
				v[b++] = -0.5f;
			}
		}
		return new DefaultMesh(Primitive.TRIANGLES, material, DefaultGeometry.createVN(v, null));
	}

	public static IMesh createGroundPlane(float extent) {
		return createGroundPlane(new ShadedMaterial(RGB.WHITE), extent);
	}

	public static IMesh createGroundPlane(IMaterial material, float extent) {
		float e = extent;
		float z = 0;
		float[] v = { -e, -e, z, e, -e, z, e, e, z, -e, -e, z, e, e, z, -e, e, z };
		float[] n = UNIT_QUAD_NORMALS;
		float[] m = UNIT_QUAD_TEX_COORDS;
		IGeometry g = requireTexCoords(material) ? DefaultGeometry.createVNM(v, n, m) :  DefaultGeometry.createVN(v, n);
		return new DefaultMesh(Primitive.TRIANGLES, material, g, Queue.TRANSPARENCY,  Flag.DONT_CAST_SHADOW);
	}

	public static IMesh createScreenRectangle(float x0, float y0, float x1, float y1, RGBA color, IGPUImage colorMap) {
		IMaterial material = colorMap == null ? new ColorMaterial(color) : new ColorMapMaterial(color, colorMap);
		float[] vertices = { x0, y0, 0, x1, y0, 0, x1, y1, 0, x0, y0, 0, x1, y1, 0, x0, y1, 0 };
		return new DefaultMesh(Primitive.TRIANGLES, material, DefaultGeometry.createVNM(vertices, UNIT_QUAD_NORMALS, UNIT_QUAD_TEX_COORDS), Queue.SCREEN_SPACE_OVERLAY);
	}
	
	private static boolean requireTexCoords(IMaterial material) {
		return ArrayUtilities.contains(material.getRequiredAttributes(), IGeometry.COLOR_MAP_ARRAY);
	}

	public static void addLine(List<Vec3> dst, float x0, float y0, float x1, float y1) {
		dst.add(new Vec3(x0, y0, 0));
		dst.add(new Vec3(x1, y1, 0));
	}

	public static void addLine(List<Vec3> dst, float x0, float y0, float z0, float x1, float y1, float z1) {
		dst.add(new Vec3(x0, y0, z0));
		dst.add(new Vec3(x1, y1, z1));
	}

	public static void addRectangle(List<Vec3> dst, float x0, float y0, float x1, float y1) {
		addRectangle(dst, x0, y0, x1, y1, 0);
	}

	public static void addRectangle(List<Vec3> dst, float x0, float y0, float x1, float y1, float z) {
		dst.add(new Vec3(x0, y0, z));
		dst.add(new Vec3(x1, y0, z));
		dst.add(new Vec3(x1, y1, z));

		dst.add(new Vec3(x0, y0, z));
		dst.add(new Vec3(x1, y1, z));
		dst.add(new Vec3(x0, y1, z));
	}

	public static void addCube(List<Vec3> dst, float tx, float ty, float tz, float sx, float sy, float sz) {
		for (int i = 0; i < UNIT_CUBE_TRIANGLES.length; i += 3) {
			dst.add(new Vec3((UNIT_CUBE_TRIANGLES[i] * sx) + tx, (UNIT_CUBE_TRIANGLES[i + 1] * sy) + ty, (UNIT_CUBE_TRIANGLES[i + 2] * sz) + tz));
		}
	}

	/**
	 * Merges a list of meshes by material. Note that materials are compared by
	 * reference.
	 */
	public static List<IMesh> mergeMeshes(Collection<IMesh> meshes) {
		int maxNumAttributes = 0;
		for (IMesh mesh : meshes)
			maxNumAttributes = Math.max(maxNumAttributes, mesh.getMaterial().getRequiredAttributes().length);
		
		FloatList[] buffers = new FloatList[maxNumAttributes];
		for (int i = 0; i < maxNumAttributes; ++i)
			buffers[i] = new FloatList();		
		
		// create a set from list (because of removeAll at the end)
		Set<IMesh> set = new HashSet<>(meshes);

		final List<IMesh> result = new ArrayList<>();		
		while (!set.isEmpty()) {
			final IMesh first = set.iterator().next();
			List<IMesh> same = set
					.stream()
					.filter(m -> 
						m.getType().equals(first.getType()) &&
						m.getQueue().equals(first.getQueue()) && 
						m.getFlags().equals(first.getFlags()) &&
						m.getMaterial().equals(first.getMaterial()))
					.collect(Collectors.toList());

			IMaterial material = first.getMaterial();
			IGeometryAttribute[] attributes = material.getRequiredAttributes();
			FloatList data[] = new FloatList[attributes.length];
			for (int i = 0; i < data.length; ++i) {
				data[i] = buffers[i];
				data[i].clear();
			}

			for (IMesh mesh : same) {
				IGeometryAttribute[] ga = mesh.getGeometry().getAttributes();
				float[][] gd = mesh.getTransformedGeometryData();
				for (int i = 0; i < attributes.length; ++i) {
					for (int j = 0; j < ga.length; j++) {
						if (attributes[i].id().equals(ga[j].id())) {
							data[i].addAll(gd[j]);
						}
					}
				}
			}
			result.add(new DefaultMesh(first.getType(), material, new DefaultGeometry(attributes, data), first.getQueue(), first.getFlags()));
			set.removeAll(same);
		}
		return result;
	}	
}
