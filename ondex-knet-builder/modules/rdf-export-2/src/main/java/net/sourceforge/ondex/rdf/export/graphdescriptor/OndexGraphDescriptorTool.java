package net.sourceforge.ondex.rdf.export.graphdescriptor;

import static net.sourceforge.ondex.core.util.ONDEXGraphUtils.getOrCreateConceptClass;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.ONDEXGraphUtils;
import uk.ac.ebi.utils.exceptions.ExceptionUtils;
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
		return JsonLdUtils.getRdfFromTemplate ( context, rdfTemplate, rdfLang );
	}
	
	/**
	 * TODO: get rdfLang from the extension
	 */
	public Model createDescriptor ( Map<String, Object> context, Path rdfTemplatePath, String rdfLang )
	{
		try {
			var tpl = IOUtils.readFile ( rdfTemplatePath.toString () );
			return JsonLdUtils.getRdfFromTemplate ( context, tpl, rdfLang );
		}
		catch ( IOException ex ) {
			throw ExceptionUtils.buildEx ( 
				UncheckedIOException.class, ex, 
				"Error while reading metadata template '%s': %s", rdfTemplatePath.toAbsolutePath (), ex.getMessage () 
			);
		}
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

		return getDescriptorConcept ( rdfOut.toString () );
	}
	
	
	
	/**
	 * Fetches the descriptor from the graph.
	 */
	public Model getDescriptor ()
	{		
		var result = ModelFactory.createDefaultModel ();
		var descriptorConcept = getDescriptorConcept ();
		
		if ( descriptorConcept != null )
		{
			var sr = new StringReader ( descriptorConcept.getDescription () );
			result.read ( sr, null, "TURTLE" );
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
	 */
	public Map<String, Object> getDescriptorAsJsonLd ()
	{
		return null;
	}

	/**
	 * Returns a by-type index of the descriptor.
	 */
	public Map<String, List<Map<String, Object>>> getDescriptorTypes ()
	{
		return null;
	}
	
	/**
	 * Returns the schema:Dataset instance from the saved descriptor
	 */
	public Map<String, Object> getDescriptorDataset () {
		return null;
	}
	
	/**
	 * Returns the schema:Organization instance from the saved descriptor
	 */
	public Map<String, Object> getDescriptorOrganization () {
		return null;
	}
	
}
