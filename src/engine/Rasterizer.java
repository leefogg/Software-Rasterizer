package engine;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;

import engine.math.Color;
import engine.math.Matrix;
import engine.math.Vector3;
import engine.models.Face;
import engine.models.Mesh;
import engine.models.Texture;
import engine.models.Vertex;

public class Rasterizer {
	private class OrganizedTriangle {
		float
		ua, ub, uc, ud,
		va, vb, vc, vd;
	}
	private OrganizedTriangle triangle = new OrganizedTriangle();
	
	public int width, height;
	private BufferedImage framebuffer;
	private Color[] pixels;
	private float[] depthBuffer;
	
	public ArrayList<Mesh> meshes = new ArrayList<Mesh>();
	public Matrix 
	projectionMatrix,
	screenmatrix;
	
	public Rasterizer() {
		this(320, 240);
	}
	
	public Rasterizer(int width, int height) {
		this(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
	}
	public Rasterizer(BufferedImage buffer) {
		framebuffer = buffer;
		
		this.width = framebuffer.getWidth();
		this.height = framebuffer.getHeight();
		
		pixels = new Color[width * height];
		depthBuffer = new float[pixels.length];
		for (int i=0; i<pixels.length; i++)
			pixels[i] = new Color(0x00000000); // TODO: Set void color
			
		projectionMatrix = Matrix.PerspectiveFovLH(0.78f, (float)width / (float)height, 0.01f, 1f);
		screenmatrix = Matrix.scaling(-width, -height, 1).multiply(Matrix.translation(width / 2, height / 2, 1));
		
		
		clearDepthBuffer();
	}
	
	public void addMesh(Mesh m) {
		if (meshes.indexOf(m) == -1)
			meshes.add(m);
	}
	
	//TODO: Make all static like OpenGL
	
	public void clearFrameBuffer() {		
		for (Color c : pixels)
			c.set(0x00000000); // TODO: Set void color
	}
	
	public void clearDepthBuffer() {
		for (int i=0; i<depthBuffer.length; i++)
			depthBuffer[i] = Integer.MAX_VALUE;		
	}
	
	
	
	public void swapBuffers() {
		int[] outputpixels = ((DataBufferInt)framebuffer.getRaster().getDataBuffer()).getData(); // Pointer to buffer
		for (int i=0; i<pixels.length; i++)
			outputpixels[i] = pixels[i].toARGB();		
	}
	
	public BufferedImage getFrameBuffer() {
		return framebuffer; 
	}
	
	public BufferedImage getDepthBuffer() {
		BufferedImage depthbuffer = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				int depth = (int)map(depthBuffer[y*width + x], mindepth, maxdepth, 255, 0);
				int rgb = (0xFF << 24) | (depth << 16) | (depth << 8) | depth;
				depthbuffer.setRGB(x, y, rgb);
			}
		}
		
		return depthbuffer;
	}
	
	public void render(Camera cam) {
		for (Mesh mesh : meshes)
			render(mesh, cam);
		
		//System.out.println("Max:" + maxdepth + ", min:" + mindepth);
		// TODO: Get real distance from camera
	}
	
	private void render(Mesh mesh, Camera cam) {		
		Matrix worldview = Matrix.multiply(mesh.worldmatrix, cam.viewMatrix);
		Matrix transformmatrix = Matrix.multiply(worldview, projectionMatrix);
		
		// Scale to screen size and move to middle
		transformmatrix.multiply(screenmatrix);
		
		mesh.projectVertcies(transformmatrix);
		
		Vector3 transformednormal = new Vector3(0,0,0);
		for (Face face : mesh.faces) {
			Matrix.transformNormal(face.normal, transformmatrix, transformednormal);
			if (transformednormal.z > 0)
				continue;
			
			drawTriangle(
					mesh.transformedvertcies[face.vertex1],
					mesh.transformedvertcies[face.vertex2],
					mesh.transformedvertcies[face.vertex3],
					mesh.texture
					);
		}
	}
	
	/* Not yet implemented
	public void drawTriangle(Vertex v1, Vertex v2, Vertex v3, Texture tex) {
		// First sort the three vertices by y-coordinate ascending so v1 is the topmost vertex
		if (v1.position.y > v2.position.y) {
			Vertex temp = v2;
			v2 = v1;
			v1 = temp;
		}
		
		if (v2.position.y > v3.position.y) {
			Vertex temp = v2;
			v2 = v3;
			v3 = temp;
			
			if (v1.position.y > v2.position.y) {
				temp = v2;
				v2 = v1;
				v1 = temp;
			}
		}
		
		
		Vector3 p1 = v1.position;
		Vector3 p2 = v2.position;
		Vector3 p3 = v3.position;
	
	  //here we know that v1.y <= v2.y <= v3.y
	  //check for trivial case of bottom-flat triangle
	  if (p2.y == p3.y) {
		  fillBottomFlatTriangle(v1, v2, v3, tex);
	  } else if (p1.y == p2.y) { //check for trivial case of top-flat triangle
		  fillTopFlatTriangle(v1, v2, v3, tex);
	  } else { //general case - split the triangle in a topflat and bottom-flat one 
		  float posslopex = (p3.x - p1.x) / (p3.y - p1.y);
		  float posslopey = (p3.y - p1.y) / (p3.x - p1.x);
		  float uslope = (v3.textureCoordinates.x - v1.textureCoordinates.x) / (v3.textureCoordinates.y - v1.textureCoordinates.y);
		  float vslope = (v3.textureCoordinates.y - v1.textureCoordinates.y) / (v3.textureCoordinates.x - v1.textureCoordinates.x);
		  float dist = p2.y - p1.y;
		  Vertex v4 = new Vertex(
				new Vector3(
						posslopex * dist,
						posslopey * dist,
						v1.position.z
						),
				new Vector3(
						uslope * dist,
						vslope * dist,
						0
						)
		     );
		  fillBottomFlatTriangle(v1, v2, v4, tex);
		  fillTopFlatTriangle(v2, v4, v3, tex);
	  }
	}
	
	private void fillBottomFlatTriangle(Vertex v1, Vertex v2, Vertex v3, Texture tex) {
		  float leftslope = (v2.position.x - v1.position.x) / (v2.position.y - v1.position.y);
		  float rightslope = (v3.position.x - v1.position.x) / (v3.position.y - v1.position.y);

		  float startx = v1.position.x;
		  float endx = v1.position.x;

		  for (int scanlineY = (int)v1.position.y; scanlineY <= v2.position.y; scanlineY++) {
		    
		    startx += leftslope;
		    endx += rightslope;
		  }
		}
		
		private void fillTopFlatTriangle(Vertex v1, Vertex v2, Vertex v3, Texture tex) {
		  float leftxslope = (v3.position.x - v1.position.x) / (v3.position.y - v1.position.y);
		  float righxtslope = (v3.position.x - v2.position.x) / (v3.position.y - v2.position.y);
		  float leftslope = (v2.position.x - v1.position.x) / (v2.position.y - v1.position.y);
		  float rightslope = (v3.position.x - v1.position.x) / (v3.position.y - v1.position.y);

		  float startx = v3.position.x;
		  float endx = v3.position.x;

		  for (int scanlineY = (int)v3.position.y; scanlineY > v1.position.y; scanlineY--) {
			
		    startx -= leftslope;
		    endx -= rightslope;

		    drawLine(
		    		startx, scanlineY,
		    		interpolate(v1.textureCoordinates.getU(), v3.textureCoordinates.getU(), gradient)
		    	);
		  }
	}
	
	private void drawLine(int x, int y, float startu, float startv, float endu, float endv, int length, Texture tex) {
		float uslope = (endu - startu) / (endv - startv);
		float vslope = (endv - startv) / (endu - startu);
		float u = startu, v = startv;
		for (int i=0; i<length; i++) {
			setPixel(
					(int)x, y, 0,
					tex.map(u, v)
					);
			u += uslope;
			v += vslope;
		}
	}
	*/
	
	//TODO: Create fragment class containing all vertcies, UVs, Texture, depth and resulting pixel color
	public void drawTriangle(Vertex v1, Vertex v2, Vertex v3, Texture texture) {
		if (isInBounds(v1.position) || isInBounds(v2.position) || isInBounds(v3.position)) {
			// Sort verts by height, v1 at top
			if (v1.position.y > v2.position.y) {
				Vertex temp = v2;
				v2 = v1;
				v1 = temp;
			}
				
			if (v2.position.y > v3.position.y) {
				Vertex temp = v2;
				v2 = v3;
				v3 = temp;
					
				if (v1.position.y > v2.position.y) {
					temp = v2;
					v2 = v1;
					v1 = temp;
				}
			}
				
				
			Vector3 toppos = v1.position;
			int top = (int)toppos.y;
			if (top < 0)
				top = 0;
			Vector3 middlepos = v2.position;
			int middle = (int)middlepos.y;
			Vector3 bottompos = v3.position;
			int bottom = (int)bottompos.y;
			if (bottom > height)
				bottom = height;
				
			// Calculate slopes. How many pixels to move across to move down by 1 pixel 
			float toptomiddlexslope = (middlepos.x - toppos.x) / (middlepos.y - toppos.y);
			float toptobottomxslope = (bottompos.x - toppos.x) / (bottompos.y - toppos.y);
			
			if (toptomiddlexslope > toptobottomxslope) {
				for (int scanline = top; scanline < bottom; scanline++) {
					if (scanline < middle) {		
						processScanline(scanline, v1, v3, v1, v2, texture);
					} else {
						processScanline(scanline, v1, v3, v2, v3, texture);
					}
				}
			} else {
				for (int scanline = top; scanline < bottom; scanline++) {
					if (scanline < middle) {	
						processScanline(scanline, v1, v2, v1, v3, texture);
					} else {	
						processScanline(scanline, v2, v3, v1, v3, texture);
					}
				}
			}
		}
	}
	
	private void processScanline(int line, Vertex va, Vertex vb, Vertex vc, Vertex vd, Texture tex) {
		Vector3 p1 = va.position;
		Vector3 p2 = vb.position;
		Vector3 p3 = vc.position;
		Vector3 p4 = vd.position;

		// Calculate how far down each edge
		float leftslopepos =  (line - p1.y) / (p2.y - p1.y);
		leftslopepos = clamp(leftslopepos);
		float rightslopepos = (line - p3.y) / (p4.y - p3.y);
		rightslopepos = clamp(rightslopepos);
		
		float startx = 	interpolate(p1.x, p2.x, leftslopepos);
		float endx = 	interpolate(p3.x, p4.x, rightslopepos);
		
		float startz = 	interpolate(p1.z, p2.z, leftslopepos);
		float endz = 	interpolate(p3.z, p4.z, rightslopepos);

		float startu = 	interpolate(va.textureCoordinates.getU(), vb.textureCoordinates.getU(), leftslopepos);
		float endu = 	interpolate(vc.textureCoordinates.getU(), vd.textureCoordinates.getU(), rightslopepos);

		float startv = 	interpolate(va.textureCoordinates.getV(), vb.textureCoordinates.getV(), leftslopepos);
		float endv = 	interpolate(vc.textureCoordinates.getV(), vd.textureCoordinates.getV(), rightslopepos);

		float stepsize = 1f / (endx - startx); // Calculate percentage to increment from width
		
		float gradient = 0;
		// Clip start and end to screen
		if (startx < 0) {
			// Move start UV position to start from the on-screen position
			gradient += -startx * stepsize;
			startx = 0;
		}
		// End early if goes off screen
		if (endx > width)
			endx = width;
		
		for (float x = startx; x < endx; x++, gradient += stepsize) {
			float z = interpolate(startz, endz, gradient);
			float u = interpolate(startu, endu, gradient);
			float v = interpolate(startv, endv, gradient);

			// TODO: Calculate world position of pixel
			// TODO: Calculate distance from camera to pixel
			setPixel(
					(int)x, line, z,
					tex.map(u, v)
					);
		}
	}
	
	float 
	mindepth = Float.MAX_VALUE,
	maxdepth = Float.MIN_VALUE;
	private void setPixel(int x, int y, float z, Color color) {
		int pixelindex = y*width + x;
		
		if (depthBuffer[pixelindex] < z) 
			return;
		
		//TODO: Add znear and zfar
		if (z > maxdepth) maxdepth = z;
		if (z < mindepth) mindepth = z;
		
		depthBuffer[pixelindex] = z;
		// TOOD: Fix depth buffer values
		pixels[pixelindex].set(color);
	}
	
	private boolean isInBounds(Vector3 point) {
		return isInBounds(point.x, point.y);
	}
	private boolean isInBounds(float x, float y) {
		if (x < 0) 		return false;
		if (y < 0) 		return false;
		if (x >= width) return false;
		if (y >= height)return false;
		return true;
	}

	private float clamp(float value, float min, float max) {
		if (value > max) return max;
		if (value < min) return min;
		return value;
	}
	private float clamp(float value) {
		return clamp(value, 0, 1);
	}
	
	private float interpolate(float min, float max, float gradient) {
		return min + (max - min) * gradient;
	}
	
	private float map(float x, float in_min, float in_max, float out_min, float out_max) {
	  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}
	
	public void dispose() {
		framebuffer.flush();
		meshes.clear();
	}
}