package net.sourceforge.ondex.filter.subgraph;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.NonContinuousArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;

public class RelationDirectionDefinition extends StringArgumentDefinition
        implements NonContinuousArgumentDefinition<String> {

    public static final String INCOMING = "incoming";

    public static final String OUTGOING = "outgoing";

    public static final String BOTH = "both";

    public RelationDirectionDefinition(String name, String description,
                                       boolean required, String defaultValue,
                                       boolean multipleInstancesAllowed) {
        super(name, description, required, defaultValue,
                multipleInstancesAllowed);
    }

    public String[] getValidValues() {
        return new String[]{INCOMING, OUTGOING, BOTH};
    }

    @Override
    public void isValidArgument(Object obj) throws InvalidPluginArgumentException {
        if (obj instanceof String) {
            String sequence = ((String) obj);
            if (sequence.length() > 0) {
                for (Object value : getValidValues()) {
                    if (sequence.equalsIgnoreCase((String) value)) {
                        return;
                    }
                }
                throw new InvalidPluginArgumentException(sequence + " is not a valid argument for " + getName());
            }
            throw new InvalidPluginArgumentException("Value is empty for " + getName());
        }
        throw new InvalidPluginArgumentException("A " + this.getName() + " argument is required to be specified as a String for " + getName());
    }
}
