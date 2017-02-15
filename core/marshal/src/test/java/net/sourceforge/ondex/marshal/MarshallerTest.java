package net.sourceforge.ondex.marshal;

import java.awt.Color;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class MarshallerTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MarshallerTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MarshallerTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        Marshaller marshaller = Marshaller.getMarshaller();
        String xml;
        Object check;

        xml = marshaller.toXML(null);
        check = marshaller.fromXML(xml);
        assertEquals(null, check);

        String string = "This is a Test";
        xml = marshaller.toXML(string);
        check = marshaller.fromXML(xml);
        assertEquals(string, check);

        java.lang.Double myDouble = new Double(34.567);
        xml = marshaller.toXML(myDouble);
        check = marshaller.fromXML(xml);
        assertEquals(myDouble, check);

        Color color = new Color(45,78,54);
        xml = marshaller.toXML(color);
        check = marshaller.fromXML(xml);
        assertEquals(color, check);
    }
}
