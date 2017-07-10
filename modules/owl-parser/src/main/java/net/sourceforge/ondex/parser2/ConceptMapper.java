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
public abstract class ConceptMapper<S> implements PairMapper<S, ConceptClass, ONDEXConcept> 
{
	private TextMapper<S> idMapper;
	private TextMapper<S> descriptionMapper;
	private TextMapper<S> preferredNameMapper;
	private TextsMapper<S> altNamesMapper;
	private EvidenceTypeMapper<S> evidenceMapper;
	private DataSourceMapper<S> dataSourceMapper;
	
	public ONDEXConcept map ( S source, ConceptClassMapper<S> ccmapper, ONDEXGraph graph ) {
		return this.map ( source, ccmapper.map ( source, graph ), graph );
	}

}
