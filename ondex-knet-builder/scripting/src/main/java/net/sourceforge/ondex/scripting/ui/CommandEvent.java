package net.sourceforge.ondex.scripting.ui;

import java.util.EventObject;

import net.sourceforge.ondex.scripting.OutputPrinter;



/**
 * @author lysenkoa
 * Event that gets created when a user command is entered
 */
public class CommandEvent extends EventObject {
	private static final long serialVersionUID = -4362622543225927822L;
	private String command;
	private OutputPrinter op;

	/**
	 * Constructor method for the event
	 * @param source - where the event is from
	 * @param command - command entered by the user
	 * @param op - where the output messages are to be send
	 */
	public CommandEvent(Object source, String command, OutputPrinter op) {
        super(source);
        this.command = command;
        this.op = op;
    }
	/**
	 * 
	 * @return command entered by the user
	 */
	public String getCommand() {
		return command;
	}
	/**
	 * 
	 * @return the output printer 
	 */
	public OutputPrinter getOutputPrinter(){
		return op;
	}
}