package net.sourceforge.ondex.parser.ec.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sourceforge.ondex.config.ValidatorRegistry;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.ec.MetaData;
import net.sourceforge.ondex.parser.ec.Parser;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;

import org.apache.log4j.Level;

/**
 * Class extracts content from EC database.
 * 
 * @author winnenbr, taubertj
 * 
 */
public class Extractor {

	// whether or not to parser deleted entries
	private boolean getDeleted = false;

	// parsed entries
	private List<Entry> entries = new ArrayList<Entry>();

	private AbstractONDEXValidator taxValidator;

	/**
	 * Constructor to initialize parsing.
	 * 
	 * @param graph
	 *            ONDEXGraph
	 * @param path
	 *            String
	 * @param getDeleted
	 *            boolean
	 */
	public Extractor(ONDEXGraph graph, String infilesdir, Boolean getDeleted) {

		this.getDeleted = getDeleted;

		taxValidator = ValidatorRegistry.validators.get("taxonomy");

		if (!infilesdir.endsWith(File.separator))
			infilesdir = infilesdir + File.separator;

		GeneralOutputEvent goe = new GeneralOutputEvent(
				"Reading EC import files...",
				ONDEXParser.getCurrentMethodName());
		goe.setLog4jLevel(Level.INFO);
		Parser.propagateEventOccurred(goe);

		/**
		 * Read in the EC files and create the entries
		 */

		/*
		 * [0]: EC-number, String (without blanks, format x.x.x.x, "-" are used
		 * for creating hierarchies) [1]: name, String [2]: is_a relation
		 * (EC-number), String [3]: is_a relation (name), String [4]: Synonyms,
		 * Vector [5]: Description, String
		 */
		String filename_ec_classes = infilesdir + "enzclass.txt";
		Map<String, String> nr2name = extractClasses(filename_ec_classes);

		String filename_ec_enzymes = infilesdir + "enzyme.dat";
		extractDetails(filename_ec_enzymes, nr2name);
	}

	/**
	 * Returns cache of entries.
	 * 
	 * @return ArrayList<Entry>
	 */
	public List<Entry> getEntries() {
		return entries;
	}

	/**
	 * Extracts ec nr to name mapping from enzclass.txt.
	 * 
	 * @param filename_ec_classes
	 *            String
	 * @return Map<String, String>
	 */
	private Map<String, String> extractClasses(String filename_ec_classes) {

		Map<String, String> nr2name = new TreeMap<String, String>();

		GeneralOutputEvent goe = new GeneralOutputEvent(
				"Extract enzyme classes...", ONDEXParser.getCurrentMethodName());
		goe.setLog4jLevel(Level.INFO);
		Parser.propagateEventOccurred(goe);

		goe = new GeneralOutputEvent("Open file " + filename_ec_classes,
				ONDEXParser.getCurrentMethodName());
		goe.setLog4jLevel(Level.INFO);
		Parser.propagateEventOccurred(goe);

		try {
			BufferedReader in_ec_classes = new BufferedReader(new FileReader(
					filename_ec_classes));

			// first clean up the lines of the class file and create a vector of
			// complete lines
			boolean read = false;

			List<String> lines = new ArrayList<String>();
			String inputline = in_ec_classes.readLine();
			while (in_ec_classes.ready()) {

				if (inputline.startsWith("1.") && !read)
					read = true;
				else if (inputline.startsWith("------") && read)
					read = false;

				if (read && (inputline.length() > 0)) {

					if (inputline.indexOf(".") == inputline.lastIndexOf(".")) {
						String lastline = lines.get(lines.size() - 1);
						lastline = lastline + " " + inputline.trim();
						lines.set(lines.size() - 1, lastline);
					} else
						lines.add(inputline);
				}

				inputline = in_ec_classes.readLine();
			}
			in_ec_classes.close();

			goe = new GeneralOutputEvent("Successfully read file.",
					ONDEXParser.getCurrentMethodName());
			goe.setLog4jLevel(Level.INFO);
			Parser.propagateEventOccurred(goe);

			// go through the created vector and extract the id, the name and
			// the is_a relation
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);

				int split = 9; // taken from the file; in this case this was
				// stable for all the lines!
				String ec_nr = line.substring(0, split);
				String ec_name = line.substring(split, line.length());

				StringTokenizer st = new StringTokenizer(ec_nr);
				String temp = "";
				while (st.hasMoreTokens())
					temp = temp + st.nextToken();

				ec_nr = temp;
				ec_name = ec_name.trim();

				// remove the last "."
				if (ec_name.endsWith("."))
					ec_name = ec_name.substring(0, ec_name.length() - 1);

				// remove the plural "s" from main category names
				if (getNrHyphens(ec_nr) > 0)
					if (ec_name.endsWith("ases"))
						ec_name = ec_name.substring(0, ec_name.length() - 1);

				nr2name.put(ec_nr, ec_name);

				String is_a_nr = getHigherTerm(ec_nr);
				Entry entry = new Entry(Entry.Type.EC, ec_nr);
				entry.setName(ec_name);
				entry.addAccession(MetaData.DS_EC, new String[] { ec_nr });

				if (!is_a_nr.equals("")) {
					String is_a_name = nr2name.get(is_a_nr);
					if (is_a_name == null)
						is_a_name = "";

					Relation rel = new Relation(ec_nr, is_a_nr,
							MetaData.RT_IS_A);
					entry.addRelation(rel);
				}

				entries.add(entry);
			}
			goe = new GeneralOutputEvent("Successfully extracted "
					+ entries.size() + " enzyme classes.",
					ONDEXParser.getCurrentMethodName());
			goe.setLog4jLevel(Level.INFO);
			Parser.propagateEventOccurred(goe);

		} catch (FileNotFoundException fnfe) {
			Parser.propagateEventOccurred(new DataFileMissingEvent(fnfe
					.getMessage(), ONDEXParser.getCurrentMethodName()));
		} catch (IOException ioe) {
			Parser.propagateEventOccurred(new DataFileErrorEvent(ioe
					.getMessage(), ONDEXParser.getCurrentMethodName()));
		}
		return nr2name;
	}

	/**
	 * Extracts details for ec from enzyme.dat.
	 * 
	 * @param filename_ec_enzymes
	 *            String
	 * @param nr2name
	 *            Map<String, String>
	 */
	private void extractDetails(String filename_ec_enzymes,
			Map<String, String> nr2name) {
		GeneralOutputEvent goe = new GeneralOutputEvent(
				"Extract enzyme details...", ONDEXParser.getCurrentMethodName());
		goe.setLog4jLevel(Level.INFO);
		Parser.propagateEventOccurred(goe);

		goe = new GeneralOutputEvent("Open file " + filename_ec_enzymes,
				ONDEXParser.getCurrentMethodName());
		goe.setLog4jLevel(Level.INFO);
		Parser.propagateEventOccurred(goe);

		try {
			BufferedReader in_ec_enzymes = new BufferedReader(new FileReader(
					filename_ec_enzymes));

			boolean deletedEntry = false;

			String ec_nr = "";
			String ec_name = "";
			List<String> synonyms = new ArrayList<String>();
			String description = "";
			String description_ca = "";
			String description_cf = "";
			String description_cc = "";

			// fast forward to the first "//"
			String inputline = in_ec_enzymes.readLine();
			while (in_ec_enzymes.ready() && (inputline.compareTo("//") != 0))
				inputline = in_ec_enzymes.readLine();

			boolean synonym_end = true;
			boolean ec_name_end = true;
			Set<String> ready = new TreeSet<String>();
			while (in_ec_enzymes.ready()) {

				if ((inputline.compareTo("//") == 0) && (ec_nr.length() > 0)) {

					if (!deletedEntry || this.getDeleted) {
						createECEntry(ec_nr, ec_name, synonyms, description,
								description_ca, description_cf, description_cc,
								nr2name, ready);
					}

					deletedEntry = false;
					ec_nr = "";
					ec_name = "";
					synonyms.clear();
					description = "";
					description_ca = "";
					description_cf = "";
					description_cc = "";
				}

				else if (inputline.startsWith("ID")) {
					StringTokenizer st = new StringTokenizer(inputline);
					st.nextToken(); // "ID"
					ec_nr = st.nextToken();
				}

				else if (inputline.startsWith("DE")) {
					StringTokenizer st = new StringTokenizer(inputline);
					st.nextToken(); // "DE"

					if (ec_name_end) {
						ec_name = st.nextToken();
					} else {
						if (ec_name.endsWith("-")) {
							ec_name = ec_name + st.nextToken();
						} else {
							ec_name = ec_name + " " + st.nextToken();
						}

						ec_name_end = true;
					}

					while (st.hasMoreTokens()) {
						ec_name = ec_name + " " + st.nextToken();
					}

					if (ec_name.endsWith(".")) {
						ec_name = ec_name.substring(0, ec_name.length() - 1);
					} else {
						ec_name_end = false;
					}

					if (ec_name.trim().toUpperCase().equals("DELETED ENTRY")) {
						deletedEntry = true;
					}
				}

				else if (inputline.startsWith("AN")) {
					StringTokenizer st = new StringTokenizer(inputline);
					st.nextToken(); // "AN"
					String synonym = "";

					if (synonym_end)
						synonym = st.nextToken();
					else {
						int lastpos = synonyms.size() - 1;
						String lastsyn = synonyms.remove(lastpos);
						if (lastsyn.endsWith("-"))
							synonym = lastsyn + st.nextToken();
						else
							synonym = lastsyn + " " + st.nextToken();

						synonym_end = true;
					}

					while (st.hasMoreTokens())
						synonym = synonym + " " + st.nextToken();

					if (synonym.endsWith("."))
						synonym = synonym.substring(0, synonym.length() - 1);
					else
						synonym_end = false;

					synonyms.add(synonym);
				}

				else if (inputline.startsWith("CA")) {
					StringTokenizer st = new StringTokenizer(inputline);
					st.nextToken(); // "CA"
					while (st.hasMoreTokens())
						description_ca = description_ca + " " + st.nextToken();
				}

				else if (inputline.startsWith("CF")) {
					StringTokenizer st = new StringTokenizer(inputline);
					st.nextToken(); // "CF"
					while (st.hasMoreTokens())
						description_cf = description_cf + " " + st.nextToken();
				}

				else if (inputline.startsWith("CC")) {
					StringTokenizer st = new StringTokenizer(inputline);
					st.nextToken(); // "CC"
					while (st.hasMoreTokens())
						description_cc = description_cc + " " + st.nextToken();
				}

				else if (inputline.startsWith("PR")) {
					if (!deletedEntry || this.getDeleted)
						createDomainReferences(ec_nr, inputline);
				}

				else if (inputline.startsWith("DR")) {
					if (!deletedEntry || this.getDeleted)
						createProteinReferences(ec_nr, inputline);
				}

				inputline = in_ec_enzymes.readLine();
			}

			// add the last entry
			if (!deletedEntry || this.getDeleted) {
				createECEntry(ec_nr, ec_name, synonyms, description,
						description_ca, description_cf, description_cc,
						nr2name, ready);
			}
			in_ec_enzymes.close();

			goe = new GeneralOutputEvent(
					"Successfully extracted details and file closed.",
					ONDEXParser.getCurrentMethodName());
			goe.setLog4jLevel(Level.INFO);
			Parser.propagateEventOccurred(goe);
		} catch (FileNotFoundException fnfe) {
			Parser.propagateEventOccurred(new DataFileMissingEvent(fnfe
					.getMessage(), ONDEXParser.getCurrentMethodName()));
		} catch (IOException ioe) {
			Parser.propagateEventOccurred(new DataFileErrorEvent(ioe
					.getMessage(), ONDEXParser.getCurrentMethodName()));
		}
	}

	private Map<String, Entry> domains = new HashMap<String, Entry>();

	private void createDomainReferences(String ec_nr, String inputline) {
		String[] line = inputline.substring(2).split(";");

		if (line.length == 2 && line[0].trim().toUpperCase().equals("PROSITE")
				&& line[1].trim().length() > 0) {
			String value = line[1].trim().toUpperCase();
			if (!domains.keySet().contains(value)) {
				Entry entry = new Entry(Entry.Type.DOMAIN, value);
				entry.addAccession(MetaData.DS_PROTSITE, new String[] { value });
				domains.put(value, entry);
				entries.add(entry);
			} else {
				Entry entry = domains.get(value);
				entry.addRelation(new Relation(value, ec_nr,
						MetaData.RT_CATALYSEING_CLASS));
			}
		}
	}

	private Map<String, Entry> proteins = new HashMap<String, Entry>();

	private void createProteinReferences(String ec_nr, String inputline) {
		String[] line = inputline.substring(2).split(";");

		for (String protein : line) {
			String[] names = protein.split(",");
			if (names.length == 2) {
				String uprotac = names[0].trim();
				String uprotid = names[1].trim();

				String speciesCode = uprotid.split("_")[1];

				String taxIdSt = (String) taxValidator
						.validate((String) speciesCode);

				if (!proteins.keySet().contains(uprotid)) {

					Entry entry = new Entry(Entry.Type.PROTEIN, uprotid);
					if (taxIdSt != null) {
						entry.setTaxid(taxIdSt);
					}

					proteins.put(uprotid, entry);
					entry.addAccession(MetaData.DS_UNIPROTKB,
							new String[] { uprotac });
					entry.setName(uprotid);
					entry.addRelation(new Relation(uprotid, ec_nr,
							MetaData.RT_CATALYSEING_CLASS));
					entries.add(entry);
				} else {
					Entry entry = proteins.get(uprotid);
					entry.addRelation(new Relation(uprotid, ec_nr,
							MetaData.RT_CATALYSEING_CLASS));
				}

			}
		}
	}

	/**
	 * Analyses a EC number given as String for hyphens (eg.
	 * getNrHyphens("1.23.12.-") = 1 )
	 * 
	 * @param s
	 *            EC number as String
	 * @return the number of hyphens in this EC number
	 */
	private int getNrHyphens(String s) {

		int nr = 0;

		StringTokenizer st = new StringTokenizer(s, ".");
		while (st.hasMoreTokens()) {

			String token = st.nextToken();
			if (token.compareTo("-") == 0)
				nr++;
		}

		return nr;
	}

	/**
	 * Analyzes EC number as String and returns the next higher term (eg.
	 * getHigherTerm("1.23.12.-") = "1.23.-.-" )
	 * 
	 * @param ec_nr
	 *            String
	 * @return the String of next higher term
	 */
	private String getHigherTerm(String ec_nr) {

		// returns
		String higher = "";

		int nrHyphens = getNrHyphens(ec_nr);
		int nrTupel = 4;

		// number of defined parts (1.23.-.- = 2)
		int position = nrTupel - nrHyphens;

		if (position > 1) {

			StringTokenizer st = new StringTokenizer(ec_nr, ".");
			String token = st.nextToken();
			higher = token;

			int currentTupel = 1;

			while (st.hasMoreTokens()) {

				token = st.nextToken();
				currentTupel++;

				/*
				 * if we are at the last section with digits, replace these
				 * digits with hyphen in order to get higher term
				 */
				if (currentTupel == position)
					token = "-";
				higher = higher + "." + token;
			}
		}

		return higher;
	}

	/**
	 * Stores the data for one single ec_nr into an Entry object an puts it into
	 * the ArrayList of entries. Also checks for parent terms.
	 * 
	 * @param ec_nr
	 *            String
	 * @param ec_name
	 *            String
	 * @param synonyms
	 *            List<String>
	 * @param description
	 *            String
	 * @param description_ca
	 *            String
	 * @param description_cf
	 *            String
	 * @param description_cc
	 *            String
	 * @param nr2name
	 *            Map<String, String>
	 * @param ready
	 *            Set<String>
	 */
	private void createECEntry(String ec_nr, String ec_name,
			List<String> synonyms, String description, String description_ca,
			String description_cf, String description_cc,
			Map<String, String> nr2name, Set<String> ready) {

		// get parent of current ec number
		String is_a_nr = getHigherTerm(ec_nr);
		// get the name for the parent of current ec number
		String is_a_name = nr2name.get(is_a_nr);

		/*
		 * If it is a transferred entry: Get the number to which it is
		 * transferred and get the parent term of this new number Attention: -
		 * The string of the transferred entry might contain an "EC" - There
		 * might be more than one new entry - In some case of more than one new
		 * entries they have also different places in the tree: Then take the
		 * first one
		 */
		if (ec_name.startsWith("Transferred entry:")) {

			String sub = "Transferred entry: ";
			String nr = ec_name.substring(sub.length(), ec_name.length());

			if (nr.indexOf(",") != -1) // number of new terms >= 2
				nr = nr.substring(0, nr.indexOf(","));
			else if ((nr.indexOf(",") == -1) && (nr.indexOf("and") != -1)) {
				// number of new terms = 2
				nr = nr.substring(0, nr.indexOf(" and"));
			}
			// no further "null" terms tested here!
			is_a_nr = getHigherTerm(nr);
			is_a_name = nr2name.get(is_a_nr);

			// if this new number is already reported as missing
			if ((is_a_name == null) && ready.contains(is_a_nr)) {
				description = (description + " MISSING TERM (in expasy flat files)")
						.trim();
			}
		}

		if ((is_a_name == null) && !ready.contains(is_a_nr)) {
			createParent(is_a_nr, nr2name, ready);
		} else if ((is_a_name == null) && ready.contains(is_a_nr)) {
			is_a_name = null;
			description = (description + " MISSING TERM (in expasy flat files)")
					.trim();
		}

		if (description_ca.length() > 0)
			description = description + " Reaction catalysed: "
					+ description_ca;

		if (description_cf.length() > 0)
			description = description + " Cofactor(s): " + description_cf;

		if (description_cc.length() > 0)
			description = description + " Comments: " + description_cc;

		description = description.trim();

		Entry entry = new Entry(Entry.Type.EC, ec_nr);
		entry.setName(ec_name);
		entry.setDescription(description);
		entry.addAccession(MetaData.DS_EC, new String[] { ec_nr });
		entry.getSynonyms().addAll(synonyms);

		Relation rel = new Relation(ec_nr, is_a_nr, MetaData.RT_IS_A);
		entry.addRelation(rel);
		entries.add(entry);
	}

	/**
	 * Recursive construction of missing parent ec numbers.
	 * 
	 * @param is_a_nr
	 *            String
	 * @param nr2name
	 *            Map<String, String>
	 * @param ready
	 *            Set<String
	 */
	private void createParent(String is_a_nr, Map<String, String> nr2name,
			Set<String> ready) {
		/*
		 * If no parent term is found, and if this case has not already been
		 * handled ("ready") then create an additional entry with the
		 * information that this term is missing such that terms referring to
		 * that as parent aren't "null" in "is_a".
		 */

		// create a seperate entry for the missing term
		ready.add(is_a_nr);

		Entry entry = new Entry(Entry.Type.EC, is_a_nr);
		entry.addAccession(MetaData.DS_EC, new String[] { is_a_nr });

		String is_a_is_a_nr = getHigherTerm(is_a_nr);
		Relation rel = new Relation(is_a_nr, is_a_is_a_nr, MetaData.RT_IS_A);
		entry.addRelation(rel);
		entry.setDescription("MISSING TERM (in expasy flat files)");
		entries.add(entry);

		if ((nr2name.get(is_a_is_a_nr) == null)
				&& !ready.contains(is_a_is_a_nr)) {
			createParent(is_a_is_a_nr, nr2name, ready);
		}
	}

}