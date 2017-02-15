package net.sourceforge.ondex.scripting;

import net.sourceforge.ondex.scripting.ui.CommandEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Trivial implementation of an InterpretationController
 *
 * @author lysenkoa
 */
public class BasicInterpretationController implements InterpretationController {
    protected List<CommandInterpreter> processingStack = new LinkedList<CommandInterpreter>();
    protected Map<Class<?>, TemplateBuilder<?>> builders = new HashMap<Class<?>, TemplateBuilder<?>>();
    protected Map<Class<?>, ProxyTemplate> proxyTemplates = new HashMap<Class<?>, ProxyTemplate>();
    protected Map<Class<?>, CommandInterpreter> interpreters = new HashMap<Class<?>, CommandInterpreter>();
    private static boolean DEBUG = true;

    public BasicInterpretationController(TemplateBuilder<?>[] builderList, ProxyTemplate[] aspetList, CommandInterpreter[] interpreterList) {
        for (TemplateBuilder<?> ab : builderList)
            if (ab != null)
                builders.put(ab.getProxyTemplateType(), ab);
        for (ProxyTemplate a : aspetList)
            if (a != null)
                proxyTemplates.put(a.getClass(), a);
        for (CommandInterpreter ci : interpreterList)
            if (ci != null)
                interpreters.put(ci.getClass(), ci);
    }

    public BasicInterpretationController(List<TemplateBuilder<?>> builderList, List<ProxyTemplate> aspetList, List<CommandInterpreter> interpreterList) {
        for (TemplateBuilder<?> ab : builderList)
            if (ab != null)
                builders.put(ab.getProxyTemplateType(), ab);
        for (ProxyTemplate a : aspetList)
            if (a != null)
                proxyTemplates.put(a.getClass(), a);
        for (CommandInterpreter ci : interpreterList)
            if (ci != null)
                interpreters.put(ci.getClass(), ci);
    }

    public void newCommand(CommandEvent evt) {
        try {
            OutputPrinter out = evt.getOutputPrinter();
            String command = evt.getCommand();
            for (CommandInterpreter ci : processingStack) {
                command = ci.process(command, out);
                if (command == null || command.equals("")) break;
            }
        }
        catch (Exception e) {
            if (DEBUG)
                e.printStackTrace();
        }
    }

    @Override
    public String getPrompt() {
        if (processingStack.size() > 1) {
            String result = processingStack.get(1).getPrompt();
            if (result != null) return result;
        }
        return "ONDEX>";
    }

    @Override
    public String getWelcomeMessage() {
        if (processingStack.size() > 1) {
            String result = processingStack.get(1).getWelcomeMessage();
            if (result != null) return result;
        }
        return "Command console v1.0 Type 'help;' for more information.";
    }

    /**
     * Method to change what interpreters are used in the interpretation controller
     *
     * @param interpreterClasses - classes of the interpreters that will for the new stack (with the order specified)
     * @throws FunctionException
     */
    public void setInterpreterOrder(Class<?>... interpreterClasses) throws FunctionException {
        List<CommandInterpreter> fallback = new LinkedList<CommandInterpreter>();
        fallback.addAll(processingStack);
        processingStack.clear();
        try {
            for (Class<?> c : interpreterClasses) {
                CommandInterpreter interpreter = interpreters.get(c);
                if (interpreter == null) {

                    throw new FunctionException("Cound not change configuaration - missing required interpreter " + c.getSimpleName() + ".", -4);
                } else if (!interpreter.isInitialised()) {
                    intialiseInterpreter(interpreter);
                }
                processingStack.add(interpreter);
            }
        }
        catch (Exception e) {
            processingStack.clear();
            processingStack.addAll(fallback);
            if (DEBUG)
                e.printStackTrace();
            throw new FunctionException(e.getMessage(), -4);
        }
    }

    private void intialiseInterpreter(CommandInterpreter interpreter) throws FunctionException {
        try {
            List<Class<?>> dependancies = interpreter.getDependancies();
            if (dependancies != null) {
                ProxyTemplate[] proxyTemplatesRequired = new ProxyTemplate[dependancies.size()];
                for (int i = 0; i < dependancies.size(); i++) {
                    proxyTemplatesRequired[i] = proxyTemplates.get(dependancies.get(i));
                    if (proxyTemplatesRequired[i] == null)
                        proxyTemplatesRequired[i] = buildproxyTemplate(dependancies.get(i));
                }
                interpreter.initialize(proxyTemplatesRequired);
            } else {
                interpreter.initialize(new ProxyTemplate[0]);
            }
        }
        catch (Exception e) {
            if (DEBUG)
                e.printStackTrace();
            else throw new FunctionException("could not initialize interpreter: " + e.getMessage(), -1);
        }
    }

    private ProxyTemplate buildproxyTemplate(Class<?> proxyTemplateCls) throws Exception {
        TemplateBuilder<?> ab = builders.get(proxyTemplateCls);
        if (ab == null)
            throw new FunctionException("Cound not change configuaration - missing required proxyTemplate " + proxyTemplateCls.getSimpleName() + ".", -4);
        if (!ab.isInitialised()) {
            List<Class<?>> dependancies = ab.getDependancies();
            ProxyTemplate[] proxyTemplatesRequired = new ProxyTemplate[dependancies.size()];
            for (int i = 0; i < dependancies.size(); i++) {
                proxyTemplatesRequired[i] = proxyTemplates.get(dependancies.get(i));
                if (proxyTemplatesRequired[i] == null) {
                    proxyTemplatesRequired[i] = buildproxyTemplate(dependancies.get(i));
                }
            }
            ab.initialize(proxyTemplatesRequired);
        }
        ProxyTemplate result = ab.getProxyTemplate();
        proxyTemplates.put(proxyTemplateCls, result);
        return result;
    }

    @Override
    public void addProxyTemplate(ProxyTemplate proxyTemplate) {
        proxyTemplates.put(proxyTemplate.getClass(), proxyTemplate);
    }

    @Override
    public void addProxyTemplateBuilder(TemplateBuilder<?> proxyTemplateBuilder) {
        builders.put(proxyTemplateBuilder.getProxyTemplateType(), proxyTemplateBuilder);
    }

    @Override
    public void addInterpreter(CommandInterpreter interpreter) {
        interpreters.put(interpreter.getClass(), interpreter);
    }

    @Override
    public void removeProxyTemplate(ProxyTemplate proxyTemplate) {
        proxyTemplates.remove(proxyTemplate.getClass());
    }

    @Override
    public void removeProxyTemplateBuilder(TemplateBuilder<?> proxyTemplateBuilder) {
        builders.remove(proxyTemplateBuilder.getProxyTemplateType());

    }

    @Override
    public void removeInterpreter(CommandInterpreter interpreter) {
        interpreters.remove(interpreter.getClass());
	}
}
