package net.sourceforge.ondex.rdf.rdf2oxl;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.RelationType;

/**
 * Loads specific files to test Ondex metadata conversion and reload.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Oct 2018</dd></dl>
 *
 */
public class MetadataConverterReloadTest extends AbstractConverterReloadTest
{
	@BeforeClass
	public static void initData () throws IOException
	{
		resultGraph = loadOxl (
			"target/metadata_reload_test.xml", 
			"target/metadata_reload_test_tdb",
			Pair.of ( new FileInputStream ( "src/main/assembly/resources/data/bioknet.owl" ), "RDF/XML" ),
			Pair.of ( new FileInputStream ( "src/main/assembly/resources/data/bk_ondex.owl" ), "RDF/XML" )
		);
	}
	
	
	@Test
	public void testConceptClassesReload ()
	{
		
		Function<String, ConceptClass> getCC = id -> {
			ConceptClass cc = resultGraph.getMetaData ().getConceptClass ( id );
			assertNotNull ( "Concept Class " + id + " not found!", cc );
			return cc;
		};
		
		ConceptClass cc = getCC.apply ( "Disease" );
		
		cc = getCC.apply ( "EC" );
		assertEquals ( "EC's fullname wrong!", "Enzyme Classification", cc.getFullname () );
		
		cc = getCC.apply ( "Environment" );
		assertEquals ( "Environment's description wrong!",
			"Treatment or surrounding conditions",
			cc.getDescription ()
		);
		
		cc = getCC.apply ( "GeneOntologyTerms" );
		assertEquals ( "GeneOntologyTerms's parent wrong!",
			"OntologyTerms",
			cc.getSpecialisationOf ().getId ()
		);					
	}
	
	
	@Test
	public void testRelationTypeReload ()
	{
		ONDEXGraphMetaData meta = resultGraph.getMetaData ();
		
		Function<String, RelationType> getRT = id -> {
			RelationType rt = resultGraph.getMetaData ().getRelationType ( id );
			assertNotNull ( "Relation Type " + id + " not found!", rt );
			return rt;
		};
		
		RelationType rt = getRT.apply ( "part_of" );
		assertEquals ( "part_of's label wrong!", "part_of", rt.getFullname () );
		assertTrue ( "part_of's description wrong!",
			rt.getDescription ().startsWith ( "For continuants: C part_of C' if and only if" ) 
		);
		assertEquals ( "part_of's parent wrong!", "physical_relation", rt.getSpecialisationOf ().getId () );
		assertEquals ( "part_of's inverse wrong!", "has_part", rt.getInverseName () );
		assertTrue ( "part_of's isTransitive wrong!", rt.isTransitiv () );
		assertFalse ( "part_of's isSymmetric wrong!", rt.isSymmetric () );
	}	
}
