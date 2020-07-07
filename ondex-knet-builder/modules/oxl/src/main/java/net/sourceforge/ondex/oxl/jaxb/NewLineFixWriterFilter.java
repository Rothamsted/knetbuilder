package net.sourceforge.ondex.oxl.jaxb;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.util.StreamWriter2Delegate;

import net.sourceforge.ondex.export.oxl.Export;

/**
 * <p>This is to fix <a href = 'https://stackoverflow.com/questions/48603942'>bug in JDK &gt;8u151</a>.</p>
 * 
 * <p>In short, when some text is serialised to XML using JAXB, newlines are wrongly escaped by an internal JDK component.<p> 
 * 
 * <p>If you see <a href = 'http://hg.openjdk.java.net/jdk8u/jdk8u-dev/jaxws/rev/6ac8c8bf6b78'>how they patched it</a>,
 * the problem is that at some point in the XML-writing pipeline {@link XMLStreamWriter#writeEntityRef(String)} is 
 * called with the wrong code {@code '#xa'} or {@code '#xd'} (corresponding to '\r' or '\n'). This is produced
 * by some JDK-internal escape handler (again, see their linked patch).</p> 
 * 
 * <p>Waiting for the release of a new JDK, we do as they do in the fix: our {@link #writeEntityRef(String)}
 * wrapper {@link #writeCharacters(String) re-writes} \r or \n if it sees one of the wrong codes, thus eliminating
 * the wrong escaping that happens in the JDK.</p>
 * 
 * <p>TODO: hopefully, this won't be needed anymore once JDK8u192 is released (or if you manage to move to JDK >8), so
 * we will be able to remove this wrapper from {@link Export#start()}.</p>  
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Apr 2018</dd></dl>
 *
 */
public class NewLineFixWriterFilter extends StreamWriter2Delegate 
{
	public NewLineFixWriterFilter ( XMLStreamWriter2 parent ) {
		super ( parent );
		this.setParent ( parent );
	}

	@Override
	public void writeEntityRef ( String eref ) throws XMLStreamException
	{
		if ( eref == null || !eref.startsWith ( "#x" ) ) {
			super.writeEntityRef ( eref );
			return;
		}
		String hex = eref.substring ( 2 );
		for ( char c: new char[] { '\r', '\n' } )
			if ( Integer.toHexString ( c ).equals ( hex ) ) {
				this.writeCharacters ( Character.toString ( c ) );
				return;
		}
		super.writeEntityRef ( eref );
	}
}
