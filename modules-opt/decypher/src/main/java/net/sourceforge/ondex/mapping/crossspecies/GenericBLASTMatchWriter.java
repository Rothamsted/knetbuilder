package net.sourceforge.ondex.mapping.crossspecies;

import java.util.Iterator;
import java.util.List;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.programcalls.Match;

/**
 * 
 * @author hindlem
 *
 */
public class GenericBLASTMatchWriter {

	private RelationType rtSet;

	private EvidenceType eviType;

	private AttributeName eValue;
	private AttributeName bitScore;
	private AttributeName coverage;
	private AttributeName overlap;
	private AttributeName frame;
	private AttributeName query_length;
	private AttributeName target_length;
	
	private ONDEXGraph graph;


	
	public GenericBLASTMatchWriter(ONDEXGraph graph) {
		this.graph = graph;
		
		eviType = graph.getMetaData().getEvidenceType(MetaData.EVIDENCE_BLAST);
		if (eviType == null) {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.EVIDENCE_BLAST, Mapping.getCurrentMethodName()));
		}
		
		eValue = graph.getMetaData().getAttributeName(MetaData.ATT_E_VALUE);
		if (eValue == null) {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new AttributeNameMissingEvent(MetaData.ATT_E_VALUE, Mapping.getCurrentMethodName()));
		}
		
		bitScore = graph.getMetaData().getAttributeName(MetaData.ATT_BITSCORE);
		if (bitScore == null) {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new AttributeNameMissingEvent(MetaData.ATT_BITSCORE, Mapping.getCurrentMethodName()));
		}
		
		coverage = graph.getMetaData().getAttributeName(MetaData.ATT_COVERAGE);
		if (coverage == null) {
			coverage = graph.getMetaData().getFactory().createAttributeName(MetaData.ATT_COVERAGE, Double.class);
		}
		// get the relationtypeset and evidencetype for this mapping
		rtSet = graph.getMetaData().getRelationType(MetaData.RT_HAS_SIMILER_SEQUENCE);
		if (rtSet == null) {
			rtSet = graph.getMetaData().getFactory().createRelationType(MetaData.RT_HAS_SIMILER_SEQUENCE);
		}
		
		overlap = graph.getMetaData().getAttributeName(MetaData.ATT_OVERLAP);
		if (overlap == null) {
			overlap = graph.getMetaData().getFactory().createAttributeName(MetaData.ATT_OVERLAP, "Alignment overlap",Integer.class);
		}
		
		frame = graph.getMetaData().getAttributeName(MetaData.ATT_FRAME);
		if (frame == null) {
			frame = graph.getMetaData().getFactory().createAttributeName(MetaData.ATT_FRAME, "Translation fame",Integer.class);
		}
		
		query_length = graph.getMetaData().getAttributeName(MetaData.ATT_QUERY_LENGTH);
		if (query_length == null) {
			query_length = graph.getMetaData().getFactory().createAttributeName(MetaData.ATT_QUERY_LENGTH, "Length of query sequence", Integer.class);
		}
		
		target_length = graph.getMetaData().getAttributeName(MetaData.ATT_TARGET_LENGTH);
		if (target_length == null) {
			target_length = graph.getMetaData().getFactory().createAttributeName(MetaData.ATT_TARGET_LENGTH, "Length of target sequence", Integer.class);
		}
		
	}

	/**
	 *  Adds relations for hits as long as its not a self match
	 * @param graph an ondex graph
	 * @param result the matches to the original query sequence
	 * @param fromConcept the original query sequence
	 */
	public void addHitsToONDEXGraph(List<Match> result,
			ONDEXConcept fromConcept) {			
		
		Iterator<Match> hitIt = result.iterator();
		
		while (hitIt.hasNext()){
			Match hit = hitIt.next();
			if (fromConcept.getId() != hit.getTargetId()){
				ONDEXConcept toConcept = graph.getConcept(hit.getTargetId());
				ONDEXRelation relation = graph.getRelation(fromConcept,toConcept,rtSet);
				// try if relation was already created
				if (relation == null) {
					// create a new relation between the two concepts
					relation = graph.getFactory().createRelation(fromConcept,toConcept,rtSet,eviType);
					
					relation.createAttribute(eValue, Double.valueOf(hit.getEValue()), false);
					relation.createAttribute(bitScore, Double.valueOf(hit.getScore()), false);
					
					relation.createAttribute(overlap, Integer.valueOf(hit.getOverlapingLength()), false);
					relation.createAttribute(frame, Integer.valueOf(hit.getQueryFrame()), false);
					relation.createAttribute(query_length, Integer.valueOf(hit.getLengthOfQuerySequence()), false);
					relation.createAttribute(target_length, Integer.valueOf(hit.getLengthOfTargetSequence()), false);
					
					relation.createAttribute(coverage, Double.valueOf(hit.geQueryCoverageSequence()), false);
					
				}
			} else {
				System.out.println("Excluding self match");
			}
		}
	}
	
}
