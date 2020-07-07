package net.sourceforge.ondex.parser.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 May 2018</dd></dl>
 *
 */
public class WordCapitalizerTest
{
	@Test
	public void testBasics ()
	{
		WordCapitalizer wc = new WordCapitalizer ();
		assertEquals ( "Bad result!", "Biological_Process", wc.apply ( "biological_process" ) );
		assertEquals ( "Bad result (multiple separators)!", "An Example_Identifier", wc.apply ( "an example_identifier" ) );
		assertEquals ( "Bad result (separator at the end)!", "Biological_Process_", wc.apply ( "biological_process_" ) );
		assertEquals ( "Bad result (contiguous separators)!", "Biological_ Process", wc.apply ( "biological_ process" ) );
		assertEquals ( "Bad result (empty string)!", "", wc.apply ( "" ) );
	}
}
