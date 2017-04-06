package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public interface GraphMapper<S> extends ONDEXMapper<ONDEXGraph, S>
{
	public default ONDEXGraph map ( S source )
	{
		return this.map ( source, null );
	}
}
