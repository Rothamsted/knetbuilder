package net.sourceforge.ondex.parser.prolog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationKey;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.base.RelationKeyImpl;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.export.prolog.Export;
import net.sourceforge.ondex.marshal.Marshaller;
import net.sourceforge.ondex.parser.ONDEXParser;

/**
 * Parses Ondex formated prolog syntax into an Ondex graph.
 * 
 * @author taubertj
 * 
 */
public class Parser extends ONDEXParser implements ArgumentNames {

	/**
	 * Track errors during import.
	 */
	private StringBuffer errorReport = null;

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		ArgumentDefinition<?>[] args = new ArgumentDefinition<?>[] {
				new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
						"Prolog file in Ondex format to import", true, true,
						false, false),
				new FileArgumentDefinition(ERRORLOG_ARG, ERRORLOG_ARG_DESC,
						false, false, false, false) };
		return args;
	}

	@Override
	public String getId() {
		return "prolog";
	}

	@Override
	public String getName() {
		return "Prolog Importer";
	}

	@Override
	public String getVersion() {
		return "17.11.2010";
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	@Override
	public void start() throws Exception {
		errorReport = new StringBuffer();

		File file = new File(
				(String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE));
		fireEventOccurred(new GeneralOutputEvent("Parsing "
				+ file.getAbsolutePath(), "[Parser - start]"));
		parseFile(file);

		// output optional log file
		if (args.getOptions().containsKey(ERRORLOG_ARG)) {
			file = new File((String) args.getUniqueValue(ERRORLOG_ARG));
			fireEventOccurred(new GeneralOutputEvent("Saving log to "
					+ file.getAbsolutePath(), "[Parser - start]"));
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(errorReport.toString());
			writer.flush();
			writer.close();
		}
	}

	/**
	 * Returns accumulated errors.
	 * 
	 * @return error log
	 */
	public String getErrorReport() {
		if (errorReport == null)
			return "";
		return errorReport.toString();
	}

	/**
	 * Parses the content of file in PROLOG format.
	 */
	private void parseFile(File file) {

		// get meta data
		ONDEXGraphMetaData meta = graph.getMetaData();
		addAppearanceAttributes(meta);

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));

			// contains primitives clauses indexed by clause name
			Map<String, List<String[]>> primitives = new HashMap<String, List<String[]>>();

			while (reader.ready()) {
				String line = reader.readLine();

				// skip comments
				if (line.startsWith("%")) {
					continue;
				}

				// skip rules for the moment, do not parse empty lines
				if (!line.contains(" :- ") && line.endsWith(").")) {

					// a clause name always from beginning until (
					String clauseName = line.substring(0, line.indexOf("("));
					if (!primitives.containsKey(clauseName)) {
						primitives.put(clauseName, new ArrayList<String[]>());
					}

					// special cases with no quotes at the end
					if (clauseName.startsWith("concept_accession")
							|| clauseName.startsWith("concept_name")) {

						// last argument does not have ', so split differently
						String[] split = line.substring(line.indexOf("(") + 1,
								line.lastIndexOf(").")).split("',");
						// strip first '
						for (int i = 0; i < split.length - 1; i++)
							split[i] = split[i].substring(1);
						primitives.get(clauseName).add(split);

					} else if (clauseName.startsWith("concept_")) {

						String[] temp = new String[3];

						// concept reference
						temp[0] = line.substring(line.indexOf("(") + 2,
								line.indexOf("',"));
						line = line.substring(line.indexOf("',") + 2,
								line.length());

						// parse value
						if (line.startsWith("'")) {
							temp[1] = line.substring(1, line.indexOf("',"));
							line = line.substring(line.indexOf("',") + 2);
						} else {
							temp[1] = line.substring(0, line.indexOf(","));
							line = line.substring(line.indexOf(",") + 1,
									line.length());
						}

						temp[2] = line.substring(0, line.indexOf(")"));
						primitives.get(clauseName).add(temp);

					} else if (clauseName.startsWith("relation_")) {

						String[] temp = new String[5];

						// relation type
						temp[0] = line.substring(line.indexOf("(") + 1,
								line.indexOf(",'"));
						line = line.substring(line.indexOf(",'") + 1);

						// from reference
						temp[1] = line.substring(1, line.indexOf("','"));
						line = line.substring(line.indexOf("','") + 2);

						// to reference
						temp[2] = line.substring(1, line.indexOf("',"));
						line = line.substring(line.indexOf("',") + 2);

						// parse value
						if (line.startsWith("'")) {
							temp[3] = line.substring(1, line.indexOf("',"));
							line = line.substring(line.indexOf("',") + 2);
						} else {
							temp[3] = line.substring(0, line.indexOf(","));
							line = line.substring(line.indexOf(",") + 1);
						}

						temp[4] = line.substring(0, line.indexOf(")"));
						primitives.get(clauseName).add(temp);

					} else {

						// arguments are comma separated
						String[] split = line.substring(line.indexOf("(") + 1,
								line.lastIndexOf(").")).split("','");
						// strip first '
						split[0] = split[0].substring(1, split[0].length());
						// strip last '
						split[split.length - 1] = split[split.length - 1]
								.substring(0,
										split[split.length - 1].length() - 1);
						primitives.get(clauseName).add(split);
					}
				}
			}

			// for (String clauseName : primitives.keySet()) {
			// System.out.println(clauseName);
			// for (String[] clause : primitives.get(clauseName)) {
			// for (String argument : clause) {
			// System.out.print("\t" + argument);
			// }
			// System.out.println();
			// }
			// }

			// get meta data from graph
			Map<String, ConceptClass> conceptClasses = getConceptClasses(meta);
			Map<String, RelationType> relationTypes = getRelationTypes(meta);
			Map<String, AttributeName> attributeNames = getAttributeNames(meta);

			// parse ONDEXConcepts first
			Map<String, ONDEXConcept> concepts = parseConcept(graph,
					conceptClasses, primitives);

			// add concept names
			parseConceptNames(concepts, primitives);

			// add concept accessions
			parseConceptAccessions(concepts, primitives);

			// add possible Attribute
			parseConceptGDS(concepts, attributeNames, primitives);

			// parse ONDEXRelations next
			Map<RelationKey, ONDEXRelation> relations = parseRelation(graph,
					concepts, relationTypes, primitives);

			// finally add most complex relation Attribute
			parseRelationGDS(relations, concepts, relationTypes,
					attributeNames, primitives);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Initialises OVTK2 appearance specific attribute names.
	 * 
	 * @param meta
	 *            ONDEXGraphMetaData
	 */
	private void addAppearanceAttributes(ONDEXGraphMetaData meta) {
		// check for attribute names
		if (meta.getAttributeName("graphicalX") == null)
			meta.getFactory().createAttributeName("graphicalX", Double.class);
		if (meta.getAttributeName("graphicalY") == null)
			meta.getFactory().createAttributeName("graphicalY", Double.class);
		if (meta.getAttributeName("color") == null)
			meta.getFactory()
					.createAttributeName("color", java.awt.Color.class);
		if (meta.getAttributeName("shape") == null)
			meta.getFactory().createAttributeName("shape",
					java.lang.Integer.class);
		if (meta.getAttributeName("visible") == null)
			meta.getFactory().createAttributeName("visible",
					java.lang.Boolean.class);
		if (meta.getAttributeName("size") == null)
			meta.getFactory().createAttributeName("size",
					java.lang.Integer.class);

	}

	/**
	 * Simply adds relation Attribute to the relations.
	 * 
	 * @param relations
	 *            ONDEXConcepts indexed by first argument
	 * @param concepts
	 *            ONDEXConcepts indexed by first argument
	 * @param relationTypes
	 *            all relation types indexed by id or full name
	 * @param attributeNames
	 *            all attribute names indexed
	 * @param primitives
	 *            all primitive clauses indexed by clause name
	 */
	private void parseRelationGDS(Map<RelationKey, ONDEXRelation> relations,
			Map<String, ONDEXConcept> concepts,
			Map<String, RelationType> relationTypes,
			Map<String, AttributeName> attributeNames,
			Map<String, List<String[]>> primitives) {

		// relation Attribute starts with relation_ + attribute name
		for (String key : primitives.keySet()) {
			if (key.startsWith("relation_")) {

				// extract attribute name id
				String anID = key.substring(9, key.length());
				if (!attributeNames.containsKey(anID)) {
					errorReport.append("\n");
					errorReport
							.append("parserRelationGDS: AttributeName for RelationAttribute not found: ");
					errorReport.append(anID);
					attributeNames.put(anID, graph.getMetaData().getFactory()
							.createAttributeName(anID, String.class));
					System.err.println("Dummy AttributeName " + anID
							+ " created.");
				}

				// get attribute name and data type
				AttributeName an = attributeNames.get(anID);
				Class<?> dataType = an.getDataType();
				for (String[] arguments : primitives.get(key)) {

					// lookup relation type in dictionary
					RelationType rt = relationTypes.get(arguments[0]);
					if (rt == null) {
						errorReport.append("\n");
						errorReport
								.append("parserRelationGDS: RelationType not found for reference: ");
						errorReport.append(arguments[0]);
						rt = graph.getMetaData().getFactory()
								.createRelationType(arguments[0]);
						System.err
								.println("parserRelationGDS: Dummy RelationType "
										+ arguments[0] + " created.");
					}

					// lookup internal reference to get from concept
					ONDEXConcept from = concepts.get(arguments[1]);
					if (from == null) {
						errorReport.append("\n");
						errorReport
								.append("parserRelationGDS: From concept not found for reference: ");
						errorReport.append(arguments[1]);
						continue;
					}

					// lookup internal reference to get to concept
					ONDEXConcept to = concepts.get(arguments[2]);
					if (to == null) {
						errorReport.append("\n");
						errorReport
								.append("parserRelationGDS: To concept not found for reference: ");
						errorReport.append(arguments[2]);
						continue;
					}

					// construct relation key
					RelationKey relKey = new RelationKeyImpl(from.getSID(),
							from.getId(), to.getId(), rt.getId());

					// lookup internal reference for relation
					ONDEXRelation relation = relations.get(relKey);
					if (relation == null) {
						errorReport.append("\n");
						errorReport
								.append("parserRelationGDS: Relation not found for reference: ");
						errorReport.append(relKey);
						continue;
					}

					// if decomposition successful create Attribute
					Object o = decideDecompose(arguments[3], dataType);
					if (o != null) {
						relation.createAttribute(an, o,
								Boolean.parseBoolean(arguments[4]));
					} else {
						errorReport.append("\n");
						errorReport
								.append("parserRelationGDS: Could not parse object from: ");
						errorReport.append(arguments[3]);
					}
				}
			}
		}
	}

	/**
	 * Simply adds concept Attribute to the concepts.
	 * 
	 * @param concepts
	 *            ONDEXConcepts indexed by first argument
	 * @param attributeNames
	 *            all attribute names indexed
	 * @param primitives
	 *            all primitive clauses indexed by clause name
	 */
	private void parseConceptGDS(Map<String, ONDEXConcept> concepts,
			Map<String, AttributeName> attributeNames,
			Map<String, List<String[]>> primitives) {

		// concept Attribute starts with concept_ + attribute name
		for (String key : primitives.keySet()) {
			if (key.startsWith("concept_") && !key.startsWith("concept_name")
					&& !key.startsWith("concept_accession")) {

				// extract attribute name id
				String anID = key.substring(8, key.length());
				if (!attributeNames.containsKey(anID)) {
					errorReport.append("\n");
					errorReport
							.append("parseConceptGDS: AttributeName for ConceptAttribute not found: ");
					errorReport.append(anID);
					attributeNames.put(anID, graph.getMetaData().getFactory()
							.createAttributeName(anID, String.class));
					errorReport.append("\n");
					errorReport.append("parseConceptGDS: Dummy AttributeName "
							+ anID + " created.");
				}

				// get attribute name and data type
				AttributeName an = attributeNames.get(anID);
				Class<?> dataType = an.getDataType();
				for (String[] arguments : primitives.get(key)) {

					// lookup internal reference to get from concept
					ONDEXConcept concept = concepts.get(arguments[0]);
					if (concept == null) {
						errorReport.append("\n");
						errorReport
								.append("parseConceptGDS: Concept not found for reference: ");
						errorReport.append(arguments[0]);
						continue;
					}

					// if decomposition successful create Attribute
					Object o = decideDecompose(arguments[1], dataType);
					if (o != null) {
						concept.createAttribute(an, o,
								Boolean.parseBoolean(arguments[2]));
					} else {
						errorReport.append("\n");
						errorReport
								.append("parseConceptGDS: Could not parse object from: ");
						errorReport.append(arguments[1]);
					}
				}
			}
		}
	}

	/**
	 * Propagates decomposition of string to object according to data type
	 * 
	 * @param s
	 *            object as String
	 * @param dataType
	 *            data type to become
	 * @return Object of data type
	 */
	private Object decideDecompose(String s, Class<?> dataType) {
		// decide how to handle Attribute value
		Object o = null;
		if (String.class.isAssignableFrom(dataType)) {
			o = decomposeString(s);
		} else if (Integer.class.isAssignableFrom(dataType)) {
			o = decomposeInteger(s);
		} else if (Float.class.isAssignableFrom(dataType)) {
			o = decomposeFloat(s);
		} else if (Double.class.isAssignableFrom(dataType)) {
			o = decomposeDouble(s);
		} else if (Boolean.class.isAssignableFrom(dataType)) {
			o = decomposeBoolean(s);
		} else {
			o = decomposeObject(s);
		}
		return o;
	}

	private Object decomposeObject(String s) {
		try {
			return Marshaller.getMarshaller().fromXML(s);
		} catch (Exception e) {
			errorReport.append("\n");
			errorReport.append("decomposeObject: Error dewrapping XML: " + s);
			return null;
		}
	}

	private Object decomposeBoolean(String s) {
		return new Boolean(Boolean.parseBoolean(s));
	}

	private Object decomposeDouble(String s) {
		try {
			return new Double(Double.parseDouble(s));
		} catch (NumberFormatException nfe) {
			errorReport.append("\n");
			errorReport.append("decomposeDouble: Wrong NumberFormat for: " + s);
			return null;
		}
	}

	private Object decomposeFloat(String s) {
		try {
			return new Float(Float.parseFloat(s));
		} catch (NumberFormatException nfe) {
			errorReport.append("\n");
			errorReport.append("decomposeFloat: Wrong NumberFormat for: " + s);
			return null;
		}
	}

	private Object decomposeInteger(String s) {
		try {
			return new Integer(Integer.parseInt(s));
		} catch (NumberFormatException nfe) {
			errorReport.append("\n");
			errorReport
					.append("decomposeInteger: Wrong NumberFormat for: " + s);
			return null;
		}
	}

	private Object decomposeString(String s) {
		return s;
	}

	/**
	 * Simply adds concept accessions to the concepts.
	 * 
	 * @param concepts
	 *            ONDEXConcepts indexed by first argument
	 * @param primitives
	 *            all primitive clauses indexed by clause name
	 */
	private void parseConceptAccessions(Map<String, ONDEXConcept> concepts,
			Map<String, List<String[]>> primitives) {

		// special clause name for concept accessions
		if (primitives.containsKey("concept_accession")) {
			for (String[] arguments : primitives.get("concept_accession")) {

				// get concept for internal reference
				ONDEXConcept concept = concepts.get(arguments[0]);
				if (concept == null) {
					errorReport.append("\n");
					errorReport
							.append("parseConceptAccessions: Concept not found for reference: ");
					errorReport.append(arguments[0]);
					continue;
				}

				// get DataSource for ID
				DataSource dataSource = graph.getMetaData().getDataSource(
						arguments[1]);
				if (dataSource == null) {
					errorReport.append("\n");
					errorReport
							.append("parseConceptAccessions: DataSource not found for ID: ");
					errorReport.append(arguments[1]);
					dataSource = graph.getMetaData().getFactory()
							.createDataSource(arguments[1]);
					errorReport.append("\n");
					errorReport
							.append("parseConceptAccessions: Dummy DataSource "
									+ arguments[1] + " created.");
				}

				// create concept accession, parse preferred boolean from last
				// argument
				concept.createConceptAccession(arguments[2], dataSource,
						Boolean.parseBoolean(arguments[3]));
			}
		}
	}

	/**
	 * Simply adds concept names to the concepts.
	 * 
	 * @param concepts
	 *            ONDEXConcepts indexed by first argument
	 * @param primitives
	 *            all primitive clauses indexed by clause name
	 */
	private void parseConceptNames(Map<String, ONDEXConcept> concepts,
			Map<String, List<String[]>> primitives) {

		// special clause name for concept names
		if (primitives.containsKey("concept_name")) {
			for (String[] arguments : primitives.get("concept_name")) {

				// get concept for internal reference
				ONDEXConcept concept = concepts.get(arguments[0]);
				if (concept == null) {
					errorReport.append("\n");
					errorReport
							.append("parseConceptNames: Concept not found for reference: ");
					errorReport.append(arguments[0]);
					continue;
				}

				// create concept name, parse preferred boolean from last
				// argument
				concept.createConceptName(arguments[1],
						Boolean.parseBoolean(arguments[2]));
			}
		}
	}

	/**
	 * Parse relations from primitives. The clause name has to match to a
	 * relation type.
	 * 
	 * @param aog
	 *            ONDEXGraph
	 * @param concepts
	 *            ONDEXConcepts indexed by first argument
	 * @param relationTypes
	 *            all relation types indexed by id or full name
	 * @param primitives
	 *            all primitive clauses indexed by clause name
	 * @return ONDEXRelations indexed by RelationKey
	 */
	private Map<RelationKey, ONDEXRelation> parseRelation(ONDEXGraph aog,
			Map<String, ONDEXConcept> concepts,
			Map<String, RelationType> relationTypes,
			Map<String, List<String[]>> primitives) {
		Map<RelationKey, ONDEXRelation> relations = new HashMap<RelationKey, ONDEXRelation>();

		// look for matching primitives key in relation types
		for (String key : primitives.keySet()) {

			for (String[] arguments : primitives.get(key)) {

				// check conditions for a relation
				if (arguments.length == 3 && !key.startsWith("concept_")
						&& !key.startsWith("relation_")) {

					// create missing relation type
					if (!relationTypes.containsKey(key)) {
						errorReport.append("\n");
						errorReport
								.append("parseRelation: RelationType not found for ID: ");
						errorReport.append(key);
						relationTypes.put(key, aog.getMetaData().getFactory()
								.createRelationType(key));
						errorReport.append("\n");
						errorReport.append("parseRelation: Dummy RelationType "
								+ key + " created.");
					}

					// get relation type from cache
					RelationType rt = relationTypes.get(key);

					// check for empty EvidenceType
					if (arguments[2].trim().length() == 0)
						arguments[2] = "IMPD";

					// get EvidenceType for ID
					EvidenceType et = aog.getMetaData().getEvidenceType(
							arguments[2]);
					if (et == null) {
						errorReport.append("\n");
						errorReport
								.append("parseRelation: EvidenceType not found for ID: ");
						errorReport.append(arguments[2]);
						et = aog.getMetaData().getFactory()
								.createEvidenceType(arguments[2]);
						errorReport.append("\n");
						errorReport.append("parseRelation: Dummy EvidenceType "
								+ arguments[2] + " created.");
					}

					// lookup internal reference to get from concept
					ONDEXConcept from = concepts.get(arguments[0]);
					if (from == null) {
						errorReport.append("\n");
						errorReport
								.append("parseRelation: From concept not found for reference: ");
						errorReport.append(arguments[0]);
						continue;
					}

					// lookup internal reference to get to concept
					ONDEXConcept to = concepts.get(arguments[1]);
					if (to == null) {
						errorReport.append("\n");
						errorReport
								.append("parseRelation: To concept not found for reference: ");
						errorReport.append(arguments[1]);
						continue;
					}

					// construct relation and index by relation key
					ONDEXRelation relation = aog.getFactory().createRelation(
							from, to, rt, et);
					relations.put(relation.getKey(), relation);
				}
			}
		}

		return relations;
	}

	/**
	 * Parses concepts from primitives. The clause name has to match to a
	 * concept class.
	 * 
	 * @param aog
	 *            ONDEXGraph
	 * @param conceptClasses
	 *            all concept classes indexed by id or full name
	 * @param primitives
	 *            all primitive clauses indexed by clause name
	 * @return ONDEXConcepts indexed by first argument
	 */
	private Map<String, ONDEXConcept> parseConcept(ONDEXGraph aog,
			Map<String, ConceptClass> conceptClasses,
			Map<String, List<String[]>> primitives) {
		Map<String, ONDEXConcept> concepts = new HashMap<String, ONDEXConcept>();

		// look for matching primitives key in concept classes
		for (String key : primitives.keySet()) {

			for (String[] arguments : primitives.get(key)) {

				// check conditions for a concept
				if (arguments.length == 5 && !key.startsWith("concept_")
						&& !key.startsWith("relation_")) {

					// create missing concept class
					if (!conceptClasses.containsKey(key)) {
						errorReport.append("\n");
						errorReport
								.append("parseConcept: ConceptClass not found for ID: ");
						errorReport.append(key);
						conceptClasses.put(key, aog.getMetaData().getFactory()
								.createConceptClass(key));
						errorReport.append("\n");
						errorReport.append("parseConcept: Dummy ConceptClass "
								+ key + " created.");
					}

					// get concept class from cache
					ConceptClass cc = conceptClasses.get(key);

					// check for empty DataSource
					if (arguments[3].trim().length() == 0)
						arguments[3] = "UC";

					// get DataSource for ID
					DataSource dataSource = aog.getMetaData().getDataSource(
							arguments[3]);
					if (dataSource == null) {
						errorReport.append("\n");
						errorReport
								.append("parseConcept: DataSource not found for ID: ");
						errorReport.append(arguments[3]);
						dataSource = aog.getMetaData().getFactory()
								.createDataSource(arguments[3]);
						errorReport.append("\n");
						errorReport.append("parseConcept: Dummy DataSource "
								+ arguments[3] + " created.");
					}

					// check for empty EvidenceType
					if (arguments[4].trim().length() == 0)
						arguments[4] = "IMPD";

					// get EvidenceType for ID
					EvidenceType et = aog.getMetaData().getEvidenceType(
							arguments[4]);
					if (et == null) {
						errorReport.append("\n");
						errorReport
								.append("parseConcept: EvidenceType not found for ID: ");
						errorReport.append(arguments[4]);
						et = aog.getMetaData().getFactory()
								.createEvidenceType(arguments[4]);
						errorReport.append("\n");
						errorReport.append("parseConcept: Dummy EvidenceType "
								+ arguments[4] + " created.");
					}

					if (concepts.containsKey(arguments[0])) {

						// multiple evidence types per concept
						ONDEXConcept concept = concepts.get(arguments[0]);
						concept.addEvidenceType(et);

					} else {

						// parse possible concatenate parser ID
						String pid = "";
						if (arguments[0].contains(": "))
							pid = arguments[0].substring(arguments[0]
									.indexOf(": ") + 2);

						// construct concept and index by first argument
						ONDEXConcept concept = aog.getFactory().createConcept(
								pid, arguments[1], arguments[2], dataSource,
								cc, et);
						concepts.put(arguments[0], concept);
					}
				}
			}
		}

		return concepts;
	}

	/**
	 * Return all concept classes from meta data either indexed by full name or
	 * id.
	 * 
	 * @param meta
	 *            ONDEXGraphMetaData
	 * @return all concept classes indexed
	 */
	private Map<String, ConceptClass> getConceptClasses(ONDEXGraphMetaData meta) {
		Map<String, ConceptClass> conceptClasses = new HashMap<String, ConceptClass>();
		for (ConceptClass cc : meta.getConceptClasses()) {
			String name = cc.getFullname();
			if (name == null || name.trim().length() == 0)
				name = cc.getId();
			name = Export.makeLower(name);
			if (conceptClasses.containsKey(name)) {
				errorReport.append("\n");
				errorReport
						.append("getConceptClasses: Lower case concept class id already exists: ");
				errorReport.append(name);
			}
			conceptClasses.put(name, cc);
			conceptClasses.put(Export.makeLower(cc.getId()), cc);
		}
		return conceptClasses;
	}

	/**
	 * Return all relation types from meta data either indexed by full name or
	 * id.
	 * 
	 * @param meta
	 *            ONDEXGraphMetaData
	 * @return all relation types indexed
	 */
	private Map<String, RelationType> getRelationTypes(ONDEXGraphMetaData meta) {
		Map<String, RelationType> relationTypes = new HashMap<String, RelationType>();
		for (RelationType rt : meta.getRelationTypes()) {
			String name = rt.getFullname();
			if (name == null || name.trim().length() == 0)
				name = rt.getId();
			name = Export.makeLower(name);
			if (relationTypes.containsKey(name)) {
				errorReport.append("\n");
				errorReport
						.append("getRelationTypes: Lower case relation type id already exists: ");
				errorReport.append(name);
			}
			relationTypes.put(name, rt);
			relationTypes.put(Export.makeLower(rt.getId()), rt);
		}
		return relationTypes;
	}

	/**
	 * Return all attribute names from meta data either indexed by full name or
	 * id.
	 * 
	 * @param meta
	 *            ONDEXGraphMetaData
	 * @return all attribute names indexed
	 */
	private Map<String, AttributeName> getAttributeNames(ONDEXGraphMetaData meta) {
		Map<String, AttributeName> attributeNames = new HashMap<String, AttributeName>();
		for (AttributeName an : meta.getAttributeNames()) {
			String name = an.getFullname();
			if (name == null || name.trim().length() == 0)
				name = an.getId();
			name = Export.makeLower(name);
			if (attributeNames.containsKey(name)) {
				errorReport.append("\n");
				errorReport
						.append("getAttributeNames: Lower case attribute name id already exists: ");
				errorReport.append(name);
			}
			attributeNames.put(name, an);
			attributeNames.put(Export.makeLower(an.getId()), an);
		}
		return attributeNames;
	}

}
