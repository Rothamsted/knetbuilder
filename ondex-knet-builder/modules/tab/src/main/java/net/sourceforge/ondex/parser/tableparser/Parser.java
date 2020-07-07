package net.sourceforge.ondex.parser.tableparser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.tableparser.tableEmulators.MSExcelEmulator;
import net.sourceforge.ondex.tools.subgraph.AttributePrototype;
import net.sourceforge.ondex.tools.subgraph.DefConst;
import net.sourceforge.ondex.tools.tab.importer.ConceptPrototype;
import net.sourceforge.ondex.tools.tab.importer.DataReader;
import net.sourceforge.ondex.tools.tab.importer.DelimitedReader;
import net.sourceforge.ondex.tools.tab.importer.PathParser;

/**
 * @author lysenkoa
 *         <p/>
 *         Parses table-like data representations into Ondex. Currently
 *         supported supports delimited files and MS-EXCEL spreadsheets.
 *         WARNING: UNDER CONSTRUCTION!
 */
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
				new StringArgumentDefinition(SHEET_ARG, SHEET_ARG_DESC, false,
						null, false),
				new StringArgumentDefinition(COLUMN_SEPARATOR_REGEX,
						COLUMN_SEPARATOR_REGEX_DESC, false, "\t", false),
				new StringArgumentDefinition(FIRST_DATA_ROW_ARG,
						FIRST_DATA_ROW_ARG_DESC, true, "1", false),
				new StringArgumentDefinition(LAST_ROW_ARG, LAST_ROW_ARG_DESC,
						true, null, false),
				new StringArgumentDefinition(CONCEPT_ATT, CONCEPT_ATT_DESC,
						false, null, true),
				new StringArgumentDefinition(CONCEPT_CLASS, CONCEPT_CLASS_DESC,
						false, "Thing", true),
				new StringArgumentDefinition(DATASOURCE_ARG, DATASOURCE_ARG_DESC,
						false, "unknown", false),
				new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
						INPUT_FILE_DESC, true, true,
						false, false) };
	}

	/**
	 * Returns the name of the producer
	 */
	public String getName() {
		return "Table format parser";
	}

	public String getVersion() {
		return "27.11.2010";
	}

	@Override
	public String getId() {
		return "tableparser";
	}

	/**
	 * Starts the parser
	 */
	public void start() throws Exception {
		File file = new File((String) args
				.getUniqueValue(FileArgumentDefinition.INPUT_FILE));
		if (file == null)
			throw new Exception("File name was not set.");
		String sheetName = safeGetStringAttribute(SHEET_ARG);
		String colRegex = safeGetStringAttribute(COLUMN_SEPARATOR_REGEX);
		int firstRow = 1;
		try {
			firstRow = Integer.valueOf(args.getUniqueValue(FIRST_DATA_ROW_ARG)
					.toString()) - 1;
		} catch (Exception e) {
		}
		int lastRow = Integer.MAX_VALUE;
		try {
			lastRow = Integer.valueOf(args.getUniqueValue(LAST_ROW_ARG)
					.toString()) - 1;
		} catch (Exception e) {
		}
		DataReader reader = null;
		if (file.getName().endsWith(".xls") || sheetName != null) {
			reader = new MSExcelEmulator(file.getAbsolutePath(), sheetName);
		} else if (colRegex != null) {
			reader = new DelimitedReader(file.getAbsolutePath(), colRegex);
		} else {
			throw new Exception(
					"Table parser is missin required argument - column separator was not specified.");
		}
		reader.setLine(firstRow);
		reader.setLastLine(lastRow);
		p = new PathParser(reader, graph);
		
		dataSource = safeGetStringAttribute(DATASOURCE_ARG);
		
		Object[] classes = args.getObjectValueArray(CONCEPT_CLASS);
		if (classes != null) {
			for (Object cc : classes) {
				String[] tmp = cc.toString().split(",");
				if (tmp.length < 2) {
					System.err
							.println("Warning - bad value for concpet class specification ("
									+ cc.toString() + ")was ignored");
					continue;
				}
				getConceptPrototype(tmp[0]).addAttributes(
						DefConst.defCC(tmp[1]));
			}
		}

		Object[] attributes = args.getObjectValueArray(CONCEPT_ATT);
		if (classes != null) {
			for (Object att : attributes) {
				String[] tmp = att.toString().split(",");
				if (tmp.length < 3) {
					System.err
							.println("Warning - bad value for concpet class specification ("
									+ att.toString() + ") was ignored");
					continue;
				}
				try {
					Integer.valueOf(tmp[1]);
				} catch (Exception e) {
					System.err
							.println("Warning - invalid column value in entry ("
									+ att.toString() + ") was ignored");
					continue;
				}
				Object[] data = AttributePrototype.getDefault(tmp[2]);
				if (data == null) {
					System.err
							.println("Warning - invalid attribute type in entry ("
									+ att.toString() + ") was ignored");
					continue;
				}
				AttributePrototype ap;
				if (tmp[2].equals(AttributePrototype.DEFATTR)) {
					data[1] = tmp[3];
					data[2] = tmp[4];
					data[3] = Integer.valueOf(tmp[1]);
					ap=new AttributePrototype(data);
					if(tmp.length>=6)
						ap.extractWithRegex(tmp[5],1);
				} else if (tmp[2].equals(AttributePrototype.DEFACC)) {
					data[2] = tmp[3];
					data[3] = Integer.valueOf(tmp[1]);
					ap=new AttributePrototype(data);
					if(tmp.length>=5)
						ap.extractWithRegex(tmp[4],1);
				} else {
					for (int i = 3; i < tmp.length; i++) {
						data[i - 2] = tmp[i];
					}
					data[data.length - 1] = Integer.valueOf(tmp[1]);
					ap=new AttributePrototype(data);
					if (tmp[2].equals(AttributePrototype.DEFNAME)) {
						if(tmp.length>=4)
							ap.extractWithRegex(tmp[3],1);
					}
				}
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
			result = p.newConceptPrototype(new AttributePrototype(AttributePrototype.DEFDATASOURCE, dataSource));
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
