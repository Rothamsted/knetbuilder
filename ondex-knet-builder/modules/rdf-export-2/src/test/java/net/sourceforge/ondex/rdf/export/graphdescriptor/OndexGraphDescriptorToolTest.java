package net.sourceforge.ondex.rdf.export.graphdescriptor;

import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 Jul 2021</dd></dl>
 *
 */
public class OndexGraphDescriptorToolTest
{
	private static ONDEXGraph graph = new MemoryONDEXGraph ( "testDataset" );
	private static OndexGraphDescriptorTool descritorTool;	
	
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () ); 
	private static Logger slog = LoggerFactory.getLogger ( OndexGraphDescriptorToolTest.class ); 
	
	/**
	 * Saves a test descriptor into the {@link #graph}. 
	 */
	@BeforeClass
	public static void init () throws IOException
	{
		descritorTool = new OndexGraphDescriptorTool.Builder ()
			.setGraph ( graph )
			.setContextPath ( "src/main/assembly/resources/examples/descriptor.properties" )
			.setRdfTemplatePath ( "src/main/assembly/resources/examples/descriptor-template.ttl" )
			.setRdfLang ( "TURTLE" )
			.build ();
				
		var datasetModel = descritorTool.createDescriptor ();
		NamespaceUtils.registerNs ( datasetModel.getNsPrefixMap () );
	}
	
	@Test
	public void testSaveDescriptor ()
	{
		assertNotNull ( "No descritor concept!", descritorTool.getDescriptorConcept () );
	}
	
	@Test
	public void testDescriptorContents () throws IOException
	{
		descritorTool.exportDescriptor ( "target/graph-descriptor-tool-test.ttl" );
		var descrModel = descritorTool.getDescriptor ();
				
		assertTrue ( 
			"schema:identifier not found!",
			descrModel.contains (
				descrModel.getResource ( iri ( "bkg:wheat" ) ),
				descrModel.getProperty ( iri ( "schema:identifier" ) ),
				descrModel.createLiteral ( "KnetMiner:Triticum_aestivum" )
			)
		);
		
		assertTrue ( 
			"schema:identifier not found!",
			descrModel.contains (
				descrModel.getResource ( iri ( "bkg:wheat" ) ),
				descrModel.getProperty ( iri ( "schema:version" ) ),
				descrModel.createLiteral ( "45" )
			)
		);		
	}
	
	@Test
	public void testDescriptorTypeFetch ()
	{
		var org = descritorTool.getDescriptorOrganization ();
		assertEquals (
			"Wrong organisation's legal name!",
			"Rothamsted Research", JsonLdUtils.asValue ( org, "legalName", true )
		);
	}

	
	@Test
	public void testGraphSummaryProps ()
	{
		Map<String, Map<String, Object>> pvals = descritorTool.getDatasetAdditionalProperties ();
		
		int nconcepts = OndexGraphDescriptorTool.getPropertyValueAsInt ( pvals, "KnetMiner:Dataset:Concepts Number" );
		assertEquals ( "Wrong property value for concepts number", 0, nconcepts );		
	}
}
