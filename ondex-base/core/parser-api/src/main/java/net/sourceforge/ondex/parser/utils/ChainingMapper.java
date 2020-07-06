package net.sourceforge.ondex.parser.utils;

import java.util.function.Function;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.DecoratingMapper;
import net.sourceforge.ondex.parser.Mapper;
import net.sourceforge.ondex.parser.TextMapper;

/**
 * A composer that invokes two mappers in a nesting/composing fashion ( outer( base ( src ) ).
 * 
 * This is defined as an abstract class because you typically want to extend an more specific type (e.g., 
 * {@link TextMapper} and pass it to some other component, which accepts that specific type only (so 
 * a concrete {@link ChainingMapper} would raise {@link ClassCastException}). 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 May 2018</dd></dl>
 *
 */
public abstract class ChainingMapper<S, O> extends DecoratingMapper<S, O>
{
	private Mapper<?, O> outerMapper;
	
	public ChainingMapper ()
	{
		super ();
	}

	public <O1> ChainingMapper ( Mapper<S, O1> baseMapper, Mapper<O1, O> outerMapper )
	{
		super ( baseMapper );
		this.outerMapper = outerMapper;
	}

	public <O1> ChainingMapper ( Mapper<S, O1> baseMapper, Function<O1, O> postProcessor ) {
		this ( baseMapper, MapperUtils.fromFunction ( postProcessor ) );
	}

	/**
	 * Applies the {@link #getBaseMapper() base mapper to the input} and then passes its result to the 
	 * {@link #getOuterMapper() outer mapper}, to return its result. The graph parameter is passed to both the
	 * mappers.
	 * 
	 */
	@Override
	@SuppressWarnings ( "unchecked" )
	public O map ( S source, ONDEXGraph graph ) {
		return ((Mapper<Object, O>) outerMapper).map ( ((Mapper<S, ?>) baseMapper).map ( source, graph ), graph );
	}
	
	
	@SuppressWarnings ( { "unchecked", "rawtypes" } )
	public <O1> Mapper<S, O1> getBaseMapper ()
	{
		return (Mapper<S, O1>) (Mapper) super.baseMapper;
	}

	public <O1> void setBaseMapper ( Mapper<S, O1> baseMapper )
	{
		super.baseMapper = baseMapper;
	}

	@SuppressWarnings ( "unchecked" )
	public <O1> Mapper<O1, O> getOuterMapper ()
	{
		return (Mapper<O1, O>) outerMapper;
	}

	public <O1> void setOuterMapper ( Mapper<O1, O> outerMapper )
	{
		this.outerMapper = outerMapper;
	}

	/**
	 * A wrapper of {@link MapperUtils#fromFunction(Function)} that ease the creation of the 
	 * {@link #getOuterMapper() outer mapper} from a mapping function that doesn't need the {@code graph} parameter
	 * usually accepted by {@link Mapper#map(Object, ONDEXGraph)}.
	 *   
	 */
	public <O1> void setPostProcessor ( Function<O1, O> postProcessor )
	{
		this.outerMapper = MapperUtils.fromFunction ( postProcessor );
	}
}
