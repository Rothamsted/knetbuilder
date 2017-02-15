package net.sourceforge.ondex.transformer.extendedcollapser;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.convertConceptClasses;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.convertRelationTypes;
import static net.sourceforge.ondex.tools.functions.GraphElementManipulation.oneToManyCollapse;
import static net.sourceforge.ondex.tools.functions.GraphElementManipulation.oneToOneCollapse;
import static net.sourceforge.ondex.tools.functions.StandardFunctions.getOtherNode;
import static net.sourceforge.ondex.tools.functions.ViewConstruction.getConceptsOfTypes;

/**
 * This transformer implements one-to-many and one-to-one relation collapse, as opposed to
 * many-to-many collapse functionality that is already provided by the "relationcollapser"
 * transformer.
 * The source and target arguments define what will be merged to what, NOT the
 * source and target of the relation. In this version (v0.1) of the transformer the directionality of
 * the relation is ignored.
 *
 * @author lysenkoa
 */
@Authors(authors = {"Artem Lysenko"}, emails = {"lysenkoa at users.sourceforge.net"})
@Status(description = "Tested December 2013 (Artem Lysenko)", status = StatusType.STABLE)
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Transformer extends ONDEXTransformer implements ArgumentNames {

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(RELATION_TYPES_ARG,
                        RELATION_TYPES_DESC, true, null, true),
                new StringArgumentDefinition(SOURCE_CCS_ARG,
                        SOURCE_CCS_DESC, true, null, true),
                new StringArgumentDefinition(TARGET_CCS_ARG,
                        TARGET_CCS_DESC, true, null, true),
                new BooleanArgumentDefinition(ONE_TO_ONE_MODE_ARG,
                        ONE_TO_ONE_MODE_DESC, true, false),
        };
    }

    @Override
    public String getName() {
        return "Reverse collapser";
    }

    @Override
    public String getVersion() {
        return "v0.1";
    }

    @Override
    public String getId() {
        return "extendedcollapser";
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
        boolean oneToOne = (Boolean) args.getUniqueValue(ONE_TO_ONE_MODE_ARG);
        Set<RelationType> rts = new HashSet<RelationType>(Arrays.asList(convertRelationTypes(graph, (List<String>) args.getObjectValueList(RELATION_TYPES_ARG))));
        Set<ConceptClass> targetClasses = new HashSet<ConceptClass>(Arrays.asList(convertConceptClasses(graph, (List<String>) args.getObjectValueList(TARGET_CCS_ARG))));
        Set<ONDEXConcept> sources = getConceptsOfTypes(graph, (List<String>) args.getObjectValueList(SOURCE_CCS_ARG));
        Set<ONDEXRelation> toDeleteR = new HashSet<ONDEXRelation>();
        Set<ONDEXConcept> joinTarget = new HashSet<ONDEXConcept>();
        Set<ONDEXConcept> toDeleteC = new HashSet<ONDEXConcept>();
        Set<ONDEXConcept> targetIndex = new HashSet<ONDEXConcept>();
        for (ONDEXConcept c : sources) {
            for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
                if (rts.contains(r.getOfType())) {
                    ONDEXConcept candidate = getOtherNode(c, r);
                    if (targetClasses.contains(candidate.getOfType()) || targetIndex.contains(candidate)) {
                        toDeleteR.add(r);
                        joinTarget.add(candidate);
                    }
                }
            }

            for (ONDEXRelation r : toDeleteR) {
                graph.deleteRelation(r.getId());
            }
            toDeleteR.clear();
            if (joinTarget.size() > 0) {
                if (oneToOne) {
                    for (ONDEXConcept t : joinTarget) {
                        oneToOneCollapse(graph, c, t);
                        toDeleteC.add(t);
                    }
                } else {
                    targetIndex.addAll(oneToManyCollapse(graph, c, joinTarget));
                }
                toDeleteC.add(c);
            }
            joinTarget.clear();
        }
        for (ONDEXConcept c : toDeleteC) {
            graph.deleteConcept(c.getId());
        }
    }
}
