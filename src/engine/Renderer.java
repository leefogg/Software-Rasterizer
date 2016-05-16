package engine;

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;

import engine.math.Color;
import engine.math.Matrix;
import engine.math.Vector3;

public class Renderer {
	private class OrganizedTriangle {
		float
		ua, ub, uc, ud,
		va, vb, vc, vd;
	}
	private OrganizedTriangle triangle = new OrganizedTriangle();
	
	public int width, height;
	public BufferedImage output;
	private int[] outputpixels;
	private Color[] pixels;
	private float[] depthBuffer;
	
	public ArrayList<Mesh> meshes = new ArrayList<Mesh>();
	public Matrix projectionMatrix;
	
	public Renderer() {
		this(320, 240);
	}
	
	public Renderer(int width, int height) {
		this(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height));
	}
	public Renderer(BufferedImage buffer) {
		output = buffer;
		outputpixels = ((DataBufferInt)output.getRaster().getDataBuffer()).getData();
		
		this.width = output.getWidth();
		this.height = output.getHeight();
		
		pixels = new Color[width * height];
		for (int i=0; i<pixels.length; i++)
			pixels[i] = new Color(0xFF000000);
			
		projectionMatrix = Matrix.PerspectiveFovLH(0.78f, (float)width / (float)height, 0.01f, 1f);
		
		depthBuffer = new float[pixels.length];
		
		clearBuffer();
		clearDepthBuffer();
	}
	
	public void clearBuffer() {		
		for (int i=0; i<pixels.length; i++)
			pixels[i].set(0xFF000000);
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
		for (int i=0; i<pixels.length; i++)
			outputpixels[i] = pixels[i].toARGB();		
	}
	
	public void drawTriangle(Vertex v1, Vertex v2, Vertex v3, Texture texture) {
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
		
		float dP1P2 = (p2.y > p1.y) ? (p2.x - p1.x) / (p2.y - p1.y) : 0;
		float dP1P3 = (p3.y > p1.y) ? (p3.x - p1.x) / (p3.y - p1.y) : 0;

		if (dP1P2 > dP1P3) {
			for (int scanline = (int)p1.y; scanline <= p3.y; scanline++) {
				if (scanline < p2.y) {
					triangle.ua = v1.textureCoordinates.x;
					triangle.ub = v3.textureCoordinates.x;
					triangle.uc = v1.textureCoordinates.x;
					triangle.ud = v2.textureCoordinates.x;
					
					triangle.va = v1.textureCoordinates.y;
					triangle.vb = v3.textureCoordinates.y;
					triangle.vc = v1.textureCoordinates.y;
					triangle.vd = v2.textureCoordinates.y;
					
					processScanline(scanline, triangle, v1, v3, v1, v2, texture);
				} else {
					triangle.ua = v1.textureCoordinates.x;
					triangle.ub = v3.textureCoordinates.x;
					triangle.uc = v2.textureCoordinates.x;
					triangle.ud = v3.textureCoordinates.x;
					
					triangle.va = v1.textureCoordinates.y;
					triangle.vb = v3.textureCoordinates.y;
					triangle.vc = v2.textureCoordinates.y;
					triangle.vd = v3.textureCoordinates.y;
					
					processScanline(scanline, triangle, v1, v3, v2, v3, texture);
				}
			}
		} else {
			for (int scanline = (int)p1.y; scanline <= p3.y; scanline++) {
				if (scanline < p2.y) {
					triangle.ua = v1.textureCoordinates.x;
					triangle.ub = v2.textureCoordinates.x;
					triangle.uc = v1.textureCoordinates.x;
					triangle.ud = v3.textureCoordinates.x;
					
					triangle.va = v1.textureCoordinates.y;
					triangle.vb = v2.textureCoordinates.y;
					triangle.vc = v1.textureCoordinates.y;
					triangle.vd = v3.textureCoordinates.y;
					
					processScanline(scanline, triangle, v1, v2, v1, v3, texture);
				} else {
					triangle.ua = v2.textureCoordinates.x;
					triangle.ub = v3.textureCoordinates.x;
					triangle.uc = v1.textureCoordinates.x;
					triangle.ud = v3.textureCoordinates.x;
					
					triangle.va = v2.textureCoordinates.y;
					triangle.vb = v3.textureCoordinates.y;
					triangle.vc = v1.textureCoordinates.y;
					triangle.vd = v3.textureCoordinates.y;
					
					processScanline(scanline, triangle, v2, v3, v1, v3, texture);
				}
			}
		}
	}
	
	private void processScanline(int line, OrganizedTriangle data, Vertex va, Vertex vb, Vertex vc, Vertex vd, Texture tex) {
		Vector3 pa = va.position;
		Vector3 pb = vb.position;
		Vector3 pc = vc.position;
		Vector3 pd = vd.position;

		float gradient1 = pa.y != pb.y ? (line - pa.y) / (pb.y - pa.y) : 1;
		float gradient2 = pc.y != pd.y ? (line - pc.y) / (pd.y - pc.y) : 1;

		float sx = interpolate(pa.x, pb.x, gradient1);
		float ex = interpolate(pc.x, pd.x, gradient2);

		float z1 = interpolate(pa.z, pb.z, gradient1);
		float z2 = interpolate(pc.z, pd.z, gradient2);

		float su = interpolate(data.ua, data.ub, gradient1);
		float eu = interpolate(data.uc, data.ud, gradient2);

		float sv = interpolate(data.va, data.vb, gradient1);
		float ev = interpolate(data.vc, data.vd, gradient2);

		float gradientslope = 1f / (ex - sx);
		boolean hasrendered = false;
		for (float x = sx, gradient=0; x < ex; x++, gradient += gradientslope) {
			if (!isInBounds(x, line)) {
				if (hasrendered)
					return;
				else
					continue;
			}
			hasrendered = true;
			
			float z = interpolate(z1, z2, gradient);
			float u = interpolate(su, eu, gradient);
			float v = interpolate(sv, ev, gradient);

			// TODO: Calculate world position of pixel
			// TODO: Calculate distance from camera to pixel
			drawPoint(
					(int)x, line, z,
					tex.map(u, v)
					);
		}
	}
	
	float 
	mindepth = Float.MAX_VALUE,
	maxdepth = Float.MIN_VALUE;
	private void drawPoint(int x, int y, float z, Color color) {
		int pixel = y*width + x;
		if (depthBuffer[pixel] < z) 
			return;
		
		if (z > maxdepth) maxdepth = z;
		if (z < mindepth) mindepth = z;
		
		depthBuffer[pixel] = z;
		// TOOD: Fix depth buffer values
		pixels[pixel] = color;
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
		if (value > 1) return 1;
		if (value < 0) return 0;
		return value;
	}
	
	private float interpolate(float min, float max, float gradient) {
		return min + (max - min) * clamp(gradient);
	}
	
	public void addMesh(Mesh m) {
		if (meshes.indexOf(m) == -1)
			meshes.add(m);
	}
	
	public void dispose() {
		output.flush();
		meshes.clear();
	}
}