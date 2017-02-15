package net.sourceforge.ondex.transformer.relationcaster;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.tools.functions.GraphElementManipulation;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author lysenkoa
 *         <p/>
 *         Transformer to change the type sets of relations.
 */
@Authors(authors = {"Artem Lysenko"}, emails = {"lysenkoa at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Transformer extends ONDEXTransformer implements ArgumentNames {

    /**
     * Starts processing of data.
     */
    public void start() throws InvalidPluginArgumentException {
        List<String> objs = (List<String>) args.getObjectValueList(OLDCLASS_TO_NEWCLASS_ARG);
        List<RelationType[]> rtss = new ArrayList<RelationType[]>();
        for (Object o : objs) {
            String[] split = o.toString().split(",");
            if (split.length != 2)
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new WrongParameterEvent("Invalid input for relation typeset(s) - should be two, comma delimited.", "[Transformer - setONDEXGraph]"));
            RelationType from = graph.getMetaData().getRelationType(split[0]);
            if (from == null)
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new WrongParameterEvent("Relation type set " + split[0] + " is undefined", "[Transformer - setONDEXGraph]"));
            RelationType to = graph.getMetaData().getRelationType(split[1]);
            if (to == null)
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new WrongParameterEvent("Relation type set " + split[1] + " is undefined", "[Transformer - setONDEXGraph]"));
            if (to != null && from != null) {
                //System.err.println("From:"+from+" To:"+to);
                rtss.add(new RelationType[]{from, to});
            }
        }
        int cCount = graph.getConcepts().size();
        int rCount = graph.getRelations().size();
        int dupes = 0;
        for (RelationType[] rtsToChange : rtss) {
        	Set<ONDEXRelation> rels = BitSetFunctions.copy(graph.getRelationsOfRelationType(rtsToChange[0]));        	
            for (ONDEXRelation r : rels) {
                if (!GraphElementManipulation.castRelation(graph, r, rtsToChange[1], false)) {
                    rCount--;
                    dupes++;
                }

            }

        }
        if (rCount != graph.getRelations().size())
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new GeneralOutputEvent("Transformer error - graph inconsistency detected(Relatoin number)! Before: " + rCount + " after: " + graph.getRelations().size(), "[Transformer - setONDEXGraph]"));
        if (cCount != graph.getConcepts().size())
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new GeneralOutputEvent("Transformer error - graph inconsistency detected(Concepts number)! Before: " + cCount + " after: " + graph.getConcepts().size(), "[Transformer - setONDEXGraph]"));
        System.err.println("Skipped duplicates: " + dupes);
    }

    /**
     * Returns name of this transformer.
     *
     * @return String
     */
    public String getName() {
        return "Relation caster";
    }

    /**
     * Returns version of this transformer.
     *
     * @return String
     */
    public String getVersion() {
        return "29.09.08";
    }

    @Override
    public String getId() {
        return "relationcaster";
    }

    /**
     * Returns arguments required by this transformer.
     *
     * @return ArgumentDefinition<?>[]
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition[]{
                new StringArgumentDefinition(
                        OLDCLASS_TO_NEWCLASS_ARG,
                        OLDCLASS_TO_NEWCLASS_ARG_DESC, true, null,
                        true),
        };
    }

    /**
     * Does not require index ondex graph.
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
