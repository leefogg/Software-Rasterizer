package resources.loaders;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import engine.Face;
import engine.Texture;
import engine.Vertex;
import engine.math.Color;
import engine.math.Vector3;
import utils.Log;

public class OBJLoader {
	public static engine.Mesh load(String path) throws IOException, MalformException {
		Texture texture = Texture.error;
		ArrayList<Face> facelist = new ArrayList<Face>();
		ArrayList<Vertex> vertlist = new ArrayList<Vertex>();
		ArrayList<Vector3> uvlist = new ArrayList<Vector3>();
		
		BufferedReader objfilereader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(path))));
		while(!objfilereader.ready()){}
		
		String line;
		int linenumber=0;
		while ((line = objfilereader.readLine()) != null)  {
			linenumber++;
			
			if (line.startsWith("v ")) {
				String[] values = line.substring(2).split(" ");
				Vertex newvertex = null;
					if (values.length >= 3) {
						newvertex = new Vertex(new Vector3(
								Float.valueOf(values[0]),
								Float.valueOf(values[1]),
								Float.valueOf(values[2])
							));
					}
					if (values.length >= 6) {
						newvertex.color = new Color(
								Float.valueOf(values[3]),
								Float.valueOf(values[4]),
								Float.valueOf(values[5])
								);
					} 
					
					if (newvertex != null) {
						vertlist.add(newvertex);						
					} else {
						Log.writeLine("Error parsing vertex in obj file '" + path + "' on line " + linenumber);
						throw new MalformException("Error parsing vertex on line " + linenumber);
					}
			
			} else if (line.startsWith("vt ")) {
				String[] values = line.substring(3).split(" ");
				uvlist.add(new Vector3(
							Float.valueOf(values[0]),
							Float.valueOf(values[1]),
							0
						));
					
			} else if (line.startsWith("f ")) {
				String[] components = line.substring(2).split(" ");
				if (components.length > 3) {
					System.out.println("Non triangle detected. Skipping face.");
					continue;
				}
				
				//TODO: Make more efficient than splitting lots
				int[] indicies = new int[9];
				int i=0;
				for (String component : components) // Add X, Y, Z
					indicies[i++] = Integer.valueOf(component.split("/")[0])-1;
				for (String component : components) // Add vertex normals
					indicies[i++] = Integer.valueOf(component.split("/")[2])-1;
				for (String component : components) // Add U, V
					indicies[i++] = Integer.valueOf(component.split("/")[1])-1;
				
				Face newface = new Face(
						indicies[0],
						indicies[1],
						indicies[2]
						);

				newface.normal = calculateFaceNormal(
						vertlist.get(indicies[0]).position,
						vertlist.get(indicies[1]).position,
						vertlist.get(indicies[2]).position
						);
				
				facelist.add(newface);
				
				vertlist.get(indicies[0]).textureCoordinates = uvlist.get(indicies[6]);
				vertlist.get(indicies[1]).textureCoordinates = uvlist.get(indicies[7]);
				vertlist.get(indicies[2]).textureCoordinates = uvlist.get(indicies[8]);
			} else if (line.startsWith("tex")) {
				String folder = path.substring(0, path.lastIndexOf("/") + 1);
				texture = new Texture(folder + line.substring(4));
			}
		}
		
		objfilereader.close();
		
		
		// Convert to lists
		Vertex[] vertcies;
		vertcies = new Vertex[vertlist.size()];
		for (int i=0; i<vertlist.size(); i++)
			vertcies[i] = vertlist.get(i);
		
		Face[] faces;
		faces = new Face[facelist.size()];
		for (int i=0; i<facelist.size(); i++)
			faces[i] = facelist.get(i);
		
		//System.out.println("Loaded model with " + vertcies.length + " vertcies and " + faces.length + " faces.");
		return new engine.Mesh(vertcies, faces, texture);
	}
	
	// Reference: https://www.opengl.org/wiki/Calculating_a_Surface_Normal
	private static Vector3 calculateFaceNormal(Vector3 v1, Vector3 v2, Vector3 v3) {
		Vector3 normal = new Vector3(0);
		Vector3 u = Vector3.subtract(v2, v1);
		Vector3 v = Vector3.subtract(v3, v1);
		normal.x = (u.y * v.z) - (u.z * v.y);
		normal.y = (u.z * v.x) - (u.x * v.z);
		normal.z = (u.x * v.y) - (u.y * v.x);
		return normal;
	}
}
