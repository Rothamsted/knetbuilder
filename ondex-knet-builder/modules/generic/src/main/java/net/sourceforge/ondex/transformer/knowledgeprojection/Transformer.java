package net.sourceforge.ondex.transformer.knowledgeprojection;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createAttName;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.tools.functions.GraphElementManipulation;
import net.sourceforge.ondex.tools.functions.StandardFunctions;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

/**
 * Copies an attribute from one entity to another, when they are joined
 * by a particular relation. Source concept may then be removed.
 *
 * @author lysenkoa
 */
@Authors(authors = {"Artem Lysenko"}, emails = {"lysenkoa at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Transformer extends ONDEXTransformer implements AttributeNames {

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(TRANSFERRING_RELATION_ARG, TRANSFERRING_RELATION_ARG_DESC, true, null, false),
                new StringArgumentDefinition(GDS_NAME_ARG, GDS_NAME_ARG_DESC, true, null, false),
                new StringArgumentDefinition(GDS_VALUE_ARG, GDS_VALUE_ARG_DESC, true, null, false),
                new BooleanArgumentDefinition(REMOVE_ARG, REMOVE_ARG_DESC, true, true)
        };

    }

    @Override
    public String getName() {
        return "Knowledge plane transitive projection transformer";
    }

    @Override
    public String getVersion() {
        return "v1.0";
    }

    @Override
    public String getId() {
        return "knowledgeprofection";
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
        String relType = null;
        String att = null;
        String attValue = null;
        boolean removeProcessed = true;
        ONDEXEventHandler oeh = ONDEXEventHandler.getEventHandlerForSID(graph.getSID());
        try {
            removeProcessed = Boolean.valueOf(args.getUniqueValue(REMOVE_ARG).toString());
            relType = args.getUniqueValue(TRANSFERRING_RELATION_ARG).toString();
            att = args.getUniqueValue(GDS_NAME_ARG).toString();
            attValue = args.getUniqueValue(GDS_VALUE_ARG).toString();
        }
        catch (NullPointerException e) {
            throw new Exception("Required arguments are missing");
        }
        ONDEXGraphMetaData meta = graph.getMetaData();
        RelationType rt = meta.getRelationType(relType);
        AttributeName an = meta.getAttributeName(att);
        AttributeName occurrences = createAttName(graph, "Occurrences", Integer.class);
        if (an == null) {
            oeh.fireEventOccurred(new GeneralOutputEvent("The attribute " + att + " is not present in this graph, so there is nothing to do.", "[Transformer - Knowledge plane transendence transformer]"));
            return;
        }
        Object planeId = null;
        if (att.getClass().equals(String.class)) {
            planeId = attValue;
        }
        try {
            Method m = att.getClass().getMethod("valueOf", new Class<?>[]{String.class});
            planeId = m.invoke(null, attValue);
        }
        catch (NoSuchMethodException e) {
            oeh.fireEventOccurred(new GeneralOutputEvent("The attribute " + att + " is not of parseable type - must be string or number.", "[Transformer - Knowledge plane transendence transformer]"));
            return;
        }
        if (planeId == null) {
            oeh.fireEventOccurred(new GeneralOutputEvent("The attribute " + att + " is not of parseable type - must be string or number.", "[Transformer - Knowledge plane transendence transformer]"));
            return;
        }

        BitSet targetPlain = new BitSet();
        Set<ONDEXConcept> all = graph.getConcepts();
        List<Integer> toRemove = new ArrayList<Integer>();
        Map<Integer, List<Integer>> otherPlaneToTargetPlain = new HashMap<Integer, List<Integer>>();
        for (ONDEXConcept c : all) {
            Attribute attribute = c.getAttribute(an);
            if (attribute == null) {
                continue;
            }

            if (attribute.getValue().equals(planeId)) {
                targetPlain.set(c.getId());
            } else {
                boolean remove = true;
                for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
                    if (r.getOfType().equals(rt)) {
                        remove = false;
                        ONDEXConcept possibleTarget = StandardFunctions.getOtherNode(c, r);
                        if (isInTargetPlain(possibleTarget, an, planeId)) {

                            List<Integer> idsInTargetPlane = otherPlaneToTargetPlain.get(c.getId());
                            if (idsInTargetPlane == null) {
                                idsInTargetPlane = new ArrayList<Integer>();
                                otherPlaneToTargetPlain.put(c.getId(), idsInTargetPlane);
                            }
                            idsInTargetPlane.add(possibleTarget.getId());
                        }
                    }
                }
                if (remove) {
                    toRemove.add(c.getId());
                }
            }
        }
        if (removeProcessed) {
            for (ONDEXRelation r : graph.getRelationsOfRelationType(rt)) {
                graph.deleteRelation(r.getId());
            }
            for (Integer rId : toRemove) {
                graph.deleteConcept(rId);
            }
        }


        toRemove = new ArrayList<Integer>(all.size() - targetPlain.cardinality());
        for (ONDEXConcept c: graph.getConcepts()) {
            if (targetPlain.get(c.getId())) {
                continue;
            }
            for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
                List<Integer> targetFrom = otherPlaneToTargetPlain.get(r.getFromConcept().getId());
                List<Integer> targetTo = otherPlaneToTargetPlain.get(r.getToConcept().getId());
                if (targetFrom == null || targetTo == null) {
                    if (removeProcessed) {
                        throw new Exception("Parser consistensy checks failed - unable to follow a match to its target.");
                    } else {
                        continue;
                    }
                }
                for (Integer aTargetFrom : targetFrom) {
                    ONDEXConcept from = graph.getConcept(aTargetFrom);
                    for (Integer aTargetTo : targetTo) {
                        ONDEXConcept to = graph.getConcept(aTargetTo);
                        ONDEXRelation targetRelation = graph.getRelation(from, to, r.getOfType());
                        if (targetRelation != null) {
                            GraphElementManipulation.changeAttributeValue(targetRelation, occurrences, (((Integer) targetRelation.getAttribute(occurrences).getValue()) + 1));
                        }
                        else {
                            GraphElementManipulation.copyRelation(graph, from, to, r);
                            r.createAttribute(occurrences, 1, false);
                        }
                    }
                }
            }
            otherPlaneToTargetPlain.remove(c.getId());
        }
        if (removeProcessed) {
            for (Integer rId : toRemove) {
                graph.deleteConcept(rId);
            }
        }
    }

    private final boolean isInTargetPlain(ONDEXConcept possibleTarget, AttributeName an, Object id) {
        Attribute attribute = possibleTarget.getAttribute(an);
        if (attribute == null)
            return false;
        if (!attribute.getValue().equals(id))
            return false;
        return true;
    }
}
