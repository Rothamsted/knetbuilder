package net.sourceforge.ondex.ovtk2.util;

/**
 * Indicates this frame should be registered with the JInternalFrameSelector
 * 
 * @author hindlem
 * 
 */
public interface RegisteredFrame {

	/**
	 * 
	 * @return the name this frame should appear as in the menu
	 */
	public String getName();

	/**
	 * 
	 * @return the group this frame should appear as in the menu
	 */
	public String getGroup();

}
