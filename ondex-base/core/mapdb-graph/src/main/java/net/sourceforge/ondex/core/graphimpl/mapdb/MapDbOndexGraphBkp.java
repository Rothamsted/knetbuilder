package net.sourceforge.ondex.core.graphimpl.mapdb;
//package net.sourceforge.ondex.core.graphimpl.mapdb;
//
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
//import org.apache.commons.collections15.BidiMap;
//import org.apache.commons.collections15.bidimap.DualHashBidiMap;
//
//import net.sourceforge.ondex.config.Config;
//import net.sourceforge.ondex.core.Attribute;
//import net.sourceforge.ondex.core.AttributeName;
//import net.sourceforge.ondex.core.ConceptClass;
//import net.sourceforge.ondex.core.DataSource;
//import net.sourceforge.ondex.core.EvidenceType;
//import net.sourceforge.ondex.core.ONDEXConcept;
//import net.sourceforge.ondex.core.ONDEXRelation;
//import net.sourceforge.ondex.core.RelationKey;
//import net.sourceforge.ondex.core.RelationType;
//import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
//import net.sourceforge.ondex.core.base.RelationKeyImpl;
//import net.sourceforge.ondex.core.memory.MemoryONDEXGraphMetaData;
//import net.sourceforge.ondex.event.ONDEXEventHandler;
//import net.sourceforge.ondex.event.ONDEXListener;
//import net.sourceforge.ondex.event.type.DuplicatedEntryEvent;
//import net.sourceforge.ondex.event.type.EventType;
//
///**
// * 
// * TODO: comment me!
// *
// * @author brandizi
// * <dl><dt>Date:</dt><dd>18 Jan 2022</dd></dl>
// *
// */
//public class MapDbOndexGraph extends AbstractONDEXGraph
//{
//
//	private static final long serialVersionUID = 2485772142007959858L;
//
//	private final BidiMap<Integer, ONDEXConcept> id2Concept;
//	private final Map<DataSource, Set<ONDEXConcept>> dataSource2Concepts;
//	private final Map<ConceptClass, Set<ONDEXConcept>> conceptClass2Concepts;
//	private final Map<ONDEXConcept, Set<ONDEXRelation>> concept2Relations;
//
//	protected final Map<AttributeName, Set<ONDEXConcept>> attributeName2Concepts;
//	protected final Map<EvidenceType, Set<ONDEXConcept>> evidenceType2Concepts;
//
//	
//	private final BidiMap<Integer, ONDEXRelation> id2Relation;
//	private final Map<RelationKey, ONDEXRelation> key2Relation;
//	
//	private final Map<DataSource, Set<ONDEXRelation>> dataSource2Relations;
//	private final Map<ConceptClass, Set<ONDEXRelation>> conceptClass2Relations;
//	private final Map<RelationType, Set<ONDEXRelation>> relationType2Relations;
//
//
//	protected final Map<AttributeName, Set<ONDEXRelation>> attributeName2Relations;
//	protected final Map<EvidenceType, Set<ONDEXRelation>> evidenceType2Relations;
//
//	protected final Map<ONDEXConcept, Set<ONDEXConcept>> tag2Concepts;
//	protected final Map<ONDEXConcept, Set<ONDEXRelation>> tag2Relations;
//
//	protected final Map<ONDEXConcept, Set<ONDEXConcept>> conceptToTags;
//	protected final Map<ONDEXRelation, Set<ONDEXConcept>> relationToTags;
//
//	// all evidence of one concept
//	protected final Map<ONDEXConcept, Set<EvidenceType>> conceptToEvidence;
//
//	// all evidence of one relation
//	protected final Map<ONDEXRelation, Set<EvidenceType>> relationToEvidence;
//
//	/**
//	 * Constructor which sets the name of the graph to the given name.
//	 * 
//	 * @param name
//	 *          name of graph
//	 */
//	public MapDbOndexGraph ( String name )
//	{
//		this ( name, null );
//	}
//
//	/**
//	 * Constructor which sets the name of the graph to the given name.
//	 * 
//	 * @param name
//	 *          name of graph
//	 * @param l
//	 *          ONDEXListener
//	 */
//	public MapDbOndexGraph ( String name, ONDEXListener l )
//	{
//		super ( name, new MemoryONDEXGraphMetaData () );
//		if ( l != null )
//			ONDEXEventHandler.getEventHandlerForSID ( getSID () ).addONDEXONDEXListener ( l );
//		this.key2Relation = new HashMap<RelationKey, ONDEXRelation> ();
//		this.id2Relation = new DualHashBidiMap<Integer, ONDEXRelation> ();
//		this.id2Concept = new DualHashBidiMap<Integer, ONDEXConcept> ();
//		this.dataSource2Concepts = new HashMap<DataSource, Set<ONDEXConcept>> ();
//		this.conceptClass2Concepts = new HashMap<ConceptClass, Set<ONDEXConcept>> ();
//		this.attributeName2Concepts = new HashMap<AttributeName, Set<ONDEXConcept>> ();
//		this.evidenceType2Concepts = new HashMap<EvidenceType, Set<ONDEXConcept>> ();
//		this.concept2Relations = new HashMap<ONDEXConcept, Set<ONDEXRelation>> ();
//		this.dataSource2Relations = new HashMap<DataSource, Set<ONDEXRelation>> ();
//		this.conceptClass2Relations = new HashMap<ConceptClass, Set<ONDEXRelation>> ();
//		this.relationType2Relations = new HashMap<RelationType, Set<ONDEXRelation>> ();
//		this.attributeName2Relations = new HashMap<AttributeName, Set<ONDEXRelation>> ();
//		this.evidenceType2Relations = new HashMap<EvidenceType, Set<ONDEXRelation>> ();
//		this.tag2Concepts = new HashMap<ONDEXConcept, Set<ONDEXConcept>> ();
//		this.tag2Relations = new HashMap<ONDEXConcept, Set<ONDEXRelation>> ();
//		this.conceptToTags = new HashMap<ONDEXConcept, Set<ONDEXConcept>> ();
//		this.relationToTags = new HashMap<ONDEXRelation, Set<ONDEXConcept>> ();
//		this.conceptToEvidence = new HashMap<ONDEXConcept, Set<EvidenceType>> ();
//		this.relationToEvidence = new HashMap<ONDEXRelation, Set<EvidenceType>> ();
//	}
//
//	@Override
//	protected ONDEXConcept removeConcept ( int id )
//	{
//		ONDEXConcept c = id2Concept.remove ( id );
//		assert c.getId () == id : "Concept appears to be registered under the wrong id";
//
//		// removes meta data references to concept
//		dataSource2Concepts.get ( c.getElementOf () ).remove ( c );
//		conceptClass2Concepts.get ( c.getOfType () ).remove ( c );
//
//		// removes attribute name references to concept
//		for ( Attribute attribute : c.getAttributes () )
//		{
//			attributeName2Concepts.get ( attribute.getOfType () ).remove ( c );
//		}
//
//		// removes evidence type references to concept
//		for ( EvidenceType et : c.getEvidence () )
//		{
//			evidenceType2Concepts.get ( et ).remove ( c );
//		}
//
//		// removes tag references to concept
//		for ( ONDEXConcept tag : c.getTags () )
//		{
//			tag2Concepts.get ( tag ).remove ( c );
//		}
//		tag2Concepts.remove ( c );
//		conceptToTags.remove ( c );
//		conceptToEvidence.remove ( c );
//
//		return c;
//	}
//
//	@Override
//	protected boolean removeRelation ( int id )
//	{
//		ONDEXRelation r = id2Relation.get ( id );
//		if ( r != null )
//		{
//			ONDEXConcept from = r.getFromConcept ();
//			ONDEXConcept to = r.getToConcept ();
//			RelationType rt = r.getOfType ();
//			id2Relation.remove ( id );
//			this.removeRelation ( from, to, rt );
//			return true;
//		} else
//		{
//			return false;
//		}
//	}
//
//	@Override
//	protected boolean removeRelation ( ONDEXConcept fromConcept, ONDEXConcept toConcept, RelationType ofType )
//	{
//		RelationKey id = new RelationKeyImpl ( sid, fromConcept.getId (), toConcept.getId (), ofType.getId () );
//		ONDEXRelation r = key2Relation.remove ( id );
//		if ( r != null )
//		{
//
//			id2Relation.remove ( r.getId () );
//
//			// remove relation type references
//			relationType2Relations.get ( ofType ).remove ( r );
//
//			// remove fromConcept references
//			concept2Relations.get ( fromConcept ).remove ( r );
//			dataSource2Relations.get ( fromConcept.getElementOf () ).remove ( r );
//			conceptClass2Relations.get ( fromConcept.getOfType () ).remove ( r );
//
//			// remove toConcept references
//			concept2Relations.get ( toConcept ).remove ( r );
//			dataSource2Relations.get ( toConcept.getElementOf () ).remove ( r );
//			conceptClass2Relations.get ( toConcept.getOfType () ).remove ( r );
//
//			// remove attribute name references to relation
//			for ( Attribute attribute : r.getAttributes () )
//			{
//				attributeName2Relations.get ( attribute.getOfType () ).remove ( r );
//			}
//
//			// remove evidence type references to relation
//			for ( EvidenceType et : r.getEvidence () )
//			{
//				evidenceType2Relations.get ( et ).remove ( r );
//			}
//
//			// remove tag references to relation
//			for ( ONDEXConcept tag : r.getTags () )
//			{
//				tag2Relations.get ( tag ).remove ( r );
//			}
//			relationToTags.remove ( r );
//			relationToEvidence.remove ( r );
//
//			return true;
//		} else
//		{
//			return false;
//		}
//	}
//
//	@Override
//	protected ONDEXConcept retrieveConcept ( int id )
//	{
//		return id2Concept.get ( id );
//	}
//
//	@Override
//	protected Set<ONDEXConcept> retrieveConceptAll ()
//	{
//		return id2Concept.values ();
//	}
//
//	@Override
//	protected Set<ONDEXConcept> retrieveConceptAllAttributeName ( AttributeName attributeName )
//	{
//		return attributeName2Concepts.get ( attributeName );
//	}
//
//	@Override
//	protected Set<ONDEXConcept> retrieveConceptAllConceptClass ( ConceptClass conceptClass )
//	{
//		return conceptClass2Concepts.get ( conceptClass );
//	}
//
//	@Override
//	protected Set<ONDEXConcept> retrieveConceptAllDataSource ( DataSource dataSource )
//	{
//		return dataSource2Concepts.get ( dataSource );
//	}
//
//	@Override
//	protected Set<ONDEXConcept> retrieveConceptAllEvidenceType ( EvidenceType evidenceType )
//	{
//		return evidenceType2Concepts.get ( evidenceType );
//	}
//
//	@Override
//	protected Set<ONDEXConcept> retrieveConceptAllTag ( ONDEXConcept concept )
//	{
//		return tag2Concepts.get ( concept );
//	}
//
//	@Override
//	protected ONDEXRelation retrieveRelation ( int id )
//	{
//		return id2Relation.get ( id );
//	}
//
//	@Override
//	protected ONDEXRelation retrieveRelation ( ONDEXConcept fromConcept, ONDEXConcept toConcept, RelationType ofType )
//	{
//		RelationKey key = new RelationKeyImpl ( sid, fromConcept.getId (), toConcept.getId (), ofType.getId () );
//		return key2Relation.get ( key );
//	}
//
//	@Override
//	protected Set<ONDEXRelation> retrieveRelationAll ()
//	{
//		return id2Relation.values ();
//	}
//
//	@Override
//	protected Set<ONDEXRelation> retrieveRelationAllAttributeName ( AttributeName attributeName )
//	{
//		return attributeName2Relations.get ( attributeName );
//	}
//
//	@Override
//	protected Set<ONDEXRelation> retrieveRelationAllConcept ( ONDEXConcept concept )
//	{
//		return concept2Relations.get ( concept );
//	}
//
//	@Override
//	protected Set<ONDEXRelation> retrieveRelationAllConceptClass ( ConceptClass conceptClass )
//	{
//		return conceptClass2Relations.get ( conceptClass );
//	}
//
//	@Override
//	protected Set<ONDEXRelation> retrieveRelationAllDataSource ( DataSource dataSource )
//	{
//		return dataSource2Relations.get ( dataSource );
//	}
//
//	@Override
//	protected Set<ONDEXRelation> retrieveRelationAllEvidenceType ( EvidenceType evidenceType )
//	{
//		return evidenceType2Relations.get ( evidenceType );
//	}
//
//	@Override
//	protected Set<ONDEXRelation> retrieveRelationAllRelationType ( RelationType relationType )
//	{
//		return relationType2Relations.get ( relationType );
//	}
//
//	@Override
//	protected Set<ONDEXRelation> retrieveRelationAllTag ( ONDEXConcept concept )
//	{
//		return tag2Relations.get ( concept );
//	}
//
//	@Override
//	protected Set<ONDEXConcept> retrieveTags ()
//	{
//
//		// create union of concepts used as tags
//		Set<ONDEXConcept> allTags = new HashSet<ONDEXConcept> ( tag2Concepts.keySet () );
//		allTags.addAll ( tag2Relations.keySet () );
//		return allTags;
//	}
//
//	@Override
//	protected ONDEXConcept storeConcept ( long sid, int id, String pid, String annotation, String description,
//			DataSource elementOf, ConceptClass ofType, Collection<EvidenceType> evidence )
//	{
//
//		// check for existing concepts
//		ONDEXConcept existingConcept = id2Concept.get ( id );
//		if ( existingConcept != null )
//		{
//			fireEventOccurred ( new DuplicatedEntryEvent (
//					Config.properties.getProperty ( "memory.ONDEXGraph.DuplicatedConcept" ) + id + " pid= " + pid,
//					"[MemoryONDEXGraph - storeConcept]" ) );
//
//			// return existing concept
//			return existingConcept;
//		} else
//		{
//
//			// create a new concept
//			ONDEXConcept c = new MemoryONDEXConcept ( sid, this, id, pid, annotation, description, elementOf, ofType );
//
//			// add all evidence to concept
//			conceptToEvidence.put ( c, new HashSet<EvidenceType> () );
//			for ( EvidenceType anEvidence : evidence )
//			{
//				c.addEvidenceType ( anEvidence );
//			}
//
//			// put concept to global list
//			id2Concept.put ( c.getId (), c );
//
//			// index by data source
//			Set<ONDEXConcept> cvSet = dataSource2Concepts.get ( elementOf );
//			if ( cvSet == null )
//			{
//				cvSet = new HashSet<ONDEXConcept> ();
//				dataSource2Concepts.put ( elementOf, cvSet );
//			}
//			cvSet.add ( c );
//
//			// index by concept class
//			Set<ONDEXConcept> ccSet = conceptClass2Concepts.get ( ofType );
//			if ( ccSet == null )
//			{
//				ccSet = new HashSet<ONDEXConcept> ();
//				conceptClass2Concepts.put ( ofType, ccSet );
//			}
//			ccSet.add ( c );
//
//			// return new concept
//			return c;
//		}
//	}
//
//	@Override
//	protected ONDEXRelation storeRelation ( long sid, int id, ONDEXConcept fromConcept, ONDEXConcept toConcept,
//			RelationType ofType, Collection<EvidenceType> evidence )
//	{
//
//		// construct corresponding relation key
//		RelationKey relkey = new RelationKeyImpl ( sid, fromConcept.getId (), toConcept.getId (), ofType.getId () );
//
//		// check for existing relation
//		ONDEXRelation existingRelation = key2Relation.get ( relkey );
//		if ( existingRelation != null )
//		{
//			fireEventOccurred ( new DuplicatedEntryEvent (
//					Config.properties.getProperty ( "memory.ONDEXGraph.DuplicatedRelation" ) + relkey + " pid from "
//							+ fromConcept.getPID () + "pid to " + toConcept.getPID (),
//					"[MemoryONDEXGraph - storeRelation]" ) );
//
//			// return existing relation
//			return existingRelation;
//		} else
//		{
//
//			// create a new relation
//			ONDEXRelation r = new MemoryONDEXRelation ( sid, this, id, fromConcept, toConcept, ofType );
//
//			// add all evidence to relation
//			relationToEvidence.put ( r, new HashSet<EvidenceType> () );
//			for ( EvidenceType anEvidence : evidence )
//			{
//				r.addEvidenceType ( anEvidence );
//			}
//
//			// put relation to global lists
//			key2Relation.put ( r.getKey (), r );
//			id2Relation.put ( r.getId (), r );
//
//			// set references for relation type
//			Set<ONDEXRelation> relationTypeSet = relationType2Relations.get ( ofType );
//			if ( relationTypeSet == null )
//			{
//				relationTypeSet = new HashSet<ONDEXRelation> ();
//				relationType2Relations.put ( ofType, relationTypeSet );
//			}
//			relationTypeSet.add ( r );
//
//			// set references for fromConcept
//			Set<ONDEXRelation> fromConceptSet = concept2Relations.get ( fromConcept );
//			if ( fromConceptSet == null )
//			{
//				fromConceptSet = new HashSet<ONDEXRelation> ();
//				concept2Relations.put ( fromConcept, fromConceptSet );
//			}
//			fromConceptSet.add ( r );
//
//			// index from properties
//			Set<ONDEXRelation> dataSourceFromSet = dataSource2Relations.get ( fromConcept.getElementOf () );
//			if ( dataSourceFromSet == null )
//			{
//				dataSourceFromSet = new HashSet<ONDEXRelation> ();
//				dataSource2Relations.put ( fromConcept.getElementOf (), dataSourceFromSet );
//			}
//			dataSourceFromSet.add ( r );
//
//			Set<ONDEXRelation> conceptClassFromSet = conceptClass2Relations.get ( fromConcept.getOfType () );
//			if ( conceptClassFromSet == null )
//			{
//				conceptClassFromSet = new HashSet<ONDEXRelation> ();
//				conceptClass2Relations.put ( fromConcept.getOfType (), conceptClassFromSet );
//			}
//			conceptClassFromSet.add ( r );
//
//			// set references for toConcept
//			Set<ONDEXRelation> toConceptSet = concept2Relations.get ( toConcept );
//			if ( toConceptSet == null )
//			{
//				toConceptSet = new HashSet<ONDEXRelation> ();
//				concept2Relations.put ( toConcept, toConceptSet );
//			}
//			toConceptSet.add ( r );
//
//			// index to properties
//			Set<ONDEXRelation> dataSourceToSet = dataSource2Relations.get ( toConcept.getElementOf () );
//			if ( dataSourceToSet == null )
//			{
//				dataSourceToSet = new HashSet<ONDEXRelation> ();
//				dataSource2Relations.put ( toConcept.getElementOf (), dataSourceToSet );
//			}
//			dataSourceToSet.add ( r );
//
//			Set<ONDEXRelation> conceptClassToSet = conceptClass2Relations.get ( toConcept.getOfType () );
//			if ( conceptClassToSet == null )
//			{
//				conceptClassToSet = new HashSet<ONDEXRelation> ();
//				conceptClass2Relations.put ( toConcept.getOfType (), conceptClassToSet );
//			}
//			conceptClassToSet.add ( r );
//
//			// return new relation
//			return r;
//		}
//	}
//
//	/**
//	 * Propagate events to registered listeners
//	 * 
//	 * @param e
//	 *          EventType to propagate
//	 */
//	protected void fireEventOccurred ( EventType e )
//	{
//		ONDEXEventHandler.getEventHandlerForSID ( getSID () ).fireEventOccurred ( e );
//	}
//}