package net.sourceforge.ondex.core.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;

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
	 * Facility to return the {@link ONDEXConcept#getConceptName() preferred concept name}, or
	 * the default value if none is available.
	 * 
	 * As in the underlying call, the result is undetermined if more than one names are defined for 
	 * the concept (which should be avoided). 
	 */
	public static String getConceptName ( ONDEXConcept concept, String defaultVal )
	{
		return Optional.ofNullable ( concept.getConceptName () )
		  .map ( ConceptName::getName )
		  .orElse ( defaultVal );
	}

	/**
	 * Defaults to {@link ONDEXConcept#getPID()}
	 */
	public static String getConceptName ( ONDEXConcept concept )
	{
		return getConceptName ( concept, concept.getPID () );
	}
	
	
	public static AttributeName getAttributeName ( ONDEXGraph graph, String attrNameId, boolean failIfNotFound )
	{
		return getMetaDataEntity ( 
			graph, attrNameId, ONDEXGraphMetaData::getAttributeName, failIfNotFound, "AttributeName" 
		);
	}

	public static AttributeName getAttributeName ( ONDEXGraph graph, String attrNameId )
	{
		return getAttributeName ( graph, attrNameId, true );
	}
	
	public static AttributeName getOrCreateAttributeName ( 
		ONDEXGraph graph, String attrNameId, String fullName, String description, 
		Class<?> dataType, Unit unit, AttributeName parentAttrName
	)
	{
		return getOrCreateMetaDataEntity ( 
			graph, attrNameId, 
			() -> getAttributeName ( graph, attrNameId, false ), 
			gmeta -> gmeta.createAttributeName ( 
				attrNameId, fullName, description, unit, dataType, parentAttrName 
			)
		);
	}

	public static AttributeName getOrCreateAttributeName ( 
		ONDEXGraph graph, String attrNameId, String fullName, String description,
		Class<?> dataType
	)
	{
		return getOrCreateAttributeName ( graph, attrNameId, fullName, description, dataType, null, null );
	}
	
	public static AttributeName getOrCreateAttributeName ( 
		ONDEXGraph graph, String attrNameId, Class<?> dataType
	)
	{
		return getOrCreateAttributeName ( graph, attrNameId, "", "", dataType );
	}
	
	
	/**
	 * @return the concept or relation attribute of type attrName.
	 * @failIfNoAttribName true if you want an exception when the attribute type nameId is null (else, null is returned).
	 *   Note that, although unusual, the attribute might exist but its value might be be null.   
	 * @failIfNoEntity true if you want an exception when the entity is null (else, null is returned).  
	 */
	public static Attribute getAttribute ( 
		ONDEXEntity entity, AttributeName attrName, boolean failIfNoAttribName, boolean failIfNoEntity 
	)
	{
		if ( entity == null )
		{
			if ( !failIfNoEntity ) return null;
			throw new IllegalArgumentException ( String.format (
			  "Attribute type '%s' requested for a null concept/relation", 
			  Optional.ofNullable ( attrName )
			  	.map ( AttributeName::getId )
			  	.orElse ( "<null>" )
			));
		}
		
		if ( attrName == null )
		{
			if ( !failIfNoAttribName ) return null;
			throw new IllegalArgumentException ( "Attribute type can't be null" );
		}
		return entity.getAttribute ( attrName );
	}	
	
	/**
	 * Defaults to true
	 */
	public static Attribute getAttribute ( 
		ONDEXEntity entity, AttributeName attrName, boolean failIfNoAttribName 
	)
	{
		return getAttribute ( entity, attrName, failIfNoAttribName, true );
	}

	/**
	 * Defaults to true, true
	 */
	public static Attribute getAttribute ( ONDEXEntity entity, AttributeName attrName )
	{
		return getAttribute ( entity, attrName, true );
	}
	
	
	
	/**
	 * <p>Gets an {@link Attribute#getValue() attribute value}, which <b>must be AT-compatible</b>, from the entity and 
	 * converts it into VT via the valueConverter. The converter <b>must</b> take care of the case where the attribute's
	 * value is null (which includes the case attrName is null and failIfNoAttribName is false).</p>
	 *  
	 */
	public static <VT> VT getAttrValue (
		ONDEXEntity entity, AttributeName attrName, Function<Object, VT> valueConverter,
		boolean failIfNoAttribName, boolean failIfNoEntity 
	)
	{		
		Object value = Optional.ofNullable ( getAttribute ( entity, attrName, failIfNoAttribName, failIfNoEntity ) )
			.map ( Attribute::getValue )
			.orElse ( null );
		
		return valueConverter.apply ( value );
	}	

	
	/**
	 * Defaults to true
	 */
	public static <VT> VT getAttrValue (
		ONDEXEntity entity, AttributeName attrName, Function<Object, VT> valueConverter, boolean failIfNoAttribName 
	)
	{
		return getAttrValue ( entity, attrName, valueConverter, failIfNoAttribName, true );
	}
	
	
	/**
	 * Defaults to true, true
	 */
	public static <VT> VT getAttrValue (
		ONDEXEntity entity, AttributeName attrName, Function<Object, VT> valueConverter 
	)
	{
		return getAttrValue ( entity, attrName, valueConverter, true );
	}

	
	/**
	 * Calls {@link #getAttrValue(ONDEXEntity, AttributeName, Function, boolean, boolean)} using 
	 * {@link Object#toString()} as converter, if flags are false, returns null when the attribute value is null 
	 * or doesn't exist.
	 *  
	 */
	public static String getAttrValueAsString (
		ONDEXEntity entity, AttributeName attrName, boolean failIfNoAttribName, boolean failIfNoEntity
	)
	{
		return getAttrValue ( entity, attrName, v -> v == null ? null : v.toString (), failIfNoAttribName, failIfNoEntity );
	}

	/**
	 * Defaults to true
	 */
	public static String getAttrValueAsString ( ONDEXEntity entity, AttributeName attrName, boolean failIfNoAttribName )
	{
		return getAttrValueAsString ( entity, attrName, failIfNoAttribName, true );
	}

	/**
	 * Defaults to true, true
	 */
	public static String getAttrValueAsString ( ONDEXEntity entity, AttributeName attrName )
	{
		return getAttrValueAsString ( entity, attrName, true );
	}
	
	

	
	/**
	 *  Uses {@link #getAttrValue(ONDEXGraph, ONDEXEntity, String, Function, boolean, boolean)} with a conversion function
	 *  that: 
	 *  - returns null if the attr value is null
	 *  - returns {@link Number#doubleValue()} if the attrib value is a compatible number (including integer, float)
	 *  - returns {@link NumberUtils#toDouble(String)} if the string version of the attribute value is a valid number
	 *  - return null if all of the above fails
	 *  As you can see, this will work with integer attributes too.  
	 */
	public static Double getAttrValueAsDouble (
		ONDEXEntity entity, AttributeName attrName, boolean failIfNoAttribName, boolean failIfNoEntity
	)
	{
		Function<Object, Double> converter = attr -> {
			if ( attr == null ) return null;
			if ( attr instanceof Number ) return ( ( Number ) attr ).doubleValue ();
			var strAttr = attr.toString ();
			if ( NumberUtils.isParsable ( strAttr ) ) return NumberUtils.toDouble ( strAttr );
			return null;
		};
		return getAttrValue ( entity, attrName, converter, failIfNoAttribName, failIfNoEntity );
	}

	/**
	 * Defaults to true
	 */
	public static Double getAttrValueAsDouble ( ONDEXEntity entity, AttributeName attrName, boolean failIfNoAttribName )
	{
		return getAttrValueAsDouble ( entity, attrName, failIfNoAttribName, true );
	}

	/**
	 * Defaults to true, true
	 */
	public static Double getAttrValueAsDouble ( ONDEXEntity entity, AttributeName attrName )
	{
		return getAttrValueAsDouble ( entity, attrName, true );
	}	
	
	
		
	
	/**
	 * Returns the attribute value as-is, hence assuming that it's of VT type. If flags are false, missing attribute or 
	 * null value return null. This is useful for types like numbers, but you <b>must</b> check for null result if flags 
	 * are true.  
	 */
	@SuppressWarnings ( "unchecked" )
	public static <VT> VT getAttrValue (
		ONDEXEntity entity, AttributeName attrName, boolean failIfNoAttribName, boolean failIfNoEntity 
	)
	{
		return getAttrValue ( entity, attrName, v -> v == null ? null : (VT) v, failIfNoAttribName, failIfNoEntity );
	}
	
	/**
	 * Defaults to true
	 */
	public static <VT> VT getAttrValue (
		ONDEXEntity entity, AttributeName attrName, boolean failIfNoAttribName
	)
	{
		return getAttrValue ( entity, attrName, failIfNoAttribName, true );
	}
	
	/**
	 * Defaults to true, true
	 */
	public static <VT> VT getAttrValue ( ONDEXEntity entity, AttributeName attrName )
	{
		return getAttrValue ( entity, attrName, true );
	}
	
	
	/**
	 * A wrapper of {@link #getAttribute(ONDEXGraph, ONDEXEntity, AttributeName, boolean, boolean)} that fetches
	 * the 
	 */
	public static Attribute getAttribute ( 
		ONDEXGraph graph, ONDEXEntity entity, String attrNameId, boolean failIfNoAttribName, boolean failIfNoEntity 
	)
	{

		AttributeName aname = getAttributeName ( graph, attrNameId, failIfNoAttribName );
		if ( aname == null ) return null;
		return getAttribute ( entity, aname, failIfNoAttribName, failIfNoEntity );
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
	public static <VT> VT getAttrValue (
		ONDEXGraph graph, ONDEXEntity entity, String attrNameId, Function<Object, VT> valueConverter,
		boolean failIfNoAttribName, boolean failIfNoEntity 
	)
	{
		AttributeName aname = getAttributeName ( graph, attrNameId, failIfNoAttribName );
		return getAttrValue ( entity, aname, valueConverter, failIfNoAttribName, failIfNoEntity );
	}

	/** Defaults to failIfNoEntity = true */
	public static <VT> VT getAttrValue (
		ONDEXGraph graph, ONDEXEntity entity, String attrNameId, Function<Object, VT> valueConverter, boolean failIfNoAttribName 
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
		AttributeName aname = getAttributeName ( graph, attrNameId, failIfNoAttribName );
		return getAttrValueAsString ( entity, aname, failIfNoAttribName, failIfNoEntity );
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
	 * Calls {@link #getAttrValue(ONDEXEntity, AttributeName, Function, boolean, boolean)} using 
	 * {@link Object#toString()} as converter, if flags are false, returns null when the attribute value is null 
	 * or doesn't exist.
	 *  
	 */
	public static Double getAttrValueAsDouble (
		ONDEXGraph graph, ONDEXEntity entity, String attrNameId, boolean failIfNoAttribName, boolean failIfNoEntity
	)
	{
		AttributeName aname = getAttributeName ( graph, attrNameId, failIfNoAttribName );
		return getAttrValueAsDouble ( entity, aname, failIfNoAttribName, failIfNoEntity );
	}

	/**
	 * Defaults to true
	 */
	public static Double getAttrValueAsDouble ( ONDEXGraph graph, ONDEXEntity entity, String attrNameId, boolean failIfNoAttribName )
	{
		return getAttrValueAsDouble ( graph, entity, attrNameId, failIfNoAttribName, true );
	}

	/**
	 * Defaults to true, true
	 */
	public static Double getAttrValueAsDouble ( ONDEXGraph graph, ONDEXEntity entity, String attrNameId )
	{
		return getAttrValueAsDouble ( graph, entity, attrNameId, true );
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
		AttributeName aname = getAttributeName ( graph, attrNameId, failIfNoAttribName );
		return getAttrValue ( entity, aname, v -> v == null ? null : (VT) v, failIfNoAttribName, failIfNoEntity );
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

	
	public static ConceptClass getConceptClass ( ONDEXGraph graph, String ccId, boolean failIfNotFound )
	{
		return getMetaDataEntity ( 
			graph, ccId, ONDEXGraphMetaData::getConceptClass, failIfNotFound, "ConceptClass" 
		);
	}
	
	/**
	 * Defaults to true
	 */
	public static ConceptClass getConceptClass ( ONDEXGraph graph, String ccId )
	{
		return getConceptClass ( graph, ccId, true );
	}
	
	public static ConceptClass getOrCreateConceptClass ( 
		ONDEXGraph graph, String ccId, String fullName, String description, 
		ConceptClass parent
	)
	{
		return getOrCreateMetaDataEntity ( 
			graph, ccId, 
			() -> getConceptClass ( graph, ccId, false ), 
			gmeta -> gmeta.createConceptClass ( ccId, fullName, description, parent) 
		);
	}

	public static ConceptClass getOrCreateConceptClass ( 
		ONDEXGraph graph, String ccId, String fullName, String description 
	)
	{
		return getOrCreateConceptClass ( graph, ccId, fullName, description, null );
	}

	public static ConceptClass getOrCreateConceptClass ( ONDEXGraph graph, String ccId )
	{
		return getOrCreateConceptClass ( graph, ccId, "", "" );
	}
	
	
	public static DataSource getOrCreateDataSource ( 
		ONDEXGraph graph, String dsId, String fullName, String description 
	)
	{
		return getOrCreateMetaDataEntity ( 
			graph, dsId, 
			() -> getDataSource ( graph, dsId, false ), 
			gmeta -> gmeta.createDataSource ( dsId, fullName, description ) 
		);
	}
	
	public static DataSource getOrCreateDataSource ( ONDEXGraph graph, String dsId )
	{
		return getOrCreateDataSource ( graph, dsId, "", "" );
	}
	
	public static DataSource getDataSource ( ONDEXGraph graph, String dsId, boolean failIfNotFound )
	{
		return getMetaDataEntity ( 
			graph, dsId, ONDEXGraphMetaData::getDataSource, failIfNotFound, "DataSource" 
		);
	}
	
	/**
	 * Defaults to true
	 */
	public static DataSource getDataSource ( ONDEXGraph graph, String dsId )
	{
		return getDataSource ( graph, dsId, true );
	}	
	

	public static EvidenceType getOrCreateEvidenceType ( 
		ONDEXGraph graph, String evId, String fullName, String description 
	)
	{
		return getOrCreateMetaDataEntity ( 
			graph, evId, 
			() -> getEvidenceType ( graph, evId, false ), 
			gmeta -> gmeta.createEvidenceType ( evId, fullName, description ) 
		);
	}
	
	public static EvidenceType getOrCreateEvidenceType ( ONDEXGraph graph, String evId )
	{
		return getOrCreateEvidenceType ( graph, evId, "", "" );
	}
	
	public static EvidenceType getEvidenceType ( ONDEXGraph graph, String evId, boolean failIfNotFound )
	{
		return getMetaDataEntity ( 
			graph, evId, ONDEXGraphMetaData::getEvidenceType, failIfNotFound, "EvidenceType" 
		);
	}
	
	/**
	 * Defaults to true
	 */
	public static EvidenceType getEvidenceType ( ONDEXGraph graph, String evId )
	{
		return getEvidenceType ( graph, evId, true );
	}		
	
	
	
	public static RelationType getOrCreateRelationType ( 
		ONDEXGraph graph, String typeId, String fullName, String description,
		String inverseName, boolean isAntiSymmetric, boolean isReflexive, boolean isSymmetric, boolean isTransitive,
		RelationType parent
	)
	{
		return getOrCreateMetaDataEntity ( 
			graph, typeId, 
			() -> getRelationType ( graph, typeId, false ), 
			gmeta -> gmeta.createRelationType ( 
				typeId, fullName, description, 
				inverseName, isAntiSymmetric, isReflexive, isSymmetric, isTransitive, parent
			) 
		);
	}

	public static RelationType getOrCreateRelationType ( 
		ONDEXGraph graph, String typeId, String fullName, String description,
		String inverseName, boolean isAntiSymmetric, boolean isReflexive, boolean isSymmetric, boolean isTransitive
	)
	{
		return getOrCreateRelationType ( 
			graph, typeId, fullName, description, inverseName, isAntiSymmetric, isReflexive, isSymmetric, isTransitive, null 
		);
	}
	
	public static RelationType getOrCreateRelationType ( 
		ONDEXGraph graph, String typeId, String fullName, String description,
		RelationType parent
	)
	{
		return getOrCreateRelationType ( graph, typeId, fullName, description, "", false, false, false, false, parent );
	}
	
	public static RelationType getOrCreateRelationType ( 
		ONDEXGraph graph, String typeId, String fullName, String description
	)
	{
		return getOrCreateRelationType ( graph, typeId, fullName, description, null );
	}
	
	public static RelationType getOrCreateRelationType ( ONDEXGraph graph, String typeId )
	{
		return getOrCreateRelationType ( graph, typeId, "", "", null );
	}
	
	public static RelationType getRelationType ( ONDEXGraph graph, String typeId, boolean failIfNotFound )
	{
		return getMetaDataEntity ( 
			graph, typeId, ONDEXGraphMetaData::getRelationType, failIfNotFound, "RelationType" 
		);
	}
	
	/**
	 * Defaults to true
	 */
	public static RelationType getRelationType ( ONDEXGraph graph, String typeId )
	{
		return getRelationType ( graph, typeId, true );
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
	
	/**
	 * A skeleton for getXXX methods.
	 * 
	 * @param <ME> they type of returned entity (eg, {@link ConceptClass}).
	 * @param id the entity ID (eg, {@link ConceptClass#getId()}
	 * @param metadataFetcher how the entity is fetched from the graph's metadata (eg, 
	 *   {@link ONDEXGraphMetaData#getConceptClass(String)}). 
	 * @param failIfNotFound if true, raises an exception when the ID isn't found. Else, returnd null.
	 * @param metadataName used to raise the not found exception (eg, "ConceptClass").
	 */
	private static <ME> ME getMetaDataEntity ( 
		ONDEXGraph graph, String id,
		BiFunction<ONDEXGraphMetaData, String, ME> metadataFetcher,
		boolean failIfNotFound,
		String metadataName 
	)
	{
		ME mentity = metadataFetcher.apply ( graph.getMetaData (), id );
		if ( mentity == null && failIfNotFound )
		{
			throw new IllegalArgumentException ( String.format (
			  "No '%s' '%s' in the graph", metadataName, id
			));
		}
		return mentity;
	}

	/**
	 * Skeleton for getOrCreate*** methods.
	 * @param <ME> the type of entity that is returned
	 * @param id the entity ID (eg, concept class ID, relation type ID)
	 * @param metadataFetcher how the entity is fetched from ID (eg, {@link #getConceptClass(ONDEXGraph, String)})
	 * @param metadataCreator how the entity is possibly created from the graph's metadata (eg, 
	 * 	{@link ONDEXGraphMetaData#createConceptClass(String, String, String, ConceptClass)}.
	 */
	private static <ME> ME getOrCreateMetaDataEntity (
		ONDEXGraph graph, String id, 
		Supplier<ME> metadataFetcher,
		Function<ONDEXGraphMetaData, ME> metadataCreator
	)
	{
		ONDEXGraphMetaData gmeta = graph.getMetaData ();
		ME result = metadataFetcher.get ();
		if ( result != null ) return result;
		
		return metadataCreator.apply ( gmeta );
	}
	
}
