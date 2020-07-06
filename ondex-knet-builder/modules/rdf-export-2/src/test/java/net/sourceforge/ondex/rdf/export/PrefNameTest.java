package net.sourceforge.ondex.rdf.export;

import java.io.StringWriter;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.marcobrandizi.rdfutils.jena.SparqlBasedTester;
import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.util.CachedGraphWrapper;

/**
 * Several tests about 
 * {@link RDFExportUtils#normalizeNames(Collection, Predicate, Function) normalised pref/alt names}
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>9 May 2018</dd></dl>
 *
 */
public class PrefNameTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	/**
	 * The tests below are variants of this template.
	 *  
	 * @param truePrefName the name expected to be translated as unique preferred
	 * @param fakePrefName the name initially {@link ConceptName#isPreferred() marked as preferred} that is expected
	 * to be exported as altName.
	 * @param altName the name that is non-preferred since the beginning.
	 * 
	 */
	private void conceptNameTestTemplate ( String truePrefName, String fakePrefName, String altName )
	{
		// Test data
		
		ONDEXGraph g = new MemoryONDEXGraph ( "test" );
		
		CachedGraphWrapper gw = CachedGraphWrapper.getInstance ( g );
		
		ConceptClass cc = gw.getConceptClass ( "TestCC", "A Test CC", "A test concept class.", null );
		DataSource ds = gw.getDataSource ( "testDS", "Test Data Source", "A test data source." );
		EvidenceType ev = gw.getEvidenceType ( "testEvidence", "Test Evidence", "A test evidence type." );
		ONDEXConcept c = gw.getConcept ( "testConcept", "", "A test concept.", ds, cc, ev );
	
		c.createConceptName ( truePrefName, true );
		c.createConceptName ( fakePrefName, true );
		c.createConceptName ( altName, false );
				
		
		// RDF export 
		
		Model model = ModelFactory.createDefaultModel ();
		
		RDFExporter xport = new RDFExporter ();
		xport.setBatchJob ( xfact -> model.add ( xfact.getJenaModel () ) );
		xport.export ( g );			

		StringWriter sw = new StringWriter ();
		model.write ( sw, "TURTLE" );
		log.debug ( "Exported RDF:\n{}", sw.toString () );
		
		// Verify
		
		SparqlBasedTester tester = new SparqlBasedTester ( model, NamespaceUtils.asSPARQLProlog () );
		tester.ask ( "prefName not found!", String.format (
			"ASK { ?c bk:prefName '%s' }",
			truePrefName
		));
		tester.ask ( 
			"prefName count wrong!", 
			"ASK { ?c bk:prefName ?prefName } HAVING ( COUNT ( DISTINCT ?prefName ) = 1 )" 
		);
		tester.ask ( "fakePrefName not found!", String.format (
			"ASK { ?c bk:altName '%s' }",
			fakePrefName
		));
		tester.ask ( "true alt name not found!", String.format (
			"ASK { ?c bk:altName '%s' }",
			altName
		));
		tester.ask ( 
			"altName count wrong!", 
			"ASK { ?c bk:altName ?altName } HAVING ( COUNT ( DISTINCT ?altName ) = 2 )" 
		);		
	}
	
	
	/**
	 * Tests the selection of the preferred name, based on shortest string criteria
	 */
	@Test
	public void testPrefName () throws Exception {
		conceptNameTestTemplate ( "Pref Name", "Additional Pref Name", "Alt Name" );
	}
	
	
	/**
	 * Tests the selection of the preferred name, based on upper case priority
	 */	
	@Test
	public void testPrefNameUpperCase () throws Exception {
		conceptNameTestTemplate ( "NAME", "NaMe", "Alt Name" );
	}	

	/**
	 * Tests the selection of the preferred name, based on lexicographic order fallback
	 */
	@Test
	public void testPrefNameLexOrdering () throws Exception {
		conceptNameTestTemplate ( "aAAa", "name", "Alt Name" );
	}	
	
}
