package engine;

import engine.math.Color;
import engine.math.Vector3;
import engine.models.Texture;

public class Fragment {
	public Color 
	sourceColor = new Color(),
	destinationColor = new Color();
	public Vector3 worldPosition, FaceCenter, faceNormal;
	public int 
	screenX, screenY;
	public float
	u, v;
	public float destinationDepth, sourceDepth;
	public Texture texture;
	
	public Fragment() {}
	
	protected Color getTextureColor() {
		sourceColor.set(texture.map(u, v));
		return sourceColor;
	}
}
