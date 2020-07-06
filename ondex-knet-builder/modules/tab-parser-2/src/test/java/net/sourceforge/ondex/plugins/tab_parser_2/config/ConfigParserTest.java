package net.sourceforge.ondex.plugins.tab_parser_2.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.io.Resources;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.tools.subgraph.DefConst;
import net.sourceforge.ondex.tools.tab.importer.ConceptPrototype;
import net.sourceforge.ondex.tools.tab.importer.PathParser;
import net.sourceforge.ondex.tools.tab.importer.RelationPrototype;

/**
 * JUnit tests for {@link ConfigParser}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>14 Dec 2016</dd></dl>
 *
 */
public class ConfigParserTest
{
	public static Document parseXmlString ( String xml ) throws ParserConfigurationException, SAXException, IOException 
	{
		DocumentBuilder docBuild = DocumentBuilderFactory.newInstance ().newDocumentBuilder ();
		return docBuild.parse ( new ReaderInputStream ( new StringReader ( xml ), "UTF-8" ) );
	}
	
	@Test
	public void testXPath () throws Exception
	{
		Document root = parseXmlString ( "<root><child>Hello</child></root>" );
		
		Assert.assertEquals ( 
			"xpath element not found!",
			"Hello",
			ConfigParser.xpath ( root, "/root/child/text()", XPathConstants.STRING ).orNull () 
		);
		
		Assert.assertFalse ( 
			"xpath doesn't return null!",
			ConfigParser.xpath ( root, "/root/foo", XPathConstants.NODE ).isPresent () 
		);
	}
	
	@Test
	public void testParseOptionElem () throws Exception
	{
		Element root = parseXmlString ( "<root><class>value</class></root>" ).getDocumentElement ();
		Assert.assertEquals ( 
			"parseOptionElem() doesn't work!", 
			"value", ConfigParser.parseOptionElem ( root, "class" ).orNull ()
		);

		root = parseXmlString ( "<root>foo</root>" ).getDocumentElement ();
		Assert.assertFalse ( 
			"parseOptionElem() doesn't return null!", 
			ConfigParser.parseOptionElem ( root, "class" ).isPresent ()
		);

	}

	@Test
	public void testParseColumn () throws Exception
	{
		Element root = parseXmlString ( "<column index = '2' />" ).getDocumentElement ();
		Assert.assertEquals ( 
			"parseColumn() didn't find index attribute!", 
			(Integer) 2, (Integer) ConfigParser.parseColumn ( root ) 
		);
		
		/* TODO: later
		root = parseXmlString ( "<column header = 'Foo Col' />" ).getDocumentElement ();
		Assert.assertEquals ( 
			"parseColumn() didn't find index attribute!", 
			"Foo Col", (String) ConfigParser.parseColumn ( root ) 
		);*/
	}

	@Test
	public void testParseColumnOrValue () throws Exception
	{
		Element root = parseXmlString ( 
			"<concept><data-source><column index = '2' /></data-source></concept>" 
		).getDocumentElement ();

		Assert.assertEquals ( 
			"parseColumnOrValue() didn't find index attribute!", 
			(Integer) 2, (Integer) ConfigParser.parseColumnOrValue ( root, "data-source" ).orNull () 
		);
		
		// TODO: 'header' too
		
		root = parseXmlString ( "<concept><data-source>PUBMED</data-source></concept>" ).getDocumentElement ();
		Assert.assertEquals ( 
			"parseColumnOrValue() didn't find string value!", 
			"PUBMED", ConfigParser.parseColumnOrValue ( root, "data-source" ).orNull () 
		);
		
	}

	
	@Test
	public void testParseConcept () throws Exception
	{
		// The XML is inconsistent, eg, a valued accession shouldn't be with a col-based data source and
		// the accession's data-source attribute shouldn't appear with <data-source>
		// TODO: for the moment we don't check that
		String xml = 
					"<concept id = 'c1'>\n"
				+	"  <class>Protein</class>\n"
				+	"  <accession data-source = 'UNIPROT' ambiguous = 'true'>Q3234</accession>\n"
				+ "  <name preferred = 'true'><column index = '1' /></name>\n"
				+ "  <data-source><column index = '0'/></data-source>\n"
				+ "  <evidence>foo-experiment</evidence>\n"
				+ "  <attribute indexed = 'true' name = 'attr1'>attribute 1 value</attribute>\n"
				+ "  <attribute type = 'INTEGER' name = 'attr2'><column index = '2'/></attribute>\n"
				+ "</concept>\n";
		
		Element root = parseXmlString ( xml ).getDocumentElement ();
		PathParser pp = new PathParser ( new MemoryONDEXGraph ( "default" ) );
		Pair<String, ConceptPrototype> result = ConfigParser.collectConcept ( root, pp );
		Assert.assertEquals ( "parseConcept() didn't extract the ID!", "c1", result.getLeft () );
		
		ConceptPrototype conceptPrototype = result.getRight (); 
		Assert.assertNotNull ( "parseConcept() didn't extract the concept class!", conceptPrototype );
		ONDEXConcept concept = conceptPrototype.parse ( new String [] { "UNIPROT", "Protein Q3234", "32768" } );
		assertEquals ( "Concept Class not extracted!", "Protein", concept.getOfType ().getId () );
		assertEquals ( "Concept name not extracted!", "Protein Q3234", concept.getConceptName ().getName () );
		assertEquals ( "Concept data source not extracted!", "UNIPROT", concept.getElementOf ().getId () );
		assertEquals ( "Concept evidence extracted!", "foo-experiment", concept.getEvidence ().iterator ().next ().getId () );
		Set<Attribute> attrs = concept.getAttributes ();
		assertEquals ( "Concept extracted attributes should be 2!", 2, attrs.size () );
		Attribute a[] = attrs.toArray ( new Attribute [ 0 ] );
		if ( !"attr1".equals ( a[ 0 ].getOfType ().getId () ) ) a = new Attribute [] { a [ 1 ], a [ 0 ] };
		assertEquals ( "Attribute's name 1 mismatch!", "attr1", a [ 0 ].getOfType ().getId () );
		assertEquals ( "Attribute's name 2 mismatch!", "attr2", a [ 1 ].getOfType ().getId () );
		assertEquals ( "attr 1 value mismatch!", "attribute 1 value", a [ 0 ].getValue () );
		assertEquals ( "attr 2 value mismatch!", 32768, a [ 1 ].getValue () );
		assertTrue ( "attr 1 indexed flag mismatch!", a [ 0].isDoIndex () );
	}
	
	@Test
	public void testParseRelation () throws Exception
	{
		String xml = 
			"<relation source-ref = 'c1' target-ref = 'c2'>\n" +
			"  <type>pub_in</type>\n" +
			"  <evidence><column index = '2'>0</column></evidence>\n" +
			"  <attribute type = 'NUMBER' name = 'p-value'><column index = '3'/></attribute>\n" +
			"</relation>";
		
		Element root = parseXmlString ( xml ).getDocumentElement ();
		
		PathParser pp = new PathParser ( new MemoryONDEXGraph ( "default" ) );
		Map<String, ConceptPrototype> concepts = new HashMap<String, ConceptPrototype> ();
		concepts.put ( "c1", pp.newConceptPrototype ( DefConst.defAccession ( 0, "UNIPROTKB" ) )	);
		concepts.put ( "c2", pp.newConceptPrototype ( DefConst.defAccession ( 1, "UNIPROTKB" ) )	);
		
		RelationPrototype relProto = ConfigParser.collectRelation ( root, pp, concepts );
		ONDEXRelation rel = relProto.parse ( 
			concepts.get ( "c1" ).parse ( new String[] { "Q1234" } ),
			concepts.get ( "c2" ).parse ( new String[] { "Q456" } ), 
			new String[] { "", "", "ArrayExpress", "1E-6" } 
		);
		
		assertEquals ( "Relation type not extracted!", "pub_in", rel.getOfType ().getId () );
		assertEquals ( "Relation evidence not extracted!", "ArrayExpress", rel.getEvidence ().iterator ().next ().getId () );
		Attribute attr = rel.getAttributes ().iterator ().next ();
		assertEquals ( "Attribute's name 1 mismatch!", "p-value", attr.getOfType ().getId () );
		assertEquals ( "attr 1 value mismatch!", 1E-6d, attr.getValue () );
	}

	
	@Test
	public void testConfigXml () throws Exception
	{
		Reader schemaReader = new InputStreamReader ( 
			Resources.getResource ( this.getClass (), "/test_tab_spec_1.xml" ).openStream (), "UTF-8" 
		);
		ONDEXGraph graph = new MemoryONDEXGraph ( "default" );
		

		PathParser pp = ConfigParser.parseConfigXml ( schemaReader, graph, "target/test-classes/ppi_example.tsv" );
		pp.parse ();
		
		Set<ONDEXConcept> concepts = graph.getConcepts ();
		assertFalse ( "No concepts found in the test file!", concepts.isEmpty () );
		assertTrue ( 
			"Expected protein not found in the test file!",
			concepts.
			stream ()
			.anyMatch ( concept -> 
		  {
		  	for ( ConceptAccession acc: concept.getConceptAccessions () )
		  		if ( "UNIPROTKB:Q13158".equals ( acc.getAccession () ) ) return true; 
		  	return false;
		  })
		);
		
		
		Set<ONDEXRelation> relations = graph.getRelations ();
		assertFalse ( "No relations found in the test file!", relations.isEmpty () );
		
		assertTrue ( 
			"Expected interaction not found in the test file!",
			relations
			.stream ()
			.anyMatch ( concept -> 
		  {
		  	ONDEXConcept from = concept.getFromConcept ();
		  	ONDEXConcept to = concept.getToConcept ();
		  	if ( !"UNIPROTKB:O55042".equals ( from.getConceptAccessions ().iterator ().next ().getAccession () ) )
		  		return false;
		  	if ( !"UNIPROTKB:Q5S006".equals ( to.getConceptAccessions ().iterator ().next ().getAccession () ) )
		  		return false;
		  	return true;
		  })
		);
		
		// TODO: more XML examples (see TabXsdTest.java)
	}
}
