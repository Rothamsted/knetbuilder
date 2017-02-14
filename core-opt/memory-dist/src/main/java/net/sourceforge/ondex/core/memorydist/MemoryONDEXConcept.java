package net.sourceforge.ondex.core.memorydist;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.base.AbstractConcept;
import net.sourceforge.ondex.event.type.DuplicatedEntryEvent;
import net.sourceforge.ondex.exception.type.AccessDeniedException;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

/**
 * This class represents a pure memory based implementation of the ONDEXConcept.
 * It uses standard JAVA datatypes.
 * 
 * @author taubertj
 * @author Matthew Pocock
 */
public class MemoryONDEXConcept extends AbstractConcept implements
		Comparable<MemoryONDEXConcept> {

	// serial version id
	private static final long serialVersionUID = 1L;

	// Concept names
	private final Set<ConceptName> names;

	// Concept accessions
	private final Set<ConceptAccession> accessions;

	// concept attributes associated with this concept
	private final BidiMap<AttributeName, Attribute> attributes;

	/**
	 * parent graph
	 */
	private final DistributedMemoryONDEXGraph graph;

	/**
	 * Constructor which fills all fields of Concept and initialise empty
	 * HashMap for possible concept names and concept accessions
	 * 
	 * @param graph
	 *            parent MemoryONDEXGraph
	 * @param id
	 *            unique ID of this Concept
	 * @param pid
	 *            parser assigned ID
	 * @param annotation
	 *            relevant annotations of this Concept
	 * @param description
	 *            every associated description of this Concept
	 * @param elementOf
	 *            DataSource to which this Concept belongs to
	 * @param ofType
	 *            ConceptClass of this Concept
	 * @throws AccessDeniedException
	 */
	MemoryONDEXConcept(long sid, DistributedMemoryONDEXGraph graph, int id, String pid,
			String annotation, String description, DataSource elementOf,
			ConceptClass ofType) {
		super(sid, id, pid, annotation, description, elementOf, ofType);
		this.graph = graph;

		// initialise new concept specific data structures
		names = new HashSet<ConceptName>();
		accessions = new HashSet<ConceptAccession>();
		attributes = new DualHashBidiMap<AttributeName, Attribute>();
	}

	@Override
	public int compareTo(MemoryONDEXConcept o) {
		return (getId() < o.getId() ? -1 : (getId() == o.getId() ? 0 : 1));
	}

	@Override
	protected boolean dropEvidenceType(EvidenceType evidencetype) {
		// evidence types are held centrally by the graph
		graph.conceptToEvidence.get(this).remove(evidencetype);
		Set<ONDEXConcept> set = graph.evidenceTypeToConcepts.get(evidencetype);
		if (set != null)
			return set.remove(this);

		// nothing found
		return false;
	}

	@Override
	protected boolean dropTag(ONDEXConcept concept) {
		return graph.conceptToTags.get(this).remove(concept)
				&& graph.tagToConcepts.get(concept).remove(this);
	}

	@Override
	protected boolean removeConceptAccession(String accession,
			DataSource elementOf) {

		// using iterator to allow concurrent modification
		for (Iterator<ConceptAccession> i = accessions.iterator(); i.hasNext();) {
			ConceptAccession ca = i.next();

			// compare if current accession matches both
			if (ca.getAccession().equals(accession)
					&& ca.getElementOf().equals(elementOf)) {

				// use iterator to remove
				i.remove();
				return true;
			}
		}

		// nothing found
		return false;
	}

	@Override
	protected boolean removeConceptAttribute(AttributeName attrname) {
		Set<ONDEXConcept> existingAttribute = graph.attributeNameToConcepts
				.get(attrname);
		if (existingAttribute != null) {
			existingAttribute.remove(this);
		}
		return attributes.remove(attrname) != null;
	}

	@Override
	protected boolean removeConceptName(String name) {

		// using iterator to allow concurrent modification
		for (Iterator<ConceptName> i = names.iterator(); i.hasNext();) {
			ConceptName cn = i.next();

			// compare if current name matches
			if (cn.getName().equals(name)) {

				// use iterator to remove
				i.remove();
				return true;
			}
		}

		// nothing found
		return false;
	}

	@Override
	protected ConceptAccession retrieveConceptAccession(String accession,
			DataSource elementOf) {

		// iterate over all concept accessions
		for (ConceptAccession ca : accessions) {

			// compare if current accession matches both
			if (ca.getAccession().equals(accession)
					&& ca.getElementOf().equals(elementOf)) {
				return ca;
			}
		}

		// nothing found
		return null;
	}

	@Override
	protected Set<ConceptAccession> retrieveConceptAccessionAll() {
		// will be wrapped as UnmodifiableSet in base
		return accessions;
	}

	@Override
	protected Attribute retrieveConceptAttribute(AttributeName attrname) {
		// attributes index by their attribute name
		return attributes.get(attrname);
	}

	@Override
	protected Set<Attribute> retrieveConceptAttributeAll() {
		// will be wrapped as UnmodifiableSet in base
		return attributes.values();
	}

	@Override
	protected ConceptName retrieveConceptName(String name) {

		// iterate over all concept names
		for (ConceptName cn : names) {

			// compare if current name matches
			if (cn.getName().equals(name)) {
				return cn;
			}
		}

		// nothing found
		return null;
	}

	@Override
	protected Set<ConceptName> retrieveConceptNameAll() {
		// will be wrapped as UnmodifiableSet in base
		return names;
	}

	@Override
	protected Set<EvidenceType> retrieveEvidenceTypeAll() {
		// will be wrapped as UnmodifiableSet in base
		return graph.conceptToEvidence.get(this);
	}

	@Override
	protected ConceptName retrievePreferredConceptName() {

		// iterate over all concept names
		for (ConceptName cn : names) {

			// first preferred name returned
			if (cn.isPreferred()) {
				return cn;
			}
		}

		// nothing found
		return null;
	}

	@Override
	protected Set<ONDEXConcept> retrieveTagAll() {
		// will be wrapped as UnmodifiableSet in base
		return graph.conceptToTags.get(this);
	}

	@Override
	protected void saveEvidenceType(EvidenceType evidencetype) {
		Set<ONDEXConcept> set = graph.evidenceTypeToConcepts.get(evidencetype);
		if (set == null) {
			set = new HashSet<ONDEXConcept>();
			graph.evidenceTypeToConcepts.put(evidencetype, set);
		}
		set.add(this);
		graph.conceptToEvidence.get(this).add(evidencetype);
	}

	@Override
	protected void saveTag(ONDEXConcept tag) {
		// associate this concept with tag
		Set<ONDEXConcept> set = graph.tagToConcepts.get(tag);
		if (set == null) {
			set = new HashSet<ONDEXConcept>();
			graph.tagToConcepts.put(tag, set);
		}
		set.add(this);
		// associate tag with this concept
		Set<ONDEXConcept> tags = graph.conceptToTags.get(this);
		if (tags == null) {
			tags = new HashSet<ONDEXConcept>();
			graph.conceptToTags.put(this, tags);
		}
		tags.add(tag);
	}

	@Override
	protected ConceptAccession storeConceptAccession(ConceptAccession ca) {

		// add concept accession to local set
		accessions.add(ca);
		return ca;
	}

	@Override
	protected Attribute storeConceptAttribute(Attribute attribute) {

		AttributeName an = attribute.getOfType();

		// check if attribute already exists
		Attribute existing = attributes.put(an, attribute);

		// complain about duplicates
		if (existing != null) {
			graph.fireEventOccurred(new DuplicatedEntryEvent(Config.properties
					.getProperty("memory.Concept.DuplicatedConceptAttribute")
					+ attribute.getOfType().getId(),
					"[Concept - storeConceptAttribute]"));
		}

		// store it in central index
		Set<ONDEXConcept> attrSet = graph.attributeNameToConcepts.get(an);
		if (attrSet == null) {
			attrSet = new HashSet<ONDEXConcept>();
			graph.attributeNameToConcepts.put(an, attrSet);
		}
		attrSet.add(this);

		return attribute;
	}

	@Override
	protected ConceptName storeConceptName(ConceptName cn) {

		// add concept name to local set
		names.add(cn);
		return cn;
	}
}
