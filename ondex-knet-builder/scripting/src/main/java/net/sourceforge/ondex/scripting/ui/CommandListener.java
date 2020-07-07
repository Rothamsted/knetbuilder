package net.sourceforge.ondex.scripting.ui;
import java.util.EventListener;

/**
 * Command listener 
 * @author lysenkoa
 * 
 */
public interface CommandListener extends EventListener {
	/**
	 * 
	 * @param evt - command event 
	 * @throws uk.ac.rothamsted.ovtk.ExtensionCore.FunctionException
	 */
	 public void newCommand(CommandEvent evt) throws Exception;
}
