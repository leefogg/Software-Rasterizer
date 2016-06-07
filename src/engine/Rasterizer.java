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
	private Vector3 transformednormal = new Vector3(0,0,0);
	
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
		
		// Scale to screen size and move to middle
		transformmatrix.multiply(screenmatrix);
		
		mesh.projectVertcies(transformmatrix);
		
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
					
					
				Vector3 toppos 		= v1.position;
				Vector3 middlepos	= v2.position;
				Vector3 bottompos	= v3.position;
				int top =    (int)toppos.y;
				int middle = (int)middlepos.y;
				int bottom = (int)bottompos.y;
				
				//Check if triangle is completely off screen
				if (bottom < 0 || top > height)
					return;
				
				// Crop start and end scanline of triangle to screen
				middle = clamp(middle, 0, height);
				bottom = clamp(bottom, 0, height);
					
				// Calculate slopes. How many pixels to move across to move down by 1 pixel
				// TODO: Is there a faster way to determine if the middle vertex is on the right?
				float toptomiddlexslope = (middlepos.x - toppos.x) / (middlepos.y - toppos.y);
				float toptobottomxslope = (bottompos.x - toppos.x) / (bottompos.y - toppos.y);
				
				float
				toptobottomdist = bottompos.y - toppos.y,
				toptomiddledist = middlepos.y - toppos.y,
				middletobottomdist = bottompos.y - middlepos.y,
				startx = toppos.x,
				endx   = toppos.x,
				startz = toppos.z,
				endz   = toppos.z,
				startu = v1.textureCoordinates.getU(),
				endu =   v1.textureCoordinates.getU(),
				startv = v1.textureCoordinates.getV(),
				endv =   v1.textureCoordinates.getV();
				if (toptomiddlexslope > toptobottomxslope) { // Middle vertex on the right
					float
					leftxslope  = (bottompos.x - toppos.x) / toptobottomdist,
					rightxslope = (middlepos.x - toppos.x) / toptomiddledist,
					leftzslope  = (bottompos.z - toppos.z) / toptobottomdist,
					rightzslope = (middlepos.z - toppos.z) / toptomiddledist,
					leftuslope  = (v3.textureCoordinates.getU() - v1.textureCoordinates.getU()) / toptobottomdist,
					rightuslope = (v2.textureCoordinates.getU() - v1.textureCoordinates.getU()) / toptomiddledist,
					leftvslope  = (v3.textureCoordinates.getV() - v1.textureCoordinates.getV()) / toptobottomdist,
					rightvslope = (v2.textureCoordinates.getV() - v1.textureCoordinates.getV()) / toptomiddledist;
					if (top < 0) { // If top vertex is off screen
						float difference = -top;
						top = 0; // Start from top of screen instead
						// Start values as if we have already looped until that scanline
						startx 	+= leftxslope  * difference;
						endx 	+= rightxslope * difference;
						startz 	+= leftzslope  * difference;
						endz 	+= rightzslope * difference;
						startu 	+= leftuslope  * difference;
						endu 	+= rightuslope * difference;
						startv 	+= leftvslope  * difference;
						endv 	+= rightvslope * difference;
					}
					
					int scanline = top;
					for (; scanline < middle; scanline++) {
						drawScanline(scanline, (int)startx, (int)endx, startz, endz, startu, endu, startv, endv, texture);
						
						startx 	+= leftxslope;
						endx 	+= rightxslope;
						startz 	+= leftzslope;
						endz 	+= rightzslope;
						startu 	+= leftuslope;
						endu	+= rightuslope;
						startv 	+= leftvslope;
						endv 	+= rightvslope;
					}
					
					rightxslope =  (bottompos.x - middlepos.x) / middletobottomdist;
					rightzslope =  (bottompos.z - middlepos.z) / middletobottomdist;
					rightuslope =  (v3.textureCoordinates.getU() - v2.textureCoordinates.getU()) / middletobottomdist;
					rightvslope =  (v3.textureCoordinates.getV() - v2.textureCoordinates.getV()) / middletobottomdist;
					endx = middlepos.x;
					endz = middlepos.z;
					endu = v2.textureCoordinates.getU();
					endv = v2.textureCoordinates.getV();
					for (; scanline < bottom; scanline++) {
						drawScanline(scanline, (int)startx, (int)endx, startz, endz, startu, endu, startv, endv, texture);
						
						startx 	+= leftxslope;
						endx 	+= rightxslope;
						startz 	+= leftzslope;
						endz 	+= rightzslope;
						startu 	+= leftuslope;
						endu	+= rightuslope;
						startv 	+= leftvslope;
						endv 	+= rightvslope;
					}
				} else {
					float
					leftxslope  =  (middlepos.x - toppos.x) / toptomiddledist,
					rightxslope =  (bottompos.x - toppos.x) / toptobottomdist,
					leftzslope  =  (middlepos.z - toppos.z) / toptomiddledist,
					rightzslope =  (bottompos.z - toppos.z) / toptobottomdist,
					leftuslope  =  (v2.textureCoordinates.getU() - v1.textureCoordinates.getU()) / toptomiddledist,
					rightuslope =  (v3.textureCoordinates.getU() - v1.textureCoordinates.getU()) / toptobottomdist,
					leftvslope  =  (v2.textureCoordinates.getV() - v1.textureCoordinates.getV()) / toptomiddledist,
					rightvslope =  (v3.textureCoordinates.getV() - v1.textureCoordinates.getV()) / toptobottomdist;
					if (top < 0) { // If top vertex is off screen
						float difference = -top;
						top = 0; // Start from top of screen instead
						// Start values as if we have already looped until that scanline
						startx 	+= leftxslope  * difference;
						endx 	+= rightxslope * difference;
						startz 	+= leftzslope  * difference;
						endz 	+= rightzslope * difference;
						startu 	+= leftuslope  * difference;
						endu 	+= rightuslope * difference;
						startv 	+= leftvslope  * difference;
						endv 	+= rightvslope * difference;
					}
					
					int scanline = top;
					for (; scanline < middle; scanline++) { 
						drawScanline(scanline, (int)startx, (int)endx, startz, endz, startu, endu, startv, endv, texture);
						
						startx 	+= leftxslope;
						endx 	+= rightxslope;
						startz 	+= leftzslope;
						endz 	+= rightzslope;
						startu 	+= leftuslope;
						endu	+= rightuslope;
						startv 	+= leftvslope;
						endv 	+= rightvslope;
					}
					
					leftxslope = (bottompos.x - middlepos.x) / middletobottomdist;
					leftzslope = (bottompos.z - middlepos.z) / middletobottomdist;
					leftuslope = (v3.textureCoordinates.getU() - v2.textureCoordinates.getU()) / middletobottomdist;
					leftvslope = (v3.textureCoordinates.getV() - v2.textureCoordinates.getV()) / middletobottomdist;
					startx = middlepos.x;
					startz = middlepos.z;
					startu = v2.textureCoordinates.getU();
					startv = v2.textureCoordinates.getV();
					for (; scanline < bottom; scanline++) { 
						drawScanline(scanline, (int)startx, (int)endx, startz, endz, startu, endu, startv, endv, texture);
						
						startx 	+= leftxslope;
						endx 	+= rightxslope;
						startz 	+= leftzslope;
						endz 	+= rightzslope;
						startu 	+= leftuslope;
						endu	+= rightuslope;
						startv 	+= leftvslope;
						endv 	+= rightvslope;
					}
				}
			}
		}
	
	private void drawScanline(int y, int startx, int endx, float startz, float endz, float startu, float endu, float startv, float endv, Texture tex) {
		// How much difference in attributes per pixel
		float scanlinelength = endx - startx;
		float uslope = (endu - startu) / scanlinelength;
		float vslope = (endv - startv) / scanlinelength;
		float zslope = (endz - startz) / scanlinelength;
		
		float u = startu;
		float v = startv;
		float z = startz;
		// Clip start and end to screen
		if (startx < 0) {
			int difference = -startx;
			startx = 0;
			
			// Move start UV and depth position to where they would be now startx has moved
			u += uslope * difference;
			v += vslope * difference;
			z += zslope * difference;
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
			setPixel(x, y, z, tex.map(u, v));
		}
	}
	
	float 
	mindepth = Float.MAX_VALUE,
	maxdepth = Float.MIN_VALUE;
	private void setPixel(int x, int y, float z, Color color) {
		int pixelindex = y*width + x;
		
		/*
		if (pixelindex >= pixels.length) {
			System.err.println("Tried to draw pixel X: "+ x + ", Y: "+ y);
			return;
		}
		if (pixelindex < 0) {
			System.err.println("Tried to draw pixel X: "+ x + ", Y: "+ y);
			return;
		}
		 */
		
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