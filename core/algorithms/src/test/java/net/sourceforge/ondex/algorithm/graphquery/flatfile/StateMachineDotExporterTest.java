package net.sourceforge.ondex.algorithm.graphquery.flatfile;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UncheckedIOException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.xmlgraphics.util.WriterOutputStream;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import guru.nidi.graphviz.attribute.Arrow;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.RankDir;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.exception.type.ParsingFailedException;
import net.sourceforge.ondex.parser.oxl.Parser;
import uk.ac.ebi.utils.io.IOUtils;


/**
 * TODO: comment me!
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
		Pair<MutableGraph,String> dot = dotIt ( "test-state-machine", "dotter-basic-test" );
		log.info ( "Resulting graph:\n{}", dot.getRight () );
	}

	@Test
	public void testDirected () throws IOException
	{
		Pair<MutableGraph,String> dot = dotIt ( "directed-state-machine", "dotter-directed-test" );
		log.info ( "Resulting graph:\n{}", dot.getRight () );
	}

	
	@Test
	public void fooDotTest () throws FileNotFoundException, IOException
	{
		MutableGraph containerGraph = Factory
			.mutGraph ()
			.setStrict ( true )
			.setDirected ( true )
			.graphAttrs ()
			.add ( RankDir.LEFT_TO_RIGHT );
			
		MutableGraph digraph = Factory
			.mutGraph ( "directed" )
			.setStrict ( true )
			.setDirected ( true )
			.graphAttrs ()
			.add ( RankDir.LEFT_TO_RIGHT );
		
		digraph.add ( Factory.mutNode ( "A" ).addLink ( Factory.mutNode ( "B" ) ) );
		
		MutableGraph graph = Factory
			.mutGraph ( "undirected" )
			.setStrict ( true )
			.setDirected ( false )
			.graphAttrs ()
			.add ( RankDir.LEFT_TO_RIGHT );
		
		graph.add ( Factory.mutNode ( "A" ).addLink ( Factory.mutNode ( "C" ) ) );
		
		containerGraph.add ( digraph );
		containerGraph.add ( graph );
		
		Graphviz viz = Graphviz.fromGraph ( containerGraph );
		viz.render ( Format.PLAIN ).toOutputStream ( new FileOutputStream ( "target/dot-merge-test.dot" ) );
		viz.render ( Format.PNG ).toOutputStream ( new FileOutputStream ( "target/dot-merge-test.png" ) );
	}

	@Test
	public void fooDotTest1 () throws FileNotFoundException, IOException
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
		viz.render ( Format.PLAIN ).toOutputStream ( new FileOutputStream ( "target/dot-merge-test-1.dot" ) );
		viz.render ( Format.PNG ).toOutputStream ( new FileOutputStream ( "target/dot-merge-test-1.png" ) );
	}
	
	private Pair<MutableGraph,String> dotIt ( String smName, String outName )
	{
		return dotIt ( smName, outName, "data/xml/ondex_metadata.xml" );
	}

	
	private Pair<MutableGraph,String> dotIt ( String smName, String outName, String metadataPath )
	{
		ONDEXGraph metaGraph = null;
		try
		{
			metaGraph = new MemoryONDEXGraph ( "metadata" );
			Reader reader = IOUtils.openResourceReader ( metadataPath );
			Parser oxlParser = new Parser ();
			oxlParser.setONDEXGraph ( metaGraph );
			oxlParser.start ( reader );
		}
		catch ( ParsingFailedException | IOException ex ) {
			throw new RuntimeException ( "Internal error: " + ex.getMessage (), ex );
		}
		
		StateMachineDotExporter dotter = new StateMachineDotExporter ( "target/test-classes/" + smName + ".txt", metaGraph );

		MutableGraph dot = dotter.getGraph ();
		Graphviz viz = Graphviz.fromGraph ( dot );
		
		StringWriter sw = new StringWriter ();

		try
		{
			String baseOut = "target/" + outName;
			viz.render ( Format.PLAIN ).toOutputStream ( new FileOutputStream ( baseOut + ".dot" ) );
			viz.render ( Format.PNG ).toOutputStream ( new FileOutputStream ( baseOut + ".png" ) );
			viz.render ( Format.PLAIN ).toOutputStream ( new WriterOutputStream ( sw, "UTF-8" ) );
		}
		catch ( IOException ex ) {
			throw new UncheckedIOException ( "Internal error: " + ex.getMessage (), ex );
		}
		
		return Pair.of ( dot, sw.toString () );
	}
}
