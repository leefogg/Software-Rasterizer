package engine.models;

import engine.math.Color;

public class Material {

	public String name;
	public Color ambientColor = Color.white;
	public float transparency = 0;
	public Texture texture;
	
	public Material(String name) {
		this.name = name;
	}
}
