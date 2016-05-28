package engine;

import engine.math.Color;
import engine.models.Texture;

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
