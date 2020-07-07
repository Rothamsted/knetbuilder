package net.sourceforge.ondex.rdf.rdf2oxl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

import net.sourceforge.ondex.rdf.rdf2oxl.support.TestUtils;

/**
 * Unit test for the example CLI {@link App}.
 */
public class Rdf2OxlCLITest
{
	private static Logger log = LoggerFactory.getLogger ( Rdf2OxlCLITest.class );

	@BeforeClass
	public static void setNoExitOption ()
	{
		// Prevents the CLI from invoking System.exit()
		System.setProperty ( Rdf2OxlCLI.NO_EXIT_PROP, "true" );
	}
	
	
	@Test
	public void testCLI () throws IOException
	{		
		String tdbPath = "target/cli-test-tdb";

		TestUtils.generateTDB ( tdbPath, 
			Pair.of ( new FileInputStream ( "src/main/assembly/resources/data/bioknet.owl" ), "RDF/XML" ),
			Pair.of ( new FileInputStream ( "src/main/assembly/resources/data/bk_ondex.owl" ), "RDF/XML" ),
			Pair.of ( Resources.getResource ( "support_test/publications.ttl" ).openStream (), "TURTLE" )
		);
		
		String outPath = "target/cli-test.oxl";
		
		Rdf2OxlCLI.main ( "--tdb", tdbPath, outPath );
		
		File fout = new File ( outPath );
		Assert.assertTrue ( "No output!", fout.exists () );
		Assert.assertTrue ( "Empty output!", fout.length () > 0 );
	}
	

	@Test
	public void testHelpOption()
	{
		// Capture the output into memory
		PrintStream outBkp = System.out;
		ByteArrayOutputStream outBuf = new ByteArrayOutputStream ();
		System.setOut ( new PrintStream ( outBuf ) );

		Rdf2OxlCLI.main (  "--help" );
		
		System.setOut ( outBkp );  // restore the original output

		log.debug ( "CLI output:\n{}", outBuf.toString () );
		assertTrue ( "Can't find CLI output!", outBuf.toString ().contains ( "--tdb" ) );
		assertEquals ( "Bad exit code!", 1, Rdf2OxlCLI.getExitCode () );
	}

}
