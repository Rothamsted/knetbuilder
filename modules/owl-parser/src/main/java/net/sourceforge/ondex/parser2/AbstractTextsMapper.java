package net.sourceforge.ondex.parser2;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>7 Jul 2017</dd></dl>
 *
 */
public class AbstractTextsMapper<S, SI> extends ScannerMapper<S, SI, String> implements TextsMapper<S> 
{
	protected AbstractTextsMapper ( Scanner<S, SI> scanner, TextMapper<SI> sourceItemMapper )
	{
		super ( scanner, sourceItemMapper );
	}
}
