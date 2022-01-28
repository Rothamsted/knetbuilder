package net.sourceforge.ondex.core.graphimpl.mapdb;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.util.ONDEXGraphUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>20 Jan 2022</dd></dl>
 *
 */
public class MapDbBasicsTest
{
	private String MAPDB_PATH = "target/test-mapdb-deletion.db" ;
	
	private DB mapDb;
	
	@Before
	public void initMapDb ()
	{
		new File ( MAPDB_PATH ).delete ();
		this.mapDb = DBMaker
			.fileDB ( MAPDB_PATH )
			.fileDeleteAfterClose()
			.make();

	}
	
	@Test
	public void testObjectDeletion ()
	{

		Set<String> set = mapDb.hashSet ( "testSet", Serializer.STRING ).createOrOpen ();
		set.add ( "test1" );
		
		Assert.assertTrue ( "Value not stored!", set.contains ( "test1" ) );
		
		mapDb.delete ( "testSet" );
		set = mapDb.hashSet ( "testSet", Serializer.STRING ).createOrOpen ();
		Assert.assertFalse ( "Value not stored!", set.contains ( "test1" ) );
	}
	
	@Test
	public void testOndexConcept ()
	{
		var graph = new MemoryONDEXGraph ( "test" );
		
		var gmeta = graph.getMetaData ();
		
		var ev = gmeta.createEvidenceType ( "e0", "Test Evidence", "" );
		var ds = gmeta.createDataSource ( "ds0", "Test Data Source", "" );
		var cc = gmeta.createConceptClass ( "cc0", "Test CC", "", null );
		var c = graph.createConcept ( "c0", "", "Test concept", ds, cc, Set.of ( ev ) );
		
		/*
		var mapDbConcept = this.mapDb.atomicVar ( "concept" ).createOrOpen ();
		mapDbConcept.set ( c );
		*/
		
		Set<Object> set = (Set<Object>) this.mapDb.hashSet ( "cset" ).createOrOpen ();
		set.add ( c );
		
	}
	
	@Test ( expected = IllegalArgumentException.class )
	public void testNullSerializer ()
	{
		Set<String> set = mapDb.hashSet ( "testSet", (Serializer<String>)null ).createOrOpen ();
		set.add ( "x" );

		Assert.assertTrue ( "Value not stored!", set.contains ( "x" ) );
	}
}
