package engine;

import engine.math.AABB;
import engine.math.Point2D;
import engine.math.Vector3;
import engine.models.Texture;
import engine.models.UVSet;
import engine.models.Vertex;

public final class Fragment {
	public Vertex
	worldtop,
	worldmiddle,
	worldbottom,
	screentop,
	screenmiddle,
	screenbottom;
	public UVSet
	topVertexUV,
	middleVertexUV,
	bottomVertexUV;
	public Texture texture;
	private AABB boundingbox = new AABB();
	
	public Fragment() {}
	
	public final void set(Vertex worldv1, Vertex worldv2, Vertex worldv3, Vertex screenv1, Vertex screenv2, Vertex screenv3, UVSet uv1, UVSet uv2, UVSet uv3, Texture texture) {
		this.texture = texture;
		
		// Sort verts by height, v1 at top
		Vertex tempvertex;
		UVSet tempuv;
		if (screenv1.position.y > screenv2.position.y) {
			tempvertex = screenv2;
			screenv2 = screenv1;
			screenv1 = tempvertex;
			
			tempuv = uv2;
			uv2 = uv1;
			uv1 = tempuv;
			
			tempvertex = worldv1;
			worldv1 = worldv2;
			worldv2 = tempvertex;
		}
			
		if (screenv2.position.y > screenv3.position.y) {
			tempvertex = screenv2;
			screenv2 = screenv3;
			screenv3 = tempvertex;
			
			tempuv = uv2;
			uv2 = uv3;
			uv3 = tempuv;
			
			tempvertex = worldv2;
			worldv2 = worldv3;
			worldv3 = tempvertex;
				
			if (screenv1.position.y > screenv2.position.y) {
				tempvertex = screenv2;
				screenv2 = screenv1;
				screenv1 = tempvertex;
				
				tempuv = uv2;
				uv2 = uv1;
				uv1 = tempuv;
				
				tempvertex = worldv1;
				worldv1 = worldv2;
				worldv2 = tempvertex;
			}
		}
		
		this.worldtop 		= worldv1;
		this.worldmiddle	= worldv2;
		this.worldbottom 	= worldv3;
		this.screentop 		= screenv1;
		this.screenmiddle	= screenv2;
		this.screenbottom 	= screenv3;
		this.topVertexUV = uv1;
		this.middleVertexUV = uv2;
		this.bottomVertexUV = uv3;
	}
	
	public void calculateboundingBox() {
		float 
		minx = Float.MAX_VALUE,
		maxx = Float.MIN_VALUE,
		miny = Float.MAX_VALUE,
		maxy = Float.MIN_VALUE;
		
		Vector3 
		v1 = screentop.position,
		v2 = screenmiddle.position,
		v3 = screenbottom.position;
		
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
	
	public boolean anyVertexOnScreen(AABB screen) {
		if (screen.isPointInside(screentop.position)) 	 return false;
		if (screen.isPointInside(screenmiddle.position)) return false;
		if (screen.isPointInside(screenbottom.position)) return false;
		
		return true;
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
	
	public Fragment Clone() {
		Fragment f = new Fragment();
		f.boundingbox = boundingbox;
		f.texture = texture;
		f.screentop = screentop;
		f.screenmiddle = screenmiddle;
		f.screenbottom = screenbottom;
		f.worldtop = worldtop;
		f.worldmiddle = worldmiddle;
		f.worldbottom = worldbottom;
		
		return f;
	}
}
