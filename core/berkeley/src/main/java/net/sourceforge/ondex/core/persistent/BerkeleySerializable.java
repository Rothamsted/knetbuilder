package net.sourceforge.ondex.core.persistent;

/**
 * Things that can serialize to a byte array.
 * 
 * @author Matthew Pocock
 */
public interface BerkeleySerializable {

	/**
	 * Serialises this object to a byte array
	 * 
	 * @return byte array representation of this object
	 */
	public byte[] serialise();

	/**
	 * Serialises this object to a byte array
	 * 
	 * @param sid
	 * @return byte array representation of this object
	 */
	public byte[] serialise(long sid);
}
