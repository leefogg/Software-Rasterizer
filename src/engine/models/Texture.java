package engine.models;

import java.awt.image.BufferedImage;

import engine.math.Color;
import engine.models.Materials.ColorTexture;

public abstract class Texture {
	public static final ColorTexture error = new ColorTexture(new Color(java.awt.Color.magenta));
	
	public abstract Color map(float tu, float tv);
	
	public abstract BufferedImage toBufferedImage();
}
