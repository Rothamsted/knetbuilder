package net.sourceforge.ondex.parser.medline2.xml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.ondex.config.LuceneRegistry;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.export.oxl.Export;
import net.sourceforge.ondex.mapping.tmbased.Mapping;
import net.sourceforge.ondex.mapping.tmbased.args.ArgumentNames;
import net.sourceforge.ondex.parser.oxl.Parser;
import net.sourceforge.ondex.tools.DirUtils;
import net.sourceforge.ondex.utils.OndexPluginUtils;

/**
 * A few tests for the {@link Mapping text mapping plug-in}
 *
 * @author jojicunnunni
 * 
 * <dl><dt>Date:</dt><dd>22 June 2022</dd></dl>
 *
 */
public class TextMappingTest
{
	private static ONDEXGraph graph;
	
	private static final String BUILD_PATH = 
		Path.of ( System.getProperty ( "maven.buildDirectory", "target" ) ).toString ();

	// test-case doesn't mean anything, imagine to have another 100 test cases...
	// private static final String TEST_DATA_PATH = BUILD_PATH + "/test-classes/test-case";
	private static final String TEST_DATA_PATH = BUILD_PATH + "/test-classes/text-mapping";

	private static final String LUCENE_PATH = BUILD_PATH + "tm-test-index";
	
	@Before
	public void initGraph () throws IOException
	{
		// WHAT THE HELL!?!
		// var outFile = new File ( testCasePath );
		// if ( !outFile.exists () ) outFile.mkdir ();
		
		graph = Parser.loadOXL ( TEST_DATA_PATH + "/textmining-sample.oxl" );

		// OH, COME ON! Look at the code, this can't be null! 
		// Assert.assertNotNull ( "graph not loaded!", graph );
		loadLuceneEnv(graph);
	}
	
	//@Test

	/** TODO: this is a mess!
	 *  - Do you want to use the test file, create a graph programatically, or both? Why? For the moment, using the file could be just enough
	 *  - we need testBasics() to verify that it works fine WITHOUT the stop word option 
	 *    - check relation sizes before/after the graph
	 *    - check 1-3 expected relations exist, maybe check some that shouldn't exist
	 *      - For at least one, check attributes like TFIDF to ensure it's > 0
	 *  - we need testStopWords() to verify the SW functionality
	 *    - check we have more relations after the plugin run (just to know it did something)
	 *    - check a few expected relations
	 *    - check that there are omitted relations as expected
	 */	
	public void testBasics ()
	{
		
		int sizeBeforeTextMining = graph.getRelations ().size ();
		
		Map<String, Object> pluginArgs = Map.of (
			// I've renamed it to have some consistency with file name conventions
			// You've added stuff into ArgumentNames, but then you don't use them?!
			// TODO: fix the other invocations too
			ArgumentNames.STOP_WORDS_ARG, TEST_DATA_PATH + "/stop-words.txt" ,
			ArgumentNames.CONCEPTCLASS_ARG, "Gene", 
			// "Search", "exact", it's a default, not needed
			// TODO: another test where genes that are ignored by one name, are then considered by another 
			ArgumentNames.PREFERRED_NAMES_ARG, "true" 
			// "UseFullText", "false" it's a default 
		);
		OndexPluginUtils.runPlugin ( Mapping.class, graph ,pluginArgs);
		 
		 int sizeAfterTextMining = graph.getRelations().size();
		 
		 Assert.assertTrue( "Mapping concepts to publications failed ", sizeBeforeTextMining == sizeAfterTextMining);
		 
		 // TODO: test a couple of cases where links are expected, maybe others where they're not
	}
	
	@Test
	public void testBasics2 () throws IOException
	{
		ONDEXGraph testGraph = createTestGraph ( "test" );
		
		int sizeBeforeTextMining = testGraph.getRelations ().size ();
		loadLuceneEnv ( testGraph );
			
		Map<String, Object> pluginArgs = Map.of ( 
		  "GeneNameStopWords", TEST_DATA_PATH + "/geneNameStopWords.txt" ,
		  "ConceptClass", "Gene", 
		  "Search", "exact",
		  "OnlyPreferredNames", "true",
		  "UseFullText", "false" );
		OndexPluginUtils.runPlugin ( Mapping.class, testGraph ,pluginArgs );
		
		int sizeAfterTextMining = testGraph.getRelations ().size ();
		Assert.assertTrue ( "Mapping concepts to publications failed ", sizeBeforeTextMining == sizeAfterTextMining );
	}
	
	private static LuceneEnv loadLuceneEnv ( ONDEXGraph graph ) throws IOException {
		
		DirUtils.deleteTree ( LUCENE_PATH );
		LuceneEnv lenv = new LuceneEnv ( LUCENE_PATH, true );
		lenv.setONDEXGraph ( graph );
		LuceneRegistry.sid2luceneEnv.put ( graph.getSID (), lenv );
		return lenv;
	}
	
	/**
	 * Do we need this, or are we fine with the test file?
	 * 
	 */
	private static ONDEXGraph createTestGraph ( String name ) {
		
		// MEH! Use target/ and keep the test files there, they might be needed for inspection and
		// mvn clean gets rid of them
		File testfile = new File ( System.getProperty ( "java.io.tmpdir" ) + File.separator + "testoxl.xml" );

		// create test graph
		
		ONDEXGraph graph = new MemoryONDEXGraph ( name );
		DataSource dataSource = graph.getMetaData ().getFactory ().createDataSource ( "cv" );
		EvidenceType eviType = graph.getMetaData().getFactory ().createEvidenceType("IMPD");
		
		
		ConceptClass ccGene = graph.getMetaData ().getFactory ().createConceptClass ( "Gene" );
		ONDEXConcept conceptPBS2 = graph.getFactory ().createConcept ( "1868", dataSource, ccGene, eviType );
		conceptPBS2.createConceptName("erg6", false);
		ONDEXConcept conceptZtPBS2 = graph.getFactory ().createConcept ( "1868", dataSource, ccGene, eviType );
		conceptZtPBS2.createConceptName("ZtERG6", true);
		ONDEXConcept conceptWis1 = graph.getFactory ().createConcept ( "1868", dataSource, ccGene, eviType );
		conceptWis1.createConceptName("ERG6", true);
		
		ConceptClass ccPub = graph.getMetaData ().getFactory ().createConceptClass ( "Publication" );
		ONDEXConcept conceptPub = graph.getFactory ().createConcept ( "11204778", dataSource, ccPub, eviType );
		conceptPub.createConceptName("PMID:11204778", true);
		
		AttributeName abs = graph.getMetaData ().getFactory ().createAttributeName ( "Abstract", String.class );
		String valueAbs = "A Mycosphaerella graminicola strain transformed with the green "
				+ "fluorescent protein (GFP) downstream of either a carbon source-repressed "
				+ "promoter or a constitutive promoter was used to investigate in situ carbohydrate "
				+ "uptake during penetration of the fungus in wheat leaves. The promoter region of the acu-3 gene from "
				+ "Neurospora crassa encoding isocitrate lyase was used as a carbon source-repressed promoter";
		conceptPub.createAttribute ( abs, valueAbs, true );
		
		AttributeName absHead = graph.getMetaData ().getFactory ().createAttributeName ( "AbstractHeader", String.class );
		String valueAbsHead ="Exploring infection of wheat and carbohydrate metabolism "
				+ "in Mycosphaerella graminicola transformants "
				+ "with differentially regulated green fluorescent protein expression.";
		conceptPub.createAttribute ( absHead, valueAbsHead, true );
		
		// What is this? AFAIK, the plug-in is supposed to create these relations 
		// 
		AttributeName bitScore = graph.getMetaData ().getFactory ().createAttributeName ( "BITSCORE", Double.class );
		Double valueBitScore = 4.4;
		conceptPub.createAttribute ( bitScore, valueBitScore, true );
		
		AttributeName tfidf = graph.getMetaData ().getFactory ().createAttributeName ( "TFIDF", Double.class );
		Double valueTfidf = 4.4;
		conceptPub.createAttribute ( tfidf, valueTfidf, true );
		
		AttributeName ipTfidf = graph.getMetaData ().getFactory ().createAttributeName ( "IP_TFIDF", Double.class );
		Double valueIpTfidf = 4.4;
		conceptPub.createAttribute ( ipTfidf, valueIpTfidf, true );
		
		AttributeName ipMaxTfidf = graph.getMetaData ().getFactory ().createAttributeName ( "MAX_TFIDF", Double.class );
		Double valueIpMaxTfidf = 4.4;
		conceptPub.createAttribute ( ipMaxTfidf, valueIpMaxTfidf, true );
		
		AttributeName blev = graph.getMetaData ().getFactory ().createAttributeName ( "BLEV", Double.class );
		Double valueBlev = 4.4;
		conceptPub.createAttribute ( blev, valueBlev, true );
		
		AttributeName matchScore = graph.getMetaData ().getFactory ().createAttributeName ( "GOMATCHSCORE", Double.class );
		Double valueMatchScore = 4.4;
		conceptPub.createAttribute ( matchScore, valueMatchScore, true );
		
		AttributeName evidence = graph.getMetaData ().getFactory ().createAttributeName ( "EVIDENCE", Set.class );
		Set<Integer> evi = new HashSet<Integer>();
		evi.add(5);
		conceptPub.createAttribute ( evidence, evi, true );
		
		AttributeName absFullText = graph.getMetaData ().getFactory ().createAttributeName ( "FullText", String.class );
		String valueFullText ="Full text of a Publication";
		conceptPub.createAttribute ( absFullText, valueFullText, true );
		
		// Why are you saving the file? Are you loading it back? Why? 
		// You can use this graph with the returned 'graph' object
		//
		Export.exportOXL ( graph, testfile.getAbsolutePath (), false, true );
		
		/* See the facility above to do the same quicker. runPlugin is another option

		Export export = new Export ();
		ONDEXPluginArguments ea = new ONDEXPluginArguments ( export.getArgumentDefinitions () );
		try {
			ea.setOption ( FileArgumentDefinition.EXPORT_FILE, testfile.getAbsolutePath () );
			ea.setOption ( ArgumentNames.EXPORT_AS_ZIP_FILE, false );
			export.setONDEXGraph ( graph );
			export.setArguments ( ea );
			export.start ();
		} catch ( Exception e ) {
			ExceptionUtils.throwEx ( UncheckedIOException.class, e,
					"Error while creating gene ", e.getMessage () );
		} */
		return graph;
	}
}
	
