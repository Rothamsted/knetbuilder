package net.sourceforge.ondex.cytoscape.mapping;

/**
 * Created by IntelliJ IDEA.
 * User: nmrp3
 * Date: 27-Oct-2009
 * Time: 14:41:21
 * To change this template use File | Settings | File Templates.
 */
public class MalformedPathException
    extends Exception
{
    public MalformedPathException(Throwable throwable)
    {
        super(throwable);
    }

    public MalformedPathException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public MalformedPathException(String s)
    {
        super(s);
    }

    public MalformedPathException()
    {
    }
}
