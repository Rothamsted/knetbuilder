package net.sourceforge.ondex.parser.utils;

import java.util.function.Function;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.Mapper;

/**
 * Some utilities about {@link Mapper mappers}
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 May 2018</dd></dl>
 *
 */
public class MapperUtils
{
	/**
	 * Creates a mapper from a {@link Function}, by applying the function and ignoring the graph that is passed to
	 * {@link Mapper#map(Object, ONDEXGraph)}.
	 * 
	 */
	public static <S, O> Mapper<S, O> fromFunction ( Function<S, O> f )
	{
		return new Mapper<S, O>() 
		{
			@Override
			public O map ( S source, ONDEXGraph graph ) {
				return f.apply ( source );
			}
		};
	}
}
