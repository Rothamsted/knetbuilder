package net.sourceforge.ondex.filter.conceptclassneighbours;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.algorithm.relationneighbours.DepthSensitiveRTValidator;
import net.sourceforge.ondex.algorithm.relationneighbours.LogicalRelationValidator;
import net.sourceforge.ondex.algorithm.relationneighbours.RelationNeighboursSearch;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.args.StringMappingPairArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

/**
 * Locates relation neighbours on all concepts of the given concept-classes
 * defined by ondex id or list of names
 * 
 * 
 * @author hindlem
 * @deprecated doesn't seem to do anything that relationneighbours doesn't do?
 */
@Status(description = "deprecated", status = StatusType.DISCONTINUED)
public class Filter extends ONDEXFilter implements ArgumentNames {

	private static Filter instance;

	/**
	 * NB: Overrides previous instance logging
	 */
	public Filter() {
		instance = this;
	}

	private Set<Integer> visibleIntConcepts;
	private Set<Integer> visibleIntRelations;

	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition[] {
				new RangeArgumentDefinition<Integer>(DEPTH_ARG, DEPTH_ARG_DESC,
						true, null, 0, Integer.MAX_VALUE, Integer.class),
				new StringArgumentDefinition(SEEDCONCEPTS_ARG,
						SEEDCONCEPTS_ARG_DESC, false, null, false),
				new StringMappingPairArgumentDefinition(
						RELATIONTYPESET_RESTRIC_ARG,
						RELATIONTYPESET_RESTRIC_ARG_DESC, false, null, true), };
	}

	public String getName() {
		return "Relation Neighbours Filter (Legacy version)";
	}

	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getId() {
		return "conceptclassneighbours";
	}

	public void start() throws InvalidPluginArgumentException {

		HashSet<ConceptClass> ccs = new HashSet<ConceptClass>();

		String[] conceptClasses = (String[]) args
				.getObjectValueArray(SEEDCONCEPTS_ARG);
		for (String ccO : conceptClasses) {
			ConceptClass conceptClass = graph.getMetaData()
					.getConceptClass(ccO);
			if (conceptClass != null) {
				ccs.add(conceptClass);
			} else {
				fireEventOccurred(new WrongParameterEvent("Concept Class "
						+ ccO + " is not a valid ConceptClass",
						getCurrentMethodName()));
			}
		}

		Integer depth = (Integer) args.getUniqueValue(DEPTH_ARG);

		System.out.println("depth " + depth);

		Map<String, List<String>> ccRestrictions = null;
		Map<String, List<String>> ccvRestrictions = null;
		Map<String, List<String>> rtRestrictions = null;
		Map<String, List<String>> rtsRestrictions = null;

		try {
			if (args.getObjectValueArray(CONCEPTCLASS_RESTRIC_ARG) != null)
				ccRestrictions = splitArgs(args
						.getObjectValueArray(CONCEPTCLASS_RESTRIC_ARG));

			if (args.getObjectValueArray(CONCEPTCV_RESTRIC_ARG) != null)
				ccvRestrictions = splitArgs(args
						.getObjectValueArray(CONCEPTCV_RESTRIC_ARG));

			if (args.getObjectValueArray(RELATIONTYPE_RESTRIC_ARG) != null)
				rtRestrictions = splitArgs(args
						.getObjectValueArray(RELATIONTYPE_RESTRIC_ARG));

			if (args.getObjectValueArray(RELATIONTYPESET_RESTRIC_ARG) != null)
				rtsRestrictions = splitArgs(args
						.getObjectValueArray(RELATIONTYPESET_RESTRIC_ARG));

		} catch (InvalidObjectException e) {
			fireEventOccurred(new WrongParameterEvent(e.getMessage(),
					getCurrentMethodName()));
			return;
		}

		LogicalRelationValidator val = null;

		RelationNeighboursSearch rn = new RelationNeighboursSearch(graph);

		if (ccRestrictions != null) {
			Iterator<String> depthsIt = ccRestrictions.keySet().iterator();
			while (depthsIt.hasNext()) {
				String depthString = depthsIt.next();
				int ccDepth = Integer.parseInt(depthString);
				Iterator<String> ccsIt = ccRestrictions.get(depthString)
						.iterator();
				while (ccsIt.hasNext()) {
					String ccId = ccsIt.next();
					ConceptClass cc = graph.getMetaData().getConceptClass(ccId);
					if (cc != null) {
						if (val == null) {
							val = new DepthSensitiveRTValidator();
						}
						((DepthSensitiveRTValidator) val)
								.addConceptClassConstraint(ccDepth, cc);
					} else {
						System.err.println(this.getName()
								+ ":Restrictions: Unknown Concept Class :"
								+ ccId);
					}
				}
			}
		}
		if (ccvRestrictions != null) {
			Iterator<String> depthsIt = ccvRestrictions.keySet().iterator();
			while (depthsIt.hasNext()) {
				String depthString = depthsIt.next();
				int ccvDepth = Integer.parseInt(depthString);
				Iterator<String> ccvsIt = ccvRestrictions.get(depthString)
						.iterator();
				while (ccvsIt.hasNext()) {
					String cvId = ccvsIt.next();
					DataSource dataSource = graph.getMetaData().getDataSource(
							cvId);
					if (dataSource != null) {
						if (val == null) {
							val = new DepthSensitiveRTValidator();
						}
						((DepthSensitiveRTValidator) val)
								.addConceptDataSourceConstraint(ccvDepth,
										dataSource);
					} else {
						System.err.println(this.getName()
								+ ":Restrictions: Unknown DataSource Class :"
								+ cvId);
					}
				}
			}
		}

		if (rtRestrictions != null) {
			Iterator<String> depthsIt = rtRestrictions.keySet().iterator();
			while (depthsIt.hasNext()) {
				String depthString = depthsIt.next();
				int rtDepth = Integer.parseInt(depthString);
				Iterator<String> rtIt = rtRestrictions.get(depthString)
						.iterator();
				while (rtIt.hasNext()) {
					String rtId = rtIt.next();
					RelationType rt = graph.getMetaData().getRelationType(rtId);
					if (rt != null) {
						if (val == null) {
							val = new DepthSensitiveRTValidator();
						}
						((DepthSensitiveRTValidator) val)
								.addRelationTypeConstraint(rtDepth, rt);
					} else {
						System.err.println(this.getName()
								+ ":Restrictions: Unknown RelationType Class :"
								+ rtId);
					}
				}
			}
		}

		if (rtsRestrictions != null) {
			Iterator<String> depthsIt = rtsRestrictions.keySet().iterator();
			while (depthsIt.hasNext()) {
				String depthString = depthsIt.next();
				int rtsDepth = Integer.parseInt(depthString);
				Iterator<String> rtIt = rtsRestrictions.get(depthString)
						.iterator();
				while (rtIt.hasNext()) {
					String rtsId = rtIt.next();
					RelationType rts = graph.getMetaData().getRelationType(
							rtsId);
					if (rts != null) {
						if (val == null) {
							val = new DepthSensitiveRTValidator();
						}
						((DepthSensitiveRTValidator) val)
								.addRelationTypeConstraint(rtsDepth, rts);
					} else {
						System.err.println(this.getName()
								+ ":Restrictions: Unknown RelationType Class :"
								+ rtsId);
					}
				}
			}
		}

		rn.setValidator(val);

		visibleIntConcepts = new HashSet<Integer>();
		visibleIntRelations = new HashSet<Integer>();

		for (ConceptClass cc : ccs) {
			for (ONDEXConcept concept : graph.getConceptsOfConceptClass(cc)) {
				rn.search(concept, depth);

				for (ONDEXConcept c : rn.getFoundConcepts())
					visibleIntConcepts.add(c.getId());
				for (ONDEXRelation r : rn.getFoundRelations())
					visibleIntRelations.add(r.getId());
			}
		}

		fireEventOccurred(new GeneralOutputEvent("Finished: Found "
				+ visibleIntConcepts.size() + " Concepts and "
				+ visibleIntRelations.size() + " Relations",
				getCurrentMethodName()));
		
		rn.shutdown();
	}

	public Set<ONDEXRelation> getVisibleRelations() {
		BitSet set = new BitSet();
		Iterator<Integer> it = visibleIntRelations.iterator();
		while (it.hasNext()) {
			int i = it.next();
			set.set(i);
		}
		assert (set.size() == visibleIntRelations.size());
		return BitSetFunctions.create(graph, ONDEXRelation.class, set);
	}

	public Set<ONDEXConcept> getVisibleConcepts() {
		BitSet set = new BitSet();
		Iterator<Integer> it = visibleIntConcepts.iterator();
		while (it.hasNext()) {
			int i = it.next();
			set.set(i);
		}
		assert (set.size() == visibleIntConcepts.size());
		return BitSetFunctions.create(graph, ONDEXConcept.class, set);
	}

	public boolean requiresIndexedGraph() {
		return true;
	}

	@Override
	public void copyResultsToNewGraph(ONDEXGraph exportGraph) {
		ONDEXGraphCloner graphCloner = new ONDEXGraphCloner(graph, exportGraph);
		Iterator<Integer> it = visibleIntConcepts.iterator();
		while (it.hasNext()) {
			graphCloner.cloneConcept(it.next());
		}
		it = visibleIntRelations.iterator();
		while (it.hasNext()) {
			graphCloner.cloneRelation(it.next());
		}
	}

	/**
	 * Returns a slightly more useful array structure from pair values
	 * 
	 * @param objs
	 *            array of pairs
	 * @return names to values hashmap
	 * @throws InvalidObjectException
	 *             if the pairs are not in the expected form
	 */
	public static Map<String, List<String>> splitArgs(Object[] objs)
			throws InvalidObjectException {
		Map<String, List<String>> args = new HashMap<String, List<String>>();
		for (Object obj : objs) {
			if (obj instanceof String) {
				String[] pair = ((String) obj).split(",");
				if (pair.length == 2) {
					List<String> existing = args.get(pair[0]);
					if (existing == null) {
						existing = new ArrayList<String>(1);
					}
					existing.add(pair[1]);
				} else {
					throw new InvalidObjectException(
							"name,value pair invalid: " + obj);
				}
			} else {
				throw new InvalidObjectException("Object is not a String: "
						+ obj);
			}
		}
		return args;
	}

	/**
	 * Convenience method for outputing the current method name in a dynamic way
	 * 
	 * @return the calling method name
	 */
	public static String getCurrentMethodName() {
		Exception e = new Exception();
		StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
		String name = trace.getMethodName();
		String className = trace.getClassName();
		int line = trace.getLineNumber();
		return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line
				+ "]";
	}

	/**
	 * Propagates an event to the last new instance of this Filter class
	 * 
	 * @param et
	 *            event to propagate
	 */
	public static void propagateEventOccurred(EventType et) {
		if (instance != null)
			instance.fireEventOccurred(et);
	}

	public String[] requiresValidators() {
		return new String[0];
	}
}
