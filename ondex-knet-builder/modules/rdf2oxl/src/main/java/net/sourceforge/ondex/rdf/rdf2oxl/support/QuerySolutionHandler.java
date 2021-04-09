package net.sourceforge.ondex.rdf.rdf2oxl.support;

import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import info.marcobrandizi.rdfutils.jena.SparqlEndPointHelper;
import net.sourceforge.ondex.rdf.rdf2oxl.Rdf2OxlConverter;
import net.sourceforge.ondex.rdf.rdf2oxl.support.freemarker.FreeMarkerHelper;

/**
 * # The query solution handler
 * 
 * Each instance of this processes sets of URIs about a resource type, to extract details via `SPARQL CONSTRUCT`, build
 * `JSON-LD` data and pass them to the {@link FreeMarkerHelper XML/OXL template engine}.  
 * 
 * See the [package description](package-summary.html) for an overview of the rdf2oxl architecture.  
 * 
 * More in detail, {@link #accept(List)} uses {@link #getConstructTemplate()} from its operations.
 * {@link #getConstructTemplate()} is assumed to be a SPARQL `CONSTRUCT` template, parameterised over a set 
 * of instance resource URIs, coming from {@link QueryProcessor} (eg, set of concept URIs). The query should extract
 * proper details for the resources (eg, concept names, description, etc). The URIs are passed to the query by 
 * considering a `$resourceIris` placeholder inside a `VALUES`:
 *  
 * ```sql
 * VALUES ( ?conceptIri ) {
 *   $resourceIris
 * }
 * ```
 * 
 * the graph returned by this query is converted into JSON-LD, the corresponding JSON is extracted from it and passed to
 * the {@link #getOxlTemplateName() OXL template}, which render resource details into XML/OXL.  
 * 
 * See examples in main/resources/oxl_templates.    
 * 
 * As in other cases, configuration details for the handlers are set via Spring and {@link Rdf2OxlConverter}.  
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Jul 2018</dd></dl>
 *
 */
@Component ( "resourceHandler" )
public class QuerySolutionHandler implements Consumer<List<QuerySolution>>
{
	/**
	 * An interface to represent a function passed to {@link QuerySolutionHandler#setDataPreProcessor(DataPreProcessor)}.
	 */
	public static interface DataPreProcessor extends BiConsumer<Model, Map<String, Object>>
	{
		/** We override this in order to avoid cast exception issues. */
		@Override
		default DataPreProcessor andThen ( BiConsumer<? super Model, ? super Map<String, Object>> after )
		{
			BiConsumer<Model, Map<String, Object>> composed = BiConsumer.super.andThen ( after );
			return (model, data) -> composed.accept ( model, data );
		}	
	}
	
	private Writer outWriter;
	
	private String oxlTemplateName;
	private String constructTemplate;
	
	private SparqlEndPointHelper sparqlHelper;
	private FreeMarkerHelper templateHelper;
	
	private DataPreProcessor dataPreProcessor;
		
	// TODO: remove?
	private String logPrefix = "[RDF Hanlder]";
	
	protected Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	/** 
	 * See above for details.  
	 * 
	 * The described behaviour is achieved by using {@link #getSparqlHelper()}, which method 
	 * {@link SparqlEndPointHelper#processConstruct(String, String, Consumer)} is invoked with a code that makes
	 * use of {@link #getConstructTemplate()} to obtain data from sols, and then adds possible additional data coming from
	 * {@link #getTemplateHelper()} and/or {@link #getDataPreProcessor()}.
	 *    
	 */
	@Override
	public void accept ( List<QuerySolution> sols )
	{
		if ( sols.size () == 0 ) return;
				
		// Get a VALUES-compliant representation of all these URIs
		String valuesStr = sols.parallelStream ()
		.map ( sol -> sol.getResource ( "resourceIri" ).getURI () )
		.map ( iri -> "( <" + iri + "> )" )
		.collect ( Collectors.joining ( "\n" ) );
		
		if ( log.isTraceEnabled () ) log.trace ( "Rendering IRIs: \n{}", valuesStr );
		
		// And use it in the SPARQL template
		String sparqlConstruct = constructTemplate.replace ( "$resourceIris", valuesStr );
		
		// Then, run it and process the OXL-generating template
		synchronized ( this.outWriter ) 
		{
			sparqlHelper.processConstruct (
				sparqlConstruct,
				model ->
				{	
					// some data massage to the JSON-LD that is extracted from the RDF CONSTRUCT results (ie, model)
					Map<String, Object> data = templateHelper.getTemplateData ( model );

					// Do I have customised data too?
					if ( dataPreProcessor != null ) dataPreProcessor.accept ( model, data );
					// And eventually here we go
					templateHelper.processTemplate ( oxlTemplateName, this.outWriter, data );
				}
			);
		}
	}


	/**
	 * All the OXL output is written here.
	 */
	public Writer getOutWriter ()
	{
		return outWriter;
	}


	public void setOutWriter ( Writer outWriter )
	{
		this.outWriter = outWriter;
	}

	/** 
	 * 
	 * The name of the OXL/XML template to generate OXL for the entity type the handler is configured with.
	 * 
	 * Currently we use FreeMarker as template engine, so this should point to some *.ftlx.    
	 * 
	 * This is usually found in the class path setup by {@link Rdf2OxlConfiguration#getTemplateClassPath()} (usually via
	 * Spring).  
	 * 
	 */
	public String getOxlTemplateName ()
	{
		return oxlTemplateName;
	}


	public void setOxlTemplateName ( String oxlTemplateName )
	{
		this.oxlTemplateName = oxlTemplateName;
	}

	/**
	 * The name of a `*.sparql` file, which contain a `CONSTRUCT` query to fetch resource details about a set of URIs of a 
	 * given type (eg, concept, relation).
	 * 
	 * See above for details.  
	 * 
	 */
	public String getConstructTemplate ()
	{
		return constructTemplate;
	}


	public void setConstructTemplate ( String constructTemplate )
	{
		this.constructTemplate = constructTemplate;
	}

	/**
	 * The {@link SparqlEndPointHelper} used to interact with the RDF data source for the conversion. This is usually
	 * configured via Spring.
	 * 
	 */
	public SparqlEndPointHelper getSparqlHelper ()
	{
		return sparqlHelper;
	}

	@Autowired
	public void setSparqlHelper ( SparqlEndPointHelper sparqlHelper )
	{
		this.sparqlHelper = sparqlHelper;
	}

	/**
	 * The {@link FreeMarkerHelper OXL/XML template engine} used to produce the final output. This is usually configured 
	 * via Spring.
	 */
	public FreeMarkerHelper getTemplateHelper ()
	{
		return templateHelper;
	}

	@Autowired	
	public void setTemplateHelper ( FreeMarkerHelper templateHelper )
	{
		this.templateHelper = templateHelper;
	}

	/**
	 * This property can be set with a function that enriches the {@link Model} and hashed data passed to the template
	 * engine with handler-custom values. 
	 */
	protected DataPreProcessor getDataPreProcessor ()
	{
		return dataPreProcessor;
	}

	protected void setDataPreProcessor ( DataPreProcessor dataPreProcessor )
	{
		this.dataPreProcessor = dataPreProcessor;
	}

	/**
	 * This is used to in log messages, to allow for associating the messages to the respective component, entity type
	 * it processed, and possibly other details (e.g., the `LIMIT/OFFSET` window or the thread name).
	 * 
	 */
	public String getLogPrefix ()
	{
		return logPrefix;
	}


	@Autowired ( required = false ) @Qualifier ( "logPrefix" )		
	public void setLogPrefix ( String logPrefix )
	{
		this.logPrefix = logPrefix;
	}
	
}
