package engine.math;

public final class CommonMath {
	public static boolean isInRange(float value, float min, float max) {
		if (value > max) return false;
		if (value < min) return false;
		return true;
	}
	
	public static float clamp(float value) {
		return clamp(value, 0, 1);
	}
	public static float clamp(float value, float min, float max) {
		if (value > max) return max;
		if (value < min) return min;
		return value;
	}
	public static int clamp(int value, int min, int max) {
		if (value > max) return max;
		if (value < min) return min;
		return value;
	}
	
	public static float interpolate(float min, float max, float gradient) {
		return min + (max - min) * gradient;
	}
	
	public static float map(float x, float in_min, float in_max, float out_min, float out_max) {
		x -= in_min;
		x *= out_max - out_min;
		x /= in_max - in_min;
		x += out_min;
		return x;
	}
	
	public static boolean isPowerOfTwo(int x) {
		return (x & (~x + 1)) == x;
	}
	
	public static int powerOfTwo(int x) {
		if (x == 1) return 0;
		
		int n = 2;
		int bit = 1;
		while (n != x) {
			bit++;
			n *= 2;
		}
		
		return bit;
	}
}
