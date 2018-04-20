package net.sourceforge.ondex.oxl.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Wraps String values with CDATA, before writing them on the XML writer. 
 * This should fix many problems with encoding or alike.
 * 
 * WARNING: not currently used (apart from being injected in {@link CDataLiteral}).
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>20 Apr 2018</dd></dl>
 *
 */
public class CDATAStringAdapter extends XmlAdapter<String, String>
{

	@Override
	public String marshal ( String javaValue ) throws Exception
	{
    return "<![CDATA[" + javaValue + "]]>";
	}

	@Override
	public String unmarshal ( String xmlValue ) throws Exception
	{
		return xmlValue;
	}
}
