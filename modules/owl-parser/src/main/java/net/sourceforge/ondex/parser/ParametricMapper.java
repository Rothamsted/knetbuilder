package net.sourceforge.ondex.parser;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>6 Apr 2017</dd></dl>
 *
 */
public interface ParametricMapper<OD,P,S>
{
	public OD map ( P parameter, S source );
}
