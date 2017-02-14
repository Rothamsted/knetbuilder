package net.sourceforge.ondex.parser.kegg52.xml;

import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.kegg52.Parser;
import net.sourceforge.ondex.parser.kegg52.data.Pathway;
import net.sourceforge.ondex.tools.ziptools.ZipEndings;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;


/**
 * @author Jan
 */
public class XMLParser {

    private final static String id = "http://xml.org/sax/features/validation";
    private String pathToDTD;

    public XMLParser(String pathToDTD) {
        this.pathToDTD = pathToDTD;
    }

    public Pathway parse(File file) {

        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();

            // set document handler
            Handler handler = new Handler();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);

            Resolver resolver = new Resolver(pathToDTD);

            xr.setEntityResolver(resolver);

            xr.setFeature(id, true);

            int detectedEnding = ZipEndings.getPostfix(file);//.lastIndexOf("."));

            FileInputStream fis = new FileInputStream(file);
            InputStream zis = null;
            switch (detectedEnding) {

                case ZipEndings.GZ:
                    zis = new GZIPInputStream(fis);
                    break;
                case ZipEndings.ZIP:
                    zis = new ZipInputStream(fis);
                    break;
                default:
                    break;
            }

            if (zis != null) xr.parse(new InputSource(zis));
            else xr.parse(new InputSource(fis));

            if (zis != null) zis.close();
            fis.close();
            return handler.getPathway();

        } catch (FileNotFoundException fnfe) {
            Parser.propagateEventOccurred(
                    new DataFileMissingEvent(fnfe.getMessage(), ""));
        } catch (IOException ioe) {
            Parser.propagateEventOccurred(
                    new DataFileErrorEvent(ioe.getMessage(), ""));
        } catch (SAXNotRecognizedException e) {
            Parser.propagateEventOccurred(
                    new GeneralOutputEvent("XMLParser Validation - Can't tell." + e.getMessage(), ""));
        } catch (SAXNotSupportedException e) {
            Parser.propagateEventOccurred(
                    new GeneralOutputEvent("XMLParser Validation - Wrong time to ask." + e.getMessage(), ""));
        } catch (SAXException saxe) {
            Parser.propagateEventOccurred(
                    new DataFileErrorEvent(saxe.getMessage(), ""));
        }
        return null;
    }


}

