package net.sourceforge.ondex.scripting.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.event.EventListenerList;

import net.sourceforge.ondex.scripting.CommandCompletenessStrategy;
import net.sourceforge.ondex.scripting.FunctionException;
import net.sourceforge.ondex.scripting.OutputPrinter;

public class ScriptingShell implements OutputPrinter {
	private boolean exit = false;
	private String prompt;
	private EventListenerList listenerList;
	
	public ScriptingShell(String prompt, String welcomeMsg){
		super();
		this.prompt = prompt;
		System.out.println(welcomeMsg);
	}

	public void activae(){
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		boolean hitEOF = false;
		do {
			System.out.print(prompt+" ");
			System.out.flush();
			try {
				String source = "";
				while(true) {
					String newline;
					newline = in.readLine();
					if (newline == null) {
						hitEOF = true;
						break;
					}
					source = source + newline + "\n";
				}
				if(source.equals("exit") || source.equals("quit"))
					exit = true;
				else
					fireCommandEvent(new CommandEvent(this, source, this));
			}
			catch (IOException ioe) {
				System.out.println(ioe.toString());
			}
			if (exit) {
				break;
			}
		} while (!hitEOF);
		System.err.println();
	}
	
	/**
	 * 
	 * @param listener
	 */
    public void addCommandListener(CommandListener listener) {
    	if(listenerList == null)listenerList = new javax.swing.event.EventListenerList();
        listenerList.add(CommandListener.class, listener);
    }
    /**
     * 
     * @param listener
     */
    public void removeCommandListener(CommandListener listener) {
    	if(listenerList == null)return;
        listenerList.remove(CommandListener.class, listener);
    }
    /**
     * 
     * @param evt create a command event when the new command is entered
     */
    void fireCommandEvent(CommandEvent evt) {
    	if(listenerList == null)return;
        Object[] listeners = listenerList.getListenerList();
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i]==CommandListener.class) {
            	try{
               		((CommandListener)listeners[i+1]).newCommand(evt);         		
            	}
            	catch(FunctionException e){
            		print(e.getMessage());
            	}
            	catch(Exception e){
            		e.printStackTrace();
            	}
            }
        }
    }
	
	public void print(String output) {
		System.out.println(output);
	}

	public void println(String output) {
		System.out.println(output);
		System.out.println(prompt);
	}
	
	public void printAndPrompt(String output){
		System.out.println(output);
		System.out.println(prompt);
	}
	
	public void prompt(){}

	@Override
	public String print(Object... output) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCommandCompletenessStrategy(CommandCompletenessStrategy ccs) {
		// TODO Auto-generated method stub
	}

	public void setDefaultCommnadCompletenessStrategy() {
		// TODO Auto-generated method stub
	}
}


