package net.sourceforge.ondex.core.persistent;

/**
 * Implements a tuple of Integer and String.
 * 
 * @author taubertj
 * 
 */
public class BerkeleyIntegerName {

	// key of type Integer
	private int key;

	// string name
	private String name = null;

	/**
	 * Constructor fills all variables.
	 * 
	 * @param key
	 *            Integer
	 * @param name
	 *            String
	 */
	public BerkeleyIntegerName(int key, String name) {
		this.key = key;
		if (name != null)
			this.name = name.intern();
	}

	/**
	 * Returns key from type Integer.
	 * 
	 * @return Integer
	 */
	public int getKey() {
		return key;
	}

	/**
	 * Returns associated name.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}
}
