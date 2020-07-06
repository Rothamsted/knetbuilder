package net.sourceforge.ondex.tools.auxfunctions.tuples;

public class Pair {

	Object a;
	Object b;

	public Pair(Object x, Object y) {

		this.a = x;
		this.b = y;

	}

	public Object fst() {

		return this.a;

	}

	public Object snd() {

		return this.b;

	}

	public Object first() {

		return this.a;

	}

	public Object second() {

		return this.b;

	}

	public boolean equals(Object q) {

		if (q instanceof Pair) {
			Pair p = (Pair) q;
			return (p.fst().equals(a) && p.snd().equals(b));
		} else {
			return false;
		}

	}

	public int hashCode() {
		return a.hashCode() + 13 * b.hashCode();
	}

	public void fst(Object x) {
		this.a = x;
	}

	public void snd(Object x) {
		this.b = x;
	}

	public void first(Object x) {
		this.a = x;
	}

	public void second(Object x) {
		this.b = x;
	}

}
