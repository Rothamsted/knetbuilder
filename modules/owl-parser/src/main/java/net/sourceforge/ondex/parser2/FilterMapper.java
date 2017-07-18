package net.sourceforge.ondex.parser2;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>18 Jul 2017</dd></dl>
 *
 * @param <S>
 * @param <O>
 */
public abstract class FilterMapper<S, O>
{
	protected Mapper<?, ?> baseMapper;

	public FilterMapper ()
	{
		super ();
	}

	@SuppressWarnings ( "unchecked" )
	protected <BS, BO> Mapper<BS, BO> getBaseMapper () {
		return (Mapper<BS, BO>) baseMapper;
	}

	protected <BS, BO> void  setBaseMapper ( Mapper<BS, BO> baseMapper ) {
		this.baseMapper = baseMapper;
	}
}