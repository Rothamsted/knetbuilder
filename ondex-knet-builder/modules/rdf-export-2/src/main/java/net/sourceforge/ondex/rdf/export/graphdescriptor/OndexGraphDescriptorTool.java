package net.sourceforge.ondex.rdf.export.graphdescriptor;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;
import static net.sourceforge.ondex.core.util.ONDEXGraphUtils.getOrCreateConceptClass;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.graph.GraphReadOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.rdf.model.Literal;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.ONDEXGraphUtils;
import uk.ac.ebi.utils.exceptions.ExceptionUtils;
import uk.ac.ebi.utils.exceptions.TooFewValuesException;
import uk.ac.ebi.utils.ids.IdUtils;
import uk.ac.ebi.utils.io.IOUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Jun 2021</dd></dl>
 *
 */
public class OndexGraphDescriptorTool
{
	public static final String DESCRIPTOR_CONCEPT_CLASS_ID = "DatasetMetadata";
	public static final String DESCRIPTOR_NODE_ID = StringUtils.uncapitalize ( DESCRIPTOR_CONCEPT_CLASS_ID );
	
	private ONDEXGraph graph;
	private Model _descriptorCache = null;
	private Map<String, Object> _jsonDescriptorCache = null;
	private Map<String, List<Map<String, Object>>> _jsonIndexCache = null;
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () ); 
	
	
	public OndexGraphDescriptorTool ( ONDEXGraph graph )
	{
		super ();
		this.graph = graph;
	}

	
	/**
	 * Creates the descriptor without saving it in the graph.
	 *  
	 */
	public Model createDescriptor ( Map<String, Object> context, String rdfTemplate, String rdfLang )
	{
		var result = JsonLdUtils.getRdfFromTemplate ( context, rdfTemplate, rdfLang );
		
		addGraphStatsDescriptors ( result );
		return result;
	}
	
	/**
	 * TODO: get rdfLang from the extension
	 */
	public Model createDescriptor ( Map<String, Object> context, Path rdfTemplatePath, String rdfLang )
	{
		String tpl = null;
		try {
			tpl = IOUtils.readFile ( rdfTemplatePath.toString () );
		}
		catch ( IOException ex ) {
			throw ExceptionUtils.buildEx ( 
				UncheckedIOException.class, ex, 
				"Error while reading metadata template '%s': %s", rdfTemplatePath.toAbsolutePath (), ex.getMessage () 
			);
		}
		
		return createDescriptor ( context, tpl, rdfLang );
	}
	

	public Model saveDescriptor ( Map<String, Object> context, String rdfTemplate, String rdfLang )
	{
		var result = createDescriptor ( context, rdfTemplate, rdfLang );
		saveDescriptor ( result );
		return result;
	}
	
	/**
	 * TODO: get rdfLang from the extension
	 */
	public Model saveDescriptor ( Map<String, Object> context, Path rdfTemplatePath, String rdfLang )
	{
		try {
			var tpl = IOUtils.readFile ( rdfTemplatePath.toString () );
			return saveDescriptor ( context, tpl, rdfLang );			
		}
		catch ( IOException ex ) {
			throw ExceptionUtils.buildEx ( 
				UncheckedIOException.class, ex, 
				"Error while reading metadata template '%s': %s", rdfTemplatePath.toAbsolutePath (), ex.getMessage () 
			);
		}
	}

	
	public ONDEXConcept saveDescriptor ( Model descriptor )
	{
		// Save the RDF, review the Optional chain.
		var rdfOut = new StringWriter ();
		descriptor.write ( rdfOut, "TURTLE" );

		this._descriptorCache = descriptor;
		
		return getDescriptorConcept ( rdfOut.toString () );
	}
	
	
	
	/**
	 * Fetches the descriptor from the graph.
	 * 
	 * @return a Jena Model that is read-only.
	 *  
	 */
	public Model getDescriptor ()
	{		
		if ( this._descriptorCache != null ) return _descriptorCache;
		
		var result = ModelFactory.createDefaultModel ();
		var descriptorConcept = getDescriptorConcept ();
		
		if ( descriptorConcept != null )
		{
			var sr = new StringReader ( descriptorConcept.getDescription () );
			result.read ( sr, null, "TURTLE" );
			
			// Turn it into read-only (as per Jena's ML suggestion)
			Graph graphRO = new GraphReadOnly ( result.getGraph() );
      result = ModelFactory.createModelForGraph ( graphRO ); 
      
			_descriptorCache = result;
			this._jsonDescriptorCache = null;
			this._jsonIndexCache = null;
		}
		return result;
	}
	

	public ONDEXConcept getDescriptorConcept ()
	{
		return getDescriptorConcept ( null );
	}
	
	
	private ONDEXConcept getDescriptorConcept ( String newDescriptorRdf )
	{
		var descrCC = getOrCreateConceptClass ( 
			graph, 
			DESCRIPTOR_CONCEPT_CLASS_ID,
			"Dataset Metadata",
			"Contains an RDF/schema.org description of dataset as a whole"
		);

		var descriptors = graph.getConceptsOfConceptClass ( descrCC )
			.stream ()
			.filter ( c ->  DESCRIPTOR_NODE_ID.equals ( c.getPID () ) )
			.collect ( Collectors.toSet () );
		
		if ( descriptors.size () > 1 ) log.warn ( 
			"The graph contains {} {} nodes, {}",
			descriptors.size (), 
			DESCRIPTOR_NODE_ID,
			newDescriptorRdf == null ? "deleting all" : "returning a random one"
		);		

		if ( newDescriptorRdf == null ) 
			return descriptors.isEmpty () ? null : descriptors.iterator ().next ();
		
		descriptors.stream ()
		.map ( ONDEXConcept::getId )
		.forEach ( graph::deleteConcept );

		var descrDataSrc = ONDEXGraphUtils.getOrCreateDataSource (
			graph, 
			DESCRIPTOR_NODE_ID + "DataSource",
			"Created by " + this.getClass ().getSimpleName (),
			""
		);
		var descrEvidence = ONDEXGraphUtils.getOrCreateEvidenceType (
			graph, 
			DESCRIPTOR_NODE_ID + "Evidence",
			"Created by " + this.getClass ().getSimpleName (),
			""
		);
		
		return graph.createConcept ( 
			DESCRIPTOR_NODE_ID,
			RDFLanguages.TURTLE.getContentType ().toHeaderString () + "; charset=UTF-8", // annotation
			newDescriptorRdf,  // description
			descrDataSrc, 
			descrCC,
			Set.of ( descrEvidence ) 
		);	
	}
	
	/**
	 * Fetches the descriptor from the graph and returns it as JSON simplified format. 
	 * 
	 * This is made efficient by internal caching, so it returns a read-only result. 
	 *  
	 */
	public Map<String, Object> getDescriptorAsJsonLd ()
	{
		if ( this._jsonDescriptorCache != null ) return _jsonDescriptorCache;		
		Model descriptor = getDescriptor ();
		if ( descriptor.isEmpty () ) return Map.of (); 				
		return _jsonDescriptorCache = Collections.unmodifiableMap ( JsonLdUtils.rdf2JsonLd ( descriptor, true ) );
	}

	/**
	 * Returns a by-type index of the descriptor.
	 * 
	 * This is made efficient by internal caching, so it returns a read-only result. 
	 */
	public Map<String, List<Map<String, Object>>> getDescriptorTypes ()
	{
		if ( this._jsonIndexCache != null ) return _jsonIndexCache;
		return _jsonIndexCache = Collections.unmodifiableMap ( JsonLdUtils.indexByType ( getDescriptorAsJsonLd() ) );
	}
	
	/**
	 * Returns the schema:Dataset instance from the saved descriptor
	 */
	public Map<String, Object> getDescriptorDataset ()
	{
		return getDescriptorType ( "schema:Dataset" );
	}
	
	public Map<String, Map<String, Object>> getDatasetAdditionalProperties ()
	{
		Map<String, Object> js = this.getDescriptorAsJsonLd ();

		Map<String, Object> dset = getDescriptorDataset ();
		
		@SuppressWarnings ( "unchecked" )
		Map<String, Map<String, Object>> result = JsonLdUtils.asList ( dset, "additionalProperty" )
		.stream ()
		.map ( pvuri -> (Map<String, Object>) js.get ( pvuri ) )
		.collect ( Collectors.toUnmodifiableMap ( 
			jobj -> (String) jobj.get ( "propertyID" ),
			Function.identity ()
		));
		
		return result;
	}
	
	public static <T> T getPropertyValue ( Map<String, Map<String, Object>> pvals, String propertyId )
	{
		return JsonLdUtils.asValue ( pvals.get ( propertyId ), "value" );
	}
	
	public static Integer getPropertyValueAsInt ( Map<String, Map<String, Object>> pvals, String propertyId )
	{
		return JsonLdUtils.asInt ( pvals.get ( propertyId ), "value" );
	}
	
	public static <T> List<T> getPropertyValueAsList ( Map<String, Map<String, Object>> pvals, String propertyId )
	{
		return JsonLdUtils.asList ( pvals.get ( propertyId ), "value" );
	}
	
	/**
	 * Returns the schema:Organization instance from the saved descriptor
	 */
	public Map<String, Object> getDescriptorOrganization ()
	{
		return getDescriptorType ( "schema:Organization" );
	}

	@SuppressWarnings ( "unchecked" )
	public Map<String, Object> getDescriptorType ( String type )
	{
		Map<String, List<Map<String, Object>>> index = getDescriptorTypes ();
		
		return Optional.ofNullable ( 
			(Map<String, Object>) JsonLdUtils.asValue ( (Map<String, Object> ) (Map<?, ?>) index, type, true )
		).orElse ( Map.of() );	
	}

	
	private Model addGraphStatsDescriptors ( Model descriptor )
	{
		String dsetUri = JENAUTILS.getSubject ( descriptor, iri ( "rdf:type" ), iri ( "schema:Dataset" ), true )
		.map ( Resource::getURI )
		.orElseThrow ( () -> new TooFewValuesException ( 
			"Can't find a schema:Dataset instance in the Knetminer dataset descriptor" 
		));

		assertPropVal ( 
			descriptor, dsetUri, 
			"KnetMiner:Dataset:Concepts Number",
			descriptor.createTypedLiteral ( this.graph.getConcepts ().size () ) 
		);

		assertPropVal ( 
			descriptor, dsetUri, 
			"KnetMiner:Dataset:Relations Number",
			descriptor.createTypedLiteral ( this.graph.getRelations ().size () ) 
		);
		
		return descriptor;
	}
	
	private String getSchemaPropVal ( Model m, String propertyId, Literal value, String unitText )
	{
		String propUri = iri ( 
			"bkr", 
			"property_" + IdUtils.hashUriSignature ( propertyId + value.toString () + (unitText == null ? "" : unitText) )
		); 
		JENAUTILS.assertResource ( m, propUri, iri ( "rdf:type" ), "schema:PropertyValue" );
		JENAUTILS.assertLiteral ( m, propUri, iri ( "schema:propertyID" ), propertyId );
		JENAUTILS.assertLiteral ( m, propUri, iri ( "schema:value" ), value );
		if ( unitText != null )
			JENAUTILS.assertLiteral ( m, propUri, iri ( "schema:unitText" ), unitText );
		return propUri;
	}

	private String getSchemaPropVal ( Model m, String propertyId, Literal value )
	{
		return getSchemaPropVal ( m, propertyId, value, null );
	}


	private String assertPropVal ( Model m, String subjectUri, String linkPropUri, String propertyId, Literal value, String unitText )
	{
		String pvuri = getSchemaPropVal ( m, propertyId, value, unitText );
		JENAUTILS.assertResource ( m, subjectUri, iri ( linkPropUri ), pvuri );
		return pvuri;
	}

	private String assertPropVal ( Model m, String subjectUri, String propertyId, Literal value, String unitText )
	{
		return assertPropVal ( m, subjectUri, "schema:additionalProperty", propertyId, value, unitText );
	}

	private String assertPropVal ( Model m, String subjectUri, String propertyId, Literal value )
	{
		return assertPropVal ( m, subjectUri, propertyId, value, null );
	}
	
}
