package net.sourceforge.ondex.rdf.rdf2oxl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.ebi.utils.xml.XPathReader;

/**
 * Loads specific files to test Ondex metadata conversion.
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
			Pair.of ( new FileInputStream ( "src/main/assembly/resources/data/bioknet.owl" ), "RDF/XML" ),
			Pair.of ( new FileInputStream ( "src/main/assembly/resources/data/bk_ondex.owl" ), "RDF/XML" )
		);
	}

	@Test
	public void testConceptClass () throws IOException
	{
		final String oxlPrefix = "/ondex/ondexmetadata/conceptclasses";
		
		XPathReader xpath = new XPathReader ( resultOxl );
		assertEquals ( "Disease class not found or too many of them!", 
			1,
			xpath.readNodeList ( oxlPrefix + "/cc[id = 'Disease']" ).getLength ()
		);
		assertEquals ( "EC's fullname wrong!",
			"Enzyme Classification", xpath.readString ( oxlPrefix + "/cc[id='EC']/fullname" )
		);
		assertEquals ( "Environment's description wrong!",
			"Treatment or surrounding conditions",
			xpath.readString ( oxlPrefix + "/cc[id='Environment']/description" )
		);
		assertEquals ( "GeneOntologyTerms's parent wrong!",
			"OntologyTerms",
			xpath.readString ( oxlPrefix + "/cc[id='GeneOntologyTerms']/specialisationOf/idRef" )
		);					
	}
	
	
	@Test
	public void testRelationType () throws IOException
	{
		final String oxlPrefix = "/ondex/ondexmetadata/relationtypes";
		
		XPathReader xpath = new XPathReader ( resultOxl );
		assertEquals ( "part_of not found or too many of them!", 
			1,
			xpath.readNodeList ( oxlPrefix + "/relation_type[id = 'part_of']" ).getLength ()
		);
		assertEquals ( "part_of's label wrong!",
			"part_of", xpath.readString ( oxlPrefix + "/relation_type[id = 'part_of']/fullname" )
		);
		assertTrue ( "part_of's description wrong!",
			xpath.readString ( oxlPrefix + "/relation_type[id = 'part_of']/description" )
			     .contains ( "For continuants: C part_of C' if and only if" )
		);
		assertEquals ( "part_of's parent wrong!",
			"physical_relation",
			xpath.readString ( oxlPrefix + "/relation_type[id = 'part_of']/specialisationOf/idRef" )
		);					
		assertEquals ( "part_of's inverse wrong!",
			"has_part",
			xpath.readString ( oxlPrefix + "/relation_type[id = 'part_of']/inverseName" )
		);					
		assertEquals ( "part_of's isTransitive wrong!",
			"true",
			xpath.readString ( oxlPrefix + "/relation_type[id = 'part_of']/isTransitive" )
		);					
		assertEquals ( "part_of's isSymmetric wrong!",
			"false",
			xpath.readString ( oxlPrefix + "/relation_type[id = 'part_of']/isSymmetric" )
		);					
	}	
}
