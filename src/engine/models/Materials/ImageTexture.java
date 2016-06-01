package engine.models.Materials;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import engine.math.Color;
import engine.models.Texture;

public final class ImageTexture extends Texture {

	private Color[] pixels;
	private int width, height;
	public float repeatX = 1, repeatY = 1;
	private int offsetX, offsetY;
	
	public ImageTexture(int width, int height) throws UnsupportedDimensionException {
		setDimension(width, height);
		
		this.pixels = new Color[width * height];
		for (int i=0; i<pixels.length; i++)
			pixels[i] = new Color(0xFFFFFFFF);
	}
	public ImageTexture(String path) throws IOException, UnsupportedDimensionException {
		this(ImageIO.read(new File(path)));
	}
	
	public ImageTexture(BufferedImage tex) throws UnsupportedDimensionException {
		setDimension(tex.getWidth(), tex.getHeight());
		
		this.pixels = new Color[width * height];
		int i=0;
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				int argb = tex.getRGB(x, y);
				this.pixels[i++] = new Color(argb);
			}
		}
	}
	
	public ImageTexture(int width, int height, Color[] buffer) throws UnsupportedDimensionException {
		setDimension(width, height);
		
		pixels = new Color[pixels.length];
		copy(buffer);
	}
	
	public void copy(Color[] buffer) {
		for (int i=0; i<Math.min(buffer.length, pixels.length); i++)
			pixels[i].set(buffer[i]);
	}
	
	public void setXOffset(int offset) {
		offsetX = offset % width;
	}
	public void setYOffset(int offset) {
		offsetY = offset % height;
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public int numberOfPixels() {
		return pixels.length;
	}
	
	public Color map(float tu, float tv) {
		tu = Math.abs(tu);
		tv = Math.abs(tv);
		int u = (int)(tu * width * repeatX) + offsetX;
		int v = (int)(tv * height * repeatY) + offsetY;
		u &= width-1;
		v &= height-1;
		Color pixel = pixels[v*width + u];
		return pixel;
	}

	@Override
	public BufferedImage toBufferedImage() {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				image.setRGB(x, y, pixels[y*width + x].toARGB());
			}
		}
		
		return image;
	}
	
	private void setDimension(int width, int height) throws UnsupportedDimensionException {
		if (!isPowerOfTwo(width) || !isPowerOfTwo(height))
			throw new UnsupportedDimensionException("Image dimension must be a power of two.");
		this.width = width;
		this.height = height;
	}
	private boolean isPowerOfTwo(int x) {
		return (x & (~x + 1)) == x;
	}
}
