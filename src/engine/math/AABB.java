package engine.math;

public class AABB {
	protected Point2D 
	position = new Point2D(0,0),
	size = new Point2D(0,0),
	max = new Point2D(0,0);

	public AABB() {
		position = new Point2D(0,0);
		size = new Point2D(0,0);
	}
	public AABB(Point2D position, Point2D size) {
		set(position, size);
	}
	public AABB(int x, int y, int width, int height) {
		set(x,y, width,height);
	}
	public AABB(Point2D[] points) {
		int 
		minx = Integer.MAX_VALUE,
		maxx = Integer.MIN_VALUE,
		miny = Integer.MAX_VALUE,
		maxy = Integer.MIN_VALUE;
		for (Point2D p : points) {
			if (p.x < minx)
				minx = p.x;
			if (p.y < miny)
				miny = p.y;
			if (p.x > maxx)
				maxx = p.x;
			if (p.y > maxy)
				maxy = p.y;
		}
		
		set(minx, miny, maxx-minx, maxy-miny);
	}
	
	public final void set(Point2D position, Point2D size) {
		set(position.x, position.y, size.x, size.y);
	}
	public final void set(int x, int y, int width, int height) {
		this.position.x = x;
		this.position.y = y;
		this.size.x = width;
		this.size.y = height;
		
		max.x = position.x+size.x;
		max.y = position.y+size.y;
	}

	public Point2D getPosition() {
		return position.Clone();
	}

	public Point2D getSize() {
		return size.Clone();
	}
	
	public Point2D getMax() { 
		return max.Clone();
	}

	public void setPosition(Point2D position) {
		set(position, size);
	}

	public void setSize(Point2D size) {
		set(position, size);
	}

	public AABB expand(AABB box) {
		Point2D
		boxmax = box.max,
		thismax = max,
		max = new Point2D(Math.max(thismax.x, boxmax.x), Math.max(thismax.y, boxmax.y)),
		min = new Point2D(Math.min(box.position.x, position.x), Math.min(box.position.y, position.y)),
		size = new Point2D(max.x-min.x, max.y-min.y);
		
		return new AABB(min, size);
	}
	
	public void expand(Point2D point) {
		expand(point.x, point.y);
	}
	public void expand(int x, int y) {
		if (x < position.x)
			position.x = x;
		if (x > position.x+size.x)
			size.x = x-position.x;
		
		if (y < position.y)
			position.x = y;
		if (y > position.y+size.y)
			size.y = y-position.y;
	}

	public boolean intersects(AABB box) {
		Point2D 
		thismax = getMax(),
		thatmax = box.getMax();
		if(box.position.x > thismax.x) return false;
		if(box.position.y > thismax.y) return false;
		if(thatmax.x < this.position.x) return false;
		if(thatmax.y < this.position.y) return false;
		
		return true;
	}
	
	public boolean isPointInside(Vector3 point) {
		if (point.x < position.x) 			return false;
		if (point.x > position.x+size.x) 	return false;
		if (point.y < position.y) 			return false;
		if (point.y > position.y+size.y) 	return false;
		
		return true;
	}
	
	public AABB union(AABB box) {
		if (!intersects(box)) return null;
		
		Point2D
		thismax = getMax(),
		boxmax = box.getMax(),
		base = new Point2D(Math.max(box.position.x, position.x), Math.max(box.position.y, position.y)),
		max = new Point2D(Math.min(boxmax.x, thismax.x), Math.min(boxmax.y, thismax.y));
		
		return new AABB(new Point2D(base.x, base.y), new Point2D(max.x-base.x, max.y-base.y));
	}
}