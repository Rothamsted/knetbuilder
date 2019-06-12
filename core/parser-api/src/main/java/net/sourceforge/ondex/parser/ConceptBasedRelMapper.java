package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * This is for those mappers that builds a new relation starting from two {@link ONDEXConcept}s (typically two 
 * concepts already mapped by another component. You might want to use either this mapper or its 
 * {@link RelationMapper parent}, which deals directly with a data source (rather than Ondex concepts). 
 * The choice will depend on the specific mapper you're developing.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 May 2017</dd></dl>
 *
 */
public interface ConceptBasedRelMapper extends RelationMapper<ONDEXConcept, ONDEXConcept> 
{
}
