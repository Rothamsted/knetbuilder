package uk.ac.rothamsted.knetminer.backend;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Test;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Feb 2022</dd></dl>
 *
 */
public class KnetMinerInitializerCLITest
{
	
	private static String testCasePath;
	
	private static String testCaseOut;
	
	@Test
	public void testBasics () throws IOException
	{
		var mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" );
		mavenBuildPath = Path.of ( mavenBuildPath ).toRealPath ().toString ();
		mavenBuildPath = mavenBuildPath.replace ( '\\', '/' );

		// Maven copies test files here.
		testCasePath = mavenBuildPath + "/test-classes/test-case";
		testCaseOut = testCasePath + "/output";
		
		KnetMinerInitializerCLI.invoke (
			"-i", testCasePath + "/text-mining.oxl", "-d", testCaseOut, "-c" , testCasePath + "/data-source-config.xml" 
		);
	}
}
