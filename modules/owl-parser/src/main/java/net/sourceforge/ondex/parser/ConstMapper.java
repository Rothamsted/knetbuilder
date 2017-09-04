package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * A constant mapper. This returns always the same value as result of mapping and can be used in Spring config files, 
 * to define constants like fixed ONDEX evidence or ONDEX data sources.  
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
