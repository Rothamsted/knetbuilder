package net.sourceforge.ondex.parser.tab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.FloatRangeArgumentDefinition;
import net.sourceforge.ondex.args.IntegerRangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.parser.ONDEXParser;

/**
 * Parses relations from tabular files.
 * 
 * @author taubertj
 * @version 10.03.2008
 */
public class Parser extends ONDEXParser implements ArgumentNames,
		MetaData {

	/**
	 * No validators required.
	 * 
	 * @return String[]
	 */
	@Override
	public String[] requiresValidators() {
		return null;
	}

	/**
	 * Returns long list of ArgumentDefinitions to facilitate parsing of tabular
	 * file.
	 * 
	 * @return ArguementDefinition<?>[]
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {

		IntegerRangeArgumentDefinition skip = new IntegerRangeArgumentDefinition(
				SKIP_ARG, SKIP_ARG_DESC, false, 22, 0, Integer.MAX_VALUE);

		IntegerRangeArgumentDefinition fromCol = new IntegerRangeArgumentDefinition(
				FROM_COL_ARG, FROM_COL_ARG_DESC, true, 0, 0, Integer.MAX_VALUE);

		IntegerRangeArgumentDefinition toCol = new IntegerRangeArgumentDefinition(
				TO_COL_ARG, TO_COL_ARG_DESC, true, 1, 0, Integer.MAX_VALUE);

		IntegerRangeArgumentDefinition fromNameCol = new IntegerRangeArgumentDefinition(
				FROM_NAME_COL_ARG, FROM_NAME_COL_ARG_DESC, false, -1, -1,
				Integer.MAX_VALUE);

		IntegerRangeArgumentDefinition toNameCol = new IntegerRangeArgumentDefinition(
				TO_NAME_COL_ARG, TO_NAME_COL_ARG_DESC, false, -1, -1,
				Integer.MAX_VALUE);

		IntegerRangeArgumentDefinition fromPhenoCol = new IntegerRangeArgumentDefinition(
				FROM_PHENO_COL_ARG, FROM_PHENO_COL_ARG_DESC, false, -1, -1,
				Integer.MAX_VALUE);

		IntegerRangeArgumentDefinition toPhenoCol = new IntegerRangeArgumentDefinition(
				TO_PHENO_COL_ARG, TO_PHENO_COL_ARG_DESC, false, -1, -1,
				Integer.MAX_VALUE);

		IntegerRangeArgumentDefinition fromTaxidCol = new IntegerRangeArgumentDefinition(
				FROM_TAXID_COL_ARG, FROM_TAXID_COL_ARG_DESC, false, -1, -1,
				Integer.MAX_VALUE);

		IntegerRangeArgumentDefinition toTaxidCol = new IntegerRangeArgumentDefinition(
				TO_TAXID_COL_ARG, TO_TAXID_COL_ARG_DESC, false, -1, -1,
				Integer.MAX_VALUE);

		IntegerRangeArgumentDefinition confCol = new IntegerRangeArgumentDefinition(
				CONF_COL_ARG, CONF_COL_ARG_DESC, false, -1, -1,
				Integer.MAX_VALUE);

		StringArgumentDefinition taxId = new StringArgumentDefinition(
				TAXID_TO_USE_ARG, TAXID_TO_USE_ARG_DESC, false, null, false);

		StringArgumentDefinition cc = new StringArgumentDefinition(CC_ARG,
				CC_ARG_DESC, false, "Thing", false);

		StringArgumentDefinition cv = new StringArgumentDefinition(CV_ARG,
				CV_ARG_DESC, false, "unknown", false);

		StringArgumentDefinition relationType = new StringArgumentDefinition(
				RELATION_TYPE_ARG, RELATION_TYPE_ARG_DESC, false, "r", false);

		FloatRangeArgumentDefinition thres = new FloatRangeArgumentDefinition(
				CONF_THRESHOLD_ARG, CONF_THRESHOLD_ARG_DESC, false,
				Float.MIN_VALUE, Float.MIN_VALUE, Float.MAX_VALUE);

		FileArgumentDefinition inputFile = new FileArgumentDefinition(
				FileArgumentDefinition.INPUT_FILE, INPUT_FILE_DESC,
				true, true, false, false);

		return new ArgumentDefinition<?>[] { skip, fromCol, toCol, fromNameCol,
				toNameCol, fromPhenoCol, toPhenoCol, confCol, taxId, cc, cv,
				relationType, thres, fromTaxidCol, toTaxidCol, inputFile };
	}

	/**
	 * Returns name of this parser.
	 * 
	 * @return name of parser
	 */
	public String getName() {
		return "Tabular Parser";
	}

	/**
	 * Returns version of this parser.
	 * 
	 * @return version of parser
	 */
	public String getVersion() {
		return "10.03.2008";
	}

	@Override
	public String getId() {
		return "tab";
	}

	/**
	 * Does the actual parsing process.
	 */
	public void start() throws InvalidPluginArgumentException {

		int skip = 0;

		// -1 = undefined
		int fromCol = -1;
		int toCol = -1;
		int toNameCol = -1;
		int fromNameCol = -1;
		int toPhenoCol = -1;
		int fromPhenoCol = -1;
		int confCol = -1;
		int fromTaxidCol = -1;
		int toTaxidCol = -1;

		// meta data for ondex
		String taxId = null;
		ConceptClass cc = null;
		DataSource dataSource = null;
		RelationType rtset = null;

		// number of lines to skip, default = 0
		if (args.getUniqueValue(SKIP_ARG) != null)
			skip = (Integer) args.getUniqueValue(SKIP_ARG);

		// index of from concept parser id
		if (args.getUniqueValue(FROM_COL_ARG) != null)
			fromCol = (Integer) args.getUniqueValue(FROM_COL_ARG);

		// index of to concept parser id
		if (args.getUniqueValue(TO_COL_ARG) != null)
			toCol = (Integer) args.getUniqueValue(TO_COL_ARG);

		// index of concept name for from concept
		if (args.getUniqueValue(FROM_NAME_COL_ARG) != null)
			fromNameCol = (Integer) args.getUniqueValue(FROM_NAME_COL_ARG);

		// index of concept name for to concept
		if (args.getUniqueValue(TO_NAME_COL_ARG) != null)
			toNameCol = (Integer) args.getUniqueValue(TO_NAME_COL_ARG);

		// index of pheno Attribute for from concept
		if (args.getUniqueValue(FROM_PHENO_COL_ARG) != null)
			fromPhenoCol = (Integer) args.getUniqueValue(FROM_PHENO_COL_ARG);

		// index of pheno Attribute for to concept
		if (args.getUniqueValue(TO_PHENO_COL_ARG) != null)
			toPhenoCol = (Integer) args.getUniqueValue(TO_PHENO_COL_ARG);

		// index of taxid for from concept
		if (args.getUniqueValue(FROM_TAXID_COL_ARG) != null)
			fromTaxidCol = (Integer) args.getUniqueValue(FROM_TAXID_COL_ARG);

		// index of taxid Attribute for to concept
		if (args.getUniqueValue(TO_TAXID_COL_ARG) != null)
			toTaxidCol = (Integer) args.getUniqueValue(TO_TAXID_COL_ARG);

		// index of relation conf Attribute Col
		if (args.getUniqueValue(CONF_COL_ARG) != null)
			confCol = (Integer) args.getUniqueValue(CONF_COL_ARG);

		// taxonomy id for concepts
		if (args.getUniqueValue(TAXID_TO_USE_ARG) != null)
			taxId = (String) args.getUniqueValue(TAXID_TO_USE_ARG);

		// get user defined meta data
		rtset = graph.getMetaData().getRelationType(
				(String) args.getUniqueValue(RELATION_TYPE_ARG));
		cc = graph.getMetaData().getConceptClass(
				(String) args.getUniqueValue(CC_ARG));
		dataSource = graph.getMetaData().getDataSource((String) args.getUniqueValue(CV_ARG));

		if (rtset == null) {
			fireEventOccurred(new RelationTypeMissingEvent((String) args
					.getUniqueValue(RELATION_TYPE_ARG), "[Parser - start]"));
			return;
		}
		if (cc == null) {
			fireEventOccurred(new RelationTypeMissingEvent((String) args
					.getUniqueValue(CC_ARG), "[Parser - start]"));
			return;
		}
		if (dataSource == null) {
			fireEventOccurred(new RelationTypeMissingEvent((String) args
					.getUniqueValue(CV_ARG), "[Parser - start]"));
			return;
		}

		// get pre-defined meta data
		EvidenceType eviType = graph.getMetaData().getEvidenceType(evidence);
		AttributeName confAn = graph.getMetaData().getAttributeName(confAttr);
		AttributeName phenoAn = graph.getMetaData().getAttributeName(phenoAttr);
		AttributeName taxidAn = graph.getMetaData().getAttributeName(taxidAttr);

		if (eviType == null) {
			fireEventOccurred(new EvidenceTypeMissingEvent(evidence,
					"[Parser - start]"));
			return;
		}
		if (confAn == null) {
			fireEventOccurred(new AttributeNameMissingEvent(confAttr,
					"[Parser - start]"));
			return;
		}
		if (phenoAn == null) {
			fireEventOccurred(new AttributeNameMissingEvent(phenoAttr,
					"[Parser - start]"));
			return;
		}
		if (taxidAn == null) {
			fireEventOccurred(new AttributeNameMissingEvent(taxidAttr,
					"[Parser - start]"));
			return;
		}

		double threshold = Double.MIN_VALUE;
		if (args.getUniqueValue(CONF_THRESHOLD_ARG) != null) {
			threshold = ((Float) args.getUniqueValue(CONF_THRESHOLD_ARG))
					.doubleValue();
		}

		// get file to parse
		File file = new File((String) args
				.getUniqueValue(FileArgumentDefinition.INPUT_FILE));

		// keep track of already created concepts
		Map<String, ONDEXConcept> concepts = new Hashtable<String, ONDEXConcept>();

		try {
			// open file
			BufferedReader reader = new BufferedReader(new FileReader(file));

			// iterate over every line
			int i = 1;
			while (reader.ready()) {

				// skip lines at start of document
				String current = reader.readLine();
				if (i > skip && current.length() > 0) {
					String[] line = current.split("\t");
					String fromId = line[fromCol];
					String toId = line[toCol];

					// lookup previous from concept
					ONDEXConcept from = concepts.get(fromId);
					if (from == null) {
						// create new concept
						from = graph.getFactory().createConcept(fromId, dataSource, cc,
								eviType);
						// add parser id also as accession
						from.createConceptAccession(fromId, dataSource, false);
						// if name is available
						if (fromNameCol > -1 && fromNameCol < line.length
								&& line[fromNameCol].trim().length() > 0)
							from.createConceptName(line[fromNameCol], true);
						// if pheno Attribute is available
						if (fromPhenoCol > -1 && fromPhenoCol < line.length
								&& line[fromPhenoCol].trim().length() > 0)
							from.createAttribute(phenoAn, line[fromPhenoCol],
									false);
						// if there is some taxid
						if (taxId != null && fromTaxidCol == -1)
							from.createAttribute(taxidAn, taxId, false);
						if (fromTaxidCol > -1 && fromTaxidCol < line.length
								&& line[fromTaxidCol].trim().length() > 0)
							from.createAttribute(taxidAn, line[fromTaxidCol],
									false);

						concepts.put(fromId, from);
					}

					// lookup previous to concept
					ONDEXConcept to = concepts.get(toId);
					if (to == null) {
						// create new concept
						to = graph.getFactory().createConcept(toId, dataSource, cc,
								eviType);
						// add parser id also as accession
						to.createConceptAccession(toId, dataSource, false);
						// if name is available
						if (toNameCol > -1 && toNameCol < line.length
								&& line[toNameCol].trim().length() > 0)
							to.createConceptName(line[toNameCol], true);
						// if pheno Attribute is available
						if (toPhenoCol > -1 && toPhenoCol < line.length
								&& line[toPhenoCol].trim().length() > 0)
							to.createAttribute(phenoAn, line[toPhenoCol],
									false);
						// if there is some taxid
						if (taxId != null && toTaxidCol == -1)
							to.createAttribute(taxidAn, taxId, false);
						if (toTaxidCol > -1 && toTaxidCol < line.length
								&& line[toTaxidCol].trim().length() > 0)
							to.createAttribute(taxidAn, line[toTaxidCol],
									false);

						concepts.put(toId, to);
					}

					boolean above = false;
					boolean defined = false;
					double v = 0;
					if (confCol > -1 && confCol < line.length
							&& line[confCol].trim().length() > 0) {
						defined = true;
						v = Double.parseDouble(line[confCol]);
						if (v > threshold)
							above = true;
					}

					// finally create relation
					ONDEXRelation r = null;
					if (!defined || defined && above) {
						r = graph.getFactory().createRelation(from, to, rtset,
								eviType);
					}

					// if there is a conf value
					if (defined && above)
						r.createAttribute(confAn, Double.valueOf(v), false);

				}
				i++;
			}

			reader.close();
		} catch (FileNotFoundException fnfe) {
			fireEventOccurred(new DataFileMissingEvent(fnfe.getMessage(),
					"[Parser - start]"));
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					"[Parser - start]"));
		}

	}
}
