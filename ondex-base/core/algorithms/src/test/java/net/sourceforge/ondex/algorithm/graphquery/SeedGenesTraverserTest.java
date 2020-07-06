package net.sourceforge.ondex.algorithm.graphquery;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.junit.Test;

import net.sourceforge.ondex.algorithm.graphquery.flatfile.StateMachineFlatFileParser2;
import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;

/**
 * Tests {@link AbstractGraphTraverser#traverseGraphFromIds(net.sourceforge.ondex.core.ONDEXGraph, java.util.Set, FilterPaths) traverseGraphFromIds()}
 * and its variants.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>8 Apr 2020</dd></dl>
 *
 */
public class SeedGenesTraverserTest
{
	private static ONDEXGraph graph;
	private static ConceptClass genecc, proteincc;
	private static RelationType encrel;
	private static DataSource ds1, ds2, dsgen;
	private static EvidenceType evgen;
	private static Set<EvidenceType> evgenColl;
	private static GraphTraverser traverser;
	
	private static final String TEST_GENE_LIST = 
		"gene1\tSRC2\n" +
		"gene4\tSRC1\n" +
		"gene6\n" + 
		"gene7\tSRC1\n" + 
		"gene20304";

	
	@BeforeClass
	public static void init () throws Exception
	{
		graph = new MemoryONDEXGraph ( "test" );
		ONDEXGraphMetaData meta = graph.getMetaData ();
		genecc = meta.createConceptClass ( "Gene", "The Gene", "", null );
		proteincc = meta.createConceptClass ( "Protein", "The Protein", "", null );
		encrel = meta.createRelationType ( "enodes", "encodes", "", "", false, false, false, false, null );
		ds1 = meta.createDataSource ( "SRC1", "Test Source 1", "" );
		ds2 = meta.createDataSource ( "SRC2", "Test Source 2", "" );
		dsgen = meta.createDataSource ( "GENERICSRC", "", "" );
		evgen = meta.createEvidenceType ( "GENERICEV", "", "" );
		evgenColl = Collections.singleton ( evgen );
		
		IntStream.range ( 0, 10 )
		.forEach ( i -> {
			String gid = "gene" + i;
			ONDEXConcept g = graph.createConcept ( gid, "", "", dsgen, genecc, evgenColl );
			g.createConceptAccession ( gid, i % 2 == 0 ? ds1 : ds2, false );
			ONDEXConcept p = graph.createConcept ( "protein" + i, "", "", dsgen, proteincc, evgenColl );
			graph.createRelation ( g, p, encrel, evgenColl );
		});
	
		StateMachineFlatFileParser2 smparser = new StateMachineFlatFileParser2 ();
		smparser.parseString (
			"#Finite States *=start state ^=end state\n" + 
			format ( "1*\t%s\n", genecc.getId () ) + 
			format ( "2^\t%s\n", proteincc.getId () ) +
			"\n" +
			"#Transitions\n" + 
			format ( "1-2\t%s\n", encrel.getId () ), 
			graph );
		
		traverser = new GraphTraverser ( smparser.getStateMachine () );
	}
	
	@SuppressWarnings ( "rawtypes" )
	private void testBasics ( Set<ONDEXConcept> seeds )
	{
		Map<ONDEXConcept, List<EvidencePathNode>> paths = traverser.traverseGraph ( graph, seeds, null );
		assertEquals ( "Wrong no. of returned paths!", 3, paths.size () );
		
		Predicate<String> geneFinder = 
			gspec -> paths.keySet ()
				.stream ()
				.map ( gene -> gene.getPID () + ":" + gene.getConceptAccessions ().iterator ().next ().getElementOf ().getId () )
				.anyMatch ( gspec::equals );

		Stream.of ( "gene1:SRC2", "gene4:SRC1", "gene6:SRC1" )
		.forEach ( gspec -> assertTrue ( "Gene " + gspec + " not found!", geneFinder.test ( gspec ) ));

		Stream
		.of ( "gene7:SRC1", "gene20304:SRC1" )
		.forEach ( gspec -> assertFalse ( "Gene " + gspec + " shouldn't be here!!", geneFinder.test ( gspec ) ));
	}

	
	@Test
	public void testStringReader ()
	{
		testBasics ( AbstractGraphTraverser.ids2Genes ( graph, new StringReader ( TEST_GENE_LIST ) ) );
	}

	@Test
	public void testStringFile () throws IOException
	{
		String geneListPath = "target/test-gene-list.tsv";
		try ( FileWriter fw = new FileWriter ( geneListPath ) )
		{
			fw.write ( TEST_GENE_LIST );
		}
		testBasics ( AbstractGraphTraverser.ids2Genes ( graph, geneListPath ) );
	}
}
