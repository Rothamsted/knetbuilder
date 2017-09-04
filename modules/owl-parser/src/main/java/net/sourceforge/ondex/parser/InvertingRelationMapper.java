package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ONDEXRelation;

/** 
 * Inverts the inputs to build an ONDEX relation. @see {@link InvertingPairMapper}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 Aug 2017</dd></dl>
 *
 */
public class InvertingRelationMapper<S2, S1> extends InvertingPairMapper<S2, S1, ONDEXRelation>
  implements RelationMapper<S2, S1>
{
	public InvertingRelationMapper ()
	{
		super ( null );
	}

	public InvertingRelationMapper ( RelationMapper<S1, S2> baseMapper )
	{
		super ( baseMapper );
	}
}
