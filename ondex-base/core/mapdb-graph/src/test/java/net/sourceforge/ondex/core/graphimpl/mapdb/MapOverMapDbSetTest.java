package net.sourceforge.ondex.core.graphimpl.mapdb;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mapdb.Serializer;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;


/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>21 Jan 2022</dd></dl>
 *
 */
public class MapOverMapDbSetTest
{
	public static String MAPDB_DIR = "target/mapdb-tests";
	
	@BeforeClass
	public static void initMapDbDir ()
	{
		MapOverMapDbSet.setMapDbDir ( MAPDB_DIR );
	}
	
	@Before
	public void resetMapDb () throws IOException
	{
		var dir = new File ( MAPDB_DIR );
		dir.mkdir ();
		FileUtils.cleanDirectory ( dir );
	}
	
	@Test
	public void testCreate ()
	{
		var map = MapOverMapDbSet.ofIntKeys ( "testMap", Serializer.STRING );
		map.createEntry ( 1, "test val 1.1" );
		map.addEntry ( 1, "test val 1.2" );
		map.addEntry ( 1, "test val 1.2" );

		map.createEntry ( 2 );
		map.addEntry ( 2, "test val 2.1" );
		
		Assert.assertEquals ( "Map has wrong size!", 2, map.size () );
		
		Set<String> vals1 = map.get ( 1 );
		Assert.assertNotNull ( "1 entry is null", vals1 );
		Assert.assertEquals ( "Wrong size for entry 1!", 2, vals1.size () );
		Assert.assertTrue ( "1 -> 1.1 doesn't exist!", vals1.contains ( "test val 1.1" ) );
		Assert.assertTrue ( "1 -> 1.2 doesn't exist!", vals1.contains ( "test val 1.2" ) );
		Assert.assertTrue (
			"2 -> 2.1 doesn't exist in entrySet!", 
			map.entrySet ().contains ( new AbstractMap.SimpleEntry<> ( 2, Set.of ( "test val 2.1" ) )));
	}
	
	@Test
	public void testMapUtilityMethods ()
	{
		Map<String, Set<String>> map = MapOverMapDbSet.ofStringKeys ( "testMap", Serializer.STRING );
		map.computeIfAbsent ( "k1", s -> Set.of () ).add ( "v1" );
		map.put ( "k2", Set.of ( "v2.1", "v2.2" ) );
		
		// Trick to get 3-argument function
		BiFunction<String, Set<String>, Function<String, Set<String>>> remapper = 
		(k, set) -> val -> {
			if ( set == null ) return Set.of ( val );
			var set1 = new HashSet<> ( set );
			set1.add ( val );
			return set1;
		};
				
		map.compute ( "k3", (k, set) -> remapper.apply ( k, set ).apply ( "v3.1" ) );
		map.compute ( "k3", (k, set) -> remapper.apply ( k, set ).apply ( "v3.2" ) );
		
		Assert.assertEquals ( "Map has wrong size!", 3, map.size () );
		Assert.assertEquals ( "k1 entry is wrong!", Set.of  ( "v1" ), map.get ( "k1" ) );
		Assert.assertEquals ( "k2 entry is wrong!", Set.of  ( "v2.1", "v2.2"  ), map.get ( "k2" ) );
		Assert.assertEquals ( "k3 entry is wrong!", Set.of  ( "v3.1", "v3.2"  ), map.get ( "k3" ) );
	}
	
	/**
	 * It works in simple cases like this, but not always.
	 */
	@Test
	public void testOndexConcept ()
	{
		var graph = new MemoryONDEXGraph ( "test" );

		Map<ONDEXConcept, Set<String>> map = new MapOverMapDbSet<> (
			"testMap", c -> Integer.toString ( c.getId () ), 
			null, Serializer.STRING
		);
		
		
		var gmeta = graph.getMetaData ();
		
		var ev = gmeta.createEvidenceType ( "e0", "Test Evidence", "" );
		var ds = gmeta.createDataSource ( "ds0", "Test Data Source", "" );
		var cc = gmeta.createConceptClass ( "cc0", "Test CC", "", null );
		var c = graph.createConcept ( "c0", "", "Test concept", ds, cc, Set.of ( ev ) );
		
		map.put ( c, Set.of ( "s1", "s2", "s3" ) );
		
		Assert.assertEquals ( "concept storing didn't work!", Set.of  ( "s3", "s2", "s1"  ), map.get ( c ) );
	}	
}
