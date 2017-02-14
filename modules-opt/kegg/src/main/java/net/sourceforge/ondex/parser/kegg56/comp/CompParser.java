/*
 * Created on 27-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.comp;

import net.sourceforge.ondex.parser.kegg56.MetaData;
import net.sourceforge.ondex.parser.kegg56.sink.Concept;
import net.sourceforge.ondex.parser.kegg56.sink.ConceptAcc;
import net.sourceforge.ondex.parser.kegg56.sink.ConceptName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author taubertj
 */
public class CompParser {

	private static final String DBLINKS = "DBLINKS";

	private InputStream compoundFile;
	private InputStream glycanFile;
	private InputStream drugFile;
	private static final String ENTRY = "ENTRY";

	/**
	 * @param compoundFile
	 * @param glycanFile
	 * @param drugFile
	 */
	public CompParser(InputStream compoundFile, InputStream glycanFile,
			InputStream drugFile) {
		this.compoundFile = compoundFile;
		this.glycanFile = glycanFile;
		this.drugFile = drugFile;
	}

	/**
	 * parses all compounds and glycans from the constructor specified file
	 * 
	 * @return index of concepts parsed
	 */
	public Map<String, Concept> parse() throws IOException {
		HashMap<String, Concept> concepts = new HashMap<String, Concept>(5000);
		parseCompound(concepts);
		parseGlycan(concepts);
		parseDrug(concepts);
		return concepts;
	}

	/**
	 * Adds ChemicalStructure Attribute to compounds in KEGG.
	 * 
	 * @param concepts
	 * @param filename
	 * @param molFile
	 */
	public void parseMol(Map<String, Concept> concepts, String filename,
			InputStream molFile) throws IOException {

		// get compound name from file name
		String name = filename.substring(filename.indexOf("/") + 1, filename
				.indexOf("."));
		name = "CPD:" + name;

		// lookup concept prototype
		Concept c = concepts.get(name);
		if (c != null) {

			// get content of MOL file
			StringBuffer buf = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					molFile));
			while (reader.ready()) {
				String line = reader.readLine();
				buf.append(line);
				buf.append("\n");
			}
			reader.close();
			molFile.close();

			c.setMol(buf.toString());
		} else {
			System.out.println("Missing compound for ID: " + name);
		}
	}

	/**
	 * @param concepts
	 *            index of concepts parsed
	 */
	private void parseCompound(Map<String, Concept> concepts)
			throws IOException {
		final Pattern spaceSplit = Pattern.compile(" ");
		final Pattern semiColonSplit = Pattern.compile(";");
		final Pattern colonSplit = Pattern.compile(":");

		Concept concept = null;
		String conceptNames = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				compoundFile));
		boolean inFormula = false;
		boolean inDblinks = false;
		boolean inName = false;
		while (reader.ready()) {
			String line = reader.readLine();
			if (line.length() > 12) {
				if (!(line.substring(0, 11).trim().length() == 0)) {
					inFormula = false;
					inDblinks = false;
					inName = false;
					if (conceptNames != null) {
						int i = 0;
						String[] result = semiColonSplit.split(conceptNames);
						for (String name : result) {
							name = name.trim();
							if (name.length() > 0) {
								ConceptName conceptName = new ConceptName(name,
										i == 0);
								concept.getConceptNames().add(conceptName);
								i++;
							}
						}
						conceptNames = null;
					}
				}

				if (line.indexOf(ENTRY) > -1) {
					String accession = "CPD:"
							+ line.substring(ENTRY.length()).trim().split(
									"[ |    ]+")[0].trim().toUpperCase();
					concept = new Concept(accession, MetaData.CV_KEGG,
							MetaData.CC_COMPOUND);
					concepts.put(concept.getId().toUpperCase(), concept);
					ConceptAcc conceptAcc = new ConceptAcc(accession
							.substring(4), MetaData.CV_KEGG);
					concept.getConceptAccs().add(conceptAcc);
				} else if (inFormula || line.indexOf("FORMULA") > -1) {
					inFormula = true;
					String formular = line.substring("FORMULA".length(),
							line.length()).trim();
					ConceptName conceptName = new ConceptName(formular, false);
					concept.getConceptNames().add(conceptName);

					if (concept.getDescription() == null) {
						concept.setDescription(formular);
					} else {
						concept.setDescription(concept.getDescription() + " "
								+ formular);
					}
				} else if (inName || line.indexOf("NAME") > -1) {
					inName = true;
					line = line.substring(12, line.length());
					if (conceptNames == null)
						conceptNames = line;
					else
						conceptNames = conceptNames + line;
				} else if (inDblinks || line.indexOf("DBLINKS") > -1) {
					inDblinks = true;
					line = line.substring(12, line.length());

					String[] result = colonSplit.split(line);
					if (result.length == 2) {
						String db = result[0];
						String acc = result[1].trim();
						String[] accs = spaceSplit.split(acc);
						for (String accession : accs) {
							ConceptAcc conceptAcc = new ConceptAcc(accession,
									db);
							concept.getConceptAccs().add(conceptAcc);
						}
					}
				}
			}
		}
		reader.close();
	}

	/**
	 * @param concepts
	 *            index of concepts parsed
	 */
	private void parseGlycan(Map<String, Concept> concepts) throws IOException {
		final Pattern spaceSplit = Pattern.compile(" ");
		final Pattern semiColonSplit = Pattern.compile(";");
		final Pattern colonSplit = Pattern.compile(":");

		Concept concept = null;
		String conceptNames = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				glycanFile));
		boolean inComposition = false;
		boolean inDblinks = false;
		boolean inName = false;
		while (reader.ready()) {
			String line = reader.readLine();
			if (line.length() > 12) {

				if (!line.substring(0, 11).trim().equals("")) {
					inComposition = false;
					inDblinks = false;
					inName = false;
					if (conceptNames != null) {
						int i = 0;
						String[] result = semiColonSplit.split(conceptNames);
						for (String name : result) {
							name = name.trim();
							if (name.length() > 0) {
								ConceptName conceptName = new ConceptName(name,
										i == 0);
								concept.getConceptNames().add(conceptName);
								i++;
							}
						}
						conceptNames = null;
					}
				}

				if (line.indexOf(ENTRY) > -1) {
					String accession = "GL:"
							+ line.substring(ENTRY.length()).trim().split(
									"[ |    ]+")[0].trim().toUpperCase();
					concept = new Concept(accession, MetaData.CV_KEGG,
							MetaData.CC_COMPOUND);
					ConceptAcc conceptAcc = new ConceptAcc(accession
							.substring(3), MetaData.CV_KEGG);
					concept.getConceptAccs().add(conceptAcc);
					concepts.put(accession, concept);
				} else if (inComposition || line.indexOf("COMPOSITION") > -1) {
					inComposition = true;
					if (concept.getDescription() == null) {
						concept.setDescription(line
								.substring(12, line.length()));
					} else {
						concept.setDescription(concept.getDescription()
								+ line.substring(12, line.length()));
					}
				} else if (inName || line.indexOf("NAME") > -1) {
					inName = true;
					line = line.substring(12, line.length());
					if (conceptNames == null)
						conceptNames = line;
					else
						conceptNames = conceptNames + line;
				} else if (inDblinks || line.indexOf("DBLINKS") > -1) {
					inDblinks = true;

					int indexof = line.indexOf(DBLINKS);
					if (indexof > -1) {
						line = line.substring(indexof + DBLINKS.length())
								.trim();
					}
					String[] result = colonSplit.split(line);
					if (result.length == 2) {
						String db = result[0];
						String acc = result[1].trim();
						String[] accs = spaceSplit.split(acc);
						for (String access : accs) {
							ConceptAcc conceptAcc = new ConceptAcc(access, db);
							concept.getConceptAccs().add(conceptAcc);
						}
					}
				}
			}
		}
		reader.close();
	}

	/**
	 * @param concepts
	 *            index of concepts parsed
	 */
	private void parseDrug(Map<String, Concept> concepts) throws IOException {
		final Pattern spaceSplit = Pattern.compile(" ");
		final Pattern semiColonSplit = Pattern.compile(";");
		final Pattern colonSplit = Pattern.compile(":");

		Concept concept = null;
		String conceptNames = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				drugFile));
		boolean inFormula = false;
		boolean inDblinks = false;
		boolean inName = false;
		while (reader.ready()) {
			String line = reader.readLine();
			if (line.length() > 12) {
				if (!(line.substring(0, 11).trim().length() == 0)) {
					inFormula = false;
					inDblinks = false;
					inName = false;
					if (conceptNames != null) {
						int i = 0;
						String[] result = semiColonSplit.split(conceptNames);
						for (String name : result) {
							name = name.trim();
							if (name.length() > 0) {
								ConceptName conceptName = new ConceptName(name,
										i == 0);
								concept.getConceptNames().add(conceptName);
								i++;
							}
						}
						conceptNames = null;
					}
				}

				if (line.indexOf(ENTRY) > -1) {
					String accession = "DR:"
							+ line.substring(ENTRY.length()).trim().split(
									"[ |    ]+")[0].trim().toUpperCase();
					concept = new Concept(accession, MetaData.CV_KEGG,
							MetaData.CC_DRUG);
					concepts.put(concept.getId().toUpperCase(), concept);
					ConceptAcc conceptAcc = new ConceptAcc(accession
							.substring(4), MetaData.CV_KEGG);
					concept.getConceptAccs().add(conceptAcc);
				} else if (inFormula || line.indexOf("FORMULA") > -1) {
					inFormula = true;
					String formular = line.substring("FORMULA".length(),
							line.length()).trim();
					ConceptName conceptName = new ConceptName(formular, false);
					concept.getConceptNames().add(conceptName);

					if (concept.getDescription() == null) {
						concept.setDescription(formular);
					} else {
						concept.setDescription(concept.getDescription() + " "
								+ formular);
					}
				} else if (inName || line.indexOf("NAME") > -1) {
					inName = true;
					line = line.substring(12, line.length());
					if (conceptNames == null)
						conceptNames = line;
					else
						conceptNames = conceptNames + line;
				} else if (inDblinks || line.indexOf("DBLINKS") > -1) {
					inDblinks = true;
					line = line.substring(12, line.length());

					String[] result = colonSplit.split(line);
					if (result.length == 2) {
						String db = result[0];
						String acc = result[1].trim();
						String[] accs = spaceSplit.split(acc);
						for (String accession : accs) {
							ConceptAcc conceptAcc = new ConceptAcc(accession,
									db);
							concept.getConceptAccs().add(conceptAcc);
						}
					}
				}
			}
		}
		reader.close();
	}

}
