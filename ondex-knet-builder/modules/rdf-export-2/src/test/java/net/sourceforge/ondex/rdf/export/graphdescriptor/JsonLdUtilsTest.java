package net.sourceforge.ondex.rdf.export.graphdescriptor;

import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import uk.ac.ebi.utils.io.IOUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Mar 2021</dd></dl>
 *
 */
public class JsonLdUtilsTest
{
	private static Model datasetModel = null;
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () ); 
	
	@BeforeClass
	public static void init () throws IOException
	{
		String rdfTpl = IOUtils.readResource ( JsonLdUtils.class, "dataset-test-template.ttl" );
		Map<String, Object> values = new HashMap<> ();
		values.put ( "datasetId", "KnetMiner:Triticum_aestivum" );
		values.put ( "datasetTitle", "Knetminer's knowledge graph about wheat (Triticum aestivum)" );
		values.put ( 
			"datasetDescription", 
			"Knetminer is a gene discovery platform,\n"
			+ "which allows for exploring knwoledge graphs computed from common plant biology data, such as ENSEMBL,\n"
			+ "UniProt, PUBMED and more." 
		);
		values.put ( "datasetVersion", 45 );
		
		datasetModel = JsonLdUtils.getRdfFromTemplate ( values, rdfTpl, "TURTLE" );
		// NamespaceUtils.registerNs ( "bkg", "http://knetminer.org/data/rdf/resources/graphs/" );
		NamespaceUtils.registerNs ( datasetModel.getNsPrefixMap () );
	}
	
	@Test
	public void testGetRdfFromTemplate ()
	{
		assertTrue ( 
			"schema:identifier not found!",
			datasetModel.contains (
				datasetModel.getResource ( iri ( "bkg:wheat" ) ),
				datasetModel.getProperty ( iri ( "schema:identifier" ) ),
				datasetModel.createLiteral ( "KnetMiner:Triticum_aestivum" )
			)
		);
		
		assertTrue ( 
			"schema:identifier not found!",
			datasetModel.contains (
				datasetModel.getResource ( iri ( "bkg:wheat" ) ),
				datasetModel.getProperty ( iri ( "schema:version" ) ),
				datasetModel.createLiteral ( "45" )
			)
		);
	}

	/**
	 * Access JSON objects by means of Jackson APIs.
	 */
	@Test
	public void testRdf2JsonLd ()
	{
		var jsonMap = JsonLdUtils.rdf2JsonLd ( datasetModel, true );
		assertNotNull ( "JSON-LD is null!", jsonMap );

		var jsmapper = new ObjectMapper ();
		var jsNode = jsmapper.valueToTree ( jsonMap );
		
		var jsOrg = jsNode.get ( "bkr:rresOrg" );
		assertNotNull ( "Organisation's JSON not found!", jsOrg );
		assertTrue ( "Organisation's JSON wrong JS type!", jsOrg.isContainerNode () );
		
		assertTrue ( "Organisation's keywords isn't an array!", jsOrg.get ( "keywords" ).isArray () );
		var kw = jsmapper.convertValue ( jsOrg.get ( "keywords" ), ArrayList.class );
		assertEquals ( "Wrong no. of organisation's keywords!", 6, kw.size () );
		
		assertTrue ( "Expected keywords for organisation not found!",  kw.contains ( "Agricultural science" ) );
		// log.info ( "JSON-LD is: {}", JsonLdUtils.toJson ( json ) );
	}

	/**
	 * Same as {@link #testRdf2JsonLd()}, but using our own helpers to access the data.
	 */
	@Test
	public void testRdf2JsonLdHelpers ()
	{
		var jsonMap = JsonLdUtils.rdf2JsonLd ( datasetModel, true );
		assertNotNull ( "JSON-LD is null!", jsonMap );
		
		Map<String, Object> jsOrg = JsonLdUtils.asValue ( jsonMap, "bkr:rresOrg" );
		assertNotNull ( "Organisation's JSON not found!", jsOrg );
		
		List<String> kw = JsonLdUtils.asList ( jsOrg, "keywords" );
		assertEquals ( "Wrong no. of organisation's keywords!", 6, kw.size () );
		assertTrue ( "Expected keywords for organisation not found!",  kw.contains ( "Agricultural science" ) );
		// log.info ( "JSON-LD is: {}", JsonLdUtils.toJson ( json ) );
	}
	
	
	
	@Test
	@SuppressWarnings ( { "unchecked", "rawtypes" } )
	public void testTypeIndex ()
	{
		var json = JsonLdUtils.rdf2JsonLd ( datasetModel, true );
		
		var typeIndex = JsonLdUtils.indexByType ( json );
		log.info ( "Indexed JSON-LD is: {}", JsonLdUtils.serialize ( (Map) typeIndex ) );
		
		// TODO: test Dataset
	}
	
}
