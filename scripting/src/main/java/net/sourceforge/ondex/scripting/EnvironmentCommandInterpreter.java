package net.sourceforge.ondex.scripting;

/**
 * 
 * The interpreter class that extends this class is guaranteed to be the first on
 * the interpreter stack and can be used to define some implementation-free commands
 * that should always be availalbe.
 * @author lysenkoa
 *
 */
public abstract class EnvironmentCommandInterpreter implements CommandInterpreter {
	/**
	 * Subclasses override this method for to get the desired behaviour;
	 */
	public String process(String command, OutputPrinter out) {
		return command;	
	}
	/**
	 * @see CommandInterpreter
	 */
	public abstract String getPrompt();
	/**
	 * @see CommandInterpreter
	 */
	public abstract String getWelcomeMessage();
	/**
	 * Convenience method checking whether interpreter is a descendant of this class
	 * @param interpreter to verify
	 * @return true if it is, false if it is not
	 */
	public static final boolean isPrimary(CommandInterpreter interpreter){
		return EnvironmentCommandInterpreter.class.isAssignableFrom(interpreter.getClass());
	}
}
