package net.sourceforge.ondex.args;

/**
 * Abstract class that defines the contract for all definitions of arguments.
 *
 * @author hindlem
 */
public abstract class AbstractArgumentDefinition<E> implements ArgumentDefinition<E> {

    // argument name
    private String name = null;

    // argument description
    private String description = null;

    // is it required?
    private boolean required = false;

    // can have multiple instances
    private boolean multiple = false;

    /**
     * Constructor to set shared argument properties.
     *
     * @param name                       String
     * @param description                String
     * @param isRequired                 boolean
     * @param isAllowedMultipleInstances boolean
     */
    public AbstractArgumentDefinition(String name, String description,
                                      boolean isRequired, boolean isAllowedMultipleInstances) {
        this.name = name;
        this.description = description;
        this.required = isRequired;
        this.multiple = isAllowedMultipleInstances;
    }

    /**
     * Returns the name of this argument as defined in the ONDEXParameters.xml
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of this argument.
     *
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns whether or not does this parser require this argument.
     *
     * @return boolean
     */
    public boolean isRequiredArgument() {
        return required;
    }

    /**
     * Returns whether or not is the parser allowed to be specified multiple
     * times, i.e. an array.
     *
     * @return boolean
     */
    public boolean isAllowedMultipleInstances() {
        return multiple;
    }

    /**
     * Returns the java class in which this object should be created.
     *
     * @return Class
     */
    public abstract Class<E> getClassType();

    /**
     * Parses this object from a string, return null if this parameter does not
     * support string initialization.
     *
     * @param argument String
     * @return E
     */
    public abstract E parseString(String argument) throws Exception;

    /**
     * Returns the default value for this argument, null if there is none.
     *
     * @return E
     */
    public abstract E getDefaultValue();

}
