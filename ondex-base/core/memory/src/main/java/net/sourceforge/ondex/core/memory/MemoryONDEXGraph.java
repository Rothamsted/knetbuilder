package net.sourceforge.ondex.core.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationKey;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.core.base.RelationKeyImpl;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.ONDEXListener;
import net.sourceforge.ondex.event.type.DuplicatedEntryEvent;
import net.sourceforge.ondex.event.type.EventType;

/**
 * This class represents a pure memory based implementation of the abstract ONDEX graph. It uses standard JAVA
 * datatypes.
 * 
 * @author taubertj
 */
public class MemoryONDEXGraph extends AbstractONDEXGraph
{
	// serial version id
	private static final long serialVersionUID = 1L;

	// contains all relations indexed by key
	protected Map<RelationKey, ONDEXRelation> keyToRelation;

	// contains all relations indexed by id
	protected BidiMap<Integer, ONDEXRelation> idToRelation;

	// contains all concepts indexed by id
	protected BidiMap<Integer, ONDEXConcept> idToConcept;

	// mapping from cv to concepts
	protected Map<DataSource, Set<ONDEXConcept>> dataSourceToConcepts;

	// mapping from conceptclass to concepts
	protected Map<ConceptClass, Set<ONDEXConcept>> conceptClassToConcepts;

	// mapping from concept to relations
	protected Map<ONDEXConcept, Set<ONDEXRelation>> conceptToRelations;

	// mapping from cv to relations
	protected Map<DataSource, Set<ONDEXRelation>> dataSourceToRelations;

	// mapping from conceptclass to relations
	protected Map<ConceptClass, Set<ONDEXRelation>> conceptClassToRelations;

	// mapping from relationtype to relations
	protected Map<RelationType, Set<ONDEXRelation>> relationTypeToRelations;

	// mapping from attribute name to concepts
	protected Map<AttributeName, Set<ONDEXConcept>> attributeNameToConcepts;

	// mapping from attribute name to relations
	protected Map<AttributeName, Set<ONDEXRelation>> attributeNameToRelations;

	// mapping from evidence type to concepts
	protected Map<EvidenceType, Set<ONDEXConcept>> evidenceTypeToConcepts;

	// mapping from evidence type to relations
	protected Map<EvidenceType, Set<ONDEXRelation>> evidenceTypeToRelations;

	// association of tags with concepts
	protected Map<ONDEXConcept, Set<ONDEXConcept>> tagToConcepts;

	// association of tags with relations
	protected Map<ONDEXConcept, Set<ONDEXRelation>> tagToRelations;

	// all tags of one concept
	protected Map<ONDEXConcept, Set<ONDEXConcept>> conceptToTags;

	// all tags of one relation
	protected Map<ONDEXRelation, Set<ONDEXConcept>> relationToTags;

	// all evidence of one concept
	protected Map<ONDEXConcept, Set<EvidenceType>> conceptToEvidence;

	// all evidence of one relation
	protected Map<ONDEXRelation, Set<EvidenceType>> relationToEvidence;

	/**
	 * Constructor which sets the name of the graph to the given name.
	 * 
	 * @param name
	 *          name of graph
	 */
	public MemoryONDEXGraph ( String name )
	{
		this ( name, null );
	}

	/**
	 * Constructor which sets the name of the graph to the given name.
	 * 
	 * @param name
	 *          name of graph
	 * @param l
	 *          ONDEXListener
	 */
	public MemoryONDEXGraph ( String name, ONDEXListener l )
	{
		super ( name, new MemoryONDEXGraphMetaData () );
		if ( l != null )
			ONDEXEventHandler.getEventHandlerForSID ( getSID () ).addONDEXONDEXListener ( l );
		
		this.initInternalData ();
	}

	protected void initInternalData ()
	{
		this.keyToRelation = getMap ( "keyToRelation" ); 
		this.idToRelation = new DualHashBidiMap<> ( getMap ( "idToRelation" ) );
		this.idToConcept = new DualHashBidiMap<> ( getMap ( "idToConcept" ) );
		this.dataSourceToConcepts = getMap ( "dataSourceToConcepts" );
		this.conceptClassToConcepts = getMap ( "conceptClassToConcepts" );
		this.attributeNameToConcepts = getMap ( "attributeNameToConcepts" );
		this.evidenceTypeToConcepts = getMap ( "evidenceTypeToConcepts" );
		this.conceptToRelations = getMap ( "conceptToRelations" );
		this.dataSourceToRelations = getMap ( "dataSourceToRelations" );
		this.conceptClassToRelations = getMap ( "conceptClassToRelations" );
		this.relationTypeToRelations = getMap ( "relationTypeToRelations" );
		this.attributeNameToRelations = getMap ( "attributeNameToRelations" );
		this.evidenceTypeToRelations = getMap ( "evidenceTypeToRelations" );
		this.tagToConcepts = getMap ( "tagToConcepts" );
		this.tagToRelations = getMap ( "tagToRelations" );
		this.conceptToTags = getMap ( "conceptToTags" );
		this.relationToTags = getMap ( "relationToTags" );
		this.conceptToEvidence = getMap ( "conceptToEvidence" );
		this.relationToEvidence = getMap ( "relationToEvidence" );
	}
	
	
	protected <V> Set<V> getSet ( String name )
	{
		return new HashSet<> ();
	}
	
	
	protected <K,V> Map<K,V> getMap ( String name )
	{
		return new HashMap<> ();
	}
	
	protected <K,V> Set<V> populateValuesMap ( Map<K, Set<V>> map, K key, String mapName, String stringKey )
	{
		return map.computeIfAbsent ( key, k -> getSet ( mapName + ":" + k ) );
	}
		
	protected <K extends ONDEXEntity,V> Set<V> populateValuesMap ( Map<K, Set<V>> map, K key, String mapName )
	{
		return map.computeIfAbsent ( key, k -> getSet ( mapName + ":" + Integer.valueOf ( key.getId () ) ) );
	}

	protected <K extends MetaData,V> Set<V> populateValuesMap ( Map<K, Set<V>> map, K key, String mapName )
	{
		return map.computeIfAbsent ( key, k -> getSet ( mapName + ":" + key.getId () ) );
	}
	
	
	
	@Override
	protected ONDEXConcept removeConcept ( int id )
	{
		ONDEXConcept c = idToConcept.remove ( id );
		assert c.getId () == id : "Concept appears to be registered under the wrong id";

		// removes meta data references to concept
		dataSourceToConcepts.get ( c.getElementOf () ).remove ( c );
		conceptClassToConcepts.get ( c.getOfType () ).remove ( c );

		// removes attribute name references to concept
		for ( Attribute attribute : c.getAttributes () )
		{
			attributeNameToConcepts.get ( attribute.getOfType () ).remove ( c );
		}

		// removes evidence type references to concept
		for ( EvidenceType et : c.getEvidence () )
		{
			evidenceTypeToConcepts.get ( et ).remove ( c );
		}

		// removes tag references to concept
		for ( ONDEXConcept tag : c.getTags () )
		{
			tagToConcepts.get ( tag ).remove ( c );
		}
		tagToConcepts.remove ( c );
		conceptToTags.remove ( c );
		conceptToEvidence.remove ( c );

		return c;
	}

	@Override
	protected boolean removeRelation ( int id )
	{
		ONDEXRelation r = idToRelation.get ( id );
		if ( r != null )
		{
			ONDEXConcept from = r.getFromConcept ();
			ONDEXConcept to = r.getToConcept ();
			RelationType rt = r.getOfType ();
			idToRelation.remove ( id );
			this.removeRelation ( from, to, rt );
			return true;
		} else
		{
			return false;
		}
	}

	@Override
	protected boolean removeRelation ( ONDEXConcept fromConcept, ONDEXConcept toConcept, RelationType ofType )
	{
		RelationKey id = new RelationKeyImpl ( sid, fromConcept.getId (), toConcept.getId (), ofType.getId () );
		ONDEXRelation r = keyToRelation.remove ( id );
		if ( r != null )
		{

			idToRelation.remove ( r.getId () );

			// remove relation type references
			relationTypeToRelations.get ( ofType ).remove ( r );

			// remove fromConcept references
			conceptToRelations.get ( fromConcept ).remove ( r );
			dataSourceToRelations.get ( fromConcept.getElementOf () ).remove ( r );
			conceptClassToRelations.get ( fromConcept.getOfType () ).remove ( r );

			// remove toConcept references
			conceptToRelations.get ( toConcept ).remove ( r );
			dataSourceToRelations.get ( toConcept.getElementOf () ).remove ( r );
			conceptClassToRelations.get ( toConcept.getOfType () ).remove ( r );

			// remove attribute name references to relation
			for ( Attribute attribute : r.getAttributes () )
			{
				attributeNameToRelations.get ( attribute.getOfType () ).remove ( r );
			}

			// remove evidence type references to relation
			for ( EvidenceType et : r.getEvidence () )
			{
				evidenceTypeToRelations.get ( et ).remove ( r );
			}

			// remove tag references to relation
			for ( ONDEXConcept tag : r.getTags () )
			{
				tagToRelations.get ( tag ).remove ( r );
			}
			relationToTags.remove ( r );
			relationToEvidence.remove ( r );

			return true;
		} else
		{
			return false;
		}
	}

	@Override
	protected ONDEXConcept retrieveConcept ( int id )
	{
		return idToConcept.get ( id );
	}

	@Override
	protected Set<ONDEXConcept> retrieveConceptAll ()
	{
		return idToConcept.values ();
	}

	@Override
	protected Set<ONDEXConcept> retrieveConceptAllAttributeName ( AttributeName attributeName )
	{
		return attributeNameToConcepts.get ( attributeName );
	}

	@Override
	protected Set<ONDEXConcept> retrieveConceptAllConceptClass ( ConceptClass conceptClass )
	{
		return conceptClassToConcepts.get ( conceptClass );
	}

	@Override
	protected Set<ONDEXConcept> retrieveConceptAllDataSource ( DataSource dataSource )
	{
		return dataSourceToConcepts.get ( dataSource );
	}

	@Override
	protected Set<ONDEXConcept> retrieveConceptAllEvidenceType ( EvidenceType evidenceType )
	{
		return evidenceTypeToConcepts.get ( evidenceType );
	}

	@Override
	protected Set<ONDEXConcept> retrieveConceptAllTag ( ONDEXConcept concept )
	{
		return tagToConcepts.get ( concept );
	}

	@Override
	protected ONDEXRelation retrieveRelation ( int id )
	{
		return idToRelation.get ( id );
	}

	@Override
	protected ONDEXRelation retrieveRelation ( ONDEXConcept fromConcept, ONDEXConcept toConcept, RelationType ofType )
	{
		RelationKey key = new RelationKeyImpl ( sid, fromConcept.getId (), toConcept.getId (), ofType.getId () );
		return keyToRelation.get ( key );
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAll ()
	{
		return idToRelation.values ();
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllAttributeName ( AttributeName attributeName )
	{
		return attributeNameToRelations.get ( attributeName );
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllConcept ( ONDEXConcept concept )
	{
		return conceptToRelations.get ( concept );
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllConceptClass ( ConceptClass conceptClass )
	{
		return conceptClassToRelations.get ( conceptClass );
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllDataSource ( DataSource dataSource )
	{
		return dataSourceToRelations.get ( dataSource );
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllEvidenceType ( EvidenceType evidenceType )
	{
		return evidenceTypeToRelations.get ( evidenceType );
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllRelationType ( RelationType relationType )
	{
		return relationTypeToRelations.get ( relationType );
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllTag ( ONDEXConcept concept )
	{
		return tagToRelations.get ( concept );
	}

	@Override
	protected Set<ONDEXConcept> retrieveTags ()
	{
		// create union of concepts used as tags
		// TODO: Should we abstract from HashSet?
		Set<ONDEXConcept> allTags = new HashSet<> ( tagToConcepts.keySet () );
		allTags.addAll ( tagToRelations.keySet () );
		return allTags;
	}

// TODO: remove?
//	protected ONDEXConcept newConcept (
//		long sid, int id, String pid, String annotation, String description, DataSource elementOf, ConceptClass ofType 
//	)
//	{
//		return new MemoryONDEXConcept ( sid, this, id, pid, annotation, description, elementOf, ofType );		
//	}
	
	@Override
	protected ONDEXConcept storeConcept ( long sid, int id, String pid, String annotation, String description,
			DataSource elementOf, ConceptClass ofType, Collection<EvidenceType> evidence )
	{

		// check for existing concepts
		ONDEXConcept existingConcept = idToConcept.get ( id );
		if ( existingConcept != null )
		{
			fireEventOccurred ( new DuplicatedEntryEvent (
					Config.properties.getProperty ( "memory.ONDEXGraph.DuplicatedConcept" ) + id + " pid= " + pid,
					"[MemoryONDEXGraph - storeConcept]" ) );

			// return existing concept
			return existingConcept;
		} 
		else
		{

			// create a new concept
			ONDEXConcept c = new MemoryONDEXConcept ( sid, this, id, pid, annotation, description, elementOf, ofType );

			// add all evidence to concept
			this.populateValuesMap ( conceptToEvidence, c, "conceptToEvidence" );
			for ( EvidenceType anEvidence : evidence )
			{
				c.addEvidenceType ( anEvidence );
			}

			// put concept to global list
			idToConcept.put ( c.getId (), c );

			// index by data source
			this.populateValuesMap ( dataSourceToConcepts, elementOf, "dataSourceToConcepts" )
			.add ( c );
			

			// index by concept class
			this.populateValuesMap ( conceptClassToConcepts, ofType, "conceptClassToConcepts" )
			.add ( c );

			// return new concept
			return c;
		}
	}

// TODO: remove?
//	protected ONDEXRelation newRelation (
//		long sid, int id, ONDEXConcept fromConcept, ONDEXConcept toConcept, RelationType ofType
//	)
//	{
//		return new MemoryONDEXRelation ( sid, this, id, fromConcept, toConcept, ofType );
//	}
	
	@Override
	protected ONDEXRelation storeRelation ( long sid, int id, ONDEXConcept fromConcept, ONDEXConcept toConcept,
			RelationType ofType, Collection<EvidenceType> evidence )
	{

		// construct corresponding relation key
		RelationKey relkey = new RelationKeyImpl ( sid, fromConcept.getId (), toConcept.getId (), ofType.getId () );

		// check for existing relation
		ONDEXRelation existingRelation = keyToRelation.get ( relkey );
		if ( existingRelation != null )
		{
			fireEventOccurred ( new DuplicatedEntryEvent (
					Config.properties.getProperty ( "memory.ONDEXGraph.DuplicatedRelation" ) + relkey + " pid from "
							+ fromConcept.getPID () + "pid to " + toConcept.getPID (),
					"[MemoryONDEXGraph - storeRelation]" ) );

			// return existing relation
			return existingRelation;
		} else
		{

			// create a new relation
			ONDEXRelation r = new MemoryONDEXRelation ( sid, this, id, fromConcept, toConcept, ofType );

			// add all evidence to relation
			this.populateValuesMap ( relationToEvidence, r, "relationToEvidence" );
			for ( EvidenceType anEvidence : evidence )
			{
				r.addEvidenceType ( anEvidence );
			}

			// put relation to global lists
			keyToRelation.put ( r.getKey (), r );
			idToRelation.put ( r.getId (), r );

			// set references for relation type
			this.populateValuesMap ( relationTypeToRelations, ofType, "relationTypeToRelations" )
			.add ( r );
			
			// set references for fromConcept
			this.populateValuesMap ( conceptToRelations, fromConcept, "conceptToRelations" )
			.add ( r );
			
			// index from properties
			this.populateValuesMap ( dataSourceToRelations, fromConcept.getElementOf (), "dataSourceToRelations" )
			.add ( r );
			
			// from type index
			this.populateValuesMap ( conceptClassToRelations, fromConcept.getOfType (), "conceptClassToRelations" )
			.add ( r );

			// to concept index
			this.populateValuesMap ( conceptToRelations, toConcept, "conceptToRelations" )
			.add ( r );
			

			// index to properties
			this.populateValuesMap ( dataSourceToRelations, toConcept.getElementOf (), "dataSourceToRelations" )
			.add ( r );
			
			// to type index
			this.populateValuesMap ( conceptClassToRelations, toConcept.getOfType (), "conceptClassToRelations" )
			.add ( r );
			
			// return new relation
			return r;
		}
	}

	/**
	 * Propagate events to registered listeners
	 * 
	 * @param e
	 *          EventType to propagate
	 */
	protected void fireEventOccurred ( EventType e )
	{
		ONDEXEventHandler.getEventHandlerForSID ( getSID () ).fireEventOccurred ( e );
	}
}