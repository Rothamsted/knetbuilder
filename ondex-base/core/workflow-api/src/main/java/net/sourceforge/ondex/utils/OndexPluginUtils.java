package net.sourceforge.ondex.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import com.machinezoo.noexception.Exceptions;

import net.sourceforge.ondex.ONDEXPlugin;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.RequiresGraph;
import net.sourceforge.ondex.UncheckedPluginException;
import net.sourceforge.ondex.core.ONDEXGraph;
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
	 */
	public static <T> T runPlugin ( ONDEXPlugin plugin, Map<String, Object> args ) throws UncheckedPluginException
	{
		try
		{
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
	 * Wrapper that expects a {@link RequiresGraph} instance for plugin and invokes it with the paramter graph.
	 * If graph is null, it just ignores the parameter and invokes {@link #runPlugin(ONDEXPlugin, Map)}
	 *  
	 */
	public static <T> T runPlugin ( ONDEXPlugin plugin, ONDEXGraph graph, Map<String, Object> args )
		throws UncheckedPluginException
	{
		if ( graph != null )
		{	
			if ( ! ( plugin instanceof RequiresGraph ) )
				throw new IllegalArgumentException ( "Can't invoke a plug-in that doesn't accept a graph with this method" );
	
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
}
