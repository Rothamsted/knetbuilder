package net.sourceforge.ondex.parser.owl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.parser2.ExploringMapper;

/**
 * <p>This is the top level mapper/parser. Usually a class of this type is configured via 
 * <a href = "https://docs.spring.io/spring/docs/current/spring-framework-reference/html/beans.html">Spring Beans</a>, 
 * equipping it with mappers that are specific to the ontology type that is being parsed and mapped to ONDEX.</p>
 *
 * <p>See examples in tests and default configurations.</p>
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public class OWLMapper extends ExploringMapper<OntModel, OntClass>
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	public ONDEXGraph map2Graph ( OntModel model, ONDEXGraph graph ) 
	{
		if ( graph == null ) graph = new MemoryONDEXGraph ( "default" );
		ScheduledExecutorService timerService = Executors.newScheduledThreadPool ( 1 );
		
		try
		{
			final ONDEXGraph graph1 = graph; // just because the timer below needs a final

			// Before delving into the mapping, let's setup a reporter, to give a sense that we're going ahead
			timerService.scheduleAtFixedRate (
				() -> log.info ( "Mapped {} OWL Classes", graph1.getConcepts ().size () ), 
				30, 30, TimeUnit.SECONDS 
			);

			this.map ( model, graph ).count ();
	
			log.info ( "Everything from the OWL file mapped. Total classes: {}", graph.getConcepts ().size () );
			return graph;
		}
		finally {
			timerService.shutdownNow ();			
		}
	}
}
