package net.sourceforge.ondex.ovtk2.util;

/**
 * Class to support id tracking of concepts within search results.
 * 
 * @author taubertj
 * @version 14.08.2008
 */
public class IdLabel {

	Integer id;

	String label;

	/**
	 * Simply sets a id and the String label
	 * 
	 * @param id
	 *            Integer
	 * @param label
	 *            String
	 */
	public IdLabel(Integer id, String label) {
		this.id = id;
		this.label = label;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof IdLabel && ((IdLabel) obj).id.equals(id);
	}

	public Integer getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return label;
	}
}
