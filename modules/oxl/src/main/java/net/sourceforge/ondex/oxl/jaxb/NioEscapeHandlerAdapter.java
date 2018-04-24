//package net.sourceforge.ondex.oxl.jaxb;
//
//import java.io.IOException;
//import java.io.Writer;
//
//import org.eclipse.persistence.oxm.CharacterEscapeHandler;
//
//import com.sun.xml.bind.marshaller.NioEscapeHandler;
//
///**
// * TODO: comment me!
// *
// * @author brandizi
// * <dl><dt>Date:</dt><dd>23 Apr 2018</dd></dl>
// *
// */
//public class NioEscapeHandlerAdapter implements CharacterEscapeHandler
//{
//	private final NioEscapeHandler baseHandler;
//	
//	public NioEscapeHandlerAdapter ( String charsetName ) {
//		this.baseHandler = new NioEscapeHandler ( charsetName );
//	}
//	
//	@Override
//	public void escape ( char[] buffer, int start, int length, boolean isAttributeValue, Writer out ) 
//		throws IOException
//	{
//		baseHandler.escape ( buffer, start, length, isAttributeValue, out );
//	}
//}
