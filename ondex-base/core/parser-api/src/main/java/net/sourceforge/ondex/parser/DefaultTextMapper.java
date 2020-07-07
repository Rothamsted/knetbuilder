package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * The default {@link TextMapper} uses some {@link TextsMapper} and returns its first result only.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>21 May 2018</dd></dl>
 *
 */
public class DefaultTextMapper<S> extends DecoratingMapper<S, String> implements TextMapper<S>
{
	public DefaultTextMapper ()
	{
		super ();
	}

	public DefaultTextMapper ( TextsMapper<S> baseMapper )
	{
		super ( baseMapper );
	}

	@Override
	public String map ( S source, ONDEXGraph graph )
	{
		return this.getBaseMapper ().map ( source, graph ).findFirst ().orElse ( null );
	}

	@SuppressWarnings ( { "unchecked" } )
	public TextsMapper<S> getBaseMapper () {
		return (TextsMapper<S>) super.baseMapper;
	}

	public void setBaseMapper ( TextsMapper<S> baseMapper )
	{
		super.baseMapper = baseMapper;
	}	
}
