package net.sourceforge.ondex.parser.oglycbase;

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
 * 
 * @author victorlesk
 * 
 */ 
public class OglycbasePublicationInfo {
	public String citationString      = "";
	public String authorsString       = "";
	public String yearPublishedString = "";
	public Set<DBRef > references  = new HashSet<DBRef> ();
	
	static ONDEXGraph graph;
	static DataSource dataSource;
	static DataSource nlm;
	static ConceptClass cc;
	static EvidenceType et;
	
	public OglycbasePublicationInfo(String referenceString) throws Exception { 
		super();
		
		int openParenIndex  = referenceString.indexOf('(');
		int closeParenIndex = referenceString.indexOf(')');
		int lastDotIndex    = referenceString.lastIndexOf('.');

		if(openParenIndex >= 0 && closeParenIndex == openParenIndex + 5) {
			authorsString       = referenceString.substring(0,openParenIndex-1).trim();
			yearPublishedString = referenceString.substring(openParenIndex+1,closeParenIndex-1);
			citationString      = referenceString.substring(closeParenIndex+1,lastDotIndex).trim();
		} else {
			if(referenceString.contains("Medline: 98241382")) {
				authorsString       = "Neumann, GM., Marinaro JA. and Bach LA.";
				yearPublishedString = "";
				citationString      = "Biochemistry, 37, 6572-6585";
			} else {
				throw new Exception("Could not parse reference.");
			}
		}
		

		
		String[]referenceStrings = referenceString.substring(lastDotIndex + 1).split(":");

		if(referenceStrings.length == 2) { 
			if(referenceStrings[0].trim().equals("Medline") ||
					referenceStrings[0].trim().equals("PMID") ||
					referenceStrings[0].trim().equals("PM") ) { 
				references.add(new DBRef(referenceStrings[1].trim(),nlm));
			} else {
				System.out.println("Reference to non-existing meta data DataSource specified by " + referenceStrings[0].trim() + ".");
			}
		}
	}

	//Attributes for Publication
	static AttributeName citationAttributeName           ;
	static AttributeName authorsAttributeName            ;
	static AttributeName yearPublishedAttributeName      ;
	
	public static void initialiseMetaData(ONDEXGraph g) {
		graph = g;
		ONDEXGraphMetaData metaData = graph.getMetaData();

		// required metadata for a publication
		dataSource  = metaData.getFactory().createDataSource("OGLYCBASE", "o-linked glycoprotein database");
		nlm = metaData.getDataSource("NLM");
		cc  = metaData.getConceptClass("Publication"); // see entries of
		et  = metaData.getEvidenceType("IMPD");

		// Attributes for publication
		authorsAttributeName            = metaData.getAttributeName("AUTHORS");
		citationAttributeName           = metaData.getAttributeName("JOURNAL_REF");
		yearPublishedAttributeName      = metaData.getAttributeName("YEAR");
	}

	@Override
	public boolean equals(Object obj) {
		return citationString.equals(obj);
	}

	@Override
	public int hashCode() {
		return citationString.hashCode();
	}

	/**
	 * Creates the corresponding ONDEXConcept on the given graph.
	 * 
	 * @return created publication ONDEXConcept
	 */
	public ONDEXConcept createONDEXConcept() {

		if (!citationString.isEmpty()) {
			ONDEXConcept c = graph.getFactory().createConcept(citationString, dataSource, cc, et);

			// publication associated Attribute
			c.createAttribute(citationAttributeName, citationString, false);
			c.createConceptName(citationString, true);

			if (!authorsString          .isEmpty()) { c.createAttribute(authorsAttributeName,          authorsString,          false); }
			if (!yearPublishedString    .isEmpty()) { c.createAttribute(yearPublishedAttributeName,    yearPublishedString,    false); }

			// add every occurrence of a references as concept accession
			for (DBRef dbref : references) {
				if   (dbref.dataSource.getId().equals("NLM")) { c.createConceptAccession(dbref.reference, dbref.dataSource, false); }
				else if (dbref.dataSource.getId().equals("DOI")) { c.createConceptAccession(dbref.reference, dbref.dataSource, false); }
				else { c.createConceptAccession (dbref.reference, dbref.dataSource, true); }
			}

			return c;
		}
		else { throw new RuntimeException("Created without valid citation tag."); } 
	}
}

