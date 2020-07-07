package net.sourceforge.ondex.workflow.engine;

/**
 * @author lysenkoa
 */
public interface JobExecutor extends Runnable {
    public void addObserver(JobProgressObserver je, boolean unregisterWhenComplete);

    public void removeObserver(JobProgressObserver je);

    public void terminate();

    public boolean getErrorState();

    public Exception getException();

    public String getCurrentPluginName();

    public void run();
}