package uk.ac.rothamsted.knetminer.backend;

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.oxl.Parser;
import net.sourceforge.ondex.utils.OndexPluginUtils;

/**
 * TODO: comment me!
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
		testCaseOut = testCasePath + "/output";
		
		graph = Parser.loadOXL ( testCasePath + "/text-mining.oxl" );
		Assert.assertNotNull ( "graph not loaded!", graph );
		
	}
	
	@Test
	public void testBasics ()
	{
		Map<String, Object> pluginArgs =  Map.of (
				"configXmlPath",testCasePath + "/data-source-config.xml" ,
				"dataPath",testCaseOut
			);
		
		// TODO: populate the args with test params (see KnetMinerInitializerTest)
		// TODO: check this runs and do basic tests on output directories existence
		// (we don't need detailed verifications here, they already occurs in KnetMinerInitializerTest)		
		OndexPluginUtils.runPlugin ( KnetMinerInitializerPlugIn.class, graph, pluginArgs );
		
		File folder = new File ( testCaseOut );
		
		String[] traverserFileNames = (String[]) Stream.of ( folder.listFiles () )
				.map ( File::getName )
				.toArray ( String[]::new );
		
		Stream.of ( "concepts2Genes", "genes2Concepts", "genes2PathLengths"  )
		.map ( name -> name + ".ser" )
		.forEach ( name -> 
			assertTrue ( 
				format ( "Traverser File '%s' not created!", name ), 
				ArrayUtils.contains ( traverserFileNames, name ) 
			)
		);	
	}
	
	
}
	
