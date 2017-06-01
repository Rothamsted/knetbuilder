package net.sourceforge.ondex.parser;

/**
 *
 * A marker interface that maps ONDEX entity/es of type O from a source of type S.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Apr 2017</dd></dl>
 *
 */
public interface Mapper<O, S>
{
	public O map ( S source );
}
