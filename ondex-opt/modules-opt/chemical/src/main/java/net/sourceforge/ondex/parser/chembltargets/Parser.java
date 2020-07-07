package net.sourceforge.ondex.parser.chembltargets;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.tools.chemical.ChEMBLWrapper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Authors(authors = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Custodians(custodians = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Status(status = StatusType.STABLE, description = "Tested December 2013 (Jacek Grzebyta)")
public class Parser extends ONDEXParser {

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[] { new FileArgumentDefinition(
				FileArgumentDefinition.INPUT_FILE,
				FileArgumentDefinition.INPUT_FILE_DESC, true, true, false) };
	}

	@Override
	public String getId() {
		return "chembltarget";
	}

	@Override
	public String getName() {
		return "ChEMBL Targets XML";
	}

	@Override
	public String getVersion() {
		return "03.07.2012";
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	@Override
	public void start() throws Exception {

		// get file content
		InputStream in = new BufferedInputStream(new FileInputStream(args
				.getUniqueValue(FileArgumentDefinition.INPUT_FILE).toString()));

		// parse XML content
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(in);
		doc.getDocumentElement().normalize();

		// get all target elements
		NodeList nList = doc.getElementsByTagName("target");

		// utility class providing XML parsing
		ChEMBLWrapper wrapper = new ChEMBLWrapper(graph);

		// iterate over all targets
		Set<ONDEXConcept> created = new HashSet<ONDEXConcept>();
		for (int i = 0; i < nList.getLength(); i++) {

			Node nNode = nList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				// parse concept from XML
				ONDEXConcept c = wrapper.parseTarget(eElement, null);
				created.add(c);
			}
		}

		fireEventOccurred(new GeneralOutputEvent("Total targets parsed:"
				+ created.size(), getCurrentMethodName()));
	}

}
