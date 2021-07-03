package net.sourceforge.ondex.rdf.export.graphdescriptor;

import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
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
		
	private Logger log = LoggerFactory.getLogger ( this.getClass () ); 
	
	@BeforeClass
	public static void init () throws IOException
	{
		Map<String, Object> values = new HashMap<> ();
		values.put ( "datasetId", "wheat" );
		values.put ( "datasetAccession", "KnetMiner:Triticum_aestivum" );
		values.put ( "datasetTitle", "Knetminer's knowledge graph about wheat (Triticum aestivum)" );
		values.put ( 
			"datasetDescription", 
			"Knetminer is a gene discovery platform,\n"
			+ "which allows for exploring knwoledge graphs computed from common plant biology data, such as ENSEMBL,\n"
			+ "UniProt, PUBMED and more." 
		);
		values.put ( "datasetVersion", 45 );
		
		var descritorTool = new OndexGraphDescriptorTool ( graph );
		var rdfTplPath = Path.of ( "target/test-classes/dataset-test-template.ttl" );
		var datasetModel = descritorTool.saveDescriptor ( values, rdfTplPath, "TURTLE" );
		
		NamespaceUtils.registerNs ( datasetModel.getNsPrefixMap () );
	}
	
	@Test
	public void testSaveDescriptor ()
	{
		var descritorTool = new OndexGraphDescriptorTool ( graph );
		assertNotNull ( "No descritor concept!", descritorTool.getDescriptorConcept () );
	}
	
	@Test
	public void testDescriptorContents ()
	{
		var descritorTool = new OndexGraphDescriptorTool ( graph );
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
	
}
