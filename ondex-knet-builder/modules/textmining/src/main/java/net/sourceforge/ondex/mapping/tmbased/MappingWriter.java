package net.sourceforge.ondex.mapping.tmbased;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;

/**
 * Writes for one publication its text mining mapping results
 * 
 * @author keywan
 *
 */
public class MappingWriter {
	
	private RelationType occInRelType;
	private RelationType pubInRelType;

	private EvidenceType textMiningEvidence;

	private AttributeName evidenceSentenceAttrName;

	private AttributeName scoreAttrName;
	
	private AttributeName citNumAttrName;

	private ONDEXGraph graph;
	
	private int numOfRelations = 0;


	
	public MappingWriter(ONDEXGraph graph) {
		this.graph = graph;
		
		textMiningEvidence = graph.getMetaData().getEvidenceType(MetaData.ET_TEXTMINING);
		if (textMiningEvidence == null) {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.ET_TEXTMINING, Mapping.getCurrentMethodName()));
		}
		
		evidenceSentenceAttrName = graph.getMetaData().getAttributeName(MetaData.ATT_EVIDENCE);
		if (evidenceSentenceAttrName == null) {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.ATT_EVIDENCE, Mapping.getCurrentMethodName()));
		}
		
		scoreAttrName = graph.getMetaData().getAttributeName(MetaData.ATT_TMSCORE);
		if (scoreAttrName == null) {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new AttributeNameMissingEvent(MetaData.ATT_TMSCORE, Mapping.getCurrentMethodName()));
		}
		
		citNumAttrName = graph.getMetaData().getAttributeName(MetaData.ATT_CITNUM);
		if (citNumAttrName == null) {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new AttributeNameMissingEvent(MetaData.ATT_CITNUM, Mapping.getCurrentMethodName()));
		}
		
		// get the relationtypeset and evidencetype for this mapping
		// TODO: this mess cannot be right, the second assignment is the same and the last one
		// doesn't make sense.
		// The cleaner version that returns the same should be like below. This is to be removed
		//
//		occInRelType = graph.getMetaData().getRelationType(MetaData.RT_OCC_IN);
//		if (occInRelType == null) {
//			RelationType occInRelType = graph.getMetaData().getRelationType(MetaData.RT_OCC_IN);
//			if (occInRelType == null) {
//				ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new RelationTypeMissingEvent(MetaData.RT_OCC_IN, Mapping.getCurrentMethodName()));
//			}
//			occInRelType = graph.getMetaData().getFactory().createRelationType(MetaData.RT_OCC_IN, occInRelType);
//		}
		
		occInRelType = Optional.ofNullable ( graph.getMetaData().getRelationType(MetaData.RT_OCC_IN) )
			.orElseGet ( () -> {
				ONDEXEventHandler.getEventHandlerForSID ( graph.getSID() )
					.fireEventOccurred ( new RelationTypeMissingEvent( MetaData.RT_OCC_IN, Mapping.getCurrentMethodName() ) );
				return graph.getMetaData().getFactory().createRelationType ( MetaData.RT_OCC_IN, (RelationType) null );
			});
		
		
		pubInRelType = graph.getMetaData().getRelationType(MetaData.publishedIn);
		
		if(pubInRelType == null){
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new RelationTypeMissingEvent("pubInRelType", Mapping.getCurrentMethodName()));
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
                ONDEXRelation relation = graph.getRelation(hitCon, pubCon, occInRelType);

                // try if relation was already created
                if (relation == null) {
                    // create a new relation between the two concepts
                    relation = graph.getFactory().createRelation(hitCon, pubCon, occInRelType, textMiningEvidence);
//					pubCon.addContext(s, pubCon);
//					hitCon.addContext(s, pubCon);
                    relation.createAttribute(evidenceSentenceAttrName, hit.getEvidence(), false);
                    relation.createAttribute(scoreAttrName, hit.getScore(), false);
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
		// addCitationCountGDS();
		
		System.out.println("Created text mining relations: "+ getNumOfRelations());

		
	}
	
	public void addCitationCountGDS(){
		//get all publications
		ConceptClass ccPub = graph.getMetaData().getConceptClass(MetaData.CC_PUBLICATION);

		//iterate over and get all relations for each publication
		for (ONDEXConcept conPub : graph.getConceptsOfConceptClass(ccPub)) {
			ONDEXConcept con = null;
			for (ONDEXRelation rel : graph.getRelationsOfConcept(conPub)) {
				//if relation is "occ_in" or "pubInRelType" get from concept
				if((rel.getOfType().equals(occInRelType) || rel.getOfType().equals(pubInRelType)) 
						&& !rel.getFromConcept().equals(con)){
					con = rel.getFromConcept();
					//increase its CitNum
					//increaseCitCount(con);
				}
				else{
//					System.out.println(con.getPID() + " was already cited in " + conPub.getPID());
				}
			}
		}
	}
	
	public void increaseCitCount(ONDEXConcept con){
		//count how many times a concept is cited
		if (con.getAttribute(citNumAttrName) == null){
			con.createAttribute(citNumAttrName, 1, false);
		}else{
			Integer value = (Integer) con.getAttribute(citNumAttrName).getValue();
			value++;
			con.getAttribute(citNumAttrName).setValue(value);
		}
	}

	public int getNumOfRelations() {
		return numOfRelations;
	}

}
