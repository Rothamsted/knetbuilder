package net.sourceforge.ondex.ovtk2.ui.console.functions;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import net.sourceforge.ondex.ONDEXPlugin;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.RequiresGraph;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.config.LuceneRegistry;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.init.PluginRegistry;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.OVTKProgressMonitor;
import net.sourceforge.ondex.ovtk2.util.PluginUtils;
import net.sourceforge.ondex.scripting.FunctionException;
import net.sourceforge.ondex.tools.DirUtils;
import net.sourceforge.ondex.tools.threading.monitoring.SimpleMonitor;
import net.sourceforge.ondex.workflow.engine.Engine;

/**
 * The custom functions for OVTK can be added here. The access to all of the
 * objects defined in the 'Application Base' section of OVTK scripting
 * initialiser will be resolved by the scripting wrapper, all of the remaining
 * arguments will be left in the prototype to be supplied when function is
 * called at run-time. To view the prototypes generated delete the
 * 'Scripting_ref.htm' file in the OVTK root folder & run the application. The
 * new file will be generated that will include all of the new functions added.
 * The name of the new function should be defined in the aspect, for examples
 * see OVTKScriptingInitialiser
 * 
 * @author lysenkoa
 */
@SuppressWarnings("unused")
public class OVTK2CustomFunctions {
	private OVTK2CustomFunctions() {
	}

	/**
	 * Methods that make filters available. Requires plugins to be on the
	 * classpath. No speed optimisations! Use on small graphs for testing
	 * purposes only!
	 * 
	 * @param viewer
	 * @param s
	 * @throws FunctionException
	 */
	/*
	 * public static void cleanupuniprotFilter(OVTK2PropertiesAggregator viewer)
	 * throws FunctionException{
	 * applyFilter("net.sourceforge.ondex.filter.cleanupuniprot.Filter", s,
	 * viewer); } public static void clonerFilter(OVTK2PropertiesAggregator
	 * viewer) throws FunctionException{
	 * applyFilter("net.sourceforge.ondex.filter.cloner.Filter", s, viewer); }
	 * public static void conceptclassFilter(OVTK2PropertiesAggregator viewer)
	 * throws FunctionException{
	 * applyFilter("net.sourceforge.ondex.filter.conceptclass.Filter", s,
	 * viewer); } public static void
	 * optimalpathsFilter(OVTK2PropertiesAggregator viewer) throws
	 * FunctionException{
	 * applyFilter("net.sourceforge.ondex.filter.optimalpaths.Filter", s,
	 * viewer); } public static void
	 * relationneighboursFilter(OVTK2PropertiesAggregator viewer) throws
	 * FunctionException{
	 * applyFilter("net.sourceforge.ondex.filter.relationneighbours.Filter", s,
	 * viewer); } public static void
	 * realtiontypesetFilter(OVTK2PropertiesAggregator viewer) throws
	 * FunctionException{
	 * applyFilter("net.sourceforge.ondex.filter.realtiontypeset.Filter", s,
	 * viewer); } public static void unconnectedFilter(OVTK2PropertiesAggregator
	 * viewer) throws FunctionException{
	 * applyFilter("net.sourceforge.ondex.filter.unconnected.Filter", s,
	 * viewer); }
	 */

	/**
	 * Methods that make mapping methods available. Requires plugins to be on
	 * the classpath. No speed optimisations! Use on small graphs for testing
	 * purposes only!
	 * 
	 * @param viewer
	 * @throws FunctionException
	 */
	public static void accessionbasedMapping(OVTK2PropertiesAggregator viewer) throws FunctionException {
		applyPlugin("net.sourceforge.ondex.mapping.accessionbased.Mapping", viewer.getONDEXJUNGGraph());
	}

	/**
	 * Method for testing filters in the frontend No speed optimisations! Use on
	 * small graphs for testing purposes only!
	 * 
	 * @param name
	 *            - fully qualified class name
	 * @param graphInput
	 *            - OVTK2PropertiesAggregator to use with the filter
	 * @param args
	 *            - filter arguments, as in workflow & in the correct order
	 * @throws FunctionException
	 */
	@SuppressWarnings("unchecked")
	private static void applyFilter(String name, ONDEXGraph graphInput, String... args) throws FunctionException {
		try {
			Class<ONDEXFilter> cf = (Class<ONDEXFilter>) Class.forName(name);
			ONDEXFilter filter = cf.getDeclaredConstructor(new Class<?>[] {}).newInstance();
			// ActionListener[] als = graphInput.getActionListeners().toArray(
			// new ActionListener[0]);
			// for (ActionListener al : als)
			// graphInput.removeActionListener(al);
			if (args.length > 0 || filter.requiresIndexedGraph()) {
				ONDEXPluginArguments fa = new ONDEXPluginArguments(filter.getArgumentDefinitions());
				ArgumentDefinition<?>[] ad = filter.getArgumentDefinitions();
				for (int i = 0; i < ad.length; i++) {
					fa.addOption(ad[i].getName(), ad[i].parseString(args[i]));
				}
				if (filter.requiresIndexedGraph()) {
					LuceneEnv lenv = loadLuceneEnv(graphInput);
					LuceneRegistry.sid2luceneEnv.put(graphInput.getSID(), lenv);
				}
				filter.setArguments(fa);
			}
			Set<ONDEXConcept> contexts = null;
			filter.setONDEXGraph(graphInput);
			filter.start();
			for (ONDEXRelation relation : filter.getVisibleRelations()) {
				if (contexts == null) {
					contexts = relation.getTags();
				} else {
					contexts.addAll(relation.getTags());
				}
			}

			Set<ONDEXRelation> relationsVisible = filter.getVisibleRelations();

			for (ONDEXRelation relation : graphInput.getRelations()) {
				if (!relationsVisible.contains(relation)) {
					graphInput.deleteRelation(relation.getId());
				}
			}

			for (ONDEXConcept concept : filter.getVisibleConcepts()) {
				if (contexts == null || contexts.size() == 0) {
					contexts = BitSetFunctions.or(concept.getTags(), graphInput.getConceptsOfTag(concept));
				} else {
					contexts.addAll(BitSetFunctions.or(concept.getTags(), graphInput.getConceptsOfTag(concept)));
				}
			}

			Set<ONDEXConcept> conceptsVisible = filter.getVisibleConcepts();

			for (ONDEXConcept concept : graphInput.getConcepts()) {
				if (!conceptsVisible.contains(concept) && (contexts == null || !contexts.contains(concept))) {
					graphInput.deleteConcept(concept.getId());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FunctionException("This optional feature is not availible, as the required libaray was not found.", -4);
		} finally {
			System.runFinalization();
		}
	}

	/**
	 * Invoke a Plugin, which is available for the Workflow Integrator.
	 * 
	 * @param name
	 *            The full qualified name of the Plugin. This name is shown for
	 *            each Plugin in the Integrator's documentation panel.
	 * @param graphInput
	 *            Graph the Plugin should work on.
	 * @param args
	 *            String arguments to be passed to the plugin. If a single
	 *            Argument accepts multiple lines, those are separated using \n.
	 *            If you don't want to specifiy a certain Argument, but use the
	 *            Plugin's default, pass <code>null</code>.
	 * @throws FunctionException
	 */
	@SuppressWarnings("unchecked")
	public static void applyPlugin(String name, ONDEXGraph graphInput, String... args) throws FunctionException {

		SimpleMonitor monitor = new SimpleMonitor("initializing PluginRegistry", 1);
		OVTKProgressMonitor.start("applyPlugin", monitor);
		try {
			PluginUtils.initPluginRegistry();
		} catch (Exception e) {
			e.printStackTrace();
			ErrorDialog.show(false, new PluginUtils.MissingPluginException("PluginRegistry could not be initialized. applyPlugin() failed.", e), Thread.currentThread());
			return;
		} finally {
			monitor.complete();
		}

		ArgumentDefinition<?>[] argumentDefinitions = null;
		try {
			Class<ONDEXPlugin> cf = (Class<ONDEXPlugin>) Class.forName(name, true, PluginRegistry.getInstance().getClassLoader());
			ONDEXPlugin plugin = cf.getDeclaredConstructor(new Class<?>[] {}).newInstance();
			argumentDefinitions = plugin.getArgumentDefinitions();
			System.out.println("Executing plugin: " + plugin.getClass().getCanonicalName());

			if (args.length > 0 || plugin.requiresIndexedGraph()) {
				ONDEXPluginArguments fa = new ONDEXPluginArguments(plugin.getArgumentDefinitions());
				for (int i = 0; i < argumentDefinitions.length; i++) {
					if (args[i] != null) { // optional parameters may be null
						if (argumentDefinitions[i].isAllowedMultipleInstances()) {
							fa.addOptions(argumentDefinitions[i].getName(), args[i].split("\n"));
						} else {
							fa.addOption(argumentDefinitions[i].getName(), argumentDefinitions[i].parseString(args[i]));
						}
					}
				}
				if (plugin.requiresIndexedGraph()) {
					LuceneEnv lenv = loadLuceneEnv(graphInput);
					LuceneRegistry.sid2luceneEnv.put(graphInput.getSID(), lenv);
				}
				plugin.setArguments(fa);
				System.out.println("Setting arguments: " + fa.getOptions());
			}
			if (plugin instanceof RequiresGraph) {
				System.out.println("Setting graph: " + graphInput.getName());
				((RequiresGraph) plugin).setONDEXGraph(graphInput);
			}
			plugin.start();
		} catch (Exception e) {
			e.printStackTrace();
			// give the user a little hint about the arguments to pass to the
			// given plugin
			StringBuffer argumentList = new StringBuffer();
			if (argumentDefinitions != null)
				for (ArgumentDefinition<?> ad : argumentDefinitions)
					argumentList.append(ad.getName() + "\n");
			else
				argumentList.append("unknown");
			throw new FunctionException("applyPlugin for plugin " + name + " failed: " + e + "\nAccepted arguments are:\n" + argumentList.toString(), -4);
		} finally {
			System.runFinalization();
		}
	}

	private static LuceneEnv loadLuceneEnv(ONDEXGraph graph) {
		Engine engine = Engine.getEngine();
		return engine.getIndex(graph, graph.getName());
	}

	private static boolean hasEvidenceType(ONDEXConcept candidate, EvidenceType ev) {
		boolean result = false;
		for (EvidenceType et : candidate.getEvidence()) {
			if (et.equals(ev)) {
				result = true;
				break;
			}
		}
		return result;
	}

}
