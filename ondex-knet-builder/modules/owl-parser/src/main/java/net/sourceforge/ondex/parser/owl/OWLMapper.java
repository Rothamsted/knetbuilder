package net.sourceforge.ondex.parser.owl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.parser.ConceptClassMapper;
import net.sourceforge.ondex.parser.ExploringMapper;
import uk.ac.ebi.utils.exceptions.ExceptionUtils;
import uk.ac.ebi.utils.exceptions.UncheckedFileNotFoundException;
import uk.ac.ebi.utils.io.IOUtils;

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
	private static Logger slog = LoggerFactory.getLogger ( OWLMapper.class );
	
	private OWLVisitable visitableHelper = new OWLVisitable ();  
	
	public ONDEXGraph map2Graph ( OntModel model )
	{
		return map2Graph ( model, null );
	}

	/**
	 * It is preferred that you use this on top-level invocation, rather than {@link #map(OntModel, ONDEXGraph)}. This
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
			int gsz0 = graph.getConcepts ().size (); // Graph doesn't necessarily start empty.

			// Before delving into the mapping, let's setup a reporter, to give a sense that we're going ahead
			timerService.scheduleAtFixedRate (
				() -> log.info ( "Mapped {} OWL Classes", graph1.getConcepts ().size () - gsz0 ), 
				30, 30, TimeUnit.SECONDS 
			);

			this.map ( model, graph ).count ();
	
			log.info ( 
				"Everything from the OWL file mapped. Total classes: {}", 
				graph.getConcepts ().size () - gsz0 
			);
			return graph;
		}
		finally {
			timerService.shutdownNow ();			
		}
	}
	
	

	@Override
	public Stream<ONDEXConcept> map ( OntModel source, ONDEXGraph graph )
	{
		ConceptClassMapper<OntClass> ccmap = this.getConceptClassMapper ();
		if ( ccmap instanceof OWLTopConceptClassMapper )
		{
			// If you have this mapper, you might need to populate it with the root OWL classes.
			//
			OWLTopConceptClassMapper owlTopMapper = (OWLTopConceptClassMapper) ccmap;
			if ( owlTopMapper.getTopClasses () == null )
				owlTopMapper.setTopClasses (
					this.getRootsScanner ().scan ( source )
					.collect ( Collectors.toSet () )
			);
		}
		return super.map ( source, graph );
	}

	
	/**
	 * Creates a Spring container from the XML configuration file and then invokes 
	 * {@link #mapFrom(ONDEXGraph, ApplicationContext, String...)}.
	 * 
	 * springXmlPath can be an URI starting with {@code file:///}.
	 *  
	 */
	public static ONDEXGraph mapFrom ( ONDEXGraph graph, String springXmlPath, String... owlInputPaths )
	{
		try
		{
			// Convert to absolute URI, it's safer with Spring
			if ( springXmlPath != null )
			{
				springXmlPath = springXmlPath.startsWith ( "file:" )
					? Paths.get ( IOUtils.uri ( springXmlPath ) ).toAbsolutePath ().toString ()
					: new File ( springXmlPath ).getCanonicalPath ();
					
				springXmlPath = "file:///" + springXmlPath;
			}
		}
		catch ( IOException ex ) 
		{
			ExceptionUtils.throwEx (
				UncheckedIOException.class, 
				"Error while loading config file '%s': %s",
				springXmlPath,
				ex.getMessage ()
			);
		}
		
		try ( FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext ( springXmlPath ) ) {
			return mapFrom ( graph, ctx, owlInputPaths );
		}
	}
	
	
	/**
	 * Maps OWL inputs by using the {@link OWLMapper} configured in a spring container.
	 * Moreover, it gets the Jena {@link Model} to be used for OWL loading from the Spring configuration too.
	 * 
	 */
	public static ONDEXGraph mapFrom ( ONDEXGraph graph, ApplicationContext ctx, String... owlInputPaths )
	{
		OntModel model = (OntModel) ctx.getBean ( "jenaOntModel" );
		long msz0 = model.size (); // Just in case it doesn't start empty
		
		// Let's keep track of the loading, useful with large files.
		ScheduledExecutorService timerService = Executors.newScheduledThreadPool ( 1 );
		timerService.scheduleAtFixedRate (
			() -> slog.info ( "{} RDF triples loaded", model.size () - msz0 ), 
			5, 5, TimeUnit.MINUTES 
		);
		
		try 
		{
			for ( String owlPath: owlInputPaths )
			{
				slog.info ( "Loading file '{}'", owlPath );
				try 
				{
					model.read ( 
						new BufferedReader ( new FileReader ( owlPath ) ), 
						"RDF/XML" 
					);
				}
				catch ( FileNotFoundException ex ) 
				{
					throw new UncheckedFileNotFoundException (  
						String.format ( "OWL file '%s' not found, details: %s", owlPath, ex.getMessage () ), 
						ex 
					);
				}
			}
		}
		finally {
			timerService.shutdownNow ();
		}
		
		OWLMapper owlMap = (OWLMapper) ctx.getBean ( "owlMapper" );
		return owlMap.map2Graph ( model, graph );
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
