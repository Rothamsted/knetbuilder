package net.sourceforge.ondex.parser;

/**
 * A marker interface that identify the mapping from a source of type S and a parameter of type P (used to fetch data
 * in S) to ONDEX entitity/es of type O.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Apr 2017</dd></dl>
 *
 */
public interface ParametricMapper<O, P, S>
{
	public O map ( P param, S source );
}
