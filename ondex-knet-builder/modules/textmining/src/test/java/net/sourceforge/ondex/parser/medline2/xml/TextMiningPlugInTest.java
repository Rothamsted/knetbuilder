package net.sourceforge.ondex.parser.medline2.xml;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jakarta.xml.bind.JAXBException;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.config.LuceneRegistry;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.export.oxl.ArgumentNames;
import net.sourceforge.ondex.export.oxl.Export;
import net.sourceforge.ondex.mapping.tmbased.Mapping;
import net.sourceforge.ondex.parser.oxl.Parser;
import net.sourceforge.ondex.tools.DirUtils;
import net.sourceforge.ondex.utils.OndexPluginUtils;
import uk.ac.ebi.utils.exceptions.ExceptionUtils;
/**
 * The usual unit tests for {@link TextMiningPlugIn}
 *
 * @author jojicunnunni
 * 
 * <dl><dt>Date:</dt><dd>22 June 2022</dd></dl>
 *
 */
public class TextMiningPlugInTest
{
	private static ONDEXGraph graph;
	
	private static String testCasePath;
	
	 private static final String LUCENE_DIR = "target/index";
	
	@Before
	public void initGraph () throws IOException
	{
		var mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" );
		mavenBuildPath = Path.of ( mavenBuildPath ).toRealPath ().toString ();
		mavenBuildPath = mavenBuildPath.replace ( '\\', '/' );

		testCasePath = mavenBuildPath + "/test-classes/test-case";
		
		var outFile = new File ( testCasePath );
		if ( !outFile.exists () ) outFile.mkdir ();
		
		graph = Parser.loadOXL ( testCasePath + "/textmining-sample.oxl" );
		Assert.assertNotNull ( "graph not loaded!", graph );
		loadLuceneEnv(graph);
	}
	
	//@Test
	public void testBasics ()
	{
		
		int sizeBeforeTextMining = graph.getRelations ().size ();
		
		Map<String, Object> pluginArgs = Map.of ( 
				 "GeneNameStopWords", testCasePath + "/geneNameStopWords.txt" ,
				 "ConceptClass", "Gene", 
				 "Search", "exact",
				 "OnlyPreferredNames", "true",
				 "UseFullText", "false" );
		 OndexPluginUtils.runPlugin ( Mapping.class, graph ,pluginArgs);
		 
		 int sizeAfterTextMining = graph.getRelations().size();
		 
		 Assert.assertTrue( "Mapping concepts to publications failed ", sizeBeforeTextMining == sizeAfterTextMining);
	}
	
	@Test
	public void testBasics2 ()
	{
		ONDEXGraph testGraph = createTestGraph ( "test" );
		
		int sizeBeforeTextMining = testGraph.getRelations ().size ();
		try {
			loadLuceneEnv ( testGraph );
		} catch ( IOException e ) {
			e.printStackTrace ();
		}
		Map<String, Object> pluginArgs = Map.of ( 
				 "GeneNameStopWords", testCasePath + "/geneNameStopWords.txt" ,
				 "ConceptClass", "Gene", 
				 "Search", "exact",
				 "OnlyPreferredNames", "true",
				 "UseFullText", "false" );
		OndexPluginUtils.runPlugin ( Mapping.class, testGraph ,pluginArgs );
		
		int sizeAfterTextMining = testGraph.getRelations ().size ();
		Assert.assertTrue ( "Mapping concepts to publications failed ", sizeBeforeTextMining == sizeAfterTextMining );
	}
	
	public static LuceneEnv loadLuceneEnv ( ONDEXGraph graph ) throws IOException {
		
		DirUtils.deleteTree ( LUCENE_DIR );
		LuceneEnv lenv = new LuceneEnv ( LUCENE_DIR, true );
		lenv.setONDEXGraph ( graph );
		LuceneRegistry.sid2luceneEnv.put ( graph.getSID (), lenv );
		return lenv;
	}
	
	public static ONDEXGraph createTestGraph ( String name ) {
		
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
		} 
		return graph;
	}
}
	
