package engine.math;

import java.util.Arrays;

public class Matrix {
	private float[] m = new float[16];

	public static final Matrix 
	identity = new Matrix(new float[]{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1});

	public Matrix() {}

	public Matrix(float[] m) {
		this.m = m;
	}

	public static float[] rotationX(float angle) {
		float[] m = new float[16];
		float s = (float)Math.sin(angle);
		float c = (float)Math.cos(angle);
		m[0] = 1.0f;
		m[15] = 1.0f;
		m[5] = c;
		m[10] = c;
		m[9] = -s;
		m[6] = s;
		return m;
	}

	public static float[] rotationY(float angle) {
		float[] m = new float[16];
		float s = (float)Math.sin(angle);
		float c = (float)Math.cos(angle);
		m[5] = 1f;
		m[15] = 1f;
		m[0] = c;
		m[2] = -s;
		m[8] = s;
		m[10] = c;
		return m;
	}
	public static float[] rotationZ(float angle) {
		float[] m = new float[16];
		float s = (float)Math.sin(angle);
		float c = (float)Math.cos(angle);
		m[10] = 1f;
		m[15] = 1f;
		m[0] = c;
		m[1] = s;
		m[4] = -s;
		m[5] = c;
		return m;
	}
	public static Matrix RotationYawPitchRoll(float yaw, float pitch, float roll) {
		return new Matrix(rotationY(yaw)).multiply(rotationX(pitch)).multiply(rotationZ(roll));
    }
	
	public static Matrix rotationAxis(Vector3 axis, float angle) {
        float s = (float)Math.sin(-angle);
        float c = (float)Math.cos(-angle);
        float c1 = 1 - c;
        axis.normalize();
        Matrix result = new Matrix();
        result.m[0] = (axis.x * axis.x) * c1 + c;
        result.m[1] = (axis.x * axis.y) * c1 - (axis.z * s);
        result.m[2] = (axis.x * axis.z) * c1 + (axis.y * s);
        result.m[3] = 0f;
        result.m[4] = (axis.y * axis.x) * c1 + (axis.z * s);
        result.m[5] = (axis.y * axis.y) * c1 + c;
        result.m[6] = (axis.y * axis.z) * c1 - (axis.x * s);
        result.m[7] = 0f;
        result.m[8] = (axis.z * axis.x) * c1 - (axis.y * s);
        result.m[9] = (axis.z * axis.y) * c1 + (axis.x * s);
        result.m[10] = (axis.z * axis.z) * c1 + c;
        result.m[11] = 0f;
        result.m[15] = 1f;
        return result;
    }
    
    public static Matrix lookAtLH(Vector3 eye, Vector3 target, Vector3 up) {
        Vector3 zAxis = Vector3.subtract(target, eye).normalize();
        Vector3 xAxis = Vector3.crossProduct(up, zAxis).normalize();
        Vector3 yAxis = Vector3.crossProduct(zAxis, xAxis).normalize();
        float ex = -Vector3.dotProduct(xAxis, eye);
        float ey = -Vector3.dotProduct(yAxis, eye);
        float ez = -Vector3.dotProduct(zAxis, eye);
        return new Matrix(new float[]{xAxis.x, yAxis.x, zAxis.x, 0, xAxis.y, yAxis.y, zAxis.y, 0, xAxis.z, yAxis.z, zAxis.z, 0, ex, ey, ez, 1f});
    }
    public static Matrix lookAtLH(Vector3 eye, Vector3 target, Vector3 up, Matrix old) {
        Vector3 zAxis = Vector3.subtract(target, eye).normalize();
        Vector3 xAxis = Vector3.crossProduct(up, zAxis).normalize();
        Vector3 yAxis = Vector3.crossProduct(zAxis, xAxis).normalize();
        float ex = -Vector3.dotProduct(xAxis, eye);
        float ey = -Vector3.dotProduct(yAxis, eye);
        float ez = -Vector3.dotProduct(zAxis, eye);
        old.m[0] = xAxis.x;
        old.m[1] = yAxis.x;
        old.m[2] = zAxis.x;
        old.m[3] = 0;
        old.m[4] = xAxis.y;
        old.m[5] = yAxis.y;
        old.m[6] = zAxis.y;
        old.m[7] = 0;
        old.m[8] = xAxis.z;
        old.m[9] = yAxis.z;
        old.m[10] = zAxis.z;
        old.m[11] = 0;
        old.m[12] = ex;
        old.m[13] = ey;
        old.m[14] = ez;
        old.m[15] = 1f;
        return old;
    }
    
    // TODO: Make reuse alias
    public static Matrix PerspectiveLH(float width, float height, float znear, float zfar) {
        Matrix matrix = new Matrix();
        matrix.m[0] = (2f * znear) / width;
        matrix.m[1] = matrix.m[2] = matrix.m[3] = 0f;
        matrix.m[5] = (2f * znear) / height;
        matrix.m[4] = matrix.m[6] = matrix.m[7] = 0f;
        matrix.m[10] = -zfar / (znear - zfar);
        matrix.m[8] = matrix.m[9] = 0f;
        matrix.m[11] = 1f;
        matrix.m[12] = matrix.m[13] = matrix.m[15] = 0f;
        matrix.m[14] = (znear * zfar) / (znear - zfar);
        return matrix;
    }
    
    // TODO: Make reuse alias
    public static Matrix PerspectiveFovLH(float fov, float aspect, float znear, float zfar) {
        Matrix matrix = new Matrix();
        float tan = (float)(1f / (Math.tan(fov * 0.5f)));
        matrix.m[0] = tan / aspect;
        matrix.m[1] = matrix.m[2] = matrix.m[3] = 0f;
        matrix.m[5] = tan;
        matrix.m[4] = matrix.m[6] = matrix.m[7] = 0f;
        matrix.m[8] = matrix.m[9] = 0f;
        matrix.m[10] = -zfar / (znear - zfar);
        matrix.m[11] = 1f;
        matrix.m[12] = matrix.m[13] = matrix.m[15] = 0f;
        matrix.m[14] = (znear * zfar) / (znear - zfar);
        return matrix;
    }
    
    public static Matrix transpose(Matrix matrix) {
        Matrix result = new Matrix();
        result.m[0] = matrix.m[0];
        result.m[1] = matrix.m[4];
        result.m[2] = matrix.m[8];
        result.m[3] = matrix.m[12];
        result.m[4] = matrix.m[1];
        result.m[5] = matrix.m[5];
        result.m[6] = matrix.m[9];
        result.m[7] = matrix.m[13];
        result.m[8] = matrix.m[2];
        result.m[9] = matrix.m[6];
        result.m[10] = matrix.m[10];
        result.m[11] = matrix.m[14];
        result.m[12] = matrix.m[3];
        result.m[13] = matrix.m[7];
        result.m[14] = matrix.m[11];
        result.m[15] = matrix.m[15];
        return result;
    }
    
    public static Matrix scaling(float x, float y, float z) {
        Matrix result = new Matrix();
        result.m[0] = x;
        result.m[5] = y;
        result.m[10] = z;
        result.m[15] = 1f;
        return result;
    }
    public static Matrix translation(float x, float y, float z) {
        Matrix result = Matrix.identity.Clone();
        result.m[12] = x;
        result.m[13] = y;
        result.m[14] = z;
        return result;
    }

	public float determinant() {
		float temp1 = (this.m[10] * this.m[15]) - (this.m[11] * this.m[14]);
		float temp2 = (this.m[9] * this.m[15]) - (this.m[11] * this.m[13]);
		float temp3 = (this.m[9] * this.m[14]) - (this.m[10] * this.m[13]);
		float temp4 = (this.m[8] * this.m[15]) - (this.m[11] * this.m[12]);
		float temp5 = (this.m[8] * this.m[14]) - (this.m[10] * this.m[12]);
		float temp6 = (this.m[8] * this.m[13]) - (this.m[9] * this.m[12]);
		return ((((this.m[0] * (((this.m[5] * temp1) - (this.m[6] * temp2)) + (this.m[7] * temp3))) - (this.m[1] * (((this.m[4] * temp1) - (this.m[6] * temp4)) + (this.m[7] * temp5)))) + (this.m[2] * (((this.m[4] * temp2) - (this.m[5] * temp4)) + (this.m[7] * temp6)))) - (this.m[3] * (((this.m[4] * temp3) - (this.m[5] * temp5)) + (this.m[6] * temp6))));
	}

	public void invert() {
		float l1 = this.m[0];
		float l2 = this.m[1];
		float l3 = this.m[2];
		float l4 = this.m[3];
		float l5 = this.m[4];
		float l6 = this.m[5];
		float l7 = this.m[6];
		float l8 = this.m[7];
		float l9 = this.m[8];
		float l10 = this.m[9];
		float l11 = this.m[10];
		float l12 = this.m[11];
		float l13 = this.m[12];
		float l14 = this.m[13];
		float l15 = this.m[14];
		float l16 = this.m[15];
		float l17 = (l11 * l16) - (l12 * l15);
		float l18 = (l10 * l16) - (l12 * l14);
		float l19 = (l10 * l15) - (l11 * l14);
		float l20 = (l9 * l16) - (l12 * l13);
		float l21 = (l9 * l15) - (l11 * l13);
		float l22 = (l9 * l14) - (l10 * l13);
		float l23 = ((l6 * l17) - (l7 * l18)) + (l8 * l19);
		float l24 = -(((l5 * l17) - (l7 * l20)) + (l8 * l21));
		float l25 = ((l5 * l18) - (l6 * l20)) + (l8 * l22);
		float l26 = -(((l5 * l19) - (l6 * l21)) + (l7 * l22));
		float l27 = 1.0f / ((((l1 * l23) + (l2 * l24)) + (l3 * l25)) + (l4 * l26));
		float l28 = (l7 * l16) - (l8 * l15);
		float l29 = (l6 * l16) - (l8 * l14);
		float l30 = (l6 * l15) - (l7 * l14);
		float l31 = (l5 * l16) - (l8 * l13);
		float l32 = (l5 * l15) - (l7 * l13);
		float l33 = (l5 * l14) - (l6 * l13);
		float l34 = (l7 * l12) - (l8 * l11);
		float l35 = (l6 * l12) - (l8 * l10);
		float l36 = (l6 * l11) - (l7 * l10);
		float l37 = (l5 * l12) - (l8 * l9);
		float l38 = (l5 * l11) - (l7 * l9);
		float l39 = (l5 * l10) - (l6 * l9);
		this.m[0] = l23 * l27;
		this.m[4] = l24 * l27;
		this.m[8] = l25 * l27;
		this.m[12] = l26 * l27;
		this.m[1] = -(((l2 * l17) - (l3 * l18)) + (l4 * l19)) * l27;
		this.m[5] = (((l1 * l17) - (l3 * l20)) + (l4 * l21)) * l27;
		this.m[9] = -(((l1 * l18) - (l2 * l20)) + (l4 * l22)) * l27;
		this.m[13] = (((l1 * l19) - (l2 * l21)) + (l3 * l22)) * l27;
		this.m[2] = (((l2 * l28) - (l3 * l29)) + (l4 * l30)) * l27;
		this.m[6] = -(((l1 * l28) - (l3 * l31)) + (l4 * l32)) * l27;
		this.m[10] = (((l1 * l29) - (l2 * l31)) + (l4 * l33)) * l27;
		this.m[14] = -(((l1 * l30) - (l2 * l32)) + (l3 * l33)) * l27;
		this.m[3] = -(((l2 * l34) - (l3 * l35)) + (l4 * l36)) * l27;
		this.m[7] = (((l1 * l34) - (l3 * l37)) + (l4 * l38)) * l27;
		this.m[11] = -(((l1 * l35) - (l2 * l37)) + (l4 * l39)) * l27;
		this.m[15] = (((l1 * l36) - (l2 * l38)) + (l3 * l39)) * l27;
	}

	public Matrix multiply(Matrix other) {
		multiply(other.m);
		return this;
	}
	public Matrix multiply(float[] other) {
		float[] m = new float[16];
		m[0] = this.m[0] * other[0] + this.m[1] * other[4] + this.m[2] * other[8] + this.m[3] * other[12];
		m[1] = this.m[0] * other[1] + this.m[1] * other[5] + this.m[2] * other[9] + this.m[3] * other[13];
		m[2] = this.m[0] * other[2] + this.m[1] * other[6] + this.m[2] * other[10] + this.m[3] * other[14];
		m[3] = this.m[0] * other[3] + this.m[1] * other[7] + this.m[2] * other[11] + this.m[3] * other[15];
		m[4] = this.m[4] * other[0] + this.m[5] * other[4] + this.m[6] * other[8] + this.m[7] * other[12];
		m[5] = this.m[4] * other[1] + this.m[5] * other[5] + this.m[6] * other[9] + this.m[7] * other[13];
		m[6] = this.m[4] * other[2] + this.m[5] * other[6] + this.m[6] * other[10] + this.m[7] * other[14];
		m[7] = this.m[4] * other[3] + this.m[5] * other[7] + this.m[6] * other[11] + this.m[7] * other[15];
		m[8] = this.m[8] * other[0] + this.m[9] * other[4] + this.m[10] * other[8] + this.m[11] * other[12];
		m[9] = this.m[8] * other[1] + this.m[9] * other[5] + this.m[10] * other[9] + this.m[11] * other[13];
		m[10] = this.m[8] * other[2] + this.m[9] * other[6] + this.m[10] * other[10] + this.m[11] * other[14];
		m[11] = this.m[8] * other[3] + this.m[9] * other[7] + this.m[10] * other[11] + this.m[11] * other[15];
		m[12] = this.m[12] * other[0] + this.m[13] * other[4] + this.m[14] * other[8] + this.m[15] * other[12];
		m[13] = this.m[12] * other[1] + this.m[13] * other[5] + this.m[14] * other[9] + this.m[15] * other[13];
		m[14] = this.m[12] * other[2] + this.m[13] * other[6] + this.m[14] * other[10] + this.m[15] * other[14];
		m[15] = this.m[12] * other[3] + this.m[13] * other[7] + this.m[14] * other[11] + this.m[15] * other[15];
		this.m = m;
		return this;
	}
	
	public static Matrix multiply(Matrix left, Matrix right) {
		Matrix out = new Matrix();
		out.m[0] = left.m[0] * right.m[0] + left.m[1] * right.m[4] + left.m[2] * right.m[8] + left.m[3] * right.m[12];
		out.m[1] = left.m[0] * right.m[1] + left.m[1] * right.m[5] + left.m[2] * right.m[9] + left.m[3] * right.m[13];
		out.m[2] = left.m[0] * right.m[2] + left.m[1] * right.m[6] + left.m[2] * right.m[10] + left.m[3] * right.m[14];
		out.m[3] = left.m[0] * right.m[3] + left.m[1] * right.m[7] + left.m[2] * right.m[11] + left.m[3] * right.m[15];
		out.m[4] = left.m[4] * right.m[0] + left.m[5] * right.m[4] + left.m[6] * right.m[8] + left.m[7] * right.m[12];
		out.m[5] = left.m[4] * right.m[1] + left.m[5] * right.m[5] + left.m[6] * right.m[9] + left.m[7] * right.m[13];
		out.m[6] = left.m[4] * right.m[2] + left.m[5] * right.m[6] + left.m[6] * right.m[10] + left.m[7] * right.m[14];
		out.m[7] = left.m[4] * right.m[3] + left.m[5] * right.m[7] + left.m[6] * right.m[11] + left.m[7] * right.m[15];
		out.m[8] = left.m[8] * right.m[0] + left.m[9] * right.m[4] + left.m[10] * right.m[8] + left.m[11] * right.m[12];
		out.m[9] = left.m[8] * right.m[1] + left.m[9] * right.m[5] + left.m[10] * right.m[9] + left.m[11] * right.m[13];
		out.m[10] = left.m[8] * right.m[2] + left.m[9] * right.m[6] + left.m[10] * right.m[10] + left.m[11] * right.m[14];
		out.m[11] = left.m[8] * right.m[3] + left.m[9] * right.m[7] + left.m[10] * right.m[11] + left.m[11] * right.m[15];
		out.m[12] = left.m[12] * right.m[0] + left.m[13] * right.m[4] + left.m[14] * right.m[8] + left.m[15] * right.m[12];
		out.m[13] = left.m[12] * right.m[1] + left.m[13] * right.m[5] + left.m[14] * right.m[9] + left.m[15] * right.m[13];
		out.m[14] = left.m[12] * right.m[2] + left.m[13] * right.m[6] + left.m[14] * right.m[10] + left.m[15] * right.m[14];
		out.m[15] = left.m[12] * right.m[3] + left.m[13] * right.m[7] + left.m[14] * right.m[11] + left.m[15] * right.m[15];
		
		return out;
	}
	
	public static Vector3 transformCoordinates(Vector3 pos, Matrix mat) {
		float x = (pos.x * mat.m[0]) + (pos.y * mat.m[4]) + (pos.z * mat.m[8]) + mat.m[12];
        float y = (pos.x * mat.m[1]) + (pos.y * mat.m[5]) + (pos.z * mat.m[9]) + mat.m[13];
        float z = (pos.x * mat.m[2]) + (pos.y * mat.m[6]) + (pos.z * mat.m[10]) + mat.m[14];
        float w = (pos.x * mat.m[3]) + (pos.y * mat.m[7]) + (pos.z * mat.m[11]) + mat.m[15];
        return new Vector3(x / w, y / w, z / w);
	}
	public Vector3 transformCoordinates(Vector3 pos) {
		return transformCoordinates(pos, this);
	}
	
	public static Vector3 transformNormal(Vector3 pos, Matrix mat) {
		float x = (pos.x * mat.m[0]) + (pos.y * mat.m[4]) + (pos.z * mat.m[8]);
        float y = (pos.x * mat.m[1]) + (pos.y * mat.m[5]) + (pos.z * mat.m[9]);
        float z = (pos.x * mat.m[2]) + (pos.y * mat.m[6]) + (pos.z * mat.m[10]);
        return new Vector3(x, y, z);
	}
	public Vector3 transformNormal(Vector3 pos) {
		return transformNormal(pos, this);
	}

	public boolean equals(Matrix value) {
		return (this.m[0] == value.m[0] && this.m[1] == value.m[1] && this.m[2] == value.m[2] && this.m[3] == value.m[3] && this.m[4] == value.m[4] && this.m[5] == value.m[5] && this.m[6] == value.m[6] && this.m[7] == value.m[7] && this.m[8] == value.m[8] && this.m[9] == value.m[9] && this.m[10] == value.m[10] && this.m[11] == value.m[11] && this.m[12] == value.m[12] && this.m[13] == value.m[13] && this.m[14] == value.m[14] && this.m[15] == value.m[15]);
	};

	public Matrix Clone() {
		return new Matrix(Arrays.copyOf(m, m.length));
	}
}