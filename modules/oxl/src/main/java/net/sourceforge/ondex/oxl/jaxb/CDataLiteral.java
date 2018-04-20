package net.sourceforge.ondex.oxl.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.sourceforge.ondex.export.oxl.Export;
import net.sourceforge.ondex.export.oxl.XMLTagNames;

/**
 * 
 * This is a wrapper that can be instantiated programmatically by {@link Export} (buildAttribute), 
 * for the purpose of wrapping raw data strings with CDATA.
 * 
 * The class and its JAXB annotations spawns a structure like {@code <literal><![CDATA[value]]></literal>}.
 * 
 * WARNING: initially written to do some experimentation. Not currently used (Export does it more manually)
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>20 Apr 2018</dd></dl>
 *
 */
@XmlRootElement ( name = XMLTagNames.LITERAL )
@XmlAccessorType ( XmlAccessType.NONE )
public class CDataLiteral
{
	private String value;

	public CDataLiteral () {
	}

	public CDataLiteral ( String value )
	{
		super ();
		this.value = value;
	}
	
	@XmlValue
	@XmlJavaTypeAdapter ( CDATAStringAdapter.class )
	public String getValue ()
	{
		return value;
	}
	
	public void setValue ( String value )
	{
		this.value = value;
	}
}
