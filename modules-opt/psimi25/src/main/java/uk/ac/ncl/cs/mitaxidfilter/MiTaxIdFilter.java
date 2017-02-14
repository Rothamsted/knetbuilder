/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.mitaxidfilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.filter.ONDEXFilter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author jweile
 */
public class MiTaxIdFilter extends ONDEXFilter {

    /* ###################
     * #### ARGUMENTS ####
     * ###################
     */

    private static final String TAXID_LIST_ARG = "taxidList";
    
    private static final String TAXID_LIST_ARG_DESC = "Comma-separated list of " +
                                                "taxonomy IDs to filter for.";
            
    private static final String EXCL_ARG = "exclusive";
    
    private static final String EXCL_ARG_DESC = "Exclusive Mode: Retain only listed taxids. Inclusive mode: Kill only listed taxids";

    private Set<Integer> targetTaxids = new HashSet<Integer>();

    private boolean exclusiveMode;

    /**
     *
     * @return
     */
    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {

        return new ArgumentDefinition<?>[] {

            new StringArgumentDefinition(TAXID_LIST_ARG, TAXID_LIST_ARG_DESC, true, "", false),
            new BooleanArgumentDefinition(EXCL_ARG, EXCL_ARG_DESC, false, false),
            
        };

    }


    /**
     *
     * @throws InvalidPluginArgumentException
     */
    private void processArguments() throws InvalidPluginArgumentException {

        try {

            String taxidArg = (String) getArguments().getUniqueValue(TAXID_LIST_ARG);
            for (String taxid :  taxidArg.split(",")) {
                targetTaxids.add(Integer.parseInt(taxid));
            }

            exclusiveMode = (Boolean) getArguments().getUniqueValue(EXCL_ARG);

        } catch (Exception e) {
            throw new InvalidPluginArgumentException(e.getMessage());
        }

    }

    /* ####################
     * ##### METADATA #####
     * ####################
     */

    private ConceptClass ccParticipant, ccORF, ccInteraction, ccExp;

    private RelationType rtParticipates;

    private AttributeName anTaxid;

    /**
     * 
     * @throws MetaDataMissingException
     */
    private void initMetaData() throws MetaDataMissingException {

        ccParticipant = requireConceptClass("Participant");
        ccInteraction = requireConceptClass("Interaction");
        ccORF = requireConceptClass("ORF");
        ccExp = requireConceptClass("Experiment");

        rtParticipates = requireRelationType("participates_in");

        anTaxid = requireAttributeName("TAXID");
        
    }

    /* ##########################
     * ##### MAIN PROCEDURE #####
     * ##########################
     */

    /**
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {

        processArguments();
        initMetaData();

        nukeTaintedInteractions();

        killEmptyExperiments();

        killSingletons();

        logger.log(Level.INFO, "deleting "+__deletedConcepts.size()+
                " concepts and "+__deletedRelations.size()+" relations...");
        
    }

    private void nukeTaintedInteractions() {

        for (ONDEXConcept c : getConceptsOfType(ccParticipant, ccORF)) {

            Attribute taxAttribute = c.getAttribute(anTaxid);
            if (taxAttribute != null) {

                int taxid = Integer.parseInt((String) taxAttribute.getValue());
                boolean hit = targetTaxids.contains(taxid);

                if (hit ^ exclusiveMode) {//XOR (and before you ask: yes, bitwise operators also work on booleans)

                    kill(c);

                    for (Neighbour n : getNeighbourhood(c) ) {
                        
                        if (isOfType(n.getRelation(),rtParticipates) &&
                                    isOfType(n.getConcept(), ccInteraction) &&
                                    !isDead(n.getConcept())) {

                            nuke(n.getConcept());
                            
                        }
                    }

                }

            } else { //null taxid

            }

        }

    }

    private void nuke(ONDEXConcept interaction) {

        kill(interaction);

        for (Neighbour neighbour : getNeighbourhood(interaction)) {

            if (isOfType(neighbour.getConcept(),ccExp)) {
                flag(neighbour.getConcept());
            }

            kill(neighbour.getRelation());
        }

    }

    private void killEmptyExperiments() {

        for (ONDEXConcept exp : getFlaggedExperiments()) {

            boolean empty = true;
            for (Neighbour neighbour : getNeighbourhood(exp)) {
                if (isOfType(neighbour.getConcept(),ccInteraction)) {
                    empty = false;
                    break;
                }
            }

            if (empty) {
                kill(exp);
                for (ONDEXRelation r : graph.getRelationsOfConcept(exp)) {
                    kill(r);
                }
            }
        }

    }

    private void killSingletons() {

        for (ONDEXConcept c : graph.getConcepts()) {
            if (graph.getRelationsOfConcept(c).size() == 0) {
                kill(c);
            }
        }

    }

    /*
     * #### Little helpers ####
     */

    private Set<ONDEXConcept> getConceptsOfType(ConceptClass... ccs) {

        Set<ONDEXConcept> set = new HashSet<ONDEXConcept>();

        for (ONDEXConcept c : graph.getConcepts()) {
            for (ConceptClass cc : ccs) {
                if (cc.isAssignableFrom(c.getOfType())) {
                    set.add(c);
                    break;
                }
            }
        }

        return set;
    }

    private boolean isOfType(ONDEXConcept c, ConceptClass cc) {
        return cc.isAssignableFrom(c.getOfType());
    }

    private boolean isOfType(ONDEXRelation r, RelationType rt) {
        return rt.isAssignableFrom(r.getOfType());
    }
    

    private List<Neighbour> getNeighbourhood(ONDEXConcept c) {

        List<Neighbour> neighbours = new ArrayList<Neighbour>();

        for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
            ONDEXConcept nc = r.getFromConcept().equals(c) ?
                            r.getToConcept() : r.getFromConcept();
            Neighbour neighbour = new Neighbour(nc,r);
            neighbours.add(neighbour);
        }

        return neighbours;
    }

    private void kill(ONDEXEntity e) {
        if (e instanceof ONDEXConcept) {
            __deletedConcepts.add((ONDEXConcept) e);
        } else if (e instanceof ONDEXRelation) {
            __deletedRelations.add((ONDEXRelation) e);
        } else {
            throw new IllegalArgumentException(e.getClass().toString());
        }
    }

    private boolean isDead(ONDEXEntity e) {
        if (e instanceof ONDEXConcept) {
            return __deletedConcepts.contains((ONDEXConcept) e);
        } else if (e instanceof ONDEXRelation) {
            return __deletedRelations.contains((ONDEXRelation) e);
        } else {
            throw new IllegalArgumentException(e.getClass().toString());
        }
    }


    private Set<ONDEXConcept> __flaggedExperiments = new HashSet<ONDEXConcept>();

    private void flag(ONDEXConcept c) {
        __flaggedExperiments.add(c);
    }

    private boolean isFlagged(ONDEXConcept c) {
        return __flaggedExperiments.contains(c);
    }

    public Set<ONDEXConcept> getFlaggedExperiments() {
        return __flaggedExperiments;
    }

    

    /* ##########################
     * ##### FILTER METHODS #####
     * ##########################
     */

    private Set<ONDEXConcept> __deletedConcepts = new HashSet<ONDEXConcept>();
    private Set<ONDEXRelation> __deletedRelations = new HashSet<ONDEXRelation>();

    @Override
    public Set<ONDEXConcept> getVisibleConcepts() {
        Set<ONDEXConcept> set = new HashSet<ONDEXConcept>();
        set.addAll(graph.getConcepts());
        set.removeAll(__deletedConcepts);
        return set;
    }

    @Override
    public Set<ONDEXRelation> getVisibleRelations() {
        Set<ONDEXRelation> set = new HashSet<ONDEXRelation>();
        set.addAll(graph.getRelations());
        set.removeAll(__deletedRelations);
        return set;
    }
    
    @Override
    public void copyResultsToNewGraph(ONDEXGraph exportGraph) {

        Map<Integer,Integer> conceptIdMap = new HashMap<Integer,Integer>();

        for (ONDEXConcept c : getVisibleConcepts()) {

            ConceptClass cc = exportGraph.getMetaData().getConceptClass(c.getOfType().getId());
            DataSource dataSource = exportGraph.getMetaData().getDataSource(c.getElementOf().getId());

            Collection<EvidenceType> evset = new ArrayList<EvidenceType>();
            for (EvidenceType ev : c.getEvidence()) {
                EvidenceType evCopy = exportGraph.getMetaData().getEvidenceType(ev.getId());
                evset.add(evCopy);
            }

            ONDEXConcept copy = exportGraph.createConcept(c.getPID(), c.getAnnotation(), c.getDescription(),dataSource, cc, evset);

            for (ConceptName cn : c.getConceptNames()) {
                copy.createConceptName(cn.getName(), cn.isPreferred());
            }
            for (ConceptAccession acc : c.getConceptAccessions()) {
                copy.createConceptAccession(acc.getAccession(), acc.getElementOf(), acc.isAmbiguous());
            }
            for (Attribute attribute : c.getAttributes()) {
                AttributeName copyAN = exportGraph.getMetaData().getAttributeName(attribute.getOfType().getId());
                copy.createAttribute(copyAN, attribute.getValue(), false);
            }

            conceptIdMap.put(c.getId(),copy.getId());
        }

        for (ONDEXRelation r : getVisibleRelations()) {

            ONDEXConcept fromCopy = exportGraph.getConcept(conceptIdMap.get(r.getFromConcept().getId()));
            ONDEXConcept toCopy = exportGraph.getConcept(conceptIdMap.get(r.getToConcept().getId()));
           
            RelationType rt = exportGraph.getMetaData().getRelationType(r.getOfType().getId());

            Collection<EvidenceType> evset = new ArrayList<EvidenceType>();
            for (EvidenceType ev : r.getEvidence()) {
                EvidenceType evCopy = exportGraph.getMetaData().getEvidenceType(ev.getId());
                evset.add(evCopy);
            }

            ONDEXRelation copy = exportGraph.createRelation(fromCopy, toCopy, rt, evset);
            
            for (Attribute attribute : r.getAttributes()) {
                AttributeName copyAN = exportGraph.getMetaData().getAttributeName(attribute.getOfType().getId());
                copy.createAttribute(copyAN, attribute.getValue(), false);
            }
        }

    }


    /* ##############################
     * ##### PLUGIN INFORMATION #####
     * ##############################
     */

    private final Logger logger = Logger.getLogger(MiTaxIdFilter.class);

    @Override
    public String getId() {
        return "mitaxid";
    }

    @Override
    public String getName() {
        return "Taxonomy ID filter for molecular interaction data";
    }

    @Override
    public String getVersion() {
        return "0.0.1";
    }
    
    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    /* ##########################
     * ##### HELPER CLASSES #####
     * ##########################
     */

    private class Neighbour {
        private ONDEXConcept concept;
        private ONDEXRelation relation;

        public Neighbour(ONDEXConcept concept, ONDEXRelation relation) {
            this.concept = concept;
            this.relation = relation;
        }

        public ONDEXConcept getConcept() {
            return concept;
        }

        public ONDEXRelation getRelation() {
            return relation;
        }
        
    }


}
