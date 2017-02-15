package net.sourceforge.ondex.tools.threading.monitoring;

/**
 * If implemented, it enables the class to be monitored by the
 * OVTKProgressMonitor.
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public interface Monitorable {
	
	/**
	 * The initial state.
	 */
	public static final String STATE_IDLE = "idle";
	
	/**
	 * The terminal state. this indicates the monitor to close.
	 */
	public static final String STATE_TERMINAL = "done";
	
	/**
	 * Should return the current process value, which must be greater or
	 * equal the minimal progress and less or equal the maximal progress.
	 * Example: If the monitored task consists of a <code>for</code> loop
	 * like for (i = min; i < max; i++) then i would be the current progress,
	 * min would be the minimal progress and max would be the maximal progress.
	 * 
	 * How to report to the monitor: Just update some variables for the state
	 * and the progress. Example:
	 * <code>
	 * private int progress = 0;
	 * private int state = "idle";
	 * 
	 * public void doWork() {
	 * 		state = "working";
	 * 		for (int i = 0; i < max; i++) {
	 * 			calculateSomething();
	 * 			progress++;
	 * 		}
	 * 		state = "done";
	 * }
	 * </code>
	 *  
	 * @return the current progress value.
	 */
	public abstract int getProgress();
	
	/**
	 * returns the current state. initially this should be STATE_IDLE.
	 * if the process has terminated, then it should be STATE_TEMINAL.
	 * in between it can be any string describing what's happening.
	 * Examples: "preprocessing", "running algorithm", "postprocessing"...
	 * 
	 * @return the current state.
	 */
	public abstract String getState();
	
	/**
	 * The maximal progress. See getProgress() for more information.
	 * @return the maximal progress.
	 */
	public abstract int getMaxProgress();
	
	/**
	 * The minimal progress. See getProgress() for more information.
	 * @return the minimal progress.
	 */
	public int getMinProgress();
	
	/**
	 * Sets whether the user has decided to cancel the operation.
	 * @param c The cancellation state.
	 */
	public abstract void setCancelled(boolean c);
	
	/**
	 * Returns whether the process is of indeterminate duration.
	 * @return whether the process is of indeterminate duration.
	 */
	public abstract boolean isIndeterminate();
	
	public abstract boolean isAbortable();
	
	public abstract Throwable getUncaughtException();

}
