package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * The wrapper to mark a mapper to a {@link ONDEXConcept}. This is a {@link PairMapper} since ONDEX requires a 
 * {@link ConceptClass} to create a new {@link ONDEXConcept}.
 * 
 * Also note that this mapper extends {@link Visitable} with the meaning that we might ask if a data item has been 
 * already mapped to a concept.
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 */
public interface ConceptMapper<S> extends PairMapper<S, ConceptClass, ONDEXConcept>, Visitable<S>
{
	/**
	 * A helper wrapper, which gets the CC to be passed to {@link #map(Object, Object, ONDEXGraph)} from the 
	 * {@link ConceptClassMapper#map(Object, ONDEXGraph) concept class mapper} passed as parameter here.
	 */
	public default ONDEXConcept map ( S source, ConceptClassMapper<S> ccmapper, ONDEXGraph graph ) 
	{
		return this.map ( source, ccmapper.map ( source, graph ), graph );
	}
}