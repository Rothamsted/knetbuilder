package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * This is usually used for a top-level mapper, a component that takes a source of type S like a file and creates a 
 * corresponding {@link ONDEXGraph} which contains the ONDEX entities that are mapped from the source. 
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public interface GraphMapper<S> extends ONDEXMapper<ONDEXGraph, S, ONDEXGraph>
{
	/**
	 * Invokes {@link #map(Object, ONDEXGraph)} with a null graph. The implementation of such method should generate
	 * a new graph when receiving such null.
	 */
	public default ONDEXGraph map ( S source )
	{
		return this.map ( source, null );
	}
}
