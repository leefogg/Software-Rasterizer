package engine.models.Materials.Shading;

public final class NormalShader extends Shader {

	@Override
	public void shade() {
		sourceColor.r = 0.5f + (faceNormal.x / 2);
		sourceColor.g = 0.5f + (faceNormal.y / 2);
		sourceColor.b = 0.5f + (faceNormal.z / 2);
		
		destinationColor.set(sourceColor);
	}
}
