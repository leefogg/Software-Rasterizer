package engine.math;
public final class Vector3 {
	public static final Vector3 
	zero = new Vector3(0),
	up = new Vector3(0,1,0);
	
	public float x, y, z;
	
	public Vector3() {}
	
	public Vector3(float all) {
		this(all, all, all);
	}
	public Vector3(double xpos, double ypos, double zpos) {
		this((float)xpos, (float)ypos, (float)zpos);
	}
	public Vector3(float xpos, float ypos, float zpos) {
		x = xpos;
		y = ypos;
		z = zpos;
	}
	
	public final void set(Vector3 other) {
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}
	public final void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public final double getMagnitude() {
		return Math.sqrt((x * x) + (y * y) + (z * z));
	}
	
	public final Vector3 normalize() {
		double length = getMagnitude();
		if (length == 0) return this;
		return divide((float)length);
	}
	
	public final Vector3 lookAt(Vector3 p) {
		return subtract(p).divide((float)p.getMagnitude());
	}
	
	public static Vector3 lookAt(Vector3 o, Vector3 p) {
		return Vector3.subtract(o, p).divide((float)p.getMagnitude());
	}
	
	public static final double getDistance(Vector3 p1, Vector3 p2) {
		float x = p1.x - p2.x;
		float y = p1.y - p2.y;
		float z = p1.z - p2.z;
		return Math.sqrt((x * x) + (y * y) + (z * z));
	}
	
	public static final float getSquaredDistance(Vector3 p1, Vector3 p2) {
		float x = p1.x - p2.x;
		float y = p1.y - p2.y;
		float z = p1.z - p2.z;
		return (x * x) + (y * y) + (z * z);
	}
	
	public final Vector3 crossProduct(Vector3 other) {
		float x = y*other.z - z*other.y;
		float y = z*other.x - x*other.z;
		float z = x*other.y - y*other.x;
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	public static Vector3 crossProduct(Vector3 vec1, Vector3 vec2) {
		return new Vector3(
				vec1.y*vec2.z - vec1.z*vec2.y,
				vec1.z*vec2.x - vec1.x*vec2.z,
				vec1.x*vec2.y - vec1.y*vec2.x
				);
	}
	
	public final float dotProduct(Vector3 other) {
		return dotProduct(this, other);
	}
	public static float dotProduct(Vector3 vec1, Vector3 vec2) {
		return vec1.x * vec2.x + vec1.y * vec2.y + vec1.z * vec2.z;
	}
	
	public final Vector3 scale(float scale) {
		x *= scale;
		y *= scale;
		z *= scale;
		return this;
	}
	public final Vector3 multiply(Vector3 vec) {
		x *= vec.x;
		y *= vec.y;
		z *= vec.z;
		return this;
	}
	public static Vector3 multiply(Vector3 vec1, Vector3 vec2) {
		return new Vector3(vec1.x * vec2.x, vec1.y * vec2.y, vec1.z * vec2.z);
	}
	public static Vector3 multiply(Vector3 vec1, float scaler) {
		return new Vector3(vec1.x * scaler, vec1.y * scaler, vec1.z * scaler);
	}
	
	public final Vector3 divide(float div) {
		x /= div;
		y /= div;
		z /= div;
		return this;
	}
	public final Vector3 divide(Vector3 vec) {
		x /= vec.x;
		y /= vec.y;
		z /= vec.z;
		return this;
	}
	public static Vector3 divide(Vector3 vec1, float div) {
		return new Vector3(vec1.x / div, vec1.y / div, vec1.z / div);
	}
	public static Vector3 divide(Vector3 vec1, Vector3 vec2) {
		return new Vector3(vec1.x / vec2.x, vec1.y / vec2.y, vec1.z / vec2.z);
	}
	
	public final Vector3 add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	public final Vector3 add(Vector3 vec) {
		x += vec.x;
		y += vec.y;
		z += vec.z;
		return this;
	}
	public static Vector3 add(Vector3 vec1, Vector3 vec2) {
		return add(vec1, vec2, new Vector3());
	}
	public static Vector3 add(Vector3 vec1, Vector3 vec2, Vector3 out) {
		out.set(vec1.x+vec2.x,
				vec1.y+vec2.y,
				vec1.z+vec2.z
				);
		
		return out;
	}
	
	public final Vector3 subtract(float x, float y, float z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}
	public final Vector3 subtract(Vector3 vec) {
		x -= vec.x;
		y -= vec.y;
		z -= vec.z;
		return this;
	}
	public static Vector3 subtract(Vector3 vec1, Vector3 vec2) {
		return subtract(vec1, vec2, new Vector3());
	}
	public static Vector3 subtract(Vector3 vec1, Vector3 vec2, Vector3 out) {
		out.set(vec1.x-vec2.x,
				vec1.y-vec2.y,
				vec1.z-vec2.z
				);
		return out;
	}
	
	public final Vector3 negate() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}
	
	public Vector3 Clone() {
		return new Vector3(x, y, z);
	}
	public final void Clone(Vector3 vector) {
		vector.x = x;
		vector.y = y;
		vector.z = z;
	}
	
	@Override
	public final String toString() {
		return "X: " + x + " Y: " + y + " Z: " + z;
	}
	
	public final String toGLSLConstructor() {
		return "Vec3(" + x + "," + y + "," + z + ")";
	}
	
}
