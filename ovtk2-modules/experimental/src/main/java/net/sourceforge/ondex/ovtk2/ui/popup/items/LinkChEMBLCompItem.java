package net.sourceforge.ondex.ovtk2.ui.popup.items;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;
import net.sourceforge.ondex.tools.chemical.ChEMBLWrapper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Ondex queries the ChEMBL webservice with the compound(s) accession numbers
 * for additional information.
 * 
 * @author taubertj
 * 
 */
public class LinkChEMBLCompItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {

		// get meta data
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		ConceptClass ccComp = graph.getMetaData().getConceptClass("Comp");
		DataSource dsCHEMBL = graph.getMetaData().getDataSource("CHEMBL");

		// look at all selected chemical compounds
		for (ONDEXConcept c : entities) {
			if (c.getOfType().equals(ccComp)) {
				for (ConceptAccession ca : c.getConceptAccessions()) {
					// at least one accession with source CHEMBL
					if (ca.getElementOf().equals(dsCHEMBL)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	protected void doAction() {

		// get meta data
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		ConceptClass ccComp = graph.getMetaData().getConceptClass("Comp");
		DataSource dsCHEMBL = graph.getMetaData().getDataSource("CHEMBL");

		// parse all accessions contained in graph
		Map<String, Set<ONDEXConcept>> accessions = new HashMap<String, Set<ONDEXConcept>>();
		for (ONDEXConcept c : entities) {
			if (c.getOfType().equals(ccComp)) {
				for (ConceptAccession ca : c.getConceptAccessions()) {
					if (ca.getElementOf().equals(dsCHEMBL)) {
						if (!accessions.containsKey(ca.getAccession()))
							accessions.put(ca.getAccession(),
									new HashSet<ONDEXConcept>());
						accessions.get(ca.getAccession()).add(c);
					}
				}
			}
		}

		// utility class providing XML parsing
		ChEMBLWrapper wrapper = new ChEMBLWrapper(graph);

		try {

			// process all accessions
			for (String accession : accessions.keySet()) {

				// build query URL
				URL url = new URL("https://www.ebi.ac.uk/chemblws/compounds/"
						+ accession);

				// open http connection
				HttpURLConnection uc = (HttpURLConnection) url.openConnection();

				// check for response code
				int code = uc.getResponseCode();

				if (code != 200) {
					// in the event of error
					String response = uc.getResponseMessage();
					System.err.println("HTTP/1.x " + code + " " + response);
				} else {

					// get main content
					InputStream in = new BufferedInputStream(
							uc.getInputStream());

					// parse XML content
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(in);
					doc.getDocumentElement().normalize();

					// get all compound elements, should be 1
					NodeList nList = doc.getElementsByTagName("compound");

					// add to compound
					Set<ONDEXConcept> comps = accessions.get(accession);
					for (ONDEXConcept c : comps) {
						for (int temp = 0; temp < nList.getLength(); temp++) {

							Node nNode = nList.item(temp);
							if (nNode.getNodeType() == Node.ELEMENT_NODE) {

								Element eElement = (Element) nNode;

								// add attributes to concepts
								wrapper.parseCompound(eElement, c);
							}
						}
					}
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.LINK;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.LinkChEMBLComp";
	}

	@Override
	protected String getUndoPropertyName() {
		return "";
	}

}
