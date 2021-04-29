package net.sourceforge.ondex.rdf.export.graphdescriptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.utils.io.IOUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Mar 2021</dd></dl>
 *
 */
public class GraphDescriptorExporterTest
{
	private static Model datasetModel = null;
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () ); 
	
	@BeforeClass
	public static void init () throws IOException
	{
		GraphDescriptorExporter dsx = new GraphDescriptorExporter ();
		String rdfTpl = IOUtils.readResource ( GraphDescriptorExporter.class, "dataset-test-template.ttl" );
		Map<String, Object> values = new HashMap<> ();
		values.put ( "datasetId", "Knetminer:Triticum_aestivum" );
		values.put ( "datasetTitle", "Knetminer's knowledge graph about wheat (Triticum aestivum)" );
		values.put ( 
			"datasetDescription", 
			"Knetminer is a gene discovery platform,\n"
			+ "which allows for exploring knwoledge graphs computed from common plant biology data, such as ENSEMBL,\n"
			+ "UniProt, PUBMED and more." 
		);
		values.put ( "datasetVersion", 45 );
		
		datasetModel = dsx.getDescriptor ( values, rdfTpl, "TURTLE" );
	}
	
	@Test // TODO: todo!
	public void testGetDescriptor ()
	{
	}

	@Test
	public void testJsonDescriptor ()
	{
		GraphDescriptorExporter dsx = new GraphDescriptorExporter ();
		var json = dsx.getDescriptorAsJsonLd ( datasetModel, true );
		
		log.info ( "JSON-LD is: {}", dsx.toJson ( json ) );
	}

	@Test
	@SuppressWarnings ( "unchecked" )
	public void testTypeIndex ()
	{
		GraphDescriptorExporter dsx = new GraphDescriptorExporter ();
		var json = dsx.getDescriptorAsJsonLd ( datasetModel, true );
		
		var typeIndex = dsx.getJsonLdTypeIndex ( json );
		log.info ( "JSON-LD is: {}", dsx.toJson ( (Map) typeIndex ) );
	}
	
}
