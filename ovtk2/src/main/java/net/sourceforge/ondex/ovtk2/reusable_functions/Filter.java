package net.sourceforge.ondex.ovtk2.reusable_functions;

import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.tools.functions.StandardFunctions;

/**
 * 
 * @author hindlem
 * 
 */
public class Filter {

	/**
	 * A useful scripting method to tell you if a regex is valid
	 * 
	 * @author hindlem
	 * @param regex
	 *            the regular expression to test
	 * @return if its a valid java expression or not
	 */
	public static final boolean isValidRegex(String regex) {
		try {
			Pattern.compile(regex);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Searches in a graph for concepts with accessions matching a regex
	 * 
	 * e.g. hideConceptsOnAcessionRegex(".+\\\.-", "EC", "EC", "EC"); //hides
	 * all EC terms with .- in the accession other useful regex --> all 4 digit
	 * EC "^\\d+\\.\\d+\\.\\d+\\.\\d+$" --> all 3 digit EC
	 * "^\\d+\\.\\d+\\.\\d+\\.-$" --> all 2 digit EC "^\\d+\\.\\d+\\.-\\.-$" -->
	 * all 1 digit EC "^\\d+\\.-\\.-\\.-$"
	 * 
	 * @author hindlem
	 * @param viewer
	 *            the graph to search concepts in
	 * @param regex
	 *            a valid Java regex (a pattern.matcher($accession).matches() is
	 *            done)
	 * @param ccName
	 *            the name of the concept class of concepts to search in (can be
	 *            null)
	 * @param concept_cvName
	 *            the name of the cv of concepts to search in (can be null)
	 * @param accession_cvName
	 *            the name of the cv of accessions to seach in (can be null)
	 * @return the number of concepts hidden
	 */
	public static final int hideConceptsOnAcessionRegex(OVTK2PropertiesAggregator viewer, String regex, String ccName, String concept_cvName, String accession_cvName) {

		ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
		ONDEXGraphMetaData md = graph.getMetaData();

		ConceptClass cc = null;
		DataSource concept_dataSource = null;
		DataSource accession_dataSource = null;

		if (ccName != null && ccName.length() > 0)
			cc = md.getConceptClass(ccName);
		if (concept_cvName != null && concept_cvName.length() > 0)
			concept_dataSource = md.getDataSource(concept_cvName);
		if (accession_cvName != null && accession_cvName.length() > 0)
			accession_dataSource = md.getDataSource(accession_cvName);

		Set<ONDEXConcept> concepts = StandardFunctions.filterConceptsOnAcessionRegex(regex, viewer.getONDEXJUNGGraph(), false, cc, concept_dataSource, accession_dataSource);
		int conceptcount = concepts.size();
		graph.setVisibility(concepts, false);
		return conceptcount;
	}

}
