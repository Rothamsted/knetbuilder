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
public class BcsdbCompoundInfo {

	public Set<DBRef> references = new HashSet<DBRef>();

	public String linearStructure    = "";
	public String structureType		 = "";
	public String molecularMass	 	 = "";
	public String aglyconInfo		 = "";
	public String molecularFormula	 = "";
	public String trivialName	 	 = "";

	static ONDEXGraph graph;
	static DataSource dataSource;
	static DataSource ccsd;		
	static ConceptClass cc;
	static EvidenceType et;

	//Attributes for Comp
	static AttributeName linearStructureAttributeName  ;
	static AttributeName structureTypeAttributeName    ;
	static AttributeName molecularMassAttributeName    ;
	static AttributeName aglyconInfoAttributeName      ;
	static AttributeName molecularFormulaAttributeName ;
	static AttributeName trivialNameAttributeName      ;

	public static void initialiseMetaData(ONDEXGraph g) {
		graph = g;
		ONDEXGraphMetaData metaData = graph.getMetaData();

		dataSource   = metaData.getFactory().createDataSource("BCSDB", "bacterial carbohydrate structure database");		
		ccsd = metaData.getDataSource("CCSD");		
		cc   = metaData.getConceptClass("Comp");
		et   = metaData.getEvidenceType("IMPD");

		linearStructureAttributeName  = metaData.getFactory().createAttributeName("linearBCSDB","structure in BCSDB linear format",String.class);
		structureTypeAttributeName    = metaData.getFactory().createAttributeName("typeBCSDB","structure type according to BCSDB classification",String.class);
		molecularMassAttributeName    = metaData.getFactory().createAttributeName("molmass","molecular mass",String.class);
		aglyconInfoAttributeName      = metaData.getFactory().createAttributeName("alglyconinfo","info about biochemical entity to which glycan is bound",String.class);
		molecularFormulaAttributeName = metaData.getFactory().createAttributeName("molformula","molecular formula",String.class);
		trivialNameAttributeName      = metaData.getFactory().createAttributeName("trivialname","trivial name",String.class);
	}

	@Override
	public boolean equals(Object obj) {
		return linearStructure.equals(obj);
	}

	@Override
	public int hashCode() {
		return linearStructure.hashCode();
	}

	/**
	 * Creates the corresponding ONDEXConcept on the given graph.
	 * 
	 * @return created publication ONDEXConcept
	 */
	public ONDEXConcept createONDEXConcept() {

		if (!linearStructure.isEmpty()) {
			ONDEXConcept c = graph.getFactory().createConcept(linearStructure, dataSource, cc, et);

			// publication associated Attribute
			c.createAttribute(linearStructureAttributeName, linearStructure, false);
			c.createConceptName(linearStructure, true);
			
			if (! structureType	   .isEmpty()) { c.createAttribute(structureTypeAttributeName   , structureType	, false);}
			if (! molecularMass	   .isEmpty() && !(molecularMass.equals("0"))) { 
				c.createAttribute(molecularMassAttributeName   , molecularMass	, false); }
			if (! aglyconInfo	   .isEmpty()) { c.createAttribute(aglyconInfoAttributeName     , aglyconInfo	    , false);}
			if (! molecularFormula .isEmpty()) { c.createAttribute(molecularFormulaAttributeName, molecularFormula , false);}
			if (! trivialName      .isEmpty()) { c.createAttribute(trivialNameAttributeName     , trivialName      , false);}

			// add every occurrence of a reference as concept accession
			//LABEL ask JAN about this
			for (DBRef dbref : references) {
				if   (!(dbref.dataSource.getId().equals("PMID"))) { c.createConceptAccession(dbref.reference, dbref.dataSource, false); }
				else { c.createConceptAccession (dbref.reference, dbref.dataSource, true); }
			}

			return c;
		}
		else { throw new RuntimeException("Created without valid structure index tag."); } 
	}
}
