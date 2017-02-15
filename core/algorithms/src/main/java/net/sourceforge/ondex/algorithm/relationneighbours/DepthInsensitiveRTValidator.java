package net.sourceforge.ondex.algorithm.relationneighbours;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.ONDEXRelation;

/**
 * A validator for registering valid relation types/ or type sets that will be
 * valid at any depth If neither RelationType or AbstractRelationTypeSet
 * restrictions are added then no Relation will be valid. Concept based
 * validation works the oposite way in that all Concepts are valid if neither CC
 * or DataSource are set. If only one (CC or DataSource) is set then the unset one is deemed
 * flexible.
 * 
 * e.g. if RT = "equ" and CCFrom = "Protein" and CCTo = "Protein" then only
 * "equ" relations between "Proteins" are valid but these can be from any DataSource
 * 
 * @author hindlem
 * 
 */
public class DepthInsensitiveRTValidator implements LogicalRelationValidator {

	private HashSet<String> incomingRelationTypeValidAtAnyDepth;

	private HashSet<String> outgoingRelationTypeValidAtAnyDepth;

	private HashMap<String, Set<String>> validConceptClassPair;

	private HashMap<String, Set<String>> validDataSourcePair;

	/**
	 * Add ConceptClass restriction valid at any depth for the to Concept If no
	 * ConceptClass restriction are set then any ConceptClass is valid
	 * 
	 * @param fromCC
	 *            the from ConceptClass that will be valid
	 * @param toCC
	 *            the to ConceptClass that will be valid
	 */
	public void addConceptClassPair(ConceptClass fromCC, ConceptClass toCC) {
		if (validConceptClassPair == null) {
			validConceptClassPair = new HashMap<String, Set<String>>(1);
		}
		Set<String> validToCCs = validConceptClassPair.get(fromCC.getId());
		if (validToCCs == null) {
			validToCCs = new HashSet<String>(1);
			validConceptClassPair.put(fromCC.getId(), validToCCs);
		}
		validToCCs.add(toCC.getId());
	}

	/**
	 * Add DataSource restriction valid at any depth for the to Concept If no DataSource
	 * restriction are set then any DataSource is valid
	 * 
	 * @param fromDataSource
	 *            the fromDataSource that will be valid
	 * @param toDataSource
	 *            the toDataSource that will be valid
	 */
	public void addDataSourcePair(DataSource fromDataSource, DataSource toDataSource) {
		if (validDataSourcePair == null) {
			validDataSourcePair = new HashMap<String, Set<String>>(1);
		}
		Set<String> validToDataSources = validDataSourcePair.get(fromDataSource.getId());
		if (validToDataSources == null) {
			validToDataSources = new HashSet<String>(1);
			validDataSourcePair.put(fromDataSource.getId(), validToDataSources);
		}
		validToDataSources.add(toDataSource.getId());
	}

	/**
	 * Add RelationType restriction valid at any depth in an incoming direction
	 * (relative to seed)
	 * 
	 * @param rts
	 *            the relation type set
	 */
	public void addIncomingRelationType(RelationType rts) {
		if (incomingRelationTypeValidAtAnyDepth == null) {
			incomingRelationTypeValidAtAnyDepth = new HashSet<String>();
		}
		incomingRelationTypeValidAtAnyDepth.add(rts.getId());
	}

	/**
	 * Add RelationType restriction valid at any depth in an outgoing direction
	 * (relative to seed)
	 * 
	 * @param rts
	 *            the relation type set
	 */
	public void addOutgoingRelationType(RelationType rts) {
		if (outgoingRelationTypeValidAtAnyDepth == null) {
			outgoingRelationTypeValidAtAnyDepth = new HashSet<String>();
		}
		outgoingRelationTypeValidAtAnyDepth.add(rts.getId());
	}

	/**
	 * Add Relation Type restriction valid at any depth in any direction
	 * 
	 * @param rt
	 *            the relation type
	 */
	public void addRelationType(RelationType rt) {
		addIncomingRelationType(rt);
		addOutgoingRelationType(rt);
	}

	public boolean isValidRelationAtDepth(ONDEXRelation relation,
			int currentPosition, ONDEXConcept conceptAtHead) {

		if (validConceptClassPair != null || validDataSourcePair != null) {
			ONDEXConcept fromConcept = relation.getFromConcept();
			ONDEXConcept toConcept = relation.getToConcept();

			if (validDataSourcePair != null) {
				Set<String> toDataSources = validDataSourcePair.get(fromConcept.getElementOf()
						.getId());
				if (toDataSources == null
						|| !toDataSources.contains(toConcept.getElementOf().getId())) {
					return false;
				}
			}

			if (validConceptClassPair != null) {
				Set<String> toCCs = validConceptClassPair.get(fromConcept
						.getOfType().getId());
				if (toCCs == null
						|| !toCCs.contains(toConcept.getOfType().getId())) {
					return false;
				}
			}

		}

		int headId = conceptAtHead.getId();
		int fromID = relation.getKey().getFromID();
		int toId = relation.getKey().getToID();

		if (outgoingRelationTypeValidAtAnyDepth != null
				|| incomingRelationTypeValidAtAnyDepth != null) {

			String rtId = relation.getKey().getRtId();
			if (outgoingRelationTypeValidAtAnyDepth != null && headId == fromID
					&& outgoingRelationTypeValidAtAnyDepth.contains(rtId)) {
				return true;
			} else if (incomingRelationTypeValidAtAnyDepth != null
					&& headId == toId
					&& incomingRelationTypeValidAtAnyDepth.contains(rtId)) {
				return true;
			}

		}
		return false;
	}

}
