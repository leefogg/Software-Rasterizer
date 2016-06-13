package engine;

import static engine.math.CommonMath.clamp;
import static engine.math.CommonMath.isInRange;

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
	worldpos = new Vector3(0,0,0); // The world position of the current pixel being shaded
	private AABB boundingbox; // Used to clip triangles
	private Fragment currentFragment = new Fragment();
	
	// Settings
	private boolean cullfaces = false;
	private int cullFaceMode = GL_BACK;
	private int blendMode = GL_FUNC_SET;
	
	public Rasterizer(float fov, int width, int height, float znear, float zfar) {
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
	
	private void resise(int width, int height) {
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
	public void render(Mesh mesh, Camera cam) {
		if (cullfaces && cullFaceMode == GL_FRONT_AND_BACK) return;
		
		Matrix.multiply(mesh.worldmatrix, cam.viewMatrix, worldviewMatrix);
		Matrix.multiply(worldviewMatrix, cam.projectionMatrix, transformMatrix);
		
		// Scale to screen size and move to middle
		transformMatrix.multiply(screenmatrix);
		
		mesh.projectVertcies(transformMatrix);
		
		for (Face face : mesh.faces) {
			// Cull front and/or back face as per settings if GL_CULL_FACE is enabled
			if (cullfaces) {
				float dot = dotFaceToCam(mesh, face, cam);
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
			
			currentFragment.set(
					mesh.transformedvertcies[face.vertex1],
					mesh.transformedvertcies[face.vertex2],
					mesh.transformedvertcies[face.vertex3],
					mesh.projectedvertcies[face.vertex1],
					mesh.projectedvertcies[face.vertex2],
					mesh.projectedvertcies[face.vertex3],
					mesh.texture
				);
			drawTriangle(currentFragment, cam);
		}
	}
	
	private float dotFaceToCam(Mesh mesh, Face face, Camera cam) {
		Face.getCenter(
				mesh.transformedvertcies[face.vertex1].position,
				mesh.transformedvertcies[face.vertex2].position,
				mesh.transformedvertcies[face.vertex3].position, 
				facecenter
			);
		Vector3 aimdirection = cam.getAimDirection(facecenter);
	
		return face.normal.dotProduct(aimdirection);
	}

	public void drawTriangle(Fragment f, Camera cam) {
		if (f.isOnScreen(boundingbox)) {
			Vector3 toppos 		= f.screentop.position;
			Vector3 middlepos 	= f.screenmiddle.position;
			Vector3 bottompos 	= f.screenbottom.position;
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
					texturestartu 		= f.screentop.textureCoordinates.u,
					textureendu 		= f.screentop.textureCoordinates.u,
					texturestartv 		= f.screentop.textureCoordinates.v,
					textureendv 		= f.screentop.textureCoordinates.v,
					worldstartx			= f.worldtop.position.x,
					worldendx			= f.worldtop.position.x,
					worldstarty			= f.worldtop.position.y,
					worldendy			= f.worldtop.position.y,
					worldstartz 		= f.worldtop.position.z,
					worldendz 			= f.worldtop.position.z;
			if (f.middleOnRight()) { // Middle vertex on the right
				float 	screenleftxslope  	= (bottompos.x - toppos.x) / toptobottomdist,
						screenrightxslope 	= (middlepos.x - toppos.x) / toptomiddledist,
						textureleftuslope  	= (f.screenbottom.textureCoordinates.u - f.screentop.textureCoordinates.u) / toptobottomdist,
						texturerightuslope 	= (f.screenmiddle.textureCoordinates.u - f.screentop.textureCoordinates.u) / toptomiddledist,
						textureleftvslope  	= (f.screenbottom.textureCoordinates.v - f.screentop.textureCoordinates.v) / toptobottomdist,
						texturerightvslope 	= (f.screenmiddle.textureCoordinates.v - f.screentop.textureCoordinates.v) / toptomiddledist,
						worldleftxslope  	= (f.worldbottom.position.x - f.worldtop.position.x) / toptobottomdist,
						worldrightxslope 	= (f.worldmiddle.position.x - f.worldtop.position.x) / toptomiddledist,
						worldleftyslope  	= (f.worldbottom.position.y - f.worldtop.position.y) / toptobottomdist,
						worldrightyslope 	= (f.worldmiddle.position.y - f.worldtop.position.y) / toptomiddledist,
						worldleftzslope  	= (f.worldbottom.position.z - f.worldtop.position.z) / toptobottomdist,
						worldrightzslope 	= (f.worldmiddle.position.z - f.worldtop.position.z) / toptomiddledist;
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
					drawScanline(scanline, (int)screenstartx, (int)screenendx, worldstartx, worldendx, worldstarty, worldendy, worldstartz, worldendz, texturestartu, textureendu, texturestartv, textureendv, cam, f.texture);

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
				texturerightuslope 	= (f.screenbottom.textureCoordinates.u - f.screenmiddle.textureCoordinates.u) / middletobottomdist;
				texturerightvslope 	= (f.screenbottom.textureCoordinates.v - f.screenmiddle.textureCoordinates.v) / middletobottomdist;
				worldrightxslope 	= (f.worldbottom.position.x - f.worldmiddle.position.x) / middletobottomdist;
				worldrightyslope 	= (f.worldbottom.position.y - f.worldmiddle.position.y) / middletobottomdist;
				worldrightzslope 	= (f.worldbottom.position.z - f.worldmiddle.position.z) / middletobottomdist;
				screenendx 			= middlepos.x;
				textureendu = f.screenmiddle.textureCoordinates.u;
				textureendv = f.screenmiddle.textureCoordinates.v;
				worldendx 	= f.worldmiddle.position.x;
				worldendy 	= f.worldmiddle.position.y;
				worldendz 	= f.worldmiddle.position.z;
				for (; scanline < bottom; scanline++) {
					drawScanline(scanline, (int)screenstartx, (int)screenendx, worldstartx, worldendx, worldstarty, worldendy, worldstartz, worldendz, texturestartu, textureendu, texturestartv, textureendv, cam, f.texture);

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
						textureleftuslope  	= (f.screenmiddle.textureCoordinates.u - f.screentop.textureCoordinates.u) / toptomiddledist,
						texturerightuslope 	= (f.screenbottom.textureCoordinates.u - f.screentop.textureCoordinates.u) / toptobottomdist,
						textureleftvslope  	= (f.screenmiddle.textureCoordinates.v - f.screentop.textureCoordinates.v) / toptomiddledist,
						texturerightvslope 	= (f.screenbottom.textureCoordinates.v - f.screentop.textureCoordinates.v) / toptobottomdist,
						worldleftxslope  	= (f.worldmiddle.position.x - f.worldtop.position.x) / toptomiddledist,
						worldrightxslope 	= (f.worldbottom.position.x - f.worldtop.position.x) / toptobottomdist,
						worldleftyslope  	= (f.worldmiddle.position.y - f.worldtop.position.y) / toptomiddledist,
						worldrightyslope 	= (f.worldbottom.position.y - f.worldtop.position.y) / toptobottomdist,
						worldleftzslope  	= (f.worldmiddle.position.z - f.worldtop.position.z) / toptomiddledist,
						worldrightzslope 	= (f.worldbottom.position.z - f.worldtop.position.z) / toptobottomdist;
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
					drawScanline(scanline, (int)screenstartx, (int)screenendx, worldstartx, worldendx, worldstarty, worldendy, worldstartz, worldendz, texturestartu, textureendu, texturestartv, textureendv, cam, f.texture);

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
				textureleftuslope 	= (f.screenbottom.textureCoordinates.u - f.screenmiddle.textureCoordinates.u) / middletobottomdist;
				textureleftvslope 	= (f.screenbottom.textureCoordinates.v - f.screenmiddle.textureCoordinates.v) / middletobottomdist;
				worldleftxslope 	= (f.worldbottom.position.x - f.worldmiddle.position.x) / middletobottomdist;
				worldleftyslope 	= (f.worldbottom.position.y - f.worldmiddle.position.y) / middletobottomdist;
				worldleftzslope 	= (f.worldbottom.position.z - f.worldmiddle.position.z) / middletobottomdist;
				screenstartx 		= middlepos.x;
				texturestartu 	= f.screenmiddle.textureCoordinates.u;
				texturestartv 	= f.screenmiddle.textureCoordinates.v;
				worldstartx 	= f.worldmiddle.position.x;
				worldstarty 	= f.worldmiddle.position.y;
				worldstartz 	= f.worldmiddle.position.z;
				for (; scanline < bottom; scanline++) {
					drawScanline(scanline, (int)screenstartx, (int)screenendx, worldstartx, worldendx, worldstarty, worldendy, worldstartz, worldendz, texturestartu, textureendu, texturestartv, textureendv, cam, f.texture);

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
	}

	private void drawScanline(int y, int screenstartx, int screenendx, float worldstartx, float worldendx, float worldstarty, float worldendy, float worldstartz, float worldendz, float texturestartu, float textureendu, float texturestartv, float textureendv, Camera camera, Texture tex) {
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
			
			int pixelindex = testPixel(x, y, distance, znear, zfar);
			if (pixelindex == -1) 
				continue;
			
			Color pixelcolor = tex.map(u, v);
			//TODO: Shade pixel
			
			setPixel(pixelindex,
					 distance,
					 pixelcolor
				);
		}
	}
	
	private int testPixel(int x, int y, float z, float znear, float zfar) {
		if (z > zfar)
			return -1;
		if (z < znear)
			return -1;
		
		if (x > width)
			return -1;
		if (y > height)
			return -1;
		
		int pixelindex = y*width + x;
		
		if (depthBuffer[pixelindex] < z) 
			return -1;
		
		return pixelindex;
	}
	
	private void setPixel(int pixelindex, float z, Color color) {
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