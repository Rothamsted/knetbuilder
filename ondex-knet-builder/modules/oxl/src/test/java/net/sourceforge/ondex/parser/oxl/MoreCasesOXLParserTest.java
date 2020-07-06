package net.sourceforge.ondex.parser.oxl;

import java.net.URL;

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

	
	private void parseHelper ( String resource ) throws Exception
	{
		ONDEXGraph theGraph = new MemoryONDEXGraph ( "UnitTestGraph" );
	
		String fileForResource = getResourcePath ( resource );
	
		Parser oxl = new Parser ();
		ONDEXPluginArguments pa = new ONDEXPluginArguments ( oxl.getArgumentDefinitions () );
		pa.setOption ( FileArgumentDefinition.INPUT_FILE, fileForResource );
		oxl.setArguments ( pa );
		oxl.setONDEXGraph ( theGraph );
		oxl.start ();
	}


	private String getResourcePath ( String resClassPath )
	{
		URL url = MoreCasesOXLParserTest.class.getClassLoader ().getResource ( resClassPath );
		if ( "file".equals ( url.getProtocol () ) )
			return url.toString ().substring ( "file:".length () );
		
		throw new IllegalArgumentException ( "Unable to file-ify resource URL: " + url );
	}
}