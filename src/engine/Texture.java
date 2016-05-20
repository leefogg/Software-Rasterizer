package engine;

import engine.math.Color;

public abstract class Texture {
	public static final ColorTexture error = new ColorTexture(new Color(java.awt.Color.pink.getRGB()));
	
	public abstract Color map(float tu, float tv);
}
