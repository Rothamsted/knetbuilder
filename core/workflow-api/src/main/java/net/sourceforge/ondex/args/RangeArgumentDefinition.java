package net.sourceforge.ondex.args;

import net.sourceforge.ondex.InvalidPluginArgumentException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ArgumentDefinition for a Number within a range. Works with AtomicInteger, AtomicLong, BigDecimal, BigInteger, Byte, Double, Float, Integer, Long, and Short.
 *
 * @author hindlem
 */
public class RangeArgumentDefinition<R extends Number> extends
        AbstractArgumentDefinition<R> implements
        NumericalArgumentDefinition<R> {

    // valid range
    private Range<R> range;

    // default float
    private R defaultValue = null;

    private Class<R> classType;

    /**
     * Constructor which fills most internal fields and sets multiple instances
     * to false.
     *
     * @param name         String
     * @param required     boolean
     * @param defaultValue the default value
     * @param lowerlimit   the minimum float allowed e.g. -5.0f
     * @param upperlimit   the maximum float allowed e.g. Float.MAX
     * @param classType    the numerical class represented in this range
     */
    public RangeArgumentDefinition(String name, String description,
                                   boolean required, R defaultValue, R lowerlimit,
                                   R upperlimit, Class<R> classType) {
        this(name, description, required, defaultValue, lowerlimit, upperlimit, classType, false);
    }

    /**
     * Constructor which fills most internal fields
     *
     * @param name         String
     * @param required     boolean
     * @param defaultValue the default value
     * @param lowerlimit   the minimum float allowed e.g. -5.0f
     * @param upperlimit   the maximum float allowed e.g. Float.MAX
     * @param classType    the numerical class represented in this range
     * @param multiple     allow multiple instances of this parameter
     */
    public RangeArgumentDefinition(String name, String description,
                                   boolean required, R defaultValue, R lowerlimit,
                                   R upperlimit, Class<R> classType, boolean multiple) {
        super(name, description, required, multiple);
        this.defaultValue = defaultValue;
        range = new Range<R>(lowerlimit, upperlimit);
        this.classType = classType;
    }

    /**
     * Returns range of valid values.
     *
     * @return Range<Float>
     */
    public Range<R> getValueRange() {
        return range;
    }

    /**
     * Returns associated java class.
     *
     * @return Class
     */
    public Class<R> getClassType() {
        return classType;
    }

    /**
     * Returns default value.
     *
     * @return R
     */
    public R getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void isValidArgument(Object obj) throws InvalidPluginArgumentException {
        if (classType.isAssignableFrom(obj.getClass())) {
            R value = (R) obj;

            if (value.doubleValue() >= range.getLowerLimit().doubleValue()
                    && value.doubleValue() <= range.getUpperLimit().doubleValue()) {
                return;
            }
            throw new InvalidPluginArgumentException(obj + " is not in the require range  between " + range.getLowerLimit() + " and " + range.getUpperLimit());
        }
        throw new InvalidPluginArgumentException(obj.getClass().getName() + " was specified where only " + classType.getName() + " is permitted");
    }

    /**
     * Parses argument object from String.
     *
     * @param argument String
     * @return R
     */
    @SuppressWarnings("unchecked")
    public R parseString(String argument) {

        if (Float.class.isAssignableFrom(classType)) {
            return (R) Float.valueOf(argument);
        } else if (Double.class.isAssignableFrom(classType)) {
            return (R) Double.valueOf(argument);
        } else if (Integer.class.isAssignableFrom(classType)) {
            return (R) Integer.valueOf(argument);
        } else if (Long.class.isAssignableFrom(classType)) {
            return (R) Long.valueOf(argument);
        } else if (Short.class.isAssignableFrom(classType)) {
            return (R) Short.valueOf(argument);
        } else if (Byte.class.isAssignableFrom(classType)) {
            return (R) Byte.valueOf(argument);
        } else if (AtomicInteger.class.isAssignableFrom(classType)) {
            return (R) new AtomicInteger(Integer.valueOf(argument));
        } else if (AtomicLong.class.isAssignableFrom(classType)) {
            return (R) new AtomicLong(Long.valueOf(argument));
        } else if (BigDecimal.class.isAssignableFrom(classType)) {
            return (R) new BigDecimal(argument);
        } else if (BigInteger.class.isAssignableFrom(classType)) {
            return (R) new BigInteger(argument);
        }

        return (R) Double.valueOf(argument);
    }

}
