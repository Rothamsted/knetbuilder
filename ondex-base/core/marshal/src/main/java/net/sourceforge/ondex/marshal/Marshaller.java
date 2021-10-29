package net.sourceforge.ondex.marshal;

import com.thoughtworks.xstream.XStream;

/**
 * Main Utils class to support conversion of Objects to XML and back.
 *
 * Xstream used purely as an example. Feel free to update to a more suitable method.
 *
 * @author Christian Brennikmeijer
 */
public class Marshaller
{

	private static Marshaller marshaller = null;

	// TODO: There was a potential problem related to JDK > 11
	// https://github.com/x-stream/xstream/issues/101
	// Which it should be fixed with the latest xstream version.
	private static XStream xstream = new XStream ();

	{
		// See https://stackoverflow.com/a/67288175/529286
		xstream.allowTypesByWildcard(new String[] { 
	    "net.sourceforge.ondex.**",
	    "java.awt.**"
    });
		
	}
	
	private Marshaller () {}

	public static Marshaller getMarshaller ()
	{
		if ( marshaller == null )
		{
			marshaller = new Marshaller ();
		}
		return marshaller;
	}

	public String toXML ( Object object )
	{
		return xstream.toXML ( object );
	}

	public Object fromXML ( String xml )
	{
		return xstream.fromXML ( xml );
	}
}
