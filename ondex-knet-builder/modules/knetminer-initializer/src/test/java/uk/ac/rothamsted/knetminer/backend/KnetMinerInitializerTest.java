package uk.ac.rothamsted.knetminer.backend;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.oxl.Parser;

/**
 * The usual Junit tests for {@link KnetMinerInitializer}
 * 
 * @author brandizi
 * @author jojicunnunni
 * 
 * <dl><dt>Date:</dt><dd>13 Feb 2022</dd></dl>
 *
 */
public class KnetMinerInitializerTest
{
	
	private static KnetMinerInitializer initializer;

	private static String testCasePath;
	private static String testCaseOut;
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	
	@BeforeClass
	public static void initKnetMinerInitializer() throws IOException
	{
		var mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" );
		mavenBuildPath = Path.of ( mavenBuildPath ).toRealPath ().toString ();
		mavenBuildPath = mavenBuildPath.replace ( '\\', '/' );

		// Maven copies test files here.
		testCasePath = mavenBuildPath + "/test-classes/test-case";
		testCaseOut = testCasePath + "/output";
		
		ONDEXGraph graph = Parser.loadOXL ( testCasePath + "/poaceae-sample.oxl" );
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
		
		assertEquals ( 
			"Wrong value for StateMachineFilePath property!",
			"file:///" + testCasePath + "/SemanticMotifs.txt",
			initializer.getOptions ().getString ( "StateMachineFilePath" )
		);
		assertEquals (
			"Wrong value for StateMachineFilePath config property!",
			4565, (int) initializer.getOptions ().getInt ( "SpeciesTaxId" ) 
		);
	}
	
	@Test
	public void testInitLuceneData ()
	{
		
		initializer.initLuceneData ();
		
		// check Lucene index files exist, using testCaseOut
		File testCaseOutFolder = new File ( testCaseOut );
		File[] listOfFiles = testCaseOutFolder.listFiles ();
		Assert.assertTrue ( "Index folder not created ", 
			Arrays.asList ( listOfFiles )
			.stream ()
			.anyMatch (
				file -> file.exists () && file.isDirectory () && ( file.getName ().endsWith ( "index" )
		)));
		
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
		
		BiConsumer<String, Map<?, ?>> verifier = (name, map) -> {
			assertNotNull ( String.format ( "%s is null!", name ), map );
			assertFalse ( format ( "%s is empty!", name ), map.isEmpty () );
			log.info ( "{} has {} mappings", name, map.size() );
		};
		
		verifier.accept ( "concepts2Genes", concepts2Genes );
		verifier.accept ( "genes2Concepts", genes2Concepts );
		verifier.accept ( "genes2PathLengths", genes2PathLengths );
		
		
		// check traverser files exist, using testCaseOut
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
	
	/**
	 * TODO: to be implemented, we don't need it in the short time (very likely, the OXL building pipeline will need
	 * to rebuild everything at each invocation). 
	 */
	public void testLuceneFilesReuse ()
	{
		// 1) read the modification date/time for the Lucene index directory 
		// 2) reissue initLuceneData() (in the test it would be a second execution after the one in initKnetMinerInitializer()) 
		// 3) read the directory's date/time again and check it didn't change. 
		// This verifies that files are not re-created when they already exist.		
	}
	
	/**
	 * TODO: like {@link #testInitLuceneData()}, not immediately needed.
	 */
	public void testTraverserFilesReuse ()
	{
		// Same as testLuceneFilesReuse
	}
}
