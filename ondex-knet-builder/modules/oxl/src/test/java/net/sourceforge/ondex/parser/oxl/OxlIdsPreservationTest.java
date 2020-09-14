package net.sourceforge.ondex.parser.oxl;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.util.ONDEXGraphUtils;
import net.sourceforge.ondex.export.oxl.Export;
import uk.ac.ebi.utils.xml.XPathReader;

/**
 * Tests {@link ONDEXGraphUtils#isLoadingMode} enabled by the {@link Parser OXL parser}.
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>14 Sep 2020</dd></dl>
 *
 */
public class OxlIdsPreservationTest
{

	@Test
	public void testOXLIdsPreservation () throws FileNotFoundException
	{
		ONDEXGraph graph = new MemoryONDEXGraph ( "test" );
		
		var ccA = ONDEXGraphUtils.getOrCreateConceptClass ( graph, "A" );
		var ccB = ONDEXGraphUtils.getOrCreateConceptClass ( graph, "B" );
		var ds = ONDEXGraphUtils.getOrCreateDataSource ( graph, "fooDS" );
		var ev = Set.of ( ONDEXGraphUtils.getOrCreateEvidenceType ( graph, "fooEvidence" ) );
		var relatedTo = ONDEXGraphUtils.getOrCreateRelationType ( graph, "related-to" );
		
		var ca = graph.createConcept ( "concept a", "", "", ds, ccA, ev );
		var cb = graph.createConcept ( "concept b", "", "", ds, ccB, ev );
		var cc = graph.createConcept ( "concept c", "", "", ds, ccB, ev );
		
		var rel = graph.createRelation ( ca, cb, relatedTo, ev );

		String oxlPath = "target/testOXLIdsPreservation.xml";
		Export.exportOXL ( graph, oxlPath, false, true );
		
		// Get the IDs in the XML
		XPathReader xpr = new XPathReader ( new FileReader ( oxlPath ) );
		Function<String, Integer> xfinder = pid -> xpr.readInt ( String.format ( "//concept/pid[text()='%s']/../id", pid ));
		List<ONDEXConcept> concepts = List.of ( ca, cb, cc );
		Map<Integer, ONDEXConcept> xmlIds2concepts = concepts.stream ()
			.collect ( Collectors.toMap ( c -> xfinder.apply ( c.getPID() ), Function.identity () ) );
				
		// Reload and check the IDs
		var oxlGraph = Parser.loadOXL ( oxlPath );
		
		assertEquals ( "Wrong concepts count in the OXL!", graph.getConcepts().size (), oxlGraph.getConcepts ().size () );
		assertEquals ( "Wrong relations count in the OXL!", graph.getRelations().size (), oxlGraph.getRelations ().size () );
		
		xmlIds2concepts
		.forEach ( (xmlid, concept) ->
		{
			ONDEXConcept xmlConcept = oxlGraph.getConcept ( xmlid );
			assertNotNull ( format ( "XML ID not retained for %d, %s!", xmlid, concept.getPID () ), xmlConcept );
			assertEquals ( format ( "Bad XML ID %d!", xmlid ), concept.getPID (), xmlConcept.getPID () ); 
		});
		
		var xmlRel = oxlGraph.getRelations ().iterator ().next ();
		assertEquals ( "OXL relation is wrong (from)!", rel.getFromConcept ().getPID (), xmlRel.getFromConcept ().getPID () );
		assertEquals ( "OXL relation is wrong (to)!", rel.getToConcept ().getPID (), xmlRel.getToConcept ().getPID () );
	}
}
