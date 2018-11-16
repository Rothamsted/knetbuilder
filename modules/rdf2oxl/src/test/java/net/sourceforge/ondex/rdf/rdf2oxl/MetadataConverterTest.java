package net.sourceforge.ondex.rdf.rdf2oxl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.Resources;

import uk.ac.ebi.utils.xml.XPathReader;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>1 Aug 2018</dd></dl>
 *
 */
public class MetadataConverterTest extends AbstractConverterTest
{
	
	@BeforeClass
	public static void initData () throws IOException
	{
		resultOxl = generateOxl (
			"target/metadata_test.oxl", 
			"target/metadata_test_tdb",
			Pair.of ( Resources.getResource ( "bioknet.owl" ).openStream (), "RDF/XML" ),
			Pair.of ( Resources.getResource ( "oxl_templates_test/bk_ondex.owl" ).openStream (), "RDF/XML" )
		);
	}

	@Test
	public void testMetdata () throws IOException
	{
		final String oxlPrefix = "/ondex/ondexmetadata/conceptclasses";
		
		XPathReader xpath = new XPathReader ( resultOxl );
		assertEquals ( "Disease class not found or too many of them!", 
			1,
			xpath.readNodeList ( oxlPrefix + "/cc[id = 'Disease']" ).getLength ()
		);
		assertEquals ( "EC's fullname not found!",
			"Enzyme Classification", xpath.readString ( oxlPrefix + "/cc[id='EC']/fullname" )
		);
		assertEquals ( "Environment's description not found!",
			"Treatment or surrounding conditions",
			xpath.readString ( oxlPrefix + "/cc[id='Environment']/description" )
		);
		assertEquals ( "GeneOntologyTerms' parent not found!",
			"OntologyTerms",
			xpath.readString ( oxlPrefix + "/cc[id='GeneOntologyTerms']/specialisationOf/idRef" )
		);					
	}
}
