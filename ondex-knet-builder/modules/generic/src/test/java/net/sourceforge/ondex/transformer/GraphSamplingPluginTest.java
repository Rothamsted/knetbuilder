package net.sourceforge.ondex.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.Test;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;

/**
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>8 Mar 2019</dd></dl>
 *
 */
public class GraphSamplingPluginTest
{
	private ONDEXGraph graph = new MemoryONDEXGraph ( "testGraph" );
	
	/**
	 * Generates a test graph of this size
	 */
	private int size = 1000;
	
	/** it's an average, they're decided randomly */
	private int relations2ConceptSizeRatio = 5;
	
	private void createTestGraph () {
		createTestGraph ( null );
	}

	/**
	 * Generates a test graph, having relations biased around 'average node'. 
	 * 
	 * <b>TL/DR</b>: 0.25 means the generated relations are (quite) uniformly distributed over all the nodes. Low values
	 * Lower values {@code biasSigma} (must be >0) means relations are concentrated around a small set of nodes (too small
	 * values will cause this method to hang). 
	 * 
	 * <h3>Longer explanation</h3>
	 *  
	 * Nodes are indexed and the average node is the one having an index which is half-way between min/max 
	 * (of course it's fictitious).
	 * 
	 * Nodes to link are chosen randomly, using a normal distribution with average 0.5 and {@code biasSigma} as 
	 * standard deviation. If you look at the CDF for this, you'll see that 0.25 CDF(x) is similar enough to the identity
	 * function, while small sigmas makes CDF() more similar to a step function with discontinuity in 0.5.
	 *   
	 */
	private void createTestGraph ( Double biasSigma )
	{	
		if ( biasSigma == null ) biasSigma = 0.25;
				
		// Metadata
		ONDEXGraphMetaData meta = graph.getMetaData ();
		ConceptClass testCC = meta.createConceptClass ( "TestCC", "Test Class", "Foo Test Class", null );
		DataSource testDS = meta.createDataSource ( "testDS", "Test Data Source", "Foo Test DS" );
		Set<EvidenceType> testEvs = Collections.singleton ( 
			meta.createEvidenceType ( "testEv", "Test Data Evidence", "Foo Test Evidence" )
		);
		// Since there cannot be more than one relation type per concept pair, we need multiple types.
		IntStream.range ( 0, relations2ConceptSizeRatio )
		.forEach ( i -> 
			meta.createRelationType ( 
				"testRelType" + i, "Test Relation Type" + i, "Foo Test Relation Type" + i, "", 
				false, false, false, false, null 
			)		
		);
		
		// Concepts
		IntStream.range ( 0,  size ).forEach ( i ->
			graph.createConcept ( "testConcept" + i, "", "A foo concept #" + i, testDS, testCC, testEvs )
		);
		
		Set<ONDEXConcept> concepts = graph.getConcepts ();
		final int nconcepts = concepts.size ();
		List<ONDEXConcept> conceptsList = concepts.parallelStream ().collect ( Collectors.toList () );

		// Relations
		NormalDistribution normDist = new NormalDistribution ( 0.5, biasSigma );
		Supplier<Integer> conceptIdxGenerator = () -> 
		{ 
			double rndVal = normDist.sample ();
			//System.out.format ( "GAUSS: %s\n", rndVal );
			rndVal = Math.max ( 0, rndVal );
			rndVal = Math.min ( rndVal, 1 );
			return (int) Math.round ( rndVal * (nconcepts - 1) );
		};
		
		IntStream.range ( 0,  nconcepts * relations2ConceptSizeRatio ).forEach ( i -> 
		{
			// Pick random concepts and relation type until you get some comibination that doesn't exist yet
			ONDEXConcept c1,c2;
			RelationType rt;
			do {
				c1 = conceptsList.get ( conceptIdxGenerator.get () );
				c2 = conceptsList.get ( conceptIdxGenerator.get () );
				rt = meta.getRelationType ( "testRelType" + RandomUtils.nextInt ( 0, relations2ConceptSizeRatio ) ); 
				// out.format ( "%s - %s -> %s\n", c1, rt.getId (), c2 );
			}
			while ( graph.getRelation ( c1, c2, rt ) != null );
			graph.createRelation ( c1, c2, rt, testEvs );
		});
	}
	
	/**
	 * Yes, it's weird, a test for a test method...
	 */
	@Test
	public void testGraphGen () 
	{
		createTestGraph ();
		assertEquals ( "Wrong no. of concepts for generated graph!", size, this.graph.getConcepts ().size () );
		assertEquals ( 
			"Wrong no. of relations for generated graph!", 
			1d * relations2ConceptSizeRatio, 
			this.graph.getRelations ().size () / this.graph.getConcepts ().size (),
			1d * relations2ConceptSizeRatio * 0.1
		);
	}
	
	/**
	 * All tests below do the same thing: {@link #createTestGraph(Double) generate a graph with a given relation bias}, 
	 * cut to a given percentage, see if new sizes look fine.  
	 */
	private void testTemplate ( double relativeSize, Double biasSigma )
	{
		createTestGraph ( biasSigma );
		
		GraphSamplingPlugin sampler = new GraphSamplingPlugin ();
		sampler.sample ( this.graph, relativeSize );
		
		assertEquals ( "Unexpected new size for concepts!", relativeSize, 1.0 * graph.getConcepts ().size () / this.size, .1 );
		
		// It migth be much smaller, depending on the relations distribution over nodes.
		assertTrue ( 
			"Unexpected new size for relations!",
			1d * graph.getRelations ().size () / ( this.size * this.relations2ConceptSizeRatio ) <= relativeSize 
		);		
	}

	
	@Test
	public void testSimple ()
	{
		testTemplate ( 0.2, null );
	}


	@Test
	public void testBiasedGraph ()
	{
		// We cut just a few concepts in a very unbiased graph. This causes the concepts removal to not be enough (despite 
		// incident relations are taken away with nodes), an hence we test that the check on/removal of relations is working 
		//
		this.relations2ConceptSizeRatio = 2;
		testTemplate ( 0.97, 0.005 );
	}
}
