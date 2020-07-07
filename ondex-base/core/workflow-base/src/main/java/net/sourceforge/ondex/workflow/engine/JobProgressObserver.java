package net.sourceforge.ondex.workflow.engine;

/**
 * @author lysenkoa
 */
public interface JobProgressObserver {
    public void notifyComplete(JobExecutor je);

    public void notifyStageError(Object callbackRef);

    public void notifyStageStarted(Object callbackRef);

    public void notifyStageComplete(Object callbackRef);
}