package net.sourceforge.ondex.filter.tag;

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
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filter on context
 *
 * @author keywan
 */
@Authors(authors = {"Keywan Hassani-pak"}, emails = {"keywan at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Filter extends ONDEXFilter implements ArgumentNames {

    private Set<ONDEXConcept> concepts;

    private Set<ONDEXRelation> relations;

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
        return BitSetFunctions.unmodifiableSet(concepts);
    }

    @Override
    public Set<ONDEXRelation> getVisibleRelations() {
        return BitSetFunctions.unmodifiableSet(relations);
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        RangeArgumentDefinition<Integer> context_arg = new RangeArgumentDefinition<Integer>(
                ArgumentNames.TAG_ARG,
                ArgumentNames.TAG_ARG_DESC,
                true, null, 1, Integer.MAX_VALUE, Integer.class);
        StringArgumentDefinition boolean_arg = new StringArgumentDefinition(
                ArgumentNames.TAG_BOOLEAN_ARG, ArgumentNames.TAG_BOOLEAN_ARG_DESC,
                false, null, false);

        return new ArgumentDefinition<?>[]{context_arg, boolean_arg};
    }

    @Override
    public String getName() {
        return "Context Filter";
    }

    @Override
    public String getVersion() {
        return "04/12/2008";
    }

    @Override
    public String getId() {
        return "tag";
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
    public void start() throws InvalidPluginArgumentException {

        Integer cId = (Integer) args.getUniqueValue(ArgumentNames.TAG_ARG);
        if (cId != null) {
            ONDEXConcept context = graph.getConcept(cId);

            Set<ONDEXConcept> newConcepts = graph.getConceptsOfTag(context);
            Set<ONDEXRelation> newRelations = graph.getRelationsOfTag(context);

            concepts = newConcepts;
            relations = newRelations;
        }

        String contextBoolean = (String) args
                .getUniqueValue(ArgumentNames.TAG_BOOLEAN_ARG);

        if (contextBoolean != null) {
            Pattern p = Pattern.compile("(\\d+) (\\w+) (\\d+)");
            Matcher m = p.matcher(contextBoolean);
            if (m.find()) {
                Integer cId1 = Integer.valueOf(m.group(1));
                String operation = m.group(2);
                Integer cId2 = Integer.valueOf(m.group(3));

                ONDEXConcept context1 = graph.getConcept(cId1);
                ONDEXConcept context2 = graph.getConcept(cId2);

                Set<ONDEXConcept> viewCon1 = graph.getConceptsOfTag(context1);
                Set<ONDEXConcept> viewCon2 = graph.getConceptsOfTag(context2);
                Set<ONDEXRelation> viewRel1 = graph.getRelationsOfTag(context1);
                Set<ONDEXRelation> viewRel2 = graph.getRelationsOfTag(context2);

                if (operation.equals("AND")) {
                    concepts = viewCon1;
                    concepts.retainAll(viewCon2);
                    relations = viewRel1;
                    relations.retainAll(viewRel2);
                } else if (operation.equals("OR")) {
                    concepts = viewCon1;
                    concepts.addAll(viewCon2);
                    relations = viewRel1;
                    relations.addAll(viewRel2);
                } else if (operation.equals("NOT")) {
                    concepts = viewCon1;
                    concepts.removeAll(viewCon2);
                    relations = viewRel1;
                    relations.removeAll(viewRel2);
                } else {
                    throw new RuntimeException("Unhandled operator :" + operation);
                }
            }
        }

    }

}
