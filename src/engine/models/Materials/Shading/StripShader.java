package engine.models.Materials.Shading;

import engine.math.Color;

public class StripShader extends Shader {
	public float height, offset;
	public Color color1, color2;

	public StripShader(float height, Color color1, Color color2) {
		this(height, 0, color1, color2);
	}
	public StripShader(float height, float offset, Color color1, Color color2) {
		this.height = height;
		this.color1 = color1;
		this.color2 = color2;		
	}

	@Override
	public void shade() {
		worldPosition.y += offset;
		worldPosition.y %= height;
		if (Math.abs(worldPosition.y) < height / 2) {
			destinationColor.set(color1);
		} else {
			destinationColor.set(color2);
		}
	}
}
