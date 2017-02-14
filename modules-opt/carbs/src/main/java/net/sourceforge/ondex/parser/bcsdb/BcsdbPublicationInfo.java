package net.sourceforge.ondex.parser.bcsdb;

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
public class BcsdbPublicationInfo {
	String journalString     	  = ""; 
	String volumeString      	  = ""; 
	String volumeParenString 	  = ""; 
	String startPageString   	  = ""; 
	String endPageString     	  = ""; 
	String authorsString      	  = "";
	String yearPublishedString    = "";
	String articleTitleString     = "";
	String urlString              = "";
	String publisherString        = "";
	String analyticalMethodString = "";
	String keywordsString         = "";

	public Set<String> analyticalMethod = new HashSet<String>();
	public Set<String> antigenInfo      = new HashSet<String>();
	public Set<DBRef > references       = new HashSet<DBRef> ();

	static ONDEXGraph graph;
	static DataSource dataSource;
	static ConceptClass cc;
	static EvidenceType et;

	String getCitation()
	{
		String citationString = journalString;
		
		if(!volumeString     .isEmpty()) { citationString += " "  + volumeString ; }
		if(!volumeParenString.isEmpty()) { citationString += " (" + volumeParenString + ")" ; }
		if(!startPageString  .isEmpty()) { citationString += " p. " + startPageString ; }
		if(!(startPageString.isEmpty() && endPageString.isEmpty())) { citationString += "-" ;}
		if(!endPageString    .isEmpty()) { citationString += endPageString ; }
		
		return citationString;
	}
	
	//Attributes for Publication
	static AttributeName authorsAttributeName            ;
	static AttributeName citationAttributeName           ;
	static AttributeName yearPublishedAttributeName      ;
	static AttributeName articleTitleAttributeName       ;
	static AttributeName urlAttributeName                ;
	static AttributeName publisherAttributeName          ;
	static AttributeName analyticalMethodAttributeName   ;
	static AttributeName keywordsAttributeName           ;

	public static void initialiseMetaData(ONDEXGraph g) {
		graph = g;
		ONDEXGraphMetaData metaData = graph.getMetaData();

		// required metadata for a publication
		dataSource = metaData.getFactory().createDataSource("BCSDB", "bacterial carbohydrate structure database");		
		cc = metaData.getConceptClass("Publication"); // see entries of
		et = metaData.getEvidenceType("IMPD");

		// Attributes for publication
		authorsAttributeName            = metaData.getAttributeName("AUTHORS");
		citationAttributeName           = metaData.getAttributeName("JOURNAL_REF");
		yearPublishedAttributeName      = metaData.getAttributeName("YEAR");
		articleTitleAttributeName       = metaData.getAttributeName("AbstractHeader");
		urlAttributeName                = metaData.getAttributeName("URL");
		publisherAttributeName          = metaData.getFactory().createAttributeName("publisher","publisher of journal",String.class);
		analyticalMethodAttributeName   = metaData.getFactory().createAttributeName("analyticalmethod","experimental techniqes used in publication",String.class);		
		keywordsAttributeName           = metaData.getFactory().createAttributeName("keywords","publication keywords",String.class);		
	}

	@Override
	public boolean equals(Object obj) {
		return getCitation().equals(obj);
	}

	@Override
	public int hashCode() {
		return getCitation().hashCode();
	}

	/**
	 * Creates the corresponding ONDEXConcept on the given graph.
	 * 
	 * @return created publication ONDEXConcept
	 */
	public ONDEXConcept createONDEXConcept() {

		String citation = getCitation();
		
		if (!citation.isEmpty()) {
			ONDEXConcept c = graph.getFactory().createConcept(citation, dataSource, cc,
					et);

			// publication associated Attribute
			c.createAttribute(citationAttributeName, citation, false);
			c.createConceptName(citation, true);
			//
			
			if (!authorsString          .isEmpty()) { c.createAttribute(authorsAttributeName,          authorsString,          false); }
			if (!articleTitleString     .isEmpty()) { c.createAttribute(articleTitleAttributeName,     articleTitleString,     false); }
			if (!yearPublishedString    .isEmpty()) { c.createAttribute(yearPublishedAttributeName,    Integer.valueOf(yearPublishedString),    false); }
			if (!urlString              .isEmpty()) { c.createAttribute(urlAttributeName,              urlString,              false); }
			if (!publisherString        .isEmpty()) { c.createAttribute(publisherAttributeName,        publisherString,        false); }
			if (!analyticalMethodString .isEmpty()) { c.createAttribute(analyticalMethodAttributeName, analyticalMethodString, false); }
			if (!keywordsString         .isEmpty()) { c.createAttribute(keywordsAttributeName,         keywordsString,         false); }

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
