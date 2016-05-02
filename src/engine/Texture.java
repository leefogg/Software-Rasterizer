package engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Texture {
	public static final Texture error;
	static {
		BufferedImage errbmp = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
		Graphics2D canvas = errbmp.createGraphics();
		canvas.setColor(Color.magenta);
		canvas.fillRect(0, 0, errbmp.getWidth(), errbmp.getHeight());
		canvas.dispose();
		error = new Texture(errbmp);
	}
	
	private BufferedImage image;
	private int[] pixels;
	private int width, height;
	public float repeatX = 1, repeatY = 1;
	
	public Texture(String path) throws IOException {
		this(ImageIO.read(new File(path)));
	}
	
	public Texture(BufferedImage tex) {
		image = new BufferedImage(tex.getWidth(), tex.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D canvas = image.createGraphics();
		canvas.drawImage(tex, 0, 0, null);
		pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		width = tex.getWidth();
		height = tex.getHeight();
	}
	
	public int map(float tu, float tv) {
		int u = (int)Math.abs((tu * width * repeatX) % width);
		int v = (int)Math.abs((tv * height * repeatY) % height);
		return pixels[v*width+u];
	}
}