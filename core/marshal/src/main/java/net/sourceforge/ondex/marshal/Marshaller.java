package net.sourceforge.ondex.marshal;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

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

	// TODO: this is going to be problematic with JDK > 11
	// https://github.com/x-stream/xstream/issues/101
	// a replacement will probably be needed.
	
	// A possible workaround in a comment to the issue cited above:
	//
	// My workaround was to override the setupConverters() method and omit the "broken" converters 
	// (ExternalizableConverter, TreeMapConverter, TreeSetConverter, PropertiesConverter, DynamicProxyConverter).
	//
	private static XStream xstream = new XStream ();

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
