package net.sourceforge.ondex.ovtk2.ui.gds;

/**
 * Interface for editor default values.
 * 
 * @author taubertj
 * 
 */
public interface GDSEditor {

	/**
	 * The default value of the editor.
	 * 
	 * @return default value
	 */
	public Object getDefaultValue();

	/**
	 * forces changes in progress to be flushed to attribute
	 */
	public void flushChanges();

}
