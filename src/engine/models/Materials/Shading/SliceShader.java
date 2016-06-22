package engine.models.Materials.Shading;

public class SliceShader extends Shader {
	public float height;

	public SliceShader(float height) {
		this.height = height;
	}

	@Override
	public void shade() {
		worldPosition.y %= height;
		if (Math.abs(worldPosition.y) < height / 2) {
			getTextureColor();
			destinationColor.set(sourceColor);
		} else {
			sourceDepth = destinationDepth;
		}
	}
}
