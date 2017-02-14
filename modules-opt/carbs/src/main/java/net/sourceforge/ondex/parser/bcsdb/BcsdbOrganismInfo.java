package net.sourceforge.ondex.parser.bcsdb;

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
public class BcsdbOrganismInfo {
	
	public String[]  taxids          = new String[0];

	static ONDEXGraph graph;
	static DataSource dataSource;
	static ConceptClass cc;
	static EvidenceType et;
	static DataSource external;
	
	//Attributes for Bacterial organism  
	static AttributeName taxidAttributeName          ;    

	public static void initialiseMetaData(ONDEXGraph g) {
		graph = g;
		ONDEXGraphMetaData metaData = graph.getMetaData();

		dataSource = metaData.getFactory().createDataSource("BCSDB", "bacterial carbohydrate structure database");		
		cc = metaData.getConceptClass("Taxon");
		et = metaData.getEvidenceType("IMPD");
		external = metaData.getDataSource("TX"); // as used by NCBI Taxonomy parser
		
		taxidAttributeName = metaData.getAttributeName("TAXID");
	}
	
	/**
	 * Creates the corresponding ONDEXConcept on the given graph.
	 * 
	 * @return created publication ONDEXConcept
	 */
	public ONDEXConcept createONDEXConcept(int index) {

		if (!taxids[index].isEmpty()) {
			ONDEXConcept c = graph.getFactory().createConcept(taxids[index], dataSource, cc, et);

			// publication associated Attribute
			c.createAttribute(taxidAttributeName, taxids[index], false);
			c.createConceptAccession(taxids[index], external, false);
			
			return c;
		}
		else { throw new RuntimeException("Organism without valid taxid tag!"); } 
	}
}
