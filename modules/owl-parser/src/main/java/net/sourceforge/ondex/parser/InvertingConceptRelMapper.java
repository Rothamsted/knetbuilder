package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * This is like {@link InvertingRelationMapper}, but it starts from ONDEX concepts as inputs, instead of data items.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 Aug 2017</dd></dl>
 *
 */
public class InvertingConceptRelMapper extends InvertingRelationMapper<ONDEXConcept, ONDEXConcept>
  implements ConceptBasedRelMapper
{

	public InvertingConceptRelMapper ()
	{
		this ( null );
	}

	public InvertingConceptRelMapper ( ConceptBasedRelMapper baseMapper )
	{
		super ( baseMapper );
	}
	
}
