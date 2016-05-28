package engine;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;

import engine.math.Color;
import engine.math.Matrix;
import engine.math.Vector3;
import engine.models.Mesh;
import engine.models.Texture;
import engine.models.Vertex;

public class Renderer {
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
	public Matrix projectionMatrix;
	
	public Renderer() {
		this(320, 240);
	}
	
	public Renderer(int width, int height) {
		this(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
	}
	public Renderer(BufferedImage buffer) {
		framebuffer = buffer;
		
		this.width = framebuffer.getWidth();
		this.height = framebuffer.getHeight();
		
		pixels = new Color[width * height];
		for (int i=0; i<pixels.length; i++)
			pixels[i] = new Color(0x00000000); // TODO: Set void color
			
		projectionMatrix = Matrix.PerspectiveFovLH(0.78f, (float)width / (float)height, 0.01f, 1f);
		
		depthBuffer = new float[pixels.length];
		
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
	
	
	public void render(Camera cam) {
		for (Mesh mesh : meshes)
			mesh.render(this, cam);
		
		//System.out.println("Max:" + maxdepth + ", min:" + mindepth);
		// TODO: Get real distance from camera
	}
	
	public void swapBuffers() {
		int[] outputpixels = ((DataBufferInt)framebuffer.getRaster().getDataBuffer()).getData(); // Pointer to buffer
		for (int i=0; i<pixels.length; i++)
			outputpixels[i] = pixels[i].toARGB();		
	}
	
	public BufferedImage getFrameBuffer() {
		System.out.println(maxdepth);
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
			
			float toptomiddlexslope = (middlepos.x - toppos.x) / (middlepos.y - toppos.y);
			float toptobottomxslope = (bottompos.x - toppos.x) / (bottompos.y - toppos.y);

			if (toptomiddlexslope > toptobottomxslope) {
				for (int scanline = top; scanline < bottom; scanline++) {
					if (scanline < middle) {
						triangle.ua = v1.textureCoordinates.getU();
						triangle.ub = v3.textureCoordinates.getU();
						triangle.uc = v1.textureCoordinates.getU();
						triangle.ud = v2.textureCoordinates.getU();
						
						triangle.va = v1.textureCoordinates.getV();
						triangle.vb = v3.textureCoordinates.getV();
						triangle.vc = v1.textureCoordinates.getV();
						triangle.vd = v2.textureCoordinates.getV();
						
						processScanline(scanline, triangle, v1, v3, v1, v2, texture);
					} else {
						triangle.ua = v1.textureCoordinates.getU();
						triangle.ub = v3.textureCoordinates.getU();
						triangle.uc = v2.textureCoordinates.getU();
						triangle.ud = v3.textureCoordinates.getU();
						
						triangle.va = v1.textureCoordinates.getV();
						triangle.vb = v3.textureCoordinates.getV();
						triangle.vc = v2.textureCoordinates.getV();
						triangle.vd = v3.textureCoordinates.getV();
						
						processScanline(scanline, triangle, v1, v3, v2, v3, texture);
					}
				}
			} else {
				for (int scanline = top; scanline < bottom; scanline++) {
					if (scanline < middle) {
						triangle.ua = v1.textureCoordinates.getU();
						triangle.ub = v2.textureCoordinates.getU();
						triangle.uc = v1.textureCoordinates.getU();
						triangle.ud = v3.textureCoordinates.getU();
						
						triangle.va = v1.textureCoordinates.getV();
						triangle.vb = v2.textureCoordinates.getV();
						triangle.vc = v1.textureCoordinates.getV();
						triangle.vd = v3.textureCoordinates.getV();
						
						processScanline(scanline, triangle, v1, v2, v1, v3, texture);
					} else {
						triangle.ua = v2.textureCoordinates.getU();
						triangle.ub = v3.textureCoordinates.getU();
						triangle.uc = v1.textureCoordinates.getU();
						triangle.ud = v3.textureCoordinates.getU();
						
						triangle.va = v2.textureCoordinates.getV();
						triangle.vb = v3.textureCoordinates.getV();
						triangle.vc = v1.textureCoordinates.getV();
						triangle.vd = v3.textureCoordinates.getV();
						
						processScanline(scanline, triangle, v2, v3, v1, v3, texture);
					}
				}
			}
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
		  float invslope1 = (v2.position.x - v1.position.x) / (v2.position.y - v1.position.y);
		  float invslope2 = (v3.position.x - v1.position.x) / (v3.position.y - v1.position.y);

		  float curx1 = v1.position.x;
		  float curx2 = v1.position.x;

		  for (int scanlineY = (int)v1.position.y; scanlineY <= v2.position.y; scanlineY++) {
		    drawLine((int)curx1, scanlineY, (int)curx2, scanlineY);
		    curx1 += invslope1;
		    curx2 += invslope2;
		  }
		}
		
		private void fillTopFlatTriangle(Vertex v1, Vertex v2, Vertex v3, Texture tex) {
		  float invslope1 = (v3.position.x - v1.position.x) / (v3.position.y - v1.position.y);
		  float invslope2 = (v3.position.x - v2.position.x) / (v3.position.y - v2.position.y);

		  float curx1 = v3.position.x;
		  float curx2 = v3.position.x;

		  for (int scanlineY = (int)v3.position.y; scanlineY > v1.position.y; scanlineY--) {
		    curx1 -= invslope1;
		    curx2 -= invslope2;
		    drawLine((int)curx1, scanlineY, (int)curx2, scanlineY);
		    drawLine(
		    		curx1, scanlineY,
		    		
		    	);
		  }
		}
	
	private void drawLine(int x, int y, float leftu, float leftv, float rightu, float rightv, int length, Texture tex) {
		float uslope = (rightu - leftu) / (rightv - leftv);
		float vslope = (rightv - leftv) / (rightu - leftu);
		float u = leftu, v = leftv;
		for (int i=0; i<length; i++) {
			setPixel(
					(int)x, y, 0,
					tex.map(u, v)
					);
			u += uslope;
			v += vslope;
		}
	}*/
	
	private void processScanline(int line, OrganizedTriangle data, Vertex va, Vertex vb, Vertex vc, Vertex vd, Texture tex) {
		Vector3 p1 = va.position;
		Vector3 p2 = vb.position;
		Vector3 p3 = vc.position;
		Vector3 p4 = vd.position;

		float gradient1 = (line - p1.y) / (p2.y - p1.y);
		float gradient2 = (line - p3.y) / (p4.y - p3.y);

		float sx = interpolate(p1.x, p2.x, gradient1);
		if (sx < 0)
			sx = 0;
		float ex = interpolate(p3.x, p4.x, gradient2);
		if (ex > width)
			ex = width;

		float z1 = interpolate(p1.z, p2.z, gradient1);
		float z2 = interpolate(p3.z, p4.z, gradient2);

		float su = interpolate(data.ua, data.ub, gradient1);
		float eu = interpolate(data.uc, data.ud, gradient2);

		float sv = interpolate(data.va, data.vb, gradient1);
		float ev = interpolate(data.vc, data.vd, gradient2);

		float gradientslope = 1f / (ex - sx);
		for (float x = sx, gradient=0; x < ex; x++, gradient += gradientslope) {
			float z = interpolate(z1, z2, gradient);
			float u = interpolate(su, eu, gradient);
			float v = interpolate(sv, ev, gradient);

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
		return min + (max - min) * clamp(gradient);
	}
	
	private float map(float x, float in_min, float in_max, float out_min, float out_max) {
	  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}
	
	public void dispose() {
		framebuffer.flush();
		meshes.clear();
	}
}