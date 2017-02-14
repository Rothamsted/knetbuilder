package net.sourceforge.ondex.parser.ec.data;

/**
 * Class to catch existing relations between entries.
 * 
 * @author taubertj
 *
 */
public class Relation {

	private final String from;
	private final String to;
	private final String type;
	
	/**
	 * 
	 * @param from the id of the Entry this refers to
	 * @param to the id of the Entry this refers to
	 * @param relationType the id in the metadata for the relationtype
	 */
	public Relation(String from, String to, String relationType) {
		this.from = from;
		this.to = to;
		this.type = relationType;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public String getRelationType() {
		return type;
	}
	
}
