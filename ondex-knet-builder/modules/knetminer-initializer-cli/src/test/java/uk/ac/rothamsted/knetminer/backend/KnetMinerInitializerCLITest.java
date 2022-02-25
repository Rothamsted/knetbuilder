package uk.ac.rothamsted.knetminer.backend;

import org.junit.Test;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Feb 2022</dd></dl>
 *
 */
public class KnetMinerInitializerCLITest
{
	@Test
	public void testBasics ()
	{
		// TODO: a real invocation, passing the same OXL in other tests (this time it's the CLI wrapper that
		// loads it) and other params, in the form of CLI argument strings. See OndexGraphDescriptorCLITest
		// or MiniPlugInCLITest for similar examples.
		KnetMinerInitializerCLI.invoke ( "-h" );
	}
}
