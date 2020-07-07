package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * A utility {@link PairMapper}, which of {@link #map(Object, Object, ONDEXGraph) map() method} passes its two source 
 * items to a {@link #getBaseMapper() base mapper} in reverse order.
 * 
 * An example where this can be useful is {@link ExploringMapper}, which, in case of top-bottom exploration of a tree
 * data source structure, invokes its {@link ExploringMapper.LinkerConfiguration#getMapper() relation mappers} passing  
 * first the parent node and then the child. If a mapper maps  a relation like 'is a', you want to wrap it into this
 * inverter (thus making 'is a' going from child to parent) and then configure the explorer with it.   
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Jul 2017</dd></dl>
 *
 */
public class InvertingPairMapper<S1, S2, O> extends DecoratingPairMapper<S1, S2, O>
{
	public InvertingPairMapper ()
	{
		this ( null );
	}

	public InvertingPairMapper ( PairMapper<S2, S1, O> baseMapper )
	{
		super ( baseMapper );
	}

	
	@Override
	public O map ( S1 src1, S2 src2, ONDEXGraph graph ) {
		return (O) this.getBaseMapper ().map ( src2, src1, graph );
	}

	@SuppressWarnings ( "unchecked" )
	@Override
	public PairMapper<S2, S1, O> getBaseMapper ()
	{
		return super.getBaseMapper ();
	}

	public <BS1, BS2, BO> void setBaseMapper ( PairMapper<BS1, BS2, BO> baseMapper )
	{
		super.setBaseMapper ( baseMapper );
	}
}
