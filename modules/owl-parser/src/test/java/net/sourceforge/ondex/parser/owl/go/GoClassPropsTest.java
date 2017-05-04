package net.sourceforge.ondex.parser.owl.go;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Closeable;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.io.Resources;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.owl.OWLMapper;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>1 May 2017</dd></dl>
 *
 */
public class GoClassPropsTest
{
	@Test
	public void testAccessions () throws Exception
	{
		OntModel model = ModelFactory.createOntologyModel ();
		model.read ( Resources.getResource ( "go_basic_tests.owl" ).toString (), "", "RDF/XML" );
		
		ApplicationContext ctx = new ClassPathXmlApplicationContext ( "go_accessions_test_cfg.xml" );
		OWLMapper owlMap = (OWLMapper) ctx.getBean ( "owlMapper" );

		ONDEXGraph graph = owlMap.map ( model );
		
		assertEquals ( "Wrong no of concepts!", 1, graph.getConcepts ().size () );
		
		ONDEXConcept c = graph.getConcepts ().iterator ().next ();

		DataSource ds = graph.getMetaData ().createDataSource ( "owlParser", "The OWL Parser", "" );
		
		assertNotNull ( "Concept accession is wrong!", c.getConceptAccession ( "GO:0000003", ds ) );
		assertNotNull ( "Concept label is wrong!", c.getConceptName ( "reproduction" ) );
		Assert.assertTrue ( 
			"Concept definition is wrong!", 
			c.getDescription ().startsWith ( "The production of new individuals that contain" ) 
		);

		ConceptClass cc = c.getOfType ();
		assertEquals ( "Concept Class ID is wrong!", "GO_0008150", cc.getId () );
		assertEquals ( "Concept Class label is wrong!",  "biological_process", cc.getFullname () );
		Assert.assertTrue ( 
			"Concept Class definition is wrong!", 
			cc.getDescription ().startsWith ( "Any process specifically pertinent to" ) 
		);
		
		( (Closeable) ctx ).close ();
	}
	
	@Test
	public void testNames () throws Exception
	{
		OntModel model = ModelFactory.createOntologyModel ();
		model.read ( Resources.getResource ( "go_basic_tests.owl" ).toString (), "", "RDF/XML" );
		
		ApplicationContext ctx = new ClassPathXmlApplicationContext ( "go_names_test_cfg.xml" );
		OWLMapper owlMap = (OWLMapper) ctx.getBean ( "owlMapper" );

		ONDEXGraph graph = owlMap.map ( model );
		
		assertEquals ( "Wrong no of concepts!", 1, graph.getConcepts ().size () );
		
		ONDEXConcept c = graph.getConcepts ().iterator ().next ();

		assertNotNull ( "Concept label is wrong!", c.getConceptName ( "reproduction" ) );
		for ( String name: new String [] { "reproductive physiological process", "fake synonym added to the test" } )
		assertNotNull ( 
			format ( "Concept additional name not found (%s)!", name ), 
			c.getConceptName ( name )
		);
		
		Assert.assertTrue ( 
			"Concept definition is wrong!", 
			c.getDescription ().startsWith ( "The production of new individuals that contain" ) 
		);
		
		( (Closeable) ctx ).close ();
	}
}
