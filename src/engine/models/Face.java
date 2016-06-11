package engine.models;

import engine.math.Vector3;

public class Face {
	public int vertex1, vertex2, vertex3;
	public Vector3 normal;

	public Face(int a, int b, int c) {
		vertex1 = a;
		vertex2 = b;
		vertex3 = c;
	}
	
	public static final Vector3 getCenter(Vector3 v1, Vector3 v2, Vector3 v3, Vector3 out) {
		out.set(v1);
		out.add(v2);
		out.add(v3);
		out.divide(3);
		return out;
	}
}