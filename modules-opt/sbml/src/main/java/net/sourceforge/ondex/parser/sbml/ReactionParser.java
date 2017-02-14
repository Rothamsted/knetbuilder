package net.sourceforge.ondex.parser.sbml;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createCC;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createDataSource;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createEvidence;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createRT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.export.sbml.Export;
import net.sourceforge.ondex.parser.oxl.XmlComponentParser;

/**
 * @author hindlem
 * @modified by lysenkoa
 * 
 *           Concerning MetaData there are three layers \t First the Parser
 *           checks the translation table \t Then it checks the MetaData \t If
 *           no valid MetaData is found on a object then a missing metadata
 *           warning is logged and a new meta data item created
 * 
 *           MetaData translation tables are specified in the
 *           ONDEXParameters.xml and should be in tab deliminated form
 * 
 *           Regarding RelationTypes and RelationTypes either or both are
 *           accepted and in the event of both being present will be verified
 *           against each other with the greater set being given preference
 *           after a error is thrown If a RelationType is unknown and
 *           RelationTypes are present the RelationType will be spawed with the
 *           given name and types
 */
public class ReactionParser implements XmlComponentParser, MetaData {

	private Map<String, ONDEXConcept> nodeIDToConcept;

	private ONDEXGraph graph;

	public ReactionParser(ONDEXGraph graph,
			Map<String, ONDEXConcept> nodeIDToConcept) {
		this.nodeIDToConcept = nodeIDToConcept;
		this.graph = graph;
	}

	public String getName() {
		return "ReactionParser";
	}

	public void parse(XMLStreamReader staxXmlReader) throws XMLStreamException {

		// get all attributes of reaction
		Map<String, String> data = SBMLUtils
				.parseAttributesToHashMap(staxXmlReader);

		Set<String> reactantIds = new HashSet<String>();
		Set<String> productIds = new HashSet<String>();
		Set<String> modifierIds = new HashSet<String>();
		Set<String> activeSet = new HashSet<String>();
		boolean finished = false;
		while (staxXmlReader.hasNext() && !finished) {

			int event = staxXmlReader.next();
			String element;
			switch (event) {

			case XMLStreamConstants.START_ELEMENT:
				element = staxXmlReader.getLocalName();

				if (element.equals(Export.LISTOFREACTANTS)) {
					activeSet = reactantIds;
				}

				if (element.equals(Export.LISTOFPRODUCTS)) {
					activeSet = productIds;
				}

				if (element.equals(Export.LISTOFMODIFIERS)) {
					activeSet = modifierIds;
				}

				if (element.equals(Export.SPECIESREFERENCE)) {
					activeSet.add(staxXmlReader.getAttributeValue(null,
							"species"));
				}

				if (element.equals(Export.MODIFIERSPECIESREFERENCE)) {
					activeSet.add(staxXmlReader.getAttributeValue(null,
							"species"));
				}
				break;

			case XMLStreamConstants.END_ELEMENT:
				element = staxXmlReader.getLocalName();

				if (element.equals(Export.REACTION)) {
					finished = true;
				}
				break;

			default:
				break;
			}
		}

		// get meta data together
		DataSource nodeDataSource = createDataSource(graph, CV_SBML);
		ConceptClass nodeCc = createCC(graph, MetaData.CC_REACTION);
		List<EvidenceType> etypes = new ArrayList<EvidenceType>();
		etypes.add(createEvidence(graph, ET_SBML));

		// create concept for reaction
		String PID = data.get("id").toString();
		ONDEXConcept reaction = graph.getFactory().createConcept(PID, nodeDataSource,
				nodeCc, etypes);
		nodeIDToConcept.put(PID, reaction);

		// add name to concept
		String name = data.get("name");
		if (name != null) {
			reaction.createConceptName(name, true);
		}

		// add substrates to reaction
		Iterator<String> it = reactantIds.iterator();
		while (it.hasNext()) {
			String id = it.next();
			ONDEXConcept fromConcept = nodeIDToConcept.get(id);
			if (fromConcept == null) {
				System.err.println("Missing concept for id: " + id);
				continue;
			}
			ONDEXRelation relation = graph.createRelation(fromConcept,
					reaction, createRT(graph, MetaData.RT_CONSUMED_BY),
					etypes);

			// add context from substrates
			for (ONDEXConcept context : fromConcept.getTags()) {
				relation.addTag(context);
				reaction.addTag(context);
			}
		}

		// add products to reaction
		it = productIds.iterator();
		while (it.hasNext()) {
			String id = it.next();
			ONDEXConcept fromConcept = nodeIDToConcept.get(id);
			if (fromConcept == null) {
				System.err.println("Missing concept for id: " + id);
				continue;
			}
			ONDEXRelation relation = graph.createRelation(fromConcept,
					reaction, createRT(graph, MetaData.RT_PRODUCED_BY),
					etypes);

			// add context from products
			for (ONDEXConcept context : fromConcept.getTags()) {
				relation.addTag(context);
				reaction.addTag(context);
			}
		}

		// add modifiers to reaction
		it = modifierIds.iterator();
		while (it.hasNext()) {
			String id = it.next();
			ONDEXConcept toConcept = nodeIDToConcept.get(id);
			if (toConcept == null) {
				System.err.println("Missing concept for id: " + id);
				continue;
			}
			ONDEXRelation relation = graph.createRelation(reaction, toConcept, createRT(graph,
					MetaData.RT_REGULATED_BY), etypes);
			
			// add context from modifiers
			for (ONDEXConcept context : toConcept.getTags()) {
				relation.addTag(context);
				reaction.addTag(context);
			}
		}
	}
}
