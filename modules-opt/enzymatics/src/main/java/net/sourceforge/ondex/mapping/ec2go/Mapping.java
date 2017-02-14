package net.sourceforge.ondex.mapping.ec2go;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.DataURL;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.annotations.metadata.AttributeNameRequired;
import net.sourceforge.ondex.annotations.metadata.DataSourceRequired;
import net.sourceforge.ondex.annotations.metadata.EvidenceTypeRequired;
import net.sourceforge.ondex.annotations.metadata.RelationTypeRequired;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.mapping.ONDEXMapping;

/**
 * Parses the ec2go mapping from a given file.
 * 
 * @author taubertj
 */
@Status(description = "Tested December 2012 (Jan Taubert)", status = StatusType.STABLE)
@DatabaseTarget(name = "geneontology-external2go-ec2go", description = "the ec component of the external2go set of mappings in the geneontology", version = "2012/06/19", url = "http://www.geneontology.org/external2go/")
@DataURL(name = "ec2go", description = "external2go mapping file", urls = "http://www.geneontology.org/external2go/ec2go")
@DataSourceRequired(ids = MetaData.cvGO)
@EvidenceTypeRequired(ids = MetaData.evidence)
@RelationTypeRequired(ids = MetaData.relType)
@AttributeNameRequired(ids = MetaData.ATT_DATASOURCE)
public class Mapping extends ONDEXMapping implements ArgumentNames {

	@Override
	public void start() throws InvalidPluginArgumentException {

		String filename = (String) args.getUniqueValue(INPUT_FILE_ARG);
		fireEventOccurred(new GeneralOutputEvent("Using EC2GO file " + filename
				+ ".", getCurrentMethodName()));

		AttributeName attDs = graph.getMetaData().getAttributeName(
				MetaData.ATT_DATASOURCE);
		if (attDs == null) {
			attDs = graph
					.getMetaData()
					.getFactory()
					.createAttributeName(MetaData.ATT_DATASOURCE,
							"Datasource where this ONDEXEntity originated",
							String.class);
		}

		// RT: equ
		RelationType relType = graph.getMetaData().getRelationType(
				MetaData.relType);
		if (relType == null)
			fireEventOccurred(new RelationTypeMissingEvent(MetaData.relType,
					getCurrentMethodName()));

		// ET: EC2GO
		EvidenceType et = graph.getMetaData()
				.getEvidenceType(MetaData.evidence);
		if (et == null)
			fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.evidence,
					getCurrentMethodName()));

		// DataSource: EC
		DataSource dataSourceEC = graph.getMetaData().getDataSource(
				net.sourceforge.ondex.parser.ec.MetaData.DS_EC);
		if (dataSourceEC == null)
			fireEventOccurred(new DataSourceMissingEvent(
					net.sourceforge.ondex.parser.ec.MetaData.DS_EC,
					getCurrentMethodName()));

		// DataSource: GO
		DataSource dataSourceGO = graph.getMetaData().getDataSource(
				MetaData.cvGO);
		if (dataSourceGO == null)
			fireEventOccurred(new DataSourceMissingEvent(MetaData.cvGO,
					getCurrentMethodName()));

		Hashtable<String, String> input = new Hashtable<String, String>();

		try {
			// read in mapping file
			BufferedReader reader = new BufferedReader(new FileReader(filename));

			while (reader.ready()) {
				String line = reader.readLine();
				if (!line.startsWith("!")) {
					String ec = line.substring(3, line.indexOf(">") - 1);
					// handle some of the mistakes in ec2go file
					if (ec.endsWith(".") || ec.endsWith("e"))
						continue;
					ec = fillEC(ec);
					String go = line.substring(line.length() - 10,
							line.length());
					input.put(ec, go);
					input.put(go, ec);
				}
			}

			reader.close();
		} catch (FileNotFoundException fnfe) {
			fireEventOccurred(new DataFileMissingEvent("File " + filename
					+ " not found.", getCurrentMethodName()));
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent("Error reading file "
					+ filename + ".", getCurrentMethodName()));
		}

		Hashtable<String, ONDEXConcept> mapping = new Hashtable<String, ONDEXConcept>();

		// map of pid to ONDEXConcept
		for (ONDEXConcept c : graph.getConceptsOfDataSource(dataSourceEC)) {
			if (input.containsKey(c.getPID()))
				mapping.put(c.getPID(), c);
		}
		for (ONDEXConcept c : graph.getConceptsOfDataSource(dataSourceGO)) {
			if (input.containsKey(c.getPID()))
				mapping.put(c.getPID(), c);
		}

		// create Relations
		for (String from : mapping.keySet()) {
			ONDEXConcept fromConcept = mapping.get(from);
			String to = input.get(from);
			ONDEXConcept toConcept = mapping.get(to);
			if (toConcept == null) {
				fireEventOccurred(new InconsistencyEvent(
						"ToConcept for mapping " + from + " = " + to
								+ " not present in graph.",
						getCurrentMethodName()));
			} else {
				ONDEXRelation existingRelation = graph.getRelation(fromConcept,
						toConcept, relType);
				if (existingRelation == null) {
					existingRelation = graph.getFactory().createRelation(
							fromConcept, toConcept, relType, et);
				} else {
					existingRelation.getEvidence().add(et);
				}
				Attribute existingAttribute = existingRelation
						.getAttribute(attDs);

				if (existingAttribute == null) {
					existingRelation.createAttribute(attDs, getId(), false);
				} else if (!existingAttribute.getValue().toString()
						.contains(getId())) {
					existingAttribute.setValue(existingAttribute.getValue()
							.toString() + "," + getId());
				}
			}
		}
	}

	/**
	 * Returns name of this mapping.
	 * 
	 * @return String
	 */
	public String getName() {
		return "EC2GO";
	}

	/**
	 * Returns version of this mapping.
	 * 
	 * @return String
	 */
	public String getVersion() {
		return "27.12.2012";
	}

	@Override
	public String getId() {
		return "ec2go";
	}

	/**
	 * Returns the input file ArgumentDefinition of this mapping.
	 * 
	 * @return ArgumentDefinition<?>[]
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		FileArgumentDefinition filenameARG = new FileArgumentDefinition(
				FileArgumentDefinition.INPUT_FILE,
				FileArgumentDefinition.INPUT_FILE_DESC, true, true, false);
		return new ArgumentDefinition<?>[] { filenameARG };
	}

	/**
	 * No IndexONDEXGraph is required.
	 * 
	 * @return false
	 */
	public boolean requiresIndexedGraph() {
		return false;
	}

	/**
	 * Transfering a given EC number into a four position one (e.g. 1.2 ->
	 * 1.2.-.-)
	 * 
	 * @param s
	 *            EC number
	 * @return normalized four position EC number
	 */
	private String fillEC(String s) {

		String[] blocks = s.split("\\.");
		String news = s;

		if (blocks.length < 4) {

			for (int i = blocks.length; i < 4; i++) {

				news = news + ".-";
			}
		}
		return news;
	}

	public String[] requiresValidators() {
		return new String[0];
	}
}
