package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDFS;
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
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.utils.EvidenceTypePrototype;
import net.sourceforge.ondex.parser.ConceptMapper;
import net.sourceforge.ondex.parser2.ConceptClassMapper;
import net.sourceforge.ondex.parser2.DefaultConceptClassMapper;
import net.sourceforge.ondex.parser2.DefaultConceptMapper;
import net.sourceforge.ondex.parser2.TextMapper;

/**
 * Tests {@link OWLMapper} and shows typical examples of how to use both the specific OWL mapping components and the 
 * components from the {@code net.sourceforge.ondex.parser} framework. 
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
	private OntClass ontSubCls;
	private String subClsId;
	
	private DefaultConceptClassMapper<OntClass> ccmap;
	private DefaultConceptMapper<OntClass> conceptMap;
	
	private ONDEXGraph graph;
	
	
	@Before
	public void initTestData ()
	{
		model = ModelFactory.createOntologyModel ( OntModelSpec.OWL_MEM );
		
		topClsId = "TopClass";
		topCls = model.createClass ( iri ( "foo", topClsId ) );
		topCls.setLabel ( "Top Class", "en" );
		topCls.setComment ( "Top Class Description", "en" );
		
		
		clsId = "ClassA";
		ontCls = model.createClass ( NamespaceUtils.iri ( "foo", clsId ) );
		ontCls.setLabel ( "Class A Label", "en" );
		ontCls.setComment ( "Class A Description", "en" );
		JENAUTILS.assertLiteral ( model, ontCls.getURI (), iri ( "dcterms:identifier" ), clsId );

		topCls.addSubClass ( ontCls );		

		subClsId = "ClassB";
		ontSubCls = model.createClass ( NamespaceUtils.iri ( "foo", subClsId ) );
		ontSubCls.setLabel ( "Class B Label", "en" );
		ontSubCls.setComment ( "Class B Description", "en" );
		JENAUTILS.assertLiteral ( model, ontSubCls.getURI (), iri ( "dcterms:identifier" ), subClsId );
		

		ontCls.addSubClass ( ontSubCls );
		
		// ---- Examples of mappers setup. You don't want to do this programmatically, Spring is much better
		// 		
		ccmap = new DefaultConceptClassMapper<> ();
		ccmap.setIdMapper ( new IRIBasedIdMapper () );
		
		// You don't usually need this facility, Spring Beans is much better.
		Function<String, TextMapper<OntClass>> txtMap = puri -> { 
			OWLTextMapper map = new OWLTextMapper ();
			map.setPropertyIri ( puri );
			return map;
		};
				
		ccmap.setFullNameMapper ( txtMap.apply ( RDFS.label.getURI () )  );
		ccmap.setDescriptionMapper ( txtMap.apply ( RDFS.comment.getURI () ) );

		conceptMap = new DefaultConceptMapper<> ();
		conceptMap.setIdMapper ( new IRIBasedIdMapper () );
		conceptMap.setPreferredNameMapper ( txtMap.apply ( RDFS.label.getURI () )  );

		OBOWLAccessionsMapper accMap = new OBOWLAccessionsMapper ();
		accMap.setPropertyIri ( iri ( "dcterms:identifier" ) );
		conceptMap.setAccessionsMappers ( new HashSet<> ( Arrays.asList ( accMap ) ) );
		
		conceptMap.setDescriptionMapper ( txtMap.apply ( RDFS.comment.getURI () ) );		
		
		graph = new MemoryONDEXGraph ( "test" );
	}
	
	@Test
	public void testConceptMapper ()
	{
		conceptMap.map ( this.ontCls, graph );
		
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
	public void testMapper ()
	{
		OwlSubClassRelMapper subClsMap = new OwlSubClassRelMapper ();
		subClsMap.setConceptClassMapper ( ccmap );
		subClsMap.setConceptMapper ( conceptMap );
		subClsMap.setEvidenceTypePrototype ( new EvidenceTypePrototype ( "IMDP", "IMDP", "" ) );
		
		OWLMapper map = new OWLMapper ();
		map.setRelationsMappers ( Collections.singleton ( subClsMap ) );
		
		ONDEXGraph graph = map.map ( this.model );

		checkAllGrah ( graph );
	}	
	
	
	@Test
	public void testSpringBootstrap () throws IOException
	{
		ApplicationContext ctx = new ClassPathXmlApplicationContext ( "mappings_ex.xml" );
		OWLMapper owlMap = (OWLMapper) ctx.getBean ( "owlMapper" );

		ONDEXGraph graph = owlMap.map ( model );
		checkAllGrah ( graph );
		
		( (Closeable) ctx ).close ();
	}
	
	private void checkAllGrah ( ONDEXGraph graph )
	{
		assertEquals ( "Wrong no of concepts!", 2, graph.getConcepts ().size () );
		
		final ONDEXConcept[] cs = new ONDEXConcept [ 2 ];

		Assert.assertTrue ( 
			"Test Concept A not found!", 
			graph
			.getConcepts ()
			.stream ()
			.anyMatch ( c -> "ClassA".equals ( c.getPID () ) && ( cs [ 0 ] = c ) != null )
		);
		Assert.assertTrue ( 
			"Test Concept B not found!", 
			graph
			.getConcepts ()
			.stream ()
			.anyMatch ( c -> "ClassB".equals ( c.getPID () ) && ( cs [ 1 ] = c ) != null )
		);
				
		DataSource ds = graph.getMetaData ().createDataSource ( "owlParser", "The OWL Parser", "" );

		// Class A
		assertNotNull ( "Concept accession is wrong!", cs[ 0 ].getConceptAccession ( clsId, ds ) );
		assertNotNull ( "Concept label is wrong!", cs[ 0 ].getConceptName ( ontCls.getLabel ( "en" ) ) );
		assertEquals ( "Concept description is wrong!", ontCls.getComment ( "en" ), cs[ 0 ].getDescription () );
		
		Set<ConceptClass> ccs = graph.getMetaData ().getConceptClasses ();
		assertEquals ( "Wrong no of concepts!", 1, ccs.size () );
		
		ConceptClass cct = cs [ 0 ].getOfType ();
		assertEquals ( "Concept Class ID is wrong!", topClsId, cct.getId () );
		assertEquals ( "Concept Class label is wrong!",  topCls.getLabel ( "en" ), cct.getFullname () );
		assertEquals ( "Concept Class description is wrong!", topCls.getComment ( "en" ), cct.getDescription () );		
		
		// Class B
		assertNotNull ( "Concept accession is wrong!", cs[ 1 ].getConceptAccession ( subClsId, ds ) );
		assertNotNull ( "Concept label is wrong!", cs[ 1 ].getConceptName ( ontSubCls.getLabel ( "en" ) ) );
		assertEquals ( "Concept description is wrong!", ontSubCls.getComment ( "en" ), cs[ 1 ].getDescription () );
		
		cct = cs [ 1 ].getOfType ();
		assertEquals ( "Concept Class ID for B is wrong!", topClsId, cct.getId () );

		// B subClassOf A
		Set<ONDEXRelation> rels = graph.getRelations ();
		assertEquals ( "Wrong no of relations!", 1, rels.size () );
		ONDEXRelation rel = rels.iterator ().next ();
		
		assertEquals ( "Relation's type is wrong!", "is_a", rel.getOfType ().getId () );
		assertEquals ( "Relation's from is wrong!", subClsId, rel.getFromConcept ().getPID () );
		assertEquals ( "Relation's to is wrong!", clsId, rel.getToConcept ().getPID () );		
	}
	
}
