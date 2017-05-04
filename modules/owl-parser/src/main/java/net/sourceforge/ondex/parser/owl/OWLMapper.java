package net.sourceforge.ondex.parser.owl;

import java.util.Set;

import org.apache.jena.ontology.OntModel;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.parser.GraphMapper;
import net.sourceforge.ondex.parser.RelationsMapper;

/**
 * <p>This is the top level. Usually a class of this type is configured via 
 * <a href = "https://docs.spring.io/spring/docs/current/spring-framework-reference/html/beans.html">Spring Beans</a>, 
 * equipping it with mappers that are specific to the ontology type that is being parsed and mapped to ONDEX.</p>
 *
 * <p>See examples in tests and default configurations.</p>
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public class OWLMapper implements GraphMapper<OntModel>
{
	private Set<RelationsMapper<OntModel, ONDEXGraph>> relationsMappers;

	@Override
	public ONDEXGraph map ( OntModel model, ONDEXGraph graph )
	{
		if ( graph == null ) graph = new MemoryONDEXGraph ( "default" );
		
		for ( RelationsMapper<OntModel, ONDEXGraph> relMap: this.getRelationsMappers () )
			relMap
			.map ( model, graph )
			.count (); // we must consume it, there are stream procedures that need to be triggered

		return graph;
	}

	/**
	 * The parser starts up from relation mappers, each configured with a {@link OWLConceptClassMapper}, which tells
	 * the relation mapper the root class to start from.
	 */
	public Set<RelationsMapper<OntModel, ONDEXGraph>> getRelationsMappers ()
	{
		return relationsMappers;
	}

	public void setRelationsMappers ( Set<RelationsMapper<OntModel, ONDEXGraph>> relationsMappers )
	{
		this.relationsMappers = relationsMappers;
	}
}
