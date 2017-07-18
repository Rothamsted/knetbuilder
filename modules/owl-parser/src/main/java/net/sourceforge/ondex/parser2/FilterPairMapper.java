package net.sourceforge.ondex.parser2;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Jul 2017</dd></dl>
 *
 */
public abstract class FilterPairMapper<S1, S2, O> implements PairMapper<S1, S2, O>
{
	protected PairMapper<?, ?, ?> baseMapper;

	@SuppressWarnings ( "unchecked" )
	protected <BS1, BS2, BO> PairMapper<BS1, BS2, BO> getBaseMapper ()
	{
		return (PairMapper<BS1, BS2, BO>) baseMapper;
	}

	protected <BS1, BS2, BO> void setBaseMapper ( PairMapper<BS1, BS2, BO> baseMapper )
	{
		this.baseMapper = baseMapper;
	}
}
