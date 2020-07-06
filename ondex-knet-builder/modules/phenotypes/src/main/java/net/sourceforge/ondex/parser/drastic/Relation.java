package net.sourceforge.ondex.parser.drastic;

public class Relation {

	private int r_from;

	private int r_to;

	private boolean reg = false;

	public Relation(int f, int t, boolean b) {

		this(f, t);
		reg = b;

	}

	public Relation(int f, int t) {

		r_from = f;
		r_to = t;

	}

	public int[] getRelation() {

		int[] tmp = new int[2];
		tmp[0] = r_from;
		tmp[1] = r_to;

		return tmp;
	}

	public int getFrom() {

		return r_from;
	}

	public int getTo() {

		return r_to;
	}

	public boolean getRegulation() {
		return reg;
	}

}
