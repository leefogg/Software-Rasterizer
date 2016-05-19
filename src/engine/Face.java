package engine;

import engine.math.Vector3;

public class Face {
	public int vertexA, vertexB, vertexC;
	public Vector3 normal;

	public Face(int a, int b, int c) {
		vertexA = a;
		vertexB = b;
		vertexC = c;
	}
}