package net.sourceforge.ondex.ovtk2.reusable_functions;

import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;

/**
 * 
 * @author hindlem
 * 
 */
public class Transform {

	/**
	 * Modifies concept accessions by string replacement of a group based on a
	 * regex
	 * 
	 * e.g. modifyConceptAcessionsOnRegex("^\\d+(\\.\\d+){3}([^\\d]+)$", "EC",
	 * null, "EC", "", 2); //cleans up trailing junk on EC 4 digit accessions
	 * e.g.
	 * modifyConceptAcessionsOnRegex("^\\d+(\\.\\d+){2}(\\.-){1}([^\\d]+)$",
	 * "EC", null, "EC", "", 3); //cleans up trailing junk on EC 3 digit
	 * accessions
	 * 
	 * @author hindlem
	 * 
	 * @param viewer
	 *            the viewer containing the graph to work on
	 * @param regex
	 *            the regex with as many groups as groupToReplace (group 0 is
	 *            everything)
	 * @param ccName
	 *            the name of the concept class of concepts to search in (can be
	 *            null)
	 * @param concept_cvName
	 *            the name of the cv of concepts to search in (can be null)
	 * @param accession_cvName
	 *            the name of the cv of accessions to seach in (can not be null)
	 * @param replacementText
	 *            teh text to replace the matching group
	 * @param groupToReplace
	 *            the matching group to replace text (group 0 is everything)
	 * @return countReplacements number of concepts where replacements where
	 *         made
	 */
	public static final int modifyConceptAcessionsOnRegex(OVTK2PropertiesAggregator viewer, String regex, String ccName, String concept_cvName, String accession_cvName, String replacementText, int groupToReplace) {

		ONDEXGraphMetaData md = viewer.getONDEXJUNGGraph().getMetaData();

		ONDEXGraph graph = viewer.getONDEXJUNGGraph();

		ConceptClass cc = null;
		DataSource concept_dataSource = null;
		DataSource accession_dataSource = md.getDataSource(accession_cvName);

		if (accession_dataSource == null) {
			System.err.println("Accession for DataSource not known :" + accession_cvName);
			return 0;
		}

		if (ccName != null && ccName.length() > 0)
			cc = md.getConceptClass(ccName);

		if (concept_cvName != null && concept_cvName.length() > 0)
			concept_dataSource = md.getDataSource(concept_cvName);

		Pattern pattern = Pattern.compile(regex);

		Set<ONDEXConcept> concepts = graph.getConcepts();

		if (cc != null) {
			concepts.retainAll(graph.getConceptsOfConceptClass(cc));
			System.out.println("restrict on cc: " + cc.getId() + " " + concepts.size());
		}

		if (concept_dataSource != null) {
			concepts.retainAll(graph.getConceptsOfDataSource(concept_dataSource));
			System.out.println("restrict on cv: " + concept_dataSource.getId() + " " + concepts.size());
		}

		int countReplacements = 0;

		System.out.println(" replace " + concepts.size());

		for (ONDEXConcept concept : concepts) {
			HashMap<String, String> replacements = new HashMap<String, String>();

			for (ConceptAccession accession : concept.getConceptAccessions()) {
				if (!accession.getElementOf().getId().equals(accession_dataSource.getId())) {
					continue;
				}
				Matcher match = pattern.matcher(accession.getAccession());
				System.out.println(match.matches() + " " + accession.getAccession() + " " + match.groupCount());
				if (match.matches() && match.groupCount() > 0) {
					System.out.println("matches!");
					StringBuilder newAccession = new StringBuilder(accession.getAccession());

					int start = match.start(groupToReplace);
					int end = match.end(groupToReplace);
					newAccession.replace(start, end, replacementText);
					replacements.put(accession.getAccession(), newAccession.toString());
				}
			}

			if (replacements.size() > 0) {
				countReplacements++;
			}

			for (String oldAccession : replacements.keySet()) {
				boolean ambiguous = concept.getConceptAccession(oldAccession, accession_dataSource).isAmbiguous();
				concept.deleteConceptAccession(oldAccession, accession_dataSource);
				concept.createConceptAccession(replacements.get(oldAccession), accession_dataSource, ambiguous);
			}
		}
		return countReplacements;
	}

}
