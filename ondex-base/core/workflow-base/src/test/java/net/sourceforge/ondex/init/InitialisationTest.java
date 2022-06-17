package net.sourceforge.ondex.init;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.xml.sax.SAXException;

import net.sourceforge.ondex.workflow.base.init.Initialisation;

/**
 * Test key aspects of the Initialisation class.
 *
 * @author Matthew Pocock
 */
public class InitialisationTest
{
    @Test
    public void testInitRaisesIOExceptionOnMissingMetaDataFile() throws SAXException
    {
        boolean raised = false;
        try
        {
            new Initialisation(new File("bob.xml"), new File(new File(new File("data"), "xml"), "ondex.xsd"));
        }
        catch (IOException e) {
            raised = true;
        }

        assertTrue("IOException must be raised if the metadata file is missing", raised);
    }

    @Test
    public void testInitRaisesSAXExceptionOnMissingXSDFile() throws IOException
    {
        boolean raised = false;
        try
        {
            new Initialisation(new File(new File(new File("data"), "xml"), "ondex_metadata.xml"), new File("bob"));
        }
        catch (SAXException e) {
            raised = true;
        }

        assertTrue("SAXException must be raised if the metadata file is missing", raised);
    }

    @Test
    public void testNothingRaisedWhenFilesPresent() throws IOException, SAXException
    {
        new Initialisation(
                new File(new File(new File("data"), "xml"), "ondex_metadata.xml"),
                new File(new File(new File("data"), "xml"), "ondex.xsd"));
    }
}
