/*
 * Created on 08.01.2005
 *
 */
package net.sourceforge.ondex.parser.kegg52.xml;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Jan
 */
public class Resolver implements EntityResolver {

    private InputSource inputSource;

    public Resolver(String pathToDTD) {
        try {
            inputSource = new InputSource(new BufferedReader(new FileReader(pathToDTD)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public InputSource resolveEntity(String publicId, String systemId)
            throws IOException, SAXException {
        return inputSource;
    }

}
