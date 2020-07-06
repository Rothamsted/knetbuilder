package net.sourceforge.ondex.algorithm.graphquery.exceptions;

/**
 * 
 * @author hindlem
 *
 */
public class InvalidFileException extends Exception {

    public InvalidFileException()
    {
    }

    public InvalidFileException(String message)
    {
        super(message);
    }

    public InvalidFileException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InvalidFileException(Throwable cause)
    {
        super(cause);
    }

    private static final long serialVersionUID = 1L;
	
}
