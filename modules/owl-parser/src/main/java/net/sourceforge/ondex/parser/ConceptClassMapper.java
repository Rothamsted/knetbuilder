package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * A marker interface to declare a mapper that returns a {@link ConceptClass} from a source of type S.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public interface ConceptClassMapper<S> extends ONDEXMapper<ConceptClass, S, ONDEXGraph>
{
}
