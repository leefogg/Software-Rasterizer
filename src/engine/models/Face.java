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
}