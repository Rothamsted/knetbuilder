package net.sourceforge.ondex.core.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

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
	 * @return the concept or relation attribute of type nameId.
	 * @failIfNoAttribName true if you want an exception when the attribute type nameId doesn't exist (else, null is returned).
	 *   Note that, although unusual, the attribute might exist but its value might be be null.   
	 * @failIfNoEntity true if you want an exception when the entity is null (else, null is returned).  
	 */
	public static Attribute getAttribute ( 
		ONDEXGraph graph, ONDEXEntity entity, String attrNameId, boolean failIfNoAttribName, boolean failIfNoEntity 
	)
	{
		if ( entity == null )
		{
			if ( !failIfNoEntity ) return null;
			throw new IllegalArgumentException ( String.format (
			  "Attribute type '%s' requested for a null concept/relation", attrNameId
			));
		}

		AttributeName aname = graph.getMetaData ().getAttributeName ( attrNameId );
		if ( aname == null )
		{
			if ( !failIfNoAttribName ) return null;
			throw new IllegalArgumentException ( String.format (
			  "No attribute type '%s' in the graph", attrNameId
			));
		}
		return entity.getAttribute ( aname );
	}
	

	/** Defaults to failIfNoEntity = true */
	public static Attribute getAttribute ( 
		ONDEXGraph graph, ONDEXEntity entity, String attrNameId, boolean failIfNoAttribName )
	{
		return getAttribute ( graph, entity, attrNameId, failIfNoAttribName, true );
	}
	
	/** Defaults to failIfNoAttribName and failIfNoEntity = true */
	public static Attribute getAttribute ( ONDEXGraph graph, ONDEXEntity entity, String attrNameId )
	{
		return getAttribute ( graph, entity, attrNameId, true );
	}

	
	/**
	 * <p>Gets an {@link Attribute#getValue() attribute value}, which <b>must be AT-compatible</b>, from the entity and 
	 * converts it into VT via the valueConverter. The converter <b>must</b> take care of the case where the attribute's
	 * value is null (or, if failIfNoAttribName is false, the attribute with attrNameId doesn't exist, in which case the
	 * converter is invoked with null as parameter).</p>
	 *  
	 */
	@SuppressWarnings ( "unchecked" )
	public static <AT, VT> VT getAttrValue (
		ONDEXGraph graph, ONDEXEntity entity, String attrNameId, Function<AT, VT> valueConverter,
		boolean failIfNoAttribName, boolean failIfNoEntity 
	)
	{
		AT value = (AT) Optional.ofNullable ( getAttribute ( graph, entity, attrNameId, failIfNoAttribName, failIfNoEntity ) )
			.map ( Attribute::getValue )
			.orElse ( null );
		
		return valueConverter.apply ( value );
	}

	/** Defaults to failIfNoEntity = true */
	public static <AT, VT> VT getAttrValue (
		ONDEXGraph graph, ONDEXEntity entity, String attrNameId, Function<AT, VT> valueConverter, boolean failIfNoAttribName 
	)
	{
		return getAttrValue ( graph, entity, attrNameId, valueConverter, failIfNoAttribName, true ); 
	}
	
	/** Defaults to failIfNoAttribName and failIfNoEntity = true */
	public static <AT, VT> VT getAttrValue (
		ONDEXGraph graph, ONDEXEntity entity, String attrNameId, Function<AT, VT> valueConverter 
	)
	{
		return getAttrValue ( graph, entity, attrNameId, true );
	}
	
	
	/**
	 * Calls {@link #getAttrValue(ONDEXGraph, ONDEXEntity, String, Function, boolean, boolean)} using {@link Object#toString()}
	 * as converter, if flags are false, return null when the attribute value is null or doesn't exist.
	 *  
	 */
	public static String getAttrValueAsString (
		ONDEXGraph graph, ONDEXEntity entity, String attrNameId, boolean failIfNoAttribName, boolean failIfNoEntity
	)
	{
		return getAttrValue ( graph, entity, attrNameId, v -> v == null ? null : v.toString (), failIfNoAttribName, failIfNoEntity );
	}

	/** Defaults to failIfNoEntity = true */
	public static String getAttrValueAsString ( ONDEXGraph graph, ONDEXEntity entity, String attrNameId, boolean failIfNoAttribName )
	{
		return getAttrValueAsString ( graph, entity, attrNameId, failIfNoAttribName, true );
	}
	
	/** Defaults to failIfNoAttribName and failIfNoEntity = true */
	public static String getAttrValueAsString ( ONDEXGraph graph, ONDEXEntity entity, String attrNameId )
	{
		return getAttrValueAsString ( graph, entity, attrNameId, true );
	}
	
	
	/**
	 * Returns the attribute value as-is, hence assuming that it's of VT type. If flags are false, missing attribute or 
	 * null value return null. This is useful for types like numbers, but you <b>must</b> check for null result if flags are
	 * true.  
	 */
	@SuppressWarnings ( "unchecked" )
	public static <VT> VT getAttrValue (
		ONDEXGraph graph, ONDEXEntity entity, String attrNameId, boolean failIfNoAttribName, boolean failIfNoEntity 
	)
	{
		return getAttrValue ( graph, entity, attrNameId, v -> v == null ? null : (VT) v, failIfNoAttribName, failIfNoEntity );
	}

	/** Defaults to failIfNoEntity = true */
	public static <VT> VT getAttrValue ( ONDEXGraph graph, ONDEXEntity entity, String attrNameId, boolean failIfNoAttribName )
	{
		return getAttrValue ( graph, entity, attrNameId, failIfNoAttribName, true );
	}
	
	/** Defaults to failIfNoAttribName and failIfNoEntity = true */
	public static <VT> VT getAttrValue ( ONDEXGraph graph, ONDEXEntity entity, String attrNameId )
	{
		return getAttrValue ( graph, entity, attrNameId, true );
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

	/**
	 * Removes the attribute with ID attrId from all the concepts in the graph that have it.
	 */
	public static int removeConceptAttribute ( ONDEXGraph graph, String attrId )
	{
		return removeEntityAttribute (
			graph, attrId, graph::getConceptsOfAttributeName, graph::getConcept
		);
	}
		
	/**
	 * Removes the attribute with ID attrId from all the relations in the graph that have it.
	 */
	public static int removeRelationAttribute ( ONDEXGraph graph, String attrId )
	{
		return removeEntityAttribute (
			graph, attrId, graph::getRelationsOfAttributeName, graph::getRelation
		);
	}

	/**
	 * Removes the attribute with ID attrId from all the entities of type OE in the graph that 
	 * have such attribute. This is the common base for {@link #removeConceptAttribute(ONDEXGraph, String)}, 
	 * {@link #removeRelationAttribute(ONDEXGraph, String)}. 
	 * 
	 * @param entitiesSelector is a function to extract all OE instances from the graph that
	 * has the attribute attrId.
	 * 
	 * @param entityGetter gets an OE entity by {@link ONDEXEntity#getId() ID}.
	 * 
	 */
	private static <OE extends ONDEXEntity> int removeEntityAttribute ( 
		ONDEXGraph graph, String attrId, 
		Function<AttributeName, Set<OE>> entitiesSelector,
		Function<Integer, OE> entityGetter
	)
	{
		AttributeName aname = graph.getMetaData ().getAttributeName ( attrId );
		if ( aname == null ) return 0;
		
  	Set<OE> entities = entitiesSelector.apply ( aname );
  	if ( entities == null || entities.isEmpty () ) return 0;
    
  	// We need this to avoid ConcurrentModificationEx
  	Set<Pair<Integer, AttributeName>> removeMe = new HashSet<> ();
  	entities.forEach ( c -> removeMe.add ( Pair.of ( c.getId(), aname ) ) );	

  	removeMe.forEach ( pair -> 
  		entityGetter.apply ( pair.getLeft () )
  		.deleteAttribute ( pair.getRight () ) 
  	);
  	
  	return removeMe.size ();
	}

}
