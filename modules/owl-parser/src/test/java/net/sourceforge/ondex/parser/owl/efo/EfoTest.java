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
	public void testAccessions () throws Exception
	{
		try ( ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext ( "efo_cfg.xml" ) )
		{
			OntModel model = (OntModel) ctx.getBean ( "jenaOntModel" );
			model.read ( Resources.getResource ( "efo_test.owl" ).toString (), "", "RDF/XML" );

			OWLMapper owlMap = (OWLMapper) ctx.getBean ( "owlMapper" );
			ONDEXGraph graph = owlMap.map2Graph ( model );
			
			assertEquals ( "Wrong no of concepts!", 1, graph.getConcepts ().size () );
			
			ONDEXConcept c = graph.getConcepts ().iterator ().next ();
			DataSource efoDs = graph.getMetaData ().getDataSource ( "EFO" );
			assertNotNull ( "EFO data source not found!", efoDs );
			
			assertNotNull ( "Concept accession is wrong!", c.getConceptAccession ( "EFO_0002888", efoDs ) );
			assertNotNull ( "Concept label is wrong!", c.getConceptName ( "Homo sapiens cell line" ) );
	
			ConceptClass cc = c.getOfType ();
			assertEquals ( "Concept Class ID is wrong!", "EFO", cc.getId () );
			assertEquals ( "Concept Class label is wrong!",  "Experimental Factor", cc.getFullname () );
			Assert.assertTrue ( 
				"Concept Class definition is wrong!", 
				cc.getDescription ().startsWith ( "The Experimental Factor Ontology (EFO) provides a systematic" ) 
			);

		}
	}
	
}
