package net.sourceforge.ondex.workflow.validation;

public class ErrorReport {
    private final String message;
    private final String argId;
    private final String pluginName;
    private int position;

    public ErrorReport(String message, String argId, String pluginName) {
        if(message == null) throw new NullPointerException("Error report can not have a null message");

        this.message = message;
        this.argId = argId;
        this.pluginName = pluginName;
    }

    public String getMessage() {
        return message;
    }

    public String getArgId() {
        return argId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getPluginName() {
        return pluginName;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("ErrorReport");
        sb.append("{message='").append(message).append('\'');
        sb.append(", argId='").append(argId).append('\'');
        sb.append(", pluginName='").append(pluginName).append('\'');
        sb.append(", position=").append(position);
        sb.append('}');
        return sb.toString();
    }
}
