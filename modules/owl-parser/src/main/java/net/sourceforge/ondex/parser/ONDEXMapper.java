package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * A marker interface to declare an ONDEX mapper, which maps ONDEX entities of type O from a source S. 
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public interface ONDEXMapper<O, S>
{
	/**
	 * the graph parameter is where result(s) of type O go(es), and also the component used to generate them.
	 */
	public O map ( S source, ONDEXGraph targetGraph );
}
