package net.sourceforge.ondex.parser.owl.efo;

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
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 Jun 2019</dd></dl>
 *
 */
public class EfoTest
{
	@Test
	public void testBasics () throws Exception
	{
		try ( ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext ( "efo_cfg.xml" ) )
		{
			OntModel model = (OntModel) ctx.getBean ( "jenaOntModel" );
			model.read ( Resources.getResource ( "efo-test.owl" ).toString (), "", "RDF/XML" );

			OWLMapper owlMap = (OWLMapper) ctx.getBean ( "owlMapper" );
			ONDEXGraph graph = owlMap.map2Graph ( model );
			
			assertEquals ( "Wrong no of concepts!", 4, graph.getConcepts ().size () );

			DataSource efoDs = graph.getMetaData ().getDataSource ( "EFO" );
			assertNotNull ( "EFO data source not found!", efoDs );

			ONDEXConcept c = graph
				.getConcepts ()
				.stream ()
				.filter ( ci -> ci.getConceptAccession ( "EFO:0002888", efoDs ) != null )
				.findAny ()
				.orElse ( null );
			
			assertNotNull ( "Test concept not found!", c );
			assertNotNull ( "Concept label is wrong!", c.getConceptName ( "Homo Sapiens Cell Line" ) );
	
			ConceptClass cc = c.getOfType ();
			assertEquals ( "Concept Class ID is wrong!", "EFO", cc.getId () );
			assertEquals ( "Concept Class label is wrong!",  "Experimental Factor", cc.getFullname () );
			Assert.assertTrue ( 
				"Concept Class definition is wrong!", 
				cc.getDescription ().startsWith ( "The Experimental Factor Ontology (EFO) provides a systematic" ) 
			);

		}
	}
	
	
	@Test
	public void testRelations () throws Exception
	{
		try ( ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext ( "efo_cfg.xml" ) )
		{
			OntModel model = (OntModel) ctx.getBean ( "jenaOntModel" );
			model.read ( Resources.getResource ( "efo-test.owl" ).toString (), "", "RDF/XML" );

			OWLMapper owlMap = (OWLMapper) ctx.getBean ( "owlMapper" );
			ONDEXGraph graph = owlMap.map2Graph ( model );
						
			DataSource efoDs = graph.getMetaData ().getDataSource ( "EFO" );
			assertNotNull ( "EFO data source not found!", efoDs );

			ONDEXConcept c = graph
				.getConcepts ()
				.stream ()
				.filter ( ci -> "GO_0033273".equals ( ci.getPID () ) )
				.findAny ()
				.orElse ( null );

			assertNotNull ( "Test concept not found!", c );
			

			ONDEXRelation about = graph.getRelationsOfConcept ( c )
			.stream ()
			.filter ( r -> "is_about".equals ( r.getOfType ().getId () ) )
			.findAny ()
			.orElse ( null );
			
			assertNotNull ( "is_about relation not found!", about );
			assertEquals ( "Wrong target for is_about", "CHEBI_33229", about.getToConcept ().getPID () );
		}
	}
	
	@Test
	public void testIriBasedAccession () throws Exception
	{
		try ( ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext ( "efo_cfg.xml" ) )
		{
			OntModel model = (OntModel) ctx.getBean ( "jenaOntModel" );
			model.read ( Resources.getResource ( "efo-test.owl" ).toString (), "", "RDF/XML" );

			OWLMapper owlMap = (OWLMapper) ctx.getBean ( "owlMapper" );
			ONDEXGraph graph = owlMap.map2Graph ( model );
			
			DataSource efoDs = graph.getMetaData ().getDataSource ( "EFO" );
			assertNotNull ( "EFO data source not found!", efoDs );

			ONDEXConcept c = graph
				.getConcepts ()
				.stream ()
				.filter ( ci -> ci.getConceptAccession ( "EFO:1001870", efoDs ) != null )
				.findAny ()
				.orElse ( null );
			
			assertNotNull ( "Test concept not found!", c );
			assertNotNull ( "Concept label is wrong!", c.getConceptName ( "Alzheimer Senile Dementia" ) );
		}
	}	
}
