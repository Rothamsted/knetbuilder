package net.sourceforge.ondex.parser.biocyc.handler;

import java.util.Set;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.exception.type.RelationTypeMissingException;

import org.biopax.paxtools.model.level2.ControlType;
import org.biopax.paxtools.model.level2.biochemicalReaction;
import org.biopax.paxtools.model.level2.catalysis;
import org.biopax.paxtools.model.level2.control;
import org.biopax.paxtools.model.level2.modulation;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.pathwayComponent;
import org.biopax.paxtools.model.level2.pathwayStep;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.process;
import org.biopax.paxtools.model.level2.transport;

/**
 * Uses pathways to added context accordingly.
 * 
 * @author taubertj
 * 
 */
public class ContextHandler extends DefaultHandler {

	/**
	 * Processes set of pathways and creates context in given graph.
	 * 
	 * @param g
	 *            ONDEXGraph to create context in
	 * @param pathways
	 *            Set of pathways
	 */
	public ContextHandler(ONDEXGraph g, Set<pathway> pathways) throws Exception {
		this.graph = g;
		for (pathway p : pathways)
			processPathway(p);
	}

	/**
	 * Translate given pathway into context lists.
	 * 
	 * @param p
	 *            pathway to translate
	 */
	private void processPathway(pathway p) throws Exception {

		// initialise relation types
		RelationType partOfRt = graph.getMetaData().getRelationType(rtPartOf);
		if (partOfRt == null)
			throw new RelationTypeMissingException(rtPartOf + " is missing.");

		// first get pathway concept
		ONDEXConcept pathwayC = rdf2Concept.get(p.getRDFId());
		if (pathwayC == null)
			System.err.println("Missing concept " + p.getRDFId());
		else {

			// self context
			pathwayC.addTag(pathwayC);

			// components which belong to this pathway
			for (pathwayComponent component : p.getPATHWAY_COMPONENTS()) {
				if (component instanceof pathwayStep) {
					pathwayStep step = (pathwayStep) component;
					for (process process : step.getSTEP_INTERACTIONS()) {

						// decide on type of process
						if (process instanceof biochemicalReaction) {
							biochemicalReaction r = (biochemicalReaction) process;
							addContextReaction(pathwayC, r);
						} else if (process instanceof catalysis) {
							catalysis cat = (catalysis) process;
							addContextCatalysis(pathwayC, cat);
						} else if (process instanceof modulation) {
							modulation mod = (modulation) process;
							addContextModulation(pathwayC, mod);
						} else if (process instanceof transport) {
							transport trans = (transport) process;
							addContextTransport(pathwayC, trans);
						} else if (process instanceof pathway) {
							pathway path = (pathway) process;
							addContextPathway(pathwayC, path);
						} else if (process instanceof control) {
							control con = (control) process;
							addContextControl(pathwayC, con);
						} else
							System.err.println("Unknown process type "
									+ process.getRDFId() + " "
									+ process.getClass());
					}
				} else
					System.err.println("Unsupported pathwayComponent: "
							+ component.getRDFId());
			}
		}
	}

	/**
	 * Adds pathway context to control relationships.
	 * 
	 * @param pathwayC
	 *            ONDEXConcept for context
	 * @param con
	 *            BioPAX control
	 * @throws Exception
	 */
	private void addContextControl(ONDEXConcept pathwayC, control con)
			throws Exception {

		// decided what kind of modulation this is
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
			System.err.println("Unknown control type for modulation: "
					+ con.getRDFId());
			return;
		}

		// co-enzymes activation / inhibition of enzymes
		for (physicalEntityParticipant controller : con.getCONTROLLER()) {

			// this is usually the co-enzyme
			physicalEntity entity = controller.getPHYSICAL_ENTITY();
			String rdfID = entity.getRDFId();
			ONDEXConcept coEnzymeC = rdf2Concept.get(rdfID);
			if (coEnzymeC == null) {
				System.err.println("Missing concept " + rdfID);
			} else {

				// add context to co-enzyme
				coEnzymeC.addTag(pathwayC);

				// relation between enzyme and co-enzyme
				for (process co : con.getCONTROLLED()) {
					ONDEXConcept enzyme = rdf2Concept.get(co.getRDFId());
					if (enzyme == null)
						System.err.println("Missing concept " + co.getRDFId());
					else {

						// just to make sure enzyme has context
						enzyme.addTag(pathwayC);

						// context on relation between enzyme and co-enzyme
						ONDEXRelation r = graph.getRelation(enzyme, coEnzymeC,
								rt);
						if (r != null) {
							// add context to modulation relation
							r.addTag(pathwayC);
						} else
							System.err.println("Missing relation "
									+ co.getRDFId() + " to " + rdfID);
					}
				}
			}
		}
	}

	/**
	 * Add context to nested Pathways and the relationships involved.
	 * 
	 * @param pathwayC
	 *            ONDEXConcept for context
	 * @param path
	 *            BioPAX pathway
	 * @throws Exception
	 */
	private void addContextPathway(ONDEXConcept pathwayC, pathway path)
			throws Exception {

		// first get pathway concept
		ONDEXConcept pathC = rdf2Concept.get(path.getRDFId());
		if (pathC == null)
			System.err.println("Missing concept " + path.getRDFId());
		else {

			// context on pathway concept
			pathC.addTag(pathwayC);

			// context on relation between pathways
			RelationType rt = graph.getMetaData().getRelationType(rtPartOf);
			if (rt == null)
				throw new RelationTypeMissingException(rtPartOf
						+ " is missing.");
			ONDEXRelation r = graph.getRelation(pathC, pathwayC, rt);
			if (r != null) {
				// add context to part_of relation
				r.addTag(pathwayC);
			} //else
			//	System.err.println("Missing relation " + path.getRDFId()
			//			+ " to " + pathwayC.getPID());
		}
	}

	/**
	 * Add context to Transport and Compounds and the relationships involved.
	 * 
	 * @param pathwayC
	 *            ONDEXConcept for context
	 * @param trans
	 *            BioPAX transport
	 * @throws Exception
	 */
	private void addContextTransport(ONDEXConcept pathwayC, transport trans)
			throws Exception {

		// first get transport concept
		ONDEXConcept transC = rdf2Concept.get(trans.getRDFId());
		if (transC == null)
			System.err.println("Missing concept " + trans.getRDFId());
		else {

			// transport concept itself need context
			transC.addTag(pathwayC);

			// what is consumed
			for (physicalEntityParticipant left : trans.getLEFT()) {
				String rdfID = left.getPHYSICAL_ENTITY().getRDFId();
				ONDEXConcept from = rdf2Concept.get(rdfID);
				if (from == null)
					System.err.println("Missing concept " + rdfID);
				else {

					// context on compound concept
					from.addTag(pathwayC);

					// context on relation between compound and transport
					RelationType rt = graph.getMetaData().getRelationType(
							rtConsumedBy);
					if (rt == null)
						throw new RelationTypeMissingException(rtConsumedBy
								+ " is missing.");
					ONDEXRelation r = graph.getRelation(from, transC, rt);
					if (r != null) {
						// add context to cs_by relation
						r.addTag(pathwayC);
					} else
						System.err.println("Missing relation " + rdfID + " to "
								+ trans.getRDFId());
				}
			}

			// what is produced
			for (physicalEntityParticipant right : trans.getRIGHT()) {
				String rdfID = right.getPHYSICAL_ENTITY().getRDFId();
				ONDEXConcept from = rdf2Concept.get(rdfID);
				if (from == null)
					System.err.println("Missing concept " + rdfID);
				else {

					// context on compound concept
					from.addTag(pathwayC);

					// context on relation between compound and transport
					RelationType rt = graph.getMetaData().getRelationType(
							rtProducedBy);
					if (rt == null)
						throw new RelationTypeMissingException(rtProducedBy
								+ " is missing.");
					ONDEXRelation r = graph.getRelation(from, transC, rt);
					if (r != null) {
						// add context to pd_by relation
						r.addTag(pathwayC);
					} else
						System.err.println("Missing relation " + rdfID + " to "
								+ trans.getRDFId());
				}
			}
		}

	}

	/**
	 * Add context to Reactions and Compounds and the relationships involved.
	 * 
	 * @param pathwayC
	 *            ONDEXConcept for context
	 * @param reaction
	 *            BioPAX reaction
	 * @throws Exception
	 */
	private void addContextReaction(ONDEXConcept pathwayC,
			biochemicalReaction reaction) throws Exception {

		// first get reaction concept
		ONDEXConcept reactionC = rdf2Concept.get(reaction.getRDFId());
		if (reactionC == null)
			System.err.println("Missing concept " + reaction.getRDFId());
		else {

			// reaction concept itself need context
			reactionC.addTag(pathwayC);

			// a reaction can have an EC number
			if (!reaction.getEC_NUMBER().isEmpty()) {

				for (String ec : reaction.getEC_NUMBER()) {

					// get EC number concept
					ONDEXConcept ecC = rdf2Concept.get(ec);
					if (ecC == null)
						System.err.println("Missing concept " + ec);
					else {

						// context on EC concept
						ecC.addTag(pathwayC);

						// context on relation between reaction and EC class
						RelationType rt = graph.getMetaData().getRelationType(
								rtCatalysingClass);
						if (rt == null)
							throw new RelationTypeMissingException(
									rtCatalysingClass + " is missing.");
						ONDEXRelation r = graph.getRelation(reactionC, ecC, rt);
						if (r != null) {
							// add context to cat_c relation
							r.addTag(pathwayC);
						} else
							System.err.println("Missing relation "
									+ reaction.getRDFId() + " to " + ec);
					}
				}
			}

			// what is consumed
			for (physicalEntityParticipant left : reaction.getLEFT()) {
				String rdfID = left.getPHYSICAL_ENTITY().getRDFId();
				ONDEXConcept from = rdf2Concept.get(rdfID);
				if (from == null)
					System.err.println("Missing concept " + rdfID);
				else {

					// context on compound concept
					from.addTag(pathwayC);

					// context on relation between compound and reaction
					RelationType rt = graph.getMetaData().getRelationType(
							rtConsumedBy);
					if (rt == null)
						throw new RelationTypeMissingException(rtConsumedBy
								+ " is missing.");
					ONDEXRelation r = graph.getRelation(from, reactionC, rt);
					if (r != null) {
						// add context to cs_by relation
						r.addTag(pathwayC);
					} else
						System.err.println("Missing relation " + rdfID + " to "
								+ reaction.getRDFId());
				}
			}

			// what is produced
			for (physicalEntityParticipant right : reaction.getRIGHT()) {
				String rdfID = right.getPHYSICAL_ENTITY().getRDFId();
				ONDEXConcept from = rdf2Concept.get(rdfID);
				if (from == null)
					System.err.println("Missing concept " + rdfID);
				else {

					// context on compound concept
					from.addTag(pathwayC);

					// context on relation between compound and reaction
					RelationType rt = graph.getMetaData().getRelationType(
							rtProducedBy);
					if (rt == null)
						throw new RelationTypeMissingException(rtProducedBy
								+ " is missing.");
					ONDEXRelation r = graph.getRelation(from, reactionC, rt);
					if (r != null) {
						// add context to pd_by relation
						r.addTag(pathwayC);
					} else
						System.err.println("Missing relation " + rdfID + " to "
								+ reaction.getRDFId());
				}
			}
		}
	}

	/**
	 * Add context to Enzymes and Reactions and the relationships involved.
	 * 
	 * @param pathwayC
	 *            ONDEXConcept for context
	 * @param cat
	 *            BioPAX catalysis
	 * @throws Exception
	 */
	private void addContextCatalysis(ONDEXConcept pathwayC, catalysis cat)
			throws Exception {

		// first get enzyme concept
		ONDEXConcept enzymeC = rdf2Concept.get(cat.getRDFId());
		if (enzymeC == null)
			System.err.println("Missing concept " + cat.getRDFId());
		else {

			// context on enzyme concept
			enzymeC.addTag(pathwayC);

			// enzymes catalysing reaction
			for (physicalEntityParticipant controller : cat.getCONTROLLER()) {

				// this is usually the enzyme
				physicalEntity entity = controller.getPHYSICAL_ENTITY();

				// is_a relation between enzyme and protein
				ONDEXConcept from = rdf2Concept.get(entity.getRDFId());
				if (from == null)
					System.err.println("Missing concept " + entity.getRDFId());
				else {

					// context on Protein concept
					from.addTag(pathwayC);

					// context on relation between enzyme and protein
					RelationType rt = graph.getMetaData()
							.getRelationType(rtIsA);
					if (rt == null)
						throw new RelationTypeMissingException(rtIsA
								+ " is missing.");
					ONDEXRelation r = graph.getRelation(from, enzymeC, rt);
					if (r != null) {
						// add context to is_a relation
						r.addTag(pathwayC);
					} else
						System.err.println("Missing relation "
								+ entity.getRDFId() + " to " + cat.getRDFId());
				}

				// ca_by relation between enzyme and reaction
				for (process process : cat.getCONTROLLED()) {
					ONDEXConcept reaction = rdf2Concept.get(process.getRDFId());
					if (reaction == null)
						System.err.println("Missing concept "
								+ process.getRDFId());
					else {

						// just to make sure reaction has context
						reaction.addTag(pathwayC);

						// context on relation between enzyme and reaction
						RelationType rt = graph.getMetaData().getRelationType(
								rtCatalysedBy);
						if (rt == null)
							throw new RelationTypeMissingException(
									rtCatalysedBy + " is missing.");
						ONDEXRelation r = graph.getRelation(reaction, enzymeC,
								rt);
						if (r != null) {
							// add context to ca_by relation
							r.addTag(pathwayC);
						} else
							System.err.println("Missing relation "
									+ process.getRDFId() + " to "
									+ cat.getRDFId());
					}
				}
			}
		}
	}

	/**
	 * Adds pathway context to modulation relationships.
	 * 
	 * @param pathwayC
	 *            ONDEXConcept for context
	 * @param mod
	 *            BioPAX modulation
	 * @throws Exception
	 */
	private void addContextModulation(ONDEXConcept pathwayC, modulation mod)
			throws Exception {

		// decided what kind of modulation this is
		RelationType rt = null;
		ControlType type = mod.getCONTROL_TYPE();
		if (type == null) {
			rt = graph.getMetaData().getRelationType(rtRegulatedBy);
			if (rt == null)
				throw new RelationTypeMissingException(rtRegulatedBy
						+ " is missing.");
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
			System.err.println("Unknown control type for modulation: "
					+ mod.getRDFId());
			return;
		}

		// co-enzymes activation / inhibition of enzymes
		for (physicalEntityParticipant controller : mod.getCONTROLLER()) {

			// this is usually the co-enzyme
			physicalEntity entity = controller.getPHYSICAL_ENTITY();
			String rdfID = entity.getRDFId();
			ONDEXConcept coEnzymeC = rdf2Concept.get(rdfID);
			if (coEnzymeC == null) {
				System.err.println("Missing concept " + rdfID);
			} else {

				// add context to co-enzyme
				coEnzymeC.addTag(pathwayC);

				// relation between enzyme and co-enzyme
				for (process co : mod.getCONTROLLED()) {
					ONDEXConcept enzyme = rdf2Concept.get(co.getRDFId());
					if (enzyme == null)
						System.err.println("Missing concept " + co.getRDFId());
					else {

						// just to make sure enzyme has context
						enzyme.addTag(pathwayC);

						// context on relation between enzyme and co-enzyme
						ONDEXRelation r = graph.getRelation(enzyme, coEnzymeC,
								rt);
						if (r != null) {
							// add context to modulation relation
							r.addTag(pathwayC);
						} else
							System.err.println("Missing relation "
									+ co.getRDFId() + " to " + rdfID);
					}
				}
			}
		}
	}
}
