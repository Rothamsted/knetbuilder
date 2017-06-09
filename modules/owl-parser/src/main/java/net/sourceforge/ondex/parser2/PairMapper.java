package net.sourceforge.ondex.parser2;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>6 Jun 2017</dd></dl>
 *
 */
public interface PairMapper<S1, S2, O>
{
	public O map ( S1 src1, S2 src2 );
}
