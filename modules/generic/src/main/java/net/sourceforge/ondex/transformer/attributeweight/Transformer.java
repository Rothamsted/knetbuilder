package net.sourceforge.ondex.transformer.attributeweight;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.exception.type.WrongParameterException;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import java.util.Set;

/**
 * This transformer calculates a weighted average over the specified Attribute after
 * first normalising all Attribute values on their own.
 *
 * @author taubertj
 * @version 21.10.2008
 */
@Authors(authors = {"Jan Taubert"}, emails = {"jantaubert at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Transformer extends ONDEXTransformer implements
        ArgumentNames {

    /**
     * Starts processing of data.
     */
    public void start() throws InvalidPluginArgumentException {

        // all valid relations
        Set<ONDEXRelation> relations = null;

        // which relation type are we working with
        RelationType rt = graph.getMetaData().getRelationType(
                (String) args.getUniqueValue(RELATION_TYPE_ARG));
        if (rt == null) {
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new WrongParameterEvent(args
                    .getUniqueValue(RELATION_TYPE_ARG)
                    + " is not a valid RelationType.",
                    "[Transformer - setONDEXGraph]"));
            throw new WrongParameterException(args
                    .getUniqueValue(RELATION_TYPE_ARG)
                    + " is not a valid RelationType.");
        }

        // parse GDSs
        String g = (String) args.getUniqueValue(ATTRIBUTE_ARG);
        String[] gs = g.split(",");
        AttributeName[] ans = new AttributeName[gs.length];
        int i = 0;
        for (String s : gs) {
            AttributeName an = graph.getMetaData().getAttributeName(s);
            if (an == null) {
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new WrongParameterEvent(s
                        + " is not a valid AttributeName.",
                        "[Transformer - setONDEXGraph]"));
                throw new WrongParameterException(s
                        + " is not a valid AttributeName.");
            }
            ans[i] = an;
            i++;
        }

        // parse weights
        String w = (String) args.getUniqueValue(WEIGHTS_ARG);
        String[] ws = w.split(",");
        double[] weights = new double[ws.length];
        i = 0;
        for (String s : ws) {
            weights[i] = Double.parseDouble(s);
            i++;
        }

        boolean[] inverse = new boolean[weights.length];
        String inv = (String) args.getUniqueValue(INVERSE_ARG);
        if (inv != null) {
            String[] invs = inv.split(",");
            i = 0;
            for (String s : invs) {
                inverse[i] = Boolean.parseBoolean(s);
                i++;
            }
        }

        double[] min = new double[ans.length];
        double[] max = new double[ans.length];
        for (i = 0; i < ans.length; i++) {
            min[i] = Double.POSITIVE_INFINITY;
            max[i] = Double.NEGATIVE_INFINITY;
        }

        // iterate over all relations to calculate sum
        for (ONDEXRelation r : graph.getRelationsOfRelationType(rt)) {
            for (int j = 0; j < ans.length; j++) {
                Attribute attribute = r.getAttribute(ans[j]);
                if (attribute != null) {
                    double value = ((Number) attribute.getValue()).doubleValue();
                    if (value < min[j])
                        min[j] = value;
                    if (value > max[j])
                        max[j] = value;
                }
            }
        }

        // scale to [0,1]
        double[] diff = new double[ans.length];
        for (i = 0; i < ans.length; i++) {
            diff[i] = max[i] - min[i];
        }

        // make sure attribute name is there
        AttributeName an = graph.getMetaData().getAttributeName(
                "weighted");
        if (an == null)
            an = graph.getMetaData().getFactory().createAttributeName("weighted",
                    Double.class);

        // and again now to calculate normalised weights
        for (ONDEXRelation r : graph.getRelationsOfRelationType(rt)) {

            double sum = 0;
            for (int j = 0; j < ans.length; j++) {
                Attribute attribute = r.getAttribute(ans[j]);
                if (attribute != null) {
                    double value = ((Number) attribute.getValue()).doubleValue();
                    double newvalue = ((value - min[j]) / diff[j]);
                    if (inverse[j])
                        newvalue = 1 - newvalue;
                    sum += weights[j] * newvalue;
                }
            }
            sum = sum / ans.length;

            // set new Attribute
            if (r.getAttribute(an) == null) {
                r.createAttribute(an, sum, false);
            } else {
                r.getAttribute(an).setValue(sum);
            }
        }
    }

    /**
     * Returns name of this transformer.
     *
     * @return String
     */
    public String getName() {
        return "Gdsweight";
    }

    /**
     * Returns version of this transformer.
     *
     * @return String
     */
    public String getVersion() {
        return "27.11.08";
    }

    @Override
    public String getId() {
        return "attributeeight";
    }

    /**
     * Returns arguments required by this transformer.
     *
     * @return ArgumentDefinition<?>[]
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition[]{
                new StringArgumentDefinition(ArgumentNames.RELATION_TYPE_ARG,
                        ArgumentNames.RELATION_TYPE_ARG_DESC, true, null, false),
                new StringArgumentDefinition(ArgumentNames.ATTRIBUTE_ARG,
                        ArgumentNames.ATTRIBUTE_ARG_DESC, true, null, false),
                new StringArgumentDefinition(ArgumentNames.WEIGHTS_ARG,
                        ArgumentNames.WEIGHTS_ARG_DESC, true, null, false),
                new StringArgumentDefinition(ArgumentNames.INVERSE_ARG,
                        ArgumentNames.INVERSE_ARG_DESC, false, null, false)};

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
