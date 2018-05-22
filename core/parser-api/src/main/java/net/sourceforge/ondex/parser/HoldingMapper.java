package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * An holding mapper maps a data source item S to an ONDEX element O and then returns always that initial mapped element.
 * This is used in cases like ontology parsers, where a root class determines the concept class to which all classes on
 * its subtree should be associated as ONDEX concepts.
 *
 * This obviously makes this mapper stateful and its state (i.e., the ONDEX element that it holds) is supposed to be 
 * cleaned when a parser starts its work on a new document/file and, once the holden value is set, that is supposed to
 * remain until for the whole lifetime of the holding mapper. This behaviour must possibly be taken into account in 
 * multi-thread parsers and in less common applications. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>18 Jul 2017</dd></dl>
 *
 */
public abstract class HoldingMapper<S, O> extends DecoratingMapper<S, O> implements Mapper<S, O>
{
	private O mappedValue = null;
	
	public HoldingMapper ()
	{
		this ( null );
	}

	public HoldingMapper ( Mapper<S, O> baseMapper )
	{
		super ( baseMapper );
	}


	@Override
	@SuppressWarnings ( "unchecked" )
	public synchronized O map ( S source, ONDEXGraph graph )
	{
		if ( this.mappedValue != null ) return this.mappedValue; 
		return this.mappedValue = ((Mapper<S, O>) this.baseMapper).map ( source, graph );
	}

}
