package net.sourceforge.ondex.args;

import net.sourceforge.ondex.InvalidPluginArgumentException;

/**
 * ArgumentDefinition for a float within a range.
 *
 * @author hindlem
 */
public class FloatRangeArgumentDefinition extends
        AbstractArgumentDefinition<Float> implements
        NumericalArgumentDefinition<Float> {

    // valid range
    private Range<Float> range;

    // default float
    private float defaultValue = 0f;

    /**
     * Constructor which fills most internal fields and sets multiple instances
     * to false.
     *
     * @param name         String
     * @param required     boolean
     * @param defaultValue the default value
     * @param lowerlimit   the minimum float allowed e.g. -5.0f
     * @param upperlimit   the maximum float allowed e.g. Float.MAX
     */
    public FloatRangeArgumentDefinition(String name, String description,
                                        boolean required, float defaultValue, float lowerlimit,
                                        float upperlimit) {
        super(name, description, required, false);
        this.defaultValue = defaultValue;
        range = new Range<Float>(lowerlimit, upperlimit);
    }

    /**
     * Returns range of valid values.
     *
     * @return Range<Float>
     */
    public Range<Float> getValueRange() {
        return range;
    }

    /**
     * Returns associated java class.
     *
     * @return Class
     */
    public Class<Float> getClassType() {
        return Float.class;
    }

    /**
     * Returns default value.
     *
     * @return Float
     */
    public Float getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void isValidArgument(Object obj) throws InvalidPluginArgumentException {
        if (obj instanceof Float) {
            Float value = ((Float) obj);
            if (value >= range.getLowerLimit()
                    && value <= range.getUpperLimit()) {
                return;
            }
            throw new InvalidPluginArgumentException(obj + " is not in the require range  between " + range.getLowerLimit() + " and " + range.getUpperLimit() + "for " + this.getName());
        }
        throw new InvalidPluginArgumentException(obj.getClass().getName() + " was specified where only " + Float.class.getName() + " is permitted for " + this.getName());
    }

    /**
     * Parses argument object from String.
     *
     * @param argument String
     * @return Float
     */
    public Float parseString(String argument) {
        return Float.parseFloat(argument);
    }

}
