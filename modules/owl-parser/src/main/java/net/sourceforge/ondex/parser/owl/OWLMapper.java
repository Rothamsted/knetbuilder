package net.sourceforge.ondex.parser.owl;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	

	@Override
	public ONDEXGraph map ( OntModel model, ONDEXGraph graph )
	{
		ScheduledExecutorService timerService = Executors.newScheduledThreadPool ( 1 );
		
		try
		{
			// The OWL files we parse are not perfect, so let's try this 
			model.setStrictMode ( false );
			
			if ( graph == null ) graph = new MemoryONDEXGraph ( "default" );
			final ONDEXGraph graph1 = graph; // just because the timer below needs a final
			
			// Before delving into the mapping, let's setup a reporter, to give a sense that we're going ahead
			timerService.scheduleAtFixedRate (
				() -> log.info ( "Mapped {} GO classes", graph1.getConcepts ().size () ), 
				30, 30, TimeUnit.SECONDS 
			);
			
			for ( RelationsMapper<OntModel, ONDEXGraph> relMap: this.getRelationsMappers () )
				relMap
				.map ( model, graph )
				.count (); // we must consume it, there are stream procedures that need to be triggered
	
			log.info ( "Everything from the OWL file mapped. Total classes: {}", graph.getConcepts ().size () );
			return graph;
		}
		finally {
			timerService.shutdownNow ();			
		}
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
