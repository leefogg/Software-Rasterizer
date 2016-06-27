package engine.models.Materials.Shading;

public class SliceShader extends Shader {
	public float height, offset;

	public SliceShader(float height) {
		this(height, 0);
	}
	public SliceShader(float height, float offset) {
		this.height = height;
		this.offset = offset;
	}

	@Override
	public void shade() {
		worldPosition.y += offset;
		worldPosition.y %= height;
		if (Math.abs(worldPosition.y) < height / 2) {
			getTextureColor();
		} else {
			sourceDepth = destinationDepth;
			sourceColor.set(destinationColor);
		}
	}
}
