package net.sourceforge.ondex.core.test;

import static org.junit.Assert.assertEquals;
import java.util.Set;
import org.junit.Test;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.GraphLabelsUtils;
import net.sourceforge.ondex.core.util.ONDEXGraphUtils;

/**
 * Tests for utilities in {@link GraphLabelsUtils}.
 * 
 * This follows the abstrac/specific approach that is described in {@link TestGraphProvider}
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 Dec 2021</dd></dl>
 *
 */
public abstract class AbstractGraphLabelsUtilsTest
{
	
	private ONDEXGraph graph = TestGraphProvider.getInstance ().createGraph ( "test" );
	
	private ConceptClass ccA = ONDEXGraphUtils.getOrCreateConceptClass ( graph, "A" );
	private DataSource srcA = ONDEXGraphUtils.getOrCreateDataSource ( graph, "srcA" );
	private EvidenceType evA = ONDEXGraphUtils.getOrCreateEvidenceType ( graph, "evA" );
	private DataSource srcENSEMBL = ONDEXGraphUtils.getOrCreateDataSource ( graph, "ENSEMBL" );	
	
	private ONDEXConcept c = graph.createConcept ( "foo", "", "", srcA, ccA, Set.of ( evA ) );
	
	
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
	
	/**
	 * Tests that accessions can be filtered from best name selection, see
	 * <a href = "https://github.com/Rothamsted/knetminer/issues/602#issuecomment-1086962980">here</a>
	 * 
	 */
	@Test
	public void testBestNameAccessionFiltering ()
	{
		var acc = "TRAESCS3D02G468400";
		var name = "MYB1";
		
		c.createConceptName ( acc, true );
		c.createConceptName ( name, false );
		
		c.createConceptAccession ( acc, srcENSEMBL, false );
		
		assertEquals ( "Accession filtering didn't work!", name, GraphLabelsUtils.getBestName ( c, true ) );
		assertEquals ( "Accession filtering in place when disabled too!", acc, GraphLabelsUtils.getBestName ( c ) );
	}
	
	@Test
	public void testBestNameAccessionFilteringFallback ()
	{
		var acc = "TRAESCS3D02G468400";
		
		c.createConceptName ( acc, true );
		c.createConceptAccession ( acc, srcENSEMBL, false );
		
		assertEquals ( "Accession filtering didn't work (fallback case)!", acc, GraphLabelsUtils.getBestName ( c, true ) );
	}
	
}
