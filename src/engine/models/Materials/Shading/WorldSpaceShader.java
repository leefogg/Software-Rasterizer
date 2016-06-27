package engine.models.Materials.Shading;

import engine.math.CommonMath;

public final class WorldSpaceShader extends Shader {
	float width, height, depth;
	
	public WorldSpaceShader(float width, float height, float depth) {
		this.width = width;
		this.height = height;
		this.depth = depth;
	}
	
	@Override
	public void shade() {
		// TODO: Optimise
		sourceColor.r = CommonMath.map(Math.abs(worldPosition.x), -width, width, 0, 1);
		sourceColor.g = CommonMath.map(Math.abs(worldPosition.y), -height, height, 0, 1);
		sourceColor.b = CommonMath.map(Math.abs(worldPosition.z), -depth, depth, 0, 1);
	}
}
