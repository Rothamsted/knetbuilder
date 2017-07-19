package net.sourceforge.ondex.parser2;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 * @param <S>
 */
public interface ConceptMapper<S> extends PairMapper<S, ConceptClass, ONDEXConcept>, Visitable<S>
{
	public default ONDEXConcept map ( S source, ConceptClassMapper<S> ccmapper, ONDEXGraph graph ) 
	{
		return this.map ( source, ccmapper.map ( source, graph ), graph );
	}

}