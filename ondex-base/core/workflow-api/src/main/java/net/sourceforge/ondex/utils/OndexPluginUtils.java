package net.sourceforge.ondex.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import com.machinezoo.noexception.Exceptions;

import net.sourceforge.ondex.ONDEXPlugin;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.RequiresGraph;
import net.sourceforge.ondex.UncheckedPluginException;
import net.sourceforge.ondex.config.LuceneRegistry;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.event.ONDEXListener;
import net.sourceforge.ondex.logging.ONDEXLogger;
import net.sourceforge.ondex.producer.ProducerONDEXPlugin;
import uk.ac.ebi.utils.exceptions.ExceptionUtils;

/**
 * Utilities for plug-ins.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Jul 2021</dd></dl>
 *
 */
public class OndexPluginUtils
{
	private static String indexDirsBasePath = System.getProperty ( "java.io.tmpdir" ) + "/ondex-indices";
	
	private OndexPluginUtils () {}

	/**
	 * Helper to invoke a plug-in as stand-alone, outside of the workflow.
	 * 
	 * @param args these are translated into {@link ONDEXPluginArguments}. In case of multi-value
	 * 				arguments, use list values.
	 * 
	 * @return a result that depends on the plug-in type: if it's instance of {@link ProducerONDEXPlugin}, returns
	 *         {@link ProducerONDEXPlugin#collectResults() collectResults()}, if it's {@link RequiresGraph}, returns
	 *         {@link RequiresGraph#getGraph() the plugin's graph}, else returns null.
	 * 
	 * Note that this {@link #setupPluginLogger(ONDEXPlugin) auto-adds the default logger} to the plug-in.
	 */
	public static <T> T runPlugin ( ONDEXPlugin plugin, Map<String, Object> args ) throws UncheckedPluginException
	{
		try
		{			
			setupPluginLogger ( plugin );
			
			// Prepare the arguments from the map
			//
			var pargDefs = plugin.getArgumentDefinitions ();
			var pargs = new ONDEXPluginArguments ( pargDefs );
			args.forEach ( Exceptions.sneak ().fromBiConsumer ( ( argName, valObj ) -> 
				{ 
					@SuppressWarnings ( "unchecked" )
					Collection<Object> vlist = valObj instanceof Collection ? (Collection<Object>) valObj : List.of ( valObj );
					// if the arg is accepting a type other than string, addOption() will try to parse it
					// using ArgugmentDefition.parseString()
					vlist.forEach ( Exceptions.sneak ().consumer ( val -> pargs.addOption ( argName, val ) ) );
				})
			);
			
			// OK, now set them
			plugin.setArguments ( pargs );
			plugin.start ();
	
			if ( plugin instanceof ProducerONDEXPlugin ) ( (ProducerONDEXPlugin) plugin ).collectResults ();
			if ( plugin instanceof RequiresGraph ) ( (RequiresGraph) plugin ).getGraph ();
			return null;
		}
		catch ( Exception ex ) {
			throw new UncheckedPluginException ( "Error during plug-in execution: " + ex.getMessage (), ex );
		}
	}

	/**
	 * Wrapper that expects a {@link RequiresGraph} instance for plugin and invokes it with the parameter graph.
	 * If graph is null, it just ignores the parameter and invokes {@link #runPlugin(ONDEXPlugin, Map)}
	 *  
	 * Note that, if the plugin {@link ONDEXPlugin#requiresIndexedGraph() requires the graph to be indexed}, 
	 * {@link #getLuceneManager(ONDEXGraph)} is used. By default, this uses {@link #getIndexDirsBasePath()}
	 * as a default directory for the index. If you want to override these defaults, either set 
	 * your {@link #setIndexDirsBasePath(String) own index base path}, or invoke 
	 * {@link #getLuceneManager(ONDEXGraph, String, boolean)} in advance. 
	 * 
	 */
	public static <T> T runPlugin ( ONDEXPlugin plugin, ONDEXGraph graph, Map<String, Object> args )
		throws UncheckedPluginException
	{
		if ( graph != null )
		{	
			if ( ! ( plugin instanceof RequiresGraph ) )
				throw new IllegalArgumentException ( "Can't invoke a plug-in that doesn't accept a graph with this method" );
	
			if ( plugin.requiresIndexedGraph () ) getLuceneManager ( graph );
			
			((RequiresGraph) plugin).setONDEXGraph ( graph );
		}
		return runPlugin ( plugin, args );
	}

	/**
	 * Wrapper to auto-instantiate the plugin using the empty constructor.
	 */
	public static <T> T runPlugin ( Class<? extends ONDEXPlugin> pluginCls, ONDEXGraph graph, Map<String, Object> args )
		throws UncheckedPluginException
	{
		var plugin = createPlugin ( pluginCls );
		return runPlugin ( plugin, graph, args );
	}

	/**
	 * Wrapper to auto-instantiate the plugin using the empty constructor.
	 */
	public static <T> T runPlugin ( Class<? extends ONDEXPlugin> pluginCls, Map<String, Object> args ) 
		throws UncheckedPluginException
	{
		return runPlugin ( pluginCls, null, args );
	}

	/**
	 * Uses {@link #loadPluginClass(String)} to run a plug-in starting from its class's fully qualified name.
	 * 
	 */
	public static <T> T runPlugin ( String pluginFQN, ONDEXGraph graph, Map<String, Object> args )
		throws UncheckedPluginException
	{
		Class<ONDEXPlugin> pluginCls = loadPluginClass ( pluginFQN );
		return runPlugin ( pluginCls, graph, args );
	}
	
	public static <T> T runPlugin ( String pluginFQN, Map<String, Object> args )
		throws UncheckedPluginException
	{
		return runPlugin ( pluginFQN, null, args );
	}
	
	/**
	 * Simple wrapper to instantiate a plug-in starting from its class and empty constructor.
	 */
	public static <P extends ONDEXPlugin> P createPlugin ( Class<P> pluginCls )
	{
		try
		{
			return ConstructorUtils.invokeConstructor ( pluginCls );
		}
		catch ( NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ex )
		{
			throw ExceptionUtils.buildEx ( 
				UncheckedPluginException.class, ex,
				"Error while trying fetching the plug-in '%s': %s",
				pluginCls.getName (), ex.getMessage ()
			);
		}
	}
	
	/**
	 * Simple wrapper to create a plug-in starting from it's class' fully-qualified name.
	 * This uses {@link #loadPluginClass(String)}.
	 */
	public static <P extends ONDEXPlugin> P createPlugin ( String pluginFQN )
	{
		@SuppressWarnings ( "unchecked" )
		var cls = (Class<P>) loadPluginClass ( pluginFQN );
		return createPlugin ( cls );
	}
	
	/**
	 * Simple wrapper to load a plug-in class starting from its fully-qualified name.
	 */
	@SuppressWarnings ( "unchecked" )
	public static <P extends ONDEXPlugin> Class<P> loadPluginClass ( String pluginFQN )
	{
		try
		{
			return (Class<P>) Class.forName ( pluginFQN );
		}
		catch ( ClassNotFoundException ex )
		{
			throw new UncheckedPluginException ( "Can't find the ONDEX plug-in class: '" + pluginFQN + "'" );
		}
	}
	
	/**
	 * Adds an {@link ONDEXListener} to the {@link ONDEXPlugin#getONDEXListeners() plugin listeners}
	 * It first checks that the logger isn't already there, in which case it does nothing.
	 */
	public static void setupPluginLogger ( ONDEXPlugin plugin )
	{
		boolean alreadyExists = Optional.ofNullable ( plugin.getONDEXListeners () )
		.map ( listeners -> 
			Stream.of ( listeners )
			.anyMatch ( l -> l instanceof ONDEXLogger )
		)
		.orElse ( false );
		
		if ( alreadyExists ) return;
		plugin.addONDEXListener ( new ONDEXLogger () );
	}
	
	/**
	 * This is where this class creates Lucene indices, when needed.
	 * By default, it is under the OS's temp dir + "/ondex-indices".
	 * 
	 * @see #getLuceneManager(String).
	 */
	public static String getIndexDirsBasePath ()
	{
		return indexDirsBasePath;
	}

	public static void setIndexDirsBasePath ( String indexDirsBasePath )
	{
		OndexPluginUtils.indexDirsBasePath = indexDirsBasePath;
	}

	/**
	 *  
	 * Gets an index manager for this graph, using the same mechanism that the workflow engine uses.
	 * If a {@link LuceneEnv} already exists for this graph, it reuses it.
	 *  
	 * @param forceCreation if it's true, the index is (re)created from scratch, even if it exists. This means
	 *   the index files are deleted and the entry in {@link LuceneRegistry#sid2luceneEnv} for this graph
	 *   is updated. 
	 *  
	 */
	public static LuceneEnv getLuceneManager ( ONDEXGraph graph, String indexPath, boolean forceCreation )
	{
		long graphId = graph.getSID ();
		
		LuceneEnv luceneMgr = forceCreation ? null : LuceneRegistry.sid2luceneEnv.get ( graphId );
		if ( luceneMgr != null ) return luceneMgr;
				
		if ( indexPath == null )
			indexPath = indexDirsBasePath + "/graph-" + graphId;
		
		if ( !new File ( indexPath ).exists () ) forceCreation = true;
		
		LuceneEnv lenv = new LuceneEnv ( indexPath, forceCreation );
		lenv.setONDEXGraph ( graph );
		LuceneRegistry.sid2luceneEnv.put ( graph.getSID (), lenv );
		
		return lenv;
	}
	
	/**
	 * Defaults to false, the index on disk is possible reused.
	 */
	public static LuceneEnv getLuceneManager ( ONDEXGraph graph, String indexPath )
	{
		return getLuceneManager ( graph, indexPath, false );
	}

	/**
	 * Defaults to {@link #getIndexDirsBasePath()}.
	 */
	public static LuceneEnv getLuceneManager ( ONDEXGraph graph )
	{
		return getLuceneManager ( graph, null );
	}

}
