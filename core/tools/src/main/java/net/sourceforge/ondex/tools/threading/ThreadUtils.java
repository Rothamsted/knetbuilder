package net.sourceforge.ondex.tools.threading;

import java.lang.Thread.State;

public class ThreadUtils {

	//####FIELDS####

	//####CONSTRUCTOR####

	//####METHODS####
	
	/**
	 * waits until all given threads have terminated.
	 */
	public static void waitFor(Thread... ts) {
		boolean wait;
		do {
			/*
			 * yield(): Causes the thread to temporarily pause 
			 * and allow other threads to execute. 
			 */
			Thread.yield();
			
			wait = false;
			for (Thread t : ts) {
				wait = wait || !t.getState().equals(State.TERMINATED);
			}
			
		} while (wait);
	}
	
}
