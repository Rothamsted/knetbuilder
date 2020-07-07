package net.sourceforge.ondex.filter.tagconsensus;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.exception.type.WrongArgumentException;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

/**
 * @author jweile
 */
@Authors(authors = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Filter extends ONDEXFilter
{

    //####CONSTANTS####

    public static final String TAG_LIST_ARG = "TagList";
    public static final String TAG_LIST_ARG_DESC = "Comma separated List of tag ids " +
            "that serve as tags, defining the set to work on. Leave blank for all.";
    public static final String THRESHOLD_ARG = "Threshold";
    public static final String THRESHOLD_ARG_DESC = "Double value representing the share of " +
            "tags that qualify a graph element for being returned. Example: 0.5";

    //#####FIELDS#####

    private BitSet visC, visR;
    private HashSet<ONDEXConcept> contexts, allContexts;
    private double threshold;

    //#####CONSTRUCTOR#####

    public Filter() {
        visC = new BitSet();
        visR = new BitSet();
        allContexts = new HashSet<ONDEXConcept>();
        contexts = new HashSet<ONDEXConcept>();
    }

    //#####METHODS#####

    /**
     */
    @Override
    public void copyResultsToNewGraph(ONDEXGraph exportGraph) {
        ONDEXGraphCloner graphCloner = new ONDEXGraphCloner(graph, exportGraph);
        for (ONDEXConcept c : getVisibleConcepts()) {
            graphCloner.cloneConcept(c);
        }
        for (ONDEXRelation r : getVisibleRelations()) {
            graphCloner.cloneRelation(r);
        }
    }

    /**
     * @see net.sourceforge.ondex.filter.ONDEXFilter#getVisibleConcepts()
     */
    @Override
    public Set<ONDEXConcept> getVisibleConcepts() {
        return BitSetFunctions.create(graph, ONDEXConcept.class, visC);
    }

    /**
     * @see net.sourceforge.ondex.filter.ONDEXFilter#getVisibleRelations()
     */
    @Override
    public Set<ONDEXRelation> getVisibleRelations() {
        return BitSetFunctions.create(graph, ONDEXRelation.class, visR);
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getArgumentDefinitions()
     */
    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new RangeArgumentDefinition<Float>(THRESHOLD_ARG, THRESHOLD_ARG_DESC,
                        false, 0.5f, 0.0f, 1.0f, Float.class),
                new StringArgumentDefinition(TAG_LIST_ARG, TAG_LIST_ARG_DESC,
                        false, null, false)
        };
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getName()
     */
    @Override
    public String getName() {
        return "Tag consensus filter";
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getVersion()
     */
    @Override
    public String getVersion() {
        return "03.02.2009";
    }

    @Override
    public String getId() {
        return "tagconsensus";
    }


    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#requiresIndexedGraph()
     */
    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#requiresValidators()
     */
    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#start()
     */
    @Override
    public void start() throws Exception {
        // fixme: nmrp3 I have no idea what this was meant to be doing - I've rewritten it but may well have broken it
        fetchContexts();
        fetchArguments();

        double count, share, max = contexts.size();

        for (ONDEXConcept c: graph.getConcepts()) {
            count = 0.0;
            for (ONDEXConcept ct : c.getTags()) {
                if (contexts.contains(ct)) {
                    count++;
                }
            }
            share = count / max;
            if (share >= threshold) {
                visC.set(c.getId());
            }
        }

        int fromID, toID;

        for (ONDEXRelation r : graph.getRelations()) {
            count = 0.0;
            for (ONDEXConcept ct : r.getTags()) {
                if (contexts.contains(ct)) {
                    count++;
                }
            }
            share = count / max;
            if (share >= threshold) {
                fromID = r.getFromConcept().getId();
                toID = r.getToConcept().getId();
                if (visC.get(fromID) && visC.get(toID)) {
                    visR.set(r.getId());
                }
            }
        }
    }

    private void fetchContexts() {
        for (ONDEXConcept c : graph.getAllTags()) {
            allContexts.add(c);
        }
    }

    private void fetchArguments() throws WrongArgumentException, InvalidPluginArgumentException {
        String str = (String) args.getUniqueValue(TAG_LIST_ARG);
        if (str != null) {
            if (str.length() != 0) {
                int id;
                boolean error = false;
                for (String idStr : str.split(",")) {
                    try {
                        id = Integer.parseInt(idStr.trim());
                        ONDEXConcept c = graph.getConcept(id);
                        if (c == null || !allContexts.contains(c)) {
                            error = true;
                        } else {
                            contexts.add(c);
                        }
                    } catch (NumberFormatException nfe) {
                        error = true;
                    }
                    if (error)
                        throw new WrongArgumentException(idStr + " is no valid context integer id");
                }
            }
        }
        if (contexts.size() == 0) {
            contexts.addAll(allContexts);
        }

        Float thresh = (Float) args.getUniqueValue(THRESHOLD_ARG);
        if (thresh != null) {
            threshold = thresh;
        } else {
            threshold = 0.5;
        }
    }

}
