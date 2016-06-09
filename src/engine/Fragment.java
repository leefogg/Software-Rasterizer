package engine;

import engine.math.AABB;
import engine.math.Point2D;
import engine.math.Vector3;
import engine.models.Texture;
import engine.models.Vertex;

public final class Fragment {
	public Vertex
	worldtop,
	worldmiddle,
	worldbottom,
	screentop,
	screenmiddle,
	screenbottom;
	public Texture texture;
	private AABB boundingbox = new AABB();
	
	public Fragment() {}
	
	public final void set(Vertex worldv1, Vertex worldv2, Vertex worldv3, Vertex screenv1, Vertex screenv2, Vertex screenv3, Texture texture) {
		this.texture = texture;
		
		// Sort verts by height, v1 at top
		if (screenv1.position.y > screenv2.position.y) {
			Vertex temp = screenv2;
			screenv2 = screenv1;
			screenv1 = temp;
		}
			
		if (screenv2.position.y > screenv3.position.y) {
			Vertex temp = screenv2;
			screenv2 = screenv3;
			screenv3 = temp;
				
			if (screenv1.position.y > screenv2.position.y) {
				temp = screenv2;
				screenv2 = screenv1;
				screenv1 = temp;
			}
		}
		if (worldv1.position.y > worldv2.position.y) {
			Vertex temp = worldv2;
			worldv2 = worldv1;
			worldv1 = temp;
		}
			
		if (worldv2.position.y > worldv3.position.y) {
			Vertex temp = worldv2;
			worldv2 = worldv3;
			worldv3 = temp;
				
			if (worldv1.position.y > worldv2.position.y) {
				temp = worldv2;
				worldv2 = worldv1;
				worldv1 = temp;
			}
		}
		
		calculateboundingBox(screenv1.position, screenv2.position, screenv3.position);
		
		this.worldtop 		= worldv1;
		this.worldmiddle	= worldv2;
		this.worldbottom 	= worldv3;
		this.screentop 		= screenv1;
		this.screenmiddle	= screenv2;
		this.screenbottom 	= screenv3;
	}
	
	private void calculateboundingBox(Vector3 v1, Vector3 v2, Vector3 v3) {
		float 
		minx = Float.MAX_VALUE,
		maxx = Float.MIN_VALUE,
		miny = Float.MAX_VALUE,
		maxy = Float.MIN_VALUE;
		
		if (v1.x < minx) minx = v1.x;
		if (v1.x > maxx) maxx = v1.x; 
		if (v1.y < miny) miny = v1.y;
		if (v1.y > maxy) maxy = v1.y;
		
		if (v2.x < minx) minx = v2.x;
		if (v2.x > maxx) maxx = v2.x; 
		if (v2.y < miny) miny = v2.y;
		if (v2.y > maxy) maxy = v2.y;
		
		if (v3.x < minx) minx = v3.x;
		if (v3.x > maxx) maxx = v3.x; 
		if (v3.y < miny) miny = v3.y;
		if (v3.y > maxy) maxy = v3.y;
		
		boundingbox.set((int)minx, (int)miny, (int)(maxx-minx), (int)(maxy-miny));
	}
	
	public boolean isOnScreen(AABB screen) {
		return boundingbox.intersects(screen);
	}
	
	public boolean middleOnLeft() {
		// Calculate slopes. How many pixels to move across to move down by 1 pixel
		// TODO: Is there a faster way to determine if the middle vertex is on the right?
		float toptomiddlexslope = (screenmiddle.position.x - screentop.position.x) / (screenmiddle.position.y - screentop.position.y);
		float toptobottomxslope = (screenbottom.position.x - screentop.position.x) / (screenbottom.position.y - screentop.position.y);
		return toptomiddlexslope < toptobottomxslope;
	}
	
	public boolean middleOnRight() {
		// Calculate slopes. How many pixels to move across to move down by 1 pixel
		// TODO: Is there a faster way to determine if the middle vertex is on the right?
		float toptomiddlexslope = (screenmiddle.position.x - screentop.position.x) / (screenmiddle.position.y - screentop.position.y);
		float toptobottomxslope = (screenbottom.position.x - screentop.position.x) / (screenbottom.position.y - screentop.position.y);
		return toptomiddlexslope > toptobottomxslope;
	}
}
