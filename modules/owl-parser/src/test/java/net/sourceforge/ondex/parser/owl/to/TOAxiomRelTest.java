package net.sourceforge.ondex.parser.owl.to;

import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Closeable;

import org.apache.jena.ontology.OntModel;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.io.Resources;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.parser.owl.OWLConceptClassMapper;
import net.sourceforge.ondex.parser.owl.OWLConceptMapper;

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
		ONDEXGraph graph = new MemoryONDEXGraph ( "test" );

		ApplicationContext ctx = new ClassPathXmlApplicationContext ( "to_cfg.xml" );

		OntModel model = (OntModel) ctx.getBean ( "jenaOntModel" );
		model.read ( Resources.getResource ( "to_owl_axiom.owl" ).toString (), "", "RDF/XML" );
		
		OWLConceptMapper conceptMapper = (OWLConceptMapper) ctx.getBean ( "conceptMapper" );
		OWLConceptClassMapper ccMapper = (OWLConceptClassMapper) ctx.getBean ( "conceptClassMapper" );
		ccMapper.setClassIri ( iri ( "obo:TO_0020095"  ) );
		conceptMapper.setConceptClassMapper ( ccMapper );
		conceptMapper.map ( model.getOntClass ( iri ( "obo:TO_0000523" ) ), graph );
		
		assertEquals ( "Wrong no of concepts!", 1, graph.getConcepts ().size () );
		
		ONDEXConcept c = graph.getConcepts ().iterator ().next ();

		DataSource ds = graph.getMetaData ().createDataSource ( "TO", "Plant Trait Ontology", "" );
		
		
		assertNotNull ( "Concept accession is wrong!", c.getConceptAccession ( "0000523", ds ) );
		assertNotNull ( "Concept label is wrong!", c.getConceptName ( "stomatal resistance" ) );
		Assert.assertTrue ( 
			"Concept definition is wrong!", 
			c.getDescription ().startsWith ( "Stomatal resistance (or its inverse" ) 
		);

		ConceptClass cc = c.getOfType ();
		assertEquals ( "Concept Class ID is wrong!", "TO_0020095", cc.getId () );
		assertEquals ( "Concept Class label is wrong!",  "stomatal process related trait", cc.getFullname () );
		Assert.assertTrue ( 
			"Concept Class definition is wrong!", 
			cc.getDescription ().startsWith ( "Trait associated with any of the stomatal" ) 
		);
		
		ds = graph.getMetaData ().createDataSource ( "Wikipedia", "Wikipedia", "" );
		assertNotNull ( "Additional x-ref to Wikipedia is wrong!", c.getConceptAccession ( "Stomatal_conductance", ds ) );
		
		( (Closeable) ctx ).close ();
	
	}
}
