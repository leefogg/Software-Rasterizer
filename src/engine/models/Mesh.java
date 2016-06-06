package engine.models;

import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import engine.Camera;
import engine.Rasterizer;
import engine.math.Matrix;
import engine.math.Vector3;
import engine.models.Materials.ImageTexture;

public class Mesh {
	public Vertex[] 
			vertcies,
			transformedvertcies;
	public Face[] faces;
	private Vector3 
	position = Vector3.zero.Clone(),
	rotation = new Vector3(0.00001f, 0.00001f, 0.00001f);
	public Texture texture = Texture.error;
	
	public Matrix worldmatrix = new Matrix();
	
	public Mesh(Vertex[] verticies, Face[] faces, Texture tex) {
		this.vertcies = verticies;
		transformedvertcies = new Vertex[vertcies.length];
		for (int i=0; i<vertcies.length; i++) {
			Vertex vertex = vertcies[i];
			transformedvertcies[i] = new Vertex(vertex.position.Clone(), vertex.textureCoordinates);
		}
			
		this.faces = faces;
		this.texture = tex;

//		updateWorldMatrix();
	}
	
	public void projectVertcies(Matrix transformmatrix) {
		for (int i=0; i<vertcies.length; i++) // TODO: Make projectPositions method that takes all verts
			Matrix.transformCoordinates(vertcies[i].position, transformmatrix, transformedvertcies[i].position);
	}
	
	public Vector3 getPosition() {
		return position.Clone();
	}
	
	public Vector3 getRotation() {
		return rotation.Clone();
	}
	
	//TODO: Add methods to transform raw vert's positions
	public void setPosition(float x, float y, float z) {
		position.x = x;
		position.y = y;
		position.z = z;
		updateWorldMatrix();
	}
	public void setPosition(Vector3 pos) {
		position = pos;
		updateWorldMatrix();
	}
	
	public void setRotation(Vector3 rot) {
		rotation = rot;
		updateWorldMatrix();
	}
	
	private void updateWorldMatrix() {
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
		BufferedImage image = texture.toBufferedImage();
		
		
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