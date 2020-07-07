package net.sourceforge.ondex.tools.threading.monitoring;


/**
 * This class is a simple adapter for monitorable processes that are 
 * indeterminate concerning their time consumption.
 * According to the adapter design pattern this should always be instanciated
 * as a anonymous class overriding the <code>task()</code> method (which should
 * then contain the actual work).
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public abstract class IndeterminateProcessAdapter implements Monitorable {

	//####FIELDS####
	
	/**
	 * The process's state.
	 */
	private String state;
	
	/**
	 * The current progress value.
	 */
	private int progress;
	
	private Throwable uncaught = null;

	//####CONSTRUCTOR####
	
	/**
	 * The constructor.
	 */
	public IndeterminateProcessAdapter() {
		state =  Monitorable.STATE_IDLE;
		progress = 0;
	}

	//####METHODS####

	/**
	 * the process is indeterminate, so it just returns an 
	 * arbitrary value greater than 0.
	 */
	@Override
	public int getMaxProgress() {
		return 100;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.tools.threading.monitoring.Monitorable#getMinProgress()
	 */
	@Override
	public int getMinProgress() {
		return 0;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.tools.threading.monitoring.Monitorable#getProgress()
	 */
	@Override
	public int getProgress() {
		return progress;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.tools.threading.monitoring.Monitorable#getState()
	 */
	@Override
	public String getState() {
		return state;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.tools.threading.monitoring.Monitorable#isIndeterminate()
	 */
	@Override
	public boolean isIndeterminate() {
		return true;
	}

	/**
	 * Cancelling an indeterminate progress would be quite fatal. Hence
	 * this does nothing.
	 * 
	 * @see net.sourceforge.ondex.tools.threading.monitoring.Monitorable#setCancelled(boolean)
	 */
	@Override
	public void setCancelled(boolean c) {
		//do nothing
	}
	
	/**
	 * to be overridden. should later contain the actual process's work.
	 */
	public abstract void task();
	
	/**
	 * runs the task in a second thread and manages the states and progress report.
	 */
	public void start() {
		Thread t = new Thread ("Indeterminate Process - Timestamp "+System.currentTimeMillis()) {
			public void run() {
				state = "running...";
				try {
					task();
				} catch (Throwable t) {
					uncaught = t;
				}
				state = Monitorable.STATE_TERMINAL;
				progress = getMaxProgress();
			}
		};
		t.start();
	}
	
	public Throwable getUncaughtException() {
		return uncaught;
	}
	
	public boolean isAbortable() {
		return false;
	}

}
