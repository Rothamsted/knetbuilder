package net.sourceforge.ondex.parser.medline2.xml;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.ondex.config.LuceneRegistry;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
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

	private static final String TEST_DATA_PATH = BUILD_PATH + "/test-classes/text-mapping";

	private static final String LUCENE_PATH = BUILD_PATH + "tm-test-index";
	
	private int TOTAL_RELATIONS = 33;
	
	@Before
	public void initGraph () throws IOException
	{
		graph = Parser.loadOXL ( TEST_DATA_PATH + "/textmining-sample.oxl" );
		loadLuceneEnv ( graph );
	}
	
	@Test
	public void testBasics ()
	{
		var sizeBeforeTextMining = graph.getRelations().size();
		
 		Map<String, Object> pluginArgs = Map.of (
			ArgumentNames.CONCEPTCLASS_ARG, "Gene", 
			ArgumentNames.PREFERRED_NAMES_ARG, "true" 
		);
		OndexPluginUtils.runPlugin ( Mapping.class, graph ,pluginArgs);
		
		var totalRelationsCreated = graph.getRelations().size();
		Set<ONDEXRelation> relations = relationsCreated ( graph,"TFIDF" );
		
		Assert.assertTrue ( "Mapping concepts to publications failed ", totalRelationsCreated > sizeBeforeTextMining );
		Assert.assertTrue ( "TFIDF Relationships not created", relations.size () > 0 );
		Assert.assertTrue ( "Evidence Relationships not created", relationsCreated ( graph,"EVIDENCE" ).size () > 0 );
	}
	
	@Test
	public void testStopWords () throws IOException
	{
		Map<String, Object> pluginArgsSW = Map.of (
			ArgumentNames.STOP_WORDS_ARG, TEST_DATA_PATH + "/stop-words.txt" ,
			ArgumentNames.CONCEPTCLASS_ARG, "Gene", 
			ArgumentNames.PREFERRED_NAMES_ARG, "true" 
		);
		OndexPluginUtils.runPlugin ( Mapping.class, graph ,pluginArgsSW );
		int sizeAfterTextMining = graph.getRelations ().size ();
		
		Assert.assertTrue ( "Stop words filtering failed", TOTAL_RELATIONS > sizeAfterTextMining );
		
		Set<ONDEXRelation> relations = relationsCreated ( graph,"TFIDF" );
		Optional<ONDEXRelation> conceptExists1 = relations.stream ()
				.filter ( a -> a.getToConcept ().getPID ().equalsIgnoreCase ( "26074495" ) ).findAny ();
		Optional<ONDEXRelation> conceptExists2 = relations.stream ()
				.filter ( a -> a.getToConcept ().getPID ().equalsIgnoreCase ( "34234765" ) ).findAny ();
		
		Assert.assertTrue ( "Relationships not created", relations.size () > 0 );
		Assert.assertTrue ( "26074495", conceptExists1.isPresent () );
		Assert.assertTrue ( "34234765", conceptExists2.isPresent () );
		
		Assert.assertTrue ( "Evidence Relationships not created", relationsCreated ( graph,"EVIDENCE" ).size () > 0 );
	}
	
	private static LuceneEnv loadLuceneEnv ( ONDEXGraph graph ) throws IOException {
		
		DirUtils.deleteTree ( LUCENE_PATH );
		LuceneEnv lenv = new LuceneEnv ( LUCENE_PATH, true );
		lenv.setONDEXGraph ( graph );
		LuceneRegistry.sid2luceneEnv.put ( graph.getSID (), lenv );
		return lenv;
	}
	
	private static Set<ONDEXRelation> relationsCreated ( ONDEXGraph graph,String type ) {
		
		Optional<Set<ONDEXRelation>> relations = graph.getMetaData ().getAttributeNames ().stream ()
				.filter ( attribute -> attribute.getId () == type )
				.map ( attribute -> graph.getRelationsOfAttributeName ( attribute ) )
				.findFirst ();
		return  relations.get ();
	}
}
	
