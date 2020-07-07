package net.sourceforge.ondex.core;

import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * This class represents a ONDEX Concept. It can have 0..* ConceptNames, 0..*
 * ConceptAccessions and 0..* ConceptAttribute. ConceptNames, ConceptAccessions
 * and ConceptAttribute can be added with the create*-methods.
 * 
 * @author sierenk, taubertj
 * 
 */
@XmlJavaTypeAdapter(net.sourceforge.ondex.webservice.Adapters.AnyTypeAdapter.class)
public interface ONDEXConcept extends ONDEXEntity, ONDEXAssociable,
		Instantiation<ConceptClass> {

	/**
	 * Creates a new ConceptAccession with the given accession, the information
	 * which DataSource it belongs to and if its accession is ambiguous. Then
	 * adds the new ConceptAccession to the list of ConceptAccessions of this
	 * AbstractConcept.
	 * 
	 * @param accession
	 *            accession of the new ConceptAccession
	 * @param elementOf
	 *            control vocabulary DataSource
	 * @param ambiguous
	 *            is ambiguous accession
	 * @return new ConceptAccession
	 * @throws NullValueException
	 *             if accession or elementOf parameters is null.
	 * @throws EmptyStringException
	 *             if accession parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public ConceptAccession createConceptAccession(String accession,
			DataSource elementOf, boolean ambiguous) throws NullValueException,
			EmptyStringException, UnsupportedOperationException;

	/**
	 * Creates a new ConceptName with the given name and the information if this
	 * name is preferred. Then adds the new ConceptName to the list of
	 * ConceptNames of this Concept.
	 * 
	 * @param name
	 *            name of the new ConceptName
	 * @param isPreferred
	 *            is name preferred?
	 * @return new ConceptName
	 * @throws NullValueException
	 *             if name parameters is null.
	 * @throws EmptyStringException
	 *             if name parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public ConceptName createConceptName(String name, boolean isPreferred)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException;

	/**
	 * Deletes a ConceptAccession specified by accession and elementOf from the
	 * list of ConceptAccession of this AbstractConcept. Returns the removed
	 * ConceptAccession or null if unsuccessful.
	 * 
	 * @param accession
	 *            accession of ConceptAccession to be deleted
	 * @param elementOf
	 *            DataSource of ConceptAccession to be deleted
	 * @return true if an entity was deleted, false otherwise
	 * @throws NullValueException
	 *             if parameter is null.
	 * @throws EmptyStringException
	 *             if parameter is empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public boolean deleteConceptAccession(String accession, DataSource elementOf)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException;

	/**
	 * Deletes a ConceptName with the given name and returns the deleted one or
	 * null if unsuccessful.
	 * 
	 * @param name
	 *            name of ConceptName to be deleted
	 * @return true if an entity was deleted, false otherwise
	 * @throws NullValueException
	 *             if the name parameter is null.
	 * @throws EmptyStringException
	 *             if the name parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public boolean deleteConceptName(String name) throws NullValueException,
			EmptyStringException, UnsupportedOperationException;

	/**
	 * Returns the annotation of this instance of AbstractConcept.
	 * 
	 * @return annotation String
	 */
	public String getAnnotation();

	/**
	 * Returns a ConceptAccession or null if unsuccessful for a given accession
	 * and DataSource or null if unsuccessful.
	 * 
	 * @param accession
	 *            accession of ConceptAccession to be returned
	 * @param elementOf
	 *            DataSource of ConceptAccession to be returned
	 * @return existing ConceptAccession
	 * @throws NullValueException
	 *             if parameter is null.
	 * @throws EmptyStringException
	 *             if parameter is an empty string.
	 */
	public ConceptAccession getConceptAccession(String accession,
			DataSource elementOf) throws NullValueException,
			EmptyStringException;

	/**
	 * Returns all ConceptAccessions contained in the list of ConceptAccessions.
	 * 
	 * @return all ConceptAccessions as Set<ConceptAccession>
	 */
	public Set<ConceptAccession> getConceptAccessions();

	/**
	 * Returns the preferred ConceptName or null if non is present.
	 * 
	 * @return preferred ConceptName
	 */
	public ConceptName getConceptName();

	/**
	 * Returns a ConceptName or null if unsuccessful for a given name or null if
	 * unsuccessful.
	 * 
	 * @param name
	 *            name of ConceptName to be returned
	 * @return existing ConceptName
	 * @throws NullValueException
	 *             if name parameter is null.
	 * @throws EmptyStringException
	 *             if name parameter is an empty string.
	 */
	public ConceptName getConceptName(String name) throws NullValueException,
			EmptyStringException;

	/**
	 * Returns all ConceptNames contained in the list of ConceptNames.
	 * 
	 * @return all ConceptNames as Set<ConceptName>
	 */
	public Set<ConceptName> getConceptNames();

	/**
	 * Returns the description of this instance of AbstractConcept.
	 * 
	 * @return description String
	 */
	public String getDescription();

	/**
	 * Returns the DataSource, which this AbstractConcept belongs to.
	 * 
	 * @return this controlled vocabulary DataSource
	 */
	public DataSource getElementOf();

	/**
	 * Returns the parser id of this instance of AbstractConcept.
	 * 
	 * @return parser id String
	 */
	public String getPID();

	/**
	 * Sets the parer id of this concept.
	 * 
	 * @param pid
	 *            the new PID
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public void setPID(String pid) throws UnsupportedOperationException;

	/**
	 * Sets the annotation of this instance of AbstractConcept.
	 * 
	 * @param annotation
	 *            the annotation to set.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public void setAnnotation(String annotation)
			throws UnsupportedOperationException;

	/**
	 * Sets the description of this instance of AbstractConcept.
	 * 
	 * @param description
	 *            the description to set
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public void setDescription(String description)
			throws UnsupportedOperationException;

}