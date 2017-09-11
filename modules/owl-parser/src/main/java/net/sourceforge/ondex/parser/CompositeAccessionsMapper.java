package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * This can be used in Spring configurations. You can obtain accessions from multiple {@link #getMappers() accession mappers}, 
 * each working on a particular mapping criterion.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Aug 2017</dd></dl>
 *
 */
public class CompositeAccessionsMapper<S>
  extends CompositeStreamPairMapper<S, ONDEXConcept, ConceptAccession>
	implements AccessionsMapper<S>
{
}
