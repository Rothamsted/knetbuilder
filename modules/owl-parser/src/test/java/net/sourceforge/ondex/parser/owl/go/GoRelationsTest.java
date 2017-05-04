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

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.parser.owl.OWLMapper;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 May 2017</dd></dl>
 *
 */
public class GoRelationsTest
{
	@Test
	public void testSomeValuesFrom () throws Exception
	{
		OntModel model = ModelFactory.createOntologyModel ();
		model.read ( Resources.getResource ( "go_tests_common.owl" ).toString (), "", "RDF/XML" );		
		model.read ( Resources.getResource ( "go_relations_test.owl" ).toString (), "", "RDF/XML" );
		
		ApplicationContext ctx = new ClassPathXmlApplicationContext ( "go_relations_test_cfg.xml" );
		OWLMapper owlMap = (OWLMapper) ctx.getBean ( "owlMapper" );

		ONDEXGraph graph = owlMap.map ( model );
		
		ONDEXRelation relation = graph
		.getRelations ()
		.stream ()
		.filter ( r -> "part_of".equals ( r.getOfType ().getId () ) )
		.filter ( r -> "GO_0000070".equals ( r.getToConcept ().getPID () ) )
		.findFirst ()
		.orElse ( null );
		
		assertNotNull ( "part_of GO_0000070 not found!", relation );

		relation = graph
		.getRelations ()
		.stream ()
		.filter ( r -> "part_of".equals ( r.getOfType ().getId () ) )
		.filter ( r -> "GO_0007052".equals ( r.getToConcept ().getPID () ) )
		.findFirst ()
		.orElse ( null );
		
		assertNotNull ( "part_of GO_0007052 not found!", relation );
		
		relation = graph
		.getRelations ()
		.stream ()
		.filter ( r -> "part_of".equals ( r.getOfType ().getId () ) )
		.filter ( r -> "GO_0000278".equals ( r.getToConcept ().getPID () ) )
		.findFirst ()
		.orElse ( null );
		
		assertNotNull ( "part_of GO_0000278 (via equivalentClass) not found!", relation );
		
		( (Closeable) ctx ).close ();
	}
}
