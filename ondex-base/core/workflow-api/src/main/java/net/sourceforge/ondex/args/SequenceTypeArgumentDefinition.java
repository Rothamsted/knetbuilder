package net.sourceforge.ondex.args;

import net.sourceforge.ondex.InvalidPluginArgumentException;

/**
 * Extension of StringArgumentDefinition for types of sequences (AA or NA).
 *
 * @author hindelm
 */
public class SequenceTypeArgumentDefinition extends StringArgumentDefinition {

    /**
     * Constructor sets description per default and multiple instances as false.
     *
     * @param name         String
     * @param required     boolean
     * @param defaultValue String
     */
    public SequenceTypeArgumentDefinition(String name, boolean required,
                                          String defaultValue) {
        super(
                name,
                "Sequence type must inherit from either Amino Acid (AA) or Nucleic Acid (NA)",
                required, defaultValue, false);
    }

    @Override
    public void isValidArgument(Object obj) throws InvalidPluginArgumentException {
        if (obj instanceof String) {
            String sequence = ((String) obj);
            if (sequence.trim().length() > 0) {
                return;
            } else if (getDefaultValue() != null && ((String) obj).equalsIgnoreCase(getDefaultValue())) {
                // Only accept "" strings if default is like that.
                return;
            }
            throw new InvalidPluginArgumentException("The " + this.getName() + " argument is empty");
        }
        throw new InvalidPluginArgumentException("A " + this.getName() + " argument is required to be specified as a String");
	}

}
