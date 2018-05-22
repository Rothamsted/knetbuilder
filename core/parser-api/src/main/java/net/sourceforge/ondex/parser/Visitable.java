package net.sourceforge.ondex.parser;

/**
 * The main components in the ONDEX parser architecture are of type visitable. A Visitable interface is able to 
 * tell if an element (es, a data item or an ONDEX element) was already processed during the parsing process.
 * 
 * This might be useful or necessary in components like {@link ExploringMapper}, to avoid to fall in infinite loops, 
 * or to avoid to repeat entire exploring paths needlessly. When there is such a necessity, a component implementing this
 * interface (or its invoker) should set an element to visited by means of the {@link #setVisited(Object, boolean)}
 * method (or its {@link #setVisited(Object) shortcut}).
 * 
 * The interface has default implementations that makes an item always not visited.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Jul 2017</dd></dl>
 *
 */
public interface Visitable<T>
{
	/** 
	 * Always false, you need to override this to provide a true implementation. 
	 */
	public default boolean isVisited ( T value ) {
		return false;
	}
	
	/**
	 * @return the previous value.
	 */
	public default boolean setVisited ( T value, boolean isVisited ) {
		return this.isVisited ( value );
	}
	
	/**
	 * Defaults to true. 
	 */
	public default boolean setVisited ( T value ) {
		return this.setVisited ( value, true );
	}
}
