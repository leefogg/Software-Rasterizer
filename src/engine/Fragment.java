package engine;

import engine.math.Color;
import engine.math.Vector3;
import engine.models.Texture;

public class Fragment {
	public Color sourceColor, destinationColor;
	public Vector3 worldPosition, FaceCenter, faceNormal;
	public int screenX, screenY;
	public float distanceToCamera;
	public Texture texture;
	
	public Fragment() {}
}
