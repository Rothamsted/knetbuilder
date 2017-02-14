package net.sourceforge.ondex.parser.carbbank;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;

/**
 * Keeps all information about a publication in one place and allows for direct
 * creation of the corresponding ONDEXConcept.
 * 
 * @author taubertj
 * 
 */
public class CarbbankPublicationInfo {
	
	public String title    = "";
	public String authors  = "";
	public String citation = "";

	public Set<String> analyticalMethod = new HashSet<String>();
	public Set<String> antigenInfo      = new HashSet<String>();
	public Set<DBRef > references       = new HashSet<DBRef> ();

	static ONDEXGraph graph;
	static DataSource dataSource;
	static ConceptClass cc;
	static EvidenceType et;
	static AttributeName analyticalMethodAttributeName;
	static AttributeName antigenInformationAttributeName;
	static AttributeName authorsAttributeName;
	static AttributeName citationAttributeName;
	static AttributeName articleTitleAttributeName;

	public static void initialiseMetaData(ONDEXGraph g) {
		graph = g;
		ONDEXGraphMetaData metaData = graph.getMetaData();

		// required metadata for a publication
		dataSource = metaData.getFactory().createDataSource("CARBBANK", "carbbank");
		cc = metaData.getConceptClass("Publication"); // see entries of
		et = metaData.getEvidenceType("IMPD");

		// Attributes for publication
		analyticalMethodAttributeName =
			metaData.getFactory().createAttributeName("analyticalmethod","experimental techniqes used in publication", Set.class);

		antigenInformationAttributeName = 
			metaData.getFactory().createAttributeName("antigeninformation","information about antigens used in publication",Set.class);

		authorsAttributeName      = metaData.getAttributeName("AUTHORS");
		citationAttributeName     = metaData.getAttributeName("JOURNAL_REF");
		articleTitleAttributeName = metaData.getAttributeName("AbstractHeader");
	}

	@Override
	public boolean equals(Object obj) {
		return citation.equals(obj);
	}

	@Override
	public int hashCode() {
		return citation.hashCode();
	}

	/**
	 * Creates the corresponding ONDEXConcept on the given graph.
	 * 
	 * @return created publication ONDEXConcept
	 */
	public ONDEXConcept createONDEXConcept() {

		if (!citation.isEmpty()) {
			ONDEXConcept c = graph.getFactory().createConcept(citation, dataSource, cc,
					et);

			// publication associated Attribute
			c.createAttribute(citationAttributeName, citation, false);
			c.createConceptName(citation, true);
			if (!authors         .isEmpty()) { c.createAttribute(authorsAttributeName,            authors,          false); }
			if (!title           .isEmpty()) { c.createAttribute(articleTitleAttributeName,       title,            false); }
			if (!analyticalMethod.isEmpty()) { c.createAttribute(analyticalMethodAttributeName,   analyticalMethod, false); }
			if (!antigenInfo     .isEmpty()) { c.createAttribute(antigenInformationAttributeName, antigenInfo,      false); }

			// add every occurrence of a references as concept accession
			for (DBRef dbref : references) {
				if   (dbref.dataSource.getId().equals("NLM")) { c.createConceptAccession(dbref.reference, dbref.dataSource, false); }
				else { c.createConceptAccession (dbref.reference, dbref.dataSource, true); }
			}

			return c;
		}
		else { throw new RuntimeException("Created without valid citation tag."); } 
	}
}
