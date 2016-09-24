package engine;


import engine.math.Matrix;
import engine.math.Vector3;

public class Camera {
	private Vector3 
	position = new Vector3(0,0,0),
	target = new Vector3(0,0,1);
	
	private int width, height;
	private float
	fov,
	znear, zfar; // TODO: Square to eliminate sqrt 
	
	Matrix 
	viewMatrix = new Matrix(),
	projectionMatrix = new Matrix();
	
	public Camera(float fov, int width, int height, float znear, float zfar) {
		set(fov, width, height, znear, zfar);
	}
	
	public void set(float fov, int width, int height, float znear, float zfar) {		
		this.fov = fov; // TOOD change to degrees
		this.width = width;
		this.height = height;
		this.znear = znear;
		this.zfar = zfar;
		
		updateViewMatrix();
		updateProjectionMatrix();
	}
	
	public Vector3 getPosition() {
		return position.Clone();
	}
	public void setPosition(Vector3 pos) {
		setPosition(pos.x, pos.y, pos.z);
	}
	public void setPosition(float x, float y, float z) {
		position.x = x;
		position.y = y;
		position.z = z;
		updateViewMatrix();
	}
	
	public Vector3 getTarget() {
		return target.Clone();
	}
	public void setTarget(Vector3 pos) {
		setTarget(pos.x, pos.y, pos.z);
	}
	public void setTarget(float x, float y, float z) {
		target.x = x;
		target.y = y;
		target.z = z;
		updateViewMatrix();
	}
	
	public int getWidth() { // TODO: Rename
		return width;
	}
	public void setWidth(int width) {// TOOD: Rename
		this.width = width;
		updateProjectionMatrix();
	}

	public int getHeight() { // TOOD: Rename
		return height;
	}
	public void setHeight(int height) {// TOOD: Rename
		this.height = height;
		updateProjectionMatrix();
	}

	
	public float getFov() {
		return fov;
	}
	public void setFov(float fov) {
		this.fov = fov;
		updateProjectionMatrix();
	}


	public float getZNear() {
		return znear;
	}
	public void setZNear(float znear) {
		this.znear = znear;
		updateProjectionMatrix();
	}

	public float getZFar() {
		return zfar;
	}
	public void setZFar(float zfar) {
		this.zfar = zfar;
		updateProjectionMatrix();
	}

	
	public Vector3 getAimDirection() {
		return getAimDirection(target);
	}
	public Vector3 getAimDirection(Vector3 target) {
		return Vector3.subtract(target, position);
	}
	
	public double getDistanceToCamera(Vector3 point) {
		return Vector3.getDistance(point, position);
	}
	public float getDistanceToCameraSquared(Vector3 point) {
		return Vector3.getSquaredDistance(point, position);
	}
	
	
	private void updateViewMatrix() {
		Matrix.lookAtLH(position, target, Vector3.up, viewMatrix);
	}
	private void updateProjectionMatrix() {
		 Matrix.PerspectiveFovLH(fov, (float)width / (float)height, znear, zfar, projectionMatrix);
	}
	
	public Camera Clone() {
		return new Camera(fov, width, height, znear, zfar);
	}
}