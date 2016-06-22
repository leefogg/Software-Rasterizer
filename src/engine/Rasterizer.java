package engine;

import static engine.math.CommonMath.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;

import engine.math.AABB;
import engine.math.Color;
import engine.math.Matrix;
import engine.math.Vector3;
import engine.models.Face;
import engine.models.Mesh;
import engine.models.Texture;
import engine.models.Materials.ImageTexture;
import engine.models.Materials.Shading.Shader;

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
	private Matrix
	screenmatrix,
	worldviewMatrix = new Matrix(),
	transformMatrix = new Matrix();
	private Vector3 
	facecenter = new Vector3(0,0,0), // The position of the center of the face being rendered
	facenormal = new Vector3(0,0,0), // The normal of the face being rendered
	worldpos = new Vector3(0,0,0); // The world position of the current pixel being shaded
	private AABB boundingbox; // Used to clip triangles
	private Primitive currentFragment = new Primitive();
	
	// Settings
	private boolean cullfaces = false;
	private int cullFaceMode = GL_BACK;
	private int blendMode = GL_FUNC_SET;
	
	public Rasterizer(int width, int height) {
		this.width = width;
		this.height = height;
		
		boundingbox = new AABB(0,0, width,height);
		
		framebuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		pixels = new Color[width * height];
		depthBuffer = new float[pixels.length];
		
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
			c.set(clearColor);
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
	
	public BufferedImage getDepthBuffer(Camera camera) {
		return getDepthBuffer(new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY), camera);
	}
	public BufferedImage getDepthBuffer(BufferedImage buffer, Camera camera) {
		byte[] outputpixels = ((DataBufferByte)buffer.getRaster().getDataBuffer()).getData(); // Pointer to buffer
		
		float
		znear = camera.getZNear(),
		zfar = camera.getZFar();
		// Could use Map function, but inputs wont change so I cache them instead
		float inversediff = 1f / (zfar - znear); // Thousands of pixels. Multiplication is faster than division.
		int i=0;
		for (float depth : depthBuffer) {
			// Have to calculate the value as float for resolution, bytes are tiny
			depth -= znear;
			depth *= inversediff;
			depth *= 255f;
			depth += znear;
			
			outputpixels[i++] = (byte)(255-depth);
		}
		
		return buffer;
	}
	
	private void resize(int width, int height) {
		int numpixels = width * height;
		Color[] newpixels = new Color[numpixels];
		// Preserve memory thrashing as much as possible
		int pixel = 0;
		// Copy existing pixels into new array
		for (; pixel<Math.min(numpixels, pixels.length); pixel++) 
			newpixels[pixel] = pixels[pixel];
		// If there's still more pixels, have to create new ones on the end
		for (; pixel<numpixels; pixel++)
			newpixels[pixel] = new Color(clearColor);
		
		pixels = newpixels;
		framebuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		depthBuffer = new float[numpixels]; // No point copying primitives over
	}
	
	//TODO: Render to texture
	public void render(Mesh mesh, Camera camera) {
		if (cullfaces && cullFaceMode == GL_FRONT_AND_BACK) return;
		
		Matrix.multiply(mesh.worldmatrix, camera.viewMatrix, worldviewMatrix);
		Matrix.multiply(worldviewMatrix, camera.projectionMatrix, transformMatrix);
		
		// Scale to screen size and move to middle
		transformMatrix.multiply(screenmatrix);
		
		mesh.projectVertcies(transformMatrix);
		
		for (Face face : mesh.faces) {
			// Perform simple culling
			// Cull front and/or back face as per settings if GL_CULL_FACE is enabled
			if (cullfaces) {
				float dot = dotFaceCenterToCam(mesh, face, camera);
				switch (cullFaceMode) {
					case GL_BACK: 
						if (dot > 0)
							continue;
						break;
					case GL_FRONT:
						if (dot < 0)
							continue;
						break;
				}
			}
			
			// More complex culling tests
			currentFragment.set(
					mesh.transformedvertcies[face.vertex1],
					mesh.transformedvertcies[face.vertex2],
					mesh.transformedvertcies[face.vertex3],
					mesh.projectedvertcies[face.vertex1],
					mesh.projectedvertcies[face.vertex2],
					mesh.projectedvertcies[face.vertex3],
					face.uv1,
					face.uv2,
					face.uv3,
					mesh.texture
					);
			currentFragment.calculateboundingBox();
			if (!currentFragment.isOnScreen(boundingbox)) continue;
			
			// Okay, draw it
			if (mesh.shader != null) {
				mesh.shader.texture = mesh.texture;
				mesh.shader.faceNormal = face.normal;
				mesh.shader.FaceCenter = face.center;
			}
			drawTriangle(currentFragment, camera, mesh.shader);
		}
	}
	
	private float dotFaceCenterToCam(Mesh mesh, Face face, Camera camera) {
		Vector3 aimdirection = camera.getAimDirection(face.center).normalize();
	
		return face.normal.dotProduct(aimdirection);
	}
	private float dotCameraAimToFaceNormal(Mesh mesh, Face face, Camera camera) {
		Vector3 aimdirection = camera.getAimDirection(facecenter).normalize();
	
		return face.normal.dotProduct(aimdirection);
	}

	public void drawTriangle(Primitive f, Camera cam, Shader shader) {
			Vector3 toppos 		= f.screentop;
			Vector3 middlepos 	= f.screenmiddle;
			Vector3 bottompos 	= f.screenbottom;
			int top 	= (int)toppos.y;
			int middle 	= (int)middlepos.y;
			int bottom 	= (int)bottompos.y;

			// Check if triangle is completely off screen
			//TOOD: is this required now fragment has AABB?
			if (bottom < 0 || top > height)
				return;

			// Crop start and end scanline of triangle to screen
			middle = clamp(middle, 0, height);
			bottom = clamp(bottom, 0, height);

			float 	toptobottomdist 	= bottompos.y - toppos.y,
					toptomiddledist 	= middlepos.y - toppos.y,
					middletobottomdist 	= bottompos.y - middlepos.y,
					screenstartx 		= toppos.x,
					screenendx 			= toppos.x,
					texturestartu 		= f.topVertexUV.u,
					textureendu 		= f.topVertexUV.u,
					texturestartv 		= f.topVertexUV.v,
					textureendv 		= f.topVertexUV.v,
					worldstartx			= f.worldtop.x,
					worldendx			= f.worldtop.x,
					worldstarty			= f.worldtop.y,
					worldendy			= f.worldtop.y,
					worldstartz 		= f.worldtop.z,
					worldendz 			= f.worldtop.z;
			if (f.middleOnRight()) { // Middle vertex on the right
				float 	screenleftxslope  	= (bottompos.x - toppos.x) / toptobottomdist,
						screenrightxslope 	= (middlepos.x - toppos.x) / toptomiddledist,
						textureleftuslope  	= (f.bottomVertexUV.u - f.topVertexUV.u) / toptobottomdist,
						texturerightuslope 	= (f.middleVertexUV.u - f.topVertexUV.u) / toptomiddledist,
						textureleftvslope  	= (f.bottomVertexUV.v - f.topVertexUV.v) / toptobottomdist,
						texturerightvslope 	= (f.middleVertexUV.v - f.topVertexUV.v) / toptomiddledist,
						worldleftxslope  	= (f.worldbottom.x - f.worldtop.x) / toptobottomdist,
						worldrightxslope 	= (f.worldmiddle.x - f.worldtop.x) / toptomiddledist,
						worldleftyslope  	= (f.worldbottom.y - f.worldtop.y) / toptobottomdist,
						worldrightyslope 	= (f.worldmiddle.y - f.worldtop.y) / toptomiddledist,
						worldleftzslope  	= (f.worldbottom.z - f.worldtop.z) / toptobottomdist,
						worldrightzslope 	= (f.worldmiddle.z - f.worldtop.z) / toptomiddledist;
				if (top < 0) { // If top vertex is off screen
					float difference = -top;
					top = 0; // Start from top of screen instead
					
					// Start values as if we have already looped until that scanline
					screenstartx 	+= screenleftxslope   * difference;
					screenendx 		+= screenrightxslope  * difference;
					texturestartu 	+= textureleftuslope  * difference;
					textureendu 	+= texturerightuslope * difference;
					texturestartv 	+= textureleftvslope  * difference;
					textureendv 	+= texturerightvslope * difference;
					worldstartx 	+= worldleftxslope  * difference;
					worldendx 		+= worldrightxslope * difference;
					worldstarty 	+= worldleftyslope  * difference;
					worldendy 		+= worldrightyslope * difference;
					worldstartz 	+= worldleftzslope  * difference;
					worldendz 		+= worldrightzslope * difference;
				}

				int scanline = top;
				for (; scanline < middle; scanline++) {
					drawScanline(scanline, (int)screenstartx, (int)screenendx, worldstartx, worldendx, worldstarty, worldendy, worldstartz, worldendz, texturestartu, textureendu, texturestartv, textureendv, cam, f.texture, shader);

					screenstartx 	+= screenleftxslope;
					screenendx 		+= screenrightxslope;
					texturestartu 	+= textureleftuslope;
					textureendu 	+= texturerightuslope;
					texturestartv 	+= textureleftvslope;
					textureendv 	+= texturerightvslope;
					worldstartx 	+= worldleftxslope;
					worldendx 		+= worldrightxslope;
					worldstarty 	+= worldleftyslope;
					worldendy 		+= worldrightyslope;
					worldstartz 	+= worldleftzslope;
					worldendz 		+= worldrightzslope;
				}

				screenrightxslope 	= (bottompos.x - middlepos.x) / middletobottomdist;
				texturerightuslope 	= (f.bottomVertexUV.u - f.middleVertexUV.u) / middletobottomdist;
				texturerightvslope 	= (f.bottomVertexUV.v - f.middleVertexUV.v) / middletobottomdist;
				worldrightxslope 	= (f.worldbottom.x - f.worldmiddle.x) / middletobottomdist;
				worldrightyslope 	= (f.worldbottom.y - f.worldmiddle.y) / middletobottomdist;
				worldrightzslope 	= (f.worldbottom.z - f.worldmiddle.z) / middletobottomdist;
				screenendx 			= middlepos.x;
				textureendu = f.middleVertexUV.u;
				textureendv = f.middleVertexUV.v;
				worldendx 	= f.worldmiddle.x;
				worldendy 	= f.worldmiddle.y;
				worldendz 	= f.worldmiddle.z;
				for (; scanline < bottom; scanline++) {
					drawScanline(scanline, (int)screenstartx, (int)screenendx, worldstartx, worldendx, worldstarty, worldendy, worldstartz, worldendz, texturestartu, textureendu, texturestartv, textureendv, cam, f.texture, shader);

					screenstartx 	+= screenleftxslope;
					screenendx 		+= screenrightxslope;
					texturestartu 	+= textureleftuslope;
					textureendu 	+= texturerightuslope;
					texturestartv 	+= textureleftvslope;
					textureendv 	+= texturerightvslope;
					worldstartx 	+= worldleftxslope;
					worldendx 		+= worldrightxslope;
					worldstarty 	+= worldleftyslope;
					worldendy 		+= worldrightyslope;
					worldstartz 	+= worldleftzslope;
					worldendz 		+= worldrightzslope;
				}
			} else {
				float 	screenleftxslope  	= (middlepos.x - toppos.x) / toptomiddledist,
						screenrightxslope 	= (bottompos.x - toppos.x) / toptobottomdist,
						textureleftuslope  	= (f.middleVertexUV.u - f.topVertexUV.u) / toptomiddledist,
						texturerightuslope 	= (f.bottomVertexUV.u - f.topVertexUV.u) / toptobottomdist,
						textureleftvslope  	= (f.middleVertexUV.v - f.topVertexUV.v) / toptomiddledist,
						texturerightvslope 	= (f.bottomVertexUV.v - f.topVertexUV.v) / toptobottomdist,
						worldleftxslope  	= (f.worldmiddle.x - f.worldtop.x) / toptomiddledist,
						worldrightxslope 	= (f.worldbottom.x - f.worldtop.x) / toptobottomdist,
						worldleftyslope  	= (f.worldmiddle.y - f.worldtop.y) / toptomiddledist,
						worldrightyslope 	= (f.worldbottom.y - f.worldtop.y) / toptobottomdist,
						worldleftzslope  	= (f.worldmiddle.z - f.worldtop.z) / toptomiddledist,
						worldrightzslope 	= (f.worldbottom.z - f.worldtop.z) / toptobottomdist;
				if (top < 0) { // If top vertex is off screen
					float difference = -top;
					top = 0; // Start from top of screen instead
					
					// Start values as if we have already looped until that scanline
					screenstartx 	+= screenleftxslope   * difference;
					screenendx 		+= screenrightxslope  * difference;
					texturestartu 	+= textureleftuslope  * difference;
					textureendu 	+= texturerightuslope * difference;
					texturestartv 	+= textureleftvslope  * difference;
					textureendv 	+= texturerightvslope * difference;
					worldstartx 	+= worldleftxslope  * difference;
					worldendx 		+= worldrightxslope * difference;
					worldstarty 	+= worldleftyslope  * difference;
					worldendy 		+= worldrightyslope * difference;
					worldstartz 	+= worldleftzslope  * difference;
					worldendz 		+= worldrightzslope * difference;
				}

				int scanline = top;
				for (; scanline < middle; scanline++) {
					drawScanline(scanline, (int)screenstartx, (int)screenendx, worldstartx, worldendx, worldstarty, worldendy, worldstartz, worldendz, texturestartu, textureendu, texturestartv, textureendv, cam, f.texture, shader);

					screenstartx 	+= screenleftxslope;
					screenendx 		+= screenrightxslope;
					texturestartu 	+= textureleftuslope;
					textureendu 	+= texturerightuslope;
					texturestartv 	+= textureleftvslope;
					textureendv 	+= texturerightvslope;
					worldstartx 	+= worldleftxslope;
					worldendx 		+= worldrightxslope;
					worldstarty 	+= worldleftyslope;
					worldendy 		+= worldrightyslope;
					worldstartz 	+= worldleftzslope;
					worldendz 		+= worldrightzslope;
				}

				screenleftxslope 	= (bottompos.x - middlepos.x) / middletobottomdist;
				textureleftuslope 	= (f.bottomVertexUV.u - f.middleVertexUV.u) / middletobottomdist;
				textureleftvslope 	= (f.bottomVertexUV.v - f.middleVertexUV.v) / middletobottomdist;
				worldleftxslope 	= (f.worldbottom.x - f.worldmiddle.x) / middletobottomdist;
				worldleftyslope 	= (f.worldbottom.y - f.worldmiddle.y) / middletobottomdist;
				worldleftzslope 	= (f.worldbottom.z - f.worldmiddle.z) / middletobottomdist;
				screenstartx 		= middlepos.x;
				texturestartu 	= f.middleVertexUV.u;
				texturestartv 	= f.middleVertexUV.v;
				worldstartx 	= f.worldmiddle.x;
				worldstarty 	= f.worldmiddle.y;
				worldstartz 	= f.worldmiddle.z;
				for (; scanline < bottom; scanline++) {
					drawScanline(scanline, (int)screenstartx, (int)screenendx, worldstartx, worldendx, worldstarty, worldendy, worldstartz, worldendz, texturestartu, textureendu, texturestartv, textureendv, cam, f.texture, shader);

					screenstartx 	+= screenleftxslope;
					screenendx 		+= screenrightxslope;
					texturestartu 	+= textureleftuslope;
					textureendu 	+= texturerightuslope;
					texturestartv 	+= textureleftvslope;
					textureendv 	+= texturerightvslope;
					worldstartx 	+= worldleftxslope;
					worldendx 		+= worldrightxslope;
					worldstarty 	+= worldleftyslope;
					worldendy 		+= worldrightyslope;
					worldstartz 	+= worldleftzslope;
					worldendz 		+= worldrightzslope;
				}
			}
	}

	private void drawScanline(int y, int screenstartx, int screenendx, float worldstartx, float worldendx, float worldstarty, float worldendy, float worldstartz, float worldendz, float texturestartu, float textureendu, float texturestartv, float textureendv, Camera camera, Texture tex, Shader shader) {
		// How much difference in attributes per pixel
		float scanlinelength = screenendx - screenstartx;
		float uslope = (textureendu - texturestartu)/ scanlinelength;
		float vslope = (textureendv - texturestartv)/ scanlinelength;
		float xslope = (worldendx - worldstartx) 	/ scanlinelength;
		float yslope = (worldendy - worldstarty) 	/ scanlinelength;
		float zslope = (worldendz - worldstartz) 	/ scanlinelength;
		
		float u = texturestartu;
		float v = texturestartv;
		float worldx = worldstartx;
		float worldy = worldstarty;
		float worldz = worldstartz;
		// Clip start and end to screen
		if (screenstartx < 0) {
			float difference = -screenstartx;
			screenstartx = 0;
			
			// Move start UV and depth position to where they would be now startx has moved
			u 		+= uslope * difference;
			v 		+= vslope * difference;
			worldx 	+= xslope * difference;
			worldy 	+= yslope * difference;
			worldz 	+= zslope * difference;
		}
		// End early if goes off screen
		if (screenendx > width)
			screenendx = width;
		
		
		float 
		znear = camera.getZNear(),
		zfar = camera.getZFar();
		//TODO: Check if any of the scanline is in depth range
		for (int x = screenstartx; x < screenendx; x++) {
			worldx 	+= xslope;
			worldy 	+= yslope;
			worldz 	+= zslope;
			u 		+= uslope;
			v 		+= vslope;
			
			worldpos.set(worldx, worldy, worldz);
			float distance = (float)camera.getDistanceToCamera(worldpos);
			
			int pixelindex = getPixelIndex(x, y, distance, znear, zfar);
			if (pixelindex == -1) 
				continue;
			
			
			if (shader != null) {
				shader.destinationDepth = depthBuffer[pixelindex];
				shader.sourceDepth = distance;
				shader.screenX = x;
				shader.screenY = y;
				shader.u = u;
				shader.v = v;
				shader.worldPosition = worldpos;
				shader.destinationColor = pixels[pixelindex];
				shader.shade();
				
				depthBuffer[pixelindex] = shader.sourceDepth;				
			} else {
				Color pixelcolor = tex.map(u, v);
				setPixel(pixelindex,
						 pixelcolor
					);
				depthBuffer[pixelindex] = distance;
			}
			
		}
	}
	
	private int getPixelIndex(int x, int y, float z, float znear, float zfar) {
		if (x > width)
			return -1;
		if (y > height)
			return -1;
		
		if (z > zfar)
			return -1;
		if (z < znear)
			return -1;
		
		int pixelindex = y*width + x;
		
		if (depthBuffer[pixelindex] < z) 
			return -1;
		
		return pixelindex;
	}
	
	private void setPixel(int pixelindex, Color color) {
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