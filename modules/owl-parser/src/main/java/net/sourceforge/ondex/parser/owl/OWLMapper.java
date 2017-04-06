package net.sourceforge.ondex.parser.owl;

import java.util.Set;

import org.apache.jena.ontology.OntModel;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.parser.ConceptMapper;
import net.sourceforge.ondex.parser.GraphMapper;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public class OWLMapper implements GraphMapper<OntModel>
{
	private Set<ConceptMapper<OntModel>> conceptMappers;

	@Override
	public ONDEXGraph map ( OntModel model, ONDEXGraph graph )
	{
		if ( graph == null ) graph = new MemoryONDEXGraph ( "default" );

		for ( ConceptMapper<OntModel> cm: this.conceptMappers ) cm.map ( model, graph );
		return graph;
	}

	public Set<ConceptMapper<OntModel>> getConceptMappers ()
	{
		return conceptMappers;
	}

	public void setConceptMappers ( Set<ConceptMapper<OntModel>> conceptMappers )
	{
		this.conceptMappers = conceptMappers;
	}

}
