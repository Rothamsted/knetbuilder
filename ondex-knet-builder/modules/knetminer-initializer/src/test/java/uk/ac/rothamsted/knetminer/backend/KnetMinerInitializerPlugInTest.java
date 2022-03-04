package uk.ac.rothamsted.knetminer.backend;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.oxl.Parser;
import net.sourceforge.ondex.utils.OndexPluginUtils;

/**
 * Junit test class for KnetMinerInitializerPlugIn
 *
 * @author brandizi
 * @author jojicunnunni
 * 
 * <dl><dt>Date:</dt><dd>23 Feb 2022</dd></dl>
 *
 */
public class KnetMinerInitializerPlugInTest
{
	private static ONDEXGraph graph;
	
	private static String testCasePath;
	
	private static String testCaseOut;
	
	@BeforeClass
	public static void initGraph () throws IOException
	{
		var mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" );
		mavenBuildPath = Path.of ( mavenBuildPath ).toRealPath ().toString ();
		mavenBuildPath = mavenBuildPath.replace ( '\\', '/' );

		// Maven copies test files here.
		testCasePath = mavenBuildPath + "/test-classes/test-case";
		testCaseOut = testCasePath + "/output-plugin";
		
		var outFile = new File ( testCaseOut );
		if ( !outFile.exists () ) outFile.mkdir ();
		
		// We need to resolve circular dependencies and get it from the backend module
		graph = Parser.loadOXL ( testCasePath + "/poaceae-sample.oxl" );
		Assert.assertNotNull ( "graph not loaded!", graph );
	}
	
	@Test
	public void testBasics ()
	{
		Map<String, Object> pluginArgs =  Map.of (
			"configXmlPath", testCasePath + "/data-source-config.xml" ,
			"dataPath", testCaseOut
		);
		
		OndexPluginUtils.runPlugin ( KnetMinerInitializerPlugIn.class, graph, pluginArgs );
		
		assertTrue ( "Lucene output not found!", new File ( testCaseOut + "/index" ).exists () );
		assertTrue ( "Traverser output not found!", new File ( testCaseOut + "/concepts2Genes.ser" ).exists () );
	}
	
	
}
	
