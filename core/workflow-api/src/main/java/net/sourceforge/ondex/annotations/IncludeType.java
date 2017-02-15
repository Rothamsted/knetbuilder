package net.sourceforge.ondex.annotations;

/**
 * Holds the types of include value for plugins
 * @author Christian
 *
 */
public enum IncludeType {
	NEVER("Never"),
	ALWAYS("Always"),
	CONDITIONAL("Conditional");
	
	private String value;
	
	private IncludeType(String value) {
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
