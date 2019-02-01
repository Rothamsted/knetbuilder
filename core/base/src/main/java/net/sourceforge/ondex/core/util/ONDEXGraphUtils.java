package net.sourceforge.ondex.core.util;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.base.ConceptAttribute;

/**
 * TODO: comment me!
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

	public static String getString ( ONDEXConcept concept )
	{
		if ( concept == null ) return "<null>";
		return String.format (
			"C{%s:%s (%d)}", concept.getOfType ().getId (), concept.getPID (), concept.getId ()
		);
	}
	
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
	
	public static String getString ( ONDEXEntity ent )
	{
		if ( ent == null ) return "<null>";
		if ( ent instanceof ONDEXConcept ) return getString ( (ONDEXConcept)  ent );
		if ( ent instanceof ONDEXRelation ) return getString ( (ONDEXRelation)  ent );
		return String.format ( "?:%s", ent.getId () );
	}

		
	public static Attribute getAttribute ( ONDEXGraph graph, ONDEXEntity entity, String nameId )
	{
		AttributeName aname = graph.getMetaData ().getAttributeName ( nameId );
		if ( aname == null ) throw new IllegalArgumentException ( String.format (
		  "No attribute type '%s' in the grqph", nameId
		));
		return entity.getAttribute ( aname );
	}
}
