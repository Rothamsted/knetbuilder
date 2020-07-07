package net.sourceforge.ondex.ovtk2.ui.contentsdisplay.plugins;

import java.util.HashSet;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin;

/**
 * Extracts the publication's abstract, title, authors, publication year,
 * journal, mesh terms, chemicals and doi number. For text mining based
 * relations the score and evidence senteces are shown.
 * 
 * @author Keywan
 */
public class PublicationPlugin extends AbstractContentDisplayPlugin {

	private final static String ATT_NAME_ABSTRACT_HEADER = "AbstractHeader";

	private final static String ATT_NAME_ABSTRACT = "Abstract";

	private final static String ATT_NAME_AUTHORS = "AUTHORS";

	private final static String ATT_NAME_YEAR = "YEAR";

	private final static String ATT_NAME_JOURNAL = "JOURNAL_REF";

	private final static String ATT_NAME_DOI = "DOI";

	private final static String ATT_NAME_MESH = "MeSH";

	private final static String ATT_NAME_CHEMICAL = "Chemical";

	// private final static String ATT_NAME_TMSCORE = "TM_SCORE";

	private final static String ATT_NAME_EVIDENCE = "EVIDENCE";

	private AttributeName header_an;

	private AttributeName abstract_an;

	private AttributeName authors_an;

	private AttributeName year_an;

	private AttributeName journal_an;

	private AttributeName doi_an;

	private AttributeName mesh_an;

	private AttributeName chem_an;

	// private AttributeName tmScore_an;

	private AttributeName evidence_an;

	/**
	 * Constructor.
	 */
	public PublicationPlugin(ONDEXGraph aog) {
		super(aog);

		header_an = aog.getMetaData().getAttributeName(ATT_NAME_ABSTRACT_HEADER);

		abstract_an = aog.getMetaData().getAttributeName(ATT_NAME_ABSTRACT);

		authors_an = aog.getMetaData().getAttributeName(ATT_NAME_AUTHORS);

		year_an = aog.getMetaData().getAttributeName(ATT_NAME_YEAR);

		journal_an = aog.getMetaData().getAttributeName(ATT_NAME_JOURNAL);

		doi_an = aog.getMetaData().getAttributeName(ATT_NAME_DOI);

		mesh_an = aog.getMetaData().getAttributeName(ATT_NAME_MESH);

		chem_an = aog.getMetaData().getAttributeName(ATT_NAME_CHEMICAL);

		evidence_an = aog.getMetaData().getAttributeName(ATT_NAME_EVIDENCE);

	}

	/**
	 * Extracts Information from a concept of type Publication
	 * 
	 * @see AbstractContentDisplayPlugin.compileContent()
	 */
	@Override
	public String compileContent(ONDEXEntity e) {

		StringBuffer b = new StringBuffer("");
		if (e instanceof ONDEXConcept) {

			ONDEXConcept c = (ONDEXConcept) e;

			if (c.getOfType().getId().equals("Publication")) {

				if (header_an != null) {
					Attribute header_attribute = c.getAttribute(header_an);
					if (header_attribute != null) {
						String header = header_attribute.getValue().toString();
						b.append("<h2>Publication</h2>");
						b.append("<h3>" + header + "</h3>");
					}
				}
				if (authors_an != null) {
					Attribute authors_attribute = c.getAttribute(authors_an);
					if (authors_attribute != null) {
						String authors = authors_attribute.getValue().toString();
						b.append("<i>" + authors + "</i><br>");
					}
				}
				if (year_an != null) {
					Attribute year_attribute = c.getAttribute(year_an);
					if (year_attribute != null) {
						String year = year_attribute.getValue().toString();
						b.append("<b>Year: </b>" + year + "<br>");
					}
				}
				if (journal_an != null) {
					Attribute journal_attribute = c.getAttribute(journal_an);
					if (journal_attribute != null) {
						String journal = journal_attribute.getValue().toString();
						b.append("<b>Published in: </b>" + journal + "<br>");
					}
				}
				if (abstract_an != null) {
					Attribute abstract_attribute = c.getAttribute(abstract_an);
					if (abstract_attribute != null) {
						String abstr = abstract_attribute.getValue().toString();
						b.append(abstr + "<br><br>");
					}
				}
				if (mesh_an != null) {
					Attribute mesh_attribute = c.getAttribute(mesh_an);
					if (mesh_attribute != null) {
						String mesh = mesh_attribute.getValue().toString();
						b.append("<b>MeSH: </b>" + mesh + "<br>");
					}
				}
				if (chem_an != null) {
					Attribute chem_attribute = c.getAttribute(chem_an);
					if (chem_attribute != null) {
						String chem = chem_attribute.getValue().toString();
						b.append("<b>Chemicals: </b>" + chem + "<br>");
					}
				}
				if (doi_an != null) {
					Attribute doi_attribute = c.getAttribute(doi_an);
					if (doi_attribute != null) {
						String doi = doi_attribute.getValue().toString();
						b.append("<b>DOI: </b>" + doi + "<br>");
					}
				}
			}
		} else if (e instanceof ONDEXRelation) {

			ONDEXRelation r = (ONDEXRelation) e;

			if (evidence_an != null) {
				Attribute evidence_attribute = r.getAttribute(evidence_an);
				if (evidence_attribute != null) {
					HashSet<String> eviSentences = (HashSet<String>) evidence_attribute.getValue();
					b.append(ATT_NAME_EVIDENCE + "<br>");
					for (String sen : eviSentences) {
						ONDEXConcept c1 = r.getFromConcept();
						for (ConceptName cn : c1.getConceptNames()) {
							String name = cn.getName();
							sen = sen.replaceAll("(?i)" + name, "<SPAN style=\"BACKGROUND-COLOR: #ffff00\">$0</SPAN>");
						}
						ONDEXConcept c2 = r.getToConcept();
						for (ConceptName cn : c2.getConceptNames()) {
							String name = cn.getName();
							sen = sen.replaceAll("(?i)" + name, "<SPAN style=\"BACKGROUND-COLOR: #80FF00\">$0</SPAN>");
						}

						b.append(sen + "<br>");
					}
				}
			}
		}

		return b.toString();

	}

	/**
	 * get producer name
	 * 
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#getName()
	 */
	@Override
	public String getName() {
		return "Publication";
	}

	/**
	 * get producer version
	 * 
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#getVersion()
	 */
	@Override
	public String getVersion() {
		return "03.06.2008";
	}

}
