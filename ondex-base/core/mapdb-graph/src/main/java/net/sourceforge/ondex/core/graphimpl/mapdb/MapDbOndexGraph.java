package net.sourceforge.ondex.core.graphimpl.mapdb;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.event.ONDEXListener;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jan 2022</dd></dl>
 *
 */
public class MapDbOndexGraph extends MemoryONDEXGraph
{	
	private static String mapDbDir = System.getProperty ( "java.io.tmpdir" );
	
	public static BiFunction<String, String, DB> mapDbDatabaseProvider = (dbPath, graphName) -> 
		DBMaker
	  .fileDB ( dbPath + '/' + graphName + "-mapdb.db" )
	  .closeOnJvmShutdown ()
	  .make ();	

	private DB mapdb;
	
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

	@Override
	@SuppressWarnings ( "unchecked" )
	protected <V> Set<V> getSet ( String name )
	{
		mapdb.delete ( name );
		return (Set<V>) mapdb.hashSet ( name ).createOrOpen ();
	}
	
	@Override
	@SuppressWarnings ( "unchecked" )
	protected <K,V> Map<K,V> getMap ( String name )
	{
		return (Map<K, V>) mapdb.hashMap ( name ).createOrOpen ();
	}
	
	protected <K, V> Map<K, Set<V>>  getMapOfSets ( String name, Function<K, String> key2StringConverter )
	{
		return new MapOverMapDbSet<> ( name, key2StringConverter );
	}

	protected <M extends MetaData, V> Map<M, Set<V>>  getMapOfMetadata ( String name )
	{
		return new MapOverMapDbSet<> ( name, meta -> meta.getId () );
	}

	protected <OE extends ONDEXEntity, V> Map<OE, Set<V>>  getMapOfONDEXEntity ( String name )
	{
		return new MapOverMapDbSet<> ( name, oe -> Integer.toString (oe.getId ()) );
	}
	
		
	@Override
	protected void initInternalData ()
	{
		this.mapdb = mapDatabases.computeIfAbsent ( 
			this.getName (), n -> mapDbDatabaseProvider.apply ( mapDbDir, n )
		);

		this.keyToRelation = getMap ( "keyToRelation" ); 
		this.idToRelation = new DualHashBidiMap<> ( getMap ( "idToRelation" ) );
		this.idToConcept = new DualHashBidiMap<> ( getMap ( "idToConcept" ) );
		this.dataSourceToConcepts = getMapOfMetadata ( "dataSourceToConcepts" );
		this.conceptClassToConcepts = getMapOfMetadata ( "conceptClassToConcepts" );
		this.attributeNameToConcepts = getMapOfMetadata ( "attributeNameToConcepts" );
		this.evidenceTypeToConcepts = getMapOfMetadata ( "evidenceTypeToConcepts" );
		this.conceptToRelations = getMapOfONDEXEntity ( "conceptToRelations" );
		this.dataSourceToRelations = getMapOfMetadata ( "dataSourceToRelations" );
		this.conceptClassToRelations = getMapOfMetadata ( "conceptClassToRelations" );
		this.relationTypeToRelations = getMapOfMetadata ( "relationTypeToRelations" );
		this.attributeNameToRelations = getMapOfMetadata ( "attributeNameToRelations" );
		this.evidenceTypeToRelations = getMapOfMetadata ( "evidenceTypeToRelations" );
		this.tagToConcepts = getMapOfONDEXEntity ( "tagToConcepts" );
		this.tagToRelations = getMapOfONDEXEntity ( "tagToRelations" );
		this.conceptToTags = getMapOfONDEXEntity ( "conceptToTags" );
		this.relationToTags = getMapOfONDEXEntity ( "relationToTags" );
		this.conceptToEvidence = getMapOfONDEXEntity ( "conceptToEvidence" );
		this.relationToEvidence = getMapOfONDEXEntity ( "relationToEvidence" );
	}
	
	public static String getMapDbDir ()
	{
		return mapDbDir;
	}


	public static void setMapDbDir ( String mapDbDir )
	{
		MapDbOndexGraph.mapDbDir = mapDbDir;
	}


	public static void setMapDbDatabaseProvider ( BiFunction<String, String, DB> mapDbDatabaseProvider )
	{
		MapOverMapDbSet.mapDbDatabaseProvider = mapDbDatabaseProvider;
	}

	public static void setMapDbDatabaseProvider ( DB mapDb )
	{
		setMapDbDatabaseProvider ( (dbPath, mapName) -> mapDb );
	}	

}
