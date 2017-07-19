package net.sourceforge.ondex.parser2;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 */
public class ConstPairMapper<S1, S2, C, O> implements PairMapper<S1, S2, O>
{
	private C value;
	
	public ConstPairMapper ( C value ) {
		this.setValue ( value );
	}

	public ConstPairMapper () {
		this ( null );
	}

	
	@Override
	@SuppressWarnings ( "unchecked" )
	public O map ( S1 src1, S2 src2, ONDEXGraph graph ) {
		return (O) this.getValue ();
	}

	
	public C getValue () {
		return value;
	}

	public void setValue ( C value ) {
		this.value = value;
	}
}
