package net.sourceforge.ondex.core.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.GraphLabelsUtils;
import net.sourceforge.ondex.core.util.ONDEXGraphUtils;

/**
 * TODO: comment me!
 *
 * @author jojicunnunni
 * <dl><dt>Date:</dt><dd>7 April 2022</dd></dl>
 *
 */
public abstract class AbstractGraphLabelsUtilsTest
{
	
	private ONDEXGraph graph; 	
	
	private ConceptClass ccA;
	
	private DataSource srcA;
	
	private EvidenceType evA;
	
	private ONDEXConcept c;
	
	@Before
	public void init () throws IOException
	{
		graph = TestGraphProvider.getInstance ().createGraph ( "test" );
		
		ccA = ONDEXGraphUtils.getOrCreateConceptClass ( graph, "A" );
		srcA = ONDEXGraphUtils.getOrCreateDataSource ( graph, "srcA" );
		evA = ONDEXGraphUtils.getOrCreateEvidenceType ( graph, "evA" );
		c = graph.createConcept ( "foo", "", "", srcA, ccA, Set.of ( evA ) );
	}
	
	/**
	 * Tests Rothamsted/knetminer#584
	 */
	@Test
	public void testBestNameMixedTypes ()
	{
		c.createConceptName ( "ABC-transporter", true );
		c.createConceptName ( "CINC_01G003904", false );
		
		assertEquals ( "Wrong name picked!", "ABC-transporter", GraphLabelsUtils.getBestName ( c ) );
	}


	/**
	 * Tests Rothamsted/knetminer#584
	 */
	@Test
	public void testBestAccessionMixedTypes ()
	{
		c.createConceptAccession ( "ABC Gene", srcA, false ); // unique
		c.createConceptAccession ( "ABC", srcA, true ); // shorter, but should choose the non-ambiguous anyway
		
		assertEquals ( "Wrong accession picked!", "ABC Gene", GraphLabelsUtils.getBestAccession ( c ) );
	}

	/**
	 * Tests Rothamsted/knetminer#584
	 */
	@Test
	public void testBestGeneAccessionPrioritySources ()
	{
		DataSource srcENSEMBL = ONDEXGraphUtils.getOrCreateDataSource ( graph, "ENSEMBL" );
		
		
		c.createConceptAccession ( "ABC Gene", srcENSEMBL, false ); // unique
		c.createConceptAccession ( "ABC", srcA, false ); // shorter but ENSEMBL should have priority.
		
		assertEquals ( "Wrong accession picked!", "ABC Gene", GraphLabelsUtils.getBestGeneAccession ( c ) );
	}

	/**
	 * Tests Rothamsted/knetminer#593
	 */
	@Test
	public void testZMSynonyms ()
	{
		DataSource srcENSEMBL = ONDEXGraphUtils.getOrCreateDataSource ( graph, "ENSEMBL" );
		
		c.createConceptAccession ( "ZM00001EB425260", srcENSEMBL, false ); // unique
		c.createConceptAccession ( "ZM00001D025723", srcENSEMBL, false ); // shorter but EB should have priority.
		
		assertEquals ( "Wrong accession picked!", "ZM00001EB425260", GraphLabelsUtils.getBestAccession ( c ) );
	}
	
	/**
	 * Tests Rothamsted/knetminer#584
	 */
	@Test
	public void testBestLabel ()
	{
		c.createConceptName ( "ABC-transporter", true );
		c.createConceptName ( "CINC_01G003904", false );
		c.createConceptAccession ( "ABC Gene", srcA, false ); // unique
		c.createConceptAccession ( "ABC", srcA, true ); // shorter, but should choose the non-ambiguous anyway
		
		assertEquals ( "Wrong label picked!", "ABC-transporter", GraphLabelsUtils.getBestConceptLabel ( c ) );
	}	

	/**
	 * Tests Rothamsted/knetminer#584
	 */
	@Test
	public void testBestLabelAccessionFallBack ()
	{
		c.createConceptAccession ( "ABC Gene", srcA, false ); // unique
		c.createConceptAccession ( "ABC", srcA, true ); // shorter, but should choose the non-ambiguous anyway
		
		assertEquals ( "Wrong label picked!", "ABC Gene", GraphLabelsUtils.getBestConceptLabel ( c ) );
	}	

	@Test
	public void testBestLabelPIDFallBack ()
	{
		assertEquals ( "Wrong label picked!", c.getPID (), GraphLabelsUtils.getBestConceptLabel ( c ) );
	}	

}
