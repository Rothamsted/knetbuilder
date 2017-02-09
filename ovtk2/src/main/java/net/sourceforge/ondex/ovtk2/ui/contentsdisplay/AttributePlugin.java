package net.sourceforge.ondex.ovtk2.ui.contentsdisplay;

/**
 * Defines plugins that work on AttributeNames
 * 
 * @author hindlem
 * 
 */
public interface AttributePlugin {

	/**
	 * 
	 * @return the AttributeName if any that this producer works on
	 */
	public abstract String[] getAttributeNames();

}
