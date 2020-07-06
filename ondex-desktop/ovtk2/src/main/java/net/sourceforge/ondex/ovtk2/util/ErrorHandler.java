package net.sourceforge.ondex.ovtk2.util;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * @author jweile
 * 
 */
public class ErrorHandler implements UncaughtExceptionHandler {

	/**
	 * singleton field
	 */
	private static ErrorHandler instance = null;

	/**
	 * singleton private constructor
	 */
	private ErrorHandler() {

	}

	/**
	 * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread,
	 *      java.lang.Throwable)
	 */
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		ErrorDialog.show(true, e, t);
	}

	/**
	 * singleton getter
	 * 
	 * @return
	 */
	public static ErrorHandler getInstance() {
		if (instance == null) {
			instance = new ErrorHandler();
		}
		return instance;
	}

	/**
	 * enforces error handling through the ErrorDialog on every running thread.
	 */
	public static void enforceErrorHandling() {
		// Find the root thread group
		ThreadGroup root = Thread.currentThread().getThreadGroup().getParent();
		while (root.getParent() != null) {
			root = root.getParent();
		}
		// Visit each thread group
		visitThread(root, 0);
	}

	/**
	 * This method recursively visits all thread groups under `group'. It then
	 * adds the ErrorHandler to each thread it finds.
	 */
	private static void visitThread(ThreadGroup group, int level) {
		// Get threads in `group'
		int numThreads = group.activeCount();
		Thread[] threads = new Thread[numThreads * 2];
		numThreads = group.enumerate(threads, false);

		// Enumerate each thread in `group'
		for (int i = 0; i < numThreads; i++) {
			// Get thread
			Thread thread = threads[i];
			thread.setUncaughtExceptionHandler(getInstance());
		}

		// Get thread subgroups of `group'
		int numGroups = group.activeGroupCount();
		ThreadGroup[] groups = new ThreadGroup[numGroups * 2];
		numGroups = group.enumerate(groups, false);

		// Recursively visit each subgroup
		for (int i = 0; i < numGroups; i++) {
			visitThread(groups[i], level + 1);
		}
	}

}
