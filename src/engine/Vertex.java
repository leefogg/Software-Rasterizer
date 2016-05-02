package engine;

import engine.math.Color;
import engine.math.Vector3;

public class Vertex {
		public Vector3
		position,
		textureCoordinates,
		normal;
		public Color color = Color.white;

		public Vertex(Vector3 position) {
			this.position = position;
		}
		public Vertex(Vector3 position, Vector3 texturecoordinates) {
			this.position = position;
			this.textureCoordinates = texturecoordinates;
		}
		
		public String toString() {
			return "Pos: " + position.toString();
		}
	}