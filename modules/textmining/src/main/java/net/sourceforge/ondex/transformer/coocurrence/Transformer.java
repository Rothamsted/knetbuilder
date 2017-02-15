package net.sourceforge.ondex.transformer.coocurrence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.exception.type.WrongParameterException;
import net.sourceforge.ondex.mapping.tmbased.Mapping;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

/**
 * This transformer is used to transform text mining results
 * into a co-occurrence network where concepts get connected
 * if they were linked to the same publication
 * 
 * A-Publication-B --> A-B
 * 
 * It can deal with text mining scores on relations and combines
 * them onto the newly created relations
 *
 * @author keywan, taubertj
 * @version 31.01.2008
 * 
 * 
 */
@Status(description = "Transforms text mining results into a weighted co-occurrence network (Hassani-Pak et al, JIB 2010). Tested June 2011 (Keywan Hassani-Pak)", status = StatusType.STABLE)
@Authors(authors = { "Keywan Hassani-Pak" }, emails = { "keywan at users.sourceforge.net" })
@Custodians(custodians = { "Keywan Hassani-Pak" }, emails = { "keywan at users.sourceforge.net" })

public class Transformer extends ONDEXTransformer implements
        ArgumentNames {
	
    private RelationType rtCoocWith;
    private EvidenceType etTRANS;
    private AttributeName attIP_TFIDF;
    private AttributeName attMAX_TFIDF;
    private AttributeName attTFIDF;
    private AttributeName attNumPubl;
    private AttributeName attNumSent;
    private AttributeName attEvidence;
    private AttributeName attPMID;
    private ConceptClass ccPublication;

    /**
     * Simple Tuple implementation.
     *
     * @author taubertj
     * @param <A>
     * @param <B>
     */
    public class Tuple<A, B> {

        private A first;
        private B second;

        public Tuple(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public A getFirst() {
            return first;
        }

        public B getSecond() {
            return second;
        }
    }

    /**
     * Starts processing of data.
     *
     * @throws EvidenceTypeMissingException
     */
    public void start() throws InvalidPluginArgumentException {

    	ONDEXGraphMetaData md = graph.getMetaData();
    	
		rtCoocWith = md.getRelationType(MetaData.RT_COOC_WITH);
		if (rtCoocWith == null) {
			fireEventOccurred(new RelationTypeMissingEvent(MetaData.RT_COOC_WITH, Mapping.getCurrentMethodName()));
		}
		
        etTRANS = md.getEvidenceType(MetaData.ET_TRANS);
        if (etTRANS == null) {
            fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.ET_TRANS, Mapping.getCurrentMethodName()));
        }
    	
    	attEvidence = md.getAttributeName(MetaData.ATT_EVIDENCE);
		if (attEvidence == null) {
			fireEventOccurred(new AttributeNameMissingEvent(MetaData.ATT_EVIDENCE, Mapping.getCurrentMethodName()));
		}
		
        attTFIDF = md.getAttributeName(MetaData.ATT_TFIDF);
        if (attTFIDF == null) {
        	fireEventOccurred(new AttributeNameMissingEvent(MetaData.ATT_TFIDF, Mapping.getCurrentMethodName()));
        }

        attPMID = graph.getMetaData().getAttributeName(MetaData.ATT_PMID);
        if (attPMID == null) {
        	fireEventOccurred(new AttributeNameMissingEvent(MetaData.ATT_PMID, Mapping.getCurrentMethodName()));
        }
        
        attNumPubl = graph.getMetaData().getAttributeName(MetaData.ATT_NUM_PUBLICATIONS);
        if (attNumPubl == null) {
        	fireEventOccurred(new AttributeNameMissingEvent(MetaData.ATT_NUM_PUBLICATIONS, Mapping.getCurrentMethodName()));
        }
        
        attNumSent = graph.getMetaData().getAttributeName(MetaData.ATT_NUM_SENTENCES);
        if (attNumSent == null) {
        	fireEventOccurred(new AttributeNameMissingEvent(MetaData.ATT_NUM_SENTENCES, Mapping.getCurrentMethodName()));
        }
        
        attMAX_TFIDF = md.getAttributeName(MetaData.attMAX_TFIDF);
        if (attMAX_TFIDF == null) {
        	fireEventOccurred(new AttributeNameMissingEvent(MetaData.attMAX_TFIDF, Mapping.getCurrentMethodName()));
        }
        attIP_TFIDF = md.getAttributeName(MetaData.attIP_TFIDF);
        if (attIP_TFIDF == null) {
        	fireEventOccurred(new AttributeNameMissingEvent(MetaData.attIP_TFIDF, Mapping.getCurrentMethodName()));
        }
        
        // which concept class for qualifier
        ccPublication = md.getConceptClass((String) args.getUniqueValue(QUALIFIER_CLASS_ARG));
        if (ccPublication == null) {
            fireEventOccurred(new WrongParameterEvent(args.getUniqueValue(QUALIFIER_CLASS_ARG)
                    + " is not a valid ConceptClass.", "[Transformer - setONDEXGraph]"));
            throw new WrongParameterException(args.getUniqueValue(QUALIFIER_CLASS_ARG)
                    + " is not a valid ConceptClass.");
        }
        
        //all relations to, from Publication
        Set<ONDEXRelation> relations = BitSetFunctions.copy(graph.getRelationsOfConceptClass(ccPublication));
        
        System.out.println("Relations for co-occurrence before filtering: " + relations.size());
        
        // only consider publications with less than N pub_in or occ_in relations
        for(ONDEXConcept c : graph.getConceptsOfConceptClass(ccPublication)){
        	Set<ONDEXRelation> rels = graph.getRelationsOfConcept(c);
        	int countPubIn = 0;
        	for(ONDEXRelation r : rels){
        		if(r.getOfType().getId().equals("pub_in")){
        			countPubIn++;
        		}
        	}
        	if(countPubIn >= 10){
        		relations.removeAll(rels);
        	}
        }
        
        // get relations of qualifier concept class
        System.out.println("Relations for co-occurrence after filtering publications with more than 10 citations: " + relations.size());
        
        //relation id -> set of evidence sentences
    	HashMap<Integer, HashSet<String>> relation2evitext = new HashMap<Integer, HashSet<String>>();

    	//publications -> concept, relation
        Map<ONDEXConcept, List<Tuple<ONDEXConcept, ONDEXRelation>>> coocurrences = new Hashtable<ONDEXConcept, List<Tuple<ONDEXConcept, ONDEXRelation>>>();

        // iterate over all relations and store in a data structure
        for (ONDEXRelation r : relations) {
            ONDEXConcept from = r.getFromConcept();
            ONDEXConcept to = r.getToConcept();

            // re-index according to qualifier
            if (from.getOfType().equals(ccPublication)) {
                if (!coocurrences.containsKey(from))
                    coocurrences
                            .put(
                                    from,
                                    new ArrayList<Tuple<ONDEXConcept, ONDEXRelation>>());
                coocurrences.get(from).add(
                        new Tuple<ONDEXConcept, ONDEXRelation>(to, r));
            } else {
                if (!coocurrences.containsKey(to))
                    coocurrences
                            .put(
                                    to,
                                    new ArrayList<Tuple<ONDEXConcept, ONDEXRelation>>());
                coocurrences.get(to).add(
                        new Tuple<ONDEXConcept, ONDEXRelation>(from, r));
            }
        }
        
        int countRelations = 0;
        // iterate over future qualifier concepts
        for (ONDEXConcept qual : coocurrences.keySet()) {
            // create all transitive relationships
        	HashSet<String> relCreatedAndUpdated = new HashSet<String>();
        	
        	for (Tuple<ONDEXConcept, ONDEXRelation> tuple : coocurrences.get(qual))
            {
                // get first part
                ONDEXConcept from = tuple.getFirst();
                ONDEXRelation fromRel = tuple.getSecond();
                
                // quadratic run time, but nothing we can do...
                for (Tuple<ONDEXConcept, ONDEXRelation> tuple2 : coocurrences.get(qual))
                {
                    // get second part
                    ONDEXConcept to = tuple2.getFirst();
                    ONDEXRelation toRel = tuple2.getSecond();
                    
                    // no self loops, no self relations, no finished relations
                    String key = fromRel.getId()+"_"+toRel.getId();
                    if (to.equals(from) || toRel.equals(fromRel) || relCreatedAndUpdated.contains(key)){
                    	continue;
                    }
                    
                    // e.g. As comparing of relation A and B is summetric,
                    // mark both A_B and B_A as processed and finished
                	String key1 = fromRel.getId()+"_"+toRel.getId();
                	String key2 = toRel.getId()+"_"+fromRel.getId();
            		relCreatedAndUpdated.add(key1);
            		relCreatedAndUpdated.add(key2);
                    
                    //to make sure all relations between cocited concepts are
                    //in the same direction
                    if (graph.getRelation(to, from, rtCoocWith) != null) {
                        ONDEXConcept temp = to;
                        to = from;
                        from = temp;
                    }
                    
                    if(relation2evitext.get(fromRel.getId()) == null){
    	                relation2evitext.put(fromRel.getId(), getEvidenceText(fromRel));
                    }
                    HashSet<String> eviText1 = relation2evitext.get(fromRel.getId());
                    
                    if(relation2evitext.get(toRel.getId()) == null){
    	                relation2evitext.put(toRel.getId(), getEvidenceText(toRel));
                    }
                    HashSet<String> eviText2 = relation2evitext.get(toRel.getId());
                    
                    HashSet<String> coocSentence = coOccurreOnSentenceLevel(eviText1, eviText2);
                    

                    // calculate new score
                    double fromScore = 1.0;
                    double toScore = 1.0;

                    //if it is a manually curated relation "pub_in"
//                    if (fromRel.getOfType().getId().equals(MetaData.RT_PUBIN)) {
//                        fromScore = 2.0;
//                    }
//                    if (toRel.getOfType().getId().equals(MetaData.RT_PUBIN)) {
//                        toScore = 2.0;
//                    }

                    //if relation has a TM_SCORE use that
                    if (fromRel.getAttribute(attTFIDF) != null) {
                        fromScore = (Double) fromRel.getAttribute(attTFIDF).getValue();
                    }
                    if (toRel.getAttribute(attTFIDF) != null) {
                        toScore = (Double) toRel.getAttribute(attTFIDF).getValue();
                    }
                    double score1 = (fromScore * toScore);

                    //add PMID as a Attribute of the co-cited relation
                    String valuePMID = null;
                    if (attPMID != null) {
                        for (ConceptAccession acc : qual.getConceptAccessions()) {
                            if (acc.getElementOf().getId().equals(MetaData.CV_NLM)) {
                                valuePMID = acc.getAccession();
                            }
                        }
                    }

                    if(graph.getRelation(from, to, rtCoocWith) == null){
                    	countRelations++;
                    	// create new transitive relation with a qualifier
                        ONDEXRelation r = graph.getFactory().createRelation(from, to,
                                rtCoocWith, etTRANS);

//                        r.createAttribute(anTmScore, score1, false);
                        if (valuePMID != null) {
                            r.createAttribute(attPMID, valuePMID, false);
                        }
                        
                        r.createAttribute(attEvidence, coocSentence, false);
                        r.createAttribute(attNumSent, coocSentence.size(), false);
                        r.createAttribute(attNumPubl, 1, false);
                    	r.createAttribute(attMAX_TFIDF, score1, false);
                    	r.createAttribute(attIP_TFIDF, score1, false);
                    	
                    	//tag concept and relation with publication
                    	from.addTag(qual);
                    	to.addTag(qual);
                    	r.addTag(qual);
                        
                    }
                    else{
                		ONDEXRelation existRel = graph.getRelation(from, to, rtCoocWith);
                		
                		//merging of pub_in with occ_in relations used to be a special case
//                    	double oldScore = (Double) existRel.getAttribute(anTmScore).getValue();
//                    	if(score1 > oldScore){
//                    		existRel.createAttribute(anTmScore, score1, false);
//                    	}	
//                    	if(coocSentence.size() > 0){
//	                        existRel.createAttribute(eviSentence, coocSentence, false);
//	                        existRel.createAttribute(anNumSent, coocSentence.size(), false);
//                    	}
                    	
                        
                        double ip_old = (Double) existRel.getAttribute(attIP_TFIDF).getValue();
                        double max_old = (Double) existRel.getAttribute(attMAX_TFIDF).getValue();

                        double ip = score1 + ip_old;
                        existRel.getAttribute(attIP_TFIDF).setValue(ip);
                        
                        if(score1 > max_old){
                        	existRel.getAttribute(attMAX_TFIDF).setValue(score1);
                        }
                        
                        // tag association with publication
                    	from.addTag(qual);
                    	to.addTag(qual);
                    	existRel.addTag(qual);
                        
                        // concatenate PMIDs
                        if (valuePMID != null) {
                            if(existRel.getAttribute(attPMID) != null){
                            	String b = existRel.getAttribute(attPMID).getValue().toString();
        	                    String concat = valuePMID + ", " + b;
        	                    existRel.getAttribute(attPMID).setValue(concat);
                            }else{
                            	existRel.createAttribute(attPMID, valuePMID, false);
                            }
                        }
                        
                        // count number of co-citation
                        Integer num = (Integer) existRel.getAttribute(attNumPubl).getValue();
                        num++;
                        existRel.getAttribute(attNumPubl).setValue(num);
                        
                        
                        // evidence sentences
                    	HashSet<String> setB = (HashSet<String>) existRel.getAttribute(attEvidence).getValue();
                    	HashSet<String> setC = new HashSet<String>();
                    	setC.addAll(coocSentence);
                    	setC.addAll(setB);
                    	existRel.getAttribute(attEvidence).setValue(setC);
                        // count number of evidence sentences
                    	existRel.getAttribute(attNumSent).setValue(setC.size());
                        
                    }
                    
                }
            }
        	
        	//next publication
        }

        System.out.println("New relations created: " + countRelations);
    }

    /**
     * Returns name of this transformer.
     *
     * @return String
     */
    public String getName() {
        return "Co-occurrence";
    }

    /**
     * Returns version of this transformer.
     *
     * @return String
     */
    public String getVersion() {
        return "20.10.08";
    }

    @Override
    public String getId() {
        return "coocurrence";
    }

    /**
     * Returns arguments required by this transformer.
     *
     * @return ArgumentDefinition<?>[]
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition[]{
                new StringArgumentDefinition(ArgumentNames.QUALIFIER_CLASS_ARG,
                        ArgumentNames.QUALIFIER_CLASS_ARG_DESC, true, "Publication" ,false),
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
    
    public HashSet<String> getEvidenceText(ONDEXRelation r){
    	
		AttributeName eviSentence = graph.getMetaData().getAttributeName(MetaData.ATT_EVIDENCE);
		HashSet<String> evidence = new HashSet<String>();
		if (eviSentence == null) {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.ATT_EVIDENCE, Mapping.getCurrentMethodName()));
		}
    	if(r.getAttribute(eviSentence) != null)
    		evidence = (HashSet<String>) r.getAttribute(eviSentence).getValue();

    	return evidence;
    }
    
    public HashSet<String> coOccurreOnSentenceLevel(Set<String> set1, Set<String> set2){
    	
    	HashSet<String> sentences = new HashSet<String>();
    	for(String s1 : set1){
			if(set2.contains(s1)){
//				System.out.println("On Sentence Level: "+s1);
				sentences.add(s1);
			}	
    	}
    	return sentences;
    }

}
