package engine.math;

import static engine.math.CommonMath.*;

public final class Color {
	public static final Color
	red = new Color(java.awt.Color.red),
	green = new Color(java.awt.Color.green),
	blue = new Color(java.awt.Color.blue),
	black = new Color(java.awt.Color.black),
	white = new Color(java.awt.Color.white),
	alpha = new Color(0x00000000);
	
	public float r, g, b, a = 1f;
	
	public Color(){}
	
	public Color(java.awt.Color color) {
		set(color.getRGB());
	}
	public Color(Color color) {
		set(
				color.a,
				color.r,
				color.g,
				color.b
			);
	}

	public Color(int argb) {
		set(argb);	
	}
	public Color(int a, int r, int g, int b) {
		set(
				(float)a / 255f,
				(float)r / 255f,
				(float)g / 255f,
				(float)b / 255f
			);
	}
	public Color(int r, int g, int b) {
		set(
				0,
				(float)r / 255f,
				(float)g / 255f,
				(float)b / 255f
			);
	}
	public Color(byte r, byte g, byte b) {
		set(
				0,
				(float)r / 255f,
				(float)g / 255f,
				(float)b / 255f
			);
	}
	public Color(float r, float g, float b) {
		set(0, r, g, b);
	}
	public Color(float a, float r, float g, float b) {
		set(a, r, g, b);
	}
	
	public final void set(int argb) {
		set(
				(argb >> 24) & 0xFF,
				(argb >> 16) & 0xFF,
				(argb >> 8) & 0xFF,
				argb & 0xFF
			);	
	}
	public final void set(int a, int r, int g, int b) {
		set(
			(float)a / 255f,
			(float)r / 255f,
			(float)g / 255f,
			(float)b / 255f
		);
	}
	public final void set(int r, int g, int b) {
		set(
				0f,
				(float)r / 255f,
				(float)g / 255f,
				(float)b / 255f
			);
	}
	public final void set(byte r, byte g, byte b) {
		set(
				0,
				(float)r / 255f,
				(float)g / 255f,
				(float)b / 255f
			);
	}
	
	public final void set(float a, float r, float g, float b) {
		if (!isValidColor(a, r, g, b))
			throw new IllegalArgumentException("ARGB value outside of supported range.");
		
		this.a = a;
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public final void set(Color c) {
		this.r = c.r;
		this.g = c.g;
		this.b = c.b; 
		this.a = c.a;
	}
	
	public final Color add(Color c) {
		add(c.a, c.r, c.g, c.b);
		return this;
	}
	public final Color add(float a, float r, float g, float b) {
		this.r += r;
		this.g += g;
		this.b += b;
		this.a += a;
		return this;
	}
	
	public final Color reverseSubtract(Color c) {
		r = c.r - r;
		g = c.g - g;
		b = c.b - b;
		a = c.a - a;
		return this;
	}
	public final Color subtract(Color c) {
		subtract(c.a, c.r, c.g, c.b);
		return this;
	}
	public final Color subtract(float a, float r, float g, float b) {
		this.r -= r;
		this.g -= g;
		this.b -= b;
		this.a -= a;
		return this;
	}
	
	public final Color multiply(Color c) {
		multiply(c.a, c.r, c.g, c.b);
		return this;
	}
	public final Color multiply(float a, float r, float g, float b) {
		this.r *= r;
		this.g *= g;
		this.b *= b;
		this.a *= a;
		return this;
	}
	public final Color multiply(float scale) {
		r *= scale;
		g *= scale;
		b *= scale;
		a *= scale;
		return this;
	}
	
	public final Color divide(Color c) {
		divide(c.a, c.r, c.g, c.b);
		return this;
	}
	public final Color divide(float a, float r, float g, float b) {
		this.r *= r;
		this.g *= g;
		this.b *= b;
		this.a *= a;
		return this;
	}
	public final Color divide(float scale) {
		r /= scale;
		g /= scale;
		b /= scale;
		a /= scale;
		return this;
	}
	
	public final void max(Color color) {
		a = Math.max(a, color.a);
		r = Math.max(r, color.r);
		g = Math.max(g, color.g);
		b = Math.max(b, color.b);
	}
	public final void min(Color color) {
		a = Math.min(a, color.a);
		r = Math.min(r, color.r);
		g = Math.min(g, color.g);
		b = Math.min(b, color.b);
	}
	
	private boolean isValidColor(float a, float r, float g, float b) {
		if (a > 1f) return false;
		if (r > 1f) return false;
		if (g > 1f) return false;
		if (b > 1f) return false;
		if (a < 0f) return false;
		if (r < 0f) return false;
		if (g < 0f) return false;
		if (b < 0f) return false;
		return true;
	}
	
	private final void ensureScale() {
		a = clamp(a);
		r = clamp(r);
		g = clamp(g);
		b = clamp(b);
	}
	
	public final void normalize() {
		float max = r;
		if (g > max)
			max = g;
		if (b > max)
			max = b;
		if (a > max)
			max = a;
		
		r /= max;
		g /= max;
		b /= max;
		a /= max;
	}
	
	public final static boolean hasAlpha(int argb) {
		return ((argb >> 24) & 0xFF) < 255;
	}
	
	public Color clone() {
		return new Color(a,r,g,b);
	}
	
	public final void clone(Color c) {
		c.a = a;
		c.r = r;
		c.g = g;
		c.b = b;
	}
	
	public java.awt.Color toColor() {
		return new java.awt.Color(r,g,b);
	}
	
	public final int toARGB() {
		ensureScale();
		int
		a = (int)(this.a * 255f),
		r = (int)(this.r * 255f),
		g = (int)(this.g * 255f),
		b = (int)(this.b * 255f);
		
		return (a << 24) | (r << 16) | (g << 8) | b; 
	}
	
	private float invertChannel(float a) {
		return Math.abs(a - 1);
	}
}