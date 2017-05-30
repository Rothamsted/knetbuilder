package net.sourceforge.ondex.parser2;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 May 2017</dd></dl>
 *
 */
public interface Mapper<S, O>
{
	public O map ( S source );
}
