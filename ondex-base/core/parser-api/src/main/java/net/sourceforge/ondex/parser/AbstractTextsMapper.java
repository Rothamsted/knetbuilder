package net.sourceforge.ondex.parser;

/**
 * Implements a {@link TextsMapper} by means of a {@link ScannerMapper}, i.e., by accepting a {@link Scanner}
 * plus a {@link TextsMapper} that maps each item returned by the scanner to a string. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>7 Jul 2017</dd></dl>
 *
 */
public abstract class AbstractTextsMapper<S, SI> extends ScannerMapper<S, SI, String> implements TextsMapper<S> 
{
	protected AbstractTextsMapper ( Scanner<S, SI> scanner, TextMapper<SI> sourceItemMapper )
	{
		super ( scanner, sourceItemMapper );
	}
}
