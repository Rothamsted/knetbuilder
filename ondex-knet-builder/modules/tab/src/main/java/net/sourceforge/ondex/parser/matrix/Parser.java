package net.sourceforge.ondex.parser.matrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.parser.ONDEXParser;

/**
 * Parses a comma-separated connectivity matrix.
 * 
 * @author taubertj
 * 
 */
public class Parser extends ONDEXParser implements ArgumentNames {

	// pattern to split on
	private final Pattern commaPattern = Pattern.compile(",");

	@Override
	public String getId() {
		return "matrix";
	}

	@Override
	public String getName() {
		return "Connectivity matrix parser";
	}

	@Override
	public String getVersion() {
		return "31.03.2011";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		StringArgumentDefinition taxId = new StringArgumentDefinition(
				TAXID_TO_USE_ARG, TAXID_TO_USE_ARG_DESC, false, null, false);

		StringArgumentDefinition cc = new StringArgumentDefinition(
				CONCEPT_CLASS_ARG, CONCEPT_CLASS_ARG_DESC, false, "Thing",
				false);

		StringArgumentDefinition ds = new StringArgumentDefinition(
				DATA_SOURCE_ARG, DATA_SOURCE_ARG_DESC, false, "unknown", false);

		StringArgumentDefinition relationType = new StringArgumentDefinition(
				RELATION_TYPE_ARG, RELATION_TYPE_ARG_DESC, false, "r", false);

		FileArgumentDefinition inputFile = new FileArgumentDefinition(
				FileArgumentDefinition.INPUT_FILE, INPUT_FILE_DESC, true, true,
				false, false);

		return new ArgumentDefinition[] { taxId, cc, ds, relationType,
				inputFile };
	}

	@Override
	public void start() throws Exception {

		// get file to parse
		String fileName = (String) args
				.getUniqueValue(FileArgumentDefinition.INPUT_FILE);
		File file = new File(fileName);
		if (!file.canRead()) {
			throw new FileNotFoundException("Cannot read file " + fileName);
		}

		this.fireEventOccurred(new GeneralOutputEvent("Parsing " + fileName,
				"[Parser - start]"));

		String taxId = (String) args.getUniqueValue(TAXID_TO_USE_ARG);

		// get concept class
		String ccName = (String) args.getUniqueValue(CONCEPT_CLASS_ARG);
		ConceptClass cc = graph.getMetaData().getConceptClass(ccName);
		if (cc == null)
			cc = graph.getMetaData().getFactory().createConceptClass(ccName);

		// get data source
		String dsName = (String) args.getUniqueValue(DATA_SOURCE_ARG);
		DataSource ds = graph.getMetaData().getDataSource(dsName);
		if (ds == null)
			ds = graph.getMetaData().getFactory().createDataSource(dsName);

		// get relation type
		String rtName = (String) args.getUniqueValue(RELATION_TYPE_ARG);
		RelationType rt = graph.getMetaData().getRelationType(rtName);
		if (rt == null)
			rt = graph.getMetaData().getFactory().createRelationType(rtName);

		// save taxonomy id
		AttributeName an = graph.getMetaData().getAttributeName("TAXID");

		// default evidence type
		EvidenceType evidence = graph.getMetaData().getEvidenceType("IMPD");

		// concepts indexed by their identifiers from matrix file
		Map<String, ONDEXConcept> concepts = new HashMap<String, ONDEXConcept>();

		// contains identifier at position i
		String[] positions = null;

		// open file for reading
		BufferedReader reader = new BufferedReader(new FileReader(file));
		int lineCount = 1;
		while (reader.ready()) {
			String line = reader.readLine();

			// split line at pattern
			String[] split = commaPattern.split(line);

			// first line use to initialise all concepts
			if (lineCount == 1) {

				// record identifier at position
				positions = new String[split.length];

				// parse all entities
				for (int i = 0; i < split.length; i++) {

					// record position
					positions[i] = split[i];

					// extract identifiers
					if (split[i].trim().length() > 0) {
						String name = split[i].trim();

						// transform into concept with identifier as name
						ONDEXConcept c = graph.getFactory().createConcept(name,
								ds, cc, evidence);
						c.createConceptName(name, true);

						if (taxId != null)
							c.createAttribute(an, taxId, false);

						concepts.put(name, c);
					}
				}

			} else {

				// retrieve from concept
				String name = split[0];
				ONDEXConcept from = concepts.get(name);
				if (from == null)
					throw new InconsistencyException("From concept not found: "
							+ name);

				// parse all connectivity
				for (int i = 1; i < split.length; i++) {

					// retrieve to concept
					name = positions[i];
					ONDEXConcept to = concepts.get(name);
					if (to == null)
						throw new InconsistencyException(
								"To concept not found: " + name);

					// parse connectivity value
					Double d = Double.parseDouble(split[i]);

					// if greater than 0 create new relation
					if (d > 0) {
						graph.getFactory().createRelation(from, to, rt,
								evidence);
					}
				}
			}

			lineCount++;
		}

		this.fireEventOccurred(new GeneralOutputEvent(
				"Finished processing matrix of row size " + lineCount,
				"[Parser - start]"));

	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

}
