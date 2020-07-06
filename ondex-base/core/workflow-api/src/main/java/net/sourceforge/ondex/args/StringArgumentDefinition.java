package net.sourceforge.ondex.args;

import net.sourceforge.ondex.InvalidPluginArgumentException;

/**
 * ArgumentDefinition for a string value.
 *
 * @author hindlem
 */
public class StringArgumentDefinition extends
        AbstractArgumentDefinition<String> implements
        ArgumentDefinition<String> {

    // default string
    private String defaultValue = "";

    /**
     * Constructor which fills all internal fields.
     *
     * @param name                     String
     * @param description              String
     * @param required                 boolean
     * @param defaultValue             the default value
     * @param multipleInstancesAllowed boolean
     */
    public StringArgumentDefinition(String name, String description,
                                    boolean required, String defaultValue,
                                    boolean multipleInstancesAllowed) {
        super(name, description, required, multipleInstancesAllowed);
        this.defaultValue = defaultValue;
    }

    /**
     * Returns associated java class.
     *
     * @return Class
     */
    public Class<String> getClassType() {
        return String.class;
    }

    /**
     * Returns default value.
     *
     * @return String
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Checks for valid argument.
     *
     * @return boolean
     */
    public void isValidArgument(Object obj) throws InvalidPluginArgumentException {
        if (obj instanceof String) {
            if (((String) obj).length() > 0) {
                return;
            } else if (getDefaultValue() != null && ((String) obj).equalsIgnoreCase(defaultValue)) {
                // Only accept "" strings if default is like that.
                return;
            }
            throw new InvalidPluginArgumentException("The argument " + this.getName() + " is empty");
        }
        throw new InvalidPluginArgumentException("A " + this.getName() + " argument is required to be specified as a String for " + getName());
    }

    /**
     * Parses argument object from String.
     *
     * @param argument String
     * @return String
     */
    public String parseString(String argument) throws Exception {
		return argument;
	}
}
