package net.sourceforge.ondex.ovtk2.ui.editor.concept;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;

/**
 * Clones concepts except for changing one attribute.
 * 
 * @author taubertj
 */
public class ConceptCloner {

	/**
	 * current ONDEXGraph to modify
	 */
	ONDEXGraph graph = null;

	/**
	 * Clones concepts on the given graph.
	 * 
	 * @param graph
	 *            ONDEXGraph to modify
	 */
	public ConceptCloner(ONDEXGraph graph) {
		this.graph = graph;
	}

	/**
	 * Clone concept with having a new PID.
	 * 
	 * @param oldC
	 *            old ONDEXConcept
	 * @param newPID
	 *            new PID to use
	 * @return new ONDEXConcept
	 */
	public ONDEXConcept clone(ONDEXConcept oldC, String newPID) {

		// first clone concept with new PID
		ONDEXConcept newC = graph.createConcept(newPID, oldC.getAnnotation(), oldC.getDescription(), oldC.getElementOf(), oldC.getOfType(), oldC.getEvidence());

		// copies everything else
		copyEverythingElse(oldC, newC);

		return newC;
	}

	/**
	 * Clone concept with having a new DataSource.
	 * 
	 * @param oldC
	 *            old ONDEXConcept
	 * @param newDataSource
	 *            new DataSource to use
	 * @return new ONDEXConcept
	 */
	public ONDEXConcept clone(ONDEXConcept oldC, DataSource newDataSource) {

		// first clone concept with new DataSource
		ONDEXConcept newC = graph.createConcept(oldC.getPID(), oldC.getAnnotation(), oldC.getDescription(), newDataSource, oldC.getOfType(), oldC.getEvidence());

		// copies everything else
		copyEverythingElse(oldC, newC);

		return newC;
	}

	/**
	 * Clone concept with having a new ConceptClass.
	 * 
	 * @param oldC
	 *            old ONDEXConcept
	 * @param newCC
	 *            new ConceptClass to use
	 * @return new ONDEXConcept
	 */
	public ONDEXConcept clone(ONDEXConcept oldC, ConceptClass newCC) {

		// first clone concept with new ConceptClass
		ONDEXConcept newC = graph.createConcept(oldC.getPID(), oldC.getAnnotation(), oldC.getDescription(), oldC.getElementOf(), newCC, oldC.getEvidence());

		// copies everything else
		copyEverythingElse(oldC, newC);

		return newC;
	}

	/**
	 * Calls all other subroutines for coping informations of concept.
	 * 
	 * @param oldC
	 *            old ONDEXConcept
	 * @param newC
	 *            new ONDEXConcept
	 */
	private void copyEverythingElse(ONDEXConcept oldC, ONDEXConcept newC) {
		// copies context and references across
		copyContext(oldC, newC);

		// transfer all accessions from old to new
		copyAccessions(oldC, newC);

		// transfer all names from old to new
		copyNames(oldC, newC);

		// transfer all Attribute from old to new
		copyGDS(oldC, newC);

		// transfer all relations from old to new
		transferRelations(oldC, newC);
	}

	/**
	 * Copies all context information from old concept to new concept and
	 * updates context references on other entities.
	 * 
	 * @param oldC
	 *            old ONDEXConcept
	 * @param newC
	 *            new ONDEXConcept
	 */
	private void copyContext(ONDEXConcept oldC, ONDEXConcept newC) {

		// add new concept to same references as old concept
		for (ONDEXConcept c : graph.getConceptsOfTag(oldC)) {
			c.addTag(newC);
		}
		for (ONDEXRelation r : graph.getRelationsOfTag(oldC)) {
			r.addTag(newC);
		}

		// transfer context from old concept to new concept
		for (ONDEXConcept c : oldC.getTags()) {
			newC.addTag(c);
		}
	}

	/**
	 * Copies all accessions from old concept to new concept.
	 * 
	 * @param oldC
	 *            old ONDEXConcept
	 * @param newC
	 *            new ONDEXConcept
	 */
	private void copyAccessions(ONDEXConcept oldC, ONDEXConcept newC) {

		// iterate over all old accessions
		for (ConceptAccession acc : oldC.getConceptAccessions()) {
			// clone old accession on new concept
			newC.createConceptAccession(acc.getAccession(), acc.getElementOf(), acc.isAmbiguous());
		}
	}

	/**
	 * Copies all names from old concept to new concept.
	 * 
	 * @param oldC
	 *            old ONDEXConcept
	 * @param newC
	 *            new ONDEXConcept
	 */
	private void copyNames(ONDEXConcept oldC, ONDEXConcept newC) {

		// iterate over all old names
		for (ConceptName name : oldC.getConceptNames()) {
			// clone old name on new concept
			newC.createConceptName(name.getName(), name.isPreferred());
		}
	}

	/**
	 * Copies all Attribute from old concept to new concept.
	 * 
	 * @param oldC
	 *            old ONDEXConcept
	 * @param newC
	 *            new ONDEXConcept
	 */
	private void copyGDS(ONDEXConcept oldC, ONDEXConcept newC) {

		// iterate over all old Attribute
		for (Attribute attribute : oldC.getAttributes()) {
			// clone old Attribute on new concept
			newC.createAttribute(attribute.getOfType(), attribute.getValue(), attribute.isDoIndex());
		}
	}

	/**
	 * Transfers all relations that the old concept takes part into to the new
	 * concept.
	 * 
	 * @param oldC
	 *            old ONDEXConcept
	 * @param newC
	 *            new ONDEXConcept
	 */
	private void transferRelations(ONDEXConcept oldC, ONDEXConcept newC) {

		// iterate over all relations of old concept
		for (ONDEXRelation oldR : graph.getRelationsOfConcept(oldC)) {
			ONDEXRelation newR = null;

			if (oldR.getFromConcept().equals(oldC)) {
				// old concept is from concept
				newR = graph.createRelation(newC, oldR.getToConcept(), oldR.getOfType(), oldR.getEvidence());
			} else if (oldR.getToConcept().equals(oldC)) {
				// old concept is to concept
				newR = graph.createRelation(oldR.getFromConcept(), newC, oldR.getOfType(), oldR.getEvidence());
			}

			// transfer context old to new
			for (ONDEXConcept c : oldR.getTags()) {
				newR.addTag(c);
			}
		}
	}

}
