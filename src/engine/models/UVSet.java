package engine.models;

public final class UVSet {
	public static final UVSet
	zero = new UVSet(0,0),
	middle = new UVSet(0.5f, 0.5f),
	end = new UVSet(1,1);
	
	float u, v;
	
	public UVSet(float u, float v) {
		set(u,v);
	}
	public void set(float u, float v) {		
		this.u = u;
		this.v = v;
	}
	
	public float getU() {
		return u;
	}
	
	public float getV() {
		return v;
	}
	
	public UVSet Clone() {
		return new UVSet(u,v);
	}
}
