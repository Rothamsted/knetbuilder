package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * A mapper that produces multiple {@link ConceptAccession}, possibly combining {@link Scanner scanners} and
 * {@link AccessionMapper single-accession mappers}. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 */
public interface AccessionsMapper<S> extends StreamPairMapper<S, ONDEXConcept, ConceptAccession>
{
}
