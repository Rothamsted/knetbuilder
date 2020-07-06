package net.sourceforge.ondex.parser.tsvparser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.tableparser.ArgumentNames;
import net.sourceforge.ondex.tools.subgraph.AttributePrototype;
import net.sourceforge.ondex.tools.subgraph.DefConst;
import net.sourceforge.ondex.tools.tab.importer.ConceptPrototype;
import net.sourceforge.ondex.tools.tab.importer.DataReader;
import net.sourceforge.ondex.tools.tab.importer.DelimitedReader;
import net.sourceforge.ondex.tools.tab.importer.PathParser;

/**
 * THIS IS A COPY OF THE ORIGINAL PARSER WITH LESS PARAMETERS.
 * 
 * @author lysenkoa
 *         <p/>
 *         Parses table-like data representations into Ondex. Currently
 *         supported supports delimited files and MS-EXCEL spreadsheets.
 *         WARNING: UNDER CONSTRUCTION!
 */
@Status(description = "Tested December 2013 (Jacek Grzebyta)", status = StatusType.STABLE)
public class Parser extends ONDEXParser implements ArgumentNames {
	// Map of prototypes
	private Map<String, ConceptPrototype> map = new HashMap<String, ConceptPrototype>();
	// PathParser
	private PathParser p;
	private String dataSource;

	/**
	 * Returns the list of required validators
	 */
	public String[] requiresValidators() {
		return new String[0];
	}

	/**
	 * PluginArgument definitions
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition[] {
				new StringArgumentDefinition(FIRST_DATA_ROW_ARG,
						FIRST_DATA_ROW_ARG_DESC, true, "1", false),
				new StringArgumentDefinition(LAST_ROW_ARG, LAST_ROW_ARG_DESC,
						false, null, false),
				new StringArgumentDefinition(CONCEPT_ATT, CONCEPT_ATT_DESC,
						false, null, true),
				new StringArgumentDefinition(CONCEPT_CLASS, CONCEPT_CLASS_DESC,
						false, "c1,Thing", true),
				new StringArgumentDefinition(DATASOURCE_ARG,
						DATASOURCE_ARG_DESC, false, "unknown", false),
				new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
						INPUT_FILE_DESC, true, true, false, false) };
	}

	/**
	 * Returns the name of the producer
	 */
	public String getName() {
		return "TSV file parser";
	}

	public String getVersion() {
		return "09.02.2012";
	}

	@Override
	public String getId() {
		return "tsvparser";
	}

	/**
	 * Starts the parser
	 */
	public void start() throws Exception {

		// file name
		File file = new File(
				(String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE));

		// first row in file
		int firstRow = 1;
		try {
			firstRow = Integer.valueOf(args.getUniqueValue(FIRST_DATA_ROW_ARG)
					.toString());
		} catch (Exception e) {
		}

		// last row in file
		int lastRow = Integer.MAX_VALUE;
		try {
			lastRow = Integer.valueOf(args.getUniqueValue(LAST_ROW_ARG)
					.toString());
		} catch (Exception e) {
		}

		// hard-coded for tab separated files
		DataReader reader = new DelimitedReader(file.getAbsolutePath(), "\t", firstRow);
		reader.setLine(firstRow);
		reader.setLastLine(lastRow);

		// parser file
		p = new PathParser(reader, graph);

		// get data source from arguments
		dataSource = safeGetStringAttribute(DATASOURCE_ARG);

		// check for concept classes
		Object[] classes = args.getObjectValueArray(CONCEPT_CLASS);
		if (classes != null) {
			for (Object cc : classes) {
				String[] tmp = cc.toString().split(",");
				// check correct length
				if (tmp.length < 2) {
					System.err
							.println("Warning - bad value for concept class specification ("
									+ cc.toString() + ") was ignored");
					continue;
				}
				// add concept class definition
				getConceptPrototype(tmp[0]).addAttributes(
						DefConst.defCC(tmp[1]));
			}
		}

		// check for attributes
		Object[] attributes = args.getObjectValueArray(CONCEPT_ATT);
		if (classes != null) {
			for (Object att : attributes) {
				String[] tmp = att.toString().split(",");
				// check correct length
				if (tmp.length < 3) {
					System.err
							.println("Warning - bad value for attribute specification ("
									+ att.toString() + ") was ignored");
					continue;
				}
				// check column value
				try {
					Integer.valueOf(tmp[1]);
				} catch (Exception e) {
					System.err
							.println("Warning - invalid column value in entry ("
									+ att.toString() + ") was ignored");
					continue;
				}
				// check attribute type
				Object[] data = AttributePrototype.getDefault(tmp[2]);
				if (data == null) {
					System.err
							.println("Warning - invalid attribute type in entry ("
									+ att.toString() + ") was ignored");
					continue;
				}
				// this is for concept attribute definition
				AttributePrototype ap = null;
				if (tmp[2].equals(AttributePrototype.DEFATTR)) {
					data[1] = tmp[3];
					data[2] = tmp[4];
					data[3] = Integer.valueOf(tmp[1]);
					ap = new AttributePrototype(data);
					if (tmp.length >= 6)
						ap.extractWithRegex(tmp[5], 1);
				}
				// this is for concept accession definition
				else if (tmp[2].equals(AttributePrototype.DEFACC)) {
					data[2] = tmp[3];
					data[3] = Integer.valueOf(tmp[1]);
					ap = new AttributePrototype(data);
					if (tmp.length >= 5)
						ap.extractWithRegex(tmp[4], 1);
				}
				// this is for concept name definition
				else if (tmp[2].equals(AttributePrototype.DEFNAME)) {
					for (int i = 3; i < tmp.length; i++) {
						data[i - 2] = tmp[i];
					}
					data[data.length - 1] = Integer.valueOf(tmp[1]);
					ap = new AttributePrototype(data);
					if (tmp.length >= 4)
						ap.extractWithRegex(tmp[3], 1);
				}
				if (ap != null)
					// add attribute to prototype
					getConceptPrototype(tmp[0]).addAttributes(ap);
			}
		}

		p.setProcessingOptions();
		p.parse();
	}

	/**
	 * Get the concept prototype that corresponds to the string
	 * 
	 * @param str
	 * @return
	 */
	private ConceptPrototype getConceptPrototype(String str) {
		ConceptPrototype result = map.get(str);
		if (result == null) {
			result = p.newConceptPrototype(new AttributePrototype(
					AttributePrototype.DEFDATASOURCE, dataSource));
			map.put(str, result);
		}
		return result;
	}

	/**
	 * Get the to string of the value with additional null check
	 * 
	 * @param key
	 * @return
	 */
	public String safeGetStringAttribute(String key)
			throws InvalidPluginArgumentException {
		Object arg = args.getUniqueValue(key);
		if (arg == null)
			return null;
		return arg.toString();
	}
}
