package net.sourceforge.ondex.core;

/**
 * Does implement a hierarchy for meta data elements.
 * 
 * @author Matthew Pocock
 * @param <H>
 */
public interface Instantiation<H extends Hierarchy<H>> {

	/**
	 * Returns whether this is inherited from the given hierarchy member.
	 * <p/>
	 * This is the case when its 'ofType h' either equals 'h' or is a transitive
	 * specialisation of 'h'.
	 * 
	 * @param h
	 *            the hierarchy member against which to test.
	 * @return whether the above holds.
	 */
	public boolean inheritedFrom(H h);

	/**
	 * Fetches the type that this instance belongs to
	 * 
	 * @return the type of the instance
	 */
	public H getOfType();
}
