package net.sourceforge.ondex.parser.utils;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.Mapper;

/**
 * A facility that does nothing, its {@link #map(Object, ONDEXGraph)} method simply returns its input.
 * This can be useful in Spring configurations, to return values like strings as-is.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Jul 2017</dd></dl>
 *
 */
public class IdentityMapper<S> implements Mapper<S, S>
{
	@Override
	public S map ( S source, ONDEXGraph graph )
	{
		return source;
	}
}
