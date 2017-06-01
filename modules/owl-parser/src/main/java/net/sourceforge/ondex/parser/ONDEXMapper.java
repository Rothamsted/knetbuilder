package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * A marker interface to declare an ONDEX mapper, which maps ONDEX entities of type O from a source S. Uses a type OT
 * to produce its output, eg, OT might be {@link ONDEXGraph}.
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public interface ONDEXMapper<O, S, OT>
{
	/**
	 * the ondexTarget parameter is usually used to build/store the result(s) of type O, 
	 */
	public O map ( S source, OT ondexTarget );
}
