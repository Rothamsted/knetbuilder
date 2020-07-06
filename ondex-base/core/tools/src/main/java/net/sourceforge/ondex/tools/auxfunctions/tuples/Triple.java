package net.sourceforge.ondex.tools.auxfunctions.tuples;

public class Triple {
	
	Object a;
	Object b;
	Object c;
	
	public Triple (Object x, Object y, Object z) {
		
		this.a = x;
		this.b = y;
		this.c = z;
		
	}
	
	public Object first() {
		
		return this.a;
		
	}
	
	public Object second() {
		
		return this.b;
		
	}
	
	public Object third() {
		
		return this.c;
		
	}
	
	
	public boolean equals(Object q) {
		
		if (q instanceof Triple) {
			Triple p = (Triple) q;
			return (p.first().equals(a) && p.second().equals(b) && p.third().equals(c));
		} else {
			return false;
		}

	}
	
	public int hashCode() { 
		return 0;
	}
	
	public void first (Object x) {
		this.a = x; 
	}
	
	public void second (Object x) {
		this.b = x;
	}
	
	public void third (Object x) {
		this.c = x;
	}

	
}
