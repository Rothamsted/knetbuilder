package net.sourceforge.ondex.mapping.transitivemembership;

import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.mapping.ONDEXMapping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

import static net.sourceforge.ondex.mapping.transitivemembership.ArgumentNames.*;

/**
 * if A-(r)->B-(r)->C where A and C are the same CC but different CVs
 *
 * @author hindlem
 */
@Custodians(custodians = {"Matthew Hindle"}, emails = {" matthew_hindle at users.sourceforge.net"})
public class Mapping extends ONDEXMapping
{

    private static String EVIDIENCE = "TransitiveInference";

    private boolean exportSentences = true;
    private boolean mapBetweenCVsOnly = true;

    @Override
    public void start() throws Exception {

        System.out.println("start");


        String dir = (String) args.getUniqueValue(EXPORT_DIR_ARG);
        if (dir == null) {
            exportSentences = false;
        } else if (new File(dir).exists() || new File(dir).mkdirs()) {
            exportSentences = true;
        }

        Map<DataSource, Set<String>> dataSourceToSentence = new HashMap<DataSource, Set<String>>();

        Set<ConceptClass> subject = getConceptClasses(args.getObjectValueList(SUBJECT_CONCEPT_CLASS_ARG));
        Set<ConceptClass> inference = getConceptClasses(args.getObjectValueList(INFERENCE_CONCEPT_CLASS_ARG));

        RelationType existingRelation = graph.getMetaData().getRelationType(args.getUniqueValue(RELATION_TYPE_ARG).toString());
        RelationType newRelation = graph.getMetaData().getRelationType(args.getUniqueValue(NEW_RELATION_TYPE_ARG).toString());

        EvidenceType evidenceType = graph.getMetaData().getEvidenceType(EVIDIENCE);
        if (evidenceType == null) {
            evidenceType = graph.getMetaData().createEvidenceType(EVIDIENCE, "Transitive Inference", "A relation that represents a transitive inference over a graph");
        }

        Set<EvidenceType> evidence = new HashSet<EvidenceType>(1);
        evidence.add(evidenceType);

        for (ONDEXRelation relation : graph.getRelationsOfRelationType(existingRelation)) {
            ONDEXConcept conceptA = relation.getFromConcept();
            ONDEXConcept conceptB = relation.getToConcept();

            if (inference.contains(conceptA.getOfType())) {
                ONDEXConcept temp = conceptA;
                conceptA = conceptB;
                conceptB = temp;
            }

            if (inference.contains(conceptB.getOfType()) &&
                    subject.contains(conceptA.getOfType())) {
                for (ONDEXRelation relation2 : graph.getRelationsOfConcept(conceptB)) {
                    if (!relation2.equals(relation)
                            && relation2.getOfType().equals(existingRelation)) {
                        ONDEXConcept concept2A = relation2.getFromConcept();
                        ONDEXConcept concept2B = relation2.getToConcept();
                        if (inference.contains(concept2B.getOfType())) {
                            ONDEXConcept temp = concept2A;
                            concept2A = concept2B;
                            concept2B = temp;
                        }

                        if (subject.contains(concept2B.getOfType()) && inference.contains(concept2A.getOfType())) {

                            if (!mapBetweenCVsOnly || !conceptA.getElementOf().equals(concept2B.getElementOf())) {
                                createInferenceRelation(conceptA, concept2B, newRelation, evidence);

                                if (exportSentences) {
                                    Set<String> list = dataSourceToSentence.get(conceptA.getElementOf());
                                    if (list == null) {
                                        list = new HashSet<String>();
                                        dataSourceToSentence.put(conceptA.getElementOf(), list);
                                    }

                                    String conceptAName = "";
                                    if (concept2B.getConceptName() != null) {
                                        conceptAName = concept2A.getConceptName().getName();
                                    }

                                    String conceptBName = "";
                                    if (concept2B.getConceptName() != null) {
                                        conceptBName = concept2B.getConceptName().getName();
                                    }

                                    list.add(conceptA.getElementOf()
                                            + "\t" + conceptA.getPID().toString()
                                            + "\t" + conceptB.getPID().toString()
                                            + "\t" + conceptAName
                                            + "\t" + concept2B.getPID().toString()
                                            + "\t" + conceptBName
                                            + "\t" + concept2B.getElementOf()
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }

        if (exportSentences) {
            Set<DataSource> cvs = dataSourceToSentence.keySet();
            for (DataSource dataSource : cvs) {
                Set<String> items = dataSourceToSentence.get(dataSource);
                File file = new File(dir + File.separator + dataSource.getId() + "_inference_sentences.tab");
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                for (String item : items) {
                    writer.write(item);
                    System.out.println("Line" + dataSource.getId() + "---" + item);
                    writer.newLine();
                }
                writer.flush();
            }
        }

    }

    /**
     * @param stringCCNames
     * @return
     */
    private Set<ConceptClass> getConceptClasses(List<?> stringCCNames) {
        Set<ConceptClass> ccs = new HashSet<ConceptClass>();
        for (Object value : stringCCNames) {
            ConceptClass cc = graph.getMetaData().getConceptClass(
                    value.toString());
            if (cc != null) {
                ccs.add(cc);
            } else {
                System.err.println(value + " is not a valid ConceptClass");
            }
        }
        return ccs;
    }

    private void createInferenceRelation(ONDEXConcept conceptA,
                                         ONDEXConcept concept2B, RelationType newRelation, Collection<EvidenceType> evidence) {
        graph.createRelation(conceptA, concept2B, newRelation, evidence);
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(SUBJECT_CONCEPT_CLASS_ARG,
                        SUBJECT_CONCEPT_CLASS_ARG_DESC, true, null, true),
                new StringArgumentDefinition(INFERENCE_CONCEPT_CLASS_ARG,
                        INFERENCE_CONCEPT_CLASS_ARG_DESC, true, null, true),
                new StringArgumentDefinition(RELATION_TYPE_ARG,
                        RELATION_TYPE_ARG_DESC, true, null, false),
                new StringArgumentDefinition(NEW_RELATION_TYPE_ARG,
                        NEW_RELATION_TYPE_ARG_DESC, true, null, false),
                new StringArgumentDefinition(EXPORT_DIR_ARG,
                        EXPORT_DIR_ARG_DESC, true, null, false)
        };
    }

    @Override
    public String getName() {
        return "transitive membership";
    }

    @Override
    public String getVersion() {
        return "alpha";
    }

    @Override
    public String getId() {
        return "transitivemembership";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

}
