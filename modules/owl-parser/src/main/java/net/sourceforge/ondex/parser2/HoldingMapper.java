package net.sourceforge.ondex.parser2;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>18 Jul 2017</dd></dl>
 *
 */
public class HoldingMapper<S, O> extends FilterMapper<S, O> implements Mapper<S, O>
{
	private O mappedValue = null;

	@Override
	@SuppressWarnings ( "unchecked" )
	public O map ( S source, ONDEXGraph graph )
	{
		if ( this.mappedValue != null ) return this.mappedValue; 
		return this.mappedValue = (O) this.getBaseMapper ().map ( source, graph );
	}	
}
