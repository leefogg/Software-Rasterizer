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
	
	public void setPosition(float x, float y, float z) {
		position.x = x;
		position.y = y;
		position.z = z;
		updateViewMatrix();
	}
	public void setPosition(Vector3 pos) {
		position = pos;
		updateViewMatrix();
	}
	
	public void setTarget(Vector3 pos) {
		target = pos;
		updateViewMatrix();
	}
	
	public Vector3 getAimDirection() {
		return target.Clone().subtract(position).normalize();
	}
	
	public double getDistanceFromCamera(Vector3 point) {
		return Vector3.getDistance(point, position);
	}
	
	private void updateViewMatrix() {
		Matrix.lookAtLH(position, target, Vector3.up, viewMatrix);
	}
	
	public Camera Clone() {
		return new Camera(position, target);
	}
}