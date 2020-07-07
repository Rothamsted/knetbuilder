/*
 * Created on 12-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.sink;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * Prototype for a ONDEX sequence Attribute
 * 
 * @author taubertj
 */
@Entity
public class Sequence {

	private String concept_fk; // required

	@PrimaryKey
	private String id; // required

	private String seq; // the sequence

	private String sequence_type_fk; // required

	@SuppressWarnings("unused")
	// used by Berkeley store
	private Sequence() {
	}

	/**
	 * Constructs an empty sequence with given ID.
	 * 
	 * @param id
	 *            String
	 */
	public Sequence(String id) {
		this.id = id;
	}

	/**
	 * Returns KEGG concept id for associated concept.
	 * 
	 * @return String
	 */
	public String getConcept_fk() {
		return concept_fk;
	}

	/**
	 * Unique id of this sequence.
	 * 
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns sequence itself.
	 * 
	 * @return String
	 */
	public String getSeq() {
		return seq;
	}

	/**
	 * Returns type of sequence, usually AA or NA.
	 * 
	 * @return String
	 */
	public String getSequence_type_fk() {
		return sequence_type_fk;
	}

	/**
	 * Sets KEGG concept id for associated concept.
	 * 
	 * @param concept_fk
	 *            String
	 */
	public void setConcept_fk(String concept_fk) {
		this.concept_fk = concept_fk.intern();
	}

	/**
	 * Sets sequence data.
	 * 
	 * @param seq
	 *            String
	 */
	public void setSeq(String seq) {
		this.seq = seq;
	}

	/**
	 * Sets type of sequence, usually AA or NA.
	 * 
	 * @param sequence_type_fk
	 *            String
	 */
	public void setSequence_type_fk(String sequence_type_fk) {
		this.sequence_type_fk = sequence_type_fk.intern();
	}

	@Override
	public String toString() {
		return "Sequence\n" + "Id: " + id + "\n" + "Concept_fk: " + concept_fk
				+ "\n" + "Sequence_type_fk: " + sequence_type_fk;
	}
}
