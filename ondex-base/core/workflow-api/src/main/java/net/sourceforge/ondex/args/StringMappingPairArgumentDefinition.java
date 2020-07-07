package net.sourceforge.ondex.args;

import net.sourceforge.ondex.InvalidPluginArgumentException;


/**
 * Extension of StringArgumentDefinition of mapping pairs.
 *
 * @author taubertj
 */
public class StringMappingPairArgumentDefinition extends
        StringArgumentDefinition {

    /**
     * Constructor which fills all internal fields.
     *
     * @param name                     String
     * @param description              String
     * @param required                 boolean
     * @param defaultValue             String
     * @param multipleInstancesAllowed boolean
     */
    public StringMappingPairArgumentDefinition(String name, String description,
                                               boolean required, String defaultValue,
                                               boolean multipleInstancesAllowed) {
        super(name, description, required, defaultValue,
                multipleInstancesAllowed);
    }

    /**
     * Checks for valid argument.
     *
     * @return boolean
     */
    public void isValidArgument(Object obj) throws InvalidPluginArgumentException {
        if (obj instanceof String) {
            String[] pair = ((String) obj).split(",");
            if (pair.length == 2 && pair[0].length() > 0
                    && pair[1].length() > 0) {
                return;
            }
            throw new InvalidPluginArgumentException("The argument for " + getName() + " is required to be specified as a name value pair separated by a \",\" character");

        }
        throw new InvalidPluginArgumentException("The argument is required to be specified as a String for "+getName());
	}
}
