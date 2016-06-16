package engine.models;

import engine.math.Vector3;

public class Face {
	public int vertex1, vertex2, vertex3;
	public UVSet uv1, uv2, uv3;
	public Vector3 
	center = new Vector3(),
	normal = new Vector3();

	public Face(int vertex1, int vertex2, int vertex3, UVSet uv1, UVSet uv2, UVSet uv3) {
		this.vertex1 = vertex1;
		this.vertex2 = vertex2;
		this.vertex3 = vertex3;
		this.uv1 = uv1;
		this.uv2 = uv2;
		this.uv3 = uv3;
	}
	
	public final void updateFaceNormal(Vector3 v1, Vector3 v2, Vector3 v3) {
		getNormal(v1, v2, v3, normal);
	}
	
	public final void updateFaceCenter(Vector3 v1, Vector3 v2, Vector3 v3) {
		getCenter(v1, v2, v3, center);
	}
	
	public static final Vector3 getCenter(Vector3 v1, Vector3 v2, Vector3 v3) {
		return getCenter(v1, v2, v3, new Vector3());
	}
	public static final Vector3 getCenter(Vector3 v1, Vector3 v2, Vector3 v3, Vector3 out) {
		out.set(v1);
		out.add(v2);
		out.add(v3);
		out.divide(3);
		return out;
	}
	
	// Reference: https://www.opengl.org/wiki/Calculating_a_Surface_Normal
	public static Vector3 getNormal(Vector3 v1, Vector3 v2, Vector3 v3) {
		return getNormal(v1, v2, v3, new Vector3());
	}
	public static Vector3 getNormal(Vector3 v1, Vector3 v2, Vector3 v3, Vector3 out) {
		Vector3 u = Vector3.subtract(v2, v1);
		Vector3 v = Vector3.subtract(v3, v1);
		out.x = (u.y * v.z) - (u.z * v.y);
		out.y = (u.z * v.x) - (u.x * v.z);
		out.z = (u.x * v.y) - (u.y * v.x);
		return out;
	}
}