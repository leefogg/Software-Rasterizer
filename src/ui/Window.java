package ui;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;



public class Window {
	public static final int width = 640, height = 480;
	
	public static void main(String arg[]) {
		new Window("Software Rasterizer", width, height, new Viewport());
	}
	
	public Window(String title, int width, int height, Viewport contents) {
		JFrame frame = new JFrame();
		frame.setIgnoreRepaint(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
				utils.Log.dispose();
			}
		});
		frame.setResizable(false);
		frame.setTitle(title);
		frame.add(contents);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	    contents.start();
	}
}
