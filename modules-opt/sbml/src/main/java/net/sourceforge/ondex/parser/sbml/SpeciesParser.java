package net.sourceforge.ondex.parser.sbml;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createAttName;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createCC;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createDataSource;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createEvidence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.export.sbml.Export;
import net.sourceforge.ondex.parser.oxl.XmlComponentParser;

/**
 * @author hindlem
 * @modified by lysenkoa
 * 
 *           Parses a species element of SBML into the ONDEX graph provided
 * 
 *           Concerning MetaData there are three layers \t First the Parser
 *           checks the translation table \t Then it checks the MetaData \t If
 *           no valid MetaData is found on a object then a missing metadata
 *           warning is logged and a new meta data item created
 * 
 *           MetaData translation tables are specified in the
 *           ONDEXParameters.xml and should be in tab deliminated form
 * 
 */
public class SpeciesParser implements XmlComponentParser, MetaData {

	private Map<String, ONDEXConcept> nodeIDToConcept;

	private ONDEXGraph graph;

	public SpeciesParser(ONDEXGraph graph,
			Map<String, ONDEXConcept> nodeIDToConcept) {
		this.nodeIDToConcept = nodeIDToConcept;
		this.graph = graph;
	}

	public String getName() {
		return "SpeciesParser";
	}

	/**
	 * Parses a node element of SBML
	 */
	public void parse(XMLStreamReader staxXmlReader) throws XMLStreamException {

		// get all attributes of species
		Map<String, String> data = SBMLUtils
				.parseAttributesToHashMap(staxXmlReader);

		// get meta data together
		DataSource nodeDataSource = createDataSource(graph, CV_SBML);
		ConceptClass nodeCc = createCC(graph, MetaData.CC_COMPOUND);
		List<EvidenceType> etypes = new ArrayList<EvidenceType>();
		etypes.add(createEvidence(graph, ET_SBML));

		// create concept for species
		String PID = data.get("id").toString();
		ONDEXConcept concept = graph.getFactory().createConcept(PID, nodeDataSource,
				nodeCc, etypes);
		nodeIDToConcept.put(PID, concept);

		// add name to concept
		String name = data.get("name");
		if (name != null) {
			concept.createConceptName(name, true);
		}

		// add localisation in compartment as context
		String compartment = data.get("compartment");
		if (compartment != null) {
			concept.addTag(nodeIDToConcept.get(compartment));
		}

		// add possible SBO term as Attribute
		String sboTerm = data.get(Export.SBOTERM);
		if (sboTerm != null) {
			AttributeName attrname = createAttName(graph, AN_SBO, String.class);
			concept.createAttribute(attrname, sboTerm, false);
		}
	}
}
