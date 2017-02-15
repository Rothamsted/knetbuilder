package net.sourceforge.ondex.export.specificity;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.StringArgumentDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hindlem
 */
public class ThresholdArgumentDefinition extends StringArgumentDefinition {

    private static Map<String, ThresholdType> params;

    /**
     * Definitions of ThresholdTypes for determining max normalization range for specificity
     *
     * @author hindlem
     */
    public static enum ThresholdType {
        COUNT, EXCLUDING_TOP_PERCENT, STDEVS_ABOVE_MEAN, STDEVS_BELOW_MEAN, ALL;
    }

    static {
        params = new HashMap<String, ThresholdType>();
        params.put("count", ThresholdType.COUNT);
        params.put("exclude_top_percent", ThresholdType.EXCLUDING_TOP_PERCENT);
        params.put("stdevs_above_mean", ThresholdType.STDEVS_ABOVE_MEAN);
        params.put("stdevs_below_mean", ThresholdType.STDEVS_BELOW_MEAN);
        params.put("all", ThresholdType.ALL);
    }

    /**
     * @param name
     * @param description
     * @param required
     * @param defaultValue
     * @param multipleInstancesAllowed
     */
    public ThresholdArgumentDefinition(String name, String description,
                                       boolean required, String defaultValue,
                                       boolean multipleInstancesAllowed) {
        super(name, description, required, defaultValue, multipleInstancesAllowed);
    }

    @Override
    public void isValidArgument(Object obj) throws InvalidPluginArgumentException {
        translateType(obj.toString());
        super.isValidArgument(obj);
    }

    /**
     * @param value
     * @return
     * @throws IllegalArgumentException
     */
    public static ThresholdType translateType(String value) throws InvalidPluginArgumentException {
        ThresholdType type = params.get(value.toLowerCase());
        if (type == null) throw new InvalidPluginArgumentException(value + " is not a valid threshold type argument");
        return type;
    }

    /**
     * @return paramiters for types of specificity annotation
     */
    public static String[] getTypes() {
        return params.keySet().toArray(new String[params.keySet().size()]);
	}
}
