package net.sourceforge.ondex.rdf.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the {@link URIAdditionCLI}.
 */
public class URIAdditionCLITest
{
	private static Logger log = LoggerFactory.getLogger ( URIAdditionCLITest.class );

	@BeforeClass
	public static void setNoExitOption ()
	{
		// Prevents the CLI from invoking System.exit()
		System.setProperty ( RDFFileExporterCLI.NO_EXIT_PROP, "true" );
	}
	
	
	
	@Test
	public void testCLI ()
	{		
		String outPath = "target/uri-adding-test-out.oxl";
		
		URIAdditionCLI.main (
			"target/dependency/rdf-export-2-plugin/examples/text_mining.oxl",
			outPath
		);
		
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

		URIAdditionCLI.main (  "--help" );
		
		System.setOut ( outBkp );  // restore the original output

		log.debug ( "CLI output:\n{}", outBuf.toString () );
		assertTrue ( "Can't find CLI output!", outBuf.toString ().contains ( "*** Ondex URI Adder ***" ) );
		assertEquals ( "Bad exit code!", 1, URIAdditionCLI.getExitCode () );
	}

}
