package net.sourceforge.ondex.tools.tab.exporter;

	/**
 * Enum for the type of label to show for a concept
 * 
 * @author hindlem
 * 
 */
public enum Label {
	NAME, ACCESSION, PID, ID;

	public static Label translate(String userdefined) {
		if (userdefined.equalsIgnoreCase("name")) {
			return NAME;
		} else if (userdefined.equalsIgnoreCase("accession")) {
			return ACCESSION;
		} else if (userdefined.equalsIgnoreCase("pid")) {
			return PID;
		} else if (userdefined.equalsIgnoreCase("id")) {
			return ID;
		}
		throw new RuntimeException("User defined label " + userdefined
				+ " is invalid");
	}
}
