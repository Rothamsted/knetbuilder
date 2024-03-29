package net.sourceforge.ondex.mapping.tmbased;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.ONDEXGraphUtils;
import net.sourceforge.ondex.mapping.tmbased.args.ArgumentNames;
import net.sourceforge.ondex.parser.oxl.Parser;
import net.sourceforge.ondex.utils.OndexPluginUtils;

/**
 * A few tests for the {@link Mapping text mapping plug-in}
 *
 * @author jojicunnunni, Brandizi
 * 
 * <dl><dt>Date:</dt><dd>22 June 2022</dd></dl>
 *
 */
public class TextMappingTest
{
	private ONDEXGraph graph;
	
	private static final String BUILD_PATH = 
		Path.of ( System.getProperty ( "maven.buildDirectory", "target" ) ).toString ();

	private static final String TEST_DATA_PATH = BUILD_PATH + "/test-classes/text-mapping";
	
	
	@Before
	public void initGraph () throws IOException
	{
		graph = Parser.loadOXL ( TEST_DATA_PATH + "/textmining-sample.oxl" );
		OndexPluginUtils.getLuceneManager ( graph, BUILD_PATH + "/tm-test-index", true );
	}
	
	@Test
	public void testBasics ()
	{
		RelationType occInRelType = ONDEXGraphUtils.getRelationType ( graph, "occ_in" );
		var initialRelCnt = graph.getRelationsOfRelationType ( occInRelType ).size ();
		
 		Map<String, Object> pluginArgs = Map.of (
			ArgumentNames.CONCEPTCLASS_ARG, "Gene", 
			ArgumentNames.PREFERRED_NAMES_ARG, true 
		);
		OndexPluginUtils.runPlugin ( Mapping.class, graph, pluginArgs);
		
		Set<ONDEXRelation> textMineRels = graph.getRelationsOfRelationType ( occInRelType );
		var postRelCnt = textMineRels.size();
		assertTrue ( "No occ_in relations created!", postRelCnt > initialRelCnt );
		
		AttributeName evidenceAttrName = ONDEXGraphUtils.getAttributeName ( graph, "EVIDENCE" );
		AttributeName tfidfAttrName = ONDEXGraphUtils.getAttributeName ( graph, "TFIDF" );

		// Check it created these attributes for all of them
		Stream.of ( evidenceAttrName, tfidfAttrName )
		.forEach ( attrType -> 
		{
			textMineRels.stream ()
			.forEach ( r -> assertNotNull ( 
				String.format ( 
					"Text mining relation %s has no %s attribute!",
					ONDEXGraphUtils.getString ( r ),
					attrType.getId ()
				), 
				r.getAttribute ( evidenceAttrName ) 
			));
		});
	}
	
	@Test
	public void testCreatedLinks () throws IOException
	{
		RelationType occInRelType = ONDEXGraphUtils.getRelationType ( graph, "occ_in" );
		var initialRelCnt = graph.getRelationsOfRelationType ( occInRelType ).size ();

		Map<String, Object> pluginArgsSW = Map.of (
			ArgumentNames.CONCEPTCLASS_ARG, "Gene", 
			ArgumentNames.PREFERRED_NAMES_ARG, true 
		);
		OndexPluginUtils.runPlugin ( Mapping.class, graph ,pluginArgsSW );
		
		Set<ONDEXRelation> textMineRels = graph.getRelationsOfRelationType ( occInRelType );
		var postRelCnt = textMineRels.size();
		assertTrue ( "Stop words filtering failed!",  postRelCnt > initialRelCnt );
		
		
		Stream.of ( "26074495", "34234765" )
		.forEach ( testPMID -> 
		{
			var found = textMineRels.stream ()
				.anyMatch ( r -> testPMID.equals ( r.getToConcept ().getPID () ) );
			
			assertTrue ( String.format ( "Test PubMed ID %s not found in text mining relations!", testPMID ), found );
		});
	}
	
	/**
	 * Checking the Stop word "ZtSEC4" with and without stop word argument.
	 */
	@Test
	public void testStopWords () throws IOException
	{
		// Annotate without stop words
		RelationType occInRelType = ONDEXGraphUtils.getRelationType ( graph, "occ_in" );
		Map<String, Object> pluginArgs = Map.of (
			ArgumentNames.CONCEPTCLASS_ARG, "Gene", 
			ArgumentNames.PREFERRED_NAMES_ARG, true 
		);
		OndexPluginUtils.runPlugin ( Mapping.class, graph, pluginArgs );
		
		boolean foundRelationWOSW = graph.getRelationsOfRelationType ( occInRelType )
			.stream ()
			.anyMatch ( rel -> rel.getFromConcept ()
				.getConceptName ()
				.getName ()
				.equalsIgnoreCase ( "ZtSEC4" )
			);
		
		assertTrue ( "Testing 'ZtSEC4' name not annotated with stop words disabled!", foundRelationWOSW );
		
		
		// And now check that the stop-words feature works
		//

		initGraph(); // Reset to the test graph
				
		
		Map<String, Object> pluginArgsSW = Map.of (
				ArgumentNames.STOP_WORDS_ARG, TEST_DATA_PATH + "/stop-words.txt" ,
				ArgumentNames.CONCEPTCLASS_ARG, "Gene", 
				ArgumentNames.PREFERRED_NAMES_ARG, true 
			);
		OndexPluginUtils.runPlugin ( Mapping.class, graph, pluginArgsSW );
		
		
		boolean foundRelationsWSW = graph.getRelationsOfRelationType ( occInRelType )
			.stream ()
			.anyMatch ( rel -> rel.getFromConcept ()
				.getConceptName ()
				.getName ()
				.equalsIgnoreCase ( "ZtSEC4" )
			);
		
		assertFalse ( "Stop word 'ZtSEC4' was annotated anyway!", foundRelationsWSW );	
	}
}
	
