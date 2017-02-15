package net.sourceforge.ondex.workflow.validation;

import net.sourceforge.ondex.ONDEXPlugin;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.workflow.events.InvalidArgumentEvent;
import net.sourceforge.ondex.workflow.model.PluginAndArgs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A collection validatory methods for checking producer paramiters
 * check for errors with getErrors()
 *
 * @author hindlem
 * @see #getErrors()
 */
public class ArgumentVerifier {

    //errors colleced during validation
    private List<EventType> errors = new ArrayList<EventType>();

    /**
     * @param initValues
     * @return whether these arguments are valid see getErrors() for details
     */
    public boolean verifyArguments(List<PluginAndArgs> initValues) throws InvalidPluginArgumentException {
        for (PluginAndArgs init : initValues) {
            ONDEXPluginArguments args = init.getArguments();

            ONDEXPlugin plugin = init.getPlugin();
            plugin.setArguments(args); // unchecked

            if (!checkArguments(args.getOptions(), plugin.getArgumentDefinitions(), init.getPlugin().getName())) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the errors thrown by the validation methods of this class
     */
    public List<EventType> getErrors() {
        return errors;
    }

    /**
     * @param options     from the paramiters
     * @param definitions from the parser,intermediate, mapping, export
     * @param callerName
     * @return whether these arguments are valid see getErrors() for details
     */
    private boolean checkArguments(Map<String, List<?>> options, ArgumentDefinition<?>[] definitions, String callerName) {
        boolean result = true;

        for (ArgumentDefinition<?> def : definitions) {
            if (def.isRequiredArgument() && !options.containsKey(def.getName())) {
                errors.add(new InvalidArgumentEvent("Missing required argument " + def.getName() + " [" + callerName + "] -- " + def.getDescription()));
                result = false;
            }
        }

        for (String arg : options.keySet()) {
            List<?> values = options.get(arg);
            for (ArgumentDefinition<?> def : definitions) {
                if (def.getName().equalsIgnoreCase(arg)) {

                    if (values.size() > 1 && !def.isAllowedMultipleInstances()) {
                        errors.add(new InvalidArgumentEvent("Multiple arguments are not allowed for " + arg + " No. Values = " + values.size() + " [" + callerName + "] -- " + def.getDescription()));
                        result = false;
                    }

                    for (Object value : values) {
                        try {
                            def.isValidArgument(value);
                        } catch (InvalidPluginArgumentException e) {
                            errors.add(new InvalidArgumentEvent("The argument (" + value + ") for argument " + arg + " is invalid" + " [" + callerName + "] -- " + def.getDescription() + "\n" + e.getMessage()));
                            result = false;
                        }
                    }

                }
            }
        }
        return result;
    }

}