package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * A constant mapper. This returns always the same value as result of mapping and can be used in Spring config files, 
 * to define constants like fixed ONDEX evidence or ONDEX data sources.  
 *
 * @param <S> the type of source to be mapped, as in {@link Mapper}
 * @param <C> the type of constant the mapper is based on. This is different than O, cause the {@link #map(Object, ONDEXGraph)}
 * method could be overridden to map a C type to an O type. 
 * @param <O> the type of output that the mapper returns, as in {@link Mapper}
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 */
public class ConstMapper<S,C,O> implements Mapper<S, O>
{
	private C value;
	
	public ConstMapper ( C value ) {
		this.value = value;
	}

	public ConstMapper () {
		this ( null );
	}

	
	@Override
	@SuppressWarnings ( "unchecked" )
	public O map ( S source, ONDEXGraph graph ) {
		return (O) value;
	}

	
	public C getValue ()
	{
		return value;
	}

	public void setValue ( C value )
	{
		this.value = value;
	}
}
