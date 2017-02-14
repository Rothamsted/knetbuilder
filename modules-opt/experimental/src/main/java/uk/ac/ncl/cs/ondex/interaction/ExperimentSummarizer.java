/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.ondex.interaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.IntegerRangeArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.transformer.ONDEXTransformer;
import org.apache.log4j.Level;
import uk.ac.ncl.cs.ondex.tools.Neighbour;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class ExperimentSummarizer extends ONDEXTransformer {

    private static final String CUTOFF_ARG = "Cutoff";
    private static final String CUTOFF_ARG_DESC = "Maximal number of nodes in an experiment to be considered for summarizing.";

    private AttributeName anNegative;
    private EvidenceType etIMPD;
    private DataSource dsUC;
    private ConceptClass ccExp;
    private EvidenceType etTF;

    private int cutoff;
    private Map<String,Set<ONDEXConcept>> index = new HashMap<String, Set<ONDEXConcept>>();

    @Override
    public String getId() {
        return "expsum";
    }

    @Override
    public String getName() {
        return "Interaction Experiment Summarizer";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
            new IntegerRangeArgumentDefinition(CUTOFF_ARG, CUTOFF_ARG_DESC, true, 10, 1, Integer.MAX_VALUE)
        };
    }

    @Override
    public void start() throws Exception {

        cutoff = (Integer) args.getUniqueValue(CUTOFF_ARG);

        ccExp = requireConceptClass("Experiment");
        anNegative = requireAttributeName("negative");
        etIMPD = requireEvidenceType("IMPD");
        dsUC = requireDataSource("UC");
        etTF = requireEvidenceType("InferredByTransformation");
        ConceptClass ccPI = requireConceptClass("MI:0914");
        ConceptClass ccPP = requireConceptClass("Polypeptide");
        ConceptClass ccGI = requireConceptClass("MI:0208");
        ConceptClass ccNucl = requireConceptClass("NucleotideFeature");

        search(ccExp, ccPI, ccPP);
        search(ccExp, ccGI, ccNucl);

        printGroups();

        merge();

        logComplaints();

    }

    private void search(ConceptClass ccExp, ConceptClass ccIntn, ConceptClass ccIntr) {

        Set<ONDEXConcept> interactors = new HashSet<ONDEXConcept>();
        Set<ONDEXConcept> interactions = new HashSet<ONDEXConcept>();
//        IntSet interactors = new IntOpenHashSet();
//        IntSet interactions = new IntOpenHashSet();

        int empty = 0;

        for (ONDEXConcept experiment : graph.getConcepts()) {
            if (experiment.inheritedFrom(ccExp)) {

                interactors.clear();
                interactions.clear();

                //count number of interactors and interactions
                for (Neighbour interactionNeighbour : Neighbour.getNeighbourhood(experiment, graph)) {
                    if (interactionNeighbour.getConcept().inheritedFrom(ccIntn)) {
                        ONDEXConcept interaction = interactionNeighbour.getConcept();
                        if (!isNegative(interaction)) {
                            interactions.add(interaction);
                        }
                        for (Neighbour interactorNeighbour : Neighbour.getNeighbourhood(interaction, graph)) {
                            if (interactorNeighbour.getConcept().inheritedFrom(ccIntr)) {
                                interactors.add(interactorNeighbour.getConcept());
                            }
                        }
                    }
                }

                //check for empty experiments
                if (interactors.size() == 0) {

                    if (interactions.size() > 0) {
                        fileComplaint("Experiment contains empty interactions: "+experiment.getConceptName());
                    } //otherwise it's probably just an experiment of the wrong type

                //if experiment is LTP (less nodes than cutoff)
                } else if (interactors.size() < cutoff) {

                    //index it according to its evidence types
                    List<String> evidences = new ArrayList<String>();
                    for (EvidenceType et : experiment.getEvidence()) {
                        if (!et.equals(etIMPD) && !et.equals(etTF)) {
                            evidences.add(et.getFullname());
                        }
                    }
                    Collections.sort(evidences);
                    index(experiment, evidences.toString());
                }

            }
        }

    }

    private boolean isNegative(ONDEXConcept cIntn) {
        Attribute a = cIntn.getAttribute(anNegative);
        if (a != null && a.getValue() != null && a.getValue().equals(Boolean.TRUE)) {
            return true;
        } else {
            return false;
        }
    }


    private static int expId = 0;

    private void merge() {

        for (Set<ONDEXConcept> cSet : index.values()) {

            //create replacement concept
            ONDEXConcept mergeTarget = graph.getFactory().createConcept("ExperimentCollection#"+(expId++), dsUC, ccExp, etTF);

            //prepare merging containers
            Set<ConceptName> names = new HashSet<ConceptName>();
            Set<ConceptAccession> accessions = new HashSet<ConceptAccession>();
            Set<EvidenceType> evidences = new HashSet<EvidenceType>();
            Map<AttributeName,Attribute> attributes = new HashMap<AttributeName, Attribute>();

            for (ONDEXConcept mergeSource : cSet) {

                //fill merge containers
                names.addAll(mergeSource.getConceptNames());
                accessions.addAll(mergeSource.getConceptAccessions());
                evidences.addAll(mergeSource.getEvidence());
                //fill concept attribute container
                for (Attribute mergeSourceAttribute : mergeSource.getAttributes()) {
                    Attribute mergeTargetAttribute = attributes.get(mergeSourceAttribute.getOfType());
                    if (mergeTargetAttribute != null) {//then we have to combine the two attributes
                        if (!mergeSourceAttribute.getValue().equals(mergeTargetAttribute.getValue())) {//then it's a conflict
                            if (Map.class.isAssignableFrom(mergeSourceAttribute.getOfType().getDataType())) {
                                ((Map)mergeTargetAttribute.getValue()).putAll((Map)mergeSourceAttribute.getValue());//FIXME may overwrite something
//                                log("Merged a map attribute");
                            } else if (Collection.class.isAssignableFrom(mergeSourceAttribute.getOfType().getDataType())) {
                                ((Collection)mergeTargetAttribute.getValue()).addAll((Collection)mergeSourceAttribute.getValue());//FIXME may overwrite something
//                                log("Merged a collection attribute");
                            } else {
                                fileComplaint("Cannot merge attribute "+mergeSourceAttribute.getOfType().getFullname());
                            }
                        } else {//no conflict
                            //ignore redundant attribute
                        }
                    } else {//then it's a new attribute
                        attributes.put(mergeSourceAttribute.getOfType(),mergeSourceAttribute);
                    }
                }

                //connect edges
                for (Neighbour sourceNeighbour : Neighbour.getNeighbourhood(mergeSource, graph)) {
                    ONDEXRelation targetRelation = null;
                    if (sourceNeighbour.isOutgoing()) {

                        targetRelation = graph.getRelation(mergeTarget, sourceNeighbour.getConcept(), sourceNeighbour.getRelation().getOfType());
                        if (targetRelation == null) {
                            targetRelation = graph.getFactory().createRelation(mergeTarget, sourceNeighbour.getConcept(), sourceNeighbour.getRelation().getOfType(), etTF);
                        }

                    } else {

                        targetRelation = graph.getRelation(sourceNeighbour.getConcept(), mergeTarget, sourceNeighbour.getRelation().getOfType());
                        if (targetRelation == null) {
                            targetRelation = graph.getFactory().createRelation(sourceNeighbour.getConcept(), mergeTarget, sourceNeighbour.getRelation().getOfType(), etTF);
                        }

                    }

                    //copy all evidences to new relation
                    for (EvidenceType et : sourceNeighbour.getRelation().getEvidence()) {
                        targetRelation.addEvidenceType(et);
                    }

                    //copy all attributes to new relation
                    for (Attribute sourceAttribute : sourceNeighbour.getRelation().getAttributes()) {
                        Attribute targetAttribute = targetRelation.getAttribute(sourceAttribute.getOfType());
                        if (targetAttribute != null) {//then it's either known or a conflict
                            if (!sourceAttribute.getValue().equals(targetAttribute.getValue())) {//then it's a conflict
                                if (Map.class.isAssignableFrom(sourceAttribute.getOfType().getDataType())) {
                                    ((Map)targetAttribute.getValue()).putAll((Map)sourceAttribute.getValue());//FIXME may overwrite something
//                                        log("Merged a map attribute");
                                } else if (Collection.class.isAssignableFrom(sourceAttribute.getOfType().getDataType())) {
                                    ((Collection)targetAttribute.getValue()).addAll((Collection)sourceAttribute.getValue());//FIXME may overwrite something
//                                        log("Merged a collection attribute");
                                } else {
                                    fileComplaint("Cannot merge attribute "+sourceAttribute.getOfType().getFullname());
                                }
                            } else {//no conflict
                                //ignore redundant attribute
                            }
                        } else {//then it's a new attribute
                            targetRelation.createAttribute(sourceAttribute.getOfType(), sourceAttribute.getValue(), false);
                        }

                    }

                }

            }

            //apply contents of merge containers
            for (ConceptName name : names) {
                mergeTarget.createConceptName(name.getName(), name.isPreferred());
            }
            for (ConceptAccession acc : accessions) {
                mergeTarget.createConceptAccession(acc.getAccession(), acc.getElementOf(), acc.isAmbiguous());
            }
            for (EvidenceType et : evidences) {
                mergeTarget.addEvidenceType(et);
            }
            for (Entry<AttributeName,Attribute> entry : attributes.entrySet()) {
                mergeTarget.createAttribute(entry.getKey(), entry.getValue().getValue(), false);
            }

            //delete old experiments
            for (ONDEXConcept cSmall : cSet) {
                graph.deleteConcept(cSmall.getId());
            }
        }
        
    }

    private void printGroups() {
        StringBuilder b = new StringBuilder();
        for (Entry<String,Set<ONDEXConcept>> entry : index.entrySet()) {
            b.append(entry.getKey()).append("\t")
                    .append(entry.getValue().size()).append("\n");
        }
        log(b.toString());
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    private Map<String,Integer> complaints = new HashMap<String, Integer>();
    
    private void fileComplaint(String complaint) {
        Integer count = complaints.get(complaint);
        if (count == null) {
            complaints.put(complaint,1);
        } else {
            complaints.put(complaint,count+1);
        }
    }

    private void logComplaints() {
        StringBuilder b = new StringBuilder();
        for (Entry<String,Integer> complaint : complaints.entrySet()) {
            b.append(complaint.getKey()).append(" (").append(complaint.getValue()).append(" cases)\n");
        }
        EventType e = new InconsistencyEvent(b.toString(), "");
        e.setLog4jLevel(Level.WARN);
        fireEventOccurred(e);
    }

    private void log(String s) {
        EventType e = new GeneralOutputEvent(s, "");
        e.setLog4jLevel(Level.INFO);
        fireEventOccurred(e);
    }

    private void index(ONDEXConcept c, String key) {
        Set<ONDEXConcept> set = index.get(key);
        if (set == null) {
            set = new HashSet();
            index.put(key,set);
        }
        set.add(c);
    }

    private String printSet(Set<EvidenceType> evidence) {
        StringBuilder b = new StringBuilder("[ ");
        for (EvidenceType et : evidence) {
            b.append(et.getFullname()).append(" ; ");
        }
        b.delete(b.length() - 3, b.length());
        b.append(" ]");
        return b.toString();
    }


}
