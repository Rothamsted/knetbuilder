package net.sourceforge.ondex.parser.gramene;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * 
 * INDEX for expected data gene table
 * 
 * 0 `ontology_association_id` int(11) NOT NULL default '0' PRIMARY KEY,
 * 1 `object_id` int(11) NOT NULL default '0',
 * 2 `object_table` varchar(32) NOT NULL default '',
 * 3 `term_accession` varchar(32) NOT NULL default '',
 * 4 `term_type` varchar(64) default NULL,
 * 
 * INDEX for expected data protein table
 * 
 * 0 `association_id` int(11) NOT NULL default '0' PRIMARY KEY,
 * 1 `term_accession` varchar(16) NOT NULL default '',
 * 2 `gene_product_id` int(11) NOT NULL default '0',
 * 3 `term_type` varchar(128) default NULL,
 * 4 `is_not` tinyint(1) default NULL,
 * 3 `role_group` int(11) default NULL,
 * 6 `assocdate` int(11) default NULL
 * 
 * 
 * Items are transfered from the gene Ontology Association File to a indexed hash
 * which can be retrieved later
 * @author hoekmanb, hindlem
 *
 */
public class OntologyParser {

	private static final boolean DEBUG = false;
	
	private HashMap<Integer,Set<Integer>> geneIdToCID = new HashMap<Integer,Set<Integer>>();
	private HashMap<Integer,Set<Integer>> proteinIdToCID = new HashMap<Integer,Set<Integer>>();

	private String geneOntologyAssocFile;
	private String proteinOntologyAssocFile;

	private ONDEXGraph og;

	/**
	 * parses gene ontology associations from the provided file
	 * @param geneOntologyAssocFile
	 *  @param proteinOntologyAssocFile
	 */
	public OntologyParser(String geneOntologyAssocFile, String proteinOntologyAssocFile, ONDEXGraph og) {
		this.geneOntologyAssocFile = geneOntologyAssocFile;
		this.proteinOntologyAssocFile = proteinOntologyAssocFile;

		this.og = og;
	}

	public void parseOntologies() {

		int i = 0;
		int j = 0;

		Pattern tabPattern = Pattern.compile("\t");
		
		HashMap<String,HashMap<String, Integer>> typeToAcc = new HashMap<String,HashMap<String, Integer>>(500);

		EvidenceType etIMPD = og.getMetaData().getEvidenceType(MetaData.IMPD);
		Parser.checkCreated(etIMPD, MetaData.IMPD);
		
		DataSource elementOfGRAMENE = og.getMetaData().getDataSource(MetaData.gramene);
		Parser.checkCreated(elementOfGRAMENE, MetaData.gramene);
		
		Map<String, ConceptClass> ccMap = new TreeMap<String, ConceptClass>();
		ccMap.put("Biological Process".toUpperCase(), og.getMetaData().getConceptClass(MetaData.BioProc));
		ccMap.put("Molecular Function".toUpperCase(), og.getMetaData().getConceptClass(MetaData.MolFunc));
		ccMap.put("Cellular Component".toUpperCase(), og.getMetaData().getConceptClass(MetaData.CelComp));
		ccMap.put("Trait".toUpperCase(), og.getMetaData().getConceptClass(MetaData.TraitOnt));
		ccMap.put("Plant Structure".toUpperCase(), og.getMetaData().getConceptClass(MetaData.POSTRUC));
		ccMap.put("Plant Growth and Development Stage".toUpperCase(), og.getMetaData().getConceptClass(MetaData.PODevStag));
		ccMap.put("Cereal Plant Growth Stage".toUpperCase(), og.getMetaData().getConceptClass(MetaData.GRO));
		ccMap.put("Enviroment".toUpperCase(), og.getMetaData().getConceptClass(MetaData.Environment));

		Iterator<String> nameIt = ccMap.keySet().iterator();
		while (nameIt.hasNext()) {
			String name = nameIt.next();
			Parser.checkCreated(ccMap.get(name), name);
		}
		
		Map<String, DataSource> dataSourceMap = new TreeMap<String, DataSource>();
		dataSourceMap.put("Biological Process".toUpperCase(), og.getMetaData().getDataSource(MetaData.goCv));
		dataSourceMap.put("Molecular Function".toUpperCase(), og.getMetaData().getDataSource(MetaData.goCv));
		dataSourceMap.put("Cellular Component".toUpperCase(), og.getMetaData().getDataSource(MetaData.goCv));
		dataSourceMap.put("Trait".toUpperCase(), og.getMetaData().getDataSource(MetaData.TraitOnt));
		dataSourceMap.put("Plant Structure".toUpperCase(), og.getMetaData().getDataSource(MetaData.plant_onto));
		dataSourceMap.put("Plant Growth and Development Stage".toUpperCase(), og.getMetaData().getDataSource(MetaData.plant_onto));
		dataSourceMap.put("Cereal Plant Growth Stage".toUpperCase(), og.getMetaData().getDataSource(MetaData.plant_onto));
		dataSourceMap.put("Enviroment".toUpperCase(), og.getMetaData().getDataSource(MetaData.env_onto));

		nameIt = dataSourceMap.keySet().iterator();
		while (nameIt.hasNext()) {
			String name = nameIt.next();
			Parser.checkCreated(dataSourceMap.get(name), name);
		}
		
		HashSet<String> unknownTypes = new HashSet<String>();

		//parse geneOntologyAssocFile for genes
		try {
			BufferedReader input = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(geneOntologyAssocFile),"UTF8"));
			
			while (input.ready()) {
				String inputLine = input.readLine();
				String[] columns = tabPattern.split(inputLine);

				if (columns.length < 5) {
					continue; //is not relation ignore
				}

				String table = columns[2].trim();
				String accession = columns[3].trim();
				String termType = columns[4].trim().toUpperCase();
				Integer geneId = Integer.parseInt(columns[1].trim());

				if (!table.equalsIgnoreCase("gene")) {
					continue;
				}

				//index by type
				HashMap<String, Integer> typeList = typeToAcc.get(termType);
				if (typeList == null) {
					typeList = new HashMap<String, Integer>();
					typeToAcc.put(termType.toUpperCase(), typeList);
				}

				//index by accession
				Integer cid = typeList.get(accession.toUpperCase());
				if (cid == null) {
					ConceptClass ot = ccMap.get(termType);

					if (ot != null) {
						ONDEXConcept concept = og.getFactory().createConcept(accession, elementOfGRAMENE, ot, etIMPD);
						cid = concept.getId();
						i++;
						typeList.put(accession.toUpperCase(), cid);
						
						DataSource dataSource = dataSourceMap.get(termType);
						if (dataSource != null) {
							concept.createConceptAccession(accession, dataSource, false);
						} else {
							unknownTypes.add(termType);
						}
					} else {
						unknownTypes.add(termType);
					}
				}

				Set<Integer> assocs = geneIdToCID.get(geneId);
				if (assocs == null) {
					assocs = new HashSet<Integer>();
					geneIdToCID.put(geneId, assocs);
				}
				if (cid != null) 
					assocs.add(cid.intValue());
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//parse proteinOntologyAssocFile for proteins
		try {
			BufferedReader input = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(proteinOntologyAssocFile),"UTF8"));

			while (input.ready()) {
				String inputLine = input.readLine();
				String[] columns = tabPattern.split(inputLine);

				try {
					if (columns.length < 4 || (columns.length >= 5 && Integer.parseInt(columns[4]) == 1)) {
						continue; //is not relation ignore
					}
				} catch (NumberFormatException e) {
					//do nothing this means there is somthing else like null in the col
				}

				String accession = columns[1].trim();
				String termType = columns[3].trim().toUpperCase();
				Integer geneProduct = Integer.parseInt(columns[2].trim());

				HashMap<String, Integer> typeList = typeToAcc.get(termType.toUpperCase());
				if (typeList == null) {
					typeList = new HashMap<String, Integer>();
					typeToAcc.put(termType.toUpperCase(), typeList);
				}

				Integer cid = typeList.get(accession.toUpperCase());
				if (cid == null) {
					ConceptClass ot = ccMap.get(termType.toUpperCase());

					if (ot != null) {
						ONDEXConcept concept = og.getFactory().createConcept(accession, elementOfGRAMENE, ot, etIMPD);
						cid = concept.getId();
						j++;
						typeList.put(accession.toUpperCase(), cid);

						DataSource dataSource = dataSourceMap.get(termType.toUpperCase());
						if (dataSource != null) {
							concept.createConceptAccession(accession, dataSource, false);
						} else {
							unknownTypes.add(termType);
						}
					} else {
						unknownTypes.add(termType);
					}
				}

				Set<Integer> assocs = proteinIdToCID.get(geneProduct);
				if (assocs == null) {
					assocs = new HashSet<Integer>();
					proteinIdToCID.put(geneProduct, assocs);
				}
				if (cid != null) 
					assocs.add(cid.intValue());
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Iterator<String> it = unknownTypes.iterator();
		while (it.hasNext()) {
			System.err.println("Did not know what to do with ontology type: "+it.next());
		}
		if (DEBUG) System.out.println("Parsed "+i+" gene ontology concepts");
		if (DEBUG) System.out.println("Parsed "+j+" protein ontology concepts");
		if (DEBUG) System.out.println("references from genes = "+geneIdToCID.size());
		if (DEBUG) System.out.println("references from proteins = "+proteinIdToCID.size());
	}

	/**
	 * Method for accessing indexed ontology associations
	 * @param objectId the gene object to fetch ontology references on
	 * @return the ontolgy concept eid
	 */
	public Set<Integer> getGeneAssocations(Integer objectId) {
		return geneIdToCID.get(objectId);
	}

	/**
	 * Method for accessing indexed ontology associations
	 * @param objectId the protein object to fetch ontology references on
	 * @return the ontolgy concept eid
	 */
	public Set<Integer> getProteinAssocations(Integer objectId) {
		return proteinIdToCID.get(objectId);
	}
}
