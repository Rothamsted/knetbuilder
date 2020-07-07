package net.sourceforge.ondex.scripting;

import net.sourceforge.ondex.scripting.ui.CommandListener;


/**
 * Controller that runs the command through a stack of interpreters
 * @author lysenkoa
 *
 */
public interface InterpretationController extends CommandListener {

	public void addInterpreter(CommandInterpreter interpreter);
	public void removeInterpreter(CommandInterpreter interpreter);
	public void addProxyTemplate(ProxyTemplate proxyTemplate);
	public void removeProxyTemplate(ProxyTemplate proxyTemplate);
	public void addProxyTemplateBuilder(TemplateBuilder<?> proxyTemplateBuilder);
	public void removeProxyTemplateBuilder(TemplateBuilder<?> proxyTemplateBuilder);


	public String getPrompt();
	/**
	 * Get current welcome message
	 * @return
	 */
	public String getWelcomeMessage();
	/**
	 * Method to change what interpreters are used in the interpretation controller
	 * @param interpreterClasses - classes of the interpreters that will for the new stack (with the order specified)
	 * @throws FunctionException
	 */
	public void setInterpreterOrder(Class<?> ... interpreterClasses) throws FunctionException;
}
