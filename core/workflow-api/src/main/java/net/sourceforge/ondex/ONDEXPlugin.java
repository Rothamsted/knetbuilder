package net.sourceforge.ondex;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.init.ArgumentDescription;
import net.sourceforge.ondex.event.ONDEXListener;

import java.util.Collection;

/**
 * Parent class for all ONDEX plugins, e.g. mappings, parser, transformer.
 *
 * @author hindlem
 * belongs to a certain ONDEXPluginArguments type.
 */
public interface ONDEXPlugin {

    /**
     * Returns the internal string identifier of the ONDEX producer - must be unique - which is also used in the name of the XML attribute in workflow files.
     *
     * @return String
     */
    public abstract String getId();

    /**
     * Returns the human readable name of the ONDEX producer.
     *
     * @return String
     */
    public abstract String getName();


    /**
     * Returns the version number of the ONDEX producer.
     *
     * @return String
     */
    public abstract String getVersion();

    /**
     * Returns arguments valid for this producer
     *
     * @return ArgumentDefinition<?>[]
     */
    public abstract ArgumentDefinition<?>[] getArgumentDefinitions();

    /**
     * Starts the producer process.
     */
    public abstract void start() throws Exception;

    /**
     * Sets the arguments the producer should use.
     *
     * @param args E
     */
    public abstract void setArguments(ONDEXPluginArguments args) throws InvalidPluginArgumentException;

    /**
     * Returns the actual arguments.
     *
     * @return E
     */
    public abstract ONDEXPluginArguments getArguments();

    /**
     * Does this producer require an indexed graph
     *
     * @return boolean
     */
    public abstract boolean requiresIndexedGraph();

    /**
     * Returns a list of Validator names that the parser relies on.
     *
     * @return String[]
     */
    public abstract String[] requiresValidators();

    /**
     * This method can return the custom PluginDescription definition. May return null, in which case the definition will be generated
     * from doclet annotation, as per default behaviour.
     *
     * @return PluginDescription, describing this producer.
     * @param position
     */
    public abstract Collection<ArgumentDescription> getArgumentDescriptions(int position);

    public void addONDEXListener(ONDEXListener l);

    public void removeONDEXListener(ONDEXListener l);

    public ONDEXListener[] getONDEXListeners();
}
