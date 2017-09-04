package net.sourceforge.ondex.parser.owl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.parser.ExploringMapper;

/**
 * <p>This is the top level mapper/parser. Usually a class of this type is configured via 
 * <a href = "https://docs.spring.io/spring/docs/current/spring-framework-reference/html/beans.html">Spring Beans</a>, 
 * equipping it with mappers that are specific to the ontology type that is being parsed and mapped to ONDEX.</p>
 * 
 * This maps a {@link OntModel Jena Ontology Model} into an ONDEXGraph containing the corresponding ontology.
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
	
	private OWLVisitable visitableHelper = new OWLVisitable ();  
	
	public ONDEXGraph map2Graph ( OntModel model )
	{
		return map2Graph ( model, null );
	}

	/**
	 * It is preferred that you use this on top-levl invocations, rather than {@link #map(OntModel, ONDEXGraph)}. This
	 * does further operations (e.g., logging the mapping progress) and also returns the {@link ONDEXGraph} that is 
	 * filled with mappings from the input model. 
	 * 
	 */
	public ONDEXGraph map2Graph ( OntModel model, ONDEXGraph graph ) 
	{
		if ( graph == null ) graph = new MemoryONDEXGraph ( "default" );
		ScheduledExecutorService timerService = Executors.newScheduledThreadPool ( 1 );
		
		try
		{
			log.info ( "Parsing OWL dataset with {} triple(s)", model.size () ); 
			
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
	

	/** 
	 * Wraps the parent's corresponding method by checking if the input was {@link #isVisited(OntClass)} and 
	 * by {@link #setVisited(OntClass, boolean) marking it as visited} before invoking
	 * 
	 * {@link ExploringMapper#scanTree(OntClass, OntClass, ONDEXGraph) the parent tree scanning}. 
	 * This avoids to be trapped in loops produced by relations like rdfs:subClassOf.
	 * 
	 */
	@Override
	protected ONDEXConcept scanTree ( OntClass rootItem, OntClass topItem, ONDEXGraph graph )
	{
		if ( this.isVisited ( rootItem ) ) return null;
		this.setVisited ( rootItem );

		return super.scanTree ( rootItem, topItem, graph );
	}

	public boolean isVisited ( OntClass ontCls )
	{
		return visitableHelper.isVisited ( ontCls );
	}

	public boolean setVisited ( OntClass ontCls, boolean isVisited )
	{
		return visitableHelper.setVisited ( ontCls, isVisited );
	}

	public boolean setVisited ( OntClass value )
	{
		return visitableHelper.setVisited ( value );
	}
	
}
