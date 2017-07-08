package net.sourceforge.ondex.parser2;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.utils.ONDEXElemWrapper;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 May 2017</dd></dl>
 *
 */
public abstract class ConceptMapper<S> implements PairMapper<S, ONDEXElemWrapper<ConceptClass>, ONDEXConcept> 
{
	private TextMapper<S> idMapper;
	private TextMapper<S> descriptionMapper;
	private TextMapper<S> preferredNameMapper;
	private TextsMapper<S> altNamesMapper;
	private EvidenceTypeMapper<S> evidenceMapper;
	private DataSourceMapper<S> dataSourceMapper;
	
	public ONDEXConcept map ( S source, ConceptClassMapper<S> ccmapper, ONDEXGraph graph )
	{
		ONDEXElemWrapper<S> wsrc = ONDEXElemWrapper.of ( source, graph ); 
		ConceptClass cc = ccmapper.map ( wsrc );
		ONDEXElemWrapper<ConceptClass> ccw = ONDEXElemWrapper.of ( cc, graph );
		return this.map ( source, ccw );
	}

	public ONDEXConcept map ( ONDEXElemWrapper<S> source, ConceptClassMapper<S> ccmapper )
	{
		ONDEXGraph graph = source.getGraph ();
		ConceptClass cc = ccmapper.map ( source );
		return this.map ( source.getElement (), ONDEXElemWrapper.of ( cc, graph ) );
	}

}
