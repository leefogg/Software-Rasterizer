package engine;

import engine.math.Vector3;

public class Face {
	public int A, B, C;
	public Vector3 normal;

	public Face(int a, int b, int c) {
		A = a;
		B = b;
		C = c;
	}
}