package net.sourceforge.ondex.rdf.export;

import static net.sourceforge.ondex.rdf.export.RDFFileExporter.DEFAULT_X_LANG;
import static org.junit.Assert.assertTrue;

import java.io.File;
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
		fx.export ( g, outPath, DEFAULT_X_LANG );
		
		// Verify the resulting RDF		
		SparqlBasedTester tester = new SparqlBasedTester ( outPath, NamespaceUtils.asSPARQLProlog () );
		String sparqlTestsPath = mavenBuildPath + "test-classes/text_mining_tests";
		
		Assert.assertTrue ( 
			"No SPARQL tests found at '" + sparqlTestsPath + "'!", 
			tester.askFromDirectory ( sparqlTestsPath ) > 0 
		);
	}

	@Test
	public void testLangParam ()
	{
		String mavenPomPath = System.getProperty ( "maven.basedir", "." ) + "/";
		String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + "/";
		
		ONDEXGraph g = Parser.loadOXL ( mavenPomPath + "src/main/assembly/resources/examples/text_mining.oxl" );
		
		String outPath = mavenBuildPath + "test.rdf";
		RDFFileExporter fx = new RDFFileExporter ();		
		fx.export ( g, outPath, "RDFXML" );
		
		File fout = new File ( outPath );
		
		assertTrue ( "XML/RDF File not created!", fout.exists () );
		assertTrue ( "XML/RDF File is empty!", fout.length () > 0 );		
	}
	
	@Test @Ignore ( "Large file loading, not a real unit test" )
	public void testkNetMinerAra ()
	{
		String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + "/";
		
		ONDEXGraph g = Parser.loadOXL ( "/Users/brandizi/Documents/Work/RRes/ondex_data/knet_miner_data/ArabidopsisKNET_201708.oxl" );
		
		RDFFileExporter fx = new RDFFileExporter ();
		fx.export ( g, mavenBuildPath + "test.ttl", DEFAULT_X_LANG );
	}

	
	@Test @Ignore ( "Large file loading, not a real unit test" )
	public void testkNetMinerWheat ()
	{
		String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + "/";
		
		// ONDEXGraph g = Parser.loadOXL ( "/Users/brandizi/Documents/Work/RRes/ondex_data/knet_miner_data/WheatKNET.oxl" );
		ONDEXGraph g = Parser.loadOXL ( "/Users/brandizi/Downloads/WheatKNET-sample.oxl" );
		
		RDFFileExporter fx = new RDFFileExporter ();
		fx.export ( g, mavenBuildPath + "wheat.ttl", DEFAULT_X_LANG );
		log.info ( "DONE" );
	}	
	
}
