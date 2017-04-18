package net.sourceforge.ondex.parser.owl;

import java.util.Set;

import org.apache.jena.ontology.OntModel;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.parser.GraphMapper;
import net.sourceforge.ondex.parser.RelationsMapper;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public class OWLMapper implements GraphMapper<OntModel>
{
	private Set<RelationsMapper<OntModel>> relationsMappers;

	@Override
	public ONDEXGraph map ( OntModel model, ONDEXGraph graph )
	{
		if ( graph == null ) graph = new MemoryONDEXGraph ( "default" );
		
		for ( RelationsMapper<OntModel> relMap: this.getRelationsMappers () )
			// we must consume it, there are stream procedures that need to be invoked
			relMap.map ( model, graph ).count ();

		return graph;
	}

	public Set<RelationsMapper<OntModel>> getRelationsMappers ()
	{
		return relationsMappers;
	}

	public void setRelationsMappers ( Set<RelationsMapper<OntModel>> relationsMappers )
	{
		this.relationsMappers = relationsMappers;
	}
}
