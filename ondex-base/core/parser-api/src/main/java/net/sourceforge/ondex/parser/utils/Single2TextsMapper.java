package net.sourceforge.ondex.parser.utils;

import java.util.stream.Stream;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.DecoratingMapper;
import net.sourceforge.ondex.parser.DefaultTextMapper;
import net.sourceforge.ondex.parser.TextMapper;
import net.sourceforge.ondex.parser.TextsMapper;

/**
 * A simple wrapper that allows to use a {@link TextMapper single-value text mapper} where a 
 * {@link TextsMapper multiple-value mapper} is needed.
 * 
 * Logically, this is the dual of {@link DefaultTextMapper}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Jun 2019</dd></dl>
 *
 */
public class Single2TextsMapper<S> extends DecoratingMapper<S, Stream<String>> implements TextsMapper<S>
{

	public Single2TextsMapper ()
	{
	}

	public Single2TextsMapper ( TextMapper<S> singleValueMapper )
	{
		this.baseMapper = singleValueMapper;
	}
	
	
	@Override
	public Stream<String> map ( S source, ONDEXGraph graph )
	{
		@SuppressWarnings ( "unchecked" )
		TextMapper<S> singleValueMapper = (TextMapper<S>) this.baseMapper;
		return Stream.of ( singleValueMapper.map ( source, graph ) );
	}
	
	@SuppressWarnings ( { "unchecked" } )
	public TextMapper<S> getBaseMapper () {
		return (TextMapper<S>) super.baseMapper;
	}

	public void setBaseMapper ( TextMapper<S> singleValueMapper )
	{
		super.baseMapper = singleValueMapper;
	}		
}
