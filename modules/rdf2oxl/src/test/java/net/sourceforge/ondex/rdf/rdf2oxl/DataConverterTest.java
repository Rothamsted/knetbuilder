package net.sourceforge.ondex.rdf.rdf2oxl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.io.Resources;

import uk.ac.ebi.utils.xml.XPathReader;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>1 Aug 2018</dd></dl>
 *
 */
public class DataConverterTest extends AbstractConverterTest
{
	@BeforeClass
	public static void initData () throws IOException
	{
		resultOxl = generateOxl (
			"target/data_converter_test.oxl", 
			"target/data_converter_test_tdb",
			Pair.of ( new FileInputStream ( "src/main/assembly/resources/data/bioknet.owl" ), "RDF/XML" ),
			Pair.of ( new FileInputStream ( "src/main/assembly/resources/data/bk_ondex.owl" ), "RDF/XML" ),
			Pair.of ( Resources.getResource ( "support_test/publications.ttl" ).openStream (), "TURTLE" )
		);
	}
	
	@Test
	public void testPubConcept () throws IOException
	{
		final String oxlPrefix = "/ondex/ondexdataseq/concepts/concept[pid = '26396590']";
		XPathReader xpath = new XPathReader ( resultOxl );

		assertEquals ( "Concept PMID:26396590 not found or too many of them!", 
			1,
			xpath.readNodeList ( oxlPrefix ).getLength ()
		);

		assertEquals ( "Concept PMID:26396590's JOURNAL_REF is wrong!", 
			"Biotechnology for biofuels",
			xpath.readString ( oxlPrefix +
				"/cogds/concept_gds[attrname/idRef = 'JOURNAL_REF']/value[@java_class = 'java.lang.String']/literal" 
			)
		);
	}

	@Test
	public void testAnnotation ()
	{
		final String oxlPrefix = "/ondex/ondexdataseq/concepts/concept[pid = 'testAnnotation']";
		XPathReader xpath = new XPathReader ( resultOxl );

		assertEquals ( "testAnnotation not found or too many of them!", 
			1,
			xpath.readNodeList ( oxlPrefix ).getLength ()
		);
		
		assertEquals ( "testAnnotation's description is wrong!", 
			"Just a test concept, used to annotate stuff",
			xpath.readString ( oxlPrefix + "/description" )
		);

		assertEquals ( "testAnnotation's evidence is wrong!", 
			"Manual curation",
			xpath.readString ( oxlPrefix + "/evidences/evidence/fullname" )
		);
				
		NodeList evs = xpath.readNodeList ( 
			oxlPrefix + "/cogds/concept_gds[attrname/idRef = 'EVIDENCE']" +
			"/value[@java_class = 'net.sourceforge.ondex.export.oxl.CollectionHolder']/literal[@clazz = 'java.util.HashSet']" +
			"/item/values[@type='xsd:string']/text()" 
		);
		assertNotNull ( "Multi-evidence attribute is null!", evs );
		assertEquals ( "Multi-evidence attribute is wrong!", 2, evs.getLength () );
		
		List<String> evStrings = IntStream.range ( 0, 2 )
		.mapToObj ( evs::item )
		.map ( Node::getNodeValue )
		.sorted ()
		.collect ( Collectors.toList () );
		
		assertEquals ( "Evidence 1 is wrong!", "Foo Evidence 1", evStrings.get ( 0 ) );
		assertEquals ( "Evidence 2 is wrong!", "Foo Evidence 2", evStrings.get ( 1 ) );
	}
	
	
	/**
	 * TODO: Remove
	 * 
	 * Just a test to move {@link Rdf2OxlConfiguration} to the XML, which should have a simpler syntax.
	 */
//	@Test 
//	public void testBean ()
//	{
//		ItemConfiguration icfg = springContext.getBean ( "testConfig", ItemConfiguration.class );
//		System.out.println ( icfg.getHeader () );
//	}
}
