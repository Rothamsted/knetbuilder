package net.sourceforge.ondex.parser;

/**
 * A scaffolding utility, which allows you to implement a {@link PairMapper} by leveraging the functionality available from a 
 * base mapper. This is obviously based on the <a href = 'https://en.wikipedia.org/wiki/Decorator_pattern>decorator pattern</a>.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Jul 2017</dd></dl>
 *
 */
public abstract class DecoratingPairMapper<S1, S2, O> implements PairMapper<S1, S2, O>
{
	protected PairMapper<?, ?, ?> baseMapper;
	
	protected DecoratingPairMapper ()
	{
		this ( null );
	}

	protected DecoratingPairMapper ( PairMapper<?, ?, ?> baseMapper )
	{
		this.baseMapper = baseMapper;
	}
	

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
