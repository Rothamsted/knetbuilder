package net.sourceforge.ondex.ovtk2.util;

/**
 * Class wraps an Integer together with a descriptive String.
 * 
 * @author taubertj
 * 
 */
public class IntegerStringWrapper implements Comparable<Object> {

	// the Integer value
	private Integer i = null;

	// a descriptive String
	private String s = null;

	/**
	 * Constructor for all parameters.
	 * 
	 * @param i
	 *            Integer
	 * @param s
	 *            String
	 */
	public IntegerStringWrapper(Integer i, String s) {
		this.i = i;
		this.s = s;
	}

	/**
	 * Returns Integer value.
	 * 
	 * @return Integer
	 */
	public Integer getValue() {
		return i;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof IntegerStringWrapper) {
			return this.i.equals(((IntegerStringWrapper) arg0).i);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return i.hashCode();
	}

	@Override
	public String toString() {
		if (s.length() > 22) {
			// then trim it to a reasonable size
			return s.substring(0, 22) + "...";
		}
		return s;
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof IntegerStringWrapper)
			return s.toUpperCase().compareTo(((IntegerStringWrapper) o).s.toUpperCase());
		else
			return 1;
	}

	public String getDescription() {
		return s;
	}

}
