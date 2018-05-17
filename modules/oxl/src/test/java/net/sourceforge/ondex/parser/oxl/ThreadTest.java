package net.sourceforge.ondex.parser.oxl;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.machinezoo.noexception.throwing.ThrowingRunnable;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.base.AbstractAttribute;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;

public class ThreadTest {

	@Test
	public void parsePoplar() throws Exception  {
		parseAndCountThreads("Poplar_DEBUG_Feb2010.xml.gz");
	}

	@Test
	public void parseGramene() throws Exception  {
		parseAndCountThreads("GrameneTraitOntology.xml.gz");
	}

	@Test
	public void parsePoplarMany() throws Exception  {
		parseManyAndCountThreads("Poplar_DEBUG_Feb2010.xml.gz", 4);
	}

	@Test
	public void parseGrameneMany() throws Exception  {
		parseManyAndCountThreads("GrameneTraitOntology.xml.gz", 4);
	}

	public void parseAndCountThreads(String resource) throws Exception 
	{
		checkCompressorThreads ( () -> 
		{
			ONDEXGraph theGraph = new MemoryONDEXGraph ( "PoplarKB" );

			String fileForResource = getResourcePath ( resource );

			Parser oxl = new Parser ();
			ONDEXPluginArguments pa = new ONDEXPluginArguments ( oxl.getArgumentDefinitions () );
			pa.setOption ( FileArgumentDefinition.INPUT_FILE, fileForResource );
			oxl.setArguments ( pa );
			oxl.setONDEXGraph ( theGraph );
			oxl.start ();
		});
	}

	public void parseManyAndCountThreads(String resource, int count) throws Exception 
	{		
		checkCompressorThreads ( () -> 
		{
			for ( int i = 0; i < count; i++ )
			{
				ONDEXGraph theGraph = new MemoryONDEXGraph ( "PoplarKB" );

				String fileForResource = getResourcePath ( resource );

				Parser oxl = new Parser ();
				ONDEXPluginArguments pa = new ONDEXPluginArguments ( oxl.getArgumentDefinitions () );
				pa.setOption ( FileArgumentDefinition.INPUT_FILE, fileForResource );
				oxl.setArguments ( pa );
				oxl.setONDEXGraph ( theGraph );
				oxl.start ();
			}
		});
	}

	
	private void checkCompressorThreads ( ThrowingRunnable task ) throws Exception
	{
		System.out.println ( "Before OXL parser" );
		Thread[] threadsBefore = new Thread [ Thread.activeCount () ];
		Thread.enumerate ( threadsBefore );
		
		// for (Thread thread : threadsBefore) {
		// System.out.println(thread.getName());
		// thread.dumpStack();
		// }

		task.run ();

		if ( AbstractAttribute.COMPRESSOR != null )
		{
			System.out.println ( "Closing all compressor threads" );
			AbstractAttribute.COMPRESSOR.shutdown ();
			while ( !AbstractAttribute.COMPRESSOR.isTerminated () )
				AbstractAttribute.COMPRESSOR.awaitTermination ( 300, TimeUnit.MILLISECONDS );
			AbstractAttribute.COMPRESSOR = null;
		}

		Thread.sleep ( 3000 );
		System.out.println ( "After OXL parser" );

		Thread[] threadsAfter = new Thread [ Thread.activeCount () ];
		Thread.enumerate ( threadsAfter );

		// fixme: refine check to fail if more than 1 thread is spawned
		// TODO: I don't get this comment (MB, 2018)
		Assert.assertEquals ( "No threads created or destroyed", threadsBefore.length, threadsAfter.length );
	}
	
	
	
	private String getResourcePath ( String resClassPath ) {
		URL url = ThreadTest.class.getClassLoader().getResource(resClassPath);
		if ("file".equals(url.getProtocol())) {
			return url.toString().substring("file:".length());
		} else {
			throw new IllegalArgumentException ( "Unable to file-ify resource URL: " + url );
		}
	}	
}