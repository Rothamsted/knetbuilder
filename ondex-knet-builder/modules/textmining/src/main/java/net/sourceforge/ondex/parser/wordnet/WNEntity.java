package net.sourceforge.ondex.parser.wordnet;

public class WNEntity extends Entity {
	
	public static final String element_of = "WN";
	
	private String posTag = "";
	
	@Override
	public String getElement_of() {
		return element_of;
	}
	
	public static final String of_type = "Thing";
	
	@Override
	public String getOf_type() {
		return of_type;
	}
	
	public static final String evidence = "IMPD";
	
	@Override
	public String getEvidence() {
		return evidence;
	}
	
	public String getPosTag() {
		return posTag;
	}
	
	public void setPosTag(String p ) {
		posTag = p;
	}
}
