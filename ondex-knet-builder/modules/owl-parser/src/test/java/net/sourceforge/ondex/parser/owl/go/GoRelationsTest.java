package net.sourceforge.ondex.parser.owl.go;

import static org.junit.Assert.assertNotNull;

import java.io.Closeable;

import org.apache.jena.ontology.OntModel;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.io.Resources;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.parser.owl.OWLMapper;

/**
 * Basic tests for Gene Ontology.
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
		ApplicationContext ctx = new ClassPathXmlApplicationContext ( "go_relations_test_cfg.xml" );
		
		OntModel model = (OntModel) ctx.getBean ( "jenaOntModel" );
		model.read ( Resources.getResource ( "go_tests_common.owl" ).toString (), "", "RDF/XML" );		
		model.read ( Resources.getResource ( "go_relations_test.owl" ).toString (), "", "RDF/XML" );
		
		OWLMapper owlMap = (OWLMapper) ctx.getBean ( "owlMapper" );
		ONDEXGraph graph = owlMap.map2Graph ( model );
		
		for ( final String target: new String [] { "GO_0000070", "GO_0007052", "GO_0000278" } )
		{
			ONDEXRelation relation = graph
			.getRelations ()
			.stream ()
			.filter ( r -> "part_of".equals ( r.getOfType ().getId () ) )
			.filter ( r -> target.equals ( r.getToConcept ().getPID () ) )
			.findFirst ()
			.orElse ( null );
			
			String note = "GO_0000278".equals ( target ) ? " (via equivalentClass)" : ""; 
			assertNotNull ( String.format ( "part_of %s%s not found!", target, note ), relation );
		}
		
		( (Closeable) ctx ).close ();
	}
	
	@Test
	public void testEquivalents () throws Exception
	{
		ApplicationContext ctx = new ClassPathXmlApplicationContext ( "go_relations_test_cfg.xml" );

		OntModel model = (OntModel) ctx.getBean ( "jenaOntModel" );
		model.read ( Resources.getResource ( "go_tests_common.owl" ).toString (), "", "RDF/XML" );		
		model.read ( Resources.getResource ( "go_relations_test.owl" ).toString (), "", "RDF/XML" );
		
		OWLMapper owlMap = (OWLMapper) ctx.getBean ( "owlMapper" );

		ONDEXGraph graph = owlMap.map2Graph ( model );
		
		for ( final String target: new String [] { "GO_0000070", "GO_0007052", "GO_0000278" } )
		{
			ONDEXRelation relation = graph
			.getRelations ()
			.stream ()
			.filter ( r -> "part_of".equals ( r.getOfType ().getId () ) )
			.filter ( r -> target.equals ( r.getToConcept ().getPID () ) )
			.findFirst ()
			.orElse ( null );
			
			String note = "GO_0000278".equals ( target ) ? " (via equivalentClass)" : ""; 
			assertNotNull ( String.format ( "part_of %s%s not found!", target, note ), relation );
		}
		
		( (Closeable) ctx ).close ();
	}	
}
