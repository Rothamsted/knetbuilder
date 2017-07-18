package net.sourceforge.ondex.parser2;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Jul 2017</dd></dl>
 *
 */
public interface Visitable<T>
{
	public default boolean isVisited ( T value ) {
		return false;
	}
	
	public default boolean setVisited ( T value, boolean isVisited ) {
		return this.isVisited ( value );
	}
	
	public default boolean setVisited ( T value ) {
		return this.setVisited ( value, true );
	}
}
