package net.sourceforge.ondex.parser.owl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>6 Apr 2017</dd></dl>
 *
 */
public class MappersTest
{	
	private OntModel model;
	private String topClsId; 
	private OntClass topCls;
	private OntClass ontCls;
	private String clsId;
	
	@Before
	public void initTestData ()
	{
		model = ModelFactory.createOntologyModel ();
		
		topClsId = "TopClass";
		topCls = model.createClass ( NamespaceUtils.iri ( "foo", topClsId ) );
		topCls.setLabel ( "Top Class", "en" );
		topCls.setComment ( "Top Class Description", "en" );
		
		clsId = "ClassA";
		ontCls = model.createClass ( NamespaceUtils.iri ( "foo", clsId ) );
		ontCls.setLabel ( "Class A Label", "en" );
		ontCls.setComment ( "Class A Description", "en" );

		topCls.addSubClass ( ontCls );		
	}
	
	@Test
	public void testBasics ()
	{
		
		OWLConceptClassMapper ccmap = new OWLConceptClassMapper ();
		ccmap.setClassIri ( topCls.getURI () );

		OWLConceptMapper conceptMap = new OWLConceptMapper ();
		conceptMap.setConceptClassMapper ( ccmap );

		OWLMapper owlMap = new OWLMapper ();
		owlMap.setConceptMappers ( new HashSet<> ( Arrays.asList ( conceptMap ) ) );

		ONDEXGraph graph = owlMap.map ( model );
		
		final ONDEXConcept[] ct = new ONDEXConcept [ 1 ];
		
		Assert.assertTrue ( 
			"Test Concept not found!", 
			graph
			.getConcepts ()
			.stream ()
			.anyMatch ( c -> "ClassA".equals ( c.getPID () ) && ( ct [ 0 ] = c ) != null )
		);
		
		DataSource ds = graph.getMetaData ().createDataSource ( "owlParser", "The OWL Parser", "" );

		assertNotNull ( "Concept accession is wrong!", ct[ 0 ].getConceptAccession ( clsId, ds ) );
		assertNotNull ( "Concept label is wrong!", ct[ 0 ].getConceptName ( ontCls.getLabel ( "en" ) ) );
		assertEquals ( "Concept description is wrong!", ontCls.getComment ( "en" ), ct[ 0 ].getDescription () );
		
		ConceptClass cct = ct [ 0 ].getOfType ();
		assertEquals ( "Concept Class ID is wrong!", topClsId, cct.getId () );
		assertEquals ( "Concept label is wrong!",  topCls.getLabel ( "en" ), cct.getFullname () );
		assertEquals ( "Concept description is wrong!", topCls.getComment ( "en" ), cct.getDescription () );
	}
	
	
	@Test
	public void testSpringBootstrap ()
	{
		ApplicationContext ctx = new ClassPathXmlApplicationContext ( "mapper_cfg_ex.xml" );
		OWLMapper owlMap = (OWLMapper) ctx.getBean ( "owlMapper" );

		ONDEXGraph graph = owlMap.map ( model );
		
		final ONDEXConcept[] ct = new ONDEXConcept [ 1 ];
		
		Assert.assertTrue ( 
			"Test Concept not found!", 
			graph
			.getConcepts ()
			.stream ()
			.anyMatch ( c -> "ClassA".equals ( c.getPID () ) && ( ct [ 0 ] = c ) != null )
		);
		
		DataSource ds = graph.getMetaData ().createDataSource ( "owlParser", "The OWL Parser", "" );

		assertNotNull ( "Concept accession is wrong!", ct[ 0 ].getConceptAccession ( clsId, ds ) );
		assertNotNull ( "Concept label is wrong!", ct[ 0 ].getConceptName ( ontCls.getLabel ( "en" ) ) );
		assertEquals ( "Concept description is wrong!", ontCls.getComment ( "en" ), ct[ 0 ].getDescription () );
		
		ConceptClass cct = ct [ 0 ].getOfType ();
		assertEquals ( "Concept Class ID is wrong!", topClsId, cct.getId () );
		assertEquals ( "Concept label is wrong!",  topCls.getLabel ( "en" ), cct.getFullname () );
		assertEquals ( "Concept description is wrong!", topCls.getComment ( "en" ), cct.getDescription () );
	}
	
}
