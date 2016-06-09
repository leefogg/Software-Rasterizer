package engine.math;

public class Point2D {
	int x, y;

	public Point2D(int x, int y) {
		set(x, y);
	}
	
	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Point2D Clone() {
		return new Point2D(x,y);
	}
}
