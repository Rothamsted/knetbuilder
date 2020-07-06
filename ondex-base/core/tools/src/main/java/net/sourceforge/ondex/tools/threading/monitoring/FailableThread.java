package net.sourceforge.ondex.tools.threading.monitoring;

/**
 * 
 * @author weilej
 * 
 */
public abstract class FailableThread extends Thread {

	/**
	 * caught exception.
	 */
	private Throwable caught = null;

	public FailableThread() {

	}

	public FailableThread(String name) {
		super(name);
	}

	/**
	 * overrides the threads run method. executes failableRun() and stores any
	 * errors or exceptions.
	 */
	public final void run() {
		try {
			failableRun();
		} catch (Throwable t) {
			caught = t;
		}
	}

	/**
	 * To be overridden. contains the code to be executed in this thread.
	 * 
	 * @throws Throwable
	 */
	public abstract void failableRun() throws Throwable;

	/**
	 * throws the caught error or exception if present.
	 * 
	 * @throws Throwable
	 */
	public void throwCaught() {
		if (caught != null) {
			if (caught instanceof RuntimeException)
				throw (RuntimeException) caught;
			else if (caught instanceof Error)
				throw (Error) caught;
			else
				throw new RuntimeException(caught);
		}
	}

	/**
	 * returns the caught error or exception
	 * 
	 * @return the caught error or exception
	 */
	public Throwable getCaught() {
		return caught;
	}

}
