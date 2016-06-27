package ui;
import static engine.Rasterizer.*;

import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.io.IOException;

import engine.Camera;
import engine.Rasterizer;
import engine.math.Vector3;
import engine.models.Mesh;
import engine.models.Materials.ImageTexture;
import engine.models.Materials.Shading.SliceShader;
import resources.loaders.OBJLoader;
import utils.FrameCounter;
import utils.Log;

public class Viewport extends Canvas implements MouseWheelListener {
	private static final long serialVersionUID = -869334646261735255L;
	
	private FrameCounter fc = new FrameCounter();

	private BufferStrategy buffer;

	private Rasterizer renderer;
	private Camera camera;
	
	Mesh model2, model1;

	public Viewport() {
		setIgnoreRepaint(true);
		setSize(Window.width, Window.height);

		addMouseWheelListener(this);
	}

	SliceShader shader = new SliceShader(.2f);
	public void start() {
		createBufferStrategy(2);
		buffer = getBufferStrategy();
		
		try {
			Log.init();
		} catch (IOException e) {
			System.err.println("Failed to open log file for writing.");
			e.printStackTrace();
		}

		camera = new Camera(0.9f, 320, 240, 1f, 10f);
		renderer = new Rasterizer(camera.getWidth(), camera.getHeight());
		renderer.setClearColor(0xFF000000);
		renderer.enable(GL_CULL_FACE);
		renderer.cullFace(GL_BACK);
		renderer.setBlendFunction(GL_FUNC_SET);
		renderer.setDepthFunction(GL_LESS);
		
		camera.setPosition(0, 3, 10);
		camera.setTarget(0, 1, 0f);
		
		try {
			String localdir =  System.getProperty("user.dir").replaceAll("\\\\", "/");
			
			model1 = OBJLoader.load(localdir + "/res/floor.obj");
			ImageTexture floortex = (ImageTexture)model1.texture;
			floortex.repeatX = 10;
			floortex.repeatY = 10;
			//model1.shader = new AmbientLightShader(light, 2);
			
			model2 = OBJLoader.load(localdir + "/res/glados.obj");
			model2.setPosition(0,1,0);
			//model2.shader = shader;
		} catch (Exception e) {
			System.out.println("An error accured loading resources.");
			e.printStackTrace();
			System.exit(1);
		}
		
		System.gc();
		
		Graphics2D graphics = (Graphics2D)buffer.getDrawGraphics();
		while(true) {
			tick();
			
			renderer.clear(GL_BUFFER | GL_DEPTH);
			
			renderer.render(model1, camera);
			renderer.render(model2, camera);
			
			renderer.swapBuffers();
			//graphics.clearRect(0, 0, Window.width, Window.height);
			graphics.drawImage(renderer.getFrameBuffer(), 0, 0, Window.width, Window.height, this);
			
			graphics.setColor(java.awt.Color.green);
			graphics.drawString("FPS: " + fc.fps, 5, 15);

			if(!buffer.contentsLost())
				buffer.show();
			
			fc.newFrame();
		} 
	}

	float sincos = 0;
	float stepsize = (float)Math.PI*2/1000f;
	float distance = 5;
	Vector3 light = new Vector3(0, 2.5f, 0);
	private void tick() {
		shader.offset += 0.005f;
		
		Vector3 cameraposition = camera.getPosition();
		cameraposition.z = (float)Math.cos(sincos) * distance;
		cameraposition.x = (float)Math.sin(sincos) * distance;
		camera.setPosition(cameraposition);
		/*

		light.z = (float)Math.cos(sincos) * distance;
		light.x = (float)Math.sin(sincos) * distance;
		*/
		
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
