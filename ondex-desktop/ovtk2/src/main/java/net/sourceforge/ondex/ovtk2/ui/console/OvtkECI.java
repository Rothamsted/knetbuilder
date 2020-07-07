package net.sourceforge.ondex.ovtk2.ui.console;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.ondex.scripting.CommandCompletenessStrategy;
import net.sourceforge.ondex.scripting.EnvironmentCommandInterpreter;
import net.sourceforge.ondex.scripting.FunctionException;
import net.sourceforge.ondex.scripting.InterpretationController;
import net.sourceforge.ondex.scripting.OutputPrinter;
import net.sourceforge.ondex.scripting.ProcessingCheckpoint;
import net.sourceforge.ondex.scripting.ProxyTemplate;
import net.sourceforge.ondex.scripting.javascript.JSInterpreter;
import net.sourceforge.ondex.scripting.sparql.SPARQLInterpreter;

/**
 * This class controls the configuration of the console and which of the avaible
 * interpreter should be used: e.g. Javascript, Jython, R.
 * 
 * @author lysenkoa
 */
public class OvtkECI extends EnvironmentCommandInterpreter {
	public final InterpretationController ic;
	private static final boolean DEBUG = true;

	public OvtkECI(InterpretationController ic) {
		this.ic = ic;
	}

	@Override
	public String process(String command, OutputPrinter out) {

		if (command.equals("help;")) {
			try {
				java.awt.Desktop.getDesktop().browse(new URI("Scripting_ref.htm"));
			} catch (Exception e) {
				out.printAndPrompt("\nThe help documentation was not found!");
			}
			return null;
		}
		// else
		// if(command.equalsIgnoreCase("R_MODE;")||command.equalsIgnoreCase("R_MODE")){
		// try {
		// out.print("Changing configuaration - please wait...");
		// ic.setInterpreterOrder(this.getClass(), RInterpreter.class);
		// out.printAndPrompt(ic.getWelcomeMessage());
		// return null;
		// } catch (FunctionException e) {
		// out.printAndPrompt(e.getMessage());
		// return null;
		// }
		// }
		else if (command.trim().equalsIgnoreCase("JS_MODE;") || command.trim().equalsIgnoreCase("JS_MODE")) {
			try {
				out.print("\nChanging configuaration - please wait...");
				ic.setInterpreterOrder(this.getClass(), JSInterpreter.class);
				out.printAndPrompt("\n" + ic.getWelcomeMessage());
				out.setDefaultCommnadCompletenessStrategy();
				return null;
			} catch (FunctionException e) {
				out.printAndPrompt(e.getMessage());
				return null;
			}

		} else if (command.trim().equalsIgnoreCase("SPARQL_MODE;") || command.trim().equalsIgnoreCase("SPARQL_MODE")) {
			try {
				out.print("\nChanging configuaration - please wait...");
				ic.setInterpreterOrder(this.getClass(), SPARQLInterpreter.class);
				out.printAndPrompt("\n" + ic.getWelcomeMessage());
				out.setCommandCompletenessStrategy(new CommandCompletenessStrategy() {
					@Override
					public boolean isComplete(String line) {
						if (line.endsWith("\n")) {
							return true;
						}
						return false;
					}

				});
				return null;
			} catch (FunctionException e) {
				out.printAndPrompt(e.getMessage());
				return null;
			}
		}
		// else
		// if(command.equalsIgnoreCase("GQL_MODE;")||command.equalsIgnoreCase("GQL_MODE")){
		// try {
		// out.print("Changing configuaration - please wait...");
		// ic.setInterpreterOrder(this.getClass(), OndexGQLInterpreter.class,
		// JSInterpreter.class);
		// if(DEBUG){
		// System.err.println("Past init stage.");
		// }
		// out.printAndPrompt(ic.getWelcomeMessage());
		// return null;
		// } catch (FunctionException e) {
		// out.printAndPrompt(e.getMessage());
		// return null;
		// }
		// }

		// else
		// if(command.equalsIgnoreCase("JYTHON_MODE;")||command.equalsIgnoreCase("JYTHON_MODE")){
		// try {
		// out.print("Changing configuaration - please wait...");
		// ic.setInterpreterOrder(this.getClass(), JythonInterpreter.class);
		// out.printAndPrompt(ic.getWelcomeMessage());
		// return null;
		// } catch (FunctionException e) {
		// out.printAndPrompt(e.getMessage());
		// return null;
		// }
		// }
		return command;
	}

	public String getPrompt() {
		return null;
	}

	public String getWelcomeMessage() {
		return null;
	}

	public List<Class<?>> getDependancies() {
		return new ArrayList<Class<?>>();
	}

	@Override
	public void initialize(ProxyTemplate... aspects) {
	}

	@Override
	public boolean isInitialised() {
		return true;
	}

	@Override
	public void setProcessingCheckpoint(ProcessingCheckpoint pc) {
		// TODO Auto-generated method stub

	}
}
