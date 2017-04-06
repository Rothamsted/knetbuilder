package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public abstract class RelationBuilder
{
	public abstract RelationType getRelationType ();
	public abstract ONDEXRelation build ( ONDEXConcept from, ONDEXConcept to );
}
