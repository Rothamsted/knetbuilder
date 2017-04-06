package net.sourceforge.ondex.parser.owl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;

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
	public static final String FOO_NS = "http://www.example.com/foo#";
	
	@Test
	public void testBasics ()
	{
		OntModel model = ModelFactory.createOntologyModel ();
		
		String topClsId = "TopClass";
		OntClass topCls = model.createClass ( FOO_NS + topClsId );
		topCls.setLabel ( "Top Class", "en" );
		topCls.setComment ( "Top Class Description", "en" );
		
		String clsId = "ClassA";
		OntClass ontCls = model.createClass ( FOO_NS + clsId );
		ontCls.setLabel ( "Class A Label", "en" );
		ontCls.setComment ( "Class A Description", "en" );

		topCls.addSubClass ( ontCls );
		
		OWLConceptClassMapper ccmap = new OWLConceptClassMapper ();
		ccmap.setClassUri ( FOO_NS + "TopClass" );
		OWLConceptMapper conceptMap = new OWLConceptMapper ();
		conceptMap.setTopClassMapper ( ccmap );

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
	
}
