package net.sourceforge.ondex.parser.oxl;

import static junit.framework.Assert.assertEquals;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import junit.framework.AssertionFailedError;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.base.AbstractAttribute;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;

import org.junit.Test;

public class ThreadTest {

	@Test
	public void parsePoplar() throws PluginConfigurationException {
		parseAndCountThreads("Poplar_DEBUG_Feb2010.xml.gz");
	}

	@Test
	public void parseGramene() throws PluginConfigurationException {
		parseAndCountThreads("GrameneTraitOntology.xml.gz");
	}

	@Test
	public void parsePoplarMany() throws PluginConfigurationException {
		parseManyAndCountThreads("Poplar_DEBUG_Feb2010.xml.gz", 4);
	}

	@Test
	public void parseGrameneMany() throws PluginConfigurationException {
		parseManyAndCountThreads("GrameneTraitOntology.xml.gz", 4);
	}

	public void parseAndCountThreads(String resource)
			throws PluginConfigurationException {

		System.out.println("Before OXL parser");

		Thread[] threadsBefore = new Thread[Thread.activeCount()];
		Thread.enumerate(threadsBefore);

		ONDEXGraph theGraph = new MemoryONDEXGraph("PoplarKB");

		String fileForResource = guessFileForResourceUsingMagicAndHope(resource);

		Parser oxl = new Parser();
		ONDEXPluginArguments pa = new ONDEXPluginArguments(
				oxl.getArgumentDefinitions());
		pa.setOption(FileArgumentDefinition.INPUT_FILE, fileForResource);
		oxl.setArguments(pa);
		oxl.setONDEXGraph(theGraph);
		oxl.start();

		// close all compressor threads and wait until finished
		if (AbstractAttribute.COMPRESSOR != null) {
			System.out.println("Closing all compressor threads");
			AbstractAttribute.COMPRESSOR.shutdown();
			while (!AbstractAttribute.COMPRESSOR.isTerminated()) {
				synchronized (this) {
					try {
						this.wait(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			AbstractAttribute.COMPRESSOR = null;
		}

		System.out.println("After OXL parser");

		Thread[] threadsAfter = new Thread[Thread.activeCount()];
		Thread.enumerate(threadsAfter);

		// there is no thread leak - the gds compression thread may be spawned,
		// however
		// fixme: refine check to fail if more than 1 thread is spawned
		try {
			assertEquals("No threads created or destroyed",
					threadsBefore.length, threadsAfter.length);
		} catch (AssertionFailedError e) {
			System.out.println("threads before: "
					+ Arrays.asList(threadsBefore));
			System.out.println("threads after: " + Arrays.asList(threadsAfter));
			throw e;
		}
	}

	public void parseManyAndCountThreads(String resource, int count)
			throws PluginConfigurationException {

		System.out.println("Before OXL parser");

		Thread[] threadsBefore = new Thread[Thread.activeCount()];
		Thread.enumerate(threadsBefore);
		// for (Thread thread : threadsBefore) {
		// System.out.println(thread.getName());
		// thread.dumpStack();
		// }

		for (int i = 0; i < count; i++) {
			ONDEXGraph theGraph = new MemoryONDEXGraph("PoplarKB");

			String fileForResource = guessFileForResourceUsingMagicAndHope(resource);

			Parser oxl = new Parser();
			ONDEXPluginArguments pa = new ONDEXPluginArguments(
					oxl.getArgumentDefinitions());
			pa.setOption(FileArgumentDefinition.INPUT_FILE, fileForResource);
			oxl.setArguments(pa);
			oxl.setONDEXGraph(theGraph);
			oxl.start();
		}

		// close all compressor threads and wait until finished
		if (AbstractAttribute.COMPRESSOR != null) {
			System.out.println("Closing all compressor threads");
			AbstractAttribute.COMPRESSOR.shutdown();
			while (!AbstractAttribute.COMPRESSOR.isTerminated()) {
				synchronized (this) {
					try {
						this.wait(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			AbstractAttribute.COMPRESSOR = null;
		}

		System.out.println("After OXL parser");

		Thread[] threadsAfter = new Thread[Thread.activeCount()];
		Thread.enumerate(threadsAfter);
		// for (Thread thread : threadsAfter) {
		// System.out.println(thread.getName());
		// thread.dumpStack();
		// }

		// fixme: refine check to fail if more than 1 thread is spawned
		try {
			assertEquals("No threads created or destroyed",
					threadsBefore.length, threadsAfter.length);
		} catch (AssertionFailedError e) {
			System.out.println("threads before: "
					+ Arrays.asList(threadsBefore));
			System.out.println("threads after: " + Arrays.asList(threadsAfter));
			throw e;
		}
	}

	private String guessFileForResourceUsingMagicAndHope(String resource) {
		URL url = ThreadTest.class.getClassLoader().getResource(resource);
		if ("file".equals(url.getProtocol())) {
			return url.toString().substring("file:".length());
		} else {
			throw new Error("Unable to file-ify resource URL: " + url);
		}
	}

}