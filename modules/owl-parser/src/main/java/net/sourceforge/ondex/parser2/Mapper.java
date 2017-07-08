package net.sourceforge.ondex.parser2;

/**
 * A mapper translates a data source S into some kind of output O. The latter can be either an ONDEX element, or
 * an intermediate type that is used to build ONDEX elements (e.g., a string, or a number).
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 May 2017</dd></dl>
 *
 */
@FunctionalInterface
public interface Mapper<S, O>
{
	public O map ( S source );
}
