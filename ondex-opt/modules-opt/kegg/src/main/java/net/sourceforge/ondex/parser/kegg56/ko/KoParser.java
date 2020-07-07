/*
 * Created on 25-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.ko;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.ondex.parser.kegg56.MetaData;
import net.sourceforge.ondex.parser.kegg56.sink.Concept;
import net.sourceforge.ondex.parser.kegg56.sink.ConceptAcc;
import net.sourceforge.ondex.parser.kegg56.sink.ConceptName;

/**
 * @author taubertj
 */
public class KoParser {

	private Map<Concept, Set<String>> koConceptToGenes = new HashMap<Concept, Set<String>>();
	private Map<String, Concept> koNamesToKoConcept = new HashMap<String, Concept>();
	private Map<String, Concept> koAccessionToKoConcept = new HashMap<String, Concept>();

	/**
	 * @param pathToKO
	 */
	public KoParser(InputStream pathToKO) throws IOException {
		parse(pathToKO);
	}

	/**
	 * Parses to ko.txt file and constructs a KO concept for each entry
	 * 
	 * @param koData
	 *            stream containing the ko (kegg ortholog) file data
	 * @return Mapping KO concept to containing genes
	 */
	private void parse(InputStream koData) throws IOException {
		koConceptToGenes.clear();

		final Pattern spaceSplit = Pattern.compile(" ");
		final Pattern commaSplit = Pattern.compile(",");
		final Pattern colonSplit = Pattern.compile(":");
		final Pattern ecPattern = Pattern
				.compile("\\d+\\.[\\d-]+\\.[\\d-]+\\.[\\d-]+");

		BufferedReader reader = new BufferedReader(
				new InputStreamReader(koData));

		// some state variables, which show the section, we are in
		boolean inDefinition = false;
		boolean inDblinks = false;
		boolean inGenes = false;

		// global variables for concept and org string
		// for multiple line parsing
		Concept concept = null;
		String org = null;
		String db = null;
		Set<String> genes = null;
		ArrayList<String> ambiguousName = new ArrayList<String>();
		while (reader.ready()) {
			String line = reader.readLine();
			if (line.length() > 12) {

				// when line does not begin with blanks reset states
				if (line.substring(0, 11).trim().length() != 0) {
					inDefinition = false;
					inDblinks = false;
					inGenes = false;
				}

				// this starts a new entry resulting in a new concept
				if (line.startsWith("ENTRY")) {
					String koAccession = line.substring(12, 18).toUpperCase();
					concept = new Concept("KO:" + koAccession,
							MetaData.CV_KEGG, MetaData.CC_KEGG_ORTHOLOG);
					koAccessionToKoConcept.put(koAccession.toUpperCase(),
							concept);
					genes = new HashSet<String>();
					koConceptToGenes.put(concept, genes);
				}

				// this parses a multiline definition
				else if (inDefinition || line.startsWith("DEFINITION")) {
					inDefinition = true;
					if (concept.getDescription() == null) {
						concept.setDescription(line
								.substring(12, line.length()));
					} else {
						// append to existing description
						concept.setDescription(concept.getDescription()
								+ line.substring(11, line.length()));
					}
				}
				// parses the name line into several concept names
				else if (line.startsWith("NAME")) {
					line = line.substring(12, line.length()).toUpperCase();
					String[] result = commaSplit.split(line);
					for (String name : result) {
						name = name.trim();
						ConceptName conceptName = new ConceptName(name, false);
						if (!koNamesToKoConcept.containsKey(name.toUpperCase())
								&& !ambiguousName.contains(name.toUpperCase())) {
							koNamesToKoConcept.put(name.toUpperCase(), concept);
						} else {
							ambiguousName.add(name.toUpperCase());
							koNamesToKoConcept.remove(name.toUpperCase());
							// this is an ambiguous term
						}

						if (concept.getConceptAccs() == null
								|| concept.getConceptAccs().size() == 0) {
							// the first name is preferred
							conceptName.setPreferred(true);
						}
						concept.getConceptNames().add(conceptName);
					}
				}

				// this parses multiline DB links, one line per link
				else if (inDblinks || line.startsWith("DBLINKS")) {
					inDefinition = false;
					inDblinks = true;
					line = line.substring(12, line.length());
					String[] result = colonSplit.split(line);
					String acc = null;
					// first comes DB name, then list of accessions
					if (result.length == 2) {
						db = result[0].trim();
						acc = result[1].trim().toUpperCase();
					} else {
						// here multiline DBLINKS, keep previous DB
						acc = line.trim();
					}
					// split multiple accessions
					String[] accs = spaceSplit.split(acc);
					for (String access : accs) {
						ConceptAcc conceptAcc = new ConceptAcc(access, db);
						concept.getConceptAccs().add(conceptAcc);
					}
				}

				// now the multiline gene part
				else if (inGenes || line.startsWith("GENES")) {
					inDblinks = false;
					inGenes = true;
					line = line.substring(12, line.length());
					line = removeBrackets(line);
					String[] results = colonSplit.split(line);
					// the organism abbreviation comes first
					if (results.length == 2) {
						org = results[0];
						line = results[1];
					}
					// split into several gene names
					results = spaceSplit.split(line.trim());
					for (String gene : results) {
						// add gene to KO concept
						String genename = org.toLowerCase() + ":" + gene;
						genes.add(genename.toUpperCase());
					}
				}
			}

			// this indicates one entry finished
			else if (line.startsWith("///")) {
				String definition = concept.getDescription();
				if (definition != null && definition.indexOf("[EC:") > 1) {
					// parse EC number from definition
					String ec = definition.substring(
							definition.indexOf("[EC:") + 4, definition
									.lastIndexOf("]"));
					// possible multiple ECs
					String[] split = spaceSplit.split(ec);
					for (String acc : split) {
						ConceptAcc conceptAcc = new ConceptAcc(acc,
								MetaData.CV_EC);
						concept.getConceptAccs().add(conceptAcc);
						//System.out.println(conceptAcc);
					}
				}
				for (ConceptName cn : concept.getConceptNames()) {
					String name = cn.getName();
					if (name.startsWith("E")) {
						name = name.substring(1);
						Matcher m = ecPattern.matcher(name);
						if (m.matches()) {
							ConceptAcc conceptAcc = new ConceptAcc(name,
									MetaData.CV_EC);
							concept.getConceptAccs().add(conceptAcc);
							//System.out.println("Matching: " + conceptAcc);
						}
					}
				}
			}
		}
		reader.close();
	}

	/**
	 * This remove everthing within brackets from a string
	 * 
	 * @param s
	 *            - given string
	 * @return resulting string
	 */
	private static String removeBrackets(String s) {
		StringBuilder buf = new StringBuilder(s.trim());
		while (buf.indexOf("(") > -1) {
			buf.delete(buf.indexOf("("), buf.indexOf(")") + 1);
			if ((buf.indexOf(")") < buf.indexOf("("))
					|| (buf.indexOf("(") == -1 && buf.indexOf(")") > -1))
				buf.delete(buf.indexOf(")"), buf.indexOf(")") + 1);
		}
		return buf.toString();
	}

	public Map<Concept, Set<String>> getKoConceptToGenes() {
		return koConceptToGenes;
	}

	public void finalise() {
		koConceptToGenes.clear();
	}

	public Map<String, Concept> getKoNamesToKoConcept() {
		return koNamesToKoConcept;
	}

	public Map<String, Concept> getKoAccessionToKoConcept() {
		return koAccessionToKoConcept;
	}

}
