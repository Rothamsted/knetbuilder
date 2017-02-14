package net.sourceforge.ondex.parser.kegg.kgml;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.parser.kegg.Parser;

import org.codehaus.stax2.XMLStreamReader2;

public class ReactionParser extends KgmlComponentParser {

	// stop parser from progressing
	private boolean ready = true;

	// EvidenceType = IMPD
	private EvidenceType et;

	// consumed_by
	private RelationType cs_by;

	// produced_by
	private RelationType produces;

	// catalysed_by
	private RelationType ca_by;

	// graphical attributes
	private AttributeName attrVisible, attrColor;

	// current ONDEX graph
	private ONDEXGraph aog;

	// map of KEGG id to ONDEXConcept
	private Map<String, ONDEXConcept> id2Concepts = new HashMap<String, ONDEXConcept>();

	public ReactionParser(ONDEXGraph aog, Parser parser,
			Map<String, ONDEXConcept> id2Concepts) {
		super(parser, "reaction");
		this.aog = aog;
		this.id2Concepts = id2Concepts;

		// check meta data
		et = aog.getMetaData().getEvidenceType("IMPD");
		cs_by = aog.getMetaData().getRelationType("cs_by");
		produces = aog.getMetaData().getRelationType("produces");
		ca_by = aog.getMetaData().getRelationType("ca_by");
		if (et == null) {
			parser.fireEventOccurred(new EvidenceTypeMissingEvent("IMPD",
					"[ReactionParser - constructor]"));
			ready = false;
		}
		if (cs_by == null) {
			parser.fireEventOccurred(new RelationTypeMissingEvent("cs_by",
					"[ReactionParser - constructor]"));
			ready = false;
		}
		if (produces == null) {
			parser.fireEventOccurred(new RelationTypeMissingEvent("produces",
					"[ReactionParser - constructor]"));
			ready = false;
		}
		if (ca_by == null) {
			parser.fireEventOccurred(new RelationTypeMissingEvent("ca_by",
					"[ReactionParser - constructor]"));
			ready = false;
		}

		// get meta data
		ONDEXGraphMetaData meta = aog.getMetaData();

		if ((attrVisible = meta.getAttributeName("visible")) == null)
			attrVisible = meta.getFactory().createAttributeName("visible",
					java.lang.Boolean.class);
		if ((attrColor = meta.getAttributeName("color")) == null)
			attrColor = meta.getFactory().createAttributeName("color",
					java.awt.Color.class);
	}

	@Override
	public void parse(XMLStreamReader2 xmlStreamReader)
			throws XMLStreamException {

		if (ready) {

			// get all attributes for reaction element
			String id = xmlStreamReader.getAttributeValue(null, "id");
			String name = xmlStreamReader.getAttributeValue(null, "name");
			String type = xmlStreamReader.getAttributeValue(null, "type");
			Set<String> substrateIDs = new HashSet<String>();
			Set<String> productIDs = new HashSet<String>();

			// get all substrates and products
			xmlStreamReader.nextTag();
			int eventType = xmlStreamReader.getEventType();
			while (!(eventType == XMLStreamConstants.END_ELEMENT && xmlStreamReader
					.getLocalName().equals("reaction"))) {
				eventType = xmlStreamReader.getEventType();
				if (eventType == XMLStreamConstants.START_ELEMENT
						&& xmlStreamReader.getLocalName().equals("substrate")) {
					// get substrate IDs
					String substrateID = xmlStreamReader.getAttributeValue(
							null, "id");
					substrateIDs.add(substrateID);
				} else if (eventType == XMLStreamConstants.START_ELEMENT
						&& xmlStreamReader.getLocalName().equals("product")) {
					// get product IDs
					String productID = xmlStreamReader.getAttributeValue(null,
							"id");
					productIDs.add(productID);
				}
				xmlStreamReader.nextTag();
			}

			// get enzyme
			ONDEXConcept enzyme = id2Concepts.get(id);
			if (enzyme == null) {
				System.err.println("Missing RN: " + name);
			} else {

				// the reversible flag is in the annotation now
				String anno = enzyme.getAnnotation();
				if (anno.length() == 0) {
					enzyme.setAnnotation(type);
				} else if (!anno.equals(type)) {
					System.err.println("Mismatch of RN reversibility: " + name
							+ " was " + anno + " proposed " + type);
				}

				// add substrate relations
				for (String keggID : substrateIDs) {
					ONDEXConcept concept = id2Concepts.get(keggID);
					if (concept == null) {
						System.err.println("Missing ID: " + keggID);
					} else {
						ONDEXRelation relation = aog.getFactory()
								.createRelation(concept, enzyme, cs_by, et);
						relation.createAttribute(attrVisible, Boolean.FALSE,
								false);
						relation.createAttribute(attrColor, Color.BLACK, false);
						for (ONDEXConcept c : concept.getTags())
							relation.addTag(c);
					}
				}

				// add product relations
				for (String keggID : productIDs) {
					ONDEXConcept concept = id2Concepts.get(keggID);
					if (concept == null) {
						System.err.println("Missing ID: " + keggID);
					} else {
						ONDEXRelation relation = aog.getFactory()
								.createRelation(enzyme, concept, produces, et);
						relation.createAttribute(attrVisible, Boolean.FALSE,
								false);
						relation.createAttribute(attrColor, Color.BLACK, false);
						for (ONDEXConcept c : concept.getTags())
							relation.addTag(c);
					}
				}
			}
		}
	}

}
