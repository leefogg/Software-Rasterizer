package engine.math;

public class Color {
	public static final Color
	red = new Color(255,0,0),
	green = new Color(0,255,0),
	blue = new Color(0,0,255),
	black = new Color(0,0,0),
	white = new Color(255,255,255),
	alpha = new Color(0,0,0,0);
	
	public float r, g, b, a;
	
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
	
	public void set(int argb) {
		set(
				(argb >> 24) & 0xFF,
				(argb >> 16) & 0xFF,
				(argb >> 8) & 0xFF,
				argb & 0xFF
			);	
	}
	public void set(int a, int r, int g, int b) {
		set(
			(float)a / 255f,
			(float)r / 255f,
			(float)g / 255f,
			(float)b / 255f
		);
	}
	public void set(int r, int g, int b) {
		set(
				0f,
				(float)r / 255f,
				(float)g / 255f,
				(float)b / 255f
			);
	}
	public void set(byte r, byte g, byte b) {
		set(
				0,
				(float)r / 255f,
				(float)g / 255f,
				(float)b / 255f
			);
	}
	
	public void set(float a, float r, float g, float b) {
		if (!isValidColor(a, r, g, b))
			throw new IllegalArgumentException("ARGB value outside of supported range.");
		
		this.a = a;
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public void set(Color c) {
		this.r = c.r;
		this.g = c.g;
		this.b = c.b; 
		this.a = c.a;
	}
	
	public Color add(Color c) {
		r += c.r;
		g += c.g;
		b += c.b;
		a += c.a;
		return this;
	}
	
	public Color reverseSubtract(Color c) {
		r = c.r - r;
		g = c.g - g;
		b = c.b - b;
		a = c.a - a;
		return this;
	}
	public Color subtract(Color c) {
		r -= c.r;
		g -= c.g;
		b -= c.b;
		a -= c.a;
		return this;
	}
	
	public Color multiply(Color c) {
		r *= c.r;
		g *= c.g;
		b *= c.b;
		a *= c.a;
		return this;
	}
	
	public Color multiply(float scale) {
		r *= scale;
		g *= scale;
		b *= scale;
		a *= scale;
		return this;
	}
	
	public void max(Color color) {
		a = Math.max(a, color.a);
		r = Math.max(r, color.r);
		g = Math.max(g, color.g);
		b = Math.max(b, color.b);
	}
	public void min(Color color) {
		a = Math.min(a, color.a);
		r = Math.min(r, color.r);
		g = Math.min(g, color.g);
		b = Math.min(b, color.b);
	}
	
	protected boolean isValidColor(float a, float r, float g, float b) {
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
	
	public void ensureScale() {
		a = Math.min(1, Math.max(0, a));
		r = Math.min(1, Math.max(0, r));
		g = Math.min(1, Math.max(0, g));
		b = Math.min(1, Math.max(0, b));
	}
	
	public void normalize() {
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
	
	public static boolean hasAlpha(int argb) {
		return ((argb >> 24) & 0xFF) < 255;
	}
	
	
	public Color clone() {
		return new Color(a,r,g,b);
	}
	
	public void clone(Color c) {
		c.a = a;
		c.r = r;
		c.g = g;
		c.b = b;
	}
	
	public java.awt.Color toColor() {
		return new java.awt.Color(r,g,b);
	}
	
	public int toARGB() {
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