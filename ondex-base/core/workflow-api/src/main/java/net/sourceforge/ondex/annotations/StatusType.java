package net.sourceforge.ondex.annotations;

/**
 * Holds the types of status valid for plugins
 * @author hindlem
 *
 */
public enum StatusType {
	DISCONTINUED("Discontinued"), 
	EXPERIMENTAL("Experimental"), 
	STABLE("Stable");
	
	private String value;
	
	private StatusType(String value) {
		this.value = value;
	}
	
	/**
	 * 
	 * @return name of enum object
	 */
    public String getName(){
        return name();
    }

    /**
     * 
     * @return value of enum object "user friendly name"
     */
    public String getValue() {
        return value;
    }
}
