package net.sourceforge.ondex.core.util;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;

/**
 * Some general utilities to handle {@link ONDEXGraph}s.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>31 Jan 2019</dd></dl>
 *
 */
public class ONDEXGraphUtils
{
	private ONDEXGraphUtils ()
	{
	}

	/**
	 * @return a synthetic, programmer-oriented representation of the concept, reporting identifying data such as
	 * concept type or accession.   
	 */
	public static String getString ( ONDEXConcept concept )
	{
		if ( concept == null ) return "<null>";
		return String.format (
			"C{%s:%s (%d)}", concept.getOfType ().getId (), concept.getPID (), concept.getId ()
		);
	}
	
	/**
	 * @return a synthetic, programmer-oriented representation of the relation, reporting identifying data such as
	 * end points or relation type.   
	 */
	public static String getString ( ONDEXRelation rel )
	{
		if ( rel == null ) return "<null>";
		return String.format (
			"R{%s:%s->%s (%d)}", 
			rel.getOfType ().getId (), 
			getString ( rel.getFromConcept () ), 
			getString ( rel.getToConcept () ),
			rel.getId ()
		);
	}
	
	/**
	 * Dispatches to {@link #getString(ONDEXConcept)} or {@link #getString(ONDEXRelation)}
	 */
	public static String getString ( ONDEXEntity ent )
	{
		if ( ent == null ) return "<null>";
		if ( ent instanceof ONDEXConcept ) return getString ( (ONDEXConcept)  ent );
		if ( ent instanceof ONDEXRelation ) return getString ( (ONDEXRelation)  ent );
		return String.format ( "?:%s", ent.getId () );
	}

	/**
	 * @return the concept or relation attribute of type nameId. If none is available, returns null.  
	 */
	public static Attribute getAttribute ( ONDEXGraph graph, ONDEXEntity entity, String nameId )
	{
		AttributeName aname = graph.getMetaData ().getAttributeName ( nameId );
		if ( aname == null ) throw new IllegalArgumentException ( String.format (
		  "No attribute type '%s' in the grqph", nameId
		));
		return entity.getAttribute ( aname );
	}
}
