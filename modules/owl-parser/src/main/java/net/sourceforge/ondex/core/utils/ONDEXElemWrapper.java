package net.sourceforge.ondex.core.utils;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>29 Apr 2017</dd></dl>
 *
 */
public class ONDEXElemWrapper<OE>
{
	private OE element;
	private ONDEXGraph graph;
	
	public ONDEXElemWrapper ( OE element, ONDEXGraph graph )
	{
		super ();
		this.setElement ( element );
		this.setGraph ( graph );
	}

	public OE getElement ()
	{
		return element;
	}

	public void setElement ( OE element )
	{
		this.element = element;
	}

	public ONDEXGraph getGraph ()
	{
		return graph;
	}

	public void setGraph ( ONDEXGraph graph )
	{
		this.graph = graph;
	}
	
	public CachedGraphWrapper getGraphWrapper ()
	{
		return CachedGraphWrapper.getInstance ( this.getGraph () );
	}
}
