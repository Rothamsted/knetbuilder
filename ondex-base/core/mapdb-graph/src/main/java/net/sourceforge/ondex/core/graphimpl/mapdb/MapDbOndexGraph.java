package net.sourceforge.ondex.core.graphimpl.mapdb;

import java.io.Closeable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.mapdb.DB;
import org.mapdb.DB.HashMapMaker;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.graphimpl.mapdb.OndexEntityMapDbSerializer.ConceptSerializer;
import net.sourceforge.ondex.core.graphimpl.mapdb.OndexEntityMapDbSerializer.RelationSerializer;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.event.ONDEXListener;

/**
 * An experiment to implement Ondex core interfaces with MapDB, so that data can stay on disk, rather
 * than occupying huge amounts of memory.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jan 2022</dd></dl>
 *
 */
public class MapDbOndexGraph extends MemoryONDEXGraph implements Closeable
{	
	private static String mapDbDir = System.getProperty ( "java.io.tmpdir" );
	
	private DB mapdb;
	
	private ConceptSerializer conceptSerializer;
	private RelationSerializer relationSerializer;
	
	
	private static Map<String, DB> mapDatabases = new ConcurrentHashMap<> ();
	
	private static final long serialVersionUID = 2800797253517929886L;

	
	public MapDbOndexGraph ( String name, ONDEXListener l )
	{
		super ( name, l );
	}

	public MapDbOndexGraph ( String name )
	{
		super ( name );
	}

	
	@SuppressWarnings ( "unchecked" )
	protected <K,V> Map<K, V> getMap ( String name, Serializer<? super K> keySerializer, Serializer<? extends V> valueSerializer )
	{
		HashMapMaker<K, V> mapMaker = (HashMapMaker<K, V>) mapdb.hashMap ( name );
		if ( keySerializer != null )
			mapMaker = (HashMapMaker<K, V>) mapMaker.keySerializer ( keySerializer );
		if ( valueSerializer != null ) 
			mapMaker = (HashMapMaker<K, V>) mapMaker.valueSerializer ( valueSerializer );
		return (Map<K, V>) mapMaker.createOrOpen ();
	}
	
	@SuppressWarnings ( "unchecked" )
	protected <M extends MetaData, V> Map<M, Set<V>>  getMapOfMetadatas ( String name, Serializer<? extends V> valueSerializer )
	{
		return new MapOverMapDbSet<> ( name, meta -> meta.getId (), this.mapdb, null, (Serializer<V>)valueSerializer );
	}

	@SuppressWarnings ( { "unchecked", "rawtypes" } )
	protected <V> Map<ONDEXConcept, Set<V>>  getMapOfConcepts ( String name, Serializer<? extends V> valueSerializer )
	{
		return (Map<ONDEXConcept, Set<V>>) (Map) new MapOverMapDbSet<> ( 
			name, oe -> Integer.toString ( oe.getId () ), this.mapdb, 
			conceptSerializer, (Serializer<V>) valueSerializer
		);
	}

	@SuppressWarnings ( { "unchecked", "rawtypes" } )
	protected <V> Map<ONDEXRelation, Set<V>>  getMapOfRelations ( String name, Serializer<? extends V> valueSerializer )
	{
		return (Map<ONDEXRelation, Set<V>>) (Map) new MapOverMapDbSet<> ( 
			name, oe -> Integer.toString ( oe.getId () ), this.mapdb, 
			relationSerializer, (Serializer<V>)valueSerializer
		);
	}
	

	@Override
	protected void initInternalData ()
	{
	  this.conceptSerializer = new ConceptSerializer ( this );
		this.relationSerializer = new RelationSerializer ( this );

		this.mapdb = mapDatabases.computeIfAbsent ( 
			this.getName (),
			gname -> DBMaker
				//.memoryDirectDB ()
		  	.fileDB ( mapDbDir + '/' + gname + "-mapdb.db" )
		  	.fileMmapEnableIfSupported ()
		    //.allocateStartSize ( 2L * 1024*1024*1024 )  // 2GB
		    //.allocateIncrement ( 512 * 1024*1024 )       // 512MB
		  	.closeOnJvmShutdown ()
		  	.make ()
		);	

		this.keyToRelation = getMap ( "keyToRelation", null, relationSerializer ); 
		this.idToRelation = new DualHashBidiMap<> ( getMap ( "idToRelation", null, relationSerializer ) );
		this.idToConcept = new DualHashBidiMap<> ( getMap ( "idToConcept", Serializer.INTEGER, conceptSerializer ) );
		this.dataSourceToConcepts = getMapOfMetadatas ( "dataSourceToConcepts", conceptSerializer );
		this.conceptClassToConcepts = getMapOfMetadatas ( "conceptClassToConcepts", conceptSerializer );
		this.attributeNameToConcepts = getMapOfMetadatas ( "attributeNameToConcepts", conceptSerializer );
		this.evidenceTypeToConcepts = getMapOfMetadatas ( "evidenceTypeToConcepts", conceptSerializer );
		this.conceptToRelations = getMapOfConcepts ( "conceptToRelations", relationSerializer );
		this.dataSourceToRelations = getMapOfMetadatas ( "dataSourceToRelations", relationSerializer );
		this.conceptClassToRelations = getMapOfMetadatas ( "conceptClassToRelations", relationSerializer );
		this.relationTypeToRelations = getMapOfMetadatas ( "relationTypeToRelations", relationSerializer );
		this.attributeNameToRelations = getMapOfMetadatas ( "attributeNameToRelations", relationSerializer );
		this.evidenceTypeToRelations = getMapOfMetadatas ( "evidenceTypeToRelations", relationSerializer );
		this.tagToConcepts = getMapOfConcepts ( "tagToConcepts", conceptSerializer );
		this.tagToRelations = getMapOfConcepts ( "tagToRelations", relationSerializer );
		this.conceptToTags = getMapOfConcepts ( "conceptToTags", conceptSerializer );
		this.relationToTags = getMapOfRelations ( "relationToTags", conceptSerializer );
		this.conceptToEvidence = getMapOfConcepts ( "conceptToEvidence", null );
		this.relationToEvidence = getMapOfRelations ( "relationToEvidence", null );
	}

	
	@Override
	public void close ()
	{
		this.mapdb.close ();
		mapDatabases.remove ( this.getName () );
		
		// TODO: check it's closed when using
		// TODO: synchro?
	}

	public static String getMapDbDir ()
	{
		return mapDbDir;
	}


	public static void setMapDbDir ( String mapDbDir )
	{
		MapDbOndexGraph.mapDbDir = mapDbDir;
	}

	@Override
	protected ONDEXConcept newConcept ( long sid, int id, String pid, String annotation, String description,
			DataSource elementOf, ConceptClass ofType )
	{
		return new MapDbOndexConcept ( sid, this, id, pid, annotation, description, elementOf, ofType );
	}

	@Override
	protected ONDEXRelation newRelation ( long sid, int id, ONDEXConcept fromConcept, ONDEXConcept toConcept,
			RelationType ofType )
	{
		return new MapDbOndexRelation ( sid, this, id, fromConcept, toConcept, ofType ); 
	}

}
