package engine;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import engine.math.Color;
import engine.math.Matrix;
import engine.math.Vector3;
import engine.models.Face;
import engine.models.Mesh;
import engine.models.Texture;
import engine.models.Vertex;
import engine.models.Materials.ImageTexture;

import static engine.math.CommonMath.*;

public class Rasterizer {
	// Global enums like OpenGL. Import statically for convenience
	public static final int
	GL_BUFFER_BIT = 1,
	GL_DEPTH_BIT = 2,
	
	GL_CULL_FACE = 1,
	GL_FRONT = 1,
	GL_BACK = 2,
	GL_FRONT_AND_BACK = 3,
	
	GL_FUNC_SET = 1,
	GL_FUNC_ADD = 2,
	GL_FUNC_SUBTRACT = 3,
	GL_FUNC_REVERSE_SUBTRACT = 4,
	GL_MIN = 5,
	GL_MAX = 6;
	
	// Data required to render an image
	//TODO: Replace with texture
	private int width, height;
	private BufferedImage framebuffer;
	private Color[] pixels;
	private float[] depthBuffer;
	private Color clearColor = Color.alpha;
	
	// Data required for rasterization process
	private float 
	znear,
	zfar;
	private Matrix 
	projectionMatrix,
	screenmatrix;
	
	// Settings
	private boolean cullfaces = false;
	private int cullFaceMode = GL_BACK;
	private int blendMode = GL_FUNC_SET; 
	
	public Rasterizer(float fov, int width, int height, float znear, float zfar) {
		this.width = width;
		this.height = height;
		this.znear = znear;
		this.zfar = zfar;
		
		framebuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		pixels = new Color[width * height];
		depthBuffer = new float[pixels.length];
			
		projectionMatrix = Matrix.PerspectiveFovLH(fov, (float)width / (float)height, znear, zfar);
		screenmatrix = Matrix.scaling(-width, -height, 1).multiply(Matrix.translation(width / 2, height / 2, 1));
		
		for (int i=0; i<pixels.length; i++)
			pixels[i] = new Color(clearColor);
		clearDepthBuffer();
	}
	
	// Setters
	public void setClearColor(int argb) {
		clearColor.set(argb);
	}
	public void setClearColor(Color color) {
		clearColor.set(color);
	}
	
	// OpenGL methods
	public void clear(int mask) {
		if ((mask & GL_BUFFER_BIT) == GL_BUFFER_BIT)
			clearFrameBuffer();
		if ((mask & GL_DEPTH_BIT) == GL_DEPTH_BIT)
			clearDepthBuffer();
	}
	public void enable(int settings) {
		if ((settings & GL_CULL_FACE) == GL_CULL_FACE)
			cullfaces = true;
	}
	public void disable(int settings) {
		if ((settings & GL_CULL_FACE) == GL_CULL_FACE)
			cullfaces = false;
	}
	public void cullFace(int settings) {
		if (settings == GL_BACK || settings == GL_FRONT || settings == GL_FRONT_AND_BACK) {
			cullFaceMode = settings;
		}
	}
	public void blendEquation(int settings) {
		if (settings == GL_FUNC_SET ||
			settings == GL_FUNC_ADD ||
			settings == GL_FUNC_SUBTRACT ||
			settings == GL_MAX ||
			settings == GL_MIN) {
			blendMode  = settings;
		}
	}
	
	public void clearFrameBuffer() {		
		for (Color c : pixels)
			c.set(clearColor); // TODO: Set void color
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
	
	public void copyBufferToTexture(ImageTexture texture) {
		texture.copy(pixels);
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
	
	//TODO: Render to texture
	public void render(Mesh mesh, Camera cam) {
		if (cullfaces && cullFaceMode == GL_FRONT_AND_BACK) return;
		
		Matrix worldview = Matrix.multiply(mesh.worldmatrix, cam.viewMatrix);
		Matrix transformmatrix = Matrix.multiply(worldview, projectionMatrix);
		
		// Scale to glados size and move to middle
		transformmatrix.multiply(screenmatrix);
		
		mesh.projectVertcies(transformmatrix);
		
		Vector3 transformednormal = new Vector3(0,0,0);
		for (Face face : mesh.faces) {
			// Cull front and/or back face as per settings if GL_CULL_FACE is enabled
			if (cullfaces) {
				Matrix.transformNormal(face.normal, transformmatrix, transformednormal);
				switch (cullFaceMode) {
					case GL_BACK: 
						if (transformednormal.z > 0)
							continue;
						break;
					case GL_FRONT:
						if (transformednormal.z < 0)
							continue;
						break;
				}
			}
			
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
		if (isInBounds(v1.position) || isInBounds(v2.position) || isInBounds(v3.position)) { // TODO: Review
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
			Vector3 middlepos = v2.position;
			Vector3 bottompos = v3.position;
			int top =    (int)toppos.y;
			int middle = (int)middlepos.y;
			int bottom = (int)bottompos.y;
			
			//Check if triangle is completely off screen
			if (bottom < 0 || top > height)
				return;
			
			// Crop start and end scanline of triangle to screen
			if (top < 0)
				top = 0;
			if (bottom > height)
				bottom = height;
				
			// Calculate slopes. How many pixels to move across to move down by 1 pixel 
			float toptomiddlexslope = (middlepos.x - toppos.x) / (middlepos.y - toppos.y);
			float toptobottomxslope = (bottompos.x - toppos.x) / (bottompos.y - toppos.y);
			
			// What side is the middle vertex?
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
		//TODO: Move to drawTriangle, precalc slopes, remove all interpolate
		float leftslopepos =  (line - p1.y) / (p2.y - p1.y);
		float rightslopepos = (line - p3.y) / (p4.y - p3.y);
		leftslopepos =  clamp(leftslopepos);
		rightslopepos = clamp(rightslopepos);
		
		int startx = 	(int)interpolate(p1.x, p2.x, leftslopepos);
		int endx = 		(int)interpolate(p3.x, p4.x, rightslopepos);
		
		float startz = 	interpolate(p1.z, p2.z, leftslopepos);
		float endz = 	interpolate(p3.z, p4.z, rightslopepos);

		float startu = 	interpolate(va.textureCoordinates.getU(), vb.textureCoordinates.getU(), leftslopepos);
		float endu = 	interpolate(vc.textureCoordinates.getU(), vd.textureCoordinates.getU(), rightslopepos);

		float startv = 	interpolate(va.textureCoordinates.getV(), vb.textureCoordinates.getV(), leftslopepos);
		float endv = 	interpolate(vc.textureCoordinates.getV(), vd.textureCoordinates.getV(), rightslopepos);

		
		// How much difference in attributes per pixel
		float scanlinelength = 1f / (endx - startx); // Simple inverse. multiplying is faster than dividing
		float uslope = (endu - startu) * scanlinelength;
		float vslope = (endv - startv) * scanlinelength;
		float zslope = (endz - startz) * scanlinelength;
		
		float u = startu;
		float v = startv;
		float z = startz;
		// Clip start and end to screen
		if (startx < 0) {
			int difference = -startx;
			startx = 0;
			
			// Move start UV and depth position to where they would be now startx has moved
			u += difference * uslope;
			v += difference * vslope;
			z += difference * zslope;
		}
		// End early if goes off screen
		if (endx > width)
			endx = width;
		
		for (int x = startx; x < endx; x++) {
			z += zslope;
			u += uslope;
			v += vslope;

			// TODO: Calculate world position of pixel
			// TODO: Calculate distance from camera to pixel
			setPixel(x, line, z, tex.map(u, v));
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
		
		// TOOD: Fix depth buffer values
		depthBuffer[pixelindex] = z;
		

		switch(blendMode) {
			case GL_FUNC_SET:
				pixels[pixelindex].set(color);
				break;
			case GL_FUNC_ADD:
				pixels[pixelindex].add(color);
				break;
			case GL_FUNC_SUBTRACT:
				pixels[pixelindex].subtract(color);
				break;
			case GL_FUNC_REVERSE_SUBTRACT:
				pixels[pixelindex].reverseSubtract(color);
				break;
			case GL_MIN:
				pixels[pixelindex].min(color);
				break;
			case GL_MAX:
				pixels[pixelindex].max(color);
				break;
		}
	}
	
	private boolean isInBounds(Vector3 point) {
		return isInBounds(point.x, point.y);
	}
	private boolean isInBounds(float x, float y) {
		return isInRange(x, 0, width) && isInRange(y, 0, height);
	}
	
	public void dispose() {
		framebuffer.flush();
	}
}