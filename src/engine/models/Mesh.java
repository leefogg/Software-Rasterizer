package engine.models;

import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import engine.Camera;
import engine.ImageTexture;
import engine.Renderer;
import engine.math.Matrix;
import engine.math.Vector3;

public class Mesh {
	private Vertex[] 
			vertcies,
			transformedvertcies;
	private Face[] faces;
	private Vector3 
	position = Vector3.zero.Clone(),
	rotation = new Vector3(0.00001f, 0.00001f, 0.00001f);
	public Texture texture = Texture.error;
	
	private Matrix worldmatrix;
	
	public Mesh(Vertex[] verticies, Face[] faces, Texture tex) {
		this.vertcies = verticies;
		transformedvertcies = new Vertex[vertcies.length];
		for (int i=0; i<vertcies.length; i++) {
			Vertex vertex = vertcies[i];
			transformedvertcies[i] = new Vertex(vertex.position.Clone(), vertex.textureCoordinates);
		}
			
		this.faces = faces;
		this.texture = tex;

		worldmatrix = Matrix.RotationYawPitchRoll(rotation.y, rotation.x, rotation.z).multiply(Matrix.translation(position.x, position.y, position.z));
	}
	
	public void render(Renderer renderer, Camera cam) {
		Matrix worldview = Matrix.multiply(worldmatrix, cam.viewMatrix);
		Matrix transformmatrix = Matrix.multiply(worldview, renderer.projectionMatrix);
		
		projectVertcies(transformmatrix, renderer.width, renderer.height);
		Vector3 transformednormal = new Vector3(0,0,0);
		for (Face face : faces) {
			Matrix.transformNormal(face.normal, transformmatrix, transformednormal);
			if (transformednormal.z >= 0)
				continue;
			
			
			renderer.drawTriangle(
					transformedvertcies[face.vertex1],
					transformedvertcies[face.vertex2],
					transformedvertcies[face.vertex3],
					texture
					);
		}
	}
	
	private void projectVertcies(Matrix m, int width, int height) {
		Matrix a = Matrix.scaling(width, -height, 1);
		Matrix b = Matrix.translation(width / 2, height / 2, 1);
		m = m.Clone().multiply(a).multiply(b);
		
		for (int i=0; i<vertcies.length; i++) {
			Matrix.transformCoordinates(vertcies[i].position, m, transformedvertcies[i].position);
		}
	}
	
	public Vector3 getPosition() {
		return position.Clone();
	}
	
	public Vector3 getRotation() {
		return rotation.Clone();
	}
	
	public void setPosition(Vector3 pos) {
		position = pos;
		worldmatrix = Matrix.RotationYawPitchRoll(rotation.y, rotation.x, rotation.z)
				.multiply(Matrix.translation(position.x, position.y, position.z));
	}
	
	public void setRotation(Vector3 rot) {
		rotation = rot;
		worldmatrix = Matrix.RotationYawPitchRoll(rotation.y, rotation.x, rotation.z)
				.multiply(Matrix.translation(position.x, position.y, position.z));
	}
	
	public void debugUVs(String imagepath) throws IOException {
		int width = 1;
		int height = 1;
		if (texture instanceof ImageTexture) {
			ImageTexture tex = (ImageTexture)texture;
			width = tex.getWidth();
			height = tex.getHeight();
		}
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		float u = 0, v=0;
		for (int y=0; y<height; y++, v += 1f/height) {
			u = 0;
			for (int x=0; x<width; x++, u += 1f/width) {
				image.setRGB(x, y, texture.map(u, v).toARGB());
			}
		}
		
		
		java.awt.Graphics canvas = image.createGraphics();
		canvas.setColor(java.awt.Color.black);
		for (Face f : faces) {
			UVSet va = vertcies[f.vertex1].textureCoordinates;
			UVSet vb = vertcies[f.vertex2].textureCoordinates;
			UVSet vc = vertcies[f.vertex3].textureCoordinates;
			canvas.drawLine(
					(int)(va.u*width),
					(int)(va.v*height),
					(int)(vb.u*width),
					(int)(vb.v*height)
			);
			canvas.drawLine(
					(int)(vb.u*width),
					(int)(vb.v*height),
					(int)(vc.u*width),
					(int)(vc.v*height)
			);
			canvas.drawLine(
					(int)(vc.u*width),
					(int)(vc.v*height),
					(int)(va.u*width),
					(int)(va.v*height)
			);
		}
		
		String extention = imagepath.substring(imagepath.lastIndexOf(".")+1);
		ImageIO.write(image, extention, new java.io.File(imagepath));
	}
	
	public void writeOBJ(FileWriter file) throws IOException {
		for (Vertex v : vertcies) {
			String output = "v " + v.position.x + " " + v.position.y + " " + v.position.z +"\n";
			file.write(output);
		}
		for (Face f : faces) {
			String out = "f " + (f.vertex1+1) + " " + (f.vertex2+1) + " " + (f.vertex3+1) + "\n";
			file.write(out);
		}
		for (Face f : faces) {
			String output = "n " + (f.normal.x + 1) + " " + (f.normal.y + 1) + " " + (f.normal.z)+"\n";
			file.write(output);
		}
		file.flush();
		file.close();
	}
}