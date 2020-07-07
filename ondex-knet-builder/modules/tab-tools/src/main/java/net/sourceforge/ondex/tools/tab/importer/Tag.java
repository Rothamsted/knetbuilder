package net.sourceforge.ondex.tools.tab.importer;
/**
 * A class used to wrap identifier
 * @author lysenkoa
 *
 */
public class Tag {
	private Object tag = null;
	
	/**
	 * Creates the new tag with Identifier specified. Null is not allowed as an argument
	 * @param tag
	 * @throws IllegalArgumentException
	 */
	public Tag(Object tag) throws IllegalArgumentException{
		if(tag == null)
			throw new IllegalArgumentException("Resource identifier must not be null!");
		this.tag = tag;	
	}
	
	public Tag(){
		
	}
	/**
	 * Sets the new identifier. Null is not allowed as an argument
	 * @param tag
	 * @throws IllegalArgumentException when attempting to initialise with null
	 */
	public void setTag(Object tag) throws IllegalArgumentException{
		if(tag == null)
			throw new IllegalArgumentException("Resource identifier must not be null!");
		this.tag = tag;
	}
	/**
	 * Gets the current identifier
	 * @return
	 * @throws NullPointerException
	 */
	public Object getTag() throws NullPointerException{
		if (tag == null)
			throw new NullPointerException("Tag was not set for this object!");
		return tag;
	}
}
