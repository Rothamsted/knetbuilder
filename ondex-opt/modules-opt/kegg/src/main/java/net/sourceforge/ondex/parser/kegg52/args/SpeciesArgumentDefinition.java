package net.sourceforge.ondex.parser.kegg52.args;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.NonContinuousArgumentDefinition;


public class SpeciesArgumentDefinition implements NonContinuousArgumentDefinition<String> {

    public static final String ALL = "all";
    private String description = "Use this parameter to specify the species to be loaded from the kegg database.";

    public SpeciesArgumentDefinition() {

    }

    public String[] getValidValues() {
        return new String[]{"all"};
    }

    public Class<String> getClassType() {
        return String.class;
    }

    public String getDefaultValue() {
        return "all";
    }

    public String getName() {
        return ArgumentNames.SPECIES_ARG;
    }

    @Override
    public void isValidArgument(Object obj) throws InvalidPluginArgumentException {
        if (obj instanceof String) {
            return;
        }
        throw new InvalidPluginArgumentException("A " + this.getName() + " argument is required to be specified as a String");
    }

    public boolean isRequiredArgument() {
        return true;
    }

    public boolean isAllowedMultipleInstances() {
        return true;
    }

    public String parseString(String argument) throws Exception {
        return argument;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
