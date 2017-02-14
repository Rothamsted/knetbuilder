package net.sourceforge.ondex.parser.sbml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.export.sbml.Export;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.oxl.XmlParser;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

/**
 * @author hindlem
 * @modified_by lysenkoa
 *              <p/>
 *              Parses files in the SBML format
 *              <p/>
 *              Concerning MetaData there are three layers \t First the Parser
 *              checks the translation table \t Then it checks the MetaData \t
 *              If no valid MetaData is found on a object then a missing
 *              metadata warning is logged and a new meta data item created
 *              <p/>
 *              MetaData translation tables are specified in the
 *              ONDEXParameters.xml and should be in tab deliminated form
 */
public class Parser extends ONDEXParser implements MetaData {

	private XMLInputFactory2 xmlif;

	public String getName() {
		return "SBML Parser";
	}

	public String getVersion() {
		return "22/11/2010";
	}

	@Override
	public String getId() {
		return "sbml";
	}

	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[] { new FileArgumentDefinition(
				FileArgumentDefinition.INPUT_FILE, "SBML file to import", true,
				true, false, false) };
	}

	public void start() throws JAXBException, XMLStreamException,
			PluginConfigurationException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		// configure XML parser
		xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
		xmlif.configureForXmlConformance();

		// define meta data TODO: have this as argument to parser
		graph.getMetaData().createDataSource(CV_SBML, "SBML",
				"Imported from SBML file");
		graph.getMetaData().getFactory()
				.createEvidenceType(ET_SBML, "Imported from SBML model");

		// mapping of SBML id to Ondex concept
		Map<String, ONDEXConcept> nodeIDToConcept = new HashMap<String, ONDEXConcept>();

		XmlParser parser = new XmlParser();

		// parsing of compartments
		parser.registerParser(Export.COMPARTMENT, new CompartmentParser(graph,
				nodeIDToConcept));

		// parsing of species
		parser.registerParser(Export.SPECIES, new SpeciesParser(graph,
				nodeIDToConcept));

		// parsing of reactions
		parser.registerParser(Export.REACTION, new ReactionParser(graph,
				nodeIDToConcept));

		parser.parse(getXMLStreamReader()); // 2nd pass for reactions
	}

	private XMLStreamReader2 getXMLStreamReader()
			throws PluginConfigurationException, XMLStreamException {
		try {
			File file = new File(
					(String) args
							.getUniqueValue(FileArgumentDefinition.INPUT_FILE));
			return (XMLStreamReader2) xmlif
					.createXMLStreamReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			throw new PluginConfigurationException(e);
		}
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

}