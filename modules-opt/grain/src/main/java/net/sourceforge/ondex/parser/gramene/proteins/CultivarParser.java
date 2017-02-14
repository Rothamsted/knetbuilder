package net.sourceforge.ondex.parser.gramene.proteins;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.parser.gramene.MetaData;
import net.sourceforge.ondex.parser.gramene.Parser;

/**
 *   `species_id` int(11) NOT NULL default '0',
 *   `ncbi_taxa_id` int(11) default NULL,
 *   `common_name` varchar(255) default NULL,
 *   `lineage_string` text,
 *   `genus` varchar(32) default NULL,
 *   `species` varchar(32) default NULL,
 * 
 *   `cultivar_id` int(11) NOT NULL default '0',
 *   `species_id` int(11) NOT NULL default '0',
 *   `cultivar_name` varchar(100) NOT NULL default '',
 *   
 *   
 *  `gene_product_to_cultivar` (
 *  `gene_product_to_cultivar_id` int(11) NOT NULL default '0',
 *  `gene_product_id` int(11) NOT NULL default '0',
 *  `cultivar_id` int(11) NOT NULL default '0',
 * 
 * @author hindlem
 *
 */
public class CultivarParser {
	
	private HashMap<Integer, Integer> geneIdTocultivarId = new HashMap<Integer, Integer>();
	private HashMap<Integer, Cultivar> cultivarIdToCultivar = new HashMap<Integer, Cultivar>();
	private HashMap<Integer, Integer> speciesIdToTaxID = new HashMap<Integer, Integer>();
	

	private ONDEXGraph graph;
	
	public CultivarParser(ONDEXGraph graph) {

		this.graph = graph;
	}
	
	/**
	 * 
	 * @param geneProductToCultivar
	 * @param speciesTable
	 * @param cultivarTable
	 */
	public void parse(String geneProductToCultivar, String speciesTable, String cultivarTable) {
		
		ConceptClass ccCultivar = graph.getMetaData().getConceptClass(MetaData.Cultivar);
		if (ccCultivar == null) {
			Parser.propagateEventOccurred(new ConceptClassMissingEvent(MetaData.Cultivar, Parser.getCurrentMethodName()));
		}
		
		DataSource elementOfGRAMENE = graph.getMetaData().getDataSource(MetaData.gramene);
		if (elementOfGRAMENE == null) {
			Parser.propagateEventOccurred(new DataSourceMissingEvent(MetaData.gramene, Parser.getCurrentMethodName()));
		}
		
		EvidenceType etIMPD = graph.getMetaData().getEvidenceType(MetaData.IMPD);
		if (etIMPD == null) {
			Parser.propagateEventOccurred(new EvidenceTypeMissingEvent(MetaData.IMPD, Parser.getCurrentMethodName()));
		}
		
		AttributeName taxIdAttr = graph.getMetaData().getAttributeName(MetaData.taxID);
		if (taxIdAttr == null) {
			Parser.propagateEventOccurred(new AttributeNameMissingEvent(MetaData.taxID, Parser.getCurrentMethodName()));
		}
		
	try {
		BufferedReader input = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(speciesTable),"UTF8"));
		
		Pattern tabPattern = Pattern.compile("\t");
		
		while (input.ready()) {
			String inputLine = input.readLine();

			String[] columns = tabPattern.split(inputLine);
			if (columns.length < 2) { 
				continue;
			}
			
			Integer specieId = Integer.parseInt(columns[0]);
			Integer taxId = Integer.parseInt(columns[1]);
			speciesIdToTaxID.put(specieId, taxId);
		}
		input.close();
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	
	try {
		BufferedReader input = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(cultivarTable),"UTF8"));
		
		Pattern tabPattern = Pattern.compile("\t");
		
		while (input.ready()) {
			String inputLine = input.readLine();

			String[] columns = tabPattern.split(inputLine);
			if (columns.length < 2) { 
				continue;
			}
			
			Integer cultivarId = Integer.parseInt(columns[0]);
			Integer speciesId = Integer.parseInt(columns[1]);
			String name = "";
			
			if (columns.length >= 3) 
				name = columns[2].trim();
			
			String taxID = String.valueOf(speciesIdToTaxID.get(speciesId));
			
			
			
			ONDEXConcept concept = graph.getFactory().createConcept(name, elementOfGRAMENE, ccCultivar, etIMPD);
			concept.createAttribute(taxIdAttr, String.valueOf(taxID), false);
			concept.createConceptAccession(String.valueOf(cultivarId), elementOfGRAMENE, false);
			
			Cultivar cultivar = new Cultivar(taxID,name, concept.getId());
			
			cultivarIdToCultivar.put(cultivarId, cultivar);
		}
		input.close();
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	
	try {
		BufferedReader input = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(geneProductToCultivar),"UTF8"));
		
		Pattern tabPattern = Pattern.compile("\t");
		
		while (input.ready()) {
			String inputLine = input.readLine();

			String[] columns = tabPattern.split(inputLine);
			if (columns.length < 3) { 
				continue;
			}
			
			Integer geneId = Integer.parseInt(columns[1]);
			Integer cultivarId = Integer.parseInt(columns[2]);
			geneIdTocultivarId.put(geneId, cultivarId);
		}
		input.close();
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	}

	public String getNCBITaxID(Integer speciesId) {
		Integer taxId = speciesIdToTaxID.get(speciesId);
		if (taxId != null) {
			return String.valueOf(taxId);
		}
		return null;
	}
	
	public Cultivar getCultivar(Integer cultivarId) {
		return cultivarIdToCultivar.get(cultivarId);
	}
	
	public Integer getCultivarId(Integer geneProduct) {
		return geneIdTocultivarId.get(geneProduct);
	}
	
	/**
	 * 
	 * @author hindlem
	 *
	 */
	public class Cultivar {

		private String taxID;
		private String name;
		private int ondexConcept;

		/**
		 * 
		 * @param taxID NCBI taxID
		 * @param name cultivar name
		 * @param ondexConcept ONDEX ConceptID for this cultivar
		 */
		public Cultivar(String taxID, String name, Integer ondexConcept) {
			this.taxID = taxID.intern();
			this.name = name;
			this.ondexConcept = ondexConcept;
		}

		public String getTaxID() {
			return taxID;
		}

		public String getName() {
			return name;
		}

		public int getOndexConcept() {
			return ondexConcept;
		}

	}
}
