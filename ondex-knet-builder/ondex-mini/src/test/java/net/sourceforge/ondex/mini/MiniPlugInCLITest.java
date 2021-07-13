package net.sourceforge.ondex.mini;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.log4j.Logger;
import org.junit.Test;


/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>13 Jul 2021</dd></dl>
 *
 */
public class MiniPlugInCLITest
{
	private String targetPath = System.getProperty ( "project.build.directory", "target" );
	private String inOxl = targetPath + "/rdf-export-2-plugin/examples/text_mining.oxl";

  private Logger log = Logger.getLogger ( this.getClass() );
	
	
	@Test
	public void testBasics () throws IOException
	{
		String outOxl = targetPath + "/plugin-cli-test.oxl";
		String summaryPath = targetPath + "/plugin-cli-test-summary.xml"; 
		String pluginFQN = "net.sourceforge.ondex.export.graphinfo.Export";

		MiniPlugInCLI.invoke (
			"-i", inOxl, "-o", outOxl, "-r", pluginFQN,
			"ExportFile=" + summaryPath
		);
				
		assertTrue ( "No output OXL!", new File ( outOxl ).exists () );
		assertTrue ( "Empty output!", Files.size ( Path.of ( outOxl ) ) > 0l );

		assertTrue ( "No plugin output!", new File ( summaryPath ).exists () );
		assertTrue ( "Empty plugin output!", Files.size ( Path.of ( summaryPath ) ) > 0l );
	}
	
	
	@Test
	public void testMissingPlugin () throws IOException
	{
		String outOxl = targetPath + "/plugin-cli-test-noplugin.oxl";
		
		MiniPlugInCLI.invoke (
			"-i", inOxl, "-o", outOxl
		);
		
		assertTrue ( "No output OXL!", new File ( outOxl ).exists () );
		assertTrue ( "Empty output!", Files.size ( Path.of ( outOxl ) ) > 0l );		
	}
	
	
	@Test
	public void testMissingInput () throws IOException
	{
		String outOxl = targetPath + "/plugin-cli-test-noin.oxl";
		String summaryPath = targetPath + "/plugin-cli-test-summary.xml"; 
		String pluginFQN = "net.sourceforge.ondex.export.graphinfo.Export";

		MiniPlugInCLI.invoke (
			"-o", outOxl, "-r", pluginFQN,
			"ExportFile=" + summaryPath
		);
				
		assertTrue ( "No output OXL!", new File ( outOxl ).exists () );
		assertTrue ( "Empty output!", Files.size ( Path.of ( outOxl ) ) > 0l );

		assertTrue ( "No plugin output!", new File ( summaryPath ).exists () );
		assertTrue ( "Empty plugin output!", Files.size ( Path.of ( summaryPath ) ) > 0l );
	}
	
	
	@Test
	public void testArguments () throws IOException
	{
		String pluginFQN = "net.sourceforge.ondex.export.graphinfo.Export";

		// Capture the output into memory
		PrintStream outBkp = System.out;
		ByteArrayOutputStream outBuf = new ByteArrayOutputStream ();
		System.setOut ( new PrintStream ( outBuf ) );

		MiniPlugInCLI.invoke ( "-r", pluginFQN, "--arguments" );
		
		System.setOut ( outBkp ); // restore the original output
		
		var outStr = outBuf.toString ();
		
		log.info ( "--arguments output:\n" + outStr + "\n" );

		assertTrue (
			"Expected output not found!",
			outStr.contains ( 
				"name: ExportFile, type: java.lang.String, description: Data file to export, " +
				"default: null, required: Y, multiple: N"
			)
		);
	}
}
