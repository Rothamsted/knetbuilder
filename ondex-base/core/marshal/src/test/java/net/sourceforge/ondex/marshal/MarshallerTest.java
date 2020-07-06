package net.sourceforge.ondex.marshal;

import static org.junit.Assert.assertEquals;

import java.awt.Color;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class MarshallerTest
{
	private static Marshaller marshaller = Marshaller.getMarshaller ();

	private <T> void doTestMarshalling ( String errMsg, T src )
	{
		String xml = marshaller.toXML ( src );
		Object revertedResult = marshaller.fromXML ( xml );
		assertEquals ( "Wrong string conversion!", src, revertedResult );
	}
	
	@Test
	public void testNull () {
		doTestMarshalling ( "null conversion didn't work!", null );
	}

	@Test
	public void testString () {
		doTestMarshalling ( "String conversion didn't work!", "This is a Test" );
	}

	@Test
	public void testDouble () {
		doTestMarshalling ( "Double conversion didn't work!", 34.567d );
	}

	@Test
	public void testColor () {
		doTestMarshalling ( "Color conversion didn't work!", new Color ( 45, 78, 54 ) );
	}

}
