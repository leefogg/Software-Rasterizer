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

public final class MTLLoader {

	public static Material[] load(String path) throws IOException, MalformException {
		ArrayList<Material> materials = new ArrayList<Material>(); 
		Material currentmaterial = null;
		
		BufferedReader mtlfilereader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(path))));
		while(!mtlfilereader.ready()){}
		
		String line;
		int linenumber=0;
		while ((line = mtlfilereader.readLine()) != null)  {
			linenumber++;
			String[] lineparts = line.split(" ");
			
			if (lineparts[0].toLowerCase().matches("newmtl ") && lineparts.length >= 2) {
				if (currentmaterial != null)
					materials.add(currentmaterial);
				
				currentmaterial = new Material(lineparts[1]);
			}
			
			if (currentmaterial != null) {
				if (lineparts[0].toLowerCase().matches("kd")) {
					if (lineparts.length < 4)
						throw new MalformException("Missing components from ambient color in material " + currentmaterial.name + " on line " + linenumber + ".");
					
					currentmaterial.ambientColor = new Color(
								Float.valueOf(lineparts[2]),
								Float.valueOf(lineparts[3]),
								Float.valueOf(lineparts[4])
							);
				} else if (lineparts[0].toLowerCase().matches("map_kd")) {
					if (lineparts.length < 2)
						throw new MalformException("Missing texture location in material " + currentmaterial.name + " on line " + linenumber + ".");
					
					String folder = path.substring(0, path.lastIndexOf("/") + 1);
					currentmaterial.texture = new ImageTexture(folder + lineparts[1]);
				}
			}
		}
		mtlfilereader.close();

		if (currentmaterial == null) { // No newmtl definitions found in file
			return new Material[0];
		}
		
		Material[] finalmaterials = new Material[materials.size()];
		for (int i=0; i<materials.size(); i++)
			finalmaterials[i] = materials.get(i);
		
		return finalmaterials;
	}
}
