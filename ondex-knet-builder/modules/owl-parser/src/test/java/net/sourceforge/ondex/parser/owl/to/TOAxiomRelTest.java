package net.sourceforge.ondex.parser.owl.to;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.jena.ontology.OntModel;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.io.Resources;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.parser.owl.OWLMapper;

/**
 * Tests for The Trait Ontology
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 May 2017</dd></dl>
 *
 */
public class TOAxiomRelTest
{
	@Test
	public void testOwlAxiomRelations () throws Exception
	{
		try ( ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext ( "to_cfg.xml" ) )
		{
			OntModel model = (OntModel) ctx.getBean ( "jenaOntModel" );
			model.setStrictMode ( false );
			model.read ( Resources.getResource ( "to_owl_axiom.owl" ).toString (), "", "RDF/XML" );
			
			OWLMapper mapper = (OWLMapper) ctx.getBean ( "owlMapper" );
			ONDEXGraph graph = mapper.map2Graph ( model );
					
			ONDEXConcept c = graph.getConcepts ()
			.stream ()
			.filter ( ci -> "TO_0000523".equals ( ci.getPID () ) )
			.findAny ()
			.orElseThrow ( () -> new RuntimeException ( "Test Concept not found!" ) );
	
			DataSource ds = graph.getMetaData ().createDataSource ( "TO", "Plant Trait Ontology", "" );
			
			assertNotNull ( "Concept accession is wrong!", c.getConceptAccession ( "0000523", ds ) );
			assertNotNull ( "Concept label is wrong!", c.getConceptName ( "stomatal resistance" ) );
			Assert.assertTrue ( 
				"Concept definition is wrong!", 
				c.getDescription ().startsWith ( "Stomatal resistance (or its inverse" ) 
			);
	
			ConceptClass cc = c.getOfType ();
			// These are for the version where the TO root class is mapped to a CC, without using a custom constant
	//		assertEquals ( "Concept Class ID is wrong!", "TO_0000387", cc.getId () );
	//		assertEquals ( "Concept Class label is wrong!",  "plant trait", cc.getFullname () );
	//		Assert.assertTrue ( 
	//			"Concept Class definition is wrong!", 
	//			cc.getDescription ().startsWith ( "A measurable or observable" ) 
	//		);
			assertEquals ( "Concept Class ID is wrong!", "Trait", cc.getId () );
			assertEquals ( "Concept Class label is wrong!",  "Trait", cc.getFullname () );
			Assert.assertTrue ( 
				"Concept Class definition is wrong!", 
				cc.getDescription ().startsWith ( "Term from the Trait Ontology (https://github.com/Planteome/plant-trait-ontology)" ) 
			);
	
			
			ds = graph.getMetaData ().createDataSource ( "Wikipedia", "Wikipedia", "" );
			assertNotNull ( "Additional x-ref to Wikipedia is wrong!", c.getConceptAccession ( "Stomatal_conductance", ds ) );
							
			// Inferred super-classes
			ONDEXRelation relation = graph
			.getRelations ()
			.stream ()
			.filter ( r -> "is_a".equals ( r.getOfType ().getId () ) )
			.filter ( r -> "TO_0000839".equals ( r.getToConcept ().getPID () ) )
			.findFirst ()
			.orElse ( null );
			
			assertNotNull ( "subClass inferred relation not found!", relation );
	
			
			relation = graph
			.getRelations ()
			.stream ()
			.filter ( r -> "part_of".equals ( r.getOfType ().getId () ) )
			.filter ( r -> "PO_0009072".equals ( r.getToConcept ().getPID () ) )
			.findFirst ()
			.orElse ( null );
			
			assertNotNull ( "subClass inferred relation not found!", relation );
		}	
	}
}
