package net.sourceforge.ondex.validator.scientificspeciesname;

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

	//scientific name
	private String name;

	@PrimaryKey
	private Integer id;

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
	public Entry(Integer id, String name) {
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
	 * @return Integer
	 */
	public Integer getId() {
		return id;
	}
}
