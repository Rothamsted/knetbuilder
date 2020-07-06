package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ONDEXRelation;

/**
 * A marker of a mapper that produces new {@link ONDEXRelation} starting from two data source items.
 * 
 * @see ConceptBasedRelMapper.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Jul 2017</dd></dl>
 *
 */
public interface RelationMapper<S1, S2> extends PairMapper<S1, S2, ONDEXRelation> 
{
}
