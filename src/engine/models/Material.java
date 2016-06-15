package engine.models;

import engine.math.Color;

public class Material {

	public String name;
	public Color ambientColor = Color.white;
	public Texture texture;
	
	public Material(String name) {
		this.name = name;
	}
}
