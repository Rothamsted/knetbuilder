package net.sourceforge.ondex.workflow.model;

import net.sourceforge.ondex.init.ArgumentDescription;

public class BoundArgumentValue {
    private final ArgumentDescription arg;
    private String value;

    public BoundArgumentValue(ArgumentDescription arg, String value) {
        if(arg == null) throw new NullPointerException("Can not construct an ArgDefValuePair with a null ArgumentDescription");
        this.arg = arg;
        this.value = value;
    }

    public ArgumentDescription getArg() {
        return arg;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("ArgDefValuePair");
        sb.append("{arg=").append(arg);
        sb.append(", value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }
}