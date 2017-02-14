package net.sourceforge.ondex.parser.sbml;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createCC;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createDataSource;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createEvidence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.oxl.XmlComponentParser;

/**
 * Parses a compartment element of SBML into the ONDEX graph provided
 * 
 * @author taubertj
 */
public class CompartmentParser implements XmlComponentParser, MetaData {

	private Map<String, ONDEXConcept> nodeIDToConcept;

	private ONDEXGraph graph;

	public CompartmentParser(ONDEXGraph graph,
			Map<String, ONDEXConcept> nodeIDToConcept) {
		this.nodeIDToConcept = nodeIDToConcept;
		this.graph = graph;
	}

	public String getName() {
		return "CompartmentParser";
	}

	/**
	 * Parses a node element of SBML
	 */
	public void parse(XMLStreamReader staxXmlReader) throws XMLStreamException {

		// get all attributes of compartment
		Map<String, String> data = SBMLUtils
				.parseAttributesToHashMap(staxXmlReader);

		// get meta data together
		DataSource nodeDataSource = createDataSource(graph, CV_SBML);
		ConceptClass nodeCc = createCC(graph, MetaData.CC_CELCOMP);
		List<EvidenceType> etypes = new ArrayList<EvidenceType>();
		etypes.add(createEvidence(graph, ET_SBML));

		// create concept for compartment
		String PID = data.get("id").toString();
		ONDEXConcept concept = graph.getFactory().createConcept(PID, nodeDataSource,
				nodeCc, etypes);
		nodeIDToConcept.put(PID, concept);

		// add name to concept
		String name = data.get("name");
		if (name != null) {
			concept.createConceptName(name, true);
		}
	}
}
