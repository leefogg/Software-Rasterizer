package engine.models.Materials.Shading;

import engine.math.Color;
import engine.math.Vector3;

public final class AmbientLightShader extends Shader {
	public Vector3 lightpos;
	public float dropoffDistance;
	
	public AmbientLightShader(Vector3 lightpos, float dropoffdistance) {
		this.lightpos = lightpos;
		this.dropoffDistance = dropoffdistance;
	}

	@Override
	public void shade() {
		Vector3 direction = Vector3.subtract(lightpos, FaceCenter);
		float dist = (float)direction.getMagnitude();
		if (dist > dropoffDistance) {
			destinationColor.set(Color.black);
			return;
		}
		float dot = Vector3.dotProduct(direction.normalize(), faceNormal);
		if (dot < 0) {
			destinationColor.set(Color.black);
			return;
		}
		
		float c = 1 - dist / dropoffDistance;
		c *= dot;
		sourceColor.set(1, c, c, c);
		destinationColor.set(sourceColor);
	}
}
