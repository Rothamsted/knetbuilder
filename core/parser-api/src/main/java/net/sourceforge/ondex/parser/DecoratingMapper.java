package net.sourceforge.ondex.parser;

/**
 * A scaffolding utility, which allows you to implement a {@link Mapper} by leveraging the functionality available from a 
 * base mapper. This is obviously based on the <a href = 'https://en.wikipedia.org/wiki/Decorator_pattern>decorator pattern</a>.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>18 Jul 2017</dd></dl>
 *
 * @param <S>
 * @param <O>
 */
public abstract class DecoratingMapper<S, O> implements Mapper<S, O>
{
	protected Mapper<?, ?> baseMapper;

	public DecoratingMapper ()
	{
		this ( null );
	}

	protected DecoratingMapper ( Mapper<?, ?> baseMapper )
	{
		this.baseMapper = baseMapper;
	}
}