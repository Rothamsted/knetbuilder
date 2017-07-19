package net.sourceforge.ondex.parser2;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 */
public class ConstMapper<S,C,O> implements Mapper<S, O>
{
	private C value;
	
	public ConstMapper ( C value ) {
		this.setValue ( value );
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
