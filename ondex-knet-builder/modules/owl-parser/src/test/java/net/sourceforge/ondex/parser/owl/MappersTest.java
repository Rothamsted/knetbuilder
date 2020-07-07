package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;
import static net.sourceforge.ondex.parser.owl.Utils.OWL_PARSER_DATA_SOURCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.util.prototypes.RelationTypePrototype;
import net.sourceforge.ondex.parser.ConceptBasedRelMapper;
import net.sourceforge.ondex.parser.ConstDataSourceMapper;
import net.sourceforge.ondex.parser.DefaultConceptMapper;
import net.sourceforge.ondex.parser.ExploringMapper.LinkerConfiguration;
import net.sourceforge.ondex.parser.InvertingConceptRelMapper;
import net.sourceforge.ondex.parser.SimpleRelationMapper;
import net.sourceforge.ondex.parser.TextMapper;



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
	
	private OWLTopConceptClassMapper ccmap;
	private DefaultConceptMapper<OntClass> conceptMap;
	
	private ONDEXGraph graph;
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );	
	
	
	@Before
	public void initTestData () throws IOException
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
		ontSubCls = model.createClass ( iri ( "foo", subClsId ) );
		ontSubCls.setLabel ( "Class B Label", "en" );
		ontSubCls.setComment ( "Class B Description", "en" );
		JENAUTILS.assertLiteral ( model, ontSubCls.getURI (), iri ( "dcterms:identifier" ), subClsId );
		

		ontCls.addSubClass ( ontSubCls );
		
		// ---- Examples of mappers setup. You don't want to do this programmatically, Spring is much better
		// 		
		ccmap = new OWLTopConceptClassMapper ();
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
		conceptMap.setAccessionsMapper ( accMap );
		
		conceptMap.setDescriptionMapper ( txtMap.apply ( RDFS.comment.getURI () ) );
		conceptMap.setDataSourceMapper ( new ConstDataSourceMapper<OntClass> ( Utils.OWL_PARSER_DATA_SOURCE ) );
		
		graph = new MemoryONDEXGraph ( "test" );
		model.write ( new FileWriter ( "target/mappersTest.ttl" ) );
	}
	
	@Test
	public void testConceptMapper ()
	{
		// This kind of initialisation is usually done by the OWLMapper automatically
		this.ccmap.setTopClassIris ( this.topCls.getURI () );
		ConceptClass cc = this.ccmap.map ( this.topCls, graph );
		this.conceptMap.map ( this.ontCls, cc, graph );
		
		ONDEXConcept ct = null;
		
		Assert.assertTrue ( 
			"Test Concept not found!", 
			( ct = graph
			.getConcepts ()
			.stream ()
			.filter ( Objects::nonNull )
			.filter ( c -> "ClassA".equals ( c.getPID () ) )
			.findAny ()
			.orElse ( null ) ) != null
		);
				
		DataSource ds = graph.getMetaData ().createDataSource ( 
			OWL_PARSER_DATA_SOURCE.getId (), OWL_PARSER_DATA_SOURCE.getFullName (), OWL_PARSER_DATA_SOURCE.getDescription () 
		);

		assertNotNull ( "Concept accession is wrong!", ct.getConceptAccession ( clsId, ds ) );
		assertNotNull ( "Concept label is wrong!", ct.getConceptName ( ontCls.getLabel ( "en" ) ) );
		assertEquals ( "Concept description is wrong!", ontCls.getComment ( "en" ), ct.getDescription () );
		
		ConceptClass cct = ct.getOfType ();
		assertEquals ( "Concept Class ID is wrong!", topClsId, cct.getId () );
		assertEquals ( "Concept label is wrong!",  topCls.getLabel ( "en" ), cct.getFullname () );
		assertEquals ( "Concept description is wrong!", topCls.getComment ( "en" ), cct.getDescription () );
	}
	
	
	@Test
	public void testMapper ()
	{
		OWLMapper mapper = new OWLMapper ();
		
		mapper.setConceptClassMapper ( ccmap );
		mapper.setConceptMapper ( conceptMap );
		mapper.setRootsScanner ( new IriBasedRootsScanner ( this.topCls.getURI () ) );
		mapper.setDoMapRootsToConcepts ( false );
				
		SimpleRelationMapper relMap = new SimpleRelationMapper ();
		relMap.setRelationTypePrototype ( RelationTypePrototype.IS_A_PROTOTYPE );
		
		ConceptBasedRelMapper relMapInv = new InvertingConceptRelMapper ( relMap );
		
		mapper.setLinkers ( Arrays.asList ( 
			new LinkerConfiguration<> ( new OWLSubClassScanner (), relMapInv ) 
		));
						
		ONDEXGraph graph = mapper.map2Graph ( this.model );

		checkAllGrah ( graph );
	}	
	
	
	@Test
	public void testSpringBootstrap () throws IOException
	{
		try ( ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext ( "mappings_ex.xml" ); )
		{
			OWLMapper owlMap = (OWLMapper) ctx.getBean ( "owlMapper" );
	
			ONDEXGraph graph = owlMap.map2Graph ( model );
			checkAllGrah ( graph );
		}
	}
	
	private void checkAllGrah ( ONDEXGraph graph )
	{
		assertEquals ( "Wrong no of concepts!", 2, graph.getConcepts ().size () );
		
		ONDEXConcept c1 = null, c2 = null;

		Assert.assertTrue ( 
			"Test Concept A not found!", 
			( c1 = graph
			.getConcepts ()
			.stream ()
			.filter ( Objects::nonNull )
			.filter ( c -> "ClassA".equals ( c.getPID () ) )
			.findAny ()
			.orElse ( null ) ) != null
		);
		Assert.assertTrue ( 
			"Test Concept B not found!", 
			( c2 = graph
			.getConcepts ()
			.stream ()
			.filter ( Objects::nonNull )
			.filter ( c -> "ClassB".equals ( c.getPID () ) )
			.findAny ()
			.orElse ( null ) ) != null
		);
				
		DataSource ds = graph.getMetaData ().createDataSource ( "owlParser", "The OWL Parser", "" );

		// Class A
		assertNotNull ( "Concept accession is wrong!", c1.getConceptAccession ( clsId, ds ) );
		assertNotNull ( "Concept label is wrong!", c1.getConceptName ( ontCls.getLabel ( "en" ) ) );
		assertEquals ( "Concept description is wrong!", ontCls.getComment ( "en" ), c1.getDescription () );
		
		Set<ConceptClass> ccs = graph.getMetaData ().getConceptClasses ();
		assertEquals ( "Wrong no of concepts!", 1, ccs.size () );
		
		ConceptClass cct = c1.getOfType ();
		assertEquals ( "Concept Class ID is wrong!", topClsId, cct.getId () );
		assertEquals ( "Concept Class label is wrong!",  topCls.getLabel ( "en" ), cct.getFullname () );
		assertEquals ( "Concept Class description is wrong!", topCls.getComment ( "en" ), cct.getDescription () );		
		
		// Class B
		assertNotNull ( "Concept accession is wrong!", c2.getConceptAccession ( subClsId, ds ) );
		assertNotNull ( "Concept label is wrong!", c2.getConceptName ( ontSubCls.getLabel ( "en" ) ) );
		assertEquals ( "Concept description is wrong!", ontSubCls.getComment ( "en" ), c2.getDescription () );
		
		cct = c2.getOfType ();
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
