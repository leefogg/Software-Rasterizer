package window;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.io.FileNotFoundException;
import java.io.IOException;

import engine.Camera;
import engine.Mesh;
import engine.Renderer;
import resources.loaders.objFile;
import utils.FrameCounter;
import utils.Log;
import utils.Vector3;


public class Viewport extends Canvas implements MouseWheelListener {
	private static final long serialVersionUID = -869334646261735255L;

	private BufferStrategy buffer;

	private FrameCounter fc = new FrameCounter();

	private Renderer renderer;
	private Camera camera = new Camera(new Vector3(0,3,5), new Vector3(0,1,0));


	public Viewport() {
		setIgnoreRepaint(true);
		setSize(640, 480);

		//		new Timer().scheduleAtFixedRate(new TimerTask() {
		//			@Override
		//			public void run() {
		//				repaint();
		//			}
		//		}, 0, 1000/30);

		addMouseWheelListener(this);
	}

	public void start() {
		createBufferStrategy(2);
		buffer = getBufferStrategy();
		
		try {
			Log.init();
		} catch (IOException e1) {
			System.err.println("Failed to open log file for writing.");
			e1.printStackTrace();
		}

		renderer = new Renderer(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(320,240));
		try {
			String localdir =  System.getProperty("user.dir").replaceAll("\\\\", "/");
			Mesh cube = objFile.load(localdir + "/res/glados.obj");
			cube.setPosition(new Vector3(0,1,0));
//			cube.texture.repeatX = 25;
//			cube.texture.repeatY = 25;
			renderer.addMesh(cube);
			
			Mesh floor = objFile.load(localdir + "/res/floor.obj");
			floor.texture.repeatX = 10;
			floor.texture.repeatY = 10;
			renderer.addMesh(floor);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Graphics2D graphics = null;

		while(true) {
			try {
				graphics = (Graphics2D)buffer.getDrawGraphics();
				graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
				graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
				renderer.render(camera);
				graphics.drawImage(renderer.output, 0, 0, getWidth(), getHeight(), this);

				graphics.setColor(Color.green);
				graphics.drawString(String.valueOf(fc.fps), 5, 15);

				if(!buffer.contentsLost())
					buffer.show();

				tick();
				fc.newFrame();
				Thread.yield();
			} finally {
				// release resources
				if(graphics != null) 
					graphics.dispose();

				renderer.dispose();
			}
		} 
	}

	private void tick() {
		Mesh m = renderer.meshes.get(0);
		m.setRotation(m.getRotation().add(0f, -0.01f, 0f));
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() < 0)
			camera.setPosition(camera.getPosition().subtract(0, 0, 0.1f));
		else
			camera.setPosition(camera.getPosition().add(0, 0, 0.1f));
	}
}