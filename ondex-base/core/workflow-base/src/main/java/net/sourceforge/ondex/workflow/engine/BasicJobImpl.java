package net.sourceforge.ondex.workflow.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import net.sourceforge.ondex.workflow.model.WorkflowTask;

/**
 * Wraps a list of tasks that will be executed in a workflow. Low-level representation
 * that does not retain any original user-readable descriptions.
 *
 * @author lysenkoa
 */
public class BasicJobImpl implements OndexJob, JobExecutor {
    private final Vector<JobProgressObserver> observers = new Vector<JobProgressObserver>();
    private final Vector<Boolean> observerDoRemove = new Vector<Boolean>();
    private final List<Processor> tasks = new ArrayList<Processor>();
    private final ResourcePool pool;
    private volatile String pluginName = "";
    private volatile boolean errorState = false;
    private volatile Exception exception;
    private int index = 0;
    private volatile boolean doTerminate = false;

    public BasicJobImpl(ResourcePool pool) {
        this.pool = pool;
    }

    /* (non-Javadoc)
      * @see net.sourceforge.ondex.workflow.engine.OndexJob#nextTask()
      */

    public Processor nextTask() throws Exception {
        if (!hasNext())
            throw new Exception("No tasks left");
        Processor result = tasks.get(index);
        index++;
        return result;
    }

    /* (non-Javadoc)
      * @see net.sourceforge.ondex.workflow.engine.OndexJob#hasNext()
      */

    public boolean hasNext() {
        return tasks.size() - 1 >= index;
    }

    /* (non-Javadoc)
      * @see net.sourceforge.ondex.workflow.engine.OndexJob#addTask(net.sourceforge.ondex.workflow2.base.AbstractWorkerElement)
      */

    public void addTask(Processor task) {
        tasks.add(task);
    }

    /* (non-Javadoc)
      * @see net.sourceforge.ondex.workflow.engine.OndexJob#removeTask(net.sourceforge.ondex.workflow2.base.AbstractWorkerElement)
      */

    public void removeTask(Processor task) {
        tasks.remove(task);
    }

    @Override
    public synchronized void addObserver(JobProgressObserver je, boolean unregisterWhenComplete) {
        observers.add(je);
        observerDoRemove.add(unregisterWhenComplete);
    }

    @Override
    public String getCurrentPluginName() {
        return pluginName;
    }

    @Override
    public synchronized boolean getErrorState() {
        return errorState;
    }

    @Override
    public synchronized Exception getException() {
        return exception;
    }

    @Override
    public synchronized void removeObserver(JobProgressObserver je) {
        for (JobProgressObserver o : observers) {
            if (o.equals(je)) {
                int index = observers.indexOf(o);
                observerDoRemove.remove(index);
                observers.remove(index);
            }
        }
    }

    @Override
    public void run() {
        Processor p = null;
        try {
            while (hasNext()) {
                if (doTerminate) {
                    return;
                }
                p = nextTask();
                this.pluginName = ((WorkflowTask) p.getDescriptionRef()).getPluginDescription().getName();
                jobStageStarted(p.getDescriptionRef());
                p.execute(pool);
                jobStageComplete(p.getDescriptionRef());
            }
            jobComplete();
        }
        catch (Exception e) {
            errorState = true;
            exception = e;
            if (p != null)
                jobStageError(p.getDescriptionRef());
            jobComplete();
        }
    }

    private void jobComplete() {
        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).notifyComplete(this);
            if (observerDoRemove.get(i)) {
                observers.remove(i);
                observerDoRemove.remove(i);
            }
        }
    }

    private void jobStageStarted(Object callbackRef) {
        System.err.println("Stage started");
        for (JobProgressObserver observer : observers) {
            observer.notifyStageStarted(callbackRef);
        }
    }

    private void jobStageComplete(Object callbackRef) {
        for (JobProgressObserver observer : observers) {
            observer.notifyStageComplete(callbackRef);
        }
    }

    private void jobStageError(Object callbackRef) {
        for (JobProgressObserver observer : observers) {
            observer.notifyStageError(callbackRef);
        }
    }


    @Override
    public void terminate() {
        doTerminate = true;
    }

    @Override
    public void addResource(UUID resource, Object o) {
        pool.putResource(resource, o, true);
    }
}