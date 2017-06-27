package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.utils.ONDEXElemWrapper;

/**
 * A marker interface to declare a mapper that returns a {@link ONDEXConcept} from a source of type S.
 * 
 * It receives a {@link ConceptClass} as context (third generic), since it usually needs the concept class
 * of which the new mapped concept is an instance. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public interface ConceptMapper<S> extends ONDEXMapper<ONDEXConcept, S, ONDEXElemWrapper<ConceptClass>>
{
}
