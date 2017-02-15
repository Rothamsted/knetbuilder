package net.sourceforge.ondex.transformer.conceptcaster;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.tools.functions.GraphElementManipulation;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import java.util.ArrayList;
import java.util.List;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createCC;

/**
 * This Transformer replaces concepts of a specified concept class with equivalent ones,
 * but with a new concept class.
 *
 * @author lysenkoa
 * @version 25.05.2008
 */
@Authors(authors = {"Artem Lysenko"}, emails = {"lysenkoa at users.sourceforge.net"})
@Status(description = "Tested December 2013 (Artem Lysenko)", status = StatusType.STABLE)
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Transformer extends ONDEXTransformer implements ArgumentNames {

    /**
     * Starts processing of data.
     */
    public void start() throws InvalidPluginArgumentException {
        List<String> objs = (List<String>) args.getObjectValueList(OLDCLASS_TO_NEWCLASS_ARG);
        List<ConceptClass[]> ccs = new ArrayList<ConceptClass[]>();
        for (String o : objs) {
            String[] split = o.split(",");
            if (split.length != 2)
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new WrongParameterEvent("Invalid input for class types - should be two, comma delimited.", "[Transformer - setONDEXGraph]"));
            ConceptClass from = graph.getMetaData().getConceptClass(split[0]);
            if (from == null) {
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new WrongParameterEvent("Concept class " + split[0] + " is undefined", "[Transformer - setONDEXGraph]"));
                from = createCC(graph, split[0]);
            }

            ConceptClass to = graph.getMetaData().getConceptClass(split[1]);
            if (to == null) {
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new WrongParameterEvent("Concept class " + split[1] + " is undefined", "[Transformer - setONDEXGraph]"));
                to = createCC(graph, split[1]);
            }

            if (to != null && from != null) {
                //System.err.println("From:"+from+" To:"+to);
                ccs.add(new ConceptClass[]{from, to});
            }
        }
        int cCount = graph.getConcepts().size();
        int rCount = graph.getRelations().size();
        for (ConceptClass[] ccToChange : ccs) {
            //System.err.println(ccToChange[0]+":"+view.size());
            for (ONDEXConcept c : graph.getConceptsOfConceptClass(ccToChange[0]).toArray(new ONDEXConcept[0]))
                GraphElementManipulation.castConcept(c, ccToChange[1], graph);
        }
        if (rCount != graph.getRelations().size())
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new GeneralOutputEvent("Transformer error - graph inconsistency detected(Relatoin number)! Before: " + rCount + " after: " + graph.getRelations().size(), "[Transformer - setONDEXGraph]"));
        if (cCount != graph.getConcepts().size())
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new GeneralOutputEvent("Transformer error - graph inconsistency detected(Concepts number)! Before: " + cCount + " after: " + graph.getConcepts().size(), "[Transformer - setONDEXGraph]"));
    }

    /**
     * Returns name of this transformer.
     *
     * @return String
     */
    public String getName() {
        return "Concept caster";
    }

    /**
     * Returns version of this transformer.
     *
     * @return String
     */
    public String getVersion() {
        return "25.05.08";
    }

    @Override
    public String getId() {
        return "conceptcaster";
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
