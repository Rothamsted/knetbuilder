package net.sourceforge.ondex.parser.utils;

import net.sourceforge.ondex.parser.TextMapper;

/**
 * A {@link ChainingMapper} for the {@link TextMapper} type.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Jun 2019</dd></dl>
 *
 */
public class ChainingTextMapper<S> extends ChainingMapper<S, String> implements TextMapper<S>
{
	public ChainingTextMapper ()
	{
	}
}
