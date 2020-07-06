package net.sourceforge.ondex.workflow.engine;

import java.util.UUID;

public interface OndexJob {

    public abstract Processor nextTask() throws Exception;

    public abstract boolean hasNext();

    public abstract void addTask(Processor task);

    public abstract void removeTask(Processor task);

    public abstract void addResource(UUID resource, Object o);

}