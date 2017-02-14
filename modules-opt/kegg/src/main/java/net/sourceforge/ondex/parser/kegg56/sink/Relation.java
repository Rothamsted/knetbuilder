/*
 * Created on 13-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.sink;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import java.util.HashSet;
import java.util.Set;

/**
 * Prototype for a relation
 * 
 * @author taubertj
 */
@Entity
public class Relation implements Comparable<Relation> {

	private static final long serialVersionUID = 4861767076901200748L;

	private String conceptIdFrom; // required

	private String conceptIdTo; // required

	// concept IDs for associated context
	private Set<String> keggConceptIdContext = new HashSet<String>();

	@PrimaryKey
	public String pk;

	private String relationTypeId; // required

	/**
	 * Needed by Berkeley based relation cache
	 */
	@SuppressWarnings("unused")
	private Relation() {
	}

	/**
	 * Create a new binary Relation for KEGG internal use.
	 * 
	 * @param from_concept
	 *            id of from concept
	 * @param to_concept
	 *            id of to concept
	 * @param of_type
	 *            id of relation type
	 */
	public Relation(String from_concept, String to_concept, String of_type) {
		if (from_concept == null)
			throw new NullPointerException("from_concept is null");
		if (to_concept == null)
			throw new NullPointerException("to_concept is null");
		if (of_type == null)
			throw new NullPointerException("of_type is null");

		this.conceptIdFrom = from_concept.toUpperCase();
		this.conceptIdTo = to_concept.toUpperCase();
		this.relationTypeId = of_type.intern();
		this.pk = this.conceptIdTo + this.conceptIdFrom + this.relationTypeId;
	}

	/**
	 * Adds another concept id to this relations context list
	 * 
	 * @param ondexConceptId
	 *            concept id
	 */
	public void addContext(String keggConceptId) {
		keggConceptIdContext.add(keggConceptId);
	}

	@Override
	public int compareTo(Relation r) {
		return this.pk.compareTo(r.pk);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Relation) {
			Relation r = (Relation) o;
			return this.pk.equals(r.getID());
		}
		return false;
	}

	/**
	 * Returns associated context list
	 * 
	 * @return Set<String>
	 */
	public Set<String> getContext() {
		return keggConceptIdContext;
	}

	/**
	 * Returns id of from concept
	 * 
	 * @return String
	 */
	public String getFrom_concept() {
		return conceptIdFrom;
	}

	/**
	 * Returns relation primary key
	 * 
	 * @return String
	 */
	public String getID() {
		return pk;
	}

	/**
	 * Returns type of relation
	 * 
	 * @return String
	 */
	public String getOf_type() {
		return relationTypeId;
	}

	/**
	 * Returns id of to concept
	 * 
	 * @return String
	 */
	public String getTo_concept() {
		return conceptIdTo;
	}

	@Override
	public int hashCode() {
		return pk.hashCode();
	}

	@Override
	public String toString() {
		return "Relation: " + this.conceptIdTo + " " + this.conceptIdFrom + " "
				+ this.relationTypeId;
	}
}
