/*
 * Created on 25-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.data;

import com.sleepycat.persist.model.Persistent;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author taubertj
 */
@Persistent
public class Reaction implements Serializable {

	/**
	 * Default serial version unique id
	 */
	private static final long serialVersionUID = 1L;

	private Set<Entry> substrates = new HashSet<Entry>();
	private Set<Entry> products = new HashSet<Entry>();

	private String name; // required
	private String type; // required
	private String definition; // required
	private String comment; // required
	private String equation; // required

	private Set<String> names = new HashSet<String>();
	private Set<String> koTerms = new HashSet<String>();
	private Set<String> ecTerms = new HashSet<String>();

	public Reaction(String name, String type) {
		if (name == null)
			throw new NullPointerException("Name is null");
		if (type == null)
			throw new NullPointerException("type is null");
		this.name = name.toUpperCase();
		this.type = type.intern();
	}

	@SuppressWarnings("unused")
	// is required for Berkley Layer
	private Reaction() {
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public Set<Entry> getProducts() {
		return products;
	}

	public Set<Entry> getSubstrates() {
		return substrates;
	}

	public Set<String> getNames() {
		return names;
	}

	public void setNames(Set<String> names) {
		this.names = names;
	}

	public Set<String> getECTerms() {
		return ecTerms;
	}

	public void setECTerms(Set<String> ecTerms) {
		this.ecTerms = ecTerms;
	}
	
	public Set<String> getKoTerms() {
		return koTerms;
	}
	
	public void setKoTerms(Set<String> koTerms) {
		this.koTerms = koTerms;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getEquation() {
		return equation;
	}

	public void setEquation(String equation) {
		this.equation = equation;
	}
}
