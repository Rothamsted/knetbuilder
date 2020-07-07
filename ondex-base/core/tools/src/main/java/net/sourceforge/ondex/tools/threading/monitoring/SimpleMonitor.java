package net.sourceforge.ondex.tools.threading.monitoring;

public class SimpleMonitor implements Monitorable {
	
//	private boolean debug;
	
	private int max, p = 0;
	
	private String state;
	
	private boolean cancelled = false;
    private Throwable uncaught = null;
	
	public SimpleMonitor(String state, int max) {
		this.state = state;
		this.max = max;
//		debug = false;
//		debug = Boolean.parseBoolean(Config.properties.getProperty("flags.debug"));
	}
	
	public boolean next(String operation) {
		state = operation;
//		if (debug) {
//			try {
//				long millis = (long)(Math.random() * 1000.0);
//				Thread.sleep(millis);
//			} catch (InterruptedException e) {}
//		}
		p++;
		if (p == getMaxProgress())
			state = Monitorable.STATE_TERMINAL;
		return !cancelled;
	}
	
	public void complete() {
		p = getMaxProgress();
		state = Monitorable.STATE_TERMINAL;
	}
	
	@Override
	public int getMaxProgress() {
		return max;
	}

	@Override
	public int getMinProgress() {
		return 0;
	}

	@Override
	public int getProgress() {
		return p;
	}

	@Override
	public boolean isIndeterminate() {
		return false;
	}

	@Override
	public void setCancelled(boolean c) {
		cancelled = c;
        state = Monitorable.STATE_TERMINAL;
	}

	@Override
	public String getState() {
		return state;
	}

	@Override
	public Throwable getUncaughtException() {
		return uncaught;
	}

    public void failed(Throwable t) {
        uncaught = t;
        state = Monitorable.STATE_TERMINAL;
    }

	@Override
	public boolean isAbortable() {
		return true;
	}
	

}
