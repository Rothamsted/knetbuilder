package net.sourceforge.ondex.args;

import net.sourceforge.ondex.InvalidPluginArgumentException;

/**
 * ArgumentDefinition for a boolean value.
 *
 * @author hindlem
 */
public class BooleanArgumentDefinition extends
        AbstractArgumentDefinition<Boolean> implements
        NonContinuousArgumentDefinition<Boolean> {

    // default state
    private boolean defaultValue;

    /**
     * Constructor which fills most internal fields and sets multiple instances
     * to false.
     *
     * @param name         String
     * @param description  String
     * @param required     boolean
     * @param defaultValue boolean
     */
    public BooleanArgumentDefinition(String name, String description,
                                     boolean required, boolean defaultValue) {
        super(name, description, required, false);
        this.defaultValue = defaultValue;
    }

    /**
     * Returns list of valid values.
     *
     * @return Boolean[]
     */
    public Boolean[] getValidValues() {
        return new Boolean[]{Boolean.TRUE, Boolean.FALSE};
    }

    /**
     * Returns associated java class.
     *
     * @return Class
     */
    public Class<Boolean> getClassType() {
        return Boolean.class;
    }

    /**
     * Returns default value.
     *
     * @return Boolean
     */
    public Boolean getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void isValidArgument(Object obj) throws InvalidPluginArgumentException {
        if (!(obj instanceof Boolean))
            throw new InvalidPluginArgumentException(obj.getClass().getName() + " was specified only " + Boolean.class.getName() + " (true|false) is permitted for " + this.getName());
    }

    /**
     * Parses argument object from String.
     *
     * @param argument String
     * @return Boolean
     */
    public Boolean parseString(String argument) {
        return Boolean.parseBoolean(argument);
    }

}
