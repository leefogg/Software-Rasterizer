package ui;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import engine.Camera;
import engine.Rasterizer;
import engine.math.Vector3;
import engine.models.Mesh;
import engine.models.Texture;
import engine.models.Materials.ImageTexture;
import resources.loaders.OBJLoader;
import utils.FrameCounter;
import utils.Log;
import static engine.Rasterizer.*;

public class Viewport extends Canvas implements MouseWheelListener {
	private static final long serialVersionUID = -869334646261735255L;

	private BufferStrategy buffer;

	private FrameCounter fc = new FrameCounter();

	private Rasterizer renderer;
	private Camera camera = new Camera(new Vector3(0,2,5), new Vector3(0,1,0));
	
	Mesh glados, floor;

	public Viewport() {
		setIgnoreRepaint(true);
		setSize(Window.width, Window.height);

		addMouseWheelListener(this);
	}

	public void start() {
		createBufferStrategy(2);
		buffer = getBufferStrategy();
		
		try {
			Log.init();
		} catch (IOException e) {
			System.err.println("Failed to open log file for writing.");
			e.printStackTrace();
		}

		renderer = new Rasterizer(0.9f, Window.width, Window.height, 0.01f, 1f);
		renderer.setClearColor(0xFF000000);
		renderer.enable(GL_CULL_FACE);
		renderer.cullFace(GL_BACK);
		renderer.blendEquation(GL_FUNC_SET);
		try {
			String localdir =  System.getProperty("user.dir").replaceAll("\\\\", "/");
			floor = OBJLoader.load(localdir + "/res/floor.obj");
			ImageTexture screentex = (ImageTexture)floor.texture;
			screentex.repeatX = 10;
			screentex.repeatY = 10;
			
			glados = OBJLoader.load(localdir + "/res/glados.obj");
			glados.setPosition(0,1,0);
		} catch (Exception e) {
			System.out.println("An error accured loading resources.");
			e.printStackTrace();
			System.exit(1);
		}
		
		Graphics2D graphics = (Graphics2D)buffer.getDrawGraphics();
		while(true) {
			tick();
			
			renderer.clear(GL_BUFFER_BIT | GL_DEPTH_BIT);
			renderer.render(floor, camera);
			renderer.render(glados, camera);
			renderer.swapBuffers();
			//graphics.clearRect(0, 0, getWidth(), getHeight());
			graphics.drawImage(renderer.getFrameBuffer(), 0, 0, this);

			graphics.setColor(Color.green);
			graphics.drawString("FPS: " + String.valueOf(fc.fps), 5, 15);

			if(!buffer.contentsLost())
				buffer.show();

			fc.newFrame();
		} 
	}

	float sincos = 0;
	float stepsize = (float)Math.PI*2/1000f;
	float distance = 5;
	private void tick() {
		/*
		 */
		Vector3 pos = camera.getPosition();
		pos.x = (float)Math.cos(sincos*10) * distance;
		pos.z = (float)Math.sin(sincos*10) * distance;
		camera.setPosition(pos);
		sincos += stepsize;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() < 0)
			distance -= 0.1f;
		else
			distance += 0.1f;
	}
}
