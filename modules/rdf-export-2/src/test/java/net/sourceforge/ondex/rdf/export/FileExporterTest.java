package net.sourceforge.ondex.rdf.export;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public void testBasics ()
	{
		String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + "/";
		String testResPath = mavenBuildPath + "test-classes/";
		
		ONDEXGraph g = Parser.loadOXL ( testResPath + "text_mining.oxl" );
		
		RDFFileExporter fx = new RDFFileExporter ();
		fx.export ( g, mavenBuildPath + "test.ttl", "TURTLE_BLOCKS" );
		//fx.export ( g, System.out, "TURTLE_BLOCKS" );
		
		// TODO: tests!
	}
	
	@Test 	@Ignore ( "Large file loading, not a real unit test" )
	public void testkNetMinerAra ()
	{
		String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + "/";
		
		ONDEXGraph g = Parser.loadOXL ( "/Users/brandizi/Documents/Work/RRes/ondex_data/knet_miner_data/ArabidopsisKNET_201708.oxl" );
		
		RDFFileExporter fx = new RDFFileExporter ();
		fx.export ( g, mavenBuildPath + "test.ttl", "TURTLE_BLOCKS" );
	}

	@Test 	@Ignore ( "Large file loading, not a real unit test" )
	public void testkNetMinerWheat ()
	{
		String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + "/";
		
		ONDEXGraph g = Parser.loadOXL ( "/Users/brandizi/Documents/Work/RRes/ondex_data/knet_miner_data/WheatKNET.oxl" );
		
		RDFFileExporter fx = new RDFFileExporter ();
		fx.export ( g, mavenBuildPath + "wheat.ttl", "TURTLE_BLOCKS" );
		log.info ( "DONE" );
	}	
	
}
