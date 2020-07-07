package net.sourceforge.ondex.workflow.engine;

import java.util.UUID;

/**
 * @author lysenkoa
 */
public abstract class AbstractProcessor implements Processor {
    private static final UUID[] NONE = new UUID[0];
    private int priority = Integer.MAX_VALUE;
    protected Object descriptionRef;
    protected Argument[] inputArguments;
    private UUID [] outputAddresses;
    protected Object[] values;
    private boolean hasBeenConfigured = false;

    /* (non-Javadoc)
      * @see net.sourceforge.ondex.workflow2.base.WorkerElement#execute(net.sourceforge.ondex.workflow.engine.ResourcePool)
      */

    public UUID[] execute(ResourcePool rp) throws Exception {
        if(!hasBeenConfigured)
            throw new IllegalStateException("Attempting to execute a processor that has not been configured");

        values = new Object[inputArguments.length];
        int i = 0;
        for (Argument arg : inputArguments) {
            if(arg.address != null) {
                arg.value = rp.getResource(arg.address);
            }
            values[i] = arg.value;
            i++;
        }

        return outputAddresses;
    }

    /* (non-Javadoc)
      * @see net.sourceforge.ondex.workflow2.base.WorkerElement#configure(java.lang.Object[], java.util.UUID[], java.util.UUID[])
      */

    public void configure(Argument [] inputArguments, UUID [] outputAddresses) {
        if(hasBeenConfigured)
            throw new IllegalStateException("Attempting to configure a processor that has already been configured");
        if(inputArguments == null)
            throw new NullPointerException("The inputArguments must not be null");
        if(outputAddresses == null)
            throw new NullPointerException("The outputAddresses must not be null");

        if(inputArguments.length == 0) {
            throw new IllegalArgumentException("The inputArguments array must not be empty");
        }

        for(int i = 0; i < inputArguments.length; i++) {
            if(inputArguments[i] == null) {
                throw new NullPointerException("The inputArguments element at " + i + " is null");
            }
        }

        this.inputArguments = inputArguments;
        this.outputAddresses = outputAddresses;

        hasBeenConfigured = true;
    }

    /* (non-Javadoc)
      * @see net.sourceforge.ondex.workflow2.base.WorkerElement#getPriority()
      */

    /* (non-Javadoc)
      * @see net.sourceforge.ondex.workflow2.base.WorkerElement#compareTo(net.sourceforge.ondex.workflow2.base.AbstractWorkerElement)
      */

    public int compareTo(AbstractProcessor o) {
        return priority - o.priority;
    }

    /* (non-Javadoc)
      * @see net.sourceforge.ondex.workflow2.base.WorkerElement#getUniqueTypeId()
      */

    public Object getDescriptionRef() {
        return descriptionRef;
    }

    /* (non-Javadoc)
      * @see net.sourceforge.ondex.workflow2.base.WorkerElement#setPriority(java.lang.Integer)
      */

}