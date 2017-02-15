package net.sourceforge.ondex.tools.ondex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;

/**
 * A tool for cloning an ONDEX graph
 * 
 * @author hindlem
 */
public class ONDEXGraphCloner {

	private static Map<Long, Map<Long, Map<Integer, Integer>>> indexedClonedConcepts = new HashMap<Long, Map<Long, Map<Integer, Integer>>>();
	private static Map<Long, Map<Long, Map<Integer, Integer>>> indexedClonedRelations = new HashMap<Long, Map<Long, Map<Integer, Integer>>>();

	private ONDEXGraph origGraph;
	private ONDEXGraph newGraph;

	private boolean metaDataHasBeenCloned = false;
	private boolean conceptsHaveBeenCloned = false;
	private boolean relationsHaveBeenCloned = false;

	private Map<Integer, Integer> old2newConceptIds = null;
	private Map<Integer, Integer> old2newRelationIds = null;

	/**
	 * @param origGraph
	 *            the original graph to clone from
	 * @param newGraph
	 *            the new graph
	 */
	public ONDEXGraphCloner(ONDEXGraph origGraph, ONDEXGraph newGraph) {
		this.origGraph = origGraph;
		this.newGraph = newGraph;
		if (origGraph.equals(newGraph)) {
			throw new IllegalArgumentException(
					"Illegal argument graph source and clone are identical");
		} else if (origGraph.getSID() == newGraph.getSID()) {
			throw new IllegalArgumentException(
					"Illegal argument graph source and clone are of the same name");

		}
		createIndex();
	}

	public static synchronized void clearIndex() {
		indexedClonedConcepts.clear();
		indexedClonedRelations.clear();
	}

	/**
	 * creates a shared index across all clone instances so graphs can be
	 * partialy be cloned in several instances
	 */
	private synchronized void createIndex() {
		Map<Long, Map<Integer, Integer>> targetGraphs = indexedClonedConcepts
				.get(origGraph.getSID());
		if (targetGraphs == null) {
			targetGraphs = new HashMap<Long, Map<Integer, Integer>>();
			indexedClonedConcepts.put(origGraph.getSID(), targetGraphs);
		}

		Map<Integer, Integer> conceptIndex = targetGraphs
				.get(newGraph.getSID());
		if (conceptIndex == null) {
			conceptIndex = new HashMap<Integer, Integer>();
			targetGraphs.put(newGraph.getSID(), conceptIndex);
		}
		old2newConceptIds = conceptIndex;

		targetGraphs = indexedClonedRelations.get(origGraph.getSID());
		if (targetGraphs == null) {
			targetGraphs = new HashMap<Long, Map<Integer, Integer>>();
			indexedClonedRelations.put(origGraph.getSID(), targetGraphs);
		}

		Map<Integer, Integer> relationIndex = targetGraphs.get(newGraph
				.getSID());
		if (relationIndex == null) {
			relationIndex = new HashMap<Integer, Integer>();
			targetGraphs.put(newGraph.getSID(), relationIndex);
		}
		old2newRelationIds = relationIndex;
	}

	/**
	 * Clones metadata from the original graph
	 */
	public void cloneMetaData() {
		if (metaDataHasBeenCloned) {
			System.err
					.println("MetaData has already been cloned: Ignoring request");
			return;
		}

		ONDEXGraphMetaData omd = origGraph.getMetaData();
		ONDEXGraphMetaData nomd = newGraph.getMetaData();

		for (Unit u : omd.getUnits()) {
			if (!nomd.checkUnit(u.getId()))
				nomd.createUnit(u.getId(), u.getFullname(), u.getDescription());
		}

		Set<AttributeName> attNames = omd.getAttributeNames();
		copyAttributeNames(attNames, omd, nomd);

		Set<ConceptClass> ccs = omd.getConceptClasses();
		copyConceptClasses(ccs, omd, nomd);

		for (DataSource dataSource : omd.getDataSources()) {
			if (!nomd.checkDataSource(dataSource.getId()))
				nomd
						.createDataSource(dataSource.getId(), dataSource.getFullname(), dataSource
								.getDescription());
		}

		for (EvidenceType et : omd.getEvidenceTypes()) {
			if (!nomd.checkEvidenceType(et.getId()))
				nomd.createEvidenceType(et.getId(), et.getFullname(), et
						.getDescription());
		}

		Set<RelationType> rts = omd.getRelationTypes();
		copyRelationTypes(rts, omd, nomd);

		metaDataHasBeenCloned = true;
	}

	/**
	 * @param attNames
	 * @param omd
	 *            copy from MetaData
	 * @param nomd
	 *            copy to MetaData
	 */
	private void copyAttributeNames(Iterable<AttributeName> attNames,
			ONDEXGraphMetaData omd, ONDEXGraphMetaData nomd) {
		// "specializations_of"s that occur before there specialisation
		HashSet<AttributeName> atDependencyNotResolved = new HashSet<AttributeName>();

		for (AttributeName att : attNames) {
			AttributeName spec = att.getSpecialisationOf();
			AttributeName newSpec = null;
			if (spec != null) {
				newSpec = nomd.getAttributeName(spec.getId());
				if (newSpec == null) {
					atDependencyNotResolved.add(att);
					continue;
				}
			}

			if (!nomd.checkAttributeName(att.getId()))
				nomd.createAttributeName(att.getId(), att.getFullname(), att
						.getDescription(), att.getUnit(), att.getDataType(),
						newSpec);
		}

		if (atDependencyNotResolved.size() > 0) {
			copyAttributeNames(atDependencyNotResolved, omd, nomd);
		}
	}

	/**
	 * @param ccs
	 * @param omd
	 *            copy from MetaData
	 * @param nomd
	 *            copy to MetaData
	 */
	private void copyConceptClasses(Iterable<ConceptClass> ccs,
			ONDEXGraphMetaData omd, ONDEXGraphMetaData nomd) {
		// "specializations_of"s that occur before there specialization
		HashSet<ConceptClass> ccDependencyNotResolved = new HashSet<ConceptClass>();

		for (ConceptClass cc : ccs) {

			ConceptClass ccSpec = cc.getSpecialisationOf();
			ConceptClass newCCSpec = null;
			if (ccSpec != null) {
				newCCSpec = nomd.getConceptClass(ccSpec.getId());
				if (newCCSpec == null) {
					ccDependencyNotResolved.add(cc);
					continue;
				}
			}

			if (!nomd.checkConceptClass(cc.getId()))
				nomd.createConceptClass(cc.getId(), cc.getFullname(), cc
						.getDescription(), newCCSpec);
		}
		// recursively resolve specialisations
		if (ccDependencyNotResolved.size() > 0) {
			copyConceptClasses(ccDependencyNotResolved, omd, nomd);
		}
	}

	/**
	 * @param rts
	 * @param omd
	 *            copy from MetaData
	 * @param nomd
	 *            copy to MetaData
	 * @return
	 */
	private void copyRelationTypes(Iterable<RelationType> rts,
			ONDEXGraphMetaData omd, ONDEXGraphMetaData nomd) {
		// "specializations_of"s that occur before there specialisation
		HashSet<RelationType> rtDependencyNotResolved = new HashSet<RelationType>();

		for (RelationType rt : rts) {
			RelationType spec = rt.getSpecialisationOf();
			RelationType newSpec = null;
			if (spec != null) {
				newSpec = nomd.getRelationType(spec.getId());
				if (newSpec == null) {
					rtDependencyNotResolved.add(rt);
					continue;
				}
			}

			if (!nomd.checkRelationType(rt.getId()))
				nomd.createRelationType(rt.getId(), rt.getFullname(), rt
						.getDescription(), rt.getInverseName(), rt
						.isAntisymmetric(), rt.isReflexive(), rt.isSymmetric(),
						rt.isTransitiv(), newSpec);
		}
		// recursively resolve specialisations
		if (rtDependencyNotResolved.size() > 0) {
			copyRelationTypes(rtDependencyNotResolved, omd, nomd);
		}
	}

	/**
	 * NB if you make a clone request here and metadata has not been cloned,
	 * then this will be done
	 */
	public void cloneAllConcepts() {
		if (!metaDataHasBeenCloned) {
			cloneMetaData();
		}
		if (conceptsHaveBeenCloned) {
			System.err
					.println("Concepts have already been cloned: Ignoring request");
			return;
		}
		conceptsHaveBeenCloned = true;
		for (ONDEXConcept c : origGraph.getConcepts()) {
			cloneConcept(c);
		}
	}

	/**
	 * NB if you make a clone request here and metadata and concepts have not
	 * been cloned, then this will be done
	 */
	public void cloneAllRelations() {
		if (!metaDataHasBeenCloned) {
			cloneMetaData();
		}
		if (!conceptsHaveBeenCloned) {
			cloneAllConcepts();
		}
		if (relationsHaveBeenCloned) {
			System.err
					.println("Relations have already been cloned: Ignoring request");
			return;
		}
		relationsHaveBeenCloned = true;
		for (ONDEXRelation r : origGraph.getRelations()) {
			cloneRelation(r);
		}
	}

	/**
	 * Clones everything from the original graph
	 */
	public void cloneAll() {
		cloneMetaData();
		cloneAllConcepts();
		cloneAllRelations();
	}

	public ONDEXGraph getNewGraph() {
		return newGraph;
	}

	/**
	 * Clones a concept from the original graph to the new graph
	 * 
	 * @param cid
	 *            the id of the concept in the original graph
	 * @return the new Concept in the new graph
	 */
	public ONDEXConcept cloneConcept(int cid) {
		ONDEXConcept concept = origGraph.getConcept(cid);
		if (concept == null) {
			throw new NullPointerException(cid
					+ " is not found in orginal Graph " + origGraph.getSID());
		}
		return cloneConcept(concept);
	}

	/**
	 * Clones a concept from the original graph to the new graph
	 * 
	 * @param conceptToClone
	 *            the concept to clone in the original graph
	 * @return the new Concept
	 */
	public ONDEXConcept cloneConcept(ONDEXConcept conceptToClone) {
		if (!metaDataHasBeenCloned) {
			cloneMetaData();
		}

		if (old2newConceptIds.containsKey(conceptToClone.getId())) {
			// System.out.println(conceptToClone.getId() +
			// " already cloned returning previously cloned concept");
			return newGraph.getConcept(old2newConceptIds.get(conceptToClone
					.getId()));
		}

		ONDEXGraphMetaData nomd = newGraph.getMetaData();

		String pid = conceptToClone.getPID();
		String desc = conceptToClone.getDescription();
		String anno = conceptToClone.getAnnotation();

		ArrayList<EvidenceType> ets = new ArrayList<EvidenceType>();

		for (EvidenceType evidence : conceptToClone.getEvidence()) {
			ets.add(nomd.getEvidenceType(evidence.getId()));
		}

		DataSource dataSource = conceptToClone.getElementOf();
		DataSource newDataSource = nomd.getDataSource(dataSource.getId());

		ConceptClass cc = conceptToClone.getOfType();
		ConceptClass newCC = nomd.getConceptClass(cc.getId());

		ONDEXConcept newConcept = newGraph.createConcept(pid, anno, desc,
				newDataSource, newCC, ets);

		// required to prevent StackOverflow when adding itself as tag
		old2newConceptIds.put(conceptToClone.getId(), newConcept.getId());

		for (ONDEXConcept tag : conceptToClone.getTags()) {
			ONDEXConcept newTag;
			if (old2newConceptIds.containsKey(tag.getId())) { // check if
				// tag
				// concept
				// exists
				int cid = old2newConceptIds.get(tag.getId());
				newTag = newGraph.getConcept(cid);
			} else {
				newTag = cloneConcept(tag); // recursive
			}
			newConcept.addTag(newTag);
		}

		for (Attribute attribute : conceptToClone.getAttributes()) {
			AttributeName att = attribute.getOfType();
			AttributeName newAtt = nomd.getAttributeName(att.getId());
			newConcept.createAttribute(newAtt, attribute.getValue(), attribute.isDoIndex());
		}

		for (ConceptName name : conceptToClone.getConceptNames()) {
			newConcept.createConceptName(name.getName(), name.isPreferred());
		}

		for (ConceptAccession acc : conceptToClone.getConceptAccessions()) {
			dataSource = acc.getElementOf();
			newDataSource = nomd.getDataSource(dataSource.getId());
			newConcept.createConceptAccession(acc.getAccession(), newDataSource, acc
					.isAmbiguous());
		}

		return newConcept;
	}

	/**
	 * @param rid
	 *            the relation id in the original graph
	 * @return the relation in the new graph
	 */
	public ONDEXRelation cloneRelation(int rid) {
		return cloneRelation(origGraph.getRelation(rid));
	}

	/**
	 * @param relationToClone
	 *            the relation to clone
	 * @return the new relation in the new graph
	 */
	public ONDEXRelation cloneRelation(ONDEXRelation relationToClone) {
		if (!metaDataHasBeenCloned) {
			cloneMetaData();
		}

		if (old2newRelationIds.containsKey(relationToClone.getId())) {
//			System.out.println(relationToClone.getId()
//					+ " already cloned returning previously cloned relation");
			return newGraph.getRelation(old2newRelationIds.get(relationToClone
					.getId()));
		}

		ONDEXGraphMetaData nomd = newGraph.getMetaData();

		int fromId = relationToClone.getKey().getFromID();
		Integer newFromRelationID = old2newConceptIds.get(fromId);
		ONDEXConcept newFrom;
		if (newFromRelationID == null || newFromRelationID < 1) { // check if
			// already
			// cloned
			newFrom = cloneConcept(fromId);
		} else {
			newFrom = newGraph.getConcept(newFromRelationID);
		}
		assert newFrom != null : "Could not find Concept " + newFromRelationID
				+ " in new graph (cloned from " + fromId
				+ " in original graph)";

		int toId = relationToClone.getKey().getToID();
		Integer newToRelationID = old2newConceptIds.get(toId);
		ONDEXConcept newTo;
		if (newToRelationID == null || newToRelationID < 1) { // check if
			// already
			// cloned
			newTo = cloneConcept(toId);
		} else {
			newTo = newGraph.getConcept(newToRelationID);
		}
		assert newTo != null : "Could not find Concept " + newToRelationID
				+ " in old graph";

		ArrayList<EvidenceType> ets = new ArrayList<EvidenceType>();

		for (EvidenceType evidence : relationToClone.getEvidence()) {
			ets.add(nomd.getEvidenceType(evidence.getId()));
		}

		RelationType rt = relationToClone.getOfType();
		RelationType newRt = nomd.getRelationType(rt.getId());

		ONDEXRelation newRelation = newGraph.createRelation(newFrom, newTo,
				newRt, ets);
		assert newRelation != null : "new relation was not created in new graph";

		for (ONDEXConcept tag : relationToClone.getTags()) {

			ONDEXConcept newTag;
			if (old2newConceptIds.containsKey(tag.getId())) { // check if
				// tag
				// concept
				// exists
				int cid = old2newConceptIds.get(tag.getId());
				newTag = newGraph.getConcept(cid);
			} else {
				newTag = cloneConcept(tag); // recursive
			}
			newRelation.addTag(newTag);
		}

		for (Attribute attribute : relationToClone.getAttributes()) {
			AttributeName att = attribute.getOfType();
			AttributeName newAtt = nomd.getAttributeName(att.getId());
			assert newAtt != null;
			assert attribute != null;
			assert (newRelation != null);
			assert attribute.getValue() != null;
			newRelation.createAttribute(newAtt, attribute.getValue(), attribute.isDoIndex());
		}
		old2newRelationIds.put(relationToClone.getId(), newRelation.getId());
		return newRelation;
	}

	public Map<Integer, Integer> getOld2newConceptIds() {
		return old2newConceptIds;
	}

	public Map<Integer, Integer> getOld2newRelationIds() {
		return old2newRelationIds;
	}

}
