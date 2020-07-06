package net.sourceforge.ondex.ovtk2.ui.console;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.config.LuceneRegistry;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.ovtk2.reusable_functions.Filter;
import net.sourceforge.ondex.ovtk2.reusable_functions.Interactivity;
import net.sourceforge.ondex.ovtk2.reusable_functions.Statisitics;
import net.sourceforge.ondex.ovtk2.reusable_functions.Transform;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.ui.OVTK2ResourceAssesor;
import net.sourceforge.ondex.ovtk2.ui.console.functions.CommonFuntions;
import net.sourceforge.ondex.ovtk2.ui.console.functions.CustomImport;
import net.sourceforge.ondex.ovtk2.ui.console.functions.IEEEFunctions;
import net.sourceforge.ondex.ovtk2.ui.console.functions.OVTK2CustomFunctions;
import net.sourceforge.ondex.ovtk2.ui.console.functions.PoplarFunctions;
import net.sourceforge.ondex.ovtk2.ui.console.functions.ReportFunctions;
import net.sourceforge.ondex.ovtk2.ui.menu.actions.FileMenuAction;
import net.sourceforge.ondex.ovtk2.util.OVTKProgressMonitor;
import net.sourceforge.ondex.scripting.BasicInterpretationController;
import net.sourceforge.ondex.scripting.CommandInterpreter;
import net.sourceforge.ondex.scripting.FunctionException;
import net.sourceforge.ondex.scripting.InterpretationController;
import net.sourceforge.ondex.scripting.OutputPrinter;
import net.sourceforge.ondex.scripting.ProxyTemplate;
import net.sourceforge.ondex.scripting.TemplateBuilder;
import net.sourceforge.ondex.scripting.base.JavaProxyTemplate;
import net.sourceforge.ondex.scripting.base.UniversalProxyTemplateBuilder;
import net.sourceforge.ondex.scripting.javascript.JSInterpreter;
import net.sourceforge.ondex.scripting.ui.CommandEvent;
import net.sourceforge.ondex.scripting.ui.CommandLine;
import net.sourceforge.ondex.scripting.wrappers.ContextualReferenceResolver;
import net.sourceforge.ondex.scripting.wrappers.OndexScriptingInitialiser;
import net.sourceforge.ondex.tools.threading.monitoring.IndeterminateProcessAdapter;
import net.sourceforge.ondex.workflow.engine.Engine;

/**
 * @author lysenkoa This class sets up the scripting environment and configures
 *         GUI components used for it
 */
@SuppressWarnings("unused")
public class OVTKScriptingInitialiser extends OndexScriptingInitialiser {
	private static CommandLine cmd;

	/**
	 * Initialises the scripting environment and returns command line
	 * 
	 * @return CommandLine
	 */
	protected static void initialiseAspectBuilder() {

		OndexScriptingInitialiser.initialiseProxyTemplateBuilder();
		OndexScriptingInitialiser.setGraphResolver(new OVTKGraphResolver());
		/*
		 * Implementation - custom methods and functions know how to access all
		 * of these objects
		 */
		// OVTK2Desktop
		try {
			proxyTemplateBuilder.addRootMethod(UniversalProxyTemplateBuilder.getMethodByName(OVTK2Desktop.class, "getInstance"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// JUNGGraph
		try {
			proxyTemplateBuilder.addRootMethod(UniversalProxyTemplateBuilder.getMethodByName(OVTK2PropertiesAggregator.class, "getONDEXJUNGGraph"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ONDEXMetaGraph
		try {
			proxyTemplateBuilder.addRootMethod(UniversalProxyTemplateBuilder.getMethodByName(OVTK2PropertiesAggregator.class, "getMetaGraph"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// VisualizationViewer
		try {
			proxyTemplateBuilder.addRootMethod(UniversalProxyTemplateBuilder.getMethodByName(OVTK2PropertiesAggregator.class, "getVisualizationViewer"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// OVTK2PropertiesAggregator
		try {
			proxyTemplateBuilder.addRootMethod(UniversalProxyTemplateBuilder.getMethodByName(OVTK2ResourceAssesor.class, "getSelectedViewer"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// OVTK2MetaGraph
		try {
			proxyTemplateBuilder.addRootMethod(UniversalProxyTemplateBuilder.getMethodByName(OVTK2ResourceAssesor.class, "getSelectedMetagraph"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Only used internally
		try {
			proxyTemplateBuilder.addRootMethod(UniversalProxyTemplateBuilder.getMethodByName(OVTK2Desktop.class, "getDesktopResources"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			proxyTemplateBuilder.addRootMethod(UniversalProxyTemplateBuilder.getMethodByName(OVTKScriptingInitialiser.class, "getCommandLine"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			proxyTemplateBuilder.addAllMethodsAsFunctions(net.sourceforge.ondex.ovtk2.reusable_functions.VisualisationExtension.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			proxyTemplateBuilder.addAllMethodsAsFunctions(ReportFunctions.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// allow execution of plugins by script
		try {
			proxyTemplateBuilder.addFunctionMethodByName(OVTK2CustomFunctions.class, "applyPlugin", "applyPlugin");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// adds all methods in the FilterFunctions and Transform class
		proxyTemplateBuilder.addAllMethodsAsFunctions(Filter.class);
		proxyTemplateBuilder.addAllMethodsAsFunctions(Transform.class);
		proxyTemplateBuilder.addAllMethodsAsFunctions(Statisitics.class);

		// adds some specific functions for the Poplar graph (keywan)
		proxyTemplateBuilder.addAllMethodsAsFunctions(PoplarFunctions.class);

		/* Global Functions */
		try {
			proxyTemplateBuilder.addAllMethodsAsFunctions(IEEEFunctions.class);
		}

		catch (Exception e) {
			e.printStackTrace();
		}
		/*
		 * try{ proxyTemplateBuilder.addFunctionMethodByName(Class.forName(
		 * "net.sourceforge.ondex.ovtk2.annotator.gds2edge.GDS2EdgeAnnotator"),
		 * "presetMaxMin", "presetMaxMin"); }catch(ClassNotFoundException e){
		 * e.printStackTrace(); }
		 */
		proxyTemplateBuilder.addAllMethodsAsFunctions(Interactivity.class);
		try {
			proxyTemplateBuilder.addAllMethodsAsFunctions(net.sourceforge.ondex.ovtk2.reusable_functions.Annotation.class);
			proxyTemplateBuilder.addAllMethodsAsFunctions(CustomImport.class);
			// proxyTemplateBuilder.addAllMethodsAsFunctions(Class.forName("net.sourceforge.ondex.xten.scripting.jython.JythonFunctions"));
			proxyTemplateBuilder.addAllMethodsAsFunctions(CommonFuntions.class);
			// proxyTemplateBuilder.addAllMethodsAsFunctions(Class.forName("net.sourceforge.ondex.xten.scripting.jython.JythonFunctions"));
			// proxyTemplateBuilder.addFunctionMethodByName(CommandLine.class,
			// "print","print");
			// proxyTemplateBuilder.addFunctionMethodByName(OVTK2Desktop.class,
			// "loadGraph", "viewGraph");
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		 * Convienince Methods
		 * proxyTemplateBuilder.addConvinience(ONDEXConcept.class, int.class,
		 * UniversalProxyTemplateBuilder.getMethodByName(
		 * OVTKScriptingInitialiser.class, "convConcept"));
		 * proxyTemplateBuilder.addConvinience(ONDEXRelation.class, int.class,
		 * UniversalProxyTemplateBuilder.getMethodByName(
		 * OVTKScriptingInitialiser.class, "convRelation"));
		 */
	}

	public static OutputPrinter getCommandLine() {
		// check if there is an active viewer present
		OVTK2PropertiesAggregator viewer = (OVTK2PropertiesAggregator) OVTK2Desktop.getDesktopResources().getSelectedViewer();
		if (viewer == null) {
			FileMenuAction.getInstance().actionPerformed(new ActionEvent(OVTK2Desktop.getInstance(), ActionEvent.ACTION_PERFORMED, "new"));
		}
		return getCommandLine(viewer);
	}

	public static OutputPrinter getCommandLine(OVTK2PropertiesAggregator viewer) {
		if (cmd != null)
			return cmd;

		OVTKScriptingInitialiser.initialiseAspectBuilder();
		cmd = new CommandLineEx();
		mainOutputPrinter = cmd;
		cmd.print("Loading...");

		new Thread() {
			public void run() {
				try {
					// RInterpreter ri = RInterpreter.getInstance();
					// create scripting reference if needed
					JavaProxyTemplate proxyTemplate = getProxyTemplateWithDoc();
					JSInterpreter jsi = new JSInterpreter();
					jsi.setProcessingCheckpoint(new VisualizationHandler());
					net.sourceforge.ondex.scripting.sparql.SPARQLInterpreter sparqy = net.sourceforge.ondex.scripting.sparql.SPARQLInterpreter.getCurrentInstance();
					sparqy.setProcessingCheckpoint(new VisualizationHandler());
					InterpretationController ic = new BasicInterpretationController(new TemplateBuilder[] {}, new ProxyTemplate[] { proxyTemplate }, new CommandInterpreter[] { jsi });
					ic.addInterpreter(new OvtkECI(ic));

					if (sparqy.configure()) {
						ic.addInterpreter(sparqy);
					}

					ic.setInterpreterOrder(OvtkECI.class, JSInterpreter.class);
					cmd.setCommandInterpreter(ic);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
		return cmd;
	}

	public static ONDEXConcept convConcept(OVTK2PropertiesAggregator viewer, int id) throws FunctionException, NullValueException, AccessDeniedException {
		ONDEXConcept result = viewer.getONDEXJUNGGraph().getConcept(id);
		if (result == null) {
			throw new FunctionException("Concept with id doea not exist.", -4);
		}
		return result;
	}

	public static ONDEXRelation convRelation(OVTK2PropertiesAggregator viewer, int id) throws FunctionException, NullValueException, AccessDeniedException {
		ONDEXRelation result = viewer.getONDEXJUNGGraph().getRelation(id);
		if (result == null) {
			throw new FunctionException("Concept with id doea not exist.", -4);
		}
		return result;
	}

	/**
	 * Method for testing filters in the frontend No speed optimisations! Use on
	 * small graphs for testing purposes only!
	 * 
	 * @param name
	 *            - fully qualified class name
	 * @param viewer
	 *            - OVTK2PropertiesAggregator to use with the filter
	 * @param args
	 *            - filter arguments, as in workflow & in the correct order
	 * @throws FunctionException
	 */
	@SuppressWarnings("unchecked")
	private static void applyFilter(String name, OVTK2PropertiesAggregator viewer, String... args) throws FunctionException {
		try {
			Class<ONDEXFilter> cf = (Class<ONDEXFilter>) Class.forName(name);
			ONDEXFilter filter = cf.getDeclaredConstructor(new Class<?>[] {}).newInstance();
			ONDEXGraph graphInput = viewer.getONDEXJUNGGraph();
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
					contexts = new HashSet<ONDEXConcept>(relation.getTags());
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

	private static class CommandLineEx extends CommandLine {
		/**
		 * generated
		 */
		private static final long serialVersionUID = 3147517962832408274L;

		public CommandLineEx() {
			super();
		}

		@Override
		public void fireCommandEvent(CommandEvent evt) {
			CommandProcess m = new CommandProcess(this, evt);
			m.start();
			JFrame parent = null;
			if (!Config.isApplet)
				parent = OVTK2Desktop.getInstance().getMainFrame();
			OVTKProgressMonitor.start(parent, "Please wait...", m);
		}

		public void superFireCommandEvent(CommandEvent evt) {
			super.fireCommandEvent(evt);
		}
	}

	private static class CommandProcess extends IndeterminateProcessAdapter {
		private final CommandLineEx cl;
		private final CommandEvent evt;

		public CommandProcess(CommandLineEx cl, CommandEvent evt) {
			this.cl = cl;
			this.evt = evt;
		}

		@Override
		public void task() {
			cl.superFireCommandEvent(evt);
			cl.waitForCommandCompletion();
		}
	}

	private static class OVTKMetaDataResolver implements ContextualReferenceResolver<ONDEXGraphMetaData> {
		@Override
		public ONDEXGraphMetaData resolveRef(Object arg) throws Exception {
			ONDEXGraphMetaData result = ((OVTK2PropertiesAggregator) arg).getONDEXJUNGGraph().getMetaData();

			if (result == null) {
				throw new FunctionException("No meta data is available.", -4);
			}
			return result;
		}
	}

	private static class OVTKGraphResolver implements ContextualReferenceResolver<ONDEXGraph> {
		@Override
		public ONDEXGraph resolveRef(Object arg) throws Exception {
			try {

				return OVTK2Desktop.getDesktopResources().getSelectedViewer().getONDEXJUNGGraph();
			} catch (NullPointerException e) {
				throw new FunctionException("Error - unable to find an ONDEX graph! Create the new graph to correct this error.", -1);
			}
		}
	}

	/**
	 * public static AbstractONDEXGraphMetaData
	 * getMetaData(OVTK2PropertiesAggregator viewer) throws FunctionException {
	 * mdResolver AbstractONDEXGraphMetaData result = viewer.getJUNGGraph()
	 * .getMetaData(); if (result == null) { throw new
	 * FunctionException("No meta data is available.", -4); } return result; }
	 */

	private static LuceneEnv loadLuceneEnv(ONDEXGraph graph) {

		Engine engine = Engine.getEngine();
		return engine.getIndex(graph, graph.getName());
	}
}