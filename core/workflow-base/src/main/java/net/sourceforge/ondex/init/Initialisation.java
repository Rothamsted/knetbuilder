package net.sourceforge.ondex.init;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.ONDEXListener;
import net.sourceforge.ondex.exception.type.PluginException;
import net.sourceforge.ondex.workflow.engine.BasicJobImpl;
import net.sourceforge.ondex.workflow.engine.ResourcePool;
import net.sourceforge.ondex.workflow.model.BoundArgumentValue;
import net.sourceforge.ondex.workflow.model.WorkflowDescription;
import net.sourceforge.ondex.workflow.model.WorkflowTask;

import org.codehaus.stax2.XMLInputFactory2;
import org.xml.sax.SAXException;

/**
 * This class parses initial meta data from the ondexmetadata.xml.
 *
 * @author taubertj
 */
public class Initialisation {

    // contains all registered listeners
    private HashSet<ONDEXListener> listeners = new HashSet<ONDEXListener>();

    // metadata file
    private File file;

    /**
     * Creates a new Initialisation.
     *
     * @param file metadata file
     * @param xsd  xsd to validate against
     */
    public Initialisation(File file, File xsd) throws SAXException, IOException
    {
        this.file = file;

        final String sl = XMLConstants.W3C_XML_SCHEMA_NS_URI;
        // Order of build path is important. If JRE libraries are below the stax libraries
        // in the order, then XMLConstants will be taken to be from stax rather than JRE
        // and promptly fail.

        try
        {
            SchemaFactory factoryx = SchemaFactory.newInstance(sl);
            Schema schema = factoryx.newSchema(new StreamSource(xsd));
            Validator v = schema.newValidator();
            v.validate(new StreamSource(file));
        }
        catch (SAXException e)
        {
            throw new SAXException("Could not validate " + file + " against " + xsd, e);
        }
        catch (IOException e)
        {
            throw new IOException("Could not validate " + file + " against " + xsd, e);
        }
    }

    /**
     * Initialises the meta data for a given AbstractONDEXGraph.
     *
     * @param og AbstractONDEXGraph
     * @throws JAXBException
     */
    public void initMetaData(ONDEXGraph og) throws Exception
    {

        //
        // Set the factory to the RI
        //
        System.setProperty("javax.xml.stream.XMLInputFactory",
                "com.ctc.wstx.stax.WstxInputFactory");

        XMLInputFactory2 xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
        xmlif.configureForSpeed();

        PluginDescription oxlBean = PluginRegistry.getInstance().getPluginDescription(PluginType.PARSER, "oxl");
        if (oxlBean == null) {
            throw new PluginException("Could not find the OXL parser");
        }

        WorkflowTask oxlConf = new WorkflowTask(oxlBean,
                new BoundArgumentValue(arg(oxlBean, "InputFile"), file.getAbsolutePath()));
        oxlConf.addArgument("graphId", og.getName());
        WorkflowDescription td = new WorkflowDescription();
        td.addPlugin(oxlConf);
        td.addResource(og.getName(), og);
        BasicJobImpl je = new BasicJobImpl(new ResourcePool());
        td.toOndexJob(je);
        je.run();

        if(je.getErrorState()) throw je.getException();
    }

    private ArgumentDescription arg(PluginDescription pBean, String name) {
        if (pBean.getArgDef() == null)
            throw new NoSuchElementException("Could not find argument '" + name + "' in " + pBean.getName());

        for(ArgumentDescription aBean : pBean.getArgDef()) {
            if(aBean.getName().equals(name)) return aBean;
        }

        throw new NoSuchElementException("Could not find argument '" + name + "' in " + pBean.getName());
    }

}
