package net.sourceforge.ondex.mapping.tmbased;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Writes for one publication its text mining mapping results
 * 
 * @author keywan
 *
 */
public class MappingWriter {
	
	private RelationType rt;
	private RelationType pub_in;

	private EvidenceType eviType;

	private AttributeName eviSentence;

	private AttributeName score;
	
	private AttributeName citNum;

	private ONDEXGraph graph;
	
	private int numOfRelations = 0;


	
	public MappingWriter(ONDEXGraph graph) {
		this.graph = graph;
		
		eviType = graph.getMetaData().getEvidenceType(MetaData.ET_TEXTMINING);
		if (eviType == null) {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.ET_TEXTMINING, Mapping.getCurrentMethodName()));
		}
		
		eviSentence = graph.getMetaData().getAttributeName(MetaData.ATT_EVIDENCE);
		if (eviSentence == null) {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.ATT_EVIDENCE, Mapping.getCurrentMethodName()));
		}
		
		score = graph.getMetaData().getAttributeName(MetaData.ATT_TMSCORE);
		if (score == null) {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new AttributeNameMissingEvent(MetaData.ATT_TMSCORE, Mapping.getCurrentMethodName()));
		}
		
		citNum = graph.getMetaData().getAttributeName(MetaData.ATT_CITNUM);
		if (citNum == null) {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new AttributeNameMissingEvent(MetaData.ATT_CITNUM, Mapping.getCurrentMethodName()));
		}
		
		// get the relationtypeset and evidencetype for this mapping
		rt = graph.getMetaData().getRelationType(MetaData.RT_OCC_IN);
		if (rt == null) {
			RelationType rt = graph.getMetaData().getRelationType(MetaData.RT_OCC_IN);
			if (rt == null) {
				ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new RelationTypeMissingEvent(MetaData.RT_OCC_IN, Mapping.getCurrentMethodName()));
			}
			rt = graph.getMetaData().getFactory().createRelationType(MetaData.RT_OCC_IN, rt);
		}
		
		pub_in = graph.getMetaData().getRelationType(MetaData.publishedIn);
		
		if(pub_in == null){
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new RelationTypeMissingEvent("pub_in", Mapping.getCurrentMethodName()));
		}
		

	}

	
	/**
	 *  Add text mining based relations between publication and other concepts
	 *  
	 * @param pubMap a publication with all its hits
	 * 
	 */
	public void addHitsToONDEXGraph(PublicationMapping pubMap) {			
		
		Integer pubID = pubMap.getPublicationID();
		HashMap<String, ArrayList<Hit>> results = pubMap.getHits();
		
		for(String cc : results.keySet()){
            for (Hit hit : results.get(cc)) {
                Integer hitID = hit.getHitConID();
                ONDEXConcept pubCon = graph.getConcept(pubID);
                ONDEXConcept hitCon = graph.getConcept(hitID);
                ONDEXRelation relation = graph.getRelation(hitCon, pubCon, rt);

                // try if relation was already created
                if (relation == null) {
                    // create a new relation between the two concepts
                    relation = graph.getFactory().createRelation(hitCon, pubCon, rt, eviType);
//					pubCon.addContext(s, pubCon);
//					hitCon.addContext(s, pubCon);
                    relation.createAttribute(eviSentence, hit.getEvidence(), false);
                    relation.createAttribute(score, hit.getScore(), false);
                    this.numOfRelations++;
                }
                else {
//					System.out.println("TM relation already exists: " + pubID + " --> " + hitID);
                    this.numOfRelations++;

                }
            }
		}
	}
	
	public void addAllHitsToONDEXGraph(Collection<PublicationMapping> pubMaps) {
        for (PublicationMapping pubMap : pubMaps) {
            addHitsToONDEXGraph(pubMap);
        }
		
		System.out.println("Add number of publications a concept is cited in...");
		addCitationCountGDS();
		
		System.out.println("Created text mining relations: "+ getNumOfRelations());

		
	}
	
	public void addCitationCountGDS(){
		//get all publications
		ConceptClass ccPub = graph.getMetaData().getConceptClass(MetaData.CC_PUBLICATION);

		//iterate over and get all relations for each publication
		for (ONDEXConcept conPub : graph.getConceptsOfConceptClass(ccPub)) {
			ONDEXConcept con = null;
			for (ONDEXRelation rel : graph.getRelationsOfConcept(conPub)) {
				//if relation is "occ_in" or "pub_in" get from concept
				if((rel.getOfType().equals(rt) || rel.getOfType().equals(pub_in)) 
						&& !rel.getFromConcept().equals(con)){
					con = rel.getFromConcept();
					//increase its CitNum
					increaseCitCount(con);
				}
				else{
//					System.out.println(con.getPID() + " was already cited in " + conPub.getPID());
				}
			}
		}
	}
	
	public void increaseCitCount(ONDEXConcept con){
		//count how many times a concept is cited
		if (con.getAttribute(citNum) == null){
			con.createAttribute(citNum, 1, false);
		}else{
			Integer value = (Integer) con.getAttribute(citNum).getValue();
			value++;
			con.getAttribute(citNum).setValue(value);
		}
	}

	public int getNumOfRelations() {
		return numOfRelations;
	}

}
