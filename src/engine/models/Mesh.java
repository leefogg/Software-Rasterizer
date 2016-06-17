package engine.models;

import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import engine.math.Matrix;
import engine.math.Vector3;
import engine.models.Materials.ImageTexture;

public class Mesh {
	private Vertex[] 
			vertcies;
	public Vector3[]
			transformedvertcies,
			projectedvertcies; 
	public Face[] faces;
	private Vector3 
		position = Vector3.zero.Clone(),
		rotation = new Vector3(0.00001f, 0.00001f, 0.00001f);
	public Texture texture = Texture.error;
	
	public Matrix worldmatrix;
	
	public Mesh(Vertex[] verticies, Face[] faces, Texture tex) {
		this.vertcies = verticies;
		
		transformedvertcies = new Vector3[vertcies.length];
		projectedvertcies = new Vector3[vertcies.length];
		for (int i=0; i<vertcies.length; i++) {
			Vertex vertex = vertcies[i];
			transformedvertcies[i] 	= vertex.position.Clone();
			projectedvertcies[i] 	= vertex.position.Clone();
		}
			
		this.faces = faces;
		this.texture = tex;

		updateWorldMatrix();
	}
	
	public void projectVertcies(Matrix projectionmatrix) {
		for (int i=0; i<vertcies.length; i++) {
			Matrix.transformCoordinates(vertcies[i].position, projectionmatrix, projectedvertcies[i]);
		}
	}
	private void transformVertcies(Matrix transformmatrix) {
		for (int i=0; i<vertcies.length; i++) {
			Matrix.transformCoordinates(vertcies[i].position, transformmatrix, transformedvertcies[i]);			
		}
		
		updateFaces();
	}
	
	private void updateFaces() {
		for (Face face : faces) {
			face.updateFaceCenter(
					transformedvertcies[face.vertex1],
					transformedvertcies[face.vertex2],
					transformedvertcies[face.vertex3]
					);
			face.updateFaceNormal(
					transformedvertcies[face.vertex1],
					transformedvertcies[face.vertex2],
					transformedvertcies[face.vertex3]
					);
		}
	}
	
	public Vector3 getPosition() {
		return position.Clone();
	}
	
	public Vector3 getRotation() {
		return rotation.Clone();
	}
	
	public void setPosition(Vector3 pos) {
		setPosition(pos.x, pos.y, pos.z);
	}
	//TODO: Add methods to transform raw vert's positions
	public void setPosition(float x, float y, float z) {
		position.x = x;
		position.y = y;
		position.z = z;
		
		updateWorldMatrix();
		transformVertcies(worldmatrix);
	}
		
	public void setRotation(Vector3 rot) {
		rotation = rot;
		
		updateWorldMatrix();
		transformVertcies(worldmatrix);
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
		canvas.setColor(java.awt.Color.white);
		for (Face f : faces) {
			UVSet va = f.uv1;
			UVSet vb = f.uv2;
			UVSet vc = f.uv3;
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
			String output = "n " + f.normal.x + " " + f.normal.y + " " + f.normal.z + "\n";
			file.write(output);
		}
		for (Face f : faces) {
			String out = "f " + (f.vertex1+1) + " " + (f.vertex2+1) + " " + (f.vertex3+1) + "\n";
			file.write(out);
		}
		file.flush();
		file.close();
	}
}