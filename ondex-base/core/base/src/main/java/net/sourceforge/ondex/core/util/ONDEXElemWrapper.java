package net.sourceforge.ondex.core.util;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * Allows you to put together an ONDEX element (eg, concept, relation) with the graph it belongs to. This is useful for
 * those cases where you need to do some operation in the graph involving the elements (e.g., add relations to a concept).
 * 
 * Unfortunately ONDEX elements don't have something like getGraph() in their interfaces and for the moment we choose 
 * to face it through this shortcut, since changing the core would be more complicated to do.
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>29 Apr 2017</dd></dl>
 *
 */
public class ONDEXElemWrapper<OE>
{
	private OE element;
	private ONDEXGraph graph;
	
	/**
	 * A facility to get a new instance quickly
	 */
	public static <OE> ONDEXElemWrapper<OE> of ( OE elem, ONDEXGraph graph ) {
		return new ONDEXElemWrapper<> ( elem, graph ); 
	} 
	
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
