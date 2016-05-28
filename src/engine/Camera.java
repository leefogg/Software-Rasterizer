package engine;


import engine.math.Matrix;
import engine.math.Vector3;

public class Camera {
	private Vector3 position, target;
	
	public Matrix viewMatrix;
	
	public Camera(Vector3 position, Vector3 target) {
		this.position = position;
		this.target = target;
		
		viewMatrix = Matrix.lookAtLH(position, target, Vector3.up);
	}
	
	public Vector3 getPosition() {
		return position.Clone();
	}
	
	public void setPosition(Vector3 pos) {
		position = pos;
		Matrix.lookAtLH(position, target, Vector3.up, viewMatrix);
	}
	
	public void setTarget(Vector3 pos) {
		target = pos;
		Matrix.lookAtLH(position, target, Vector3.up, viewMatrix);
	}
	
	public Vector3 getAimDirection() {
		return target.Clone().subtract(position).normalize();
	}
	
	public Camera Clone() {
		return new Camera(position, target);
	}
}