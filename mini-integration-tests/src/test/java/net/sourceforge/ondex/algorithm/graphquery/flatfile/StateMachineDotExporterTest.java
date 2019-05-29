package net.sourceforge.ondex.algorithm.graphquery.flatfile;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.xmlgraphics.util.WriterOutputStream;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import guru.nidi.graphviz.attribute.Arrow;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.RankDir;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.exception.type.ParsingFailedException;
import net.sourceforge.ondex.parser.oxl.Parser;
import uk.ac.ebi.utils.io.IOUtils;


/**
 * Tests for {@link StateMachineDotExporter}. These would be unit tests in the algorithms module, but the Ondex 
 * arrangement is nasty, these test would require the OXL loader, which creates a circular dependency on algorithms
 * itself.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>27 May 2019</dd></dl>
 *
 */
public class StateMachineDotExporterTest
{	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Test
	public void testBasics () throws IOException
	{
		dotIt ( 
			"test-state-machine", "dotter-basic-test", 
		  "Gene(1)", "enc", "BioProc(3)", false,
		  "TO(7)", "asso_wi", "MolFunc(9)", false,
		  "TO(7)", "asso_wi", "Enzyme(8)", false
		);
	}

	@Test
	public void testDirected () throws IOException
	{
		dotIt ( 
			"directed-state-machine", "dotter-directed-test", 
		  "Gene(1)", "enc", "BioProc(3)", false,
		  "BioProc(3)", "part_of", "Path(5)", false,
		  "BioProc(3)", "pub_in", "Publication(6)", false,
		  "TO(7)", "asso_wi", "MolFunc(9)", true,
		  "TO(7)", "asso_wi", "Enzyme(8)", true
		);
	}

	@Test
	public void testLenConstraints () throws IOException
	{
		dotIt ( 
			"constrained-state-machine", "dotter-constrained-test",
		  "Gene(1)", "asso_wi(<=5)\npub_in(<=4)", "Publication(6)", false,
		  "Publication(6)", "asso_wi", "TO(7)", false
		);
	}

	@Test
	public void testLoop () throws IOException
	{
		dotIt ( 
			"loop-state-machine", "dotter-loop-test",
		  "BioProc(3)", "part_of(<=5)", "BioProc(3)", false,
		  "BioProc(3)", "is_part_of(<=7)\npart_of(<=9)", "Path(5)", false
		);
	}
	
	@Test
	public void testArabidopsis () throws IOException
	{
		dotItWithOndexDefs ( 
			"ara-state-machine", "dotter-ara-test", "wheat-metadata.xml",
			"Protein(10)", "h_s_s(<=4)\northo(<=4)\nxref(<=4)", "Protein(10)", false,
			"Protein(10)", "genetic(<=6)\nphysical(<=6)", "Protein(10)", true,
			"Protein(10)", "genetic(<=6)\nphysical(<=6)", "Protein(7)", true,
			"Reaction(13)", "part_of", "Path(14)", false,
			"Gene(1)", "has_variation", "SNP(15)", false
		);
	}
	
	
	@Test @Ignore ( "Just a scrap try, not a real unit test" )
	public void testHybridViz () throws FileNotFoundException, IOException
	{
		MutableGraph graph = Factory
			.mutGraph ()
			.setDirected ( true )
			.graphAttrs ()
			.add ( RankDir.LEFT_TO_RIGHT );
		
		MutableNode a = Factory.mutNode ( "A" );
		MutableNode b = Factory.mutNode ( "B" );
		MutableNode c = Factory.mutNode ( "C" );
		
		graph.add ( a, b, c );
		a.addLink ( Factory.to ( b ).add ( Arrow.NONE ) );
		a.addLink ( Factory.to ( c ).add ( Arrow.NORMAL ) );
				
		Graphviz viz = Graphviz.fromGraph ( graph );
		viz.render ( Format.PLAIN ).toOutputStream ( new FileOutputStream ( "target/dot-hyb-test.dot" ) );
		viz.render ( Format.PNG ).toOutputStream ( new FileOutputStream ( "target/dot-hyb-test.png" ) );
	}
	
	@Test @Ignore ( "Just a scrap try, not a real unit test" )
	public void testHybridVizSubGraphs () throws FileNotFoundException, IOException
	{
		MutableGraph containerGraph = Factory.mutGraph ()
				.setDirected ( true )
				.graphAttrs ().add ( RankDir.LEFT_TO_RIGHT );

			// Shoul yield A->B
			MutableGraph digraph = Factory.mutGraph ().setDirected ( true ).graphAttrs ().add ( RankDir.LEFT_TO_RIGHT );
			digraph.add ( Factory.mutNode ( "A" ).addLink ( Factory.mutNode ( "B" ) ) );

			// Should yield A-C
			MutableGraph graph = Factory.mutGraph ().setDirected ( false ).graphAttrs ().add ( RankDir.LEFT_TO_RIGHT );
			graph.add ( Factory.mutNode ( "A" ).addLink ( Factory.mutNode ( "C" ) ) );

			containerGraph.add ( digraph );
			containerGraph.add ( graph );

			Graphviz viz = Graphviz.fromGraph ( containerGraph );
			viz.render ( Format.PLAIN ).toOutputStream ( new FileOutputStream ( "target/dot-hyb-subgraphs-test.dot" ) );
			viz.render ( Format.PNG ).toOutputStream ( new FileOutputStream ( "target/dot-hyb-subgraphs-test.png" ) );		
	}
	
	
	private Pair<MutableGraph,String> dotIt ( String smName, String outName, Object... expectedEdges )
	{
		return dotItWithOndexDefs ( smName, outName, "data/xml/ondex_metadata.xml", expectedEdges );
	}

	
	/**
	 * @param expectedEdges every 4 elements one edge, see the code for details
	 */
	private Pair<MutableGraph,String> dotItWithOndexDefs ( 
		String smName, String outName, String metadataPath,
		Object... expectedEdges
	)
	{
		ONDEXGraph metaGraph = null;
		try
		{
			// The FF parser needs a source of metadata defs
			metaGraph = new MemoryONDEXGraph ( "metadata" );
			Reader reader = IOUtils.openResourceReader ( metadataPath );
			Parser oxlParser = new Parser ();
			oxlParser.setONDEXGraph ( metaGraph );
			oxlParser.start ( reader );
		}
		catch ( ParsingFailedException | IOException ex ) {
			throw new RuntimeException ( "Internal error: " + ex.getMessage (), ex );
		}
		
		StateMachineDotExporter dotter = new StateMachineDotExporter (
			"target/test-classes/state-machine-dotter/" + smName + ".txt", metaGraph 
		);

		MutableGraph dot = dotter.getGraph ();
		Graphviz viz = Graphviz.fromGraph ( dot );
		
		StringWriter sw = new StringWriter ();
		
		try
		{
			// Some useful diagnostic output
			if ( log.isTraceEnabled () )
			{
				String baseOut = "target/" + outName;
				viz.render ( Format.PLAIN ).toOutputStream ( new FileOutputStream ( baseOut + ".dot" ) );
				viz.render ( Format.PNG ).toOutputStream ( new FileOutputStream ( baseOut + ".png" ) );
				viz.render ( Format.PLAIN ).toOutputStream ( new WriterOutputStream ( sw, "UTF-8" ) );
			}
		}
		catch ( IOException ex ) {
			throw new UncheckedIOException ( "Internal error: " + ex.getMessage (), ex );
		}
		
		// Verify if the expected edges are here
		//
		for ( int i = 0; i < expectedEdges.length; i++ )
		{
			String src = (String) expectedEdges [ i ];
			String edge = (String) expectedEdges [ ++i ];
			String dst = (String) expectedEdges [ ++i ];
			boolean isDirected = (boolean) expectedEdges [ ++i ];
			
			// All the links taken from all the nodes. The guru model is quite weird and this seems the only way
			//
			Stream<Link> links = dot
				.nodes ()
				.stream ()
				.map ( MutableNode::asLinkSource )
				.flatMap ( node -> node.links ().stream () );

			// Filter edges compatible with current probe  
			Link foundLink = links
				.peek ( link -> 
					log.trace ( 
						"CURRENT LINK: {} -- {} -- {} -- {}",
						((Label) link.get ( "label" )).value (),
						link.get ( "arrowhead" ),
						((MutableNode) link.from () ).name ().value (),
						((MutableNode) link.to () ).name ().value ()
					)
				)
				.filter ( link -> ! ( isDirected ^ "normal".equals ( link.get ( "arrowhead" ) ) ) )
				.filter ( link -> edge.equals ( ((Label) link.get ( "label" )).value () ) )
				.filter ( link -> src.equals ( ((MutableNode) link.from () ).name ().value () ) )
				.filter ( link -> dst.equals ( ((MutableNode) link.to () ).name ().value () ) )
				.findAny ()
				.orElse ( null );
			
			Assert.assertNotNull (
				String.format (
					"edge <%s>( <%s> %s <%s> ) not found!", 
					escapeJava ( edge ), src, isDirected ? "-" : "->", dst
				),
				foundLink
			);
		}
		
		return Pair.of ( dot, sw.toString () );
	}
}
