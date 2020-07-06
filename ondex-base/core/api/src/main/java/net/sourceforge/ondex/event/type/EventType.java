package net.sourceforge.ondex.event.type;

import java.util.function.BiConsumer;

import org.apache.log4j.Logger;

/**
 * Abstract class specifying the kind of event that occurred.
 *
 * @author taubertj
 */
public abstract class EventType {

    // stores description
    protected String desc = "";

    // specific message of this event type
    private final String message;

    // extension to message, like method name
    private final String extension;

    // specifies a Log4j level for this event type
    private Level log4jLevel = Level.DEBUG;

    /**
     * 
     * Custom level, to make methods like {@link EventType#setLog4jLevel(Level)} independent on 
     * any logging system.
     *
     * @author brandizi
     * <dl><dt>Date:</dt><dd>18 Feb 2019</dd></dl>
     *
     */
    public static enum Level 
    {
    	FATAL( (log, msg) -> log.fatal ( msg ) ), 
    	ERROR( (log, msg) -> log.error ( msg ) ),
    	WARN( (log, msg) -> log.warn ( msg ) ),
    	INFO( (log, msg) -> log.info ( msg ) ),
    	DEBUG( (log, msg) -> log.debug ( msg ) ),
    	TRACE( (log, msg) -> log.trace ( msg ) );
    	
    	private BiConsumer<Logger, String> loggerConsumer;
    	  
    	Level ( BiConsumer<Logger, String> loggerConsumer ) {
    		this.loggerConsumer = loggerConsumer;
    	}
    	public void log ( Logger logger, String message )
    	{
    		loggerConsumer.accept ( logger, message );
    	}
    }
    
    
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
