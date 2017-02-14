package net.sourceforge.ondex.parser.merger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.event.type.ParsingErrorEvent;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.exception.type.ParsingFailedException;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.oxl.ConceptMetaDataParser;
import net.sourceforge.ondex.parser.oxl.ConceptParser;
import net.sourceforge.ondex.parser.oxl.GeneralMetaDataParser;
import net.sourceforge.ondex.parser.oxl.RelationMetaDataParser;
import net.sourceforge.ondex.parser.oxl.RelationParser;
import net.sourceforge.ondex.parser.oxl.XmlParser;

import org.codehaus.stax2.XMLInputFactory2;

/**
 * Merges two OXL files together based solely on their concept IDs, use only for
 * OXL files with same origin and preserved concept order, i.e. only relations
 * have been modified.
 * 
 * @author taubertj
 * @version 01.01.2013
 */
public class Parser extends ONDEXParser {

	private final XMLInputFactory2 xmlif;

	/**
	 * Constructor initializes the XMLInputFactory2
	 */
	public Parser() {
		// setup XMLStreamReader
		System.setProperty("javax.xml.stream.XMLInputFactory",
				"com.ctc.wstx.stax.WstxInputFactory");
		xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
		xmlif.configureForSpeed();
	}

	@Override
	public String getId() {
		return "oxlmerger";
	}

	@Override
	public String getName() {
		return "OXL concept ID merger";
	}

	@Override
	public String getVersion() {
		return "01.01.2013";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		FileArgumentDefinition arg1 = new FileArgumentDefinition(
				"firstInputFile", "The first OXL input file.", true, true,
				false);
		FileArgumentDefinition arg2 = new FileArgumentDefinition(
				"secondInputFile", "The second OXL input file.", true, true,
				false);
		return new ArgumentDefinition<?>[] { arg1, arg2 };
	}

	/**
	 * Read whole graph from specified file into ONDEX graph.
	 * 
	 * @param file
	 * @param og
	 * @throws Exception
	 */
	private void readWholeGraph(File file, ONDEXGraph og) throws Exception {

		try {
			InputStream in = new GZIPInputStream(new FileInputStream(file));

			XMLStreamReader xmlr;
			xmlr = xmlif.createXMLStreamReader(in);

			XmlParser parser = new XmlParser();

			// hashtable for id mapping old to new concept ids
			Map<Integer, Integer> table = new HashMap<Integer, Integer>();
			Map<Integer, Set<Integer>> context = new HashMap<Integer, Set<Integer>>();

			parser.registerParser("cv", new ConceptMetaDataParser(og, "cv"));
			parser.registerParser("unit", new GeneralMetaDataParser(og, "unit"));
			parser.registerParser("attrname", new GeneralMetaDataParser(og,
					"attrname"));
			parser.registerParser("evidences", new GeneralMetaDataParser(og,
					"evidences"));
			parser.registerParser("cc", new ConceptMetaDataParser(og, "cc"));
			parser.registerParser("relation_type", new RelationMetaDataParser(
					og, "relation_type"));
			parser.registerParser("relationtypeset",
					new RelationMetaDataParser(og, "relationtypeset"));

			ConceptParser cp = new ConceptParser(og, table, context);
			parser.registerParser("concept", cp);

			RelationParser rp = new RelationParser(og, table);
			parser.registerParser("relation", rp);

			parser.parse(xmlr);

			ConceptParser.syncContext(og, table, context);

			// catch exceptions and throw them upwards
			if (cp.errorMessages.size() > 0) {
				fireEventOccurred(new ParsingErrorEvent(
						cp.errorMessages.toString(), getCurrentMethodName()));
				throw new ParsingFailedException(cp.errorMessages.toString());
			}

			// close reader
			xmlr.close();

		} catch (InconsistencyException e) {
			fireEventOccurred(new ParsingErrorEvent(e.getMessage(),
					getCurrentMethodName()));
			throw new ParsingFailedException(e);
		} catch (XMLStreamException e) {
			fireEventOccurred(new ParsingErrorEvent(e.getMessage(),
					getCurrentMethodName()));
			throw new ParsingFailedException(e);
		} catch (JAXBException e) {
			fireEventOccurred(new ParsingErrorEvent(e.getMessage(),
					getCurrentMethodName()));
			throw new ParsingFailedException(e);
		} catch (ClassNotFoundException e) {
			fireEventOccurred(new ParsingErrorEvent(e.getMessage(),
					getCurrentMethodName()));
			throw new ParsingFailedException(e);
		} catch (InstantiationException e) {
			fireEventOccurred(new ParsingErrorEvent(e.getMessage(),
					getCurrentMethodName()));
			throw new ParsingFailedException(e);
		} catch (IllegalAccessException e) {
			fireEventOccurred(new ParsingErrorEvent(e.getMessage(),
					getCurrentMethodName()));
			throw new ParsingFailedException(e);
		}
	}

	@Override
	public void start() throws Exception {

		// this is the base graph
		File file1 = new File(args.getUniqueValue("firstInputFile").toString());
		readWholeGraph(file1, graph);

		// this is used to fill up additional relation attributes
		File file2 = new File(args.getUniqueValue("secondInputFile").toString());

		// temporarily read in second file into another memory graph
		MemoryONDEXGraph temp = new MemoryONDEXGraph("temp");
		readWholeGraph(file2, temp);

		// now merge relations from second graph into first
		mergeRelations(graph, temp);

		// clean up
		temp = null;
		System.runFinalization();
	}

	/**
	 * Merges relations occurrences and attributes across two ONDEX graphs into
	 * first one.
	 * 
	 * @param graph1
	 * @param graph2
	 */
	private void mergeRelations(ONDEXGraph graph1, ONDEXGraph graph2) {

		// iterate over all relations in second graph
		for (ONDEXRelation r : graph2.getRelations()) {

			// check if relation already present in first graph
			ONDEXRelation r1 = graph1.getRelation(r.getFromConcept(),
					r.getToConcept(), r.getOfType());
			if (r1 == null) {
				// create new relation in first graph
				r1 = graph1.createRelation(r.getFromConcept(),
						r.getToConcept(), r.getOfType(), r.getEvidence());
			}

			// synchronise tags
			Set<ONDEXConcept> newTags = new HashSet<ONDEXConcept>(r.getTags());
			newTags.removeAll(r1.getTags());
			for (ONDEXConcept c : newTags) {
				if (graph1.getConcept(c.getId()) != null)
					r1.addTag(c);
			}

			// synchronise evidence
			Set<EvidenceType> newEvidence = new HashSet<EvidenceType>(
					r.getEvidence());
			newEvidence.removeAll(r1.getEvidence());
			for (EvidenceType et : newEvidence) {
				// create new evidence type if not existing
				if (graph1.getMetaData().getEvidenceType(et.getId()) == null)
					graph1.getMetaData().createEvidenceType(et.getId(),
							et.getFullname(), et.getDescription());
				r1.addEvidenceType(et);
			}

			// synchronise attributes
			Set<Attribute> newAttributes = new HashSet<Attribute>(
					r.getAttributes());
			newAttributes.removeAll(r1.getAttributes());
			for (Attribute attr : newAttributes) {
				AttributeName an = attr.getOfType();
				// create new attribute name if not existing
				if (graph1.getMetaData().getAttributeName(an.getId()) == null)
					graph1.getMetaData().createAttributeName(an.getId(),
							an.getFullname(), an.getDescription(),
							an.getUnit(), an.getDataType(),
							an.getSpecialisationOf());
				r1.createAttribute(an, attr.getValue(), attr.isDoIndex());
			}
		}
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

}
