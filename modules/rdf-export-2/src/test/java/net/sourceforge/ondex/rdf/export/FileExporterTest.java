package net.sourceforge.ondex.rdf.export;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.marcobrandizi.rdfutils.jena.SparqlBasedTester;
import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.oxl.Parser;

/**
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Nov 2017</dd></dl>
 *
 */
public class FileExporterTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	
	@Test
	public void testBasics () throws IOException
	{
		String mavenPomPath = System.getProperty ( "maven.basedir", "." ) + "/";
		String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + "/";
		
		ONDEXGraph g = Parser.loadOXL ( mavenPomPath + "src/main/assembly/resources/examples/text_mining.oxl" );
		
		String outPath = mavenBuildPath + "test.ttl";
		RDFFileExporter fx = new RDFFileExporter ();		
		fx.export ( g, outPath, "TURTLE_BLOCKS" );
		
		// Verify the resulting RDF		
		SparqlBasedTester tester = new SparqlBasedTester ( outPath, NamespaceUtils.asSPARQLProlog () );
		String sparqlTestsPath = mavenBuildPath + "test-classes/text_mining_tests";
		
		Assert.assertTrue ( 
			"No SPARQL tests found at '" + sparqlTestsPath + "'!", 
			tester.askFromDirectory ( sparqlTestsPath ) > 0 
		);
	}

	
	@Test @Ignore ( "Large file loading, not a real unit test" )
	public void testkNetMinerAra ()
	{
		String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + "/";
		
		ONDEXGraph g = Parser.loadOXL ( "/Users/brandizi/Documents/Work/RRes/ondex_data/knet_miner_data/ArabidopsisKNET_201708.oxl" );
		
		RDFFileExporter fx = new RDFFileExporter ();
		fx.export ( g, mavenBuildPath + "test.ttl", "TURTLE_BLOCKS" );
	}

	
	@Test @Ignore ( "Large file loading, not a real unit test" )
	public void testkNetMinerWheat ()
	{
		String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + "/";
		
		ONDEXGraph g = Parser.loadOXL ( "/Users/brandizi/Documents/Work/RRes/ondex_data/knet_miner_data/WheatKNET.oxl" );
		
		RDFFileExporter fx = new RDFFileExporter ();
		fx.export ( g, mavenBuildPath + "wheat.ttl", "TURTLE_BLOCKS" );
		log.info ( "DONE" );
	}	
	
}
