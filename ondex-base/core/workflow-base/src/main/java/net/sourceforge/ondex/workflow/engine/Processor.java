package net.sourceforge.ondex.workflow.engine;

import java.util.UUID;

public interface Processor {

    public abstract UUID[] execute(ResourcePool rp) throws Exception;

    public abstract void configure(Argument [] inputArguments, UUID [] outputAddresses);

    public abstract int compareTo(AbstractProcessor o);

    public abstract Object getDescriptionRef();

    public static final class Argument {
        public Object value;
        public UUID address;

        public Argument(Object argument, UUID address)
        {
            this.value = argument;
            this.address = address;
        }
    }
}