package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * Pair mappers are often needed to map two data source items to a single ONDEX element (e.g., a relation).
 * 
 * Note that this doesn't extend {@link Visitable}, since that interface assumes it is dealing with one generic type
 * of data source only and here we have two of them. There are mappers (e.g., {@link ConceptMapper} for which it is 
 * clear that only one of the two parameters passed to the pair mapper might need to be tested for already-done visits, 
 * and in that cases the mapper should extend/implement both this interface and {@link Visitable}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>6 Jun 2017</dd></dl>
 *
 */
public interface PairMapper<S1, S2, O>
{
	public O map ( S1 src1, S2 src2, ONDEXGraph graph );
	
	public default O map ( S1 src1, S2 src2 ) {
		return this.map ( src1, src2, null );
	}
}
