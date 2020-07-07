package net.sourceforge.ondex.parser.kegg53.args;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.NonContinuousArgumentDefinition;


public class SpeciesArgumentDefinition implements NonContinuousArgumentDefinition<String> {

    public static final String ALL = "all";
    private String description = "Use this parameter to specify the species to be loaded from the kegg database, do NOT use \"all\" unless you know you have a server requirements to support this!";

    private final String argument;
    private final String argumentDescription;
    private final boolean required;

    /**
     * @param argument
     * @param argumentDescription
     * @param required
     */
    public SpeciesArgumentDefinition(String argument, String argumentDescription, boolean required) {
        this.argument = argument;
        this.argumentDescription = argumentDescription;
        this.required = required;
    }

    public String[] getValidValues() {
        return new String[]{"all"};
    }

    public Class<String> getClassType() {
        return String.class;
    }

    public String getDefaultValue() {
        return null;
    }

    public String getName() {
        return argument;
    }

    @Override
    public void isValidArgument(Object obj) throws InvalidPluginArgumentException {
        if (obj instanceof String) {
            return;
        }
        throw new InvalidPluginArgumentException("A " + this.getName() + " argument is required to be specified as a String");
    }

    public boolean isRequiredArgument() {
        return required;
    }

    public boolean isAllowedMultipleInstances() {
        return true;
    }

    public String parseString(String argument) throws Exception {
        return argument;
    }

    public String getDescription() {
        return argumentDescription;
    }
}
