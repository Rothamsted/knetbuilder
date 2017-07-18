package net.sourceforge.ondex.parser2;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Jul 2017</dd></dl>
 *
 */
public class InvertingPairMapper<S1, S2, O> extends FilterPairMapper<S1, S2, O>
{
	public InvertingPairMapper () {
		super ();
	}
	
	@Override
	@SuppressWarnings ( "unchecked" )
	public O map ( S1 src1, S2 src2, ONDEXGraph graph ) {
		return (O) this.getBaseMapper ().map ( src2, src1 );
	}

	@Override
	public <BS1, BS2, BO> PairMapper<BS1, BS2, BO> getBaseMapper ()
	{
		return super.getBaseMapper ();
	}

	@Override
	public <BS1, BS2, BO> void setBaseMapper ( PairMapper<BS1, BS2, BO> baseMapper )
	{
		super.setBaseMapper ( baseMapper );
	}
	
}
