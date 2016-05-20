package engine;

import engine.math.Color;

public final class ColorTexture extends Texture {
	private Color color;
	
	public ColorTexture(Color color) {
		this.color = color;
	}

	@Override
	public Color map(float tu, float tv) {
		return color;
	}

}
