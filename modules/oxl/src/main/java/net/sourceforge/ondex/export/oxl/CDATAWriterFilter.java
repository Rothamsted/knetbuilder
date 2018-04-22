package net.sourceforge.ondex.export.oxl;

import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.util.StreamWriter2Delegate;

/**
 * TODO: REMOVE
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Apr 2018</dd></dl>
 *
 */
public class CDATAWriterFilter extends StreamWriter2Delegate 
{
	final static char[] ESCAPE_CHARS = { '<', '>', '&' };
	
	public CDATAWriterFilter ( XMLStreamWriter2 parent ) {
		super ( parent );
		this.setParent ( parent );
	}

	@Override
	public void writeCharacters ( String s ) throws XMLStreamException
	{
		if ( !( s == null || s.isEmpty () ) )
		{
			for ( char escChar: ESCAPE_CHARS )
				if ( s.indexOf ( escChar ) != -1 ) {
					this.writeCData ( s );
					return;
				}
		}
		super.writeCharacters ( s );
	}

	@Override
	public void writeCharacters ( char[] chars, int start, int len ) throws XMLStreamException
	{
		if ( !( chars == null || chars.length == 0 ) )
		{
			int end = start + len;
			if ( end < chars.length )
				for ( int i = start; i < end; i++ )
					for ( char escChar: ESCAPE_CHARS )
						if ( chars [ i ] == escChar ) {
							this.writeCData ( chars, start, end );
							return;
						}
		}
		super.writeCharacters ( chars, start, len );
	}
}
