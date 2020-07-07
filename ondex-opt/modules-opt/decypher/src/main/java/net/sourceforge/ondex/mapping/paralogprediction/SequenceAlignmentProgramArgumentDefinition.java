package net.sourceforge.ondex.mapping.paralogprediction;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.NonContinuousArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;

/**
 * Defines the Sequence Alignment Programs valid for this mapping method
 *
 * @author hindlem
 */
public class SequenceAlignmentProgramArgumentDefinition extends StringArgumentDefinition implements NonContinuousArgumentDefinition<String> {

//	public static final String BLAST = "blast";
//	public static final String PATTERN_HUNTER = "patternhunter";
//	public static final String FSA_BLAST = "fsablast";
    public static final String DECYPHER = "decypher";

    public SequenceAlignmentProgramArgumentDefinition(String name, boolean required, String defaultValue) {
        super(name, "The Aligment program to use e.g. blast/patternhunter/fsablast", required, defaultValue, false);
    }

    public String[] getValidValues() {
        return new String[]{DECYPHER};
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
