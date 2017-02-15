package net.sourceforge.ondex.mapping.inferGoTerm;

import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.mapping.ONDEXMapping;

import java.util.*;

/**
 * @author hindlem
 */
@Custodians(custodians = {"Keywan Hassani-pak"}, emails = {"keywan at users.sourceforge.net"})
public class Mapping extends ONDEXMapping
{


    @Override
    public String getId() {
        return "inferGoTerm";
    }

    @Override
    public String getName() {
        return "inference of GO terms based on Ondex graph";
    }

    @Override
    public String getVersion() {
        return "alpha";
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[0];
    }

    @Override
    public void start() throws Exception {

        AttributeName attDS = graph.getMetaData().getAttributeName(MetaData.ATT_DATASOURCE);
        if (attDS == null) {
            attDS = graph.getMetaData().getFactory().createAttributeName(MetaData.ATT_DATASOURCE, "Datasource where this ONDEXEntity originated", String.class);
        }

        EvidenceType ettf = graph.getMetaData().getFactory().createEvidenceType("IEA:TFIMPD", "transcription factor IMPD");
        EvidenceType etenz = graph.getMetaData().getFactory().createEvidenceType("IEA:EIMPD", "enzyme IMPD");

        ConceptClass function = graph.getMetaData().getConceptClass(MetaData.MolFunc);
        ConceptClass process = graph.getMetaData().getConceptClass(MetaData.BioProc);
        ConceptClass location = graph.getMetaData().getConceptClass(MetaData.CelComp);

        Set<ConceptClass> goTerms = new HashSet<ConceptClass>();
        goTerms.add(function);
        goTerms.add(process);
        goTerms.add(location);

        DataSource go_dataSource = graph.getMetaData().getDataSource(net.sourceforge.ondex.parser.go.MetaData.cvGO);

        Map<String, Set<Integer>> map = createIndex(goTerms, go_dataSource, graph);

        Set<ONDEXConcept> enzymeActivity = new HashSet<ONDEXConcept>();
        for (Integer cid : map.get("GO:0003824")) {
            enzymeActivity.add(graph.getConcept(cid));
        }

        Set<ONDEXConcept> transcriptionFactorActivityFunction = new HashSet<ONDEXConcept>();
        for (Integer cid : map.get("GO:0003700"))
            transcriptionFactorActivityFunction.add(graph.getConcept(cid));

        Set<ONDEXConcept> transcriptionFactorActivityProcess = new HashSet<ONDEXConcept>();
        for (Integer cid : map.get("GO:0090046"))
            transcriptionFactorActivityProcess.add(graph.getConcept(cid));

        RelationType is_a = graph.getMetaData().getRelationType("is_a");

        RelationType has_function = graph.getMetaData().getRelationType(MetaData.hasFunction);
        RelationType has_participant = graph.getMetaData().getRelationType(MetaData.hasParticipant);
        RelationType loc_in = graph.getMetaData().getRelationType(MetaData.locatedIn);

        Set<RelationType> relations_internal = new HashSet<RelationType>();
        relations_internal.add(has_function);
        relations_internal.add(has_participant);
        relations_internal.add(loc_in);

        int i = 0;
        Set<ONDEXConcept> tfs = graph.getConceptsOfConceptClass(graph.getMetaData().getConceptClass("TF"));
        for (ONDEXConcept tf : tfs) {
            for (ONDEXConcept go : transcriptionFactorActivityFunction) {
                if (graph.getRelation(tf, go, has_function) == null) {
                    ONDEXRelation relation = graph.getFactory().createRelation(tf, go, has_function, ettf);
                    relation.createAttribute(attDS, "Inferred from TF ConceptClass", false);
                    i++;
                }
            }
            for (ONDEXConcept go : transcriptionFactorActivityProcess) {
                if (graph.getRelation(tf, go, has_function) == null) {
                    ONDEXRelation relation = graph.getFactory().createRelation(tf, go, has_function, ettf);
                    relation.createAttribute(attDS, "Inferred from TF ConceptClass", false);
                    i++;
                }
            }
        }
        System.out.println("Created " + i + " new relations from transcription factors to GO:0003700 and GO:0090046");

        Set<ONDEXConcept> enzymes = graph.getConceptsOfConceptClass(graph.getMetaData().getConceptClass("Enzyme"));
        for (ONDEXConcept enzyme : enzymes) {
            for (ONDEXConcept go : enzymeActivity) {
                if (graph.getRelation(enzyme, go, has_function) == null) {
                    ONDEXRelation relation = graph.getFactory().createRelation(enzyme, go, has_function, etenz);
                    relation.createAttribute(attDS, "Inferred from Enzyme ConceptClass", false);
                    i++;
                }
            }
        }
        System.out.println("Created " + i + " new relations from enzymes to GO:0003824");

        ConceptClass protein = graph.getMetaData().getConceptClass(MetaData.protein);
        ConceptClass gene = graph.getMetaData().getConceptClass(MetaData.gene);
        ConceptClass enzyme = graph.getMetaData().getConceptClass(MetaData.enzyme);
        ConceptClass tf = graph.getMetaData().getConceptClass(MetaData.tf);

        int stats = propergateGoRelations(tf, protein, is_a, relations_internal);
        System.out.println("Copied " + stats + " GO annotations from TF to Protein");
        stats = propergateGoRelations(enzyme, protein, is_a, relations_internal);
        System.out.println("Copied " + stats + " GO annotations from ENZYME to Protein");
        stats = propergateGoRelations(gene, protein, is_a, relations_internal);
        System.out.println("Copied " + stats + " GO annotations from GENE to Protein");

        for (RelationType rt : relations_internal) {
            for (ONDEXRelation relation : graph.getRelationsOfRelationType(rt)) {
                ONDEXConcept goConcept = relation.getFromConcept();
                if (!goTerms.contains(goConcept.getOfType())) {
                    goConcept = relation.getToConcept();
                }
                if (!goTerms.contains(goConcept.getOfType())) continue;//invalid

                String[] cvs = goConcept.getElementOf().getId().split(":");
                List<String> cvdatasource = new ArrayList<String>();
                for (String cv : cvs) {
                    if (!cv.equals("GO")) cvdatasource.add(cv);
                }
                Attribute dataSource = relation.getAttribute(attDS);
                if (dataSource == null) {
                    relation.createAttribute(attDS, cvdatasource, false);
                } else if (dataSource.getValue() instanceof String) {
                    if (!cvdatasource.contains(dataSource.getValue())) {
                        cvdatasource.add((String) dataSource.getValue());
                    }
                    dataSource.setValue(cvdatasource);
                } else if (dataSource.getValue() instanceof Collection) {
                    for (Object source : (Collection) dataSource.getValue()) {
                        if (!cvdatasource.contains((String) source)) {
                            cvdatasource.add((String) source);
                        }
                    }
                    dataSource.setValue(cvdatasource);
                }
            }
        }
    }

    /**
     * @param source
     * @param copy_to
     * @param rt
     * @param internalTypes
     */
    private int propergateGoRelations(ConceptClass source,
                                      ConceptClass copy_to,
                                      RelationType rt,
                                      Set<RelationType> internalTypes) {
        int i = 0;

        for (ONDEXConcept concept : graph.getConceptsOfConceptClass(source)) {
            Set<ONDEXRelation> relationsToBeCopied = new HashSet<ONDEXRelation>();
            Set<ONDEXConcept> conceptsToBeCopied = new HashSet<ONDEXConcept>();

            for (ONDEXRelation relation : graph.getRelationsOfConcept(concept)) {
                if (internalTypes.contains(relation.getOfType())) {
                    relationsToBeCopied.add(relation);
                } else if (relation.getOfType().equals(rt)) {
                    if (relation.getKey().getFromID() == concept.getId()
                            && relation.getToConcept().getOfType().equals(copy_to)) {
                        conceptsToBeCopied.add(relation.getToConcept());
                    } else if (relation.getKey().getToID() == concept.getId()
                            && relation.getFromConcept().getOfType().equals(copy_to)) {
                        conceptsToBeCopied.add(relation.getFromConcept());
                    }
                }
            }

            for (ONDEXConcept conceptTo : conceptsToBeCopied) {
                for (ONDEXRelation relation : relationsToBeCopied) {
                    if (relation.getKey().getFromID() == concept.getId()) {
                        if (graph.getRelation(conceptTo, relation.getToConcept(), relation.getOfType()) == null) {
                            graph.createRelation(conceptTo,
                                    relation.getToConcept(),
                                    relation.getOfType(),
                                    relation.getEvidence());
                            i++;
                        }
                    } else if (relation.getKey().getToID() == concept.getId()) {
                        if (graph.getRelation(relation.getFromConcept(), conceptTo, relation.getOfType()) == null) {
                            if (graph.getRelation(conceptTo, relation.getFromConcept(), relation.getOfType()) == null) {
                                graph.createRelation(relation.getFromConcept(),
                                        conceptTo,
                                        relation.getOfType(),
                                        relation.getEvidence());
                                i++;
                            }
                        }
                    }
                }

            }
        }
        return i;
    }

    /**
     * @param ontologies
     * @param go_dataSource
     * @param graph
     * @return
     */
    private Map<String, Set<Integer>> createIndex(Set<ConceptClass> ontologies, DataSource go_dataSource, ONDEXGraph graph) {

        Map<String, Set<Integer>> index = new HashMap<String, Set<Integer>>();
        for (ConceptClass ontology : ontologies) {
            for (ONDEXConcept concept : graph.getConceptsOfConceptClass(ontology)) {
                for (ConceptAccession accession : concept.getConceptAccessions()) {
                    if (accession.getElementOf().equals(go_dataSource)) {
                        Set<Integer> concepts = index.get(accession.getAccession().trim().toUpperCase());
                        if (concepts == null) {
                            concepts = new HashSet<Integer>();
                            index.put(accession.getAccession().trim().toUpperCase(),
                                    concepts);
                        }
                        concepts.add(concept.getId());
                    }
                }
            }
        }
        return index;
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
