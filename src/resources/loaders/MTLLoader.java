package resources.loaders;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import engine.math.Color;
import engine.models.Material;
import engine.models.Materials.ImageTexture;
import engine.models.Materials.UnsupportedDimensionException;

public final class MTLLoader {

	public static Material[] load(String path) throws IOException, MalformException, UnsupportedDimensionException {
		path = path.replace("\\\\", "/");
		
		ArrayList<Material> materials = new ArrayList<Material>(); 
		Material currentmaterial = null;
		BufferedReader mtlfilereader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(path))));
		while(!mtlfilereader.ready()){}
		
		String line;
		int linenumber=0;
		while ((line = mtlfilereader.readLine()) != null)  {
			linenumber++;
			String[] lineparts = line.split(" ");
			
			if (lineparts[0].toLowerCase().matches("newmtl") && lineparts.length >= 2) { // A new Material has been found
				if (currentmaterial != null) // If we have one..
					materials.add(currentmaterial); // save it
				
				currentmaterial = new Material(lineparts[1]); // and create the next
				continue;
			}
			
			if (currentmaterial != null) {
				if (lineparts[0].toLowerCase().matches("kd")) { // Ambient color
					if (lineparts.length < 4)
						throw new MalformException("Missing components from ambient color in material " + currentmaterial.name + " on line " + linenumber + ".");
					
					currentmaterial.ambientColor = new Color(
								Float.valueOf(lineparts[1]),
								Float.valueOf(lineparts[2]),
								Float.valueOf(lineparts[3])
							);
					continue;
				} else if (lineparts[0].toLowerCase().matches("map_kd")) { // Texture
					if (lineparts.length < 2)
						throw new MalformException("Missing texture location in material " + currentmaterial.name + " on line " + linenumber + ".");
					
					if (lineparts[1].contains(":")) { // Is absolute path
						currentmaterial.texture = new ImageTexture(lineparts[1]);
					} else {
						String folder = path.substring(0, path.lastIndexOf("/") + 1);
						String filepath = lineparts[1];
						filepath = filepath.replace("\\\\", "/");
						if (filepath.startsWith("/"))
							filepath = filepath.substring(1);
						
						currentmaterial.texture = new ImageTexture(folder + lineparts[1]);
					}
				}
			}
		}
		mtlfilereader.close();
		
		if (currentmaterial != null)
			materials.add(currentmaterial);
		else // No newmtl definitions found in file
			return new Material[0];
		
		Material[] finalmaterials = new Material[materials.size()];
		for (int i=0; i<materials.size(); i++)
			finalmaterials[i] = materials.get(i);
		
		return finalmaterials;
	}
}
