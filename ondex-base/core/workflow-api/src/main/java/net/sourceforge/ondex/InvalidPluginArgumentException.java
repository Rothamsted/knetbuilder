package net.sourceforge.ondex;

import net.sourceforge.ondex.exception.type.PluginConfigurationException;

/**
 * Thrown when a method tries to add or get an invlaid argument
 *
 * @author hindlem
 */
public class InvalidPluginArgumentException extends PluginConfigurationException {

    /**
     * @param s
     */
    public InvalidPluginArgumentException(String s) {
        super(s);
    }

}
