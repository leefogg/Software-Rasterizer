package engine.models;

import engine.math.Color;
import engine.math.Vector3;

public class Vertex {
	public Vector3 position = Vector3.zero;
	public UVSet textureCoordinates = UVSet.zero;
	public Color color = Color.white;

	public Vertex(Vector3 position) {
		this.position = position;
	}
	public Vertex(Vector3 position, UVSet texturecoordinates) {
		this.position = position;
		this.textureCoordinates = texturecoordinates;
	}
	
	public String toString() {
		return "Pos: " + position.toString();
	}
	
	public Vertex Clone() {
		return new Vertex(position.Clone(), textureCoordinates.Clone());
	}
}