package net.sourceforge.ondex.filter.transitive;

import java.util.BitSet;
import java.util.Set;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

/**
 * Depth filter. A set of concepts identified either by a particular DataSource or Attribute Attribute name and
 * are used as seed concepts. All concepts that are not connected to the seed ones or are removed
 * from them further than a specified depth level are filtered out from the graph.
 *
 * @author lysenkoa
 * @version 09.05.2008
 */
@Authors(authors = {"Artem Lysenko"}, emails = {"lysenkoa at users.sourceforge.net"})

@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Filter extends ONDEXFilter implements ArgumentNames {
    int depth = Integer.MAX_VALUE;
    // contains list of visible concepts
    private Set<ONDEXConcept> concepts = null;

    // contains list of visible relations
    private Set<ONDEXRelation> relations = null;

    /**
     * Constructor
     */
    public Filter() {
    }

    @Override
    public void copyResultsToNewGraph(ONDEXGraph exportGraph) {
        ONDEXGraphCloner graphCloner = new ONDEXGraphCloner(graph,
                exportGraph);
        for (ONDEXConcept c : concepts) {
            graphCloner.cloneConcept(c);
        }
        for (ONDEXRelation r : relations) {
            graphCloner.cloneRelation(r);
        }
    }

    @Override
    public Set<ONDEXConcept> getVisibleConcepts() {
        return BitSetFunctions.unmodifiableSet(concepts);
    }

    @Override
    public Set<ONDEXRelation> getVisibleRelations() {
        return BitSetFunctions.unmodifiableSet(relations);
    }

    @Override
    public String getName() {
        return "Transitive Filter";
    }

    @Override
    public String getVersion() {
        return "09.05.2008";
    }


    @Override
    public String getId() {
        return "transitive";
    }

    /**
     *
     *
     *
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        StringArgumentDefinition cv_arg = new StringArgumentDefinition(CV_ARG, CV_ARG_DESC, false, null, false);
        StringArgumentDefinition att_arg = new StringArgumentDefinition(ATT_ARG, ATT_ARG_DESC, false, null, false);
        StringArgumentDefinition lv_arg = new StringArgumentDefinition(LEVEL_ARG, LEVEL_ARG_DESC, false, null, false);
        return new ArgumentDefinition<?>[]{cv_arg, att_arg, lv_arg};
    }

    /**
     * Filters the graph and constructs the lists for visible concepts and
     * relations.
     *
     * @throws Exception
     */
    public void start() throws Exception {
        Object ocv, oat;
        DataSource dataSource = null;
        AttributeName at = null;

        try {
            if ((ocv = args.getUniqueValue(CV_ARG)) != null) {
                dataSource = graph.getMetaData().getDataSource(ocv.toString());
            } else if ((oat = args.getUniqueValue(ATT_ARG)) != null) {
                at = graph.getMetaData().getAttributeName(oat.toString());
            } else {
                return;
            }
        } catch (EmptyStringException e) {
            throw new Exception("The DataSource supplied must not be empty");
        }


        Object lv = args.getUniqueValue(LEVEL_ARG);
        if (lv != null) {
            depth = Integer.parseInt(lv.toString());
        }
        BitSet toKeep = new BitSet();

        for (ONDEXConcept c : graph.getConcepts()) {
            if (dataSource != null) {
                for (ConceptAccession ca : c.getConceptAccessions()) {
                    if (ca.getElementOf().equals(dataSource)) {
                        propagate(toKeep, c, 0);
                        break;
                    }
                }
            } else if (at != null) {
                for (Attribute attribute : c.getAttributes()) {
                    if (attribute.getOfType().equals(at)) {
                        propagate(toKeep, c, 0);
                        break;
                    }
                }
            }

        }

        concepts = BitSetFunctions.create(graph, ONDEXConcept.class, toKeep);
        relations = graph.getRelations();
    }

    private void propagate(BitSet toKeep, ONDEXConcept c, int l) throws NullValueException, AccessDeniedException {
        if (l > depth) return;
        if (toKeep.get(c.getId())) return;
        toKeep.set(c.getId());
        for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
            if (!r.getToConcept().equals(c)) {
                propagate(toKeep, r.getToConcept(), l + 1);
            } else {
                propagate(toKeep, r.getFromConcept(), l + 1);
            }
        }
    }

    /**
     * An indexed graph is not required.
     *
     * @return false
     */
    public boolean requiresIndexedGraph() {
        return false;
    }

    public String[] requiresValidators() {
        return new String[0];
    }
}
