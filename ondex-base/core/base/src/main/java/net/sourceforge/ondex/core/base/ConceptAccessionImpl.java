package net.sourceforge.ondex.core.base;

import net.sourceforge.ondex.config.ONDEXGraphRegistry;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.DataSource;

/**
 * Implementation of ConceptAccession.
 * 
 * @author Matthew Pocock
 */
public class ConceptAccessionImpl extends AbstractONDEXEntity implements
		ConceptAccession {

	/**
	 * owning concept's id
	 */
	protected final int conceptId;

	/**
	 * accession name
	 */
	protected String accession;

	/**
	 * data source this accession belongs to
	 */
	protected DataSource elementOf;

	/**
	 * is ambiguous?
	 */
	protected boolean ambiguous;

	/**
	 * pre-computed hash code
	 */
	protected int hashCode;

	/**
	 * Constructor which fills all fields of ConceptAccession
	 * 
	 * @param sid
	 *            unique id
	 * @param conceptId
	 *            parent concept id
	 * @param accession
	 *            the accession to be stored
	 * @param elementOf
	 *            DataSource to which accession belongs to
	 * @param ambiguous
	 *            whether or not this accession is ambiguous
	 */
	protected ConceptAccessionImpl(long sid, int conceptId, String accession,
			DataSource elementOf, boolean ambiguous) {
		this.sid = sid;
		this.conceptId = conceptId;
		this.accession = accession;
		this.elementOf = elementOf;
		this.ambiguous = ambiguous;
		this.hashCode = accession.hashCode() + elementOf.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof ConceptAccession) {
			ConceptAccession ca = (ConceptAccession) o;
			return this.getAccession().equals(ca.getAccession())
					&& this.getElementOf().equals(ca.getElementOf());
		}
		return false;
	}

	/**
	 * Returns the store accession name.
	 * 
	 * @return String
	 */
	@Override
	public String getAccession() {
		return accession;
	}

	/**
	 * Returns the DataSource this accession belongs to.
	 * 
	 * @return DataSource
	 */
	@Override
	public DataSource getElementOf() {
		return elementOf;
	}

	@Override
	public int getOwnerId() {
		return conceptId;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	/**
	 * Returns whether or not this accession is ambiguous.
	 * 
	 * @return boolean
	 */
	@Override
	public boolean isAmbiguous() {
		return ambiguous;
	}

	/**
	 * Changes whether or not this accession is ambiguous.
	 * 
	 * @param ambiguous
	 *            set ambiguous
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	@Override
	public void setAmbiguous(boolean ambiguous)
			throws UnsupportedOperationException {

		// check for read-only graphs
		if (ONDEXGraphRegistry.graphs.get(sid).isReadOnly())
			throw new UnsupportedOperationException();

		this.ambiguous = ambiguous;
	}

}
