package net.sourceforge.ondex.parser.uniprot.xml.filter;


/**
 * 
 * @author peschr
 */
public interface ValueFilter {
	
	/**
	 * sets the state
	 * 
	 * @param state
	 */
	public void setState(boolean state);

	/**
	 * returns the internal state
	 * 
	 * @return
	 */
	public boolean getState();
	
	/**
	 * Returns the amount of the internal values
	 * @return int
	 */
	public int getSize();
	
	/**
	 * resets the internal state to the default value
	 */
	public void resetState();


	/**
	 * checks a value and sets the internal state. The internal state can be
	 * asked for by calling the <code>getState()<code> method
	 * 
	 * @param toCheck
	 *            the value which should be checked
	 */
	public void check(Object toCheck);
}
