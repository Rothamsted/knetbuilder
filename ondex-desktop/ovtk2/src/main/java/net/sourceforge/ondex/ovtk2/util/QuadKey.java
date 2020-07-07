package net.sourceforge.ondex.ovtk2.util;

/**
 * Stores quads of Strings as relation keys.
 * 
 * @author taubertj
 * 
 */
public class QuadKey {

	String[] key = new String[4];

	public QuadKey(String first, String second, String third, String forth) {
		key[0] = first;
		key[1] = second;
		key[2] = third;
		key[3] = forth;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof QuadKey) {
			QuadKey k = (QuadKey) arg0;
			return key[0].equals(k.key[0]) && key[1].equals(k.key[1]) && key[2].equals(k.key[2]) && key[3].equals(k.key[3]);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return key[0].hashCode() + 3 * key[1].hashCode() + 5 * key[2].hashCode() + 7 * key[3].hashCode();
	}

	@Override
	public String toString() {
		return "[" + key[0] + "," + key[1] + "," + key[2] + "," + key[3] + "]";
	}

}
