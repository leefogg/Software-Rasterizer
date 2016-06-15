package engine.models;

import engine.math.Color;
import engine.math.Vector3;

public class Vertex {
	public Vector3 position = Vector3.zero;
	public Color color = Color.white;

	public Vertex(Vector3 position) {
		this.position = position;
	}

	
	public String toString() {
		return "Pos: " + position.toString();
	}
	
	public Vertex Clone() {
		Vertex vert = new Vertex(position.Clone());
		vert.color = color.clone();
		
		return vert;
	}
}