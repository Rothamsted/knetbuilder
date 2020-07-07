package net.sourceforge.ondex.filter.subgraph;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Filter extends ONDEXFilter implements ArgumentNames {

    // contains list of visible concepts
    private Set<ONDEXConcept> concepts = null;

    // contains list of visible relations
    private Set<ONDEXRelation> relations = null;

    private Set<ONDEXConcept> inverseConcepts = null;

    private Set<ONDEXRelation> inverseRelations = null;

    private Set<Integer> visibleC = null;

    private Set<Integer> visibleR = null;

    private Map<Integer, String> rdForLevel = null;

    private Map<Integer, Set<ConceptClass>> ccForLevel = null;

    private Map<Integer, Set<RelationType>> rtForLevel = null;

    public static String[] levelsRT = {FIRST_RT_ARG, SECOND_RT_ARG,
            THIRD_RT_ARG, FORTH_RT_ARG, FIFTH_RT_ARG};

    public static String[] levelsCC = {FIRST_CC_ARG, SECOND_CC_ARG,
            THIRD_CC_ARG, FORTH_CC_ARG, FIFTH_CC_ARG};

    public static String[] levelsRD = {FIRST_RD_ARG, SECOND_RD_ARG,
            THIRD_RD_ARG, FORTH_RD_ARG, FIFTH_RD_ARG};

    @Override
    public void copyResultsToNewGraph(ONDEXGraph exportGraph) {
        ONDEXGraphCloner graphCloner = new ONDEXGraphCloner(graph, exportGraph);
        for (ONDEXConcept c : concepts) {
            graphCloner.cloneConcept(c);
        }
        for (ONDEXRelation r : relations) {
            graphCloner.cloneRelation(r);
        }
    }

    @Override
    public Set<ONDEXConcept> getVisibleConcepts() {
        return concepts;
    }

    public Set<ONDEXConcept> getInVisibleConcepts() {
        return inverseConcepts;
    }

    @Override
    public Set<ONDEXRelation> getVisibleRelations() {
        return relations;
    }

    public Set<ONDEXRelation> getInVisibleRelations() {
        return inverseRelations;
    }

    @Override
    public String getName() {
        return "SubGraph Filter";
    }

    @Override
    public String getVersion() {
        return "28.01.2009";
    }

    @Override
    public String getId() {
        return "subgraph";
    }


    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {

        // get root id and concept
        Integer rootID = (Integer) args.getUniqueValue(ROOT_ARG);
        ONDEXConcept concept = graph.getConcept(rootID);
        if (concept == null) {
            fireEventOccurred(new WrongParameterEvent(
                    "There was no valid root concept specified: Aborting",
                    getCurrentMethodName()));
            return;
        }

        ONDEXGraphMetaData meta = graph.getMetaData();

        // all present levels for relation types
        rtForLevel = new Hashtable<Integer, Set<RelationType>>();

        // construct lookup table for relation types
        int i = 0;
        for (String arg : levelsRT) {
            Integer level = i;
            rtForLevel.put(level, new HashSet<RelationType>());
            for (Object o : args.getObjectValueArray(arg)) {
                RelationType rt = meta.getRelationType(o.toString());
                if (rt == null) {
                    fireEventOccurred(new WrongParameterEvent(
                            "Wrong relation type '" + o
                                    + "' specified at level '" + arg
                                    + "': Aborting", getCurrentMethodName()));
                    return;
                } else {
                    rtForLevel.get(level).add(rt);
                }
            }
            i++;
        }
        System.out.println("RelationTypes:" + rtForLevel);

        // all present levels for concept classes
        ccForLevel = new Hashtable<Integer, Set<ConceptClass>>();

        // construct lookup table for concept classes
        i = 0;
        for (String arg : levelsCC) {
            Integer level = i;
            ccForLevel.put(level, new HashSet<ConceptClass>());
            for (Object o : args.getObjectValueArray(arg)) {
                ConceptClass cc = meta.getConceptClass(o.toString());
                if (cc == null) {
                    fireEventOccurred(new WrongParameterEvent(
                            "Wrong concept class '" + o
                                    + "' specified at level '" + arg
                                    + "': Aborting", getCurrentMethodName()));
                    return;
                } else {
                    ccForLevel.get(level).add(cc);
                }
            }
            i++;
        }
        System.out.println("ConceptClasses:" + ccForLevel);

        // all present levels for relation direction
        rdForLevel = new Hashtable<Integer, String>();
        i = 0;
        for (String arg : levelsRD) {
            Integer level = i;
            if (args.getUniqueValue(arg) != null)
                rdForLevel.put(level, args.getUniqueValue(arg).toString());
            else
                rdForLevel.put(level, RelationDirectionDefinition.BOTH);
            i++;
        }
        System.out.println("RelationDirection:" + rdForLevel);

        visibleC = new HashSet<Integer>();
        visibleR = new HashSet<Integer>();

        // start graph traversal
        recursion(concept, 0, 4);

        // construct concept view
        BitSet set = new BitSet();
        Iterator<Integer> it = visibleC.iterator();
        while (it.hasNext()) {
            set.set(it.next());
        }
        concepts = BitSetFunctions.create(graph, ONDEXConcept.class,
                set);
        inverseConcepts = BitSetFunctions.copy(graph.getConcepts());
        inverseConcepts.removeAll(concepts);

        // construct relation view
        set = new BitSet();
        it = visibleR.iterator();
        while (it.hasNext()) {
            set.set(it.next());
        }
        relations = BitSetFunctions.create(graph,
                ONDEXRelation.class, set);
        inverseRelations = BitSetFunctions.copy(graph.getRelations());
        inverseRelations.removeAll(relations);
    }

    private void recursion(ONDEXConcept root, int level, int cutoff) {
        // add root concept as visible per default
        visibleC.add(root.getId());
        if (level > cutoff)
            return;

        Integer key = level;
        // increase level
        level++;
        for (ONDEXRelation r : graph.getRelationsOfConcept(root)) {
            ONDEXConcept from = r.getFromConcept();
            ONDEXConcept to = r.getToConcept();
            RelationType rt = r.getOfType();

            // get allowed directions
            String dir = rdForLevel.get(key);
            boolean incoming = dir.equals(RelationDirectionDefinition.INCOMING)
                    || dir.equals(RelationDirectionDefinition.BOTH);
            boolean outgoing = dir.equals(RelationDirectionDefinition.OUTGOING)
                    || dir.equals(RelationDirectionDefinition.BOTH);

            // check for optional relation type
            if (rtForLevel.get(key).size() == 0
                    || rtForLevel.get(key).contains(rt)) {

                if (outgoing && from.equals(root)) {
                    // proceed to concept
                    ConceptClass toCC = to.getOfType();
                    if (ccForLevel.get(key).contains(toCC)) {
                        visibleR.add(r.getId());
                        recursion(to, level, cutoff);
                    }
                } else if (incoming && to.equals(root)) {
                    // proceed from concept
                    ConceptClass fromCC = from.getOfType();
                    if (ccForLevel.get(key).contains(fromCC)) {
                        visibleR.add(r.getId());
                        recursion(from, level, cutoff);
                    }
                }
            }
        }
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        RangeArgumentDefinition<Integer> root = new RangeArgumentDefinition<Integer>(
                ROOT_ARG, ROOT_ARG_DESC, true, null, 1, Integer.MAX_VALUE, Integer.class);

        StringArgumentDefinition firstRT = new StringArgumentDefinition(
                FIRST_RT_ARG, FIRST_RT_ARG_DESC, false, null, true);
        StringArgumentDefinition firstCC = new StringArgumentDefinition(
                FIRST_CC_ARG, FIRST_CC_ARG_DESC, false, null, true);
        RelationDirectionDefinition firstRD = new RelationDirectionDefinition(
                FIRST_RD_ARG, FIRST_RD_ARG_DESC, false,
                RelationDirectionDefinition.BOTH, false);

        StringArgumentDefinition secondRT = new StringArgumentDefinition(
                SECOND_RT_ARG, SECOND_RT_ARG_DESC, false, null, true);
        StringArgumentDefinition secondCC = new StringArgumentDefinition(
                SECOND_CC_ARG, SECOND_CC_ARG_DESC, false, null, true);
        RelationDirectionDefinition secondRD = new RelationDirectionDefinition(
                SECOND_RD_ARG, SECOND_RD_ARG_DESC, false,
                RelationDirectionDefinition.BOTH, false);

        StringArgumentDefinition thirdRT = new StringArgumentDefinition(
                THIRD_RT_ARG, THIRD_RT_ARG_DESC, false, null, true);
        StringArgumentDefinition thirdCC = new StringArgumentDefinition(
                THIRD_CC_ARG, THIRD_CC_ARG_DESC, false, null, true);
        RelationDirectionDefinition thirdRD = new RelationDirectionDefinition(
                THIRD_RD_ARG, THIRD_RD_ARG_DESC, false,
                RelationDirectionDefinition.BOTH, false);

        StringArgumentDefinition forthRT = new StringArgumentDefinition(
                FORTH_RT_ARG, FORTH_RT_ARG_DESC, false, null, true);
        StringArgumentDefinition forthCC = new StringArgumentDefinition(
                FORTH_CC_ARG, FORTH_CC_ARG_DESC, false, null, true);
        RelationDirectionDefinition forthRD = new RelationDirectionDefinition(
                FORTH_RD_ARG, FORTH_RD_ARG_DESC, false,
                RelationDirectionDefinition.BOTH, false);

        StringArgumentDefinition fifthRT = new StringArgumentDefinition(
                FIFTH_RT_ARG, FIFTH_RT_ARG_DESC, false, null, true);
        StringArgumentDefinition fifthCC = new StringArgumentDefinition(
                FIFTH_CC_ARG, FIFTH_CC_ARG_DESC, false, null, true);
        RelationDirectionDefinition fifthRD = new RelationDirectionDefinition(
                FIFTH_RD_ARG, FIFTH_RD_ARG_DESC, false,
                RelationDirectionDefinition.BOTH, false);

        return new ArgumentDefinition<?>[]{root, firstRT, firstCC, firstRD,
                secondRT, secondCC, secondRD, thirdRT, thirdCC, thirdRD,
                forthRT, forthCC, forthRD, fifthRT, fifthCC, fifthRD};
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

}
