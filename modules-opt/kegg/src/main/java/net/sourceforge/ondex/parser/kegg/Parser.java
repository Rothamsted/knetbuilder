package net.sourceforge.ondex.parser.kegg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.kegg.kgml.EntryParser;
import net.sourceforge.ondex.parser.kegg.kgml.KgmlParser;
import net.sourceforge.ondex.parser.kegg.kgml.PathwayParser;
import net.sourceforge.ondex.parser.kegg.kgml.ReactionParser;

/**
 * Parser for the KEGG database. Fully utilises ternary relations and context
 * lists.
 * 
 * @author taubertj
 */
@Status(description = "Under development see: Jan Taubert", status = StatusType.EXPERIMENTAL)
@DatabaseTarget(name = "KEGG", description = "The KEGG database", version = "Release 56.0", url = "http://www.genome.jp/kegg")
@Authors(authors = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Custodians(custodians = { "Shaochih Kuo" }, emails = { "sckuo at users.sourceforge.net" })
public class Parser extends ONDEXParser {

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	/**
	 * Returns name of parser.
	 * 
	 * @return String
	 */
	public String getName() {
		return "KEGG map parser";
	}

	/**
	 * Returns version of parser.
	 * 
	 * @return String
	 */
	public String getVersion() {
		return "11.11.2011";
	}

	@Override
	public String getId() {
		return "keggmap";
	}

	/**
	 * Returns list of arguments.
	 * 
	 * @return ArgumentDefinition<?>[]
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {

		ArrayList<ArgumentDefinition<?>> args = new ArrayList<ArgumentDefinition<?>>();
		args.add(new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR,
				FileArgumentDefinition.INPUT_DIR_DESC, true, true, true, false));

		return args.toArray(new ArgumentDefinition[args.size()]);
	}

	/**
	 * Starts parsing.
	 */
	public void start() throws Exception {

		parseKGML(graph);

	}

	/**
	 * Parses kgml.tar.gz file.
	 * 
	 * @param aog
	 *            AbstractONDEXGraph
	 */
	private void parseKGML(ONDEXGraph aog)
			throws InvalidPluginArgumentException {

		// get file from arguments
		File dir = new File(
				(String) args.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

		// for parsing of KGML xml file
		KgmlParser kgml = new KgmlParser(this);
		PathwayParser pathways = new PathwayParser(aog, this);
		kgml.registerComponentParser(pathways);
		EntryParser entries = new EntryParser(aog, this, pathways);
		kgml.registerComponentParser(entries);
		ReactionParser reactions = new ReactionParser(aog, this,
				entries.getId2Concepts());
		kgml.registerComponentParser(reactions);

		try {

			// parse all .xml files in given directory
			for (File file : dir.listFiles()) {
				if (file.getName().endsWith(".xml")) {

					// open file stream
					FileInputStream input = new FileInputStream(file);
					kgml.parse(input);
					input.close();

					System.out.println("Types not found in "
							+ file.getAbsolutePath() + ": "
							+ EntryParser.notfound);
				}
			}
		} catch (FileNotFoundException fnfe) {
			fireEventOccurred(new DataFileMissingEvent(fnfe.getMessage(),
					"[Parser - parseKGML]"));
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					"[Parser - parseKGML]"));
		}
	}

}
