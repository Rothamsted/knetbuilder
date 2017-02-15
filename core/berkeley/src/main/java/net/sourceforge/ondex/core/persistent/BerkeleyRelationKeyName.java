package net.sourceforge.ondex.core.persistent;


/**
 * Implements a tuple of RelationKey and String.
 * 
 * @author taubertj
 * 
 */
public class BerkeleyRelationKeyName {

	// key of type BerkeleyRelationKey
	private BerkeleyRelationKey key;

	// string name
	private String name = null;

	/**
	 * Constructor fills all variables.
	 * 
	 * @param key
	 *            BerkeleyRelationKey
	 * @param name
	 *            String
	 */
	public BerkeleyRelationKeyName(BerkeleyRelationKey key, String name) {
		this.key = key;
		if (name != null)
			this.name = name.intern();
	}

	/**
	 * Returns key from type BerkeleyRelationKey.
	 * 
	 * @return BerkeleyRelationKey
	 */
	public BerkeleyRelationKey getKey() {
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
