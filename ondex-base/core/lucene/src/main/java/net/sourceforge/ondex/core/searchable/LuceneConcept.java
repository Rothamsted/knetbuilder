package net.sourceforge.ondex.core.searchable;

import java.util.Set;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * This class represents a wrapper implementation of the AbstractConcept.
 * 
 * @author taubertj
 * 
 */
public class LuceneConcept implements ONDEXConcept {

	/**
	 * Unique graph ID.
	 */
	private long sid;

	/**
	 * wrapped concept
	 */
	private ONDEXConcept parent;

	/**
	 * Constructor which wraps a given concept.
	 * 
	 * @param sid
	 *            unique id
	 * @param c
	 *            ONDEXConcept to wrap
	 */
	LuceneConcept(long sid, ONDEXConcept c) {
		this.sid = sid;
		this.parent = c;
	}

	/**
	 * Returns the parent Concept which is wrapped in this instance.
	 * 
	 * @return AbstractConcept
	 */
	public ONDEXConcept getParent() {
		return this.parent;
	}

	@Override
	public void addEvidenceType(EvidenceType evidencetype)
			throws NullValueException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addTag(ONDEXConcept concept) throws NullValueException,
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Attribute createAttribute(AttributeName attrname, Object value,
			boolean doIndex) throws NullValueException,
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean deleteAttribute(AttributeName attrname)
			throws NullValueException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Attribute getAttribute(AttributeName attrname)
			throws NullValueException {
		return parent.getAttribute(attrname);
	}

	@Override
	public Set<Attribute> getAttributes() {
		return parent.getAttributes();
	}

	@Override
	public Set<EvidenceType> getEvidence() {
		return parent.getEvidence();
	}

	@Override
	public int getId() {
		return parent.getId();
	}

	@Override
	public Set<ONDEXConcept> getTags() {
		return parent.getTags();
	}

	@Override
	public boolean removeEvidenceType(EvidenceType evidencetype)
			throws NullValueException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeTag(ONDEXConcept concept) throws NullValueException,
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getSID() {
		return sid;
	}

	@Override
	public boolean inheritedFrom(ConceptClass h) {
		return parent.inheritedFrom(h);
	}

	@Override
	public ConceptClass getOfType() {
		return parent.getOfType();
	}

	@Override
	public ConceptAccession createConceptAccession(String accession,
			DataSource elementOf, boolean ambiguous) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ConceptName createConceptName(String name, boolean isPreferred)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean deleteConceptAccession(String accession, DataSource elementOf)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean deleteConceptName(String name) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAnnotation() {
		return parent.getAnnotation();
	}

	@Override
	public ConceptAccession getConceptAccession(String accession,
			DataSource elementOf) throws NullValueException,
			EmptyStringException {
		return parent.getConceptAccession(accession, elementOf);
	}

	@Override
	public Set<ConceptAccession> getConceptAccessions() {
		return parent.getConceptAccessions();
	}

	@Override
	public ConceptName getConceptName() {
		return parent.getConceptName();
	}

	@Override
	public ConceptName getConceptName(String name) throws NullValueException,
			EmptyStringException {
		return parent.getConceptName(name);
	}

	@Override
	public Set<ConceptName> getConceptNames() {
		return parent.getConceptNames();
	}

	@Override
	public String getDescription() {
		return parent.getDescription();
	}

	@Override
	public DataSource getElementOf() {
		return parent.getElementOf();
	}

	@Override
	public String getPID() {
		return parent.getPID();
	}

	@Override
	public void setPID(String pid) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAnnotation(String annotation)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDescription(String description)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

}
