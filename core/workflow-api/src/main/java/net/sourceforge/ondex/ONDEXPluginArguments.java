package net.sourceforge.ondex;

import net.sourceforge.ondex.args.ArgumentDefinition;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Arguments bundle for plugins.
 *
 * @author hindlem
 * @author Matthew Pocock
 */
public class ONDEXPluginArguments
{

    // indexed option list
    private final Map<String, List<?>> options;
    private final Map<String, ArgumentDefinition<?>> definitions;

    /**
     * Initialize backing data structure.
     */
    public ONDEXPluginArguments(ArgumentDefinition<?>[] definitions) {
        this.options = new HashMap<String, List<?>>();
        this.definitions = new HashMap<String, ArgumentDefinition<?>>();
        for (ArgumentDefinition<?> definition : definitions) {
            this.definitions.put(definition.getName(), definition);
        }
    }

    /**
     * Checks if this is a valid argument name for this PluginArgument
     *
     * @param name the name of the argument
     * @return is this a valid argument name for these arguments
     */
    public boolean hasArgument(String name) {
        return definitions.containsKey(name);
    }

    /**
     * Checks if a value has been assigned for the given argument
     *
     * @param name the name of the argument
     * @return is there at least one value assigned for this argument
     */
    public boolean hasValueForArgument(String name) {
        List<?> list = options.get(name);
        return list != null && list.size() == 0;
    }

    /**
     * Counts the number of values for a given argument
     *
     * @param name the name of the argument
     * @return the number of values set for the argument
     */
    public int countValuesForArgument(String name) {
        List<?> list = options.get(name);
        if (list == null) return 0;
        return list.size();
    }

    /**
     * @param name  String the name of the argument
     * @param value the value to remove
     * @param <A>   Class of value
     * @return if this object was removed
     */
    public <A extends Object> boolean removeOption(String name, A value) {
        List<A> list = (List<A>) options.get(name);
        if (list != null) {
            boolean result = list.remove(value);
            if (list.size() == 0)
                options.remove(list);
            return result;
        }
        return false;
    }

    /**
     * Removes all options for the given argument
     *
     * @param name String the name of the argument
     * @param <A>  the value to remove
     * @return if any objects were removed
     */
    public <A extends Object> boolean removeAllOptions(String name) {
        List<A> list = (List<A>) options.get(name);
        if (list != null) {
            boolean removed = list.size() > 0;
            options.remove(list);
            return removed;
        }
        return false;
    }

    /**
     * Adds the given values for an argument
     *
     * @param name   the name of the argument
     * @param values values to add for argument
     * @param <A>    the class of the values
     * @throws InvalidPluginArgumentException
     */
    public <A extends Object> void addOptions(String name, A[] values) throws InvalidPluginArgumentException {
        for (A value : values)
            addOption(name, value);
    }


    /**
     * Adds the given value for an argument
     *
     * @param name  the name of the argument
     * @param value Object of the expected type or a String replresentation
     */
    public <A extends Object> void addOption(String name, A value) throws InvalidPluginArgumentException {

        ArgumentDefinition<?> definition = definitions.get(name);
        if (definition != null) {
            if (!definition.getClassType().isAssignableFrom(value.getClass())) {
                throw new InvalidPluginArgumentException(name + " can not be assigned a value of type "
                        + value.getClass() + " only values of type "
                        + definition.getClassType() + " are accepted");
            }
        } else {
            throw new InvalidPluginArgumentException(name + " is not a valid argument name");
        }

        if (value == null) {
            throw new InvalidPluginArgumentException("An null value was passed as a parameter for " + name);
        }

        if (value instanceof String) {

            if (value.toString().trim().length() == 0) {
                throw new InvalidPluginArgumentException("An empty value was passed as a parameter for " + name);
            }

            try {
                value = (A) definition.parseString((String) value);
            } catch (Exception e) {
                throw new InvalidPluginArgumentException(e.getMessage());
            }
        }

        definition.isValidArgument(value);

        List<A> list = (List<A>) options.get(name);
        if (list == null) {
            list = new ArrayList<A>(1);
            options.put(name, list);
        }

        if (!definition.isAllowedMultipleInstances() && list.size() > 0) {
            throw new InvalidPluginArgumentException("Only one value for " + name +
                    "  is permitted, remove the current value before assigning a new one.");
        }
        list.add(value);
    }

    /**
     * Removes an option by a given name.
     *
     * @param name String
     * @return List<?>
     */
    public List<?> removeOption(String name) {
        return options.remove(name);
    }

    /**
     * Returns all options as a Map.
     *
     * @return Map<String, List<?>>
     */
    public Map<String, List<?>> getOptions() {
        return options;
    }

    /**
     * Extracts the unique value belonging to the given name from the list of
     * options.
     *
     * @param name String
     * @return Object
     */
    public Object getUniqueValue(String name) throws InvalidPluginArgumentException {
        List<?> values = getObjectValueList(name);

        if (values == null || values.size() == 0) {
            return null;
        }
        if (values.size() == 1) {
            return values.get(0);
        }
        System.err.println("Multiple values for " + name + " found when one was expected : " + values.size() + " using last added value = " + values.get(0));
        return values.get(values.size() - 1);
    }

    /**
     * Extracts the complete list of values belonging to the given name from the
     * list of options.
     *
     * @param name String
     * @return List<Object>
     */
    public List<?> getObjectValueList(String name) throws InvalidPluginArgumentException {
        ArgumentDefinition<?> definition = definitions.get(name);
        if (definition == null) {
            throw new InvalidPluginArgumentException(name + " is not a valid argument name");
        }

        List<?> value = options.get(name);
        if (value == null) {
            return getDefaultArgument(name);
        }
        return value;
    }

    /**
     * Extracts the complete list of values belonging to the given name from the
     * list of options.
     *
     * @param name <code>String</code> name of the property to fetch
     * @param type <code>Class</code> giving the type of the property values
     * @return List<T>  the values as a <code>List</code> of <code>T</code>
     */
    public <T> List<T> getObjectValueList(String name, Class<T> type) throws InvalidPluginArgumentException {
        ArgumentDefinition<?> definition = definitions.get(name);
        if (definition == null) {
            throw new InvalidPluginArgumentException(name + " is not a valid argument name");
        }
        if (!type.isAssignableFrom(definition.getClassType())) {
            throw new InvalidPluginArgumentException(name + " is not assignable to type " + type
                    + " as it is of type " + definition.getClassType());
        }

        List<T> value = (List<T>) options.get(name);
        if (value == null) {
            return getDefaultArgument(name, type);
        }
        return value;
    }

    /**
     * Extracts the values belonging to the given name from the list of options
     * as an array.
     *
     * @param name String
     * @return A[] of the object type specified in the argument definition of name
     */
    public Object[] getObjectValueArray(String name) throws InvalidPluginArgumentException {
        List<?> list = getObjectValueList(name);
        ArgumentDefinition<?> definition = definitions.get(name);
        return list.toArray((Object[]) Array.newInstance(definition.getClassType(), list.size()));
    }

    /**
     * Retrieves default values for arguments where an argument is not set
     */
    private List<?> getDefaultArgument(String argument) throws InvalidPluginArgumentException {

        for (ArgumentDefinition<?> definition : definitions.values()) {
            if (definition.getName().equals(argument)) {

                if (definition.getDefaultValue() != null && definition.getDefaultValue().getClass().isArray()) {
                    return Arrays.asList((Object[]) definition.getDefaultValue());
                } else if (definition.getDefaultValue() != null && List.class.isAssignableFrom(definition.getClassType())) {
                    return (List) definition.getDefaultValue();
                } else {
                    List list = new ArrayList(1);
                    if (definition.getDefaultValue() != null)
                        list.add(definition.getDefaultValue());
                    return list;
                }
            }
        }
        throw new InvalidPluginArgumentException(argument + " is not a valid argument name");
    }

    /**
     * Retrieves default values for arguments where an argument is not set
     */
    private <T> List<T> getDefaultArgument(String argument, Class<T> type) throws InvalidPluginArgumentException {

        for (ArgumentDefinition<?> definition : definitions.values()) {
            if (definition.getName().equals(argument)) {
                if (!type.isAssignableFrom(definition.getClassType())) {
                    throw new InvalidPluginArgumentException(argument + " is not assignable to type " + type
                            + " as it is of type " + definition.getClassType());
                }
                if (definition.getDefaultValue() != null && definition.getDefaultValue().getClass().isArray()) {
                    return Arrays.asList((T[]) definition.getDefaultValue());
                } else if (definition.getDefaultValue() != null && List.class.isAssignableFrom(definition.getClassType())) {
                    return (List<T>) definition.getDefaultValue();
                } else {
                    List<T> list = new ArrayList<T>(1);
                    if (definition.getDefaultValue() != null)
                        list.add((T) definition.getDefaultValue());
                    return list;
                }
            }
        }
        throw new InvalidPluginArgumentException(argument + " is not a valid argument name");
    }

    /**
     * Overwrites all previous values for this argument
     *
     * @param name   the argument name
     * @param values the values to assign to this argument
     */
    public <A extends Object> void setOption(String name, A[] values) throws InvalidPluginArgumentException {
        removeAllOptions(name);
        addOption(name, values);
    }

    /**
     * Overwrites all previous values for this argument
     *
     * @param name  the argument name
     * @param value the value to assign to this option
     */
    public <A extends Object> void setOption(String name, A value) throws InvalidPluginArgumentException {
        removeAllOptions(name);
        addOption(name, value);
    }

}
