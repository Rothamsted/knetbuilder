package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * A wrapper to map a data source item to an accession. This is a {@link PairMapper} because ONDEX needs 
 * an {@link ONDEXConcept} to create a new accession, hence, this must be passed to the mapper, together with 
 * the data that is used to create it.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 */
public interface AccessionMapper<S> extends PairMapper<S, ONDEXConcept, ConceptAccession>
{
}
