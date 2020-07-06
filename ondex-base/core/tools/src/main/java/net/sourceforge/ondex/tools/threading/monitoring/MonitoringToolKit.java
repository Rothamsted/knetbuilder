package net.sourceforge.ondex.tools.threading.monitoring;

/**
 * Provides convenience methods for progress monitoring.
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class MonitoringToolKit {

	//####FIELDS####

	//####CONSTRUCTOR####

	//####METHODS####
	
	/**
	 * Builds a string containing the time yet to wait for a given monitorable
	 * process. 
	 * @param before time stamp of process start.
	 * @param m the monitorable process.
	 * @return A string holding the time yet to wait.
	 */
	public static String calculateWaitingTime(long before, Monitorable m) {
		int max_progress = m.getMaxProgress();
		int min_progress = m.getMinProgress();
		int progress = m.getProgress();
		String state;
		long time = System.currentTimeMillis() - before;
		double timePerRun = (double)time/(double)(progress - min_progress);
		double runsYetToDo = ((double)(max_progress - progress));
		double timeToWaitInSeconds = (timePerRun * runsYetToDo) / 1000.0;
		int timeToWaitInSecondsInt = (int)Math.ceil(timeToWaitInSeconds);
		int hours = (timeToWaitInSecondsInt) / 3600;
		int minutes = (timeToWaitInSecondsInt % 3600 ) / 60;
		int seconds = (timeToWaitInSecondsInt % 3600 ) % 60;
		if (hours == 0) {
			if (minutes == 0)
				state = "Wait:  "+seconds+" s";
			else
				state = "Wait:  "+minutes+ " min";
		}
		else
			state = "Wait:  "+hours+" h";
		return state;
	}
}
