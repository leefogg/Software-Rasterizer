package engine.models.Materials;

import java.awt.image.BufferedImage;

import engine.math.Color;
import engine.models.Texture;

public final class ColorTexture extends Texture {
	private Color color;
	
	public ColorTexture(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color.clone();
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public final Color map(float tu, float tv) {
		return color;
	}

	@Override
	public final BufferedImage toBufferedImage() {
		BufferedImage image = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, color.toARGB());
		return image;
	}

}
