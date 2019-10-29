package net.sourceforge.ondex.core.util;

import java.util.Collection;
import java.util.Iterator;

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
	 * @failIfNoAttribName true if you want an exception when the attribute type nameId doesn't exist.  
	 */
	public static Attribute getAttribute ( ONDEXGraph graph, ONDEXEntity entity, String nameId, boolean failIfNoAttribName )
	{
		AttributeName aname = graph.getMetaData ().getAttributeName ( nameId );
		if ( aname == null )
		{
			if ( !failIfNoAttribName ) return null;
			throw new IllegalArgumentException ( String.format (
			  "No attribute type '%s' in the grqph", nameId
			));
		}
		return entity.getAttribute ( aname );
	}

	/**
	 * Defaults to true.
	 */
	public static Attribute getAttribute ( ONDEXGraph graph, ONDEXEntity entity, String nameId )
	{
		return getAttribute ( graph, entity, nameId, true );
	}

	
	/**
	 * Simple utility to infer the type of an {@link ONDEXEntity} collection as a string.
	 * 
	 * @return "concept" or "relation" if the paramter is non-null and not empty (first element is needed to try to guess),
	 * else returns null.
	 *  
	 * @throws IllegalArgumentException if the first element in the collection is unlikely neither a concept nor a relation.
	 */
	public static <OE extends ONDEXEntity> String getEntityType ( Collection<OE> ondexEntities )
	{
		if ( ondexEntities == null ) return null;
		Iterator<OE> oeitr = ondexEntities.iterator ();
		if ( !oeitr.hasNext () ) return null;
		return getEntityType ( oeitr.next () );
	}

	/**
	 * Variant of {@link #getEntityType(Collection)}
	 */
	public static <OE extends ONDEXEntity> String getEntityType ( OE ondexEntity )
	{
		if ( ondexEntity == null ) return null;
		return ( getEntityType ( ondexEntity.getClass () ));
	}


	/**
	 * Variant of {@link #getEntityType(Collection)}
	 */
	public static <OE extends ONDEXEntity> String getEntityType ( Class<OE> ondexEntityType )
	{
		if ( ondexEntityType == null ) return null;
		if ( ONDEXConcept.class.isAssignableFrom ( ondexEntityType ) ) return "concept";
		if ( ONDEXRelation.class.isAssignableFrom ( ondexEntityType ) ) return "relation";
		throw new IllegalArgumentException ( 
			ONDEXGraphUtils.class.getSimpleName () + ".getEntityType() has a value of unkknown type: " + ondexEntityType.getName () 
		);
	}

}
