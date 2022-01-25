package net.sourceforge.ondex.core.graphimpl.mapdb;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * TODO: comment me!
 *
 * @author brandizi
 *         <dl>
 *           <dt>Date:</dt>
 *           <dd>19 Jan 2022</dd>
 *         </dl>
 *
 */
public class MapOverMapDbSet<K, V> extends AbstractMap<K, Set<V>>
{
	private String mapName;
	private Function<K, String> key2StringConverter;
	private static String mapDbDir = System.getProperty ( "java.io.tmpdir" );
	
	private static BiFunction<String, String, DB> mapDbDatabaseProvider = (dbPath, mapName) -> 
		DBMaker
	  .fileDB ( dbPath + '/' + mapName + "-mapdb.db" )
	  .closeOnJvmShutdown ()
	  .make ();	

	private DB mapdb;

	private Set<K> keys;
	
	
	@SuppressWarnings ( "unchecked" )
	public MapOverMapDbSet ( String mapName, Function<K, String> key2StringConverter )
	{
		super ();
		this.mapName = mapName;
		this.key2StringConverter = key2StringConverter;
		this.mapdb = mapDbDatabaseProvider.apply ( mapDbDir, mapName ); 
		this.keys = (Set<K>) mapdb.hashSet ( getKeysSetName () ).createOrOpen ();
	}
	
	
	public static <V> MapOverMapDbSet<Integer, V> ofIntKeys ( String mapName )
	{
		return new MapOverMapDbSet<> ( mapName, i -> Integer.toString ( i ) );
	}
	
	public static <V> MapOverMapDbSet<String, V> ofStringKeys ( String mapName )
	{
		return new MapOverMapDbSet<> ( mapName, Function.identity () );
	}
	
	
	
	private String getSetName ( K key ) {
		return mapName + ":" + key2StringConverter.apply ( key );
	}
	
	private String getKeysSetName () {
		return mapName + ":_keys_";
	}
	

	@SuppressWarnings ( "unchecked" )
	private Set<V> getValuesSet ( K key ) {
		return (Set<V>) mapdb.hashSet ( getSetName ( key ) ).createOrOpen ();
	}


	private void removeValuesSet ( K key ) {
		mapdb.delete ( getSetName ( key ) );
	}
		
	private Set<V> setValuesSet ( K key, Set<V> values )
	{
		var vals = this.getValuesSet ( key );
		vals.clear (); 
		vals.addAll ( values );
		return vals;
	}
	
	
	private class Entry extends AbstractMap.SimpleEntry<K, Set<V>>
	{
		private static final long serialVersionUID = 4169765238868828530L;

		public Entry ( K key )
		{
			super ( key, getValuesSet ( key ) );
		}

		@Override
		public Set<V> setValue ( Set<V> values )
		{
			return setValuesSet ( this.getKey (), values );
		}
	}
	
	private class EntrySet extends AbstractSet<Map.Entry<K, Set<V>>>
	{
		public final int size ()
		{
			return keys.size ();
		}

		public final void clear ()
		{
			MapOverMapDbSet.this.clear ();
		}

		public final Iterator<Map.Entry<K, Set<V>>> iterator ()
		{
			return keys.stream ()
			.map ( k -> (Map.Entry<K, Set<V>>) new Entry ( k ) )
			.iterator ();
		}

		public final boolean contains ( Object o )
		{
			if ( ! ( o instanceof Map.Entry ) ) return false;
			
			@SuppressWarnings ( "unchecked" ) // TODO: check type instead?
			var e = (Map.Entry<K, Set<V>>) o;
			var k = e.getKey ();
			if ( !keys.contains ( k ) ) return false;

			Set<V> values = getValuesSet ( k );
			return values.equals ( e.getValue () );
		}

		public final boolean remove ( Object o )
		{
			if ( !contains ( o ) ) return false;
			
			Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			@SuppressWarnings ( "unchecked" )
			K key = (K) e.getKey ();
			
			MapOverMapDbSet.this.remove ( key );
			
			return true;
		}

		public final Spliterator<Map.Entry<K, Set<V>>> spliterator ()
		{
			return keys.stream ()
			.map ( k -> (Map.Entry<K, Set<V>>) new Entry ( k ) )
			.spliterator ();
		}

		public final void forEach ( Consumer<? super Map.Entry<K, Set<V>>> action )
		{
			keys.forEach ( k -> action.accept ( new Entry ( k ) ) );
		}
	}

	@Override
	public Set<Map.Entry<K, Set<V>>> entrySet ()
	{
		return new EntrySet (); 
	}

	@Override
	public boolean containsKey ( Object key )
	{
		return keys.contains ( key );
	}

	@SuppressWarnings ( "unchecked" )
	@Override
	public Set<V> get ( Object key )
	{
		if ( !keys.contains ( key ) ) return null;
		return getValuesSet ( (K) key );
	}


	@Override
	public int size ()
	{
		return keys.size ();
	}

	@Override
	public Set<V> put ( K key, Set<V> values )
	{
		if ( values == null )
		{
			if ( !keys.contains ( key ) ) return null;
			return remove ( key );
		}

		if ( !keys.contains ( key ) ) keys.add ( key );
		return this.setValuesSet ( key, values );
	}

	public Set<V> createEntry ( K key, V value )
	{
		return this.put ( key, Set.of ( value ) );
	}

	public Set<V> createEntry ( K key )
	{
		return this.put ( key, Set.of () );
	}

	public boolean addEntry ( K key, V value )
	{
		if ( !keys.contains ( key ) ) {
			createEntry ( key, value );
			return true;
		}
		Set<V> vals = this.getValuesSet ( key );
		return vals.add ( value );
	}
	
	
	
	@SuppressWarnings ( "unchecked" )
	@Override
	public Set<V> remove ( Object key )
	{
		this.removeValuesSet ( (K) key );
		keys.remove ( key );
		return null;
	}


	@Override
	public void clear ()
	{
		keys.parallelStream ().forEach ( this::remove );
		keys.clear ();
	}

	@Override
	public Set<K> keySet ()
	{
		return Collections.unmodifiableSet ( keys );
	}


	private Set<V> properNewValue ( K key, Set<V> newValue )
	{
		return newValue == null ? null : get ( key );
	}
	
	@Override
	public Set<V> computeIfAbsent ( K key, Function<? super K, ? extends Set<V>> mappingFunction )
	{
		return properNewValue ( key, super.computeIfAbsent ( key, mappingFunction ) );
	}


	@Override
	public Set<V> computeIfPresent ( K key, BiFunction<? super K, ? super Set<V>, ? extends Set<V>> remappingFunction )
	{
		return properNewValue ( key, super.computeIfPresent ( key, remappingFunction ) );
	}


	@Override
	public Set<V> compute ( K key, BiFunction<? super K, ? super Set<V>, ? extends Set<V>> remappingFunction )
	{
		return properNewValue ( key, super.compute ( key, remappingFunction ) );
	}


	@Override
	public Set<V> merge ( 
		K key, Set<V> value, BiFunction<? super Set<V>, ? super Set<V>, ? extends Set<V>> remappingFunction )
	{
		return properNewValue ( key, super.merge ( key, value, remappingFunction ) );
	}


	public static String getMapDbDir ()
	{
		return mapDbDir;
	}


	public static void setMapDbDir ( String mapDbDir )
	{
		MapOverMapDbSet.mapDbDir = mapDbDir;
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
