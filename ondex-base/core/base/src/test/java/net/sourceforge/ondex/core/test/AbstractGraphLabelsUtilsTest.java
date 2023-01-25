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
	private ConceptClass ccGene = ONDEXGraphUtils.getOrCreateConceptClass ( graph, "Gene" );
	private DataSource srcA = ONDEXGraphUtils.getOrCreateDataSource ( graph, "srcA" );
	private EvidenceType evA = ONDEXGraphUtils.getOrCreateEvidenceType ( graph, "evA" );
	private DataSource srcENSEMBL = ONDEXGraphUtils.getOrCreateDataSource ( graph, "ENSEMBL" );	
	
	private ONDEXConcept c = graph.createConcept ( "foo", "", "", srcA, ccA, Set.of ( evA ) );
	private ONDEXConcept geneConcept = graph.createConcept ( "foo gene", "", "", srcA, ccGene, Set.of ( evA ) );
	
	
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
	
	@Test
	public void testSpeciePrefixGeneNames(){
		geneConcept.createConceptName ( "ABC", false );
		geneConcept.createConceptName ( "FoABC", false );
		geneConcept.createConceptName ( "AB", false ); 
		
		assertEquals ( 
			"Wrong label picked!", "FoABC", GraphLabelsUtils.getBestConceptLabelWithGeneSpeciePrefix ( geneConcept ) 
		);
	}
	
	/**
	 * prefixed names take priority over preferredName flag
	 */
	@Test
	public void testSpeciePrefixGeneNamesWithPreferred(){
		geneConcept.createConceptName ( "ABC", true );
		geneConcept.createConceptName ( "FoABC", false );
		geneConcept.createConceptName ( "AB", false ); 
		
		assertEquals ( 
			"Wrong label picked!", "FoABC", GraphLabelsUtils.getBestConceptLabelWithGeneSpeciePrefix ( geneConcept )
		);
	}
	
	@Test
	public void testSpeciePrefixGeneNamesWrongPattern(){
		geneConcept.createConceptName ( "ABC", false );
		geneConcept.createConceptName ( "foAbc", false );
		geneConcept.createConceptName ( "AB",  false ); 
		
		assertEquals ( "Wrong label picked!", "AB", GraphLabelsUtils.getBestConceptLabelWithGeneSpeciePrefix ( geneConcept ) );
	}
	
	/**
	 * Tricks with TaXXX and isPreferred, see the code
	 */
	@Test
	public void testSpeciePrefixGeneNamesRealCase()
	{
		geneConcept.createConceptName ( "TaE12A11", true );
		geneConcept.createConceptName ( "E12A11", false );
		geneConcept.createConceptName ( "TaMFT", true );
		geneConcept.createConceptName ( "MFT", true );
		geneConcept.createConceptName ( "MFT2", false ); 
		
		assertEquals ( 
			"Wrong label picked (with gene specie prefix)!",
			"TaMFT", 
			GraphLabelsUtils.getBestConceptLabelWithGeneSpeciePrefix ( geneConcept )
		);
		
		// You get MFT here only if it's isPreferred too. Maybe, in a case like this you 
		// don't want to mark TaXXX as preferred, since getBestConceptLabelWithGeneSpeciePrefix() 
		// (ie, the label for the network view) gives them priority independently on isPreferred,
		// while you don't want TaXXX to win over shorter names in getBestConceptLabel()
		// (ie, the gene view).
		//
		assertEquals ( 
			"Wrong label picked (without gene specie prefix)!",
			"MFT", 
			GraphLabelsUtils.getBestConceptLabel ( geneConcept )
		);
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
