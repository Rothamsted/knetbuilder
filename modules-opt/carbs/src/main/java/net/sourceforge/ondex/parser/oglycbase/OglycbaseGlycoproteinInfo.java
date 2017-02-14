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
public class OglycbaseGlycoproteinInfo {
	public String idString         = "";
	public String glycprotString   = "";
	public String dbrefString      = "";
	public String oglycanString    = "";
	public String[]serExpStrings   = new String[0]; 
	public String[]serPreStrings   = new String[0]; 
	public String[]thrExpStrings   = new String[0]; 
	public String[]thrPreStrings   = new String[0]; 
	public String[]asnExpStrings   = new String[0]; 
	public String[]asnPreStrings   = new String[0]; 
	public String[]trpExpStrings   = new String[0]; 
	public String[]trpPreStrings   = new String[0]; 
	public Integer numseq          = -1;
	public String sequenceString                  = "";
	public String sequenceByGlycosylationString   = "";
	public Set<String>commentStrings = new HashSet<String>();
	public Set<DBRef >references     = new HashSet<DBRef> ();
	
	static ONDEXGraph graph;
	static DataSource dataSource;
	static ConceptClass cc;
	static EvidenceType et;
	
	//Attributes for Publication
	static AttributeName oglycbaseIDAttributeName                                ;
	static AttributeName proteinNameAttributeName                                ;
	static AttributeName oglycanInfoAttributeName                                ;
	static AttributeName experimentalGlycosylatedSerinePositionsAttributeName    ;
	static AttributeName predictedGlycosylatedSerinePositionsAttributeName       ; 
	static AttributeName experimentalGlycosylatedThreoninePositionsAttributeName ;
	static AttributeName predictedGlycosylatedThreoninePositionsAttributeName    ;
	static AttributeName experimentalGlycosylatedAsparaginePositionsAttributeName;
	static AttributeName predictedGlycosylatedAsparaginePositionsAttributeName   ;
	static AttributeName experimentalGlycosylatedTryptophanPositionsAttributeName;
	static AttributeName predictedGlycosylatedTryptophanPositionsAttributeName   ;
	static AttributeName sequenceLengthAttributeName                             ;
	static AttributeName sequenceAttributeName                                   ;
	static AttributeName sequenceByGlycosylationAttributeName                    ;
	static AttributeName commentsAttributeName                                   ; 
	
	public static void initialiseMetaData(ONDEXGraph g) {
		graph = g;
		ONDEXGraphMetaData metaData = graph.getMetaData();

		// required metadata for a glycoprotein
		dataSource = metaData.getFactory().createDataSource("OGLYCBASE", "o-linked glycoprotein database");		
		cc = metaData.getFactory().createConceptClass("Glycoprotein");
		et = metaData.getEvidenceType("IMPD");

		// Attributes for glycoprotein
		oglycbaseIDAttributeName                                 = metaData.getFactory().createAttributeName("oglycbaseID", "accession number in OGlycBASE", String.class);
		proteinNameAttributeName                                 = metaData.getFactory().createAttributeName("proteinname", "name of protein", String.class);
		oglycanInfoAttributeName                                 = metaData.getFactory().createAttributeName("oglycaninfo", "notes about glycosylation", String.class);
		experimentalGlycosylatedSerinePositionsAttributeName     = metaData.getFactory().createAttributeName("expgserpos" , "experimentally determined positions of glycosylated serine residues", String[].class);
		predictedGlycosylatedSerinePositionsAttributeName        = metaData.getFactory().createAttributeName("pregserpos" , "experimentally determined positions of glycosylated serine residues", String[].class);
		experimentalGlycosylatedThreoninePositionsAttributeName  = metaData.getFactory().createAttributeName("expgthrpos" , "experimentally determined positions of glycosylated threonine residues", String[].class);
		predictedGlycosylatedThreoninePositionsAttributeName     = metaData.getFactory().createAttributeName("pregthrpos" , "experimentally determined positions of glycosylated threonine residues", String[].class);
		experimentalGlycosylatedAsparaginePositionsAttributeName = metaData.getFactory().createAttributeName("expgasnpos" , "experimentally determined positions of glycosylated asparagine residues", String[].class);
		predictedGlycosylatedAsparaginePositionsAttributeName    = metaData.getFactory().createAttributeName("pregasnpos" , "experimentally determined positions of glycosylated asparagine residues", String[].class);
		experimentalGlycosylatedTryptophanPositionsAttributeName = metaData.getFactory().createAttributeName("expgtrppos" , "experimentally determined positions of glycosylated tryptophan residues", String[].class);
		predictedGlycosylatedTryptophanPositionsAttributeName    = metaData.getFactory().createAttributeName("pregtrppos" , "experimentally determined positions of glycosylated tryptophan residues", String[].class);
		sequenceLengthAttributeName                              = metaData.getFactory().createAttributeName("seqlen"     , "number of residues in protein sequence", Integer.class);
		sequenceAttributeName                                    = metaData.getFactory().createAttributeName("seq"        , "amino-acid sequence", String.class);
		sequenceByGlycosylationAttributeName                     = metaData.getFactory().createAttributeName("gseq"       , "amino-acid sequence with glycosylation info", String.class);
		commentsAttributeName                                    = metaData.getFactory().createAttributeName("comments"   , "comments", HashSet.class);

	}

	@Override
	public boolean equals(Object obj) {
		return idString.equals(obj);
	}

	@Override
	public int hashCode() {
		return idString.hashCode();
	}

	/**
	 * Creates the corresponding ONDEXConcept on the given graph.
	 * 
	 * @return created publication ONDEXConcept
	 */
	public ONDEXConcept createONDEXConcept() {
		
		if (!(glycprotString).isEmpty()) {
			ONDEXConcept c = graph.getFactory().createConcept(glycprotString, dataSource, cc,
					et);

			//Glycoprotein-associated Attribute
			c.createAttribute(proteinNameAttributeName, glycprotString, false);
			c.createConceptName(glycprotString, true);
			
			if (!idString        .isEmpty()) { c.createAttribute( oglycbaseIDAttributeName, idString       , false); }
			if (!oglycanString   .isEmpty()) { c.createAttribute( oglycanInfoAttributeName, oglycanString  , false); }
			if (serExpStrings   .length > 0) { c.createAttribute( experimentalGlycosylatedSerinePositionsAttributeName     , serExpStrings, false); }
			if (serPreStrings   .length > 0) { c.createAttribute( predictedGlycosylatedSerinePositionsAttributeName        , serPreStrings, false); }
			if (thrExpStrings   .length > 0) { c.createAttribute( experimentalGlycosylatedThreoninePositionsAttributeName  , thrExpStrings, false); }
			if (thrPreStrings   .length > 0) { c.createAttribute( predictedGlycosylatedThreoninePositionsAttributeName     , thrPreStrings, false); }
			if (asnExpStrings   .length > 0) { c.createAttribute( experimentalGlycosylatedAsparaginePositionsAttributeName , asnExpStrings, false); }
			if (asnPreStrings   .length > 0) { c.createAttribute( predictedGlycosylatedAsparaginePositionsAttributeName    , asnPreStrings, false); }
			if (trpExpStrings   .length > 0) { c.createAttribute( experimentalGlycosylatedTryptophanPositionsAttributeName , trpExpStrings, false); }
			if (trpPreStrings   .length > 0) { c.createAttribute( predictedGlycosylatedTryptophanPositionsAttributeName    , trpPreStrings, false); }
			if (numseq > 0) { c.createAttribute(   sequenceLengthAttributeName , numseq , false ); }
			if (!sequenceString                .isEmpty()) { c.createAttribute( sequenceAttributeName                 ,      sequenceString            , false); }
			if (!sequenceByGlycosylationString .isEmpty()) { c.createAttribute( sequenceByGlycosylationAttributeName  ,  sequenceByGlycosylationString , false); }
			if (!commentStrings.isEmpty()) { c.createAttribute(   commentsAttributeName , commentStrings , false); }
			
			// add every occurrence of a references as concept accession
			for (DBRef dbref : references) {
				if(dbref.reference.length()>5 && dbref.reference.startsWith("HGNC:")) {
					dbref.reference = dbref.reference.substring(6);
				}
				c.createConceptAccession (dbref.reference, dbref.dataSource, true);
			}

			return c;
		}
		else { throw new RuntimeException("Attempted to create glycoprotein node without name."); } 
	}
}
