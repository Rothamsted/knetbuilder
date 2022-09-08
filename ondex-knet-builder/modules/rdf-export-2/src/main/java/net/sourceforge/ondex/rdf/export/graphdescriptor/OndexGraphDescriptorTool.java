package net.sourceforge.ondex.rdf.export.graphdescriptor;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;
import static net.sourceforge.ondex.core.util.ONDEXGraphUtils.getOrCreateConceptClass;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.graph.GraphReadOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.ONDEXGraphUtils;
import uk.ac.ebi.utils.collections.OptionsMap;
import uk.ac.ebi.utils.exceptions.ExceptionUtils;
import uk.ac.ebi.utils.exceptions.TooFewValuesException;
import uk.ac.ebi.utils.ids.IdUtils;
import uk.ac.ebi.utils.opt.io.IOUtils;

/**
 * The OXL metadata descriptor.
 * 
 * <p>This is a small utility to generate metadata about an OXL as a whole dataset, based on schema.org
 * (eg, schema:Dataset).</p>
 * 
 * <p>It works by instantiating a template like resources/knetminer-descriptors/knetminer-metadata-template.ttl
 * with values from a .properties file plus other values and RDF statements that are extracted from the OXL.</p>
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Jun 2021</dd></dl>
 *
 */
public class OndexGraphDescriptorTool extends OndexGraphDescriptorToolFields
{		
	public static class Builder extends OndexGraphDescriptorToolFields
	{
		private String contextPath;
		private String rdfTemplatePath;
		
		private Logger log = LoggerFactory.getLogger ( this.getClass () ); 
		
		/**
		 * Just invoke this after an initial setup via {@link Builder}.
		 */
		public OndexGraphDescriptorTool build ()
		{
			if ( contextPath != null && context != null ) throw new IllegalArgumentException (
				this.getClass ().getSimpleName () + " contextPath and context are alternative options, can't specify both"
			);
			
			if ( rdfTemplatePath != null && rdfTemplate != null ) throw new IllegalArgumentException (
				this.getClass ().getSimpleName () + " rdfTemplatePath and rdfTemplate are alternative options, can't specify both"
			);

			try
			{
				if ( contextPath != null ) {
					// TODO: Apache multi-format (https://stackoverflow.com/questions/70123979)
					log.info ( "Loading graph descriptor properties from '{}'", contextPath );
					Properties props = new Properties ();
					props.load ( new FileReader ( contextPath, StandardCharsets.UTF_8 ) );
					context = OptionsMap.from ( props );
				}
				
				if ( rdfTemplatePath != null )
				{
					log.info ( "Loading graph descriptor template from '{}'", rdfTemplatePath );
					
					// TODO: get rdfLang from the file ext
					rdfTemplate = IOUtils.readFile ( rdfTemplatePath.toString () );
				}
				
				return new OndexGraphDescriptorTool ( graph, context, rdfTemplate, rdfLang, oxlSourceURL );
				
			}
			catch ( IOException ex ) {
				throw new UncheckedIOException ( "Error while creating OndexGraphDescriptorTool: " + ex.getMessage (), ex );
			}
		}
		
		
		public Builder setGraph ( ONDEXGraph graph )
		{
			this.graph = graph;
			return this;
		}

		/**
		 * A set of JSON-like properties that are used to fill the {@link #setRdfTemplate(String) RDF template}.
		 */
		public Builder setContext ( Map<String, Object> context )
		{
			this.context = context;
			return this;
		}

		/**
		 * The {@link #setContext(Map) context} can be specified directly, or via .properties file, here.
		 * @param contextPath
		 * @return
		 */
		public Builder setContextPath ( String contextPath )
		{
			this.contextPath = contextPath;
			return this;
		}

		/**
		 * The descriptor is generated starting from this RDF template.
		 */
		public Builder setRdfTemplate ( String rdfTemplate )
		{
			this.rdfTemplate = rdfTemplate;
			return this;
		}

		/**
		 * The template can either be loaded from a file or passed directly as a string.
		 */
		public Builder setRdfTemplatePath ( String rdfTemplatePath )
		{
			this.rdfTemplatePath = rdfTemplatePath;
			return this;
		}

		/**
		 * The RDF format the template is based on. Default is turtle. This is passed to Jena, see Jena documentation
		 * for valid formats.
		 */
		public Builder setRdfLang ( String rdfLang )
		{
			this.rdfLang = rdfLang;
			return this;
		}

		/**
		 * The OXL's URL is used for the schema:url property, that is, to give the descriptor a 
		 * download URL about the original OXL.
		 * 
		 * It's also used to generate a checksum MD5 hash for the pointed OXL file, which is added to the metadata and 
		 * can be useful to check that a descriptor file correspond to the referenced OXL.
		 * 
		 */
		public Builder setOxlSourceURL ( String oxlSourceURL )
		{
			this.oxlSourceURL = oxlSourceURL;
			return this;
		}		
	}
	// end:Builder
	
	public static final String DESCRIPTOR_CONCEPT_CLASS_ID = "DatasetMetadata";
	public static final String DESCRIPTOR_NODE_ID = StringUtils.uncapitalize ( DESCRIPTOR_CONCEPT_CLASS_ID );
		
	
	private Model _descriptorCache = null;
	private Map<String, Object> _jsonDescriptorCache = null;
	private Map<String, List<Map<String, Object>>> _jsonIndexCache = null;
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () ); 

	
	private OndexGraphDescriptorTool ( 
		ONDEXGraph graph, Map<String, Object> context, String rdfTemplate, String rdfLang, String oxlSourceURL
	)
	{
		super ( graph, context, rdfTemplate, rdfLang, oxlSourceURL );
	}

	/**
	 * Creates the descriptor without saving it in the graph.
	 *  
	 */
	public Model createDescriptor ( boolean doSave )
	{
		log.info ( "Creating Graph Descriptor" );
		var result = JsonLdUtils.getRdfFromTemplate ( this.context, this.rdfTemplate, this.rdfLang );
		
		addGraphStatsDescriptors ( result );

		if ( doSave ) this.saveDescriptor ( result );
		
		return result;
	}
	
	public Model createDescriptor ()
	{		
		return this.createDescriptor ( true );
	}


	
	public ONDEXConcept saveDescriptor ( Model descriptor )
	{
		log.info ( "Adding metadata about graph description" );

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

	public void exportDescriptor ( String filePath, String lang )
	{
		log.info ( "Exporting OXL descriptor to '{}'", filePath );
		
		Model descriptor = getDescriptor ();
		
		try {
			descriptor.write ( new FileWriter ( filePath ), lang );
		}
		catch ( IOException ex ) {
			throw new UncheckedIOException ( "Error while exporting OXL descriptor: " + ex.getMessage (), ex );
		}
		
		log.info ( "Descriptor exported" );
	}

	
	public void exportDescriptor ( String filePath )
	{
		exportDescriptor ( filePath, "TURTLE" );
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

		// when a property like schema:value is used to link values of multiple types, one "value"
		// property is created with one type in @context, while the others are left as "schema:value".
		// So, we have to chase both and hope for the best. This is why everyone hates the Semantic Web...
		//
		.map ( jobj -> {
			if ( !jobj.containsKey ( "schema:value" ) ) return jobj;
			jobj = new HashMap<String, Object> ( jobj );
			jobj.put ( "value", jobj.get ( "schema:value" ) );
			jobj.remove ( "schema:value" );
			return jobj;
		})
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

		assertSchemaAdditionalProp ( 
			descriptor, dsetUri, 
			"KnetMiner:Dataset:Concepts Number",
			descriptor.createTypedLiteral ( this.graph.getConcepts ().size () ) 
		);

		assertSchemaAdditionalProp ( 
			descriptor, dsetUri, 
			"KnetMiner:Dataset:Relations Number",
			descriptor.createTypedLiteral ( this.graph.getRelations ().size () ) 
		);
		
		assertGraphHash ( descriptor, dsetUri );
				
		return descriptor;
	}
	
	
	private String assertGraphHash ( Model descriptor, String datasetUri )
	{
		if ( this.oxlSourceURL == null ) return null;
		URL srcURL = IOUtils.url ( oxlSourceURL );
		
		try
		{
			String hash = DigestUtils.md5Hex ( srcURL.openStream () ).toLowerCase ();

			return assertSchemaAdditionalProp ( 
				descriptor, datasetUri, 
				"KnetMiner:Dataset:Source MD5",
				descriptor.createTypedLiteral ( hash ) 
			);
		}
		catch ( IOException ex )
		{
			throw ExceptionUtils.buildEx ( UncheckedIOException.class, ex,  
			  "Error while hashing the source OXL '%s': %s", oxlSourceURL, ex.getMessage ()
			);
		}
	}
	
	
	private String assertSchemaPropVal ( Model m, String propertyId, Literal value, String unitText )
	{
		String propUri = iri ( 
			"bkr", 
			"property_" + IdUtils.hashUriSignature ( propertyId + value.toString () + (unitText == null ? "" : unitText) )
		); 
		JENAUTILS.assertResource ( m, propUri, iri ( "rdf:type" ), iri ( "schema:PropertyValue" ) );
		JENAUTILS.assertLiteral ( m, propUri, iri ( "schema:propertyID" ), propertyId );
		JENAUTILS.assertLiteral ( m, propUri, iri ( "schema:value" ), value );
		if ( unitText != null )
			JENAUTILS.assertLiteral ( m, propUri, iri ( "schema:unitText" ), unitText );
		return propUri;
	}

	/**
	 * TODO: not used yet
	 */
	private String assertSchemaPropVal ( Model m, String propertyId, Literal value )
	{
		return assertSchemaPropVal ( m, propertyId, value, null );
	}


	private String assertSchemaAdditionalProp ( Model m, String subjectUri, String linkPropUri, String propertyId, Literal value, String unitText )
	{
		String pvuri = assertSchemaPropVal ( m, propertyId, value, unitText );
		JENAUTILS.assertResource ( m, subjectUri, iri ( linkPropUri ), pvuri );
		return pvuri;
	}

	private String assertSchemaAdditionalProp ( Model m, String subjectUri, String propertyId, Literal value, String unitText )
	{
		return assertSchemaAdditionalProp ( m, subjectUri, "schema:additionalProperty", propertyId, value, unitText );
	}

	private String assertSchemaAdditionalProp ( Model m, String subjectUri, String propertyId, Literal value )
	{
		return assertSchemaAdditionalProp ( m, subjectUri, propertyId, value, null );
	}
	
}
