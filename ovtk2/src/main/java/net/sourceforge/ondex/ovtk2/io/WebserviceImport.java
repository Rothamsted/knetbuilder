package net.sourceforge.ondex.ovtk2.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.parser.oxl.ConceptMetaDataParser;
import net.sourceforge.ondex.parser.oxl.ConceptParser;
import net.sourceforge.ondex.parser.oxl.GeneralMetaDataParser;
import net.sourceforge.ondex.parser.oxl.RelationMetaDataParser;
import net.sourceforge.ondex.parser.oxl.RelationParser;
import net.sourceforge.ondex.parser.oxl.XmlParser;
import net.sourceforge.ondex.webservice.client.ONDEXapiWS;
import net.sourceforge.ondex.webservice.client.ONDEXapiWSService;
import net.sourceforge.ondex.webservice.client.WSGraph;
import net.sourceforge.ondex.webservice.client.WebserviceException_Exception;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import com.ctc.wstx.io.CharsetNames;

public class WebserviceImport {

	public WebserviceImport(ONDEXGraph aog, URL url, WSGraph graph) throws XMLStreamException, ClassNotFoundException, JAXBException, InconsistencyException, WebserviceException_Exception, InstantiationException, IllegalAccessException {
		// load webservice from URL for graph name
		System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory");

		String oxl = "";

		ONDEXapiWSService ondexService = new ONDEXapiWSService(url);
		System.out.println("Service: " + ondexService);
		ONDEXapiWS ondexGraph = ondexService.getONDEXapiWSPort();
		System.out.println("Graph: " + ondexGraph);

		oxl = ondexGraph.exportGraphLite(graph.getId().getValue());

		XMLInputFactory2 xmlInput = (XMLInputFactory2) XMLInputFactory2.newInstance();
		xmlInput.configureForSpeed();

		if (oxl != null && oxl.length() > 0) {
			// the following might not work...
			BufferedInputStream inStream = new BufferedInputStream(new ByteArrayInputStream(oxl.getBytes()));

			XMLStreamReader2 xmlr = (XMLStreamReader2) xmlInput.createXMLStreamReader(inStream, CharsetNames.CS_UTF8);
			// start parsing
			XmlParser parser = new XmlParser();

			// hashtable for id mapping old to new concept ids
			Map<Integer, Integer> table = new HashMap<Integer, Integer>();
			Map<Integer, Set<Integer>> context = new HashMap<Integer, Set<Integer>>();

			parser.registerParser("cv", new ConceptMetaDataParser(aog, "cv"));
			parser.registerParser("unit", new GeneralMetaDataParser(aog, "unit"));
			parser.registerParser("attrname", new GeneralMetaDataParser(aog, "attrname"));
			parser.registerParser("evidences", new GeneralMetaDataParser(aog, "evidences"));
			parser.registerParser("cc", new ConceptMetaDataParser(aog, "cc"));
			parser.registerParser("relation_type", new RelationMetaDataParser(aog, "relation_type"));

			parser.registerParser("concept", new ConceptParser(aog, table, context));
			parser.registerParser("relation", new RelationParser(aog, table));
			parser.parse(xmlr);
			ConceptParser.syncContext(aog, table, context);
			// close reader
			xmlr.close();
		}
	}

	public static List<WSGraph> getGraphs(URL url) throws WebserviceException_Exception {
		ONDEXapiWSService ondexService = new ONDEXapiWSService(url);
		ONDEXapiWS ondexGraph = ondexService.getONDEXapiWSPort();
		return ondexGraph.getGraphs().getWSGraph();
	}

}
