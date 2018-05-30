package net.sourceforge.ondex.oxl.jaxb;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.util.StreamWriter2Delegate;

/**
 * <p>a wrapper of {@link XMLStreamWriter2} that overrides {@link #writeCharacters(String)}
 * and {@link #writeCharacters(char[], int, int)} methods, so that {@link #writeCData(String)} and
 * {@link #writeCData(char[], int, int)} is invoked before invoking the delegate writing method, i.e., 
 * wraps the writing of node text with XML's CDATA blocks.</p>
 * 
 * <p>This avoids issues like text containing HTML.</p>
 * 
 * <p>We trigger the CDATA-wrapping even when the input text contains '\n'. 
 * This is to face a <a href = 'https://stackoverflow.com/questions/48603942'>bug in JDK &gt;8u151</a>.</p>
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Apr 2018</dd></dl>
 *
 */
public class CDATAWriterFilter extends StreamWriter2Delegate 
{
	public final static char[] ESCAPE_CHARS = { '<', '>', '&' };
	
	public CDATAWriterFilter ( XMLStreamWriter2 parent ) {
		super ( parent );
		this.setParent ( parent );
	}

	@Override
	public void writeCharacters ( final String s ) throws XMLStreamException
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
	public void writeCharacters ( final char[] chars, final int start, final int len ) throws XMLStreamException
	{
		if ( !( chars == null || chars.length == 0 ) )
		{
			int end = start + len;
			// we don't care about out-of-bound cases, the delegate will do it 
			if ( end < chars.length )
				for ( int i = start; i < end; i++ )
				{
					char currentChar = chars [ i ];
					for ( char escChar: ESCAPE_CHARS )
						if ( currentChar == escChar ) {
							this.writeCData ( chars, start, len );
							return;
					}
			}
		}
		super.writeCharacters ( chars, start, len );
	}
}
