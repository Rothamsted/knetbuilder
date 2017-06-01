package net.sourceforge.ondex.parser;

import java.util.stream.Stream;

import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.utils.ONDEXElemWrapper;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>28 Apr 2017</dd></dl>
 *
 */
public interface NamesMapper<S> extends ONDEXMapper<Stream<ConceptName>, S, ONDEXElemWrapper<ONDEXConcept>>
{

}
