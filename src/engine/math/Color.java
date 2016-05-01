package engine.math;

public class Color {
	public static final Color
	red = new Color(255,0,0),
	green = new Color(0,255,0),
	blue = new Color(0,0,255),
	black = new Color(0,0,0),
	white = new Color(255,255,255);
	
	//TODO: subtractive and additive modes
	public float r, g, b; // TODO: Alpha

	public Color(double r, double g, double b) {
		this(
				(float)r,
				(float)g,
				(float)b
			);
	}
	public Color(int rgba) {
		this(
				rgba & 0xFF,
				(rgba >> 8) & 0xFF,
				(rgba >> 16) & 0xFF
			);	
	}
	public Color(int r, int g, int b) {
		this(
				(float)r / 255f,
				(float)g / 255f,
				(float)b / 255f
			);
	}
	public Color(float r, float g, float b) {
		if (!isValidColor(r, g, b))
			throw new IllegalArgumentException("RGB value outside of supported range.");
		
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public Color add(Color c) {
		r += c.r;
		g += c.g;
		b += c.b;
		ensureScale();
		return this;
	}
	
	public Color subtract(Color c) {
		r -= c.r;
		g -= c.g;
		b -= c.b;
		ensureScale();
		return this;
	}
	
	public Color multiply(Color c) {
		r *= c.r;
		g *= c.g;
		b *= c.b;
		// Ensure scale is not needed
		// as multiplying two decimals always results in a decimal
		return this;
	}
	
	public Color multiply(float scale) {
		r *= scale;
		g *= scale;
		b *= scale;
		// Ensure scale is not needed
		// as multiplying two decimals always results in a decimal
		return this;
	}
	
	
	private boolean isValidColor(float r, float g, float b) {
		return  r >= 0f && r <= 1f &&
				g >= 0f && g <= 1f &&
				b >= 0f && b <= 1f;
	}
	
	private void ensureScale() {
		r = Math.min(1, Math.max(0, r));
		g = Math.min(1, Math.max(0, g));
		b = Math.min(1, Math.max(0, b));
	}
	
	public Color clone() {
		return new Color(r,g,b);
	}
	
	public java.awt.Color toColor() {
		return new java.awt.Color(r,g,b);
	}
	
	public int toARGB() {
		int
		r = (int)(this.r * 255f),
		g = (int)(this.g * 255f),
		b = (int)(this.b * 255f);
		
		return (0xFF000000) | (r << 16) | (g << 8) | b; 
	}

}