package engine.models.Materials.Shading;

import engine.math.Color;
import engine.math.Vector3;

public final class AmbientLightShader extends Shader {
	public Vector3 lightpos;
	public float dropoffDistance;
	
	private Vector3 direction = new Vector3();
	
	public AmbientLightShader(Vector3 lightpos, float dropoffdistance) {
		this.lightpos = lightpos;
		this.dropoffDistance = dropoffdistance;
	}

	@Override
	public void shade() {
		Vector3.subtract(lightpos, worldPosition, direction);
		float dist = (float)direction.getMagnitude();
		if (dist > dropoffDistance) {
			destinationColor.set(Color.black);
			return;
		}
		float dot = faceNormal.dotProduct(direction.normalize());
		if (dot < 0) {
			sourceColor.set(Color.black);
			return;
		}
		
		float c = 1 - (dist / dropoffDistance);
		c *= dot;
		
		getTextureColor();
		sourceColor.multiply(1, c, c, c);
	}
}
