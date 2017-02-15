package net.sourceforge.ondex.transformer.evidenceweightings;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Implements the annotation of concepts in a graph with evidence code
 * weightings, read from a properties file
 *
 * @author hindlem
 */
@Authors(authors = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Transformer extends ONDEXTransformer implements ArgumentNames {

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new BooleanArgumentDefinition(
                        ArgumentNames.ANALYSE_RELATIONS_ARG,
                        ArgumentNames.ANALYSE_RELATIONS_ARG_DESC, true, true),
                new BooleanArgumentDefinition(
                        ArgumentNames.ANALYSE_CONCEPTS_ARG,
                        ArgumentNames.ANALYSE_CONCEPTS_ARG_DESC, true, true),
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                        ArgumentNames.INPUT_FILE_DESC, true, true, false,
                        false)};
    }

    @Override
    public String getName() {
        return "evidence weightings";
    }

    @Override
    public String getVersion() {
        return "alpha";
    }

    @Override
    public String getId() {
        return "evidenceweightings";
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
        File file = new File((String) args
                .getUniqueValue(FileArgumentDefinition.INPUT_FILE));
        annotateGraph(parseProperties(file.getAbsolutePath()), (Boolean) args
                .getUniqueValue(ArgumentNames.ANALYSE_RELATIONS_ARG),
                (Boolean) args.getUniqueValue(ArgumentNames.ANALYSE_CONCEPTS_ARG));
    }

    /**
     * Annotates the graph with the weightings specified in the properties file
     * <AttributeName(id), Double(weighting)>
     *
     * @param properties       containing mappings of evidence to weights
     * @param analyseRelations add weights to relations?
     * @param analyseConcepts  add weights to concepts?
     */
    public void annotateGraph(Properties properties, boolean analyseRelations,
                              boolean analyseConcepts) {

        AttributeName weightingAtt = graph.getMetaData().getAttributeName(
                MetaData.ATT_GO_EC_WEIGHTINGS);
        if (weightingAtt == null) {
            weightingAtt = graph
                    .getMetaData()
                    .getFactory()
                    .createAttributeName(
                            MetaData.ATT_GO_EC_WEIGHTINGS,
                            "Specifed evidence weighting from 1-0 (1 being the >)",
                            Double.class);
        }

        Map<EvidenceType, Double> weightings = new HashMap<EvidenceType, Double>(
                properties.keySet().size());

        for (Object evidenceCode : properties.keySet()) {
            Object value = properties.get(evidenceCode);

            Double weighting;
            try {
                weighting = Double.valueOf(value.toString());
            } catch (NumberFormatException e) {
                System.err.println(value.toString()
                        + " is not a valid weighting");
                e.printStackTrace();
                return;
            }

            EvidenceType evidenceType = graph.getMetaData().getEvidenceType(
                    evidenceCode.toString());

            if (evidenceType != null) {
                weightings.put(evidenceType, weighting);
            } else {
                System.out.println("Evidence Type: " + evidenceCode.toString()
                        + " is not present in the graph");
            }
        }

        if (analyseRelations) {
            Set<ONDEXRelation> relationsToAnalyse = null;

            for (EvidenceType evidence : weightings.keySet()) {
                Set<ONDEXRelation> relations = BitSetFunctions.copy(graph.getRelationsOfEvidenceType(evidence));
                if (relationsToAnalyse == null) {
                    relationsToAnalyse = relations;
                } else {
                    relationsToAnalyse.addAll(relations);
                }
            }

            if (relationsToAnalyse != null) {
                for (ONDEXRelation relation: relationsToAnalyse) {
                    createWeightingsOnEntity(relation, weightings, weightingAtt);
                }
            }
        }

        if (analyseConcepts) {
            Set<ONDEXConcept> conceptsToAnalyse = null;

            for (EvidenceType evidence : weightings.keySet()) {
                Set<ONDEXConcept> concepts = graph
                        .getConceptsOfEvidenceType(evidence);
                if (conceptsToAnalyse == null) {
                    conceptsToAnalyse = concepts;
                } else {
                    conceptsToAnalyse.addAll(concepts);
                }
            }

            if (conceptsToAnalyse != null) {
                for (ONDEXConcept concept : conceptsToAnalyse) {
                    createWeightingsOnEntity(concept, weightings, weightingAtt);
                }
            }
        }

    }

    /**
     * Adds a weighting to the entity corresponding to the highest weight
     * evidence
     *
     * @param entity       the Relation or Concept to add to
     * @param weightings   a map of evidence to weights
     * @param weightingAtt the weighting attribute
     */
    private void createWeightingsOnEntity(ONDEXEntity entity,
                                          Map<EvidenceType, Double> weightings, AttributeName weightingAtt) {
        Double highestWeighting = null;
        for (EvidenceType evidence : entity.getEvidence()) {
            Double weighting = weightings.get(evidence);
            if (weighting != null) {
                if (highestWeighting == null || weighting > highestWeighting) {
                    highestWeighting = weighting;
                }
            }
        }

        if (highestWeighting != null) {
            if (entity instanceof ONDEXConcept)
                ((ONDEXConcept) entity).createAttribute(weightingAtt,
                        highestWeighting, false);
            else if (entity instanceof ONDEXRelation)
                ((ONDEXRelation) entity).createAttribute(weightingAtt,
                        highestWeighting, false);
        }
    }

    /**
     * Parses a properties file
     *
     * @param file the location of the properties file
     * @return Properties object
     */
    private Properties parseProperties(String file) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

}
