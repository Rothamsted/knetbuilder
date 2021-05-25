package net.sourceforge.ondex;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import com.machinezoo.noexception.Exceptions;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.ONDEXListener;
import net.sourceforge.ondex.exception.type.PluginException;
import net.sourceforge.ondex.init.ArgumentDescription;
import net.sourceforge.ondex.producer.ProducerONDEXPlugin;
import uk.ac.ebi.utils.exceptions.ExceptionUtils;

/**
 * The root for all the Ondex plug-ins.
 * 
 * @author hindlem
 * @author additions by Marco Brandizi
 */
public interface ONDEXPlugin
{

	/**
	 * Returns the internal string identifier of the ONDEX producer - must be unique - which is also used in the name of
	 * the XML attribute in workflow files.
	 *
	 * @return String
	 */
	public abstract String getId ();

	/**
	 * Returns the human readable name of the ONDEX producer.
	 *
	 * @return String
	 */
	public abstract String getName ();

	/**
	 * Returns the version number of the ONDEX producer.
	 *
	 * @return String
	 */
	public abstract String getVersion ();

	/**
	 * Returns arguments valid for this producer
	 *
	 * @return ArgumentDefinition<?>[]
	 */
	public abstract ArgumentDefinition<?>[] getArgumentDefinitions ();

	/**
	 * Starts the producer process.
	 */
	public abstract void start () throws Exception;

	/**
	 * Sets the arguments the producer should use.
	 *
	 * @param args E
	 */
	public abstract void setArguments ( ONDEXPluginArguments args ) throws InvalidPluginArgumentException;

	/**
	 * Returns the actual arguments.
	 *
	 * @return E
	 */
	public abstract ONDEXPluginArguments getArguments ();

	/**
	 * Does this producer require an indexed graph
	 *
	 * @return boolean
	 */
	public abstract boolean requiresIndexedGraph ();

	/**
	 * Returns a list of Validator names that the parser relies on.
	 *
	 * @return String[]
	 */
	public abstract String[] requiresValidators ();

	/**
	 * This method can return the custom PluginDescription definition. May return null, in which case the definition
	 * will be generated from doclet annotation, as per default behaviour.
	 *
	 * @return PluginDescription, describing this producer.
	 * @param position
	 */
	public abstract Collection<ArgumentDescription> getArgumentDescriptions ( int position );

	public void addONDEXListener ( ONDEXListener l );

	public void removeONDEXListener ( ONDEXListener l );

	public ONDEXListener[] getONDEXListeners ();

	/**
	 * Helper to invoke a plug-in as stand-alone, outside of the workflow.
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
			var pargDefs = plugin.getArgumentDefinitions ();
			var pargs = new ONDEXPluginArguments ( pargDefs );
			args.forEach ( Exceptions.sneak ().fromBiConsumer ( ( k, v ) -> pargs.setOption ( k, v ) ) );

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
	 *  
	 */
	public static <T> T runPlugin ( ONDEXPlugin plugin, ONDEXGraph graph, Map<String, Object> args )
		throws UncheckedPluginException
	{
		if ( ! ( plugin instanceof RequiresGraph ) )
			throw new IllegalArgumentException ( "Can't invoke a plug-in that doesn't accept a graph with this method" );

		((RequiresGraph) plugin).setONDEXGraph ( graph );
		return runPlugin ( plugin, args );
	}

	/**
	 * Wrapper to auto-instantiate the plugin using the empty constructor.
	 */
	public static <T> T runPlugin ( Class<? extends ONDEXPlugin> pluginCls, Map<String, Object> args ) 
		throws UncheckedPluginException
	{
		try
		{
			var plugin = ConstructorUtils.invokeConstructor ( pluginCls );
			return runPlugin ( plugin, args );
		}
		catch ( NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ex )
		{
			throw ExceptionUtils.buildEx ( 
				IllegalArgumentException.class, ex, 
				"Error with instantiating the plug-in %s: %s",
				pluginCls.getName (), ex.getMessage ()
			);
		}
	}

	/**
	 * Wrapper to auto-instantiate the plugin using the empty constructor.
	 */
	public static <T> T runPlugin ( Class<? extends ONDEXPlugin> pluginCls, ONDEXGraph graph, Map<String, Object> args )
			throws UncheckedPluginException
	{
		try
		{
			var plugin = ConstructorUtils.invokeConstructor ( pluginCls );
			return runPlugin ( plugin, graph, args );
		}
		catch ( NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ex )
		{
			throw ExceptionUtils.buildEx ( 
				IllegalArgumentException.class, ex, 
				"Error with instantiating the plug-in %s: %s",
				pluginCls.getName (), ex.getMessage ()
			);
		}
	}
}
