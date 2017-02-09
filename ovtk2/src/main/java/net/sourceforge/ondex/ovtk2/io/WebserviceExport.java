package net.sourceforge.ondex.ovtk2.io;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.export.oxl.Export;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.webservice.client.ONDEXapiWS;
import net.sourceforge.ondex.webservice.client.ONDEXapiWSService;
import net.sourceforge.ondex.webservice.client.WebserviceException_Exception;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;

import com.ctc.wstx.io.CharsetNames;

public class WebserviceExport extends Export {

	// default constructor
	public WebserviceExport() {
		super();
	}

	// to avoid clasing with the oxl exporter id
	public String getId() {
		return "OVTK_Webservice_exporter";
	}

	public WebserviceExport(ONDEXGraph aog, URL url, String name) throws XMLStreamException, IOException, JAXBException, WebserviceException_Exception {

		XMLOutputFactory2 xmlOutput = (XMLOutputFactory2) XMLOutputFactory2.newInstance();
		xmlOutput.configureForSpeed();
		xmlOutput.setProperty(XMLOutputFactory2.IS_REPAIRING_NAMESPACES, false);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedOutputStream outStream = new BufferedOutputStream(baos);

		XMLStreamWriter2 xmlWriteStream = (XMLStreamWriter2) xmlOutput.createXMLStreamWriter(outStream, CharsetNames.CS_UTF8);
		buildDocument(xmlWriteStream, aog);

		xmlWriteStream.flush();
		xmlWriteStream.close();

		outStream.flush();
		outStream.close();

		ONDEXapiWSService ondexService = new ONDEXapiWSService(url);
		ONDEXapiWS ondexGraph = ondexService.getONDEXapiWSPort();
		System.out.println(ondexService + " " + ondexGraph);

		Long id = ondexGraph.createGraph(name);
		System.out.println("ID of new graph: " + id);

		String oxl = baos.toString();
		try {
			ondexGraph.importGraphLite(id, oxl);
		} catch (WebserviceException_Exception e) {
			ErrorDialog.show(e);
		}
	}

}
