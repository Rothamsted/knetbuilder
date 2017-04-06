package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public interface ONDEXMapper<O, S>
{
	public O map ( S source, ONDEXGraph targetGraph );
}
