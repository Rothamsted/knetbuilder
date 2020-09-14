package net.sourceforge.ondex.parser.oxl;

import static java.lang.System.out;

import java.net.URL;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.base.AbstractAttribute;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;

/**
 * MB (2020): was a weird test on the number of threads before and after the run of
 * the {@link AbstractAttribute#COMPRESSOR attribute compressor}. Since that doesn't make 
 * sense (what do you want to test?! The JDK internals?!), I changed the names and kept 
 * the test for the purpose of ensuring that the parsing of a number of OXLs goes fine.
 *
 */
public class MoreCasesOXLParserTest
{
	// TODO: set a config file.
	private Logger log = Logger.getLogger ( this.getClass () );
	
	@Test
	public void parsePoplar () throws Exception
	{
		parseHelper ( "Poplar_DEBUG_Feb2010.xml.gz" );
	}

	@Test
	public void parseGramene () throws Exception
	{
		parseHelper ( "GrameneTraitOntology.xml.gz" );
	}

	@Test
	public void parsePoplarMany () throws Exception
	{
		parseHelper ( "Poplar_DEBUG_Feb2010.xml.gz" );
	}

	@Test
	public void parseGrameneMany () throws Exception
	{
		parseHelper ( "GrameneTraitOntology.xml.gz" );
	}

	@Test @Ignore ( "Not a real unit test" )
	public void parseNeuroSpora202007 () throws Exception
	{
		var graph = parseHelper ( "file:///Users/brandizi/tmp/ondex/NeurosporaKNET_v43.xml" );
		out.println ( "Concepts: " + graph.getConcepts ().size () );
	}
	
	private ONDEXGraph parseHelper ( String resource ) throws Exception
	{
		ONDEXGraph graph = new MemoryONDEXGraph ( "UnitTestGraph" );
	
		String fileForResource = getResourcePath ( resource );
	
		Parser oxl = new Parser ();
		ONDEXPluginArguments pa = new ONDEXPluginArguments ( oxl.getArgumentDefinitions () );
		pa.setOption ( FileArgumentDefinition.INPUT_FILE, fileForResource );
		oxl.setArguments ( pa );
		oxl.setONDEXGraph ( graph );
		oxl.start ();
		
		return graph;
	}


	private String getResourcePath ( String resPath )
	{
		if ( resPath.startsWith ( "file://" ) )
			return resPath.substring ( "file://".length () );
			
		URL url = MoreCasesOXLParserTest.class.getClassLoader ().getResource ( resPath );
		if ( "file".equals ( url.getProtocol () ) )
			return url.toString ().substring ( "file:".length () );
		
		throw new IllegalArgumentException ( "Unable to file-ify resource URL: " + url );
	}
}
