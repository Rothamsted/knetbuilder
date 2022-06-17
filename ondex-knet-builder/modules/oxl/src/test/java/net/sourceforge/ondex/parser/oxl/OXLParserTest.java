/**
 *
 */
package net.sourceforge.ondex.parser.oxl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jakarta.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.export.oxl.Export;

/**
 * @author hindlem
 * @author Marco Brandizi (reviews in 2020)
 */
public class OXLParserTest
{
	private ONDEXGraph og;
	private File testfile;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp () throws Exception
	{
		og = new MemoryONDEXGraph ( "test" );
		testfile = new File ( System.getProperty ( "java.io.tmpdir" ) + File.separator + "testoxl.xml" );
		testfile.deleteOnExit ();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown () throws Exception
	{
		testfile.delete ();
	}


	@Test
	public void testRoundTrip () throws JAXBException, XMLStreamException, IOException, PluginConfigurationException
	{

		// create test graph

		DataSource dataSource = og.getMetaData ().getFactory ().createDataSource ( "cv" );
		ConceptClass cc = og.getMetaData ().getFactory ().createConceptClass ( "cc" );
		EvidenceType et = og.getMetaData ().getFactory ().createEvidenceType ( "et" );

		ONDEXConcept c = og.getFactory ().createConcept ( "concept", dataSource, cc, et );

		AttributeName an = og.getMetaData ().getFactory ().createAttributeName ( "an", List.class );
		List<Integer> list = IntStream.rangeClosed ( 1, 3 )
			.boxed ()
			.collect ( Collectors.toList () );
		c.createAttribute ( an, list, false );
		
		AttributeName colourAtt = og.getMetaData ().getFactory ().createAttributeName ( "colour", Color.class );
		Color colour = Color.WHITE;
		c.createAttribute ( colourAtt, colour, false );

		ONDEXConcept c2 = og.getFactory ().createConcept ( "concept2", dataSource, cc, et );

		RelationType rt = og.getMetaData ().getFactory ().createRelationType ( "rt" );

		ONDEXRelation relation = og.getFactory ().createRelation ( c, c2, rt, et );

		relation.createAttribute ( an, list, false );
		relation.createAttribute ( colourAtt, colour, false );

		// export

		Export export = new Export ();

		ONDEXPluginArguments ea = new ONDEXPluginArguments ( export.getArgumentDefinitions () );
		ea.setOption ( FileArgumentDefinition.EXPORT_FILE, testfile.getAbsolutePath () );
		ea.setOption ( net.sourceforge.ondex.export.oxl.ArgumentNames.EXPORT_AS_ZIP_FILE, false );

		export.setONDEXGraph ( og );
		export.setArguments ( ea );
		export.start ();

		// import

		ONDEXGraph g2 = new MemoryONDEXGraph ( "test2" );

		Parser parser = new Parser ();

		ONDEXPluginArguments pa = new ONDEXPluginArguments ( parser.getArgumentDefinitions () );
		pa.setOption ( FileArgumentDefinition.INPUT_FILE, testfile.getAbsolutePath () );

		parser.setONDEXGraph ( g2 );
		parser.setArguments ( pa );
		parser.start ();

		// check results

		assertEquals ( "There should be two concepts.", 2, g2.getConcepts ().size () );
		
		Consumer<ONDEXEntity> attrTester = odxe ->
		{
			Attribute attribute = odxe.getAttribute ( an );
			@SuppressWarnings ( "unchecked" )
			List<Integer> readList = (List<Integer>) attribute.getValue ();
			
			assertEquals ( "There should be three values in the list attribute.", 3, readList.size () );
			IntStream.rangeClosed ( 1, 3 )
					.forEach ( v -> assertTrue ( "attribute value " + v + " not found", readList.contains ( 1 ) ) );

			Attribute attribute2 = odxe.getAttribute ( colourAtt );
			assertEquals ( "WHITE attributed not found", Color.WHITE, attribute2.getValue () );
		};
		
		
		for ( ONDEXConcept concept : g2.getConcepts () )
		{
			if ( concept.getPID ().equals ( c.getPID () ) )
				attrTester.accept ( concept );
			else
				assertEquals ( "Concept shouldn't have any attribute", 0, concept.getAttributes ().size () );
		}

		assertEquals ( "There should be one relation.", 1, g2.getRelations ().size () );
		for ( ONDEXRelation relationT : g2.getRelations () )
		{
			assertEquals ( "Relation should have 2 attributes", 2, relationT.getAttributes ().size () );
			attrTester.accept ( relationT );
		}
	}

}