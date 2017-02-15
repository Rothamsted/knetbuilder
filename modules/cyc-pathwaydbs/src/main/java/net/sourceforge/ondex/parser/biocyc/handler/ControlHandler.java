package net.sourceforge.ondex.parser.biocyc.handler;

import java.util.Set;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.exception.type.RelationTypeMissingException;

import org.biopax.paxtools.model.level2.ControlType;
import org.biopax.paxtools.model.level2.control;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.process;

/**
 * Translates control entities into relationships.
 * 
 * @author taubertj
 * 
 */
public class ControlHandler extends DefaultHandler {

	/**
	 * Processes set of control and creates relations in given graph.
	 * 
	 * @param g
	 *            ONDEXGraph to create concepts in
	 * @param control
	 *            Set of control
	 */
	public ControlHandler(ONDEXGraph g, Set<control> control) throws Exception {
		this.graph = g;
		for (control con : control)
			processControl(con);
	}

	/**
	 * Translate given control into relations.
	 * 
	 * @param con
	 *            control to translate
	 */
	private void processControl(control con) throws Exception {

		// check for evidence type
		EvidenceType evidence = graph.getMetaData().getEvidenceType(etIMPD);
		if (evidence == null)
			throw new EvidenceTypeMissingException(etIMPD + " is missing.");

		AttributeName an = graph.getMetaData().getAttributeName(
				anClassification);
		if (an == null)
			throw new AttributeNameMissingException(anClassification
					+ " is missing.");

		// decided what kind of control this is
		RelationType rt = null;
		ControlType type = con.getCONTROL_TYPE();
		if (type == null) {
			// an empty control type seems to be synonymous to catalysis
			return;
		} else if (type.equals(ControlType.ACTIVATION)
				|| type.equals(ControlType.ACTIVATION_ALLOSTERIC)
				|| type.equals(ControlType.ACTIVATION_NONALLOSTERIC)
				|| type.equals(ControlType.ACTIVATION_UNKMECH)) {
			rt = graph.getMetaData().getRelationType(rtActivatedBy);
			if (rt == null)
				throw new RelationTypeMissingException(rtActivatedBy
						+ " is missing.");
		} else if (type.equals(ControlType.INHIBITION)
				|| type.equals(ControlType.INHIBITION_ALLOSTERIC)
				|| type.equals(ControlType.INHIBITION_COMPETITIVE)
				|| type.equals(ControlType.INHIBITION_IRREVERSIBLE)
				|| type.equals(ControlType.INHIBITION_NONCOMPETITIVE)
				|| type.equals(ControlType.INHIBITION_OTHER)
				|| type.equals(ControlType.INHIBITION_UNCOMPETITIVE)
				|| type.equals(ControlType.INHIBITION_UNKMECH)) {
			rt = graph.getMetaData().getRelationType(rtInhibitedBy);
			if (rt == null)
				throw new RelationTypeMissingException(rtInhibitedBy
						+ " is missing.");
		} else {
			System.err.println("Unknown control type for control: "
					+ con.getRDFId());
			return;
		}

		// co-enzymes activation / inhibition of enzymes
		for (physicalEntityParticipant controller : con.getCONTROLLER()) {

			// this is usually the co-enzyme
			physicalEntity entity = controller.getPHYSICAL_ENTITY();
			String rdfID = entity.getRDFId();
			ONDEXConcept c = rdf2Concept.get(rdfID);
			if (c == null) {
				System.err.println("Missing concept " + rdfID);
			} else {

				// relation between enzyme and co-enzyme
				for (process process : con.getCONTROLLED()) {
					ONDEXConcept enzyme = rdf2Concept.get(process.getRDFId());
					if (enzyme == null)
						System.err.println("Missing concept "
								+ process.getRDFId());
					else {
						// here is a systematic difference between ONDEX and
						// BioPax, BioPax treats control per pathwayStep,
						// ONDEX only in general across all pathways
						if (graph.getRelation(enzyme, c, rt) == null) {
							// create inhibition or activation relationship
							ONDEXRelation r = graph.getFactory()
									.createRelation(enzyme, c, rt, evidence);
							if (type != null)
								r.createAttribute(an, type.name(), false);
						}
					}
				}
			}
		}

	}

}
