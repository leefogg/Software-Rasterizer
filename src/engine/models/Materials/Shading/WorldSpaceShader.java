package engine.models.Materials.Shading;

import engine.math.Color;
import engine.math.CommonMath;

public final class WorldSpaceShader extends Shader {
	float width, height, depth;
	private Color transformed = new Color();
	
	public WorldSpaceShader(float width, float height, float depth) {
		this.width = width;
		this.height = height;
		this.depth = depth;
	}
	
	@Override
	public void shade() {
		transformed.r = CommonMath.map(Math.abs(worldPosition.x), -width, width, 0, 1);
		transformed.g = CommonMath.map(Math.abs(worldPosition.y), -height, height, 0, 1);
		transformed.b = CommonMath.map(Math.abs(worldPosition.z), -depth, depth, 0, 1);
		
		destinationColor.set(transformed);
	}
}
