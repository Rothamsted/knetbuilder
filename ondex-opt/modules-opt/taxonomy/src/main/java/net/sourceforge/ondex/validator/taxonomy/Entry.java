package net.sourceforge.ondex.validator.taxonomy;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * Part of the Berkeley db index structure.
 * 
 * @author taubertj
 * 
 */
@Entity
public class Entry {

	// Primary key is name
	@PrimaryKey
	private String name;

	// taxid
	private String id;

	/**
	 * Empty default constructor required.
	 * 
	 */
	public Entry() {

	}

	/**
	 * Constructor to fill up fields.
	 * 
	 * @param name
	 *            String
	 * @param id
	 *            String
	 */
	public Entry(String name, String id) {
		this.name = name;
		this.id = id;
	}

	/**
	 * Set organism name.
	 * 
	 * @param data
	 *            String
	 */
	public void setName(String data) {
		name = data;
	}

	/**
	 * Set taxonomy id.
	 * 
	 * @param data
	 *            String
	 */
	public void setId(String data) {
		id = data;
	}

	/**
	 * Returns organism name.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns taxonomy id.
	 * 
	 * @return String
	 */
	public String getId() {
		return id;
	}
}
