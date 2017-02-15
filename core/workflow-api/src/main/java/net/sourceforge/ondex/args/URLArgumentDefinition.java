package net.sourceforge.ondex.args;

import net.sourceforge.ondex.InvalidPluginArgumentException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * ArgumentDefinition for a string value representing a URL.
 *
 * @author hindlem
 */
public class URLArgumentDefinition extends AbstractArgumentDefinition<String>
        implements ArgumentDefinition<String> {

    /**
     * Constructor which fills most internal fields and sets multiple instances
     * to false.
     *
     * @param name        String
     * @param description String
     * @param required    boolean
     */
    public URLArgumentDefinition(String name, String description,
                                 boolean required) {
        super(name, description, required, false);
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
        return null;
    }

    @Override
    public void isValidArgument(Object obj) throws InvalidPluginArgumentException {
        if (obj instanceof String) {
            try {
                new URL((String) obj);
            } catch (MalformedURLException e) {
                throw new InvalidPluginArgumentException(e.getMessage());
            }
        }
        throw new InvalidPluginArgumentException("A URL argument is required to be specified as a String for " + getName());

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
