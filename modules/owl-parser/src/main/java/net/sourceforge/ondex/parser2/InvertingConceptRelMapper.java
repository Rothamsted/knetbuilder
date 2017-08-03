package net.sourceforge.ondex.parser2;

import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * TODO: comment me!
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
