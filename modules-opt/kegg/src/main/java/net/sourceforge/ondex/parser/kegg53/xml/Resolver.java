/*
 * Created on 08.01.2005
 *
 */
package net.sourceforge.ondex.parser.kegg53.xml;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;

/**
 * @author Jan
 */
public class Resolver implements EntityResolver {

    private InputSource inputSource;

    public Resolver(URL url) throws IOException {
        inputSource = new InputSource(url.openStream());
    }

    public InputSource resolveEntity(String publicId, String systemId)
            throws IOException, SAXException {
        return inputSource;
    }

}
