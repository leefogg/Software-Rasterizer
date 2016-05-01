package utils;
import java.util.Timer;
import java.util.TimerTask;

public class FrameCounter extends TimerTask {
	public  int fps = 0;
	private int fpstemp = 0;
	
	public FrameCounter() {
		new Timer().scheduleAtFixedRate(this, 0, 1000);
	}
	
	public void newFrame() {fpstemp++;}
	
	public void run() {
		fps = fpstemp;
		fpstemp=0;
	}
}