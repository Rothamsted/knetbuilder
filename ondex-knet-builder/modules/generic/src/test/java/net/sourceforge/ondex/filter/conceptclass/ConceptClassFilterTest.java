package net.sourceforge.ondex.filter.conceptclass;

import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.oxl.Parser;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>14 Feb 2019</dd></dl>
 *
 */
public class ConceptClassFilterTest
{
	/**
	 * Tests the case of relation matching a DS criterion, but not a CC criterion.
	 * See <a href = "https://github.com/Rothamsted/ondex-knet-builder/issues/15">#15</a>.
	 */
	@Test
	public void testUnfilteredCCFilteredDS () throws InvalidPluginArgumentException
	{
		ONDEXGraph graph = Parser.loadOXL ( "target/test-classes/net/sourceforge/ondex/filter/conceptclass/unfiltered-cc-filtered-ds.oxl" );
		
		Filter filter = new Filter ();
		
		ONDEXPluginArguments args = new ONDEXPluginArguments ( filter.getArgumentDefinitions () );
		args.setOption ( Filter.TARGETCC_ARG, "Gene" );
		args.setOption ( Filter.FILTER_CV_ARG, "TAIR" );
		args.setOption ( Filter.EXCLUDE_ARG, true );
		filter.setArguments ( args );
		
		filter.setONDEXGraph ( graph );
		filter.start ();
		
		Assert.assertEquals ( "Test concept wasn't kept!", 2, filter.getVisibleConcepts ().size () );
		Assert.assertEquals ( "Test relation wasn't kept!", 1, filter.getVisibleRelations ().size () );
		Assert.assertEquals ( "Test concept wasn't kept!", 1, filter.getInVisibleConcepts ().size () );
		Assert.assertEquals ( "Test relation wasn't kept!", 1, filter.getInVisibleRelations ().size () );
	}
}
