package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * A marker interface to declare a mapper that returns a {@link ONDEXConcept} from a source of type S.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public interface ConceptMapper<S> extends ONDEXMapper<ONDEXConcept, S, ONDEXGraph>
{
}
