package net.sourceforge.ondex.args;

import net.sourceforge.ondex.InvalidPluginArgumentException;

/**
 * ArgumentDefinition for a integer within a range.
 *
 * @author hindlem
 */
public class IntegerRangeArgumentDefinition extends
        AbstractArgumentDefinition<Integer> implements
        NumericalArgumentDefinition<Integer> {

    // valid range
    private Range<Integer> range;

    // default integer
    private int defaultValue = 0;

    /**
     * Constructor which fills most internal fields and sets multiple instances
     * to false.
     *
     * @param name         String
     * @param description  String
     * @param required     boolean
     * @param defaultValue the default value
     * @param lowerlimit   the minimum integer allowed e.g. -5
     * @param upperlimit   the maximum integer allowed e.g. Integer.MAX
     */
    public IntegerRangeArgumentDefinition(String name, String description,
                                          boolean required, Integer defaultValue, int lowerlimit,
                                          int upperlimit) {
        super(name, description, required, false);
        this.defaultValue = defaultValue;
        range = new Range<Integer>(lowerlimit, upperlimit);
    }

    /**
     * Returns range of valid values.
     *
     * @return Range<Integer>
     */
    public Range<Integer> getValueRange() {
        return range;
    }

    /**
     * Returns associated java class.
     *
     * @return Class
     */
    public Class<Integer> getClassType() {
        return Integer.class;
    }

    /**
     * Returns default value.
     *
     * @return Integer
     */
    public Integer getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void isValidArgument(Object obj) throws InvalidPluginArgumentException {
        if (obj instanceof Integer) {
            Integer value = ((Integer) obj);
            if (value >= range.getLowerLimit()
                    && value <= range.getUpperLimit()) {
                return;
            }
            throw new InvalidPluginArgumentException(obj + " is not in the require range  between " + range.getLowerLimit() + " and " + range.getUpperLimit() + " for " + getName());
        }
        throw new InvalidPluginArgumentException(obj.getClass().getName() + " was specified where only " + Integer.class.getName() + " is permitted");
    }

    /**
     * Parses argument object from String.
     *
     * @param argument String
     * @return Integer
     */
    public Integer parseString(String argument) throws Exception {
        return Integer.parseInt(argument);
    }
}
