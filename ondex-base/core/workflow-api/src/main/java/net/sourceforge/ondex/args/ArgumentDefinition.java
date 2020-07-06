package net.sourceforge.ondex.args;

import net.sourceforge.ondex.InvalidPluginArgumentException;

/**
 * Interface that defines the contract for all definitions of arguments.
 *
 * @author hindlem
 */
public interface ArgumentDefinition<E> {

    /**
     * Returns the name of this argument as defined in the ONDEXParameters.xml
     *
     * @return String
     */
    public String getName();

    /**
     * Returns the description of this argument.
     *
     * @return String
     */
    public String getDescription();

    /**
     * Returns whether or not does this parser require this argument.
     *
     * @return boolean
     */
    public boolean isRequiredArgument();

    /**
     * Returns whether or not is the parser allowed to be specified multiple
     * times, i.e. an array.
     *
     * @return boolean
     */
    public boolean isAllowedMultipleInstances();

    /**
     * Returns the java class in which this object should be created.
     *
     * @return Class
     */
    public Class<E> getClassType();

    /**
     * Parses this object from a string, return null if this parameter does not
     * support string initialization.
     *
     * @param argument String
     * @return E
     */
    public E parseString(String argument) throws Exception;

    /**
     * Returns the default value for this argument, null if there is none.
     *
     * @return E
     */
    public E getDefaultValue();

    /**
     * Returns whether or not is this a valid argument.
     *
     * @param obj the argument value to test
     * @throws InvalidPluginArgumentException
     */
    public void isValidArgument(Object obj) throws InvalidPluginArgumentException;

}
