/*
 * Created on 26-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.sink;

/**
 * Prototype for a concept name
 * 
 * @author taubertj
 */
public class ConceptName {

	private String name; // required

	private boolean preferred = false; // required

	/**
	 * Create a new ConceptName for KEGG internal use.
	 * 
	 * @param name
	 *            actual name
	 */
	public ConceptName(String name, boolean preferred) {

		// check name
		if (name == null)
			throw new NullPointerException("Name is null");
		this.name = name;
		
		this.preferred = preferred;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ConceptName) {
			ConceptName cn = (ConceptName) o;
			return this.name.equals(cn.getName());
		}
		return false;
	}

	/**
	 * Returns actual name.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Returns whether or not this name is preferred.
	 * 
	 * @return boolean
	 */
	public boolean isPreferred() {
		return preferred;
	}

	/**
	 * Sets the preferred flag for this name.
	 * 
	 * @param preferred
	 *            boolean
	 */
	public void setPreferred(boolean preferred) {
		this.preferred = preferred;
	}

	@Override
	public String toString() {
		return "ConceptName:" + name;
	}
}
