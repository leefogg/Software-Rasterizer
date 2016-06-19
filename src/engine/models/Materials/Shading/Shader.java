package engine.models.Materials.Shading;

import engine.Fragment;

public class Shader extends Fragment {
	
	public void shade() {
		destinationColor.set(sourceColor);
	}
}
