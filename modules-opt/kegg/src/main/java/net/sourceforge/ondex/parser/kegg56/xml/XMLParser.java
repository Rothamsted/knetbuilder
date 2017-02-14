package net.sourceforge.ondex.parser.kegg56.xml;

import net.sourceforge.ondex.parser.kegg56.data.Pathway;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


/**
 * @author Jan
 */
public class XMLParser {

    private final static String id = "http://xml.org/sax/features/validation";
    private URL dtdURL;

    /**
     * @param dtdURL
     */
    public XMLParser(URL dtdURL) {
        this.dtdURL = dtdURL;
    }

    /**
     *
     */
    public XMLParser() {
    }

    /**
     * @param stream to kgml file
     * @return
     */
    public Pathway parse(InputStream stream) throws IOException, SAXException {
        XMLReader xr = XMLReaderFactory.createXMLReader();

        // set document handler
        Handler handler = new Handler();
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);
        if (dtdURL == null)
            xr.setEntityResolver(new Resolver(dtdURL));
        xr.setFeature(id, true);
        xr.parse(new InputSource(stream));
        return handler.getPathway();
    }


}

