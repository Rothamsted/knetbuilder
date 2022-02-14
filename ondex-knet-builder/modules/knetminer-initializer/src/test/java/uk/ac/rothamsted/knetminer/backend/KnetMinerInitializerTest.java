package uk.ac.rothamsted.knetminer.backend;

import org.junit.Test;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.oxl.Parser;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>13 Feb 2022</dd></dl>
 *
 */
public class KnetMinerInitializerTest
{
	@Test
	public void testBasics ()
	{
		String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + "/";
		// Maven copies test files here.
		String testCasePath = mavenBuildPath + "/test-classes/test-case";
		
		ONDEXGraph graph = Parser.loadOXL ( testCasePath + "/text-mining.oxl" );

		var initializer = new KnetMinerInitializer ();
		initializer.setGraph ( graph );
		initializer.setConfigXmlPath ( testCasePath + "/data-source-config.xml" );
		initializer.createKnetMinerData ();
		
		// TODO: check Lucene index files
		// TODO: check traverser files
	}
}
