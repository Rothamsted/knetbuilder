package net.sourceforge.ondex.scripting;

/**
 * 
 * @author lysenkoa
 * Classes that can interpret commands implement this interface
 */
public interface CommandInterpreter extends ProxyTemplateDependant{
	/**
	 * 
	 * @param command - command
	 * @param out - output printer
	 * @return command after pre-parsing or null if command was executed fully
	 */
	public String process(String command, OutputPrinter out);
	/**
	 * Get prompt string of this interpreter
	 * @return
	 */
	public String getPrompt();
	/**
	 * 
	 * Get welcome message of this interpreter
	 * @return
	 */
	public String getWelcomeMessage();
	/**
	 * Sets the stuff to do before and after the execution of every command
	 * @param pc
	 */
	public void setProcessingCheckpoint(ProcessingCheckpoint pc);
}
