package net.sourceforge.ondex.ovtk2.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.export.oxl.Export;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.tools.ziptools.ZipEndings;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;

import com.ctc.wstx.io.CharsetNames;

/**
 * Exporter for the OXL format.
 * 
 * @author taubertj
 * 
 */
public class OXLExport extends Export {

	/**
	 * File to export to
	 */
	private File file = null;

	/**
	 * Sets internal variables
	 * 
	 * @param aog
	 *            ONDEXGraph to export
	 * @param file
	 *            file to export to
	 */
	public OXLExport(ONDEXJUNGGraph aog, File file) {
		this.graph = aog;
		this.annotations = aog.getAnnotations();
		this.file = file;
	}

	public void start() throws JAXBException {
		XMLOutputFactory2 xmlOutput = (XMLOutputFactory2) XMLOutputFactory2.newInstance();
		xmlOutput.configureForSpeed();
		xmlOutput.setProperty(XMLOutputFactory2.IS_REPAIRING_NAMESPACES, false);

		int detectedEnding = ZipEndings.getPostfix(file);

		try {

			OutputStream outStream = null;

			switch (detectedEnding) {

			case ZipEndings.GZ:
				// use gzip compression
				outStream = new GZIPOutputStream(new FileOutputStream(file));
				System.out.println("Detected GZIP file");
				break;
			case ZipEndings.OXL:
				// use gzip compression
				outStream = new GZIPOutputStream(new FileOutputStream(file));
				System.out.println("Detected OXL file");
				break;
			case ZipEndings.XML:
				// output file writer
				outStream = new FileOutputStream(file);
				System.out.println("Detected Uncompressed file");
				break;
			default:
				File f = new File(file.getAbsolutePath() + ".oxl");
				outStream = new GZIPOutputStream(new FileOutputStream(f));
			}

			if (outStream != null) {

				XMLStreamWriter2 xmlWriteStream = (XMLStreamWriter2) xmlOutput.createXMLStreamWriter(outStream, CharsetNames.CS_UTF8);
				buildDocument(xmlWriteStream, graph);

				xmlWriteStream.flush();
				xmlWriteStream.close();

				outStream.flush();
				outStream.close();
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
