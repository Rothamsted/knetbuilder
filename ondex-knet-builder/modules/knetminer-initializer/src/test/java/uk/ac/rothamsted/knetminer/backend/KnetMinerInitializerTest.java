package uk.ac.rothamsted.knetminer.backend;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.BeforeClass;
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
	
	static KnetMinerInitializer initializer;
	
	static String testCaseOut;
	
	@BeforeClass
	public static void initKnetMinerInitializer() {
		String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + "/";

		// Maven copies test files here.
		var testCasePath = mavenBuildPath + "/test-classes/test-case";
		testCaseOut = testCasePath + "/output";
		
		ONDEXGraph graph = Parser.loadOXL ( testCasePath + "/text-mining.oxl" );
		Assert.assertNotNull ( "graph not loaded!", graph );

		initializer = new KnetMinerInitializer ();
		initializer.setGraph ( graph );
		initializer.setConfigXmlPath ( testCasePath + "/data-source-config.xml" );
		initializer.setDataPath ( testCaseOut );
		
		initializer.loadOptions();
	}
	
	@Test
	public void testGetOptions ()
	{
		
		Assert.assertNotNull ( "StateMachineFilePath Property not found in data-source-config.xml ",
				initializer.getOptions ().get ( "StateMachineFilePath" ) );
		Assert.assertNotNull("StateMachineFilePath Property not found in data-source-config.xml ",
				initializer.getOptions ().get ( "SpeciesTaxId" ) );
	}
	
	@Test
	public void testInitLuceneData ()
	{
		
		initializer.initLuceneData ();
		
		// check Lucene index files exist, using testCaseOut
		File testCaseOutFolder = new File ( testCaseOut );
		File[] listOfFiles = testCaseOutFolder.listFiles ();
		Assert.assertTrue ( "Index folder not created ", Arrays.asList ( listOfFiles ).stream ().anyMatch (
				file -> file.exists () && file.isDirectory ()&& ( file.getName ().endsWith ( "index" ))));
		
		File indexFolder = new File ( testCaseOut + "/index/" );
		File[] indexFiles = indexFolder.listFiles ();
		Assert.assertTrue ( "Index files not created ",indexFiles.length > 0 );
				
	}
	
	@Test
	public void testInitSemanticMotifData ()
	{
		initializer.initSemanticMotifData();
		
		Map<Integer, Set<Integer>> concepts2Genes = initializer.getConcepts2Genes ();
		Map<Integer, Set<Integer>> genes2Concepts = initializer.getGenes2Concepts ();
		Map<Pair<Integer, Integer>, Integer> genes2PathLengths = initializer.getGenes2PathLengths ();
		
		Assert.assertTrue ( "Concepts2Genes is empty or null ", null == concepts2Genes || !concepts2Genes.isEmpty () );
		Assert.assertTrue ( "Genes2Concepts is empty or null ", null == genes2Concepts || !genes2Concepts.isEmpty () );
		Assert.assertTrue ( "Genes2PathLengths is empty or null ", null == genes2PathLengths || !genes2PathLengths.isEmpty () );
		
		// check traverser files exist, using testCaseOut
		File folder = new File ( testCaseOut );
		File[] traverserFiles = folder.listFiles ();
		Assert.assertTrue( "Graph Traverser files not created",
				Arrays.asList ( traverserFiles ).stream ()
						.anyMatch( file -> file.exists () && file.isFile ()
								&& ( file.getName ().equalsIgnoreCase ( "concepts2Genes" )
										|| file.getName ().equalsIgnoreCase ( "genes2Concepts" )
										|| file.getName ().equalsIgnoreCase ( "genes2PathLengths" ))));
	}
}
