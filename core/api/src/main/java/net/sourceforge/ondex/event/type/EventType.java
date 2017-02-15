package net.sourceforge.ondex.event.type;

import org.apache.log4j.Level;

/**
 * Abstract class specifying the kind of event that occurred.
 *
 * @author taubertj
 */
public abstract class EventType {

    // stores description
    protected String desc = "A general event occurred.";

    // specific message of this event type
    private final String message;

    // extension to message, like method name
    private final String extension;

    // specifies a Log4j level for this event type
    private Level log4jLevel = Level.DEBUG;

    /**
     * Constructor takes a message and a possible extension for this EventType.
     *
     * @param message   String
     * @param extension String
     */
    public EventType(String message, String extension) {
        this.message = message;
        this.extension = extension;
    }

    protected EventType(String desc, String message, String extension)
    {
        this.desc = desc;
        this.message = message;
        this.extension = extension;
    }

    protected EventType(String desc, String message, String extension, Level log4jLevel)
    {
        this.desc = desc;
        this.message = message;
        this.extension = extension;
        this.log4jLevel = log4jLevel;
    }

    /**
     * Returns the message of this EventType.
     *
     * @return String
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the complete message of this EventType.
     *
     * @return String
     */
    public String getCompleteMessage() {
        return message + " " + extension;
    }

    /**
     * Returns a description of this EventType.
     *
     * @return String
     */
    public String getDescription() {
        return desc;
    }

    /**
     * Returns the Log4j Level associated with this EventType.
     *
     * @return Level
     */
    public Level getLog4jLevel() {
        return log4jLevel;
    }

    /**
     * Sets the Log4j Level for this EventType.
     *
     * @param l Level
     */
	public void setLog4jLevel(Level l) {
		this.log4jLevel = l;
	}
}
