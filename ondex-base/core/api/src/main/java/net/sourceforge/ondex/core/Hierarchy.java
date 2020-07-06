package net.sourceforge.ondex.core;


/**
 * A hierarchy of types.
 * 
 * @author Matthew Pocock
 */
public interface Hierarchy<H> {

	/**
	 * Returns the direct parent in the hierarchy.
	 * 
	 * @return the parent, or null if this is the hierarchy root
	 */
	public H getSpecialisationOf();

	/**
	 * Sets the direct pareint in the hierarchy.
	 * 
	 * @param specialisationOf
	 *            new parent
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public void setSpecialisationOf(H specialisationOf)
			throws UnsupportedOperationException;

	/**
	 * Discover if this is a descendant of the <code>possibleAncestor</code>.
	 * <p/>
	 * <em>Implementation note:</em> Consider implementing by using
	 * Hierarchy.Helper.transitiveParent(a, b)
	 * 
	 * @param possibleAncestor
	 *            the <code>H</code> that may be a parent (direct or indirect)
	 * @return true if <code>possibleAncestor</code> is an ancestor, false
	 *         otherwise
	 */
	public boolean isAssignableTo(H possibleAncestor);

	/**
	 * Returns whether this RelationType is a transitive superclass of
	 * <code>possibleDescendant</code>.
	 * <p/>
	 * <em>Implementation note:</em> Consider implementing by using
	 * Hierarchy.Helper.transitiveParent(a, b)
	 * 
	 * @param possibleDescendant
	 *            the <code>H</code> that may be a child (direct or indirect)
	 * @return true if <code>possibleParent</code> is a descendant, false
	 *         otherwise
	 */
	boolean isAssignableFrom(H possibleDescendant);

	public static final class Helper {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static <H extends Hierarchy> boolean transitiveParent(
				Hierarchy<H> p, Hierarchy<H> c) {
			while (c != null) {
				if (c == p)
					return true;
				c = c.getSpecialisationOf();
			}

			return false;
		}
	}
}
