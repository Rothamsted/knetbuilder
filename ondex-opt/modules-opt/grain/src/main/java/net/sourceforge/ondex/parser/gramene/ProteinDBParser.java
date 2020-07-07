package net.sourceforge.ondex.parser.gramene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.parser.gramene.XrefParser.AccessionReference;
import net.sourceforge.ondex.parser.gramene.proteins.CultivarParser;
import net.sourceforge.ondex.parser.gramene.proteins.CultivarParser.Cultivar;
import net.sourceforge.ondex.parser.gramene.proteins.ProteinDBCellLocalization;
import net.sourceforge.ondex.parser.gramene.proteins.ProteinDBTissueLocalization;
import net.sourceforge.ondex.parser.gramene.proteins.ProteinSequenceParser;

/**
 * INDEX for gene_product data table
 * 0 `gene_product_id` int(11) NOT NULL default '0' PRIMARY KEY,
 * 1 `gene_product_symbol` varchar(128) NOT NULL default '',
 * 2 `organism_dbxref_id` int(11) NOT NULL default '0',
 * 3 `species_id` int(11) default NULL,
 * 4 `gene_product_full_name` text,
 * 
 * Controls the parsing of proteins from the gramene protein database
 * @author hindlem
 *
 */
public class ProteinDBParser {

	private ONDEXGraph graph;

	private GenesDBParser grameneGeneParser;

	private OntologyParser ontoParser;

	private LiteratureDBParser litParser;

	/**
	 * 
	 * @param s current session
	 * @param graph
	 * @param grameneGeneId2conceptId 
	 * @param ontoParser 
	 * @param litParser 
	 */
	public ProteinDBParser(ONDEXGraph graph, GenesDBParser grameneGeneParser, OntologyParser ontoParser, LiteratureDBParser litParser) {
		this.graph = graph;
		this.grameneGeneParser = grameneGeneParser;
		this.ontoParser = ontoParser;
		this.litParser = litParser;
	}

	private static final String SPECIES_FILE = "species.txt";
	private static final String XREF_FILE = "dbxref.txt";
	private static final String REF2OBJECT_FILE = "objectxref.txt";
	private static final String ORGANELLE_FILE = "gene_product_organelle.txt";
	private static final String TISSUE_FILE = "gene_product_tissue.txt";
	
	private static final String GENE_PRODUCT_FILE = "gene_product.txt";
	private static final String GENE_PRODUCT_2_SEQ_FILE = "gene_product_seq.txt";
	private static final String GENE_PRODUCT_2_PID_FILE = "gene_product_to_pid.txt";
	private static final String SEQ_FILE = "seq.txt";

	private static final String CULT_FILE = "cultivar.txt";
	private static final String GENE_PROD_TO_CULT_FILE = "gene_product_to_cultivar.txt";
	
	/**
	 * 
	 * @param dir the directory containing the text files tab dumps for the protein database
	 */
	public void parseProteins(String dir) {
		
		XrefParser xrefs = new XrefParser(dir+File.separator+XREF_FILE);
		Object2XrefParser object2xrefs = new Object2XrefParser(dir+File.separator+REF2OBJECT_FILE, "gramene.protein");
		ProteinDBCellLocalization cellLocalities = new ProteinDBCellLocalization(dir+File.separator+ORGANELLE_FILE);
		ProteinDBTissueLocalization tissuesLocalities = new ProteinDBTissueLocalization(dir+File.separator+TISSUE_FILE);
		CultivarParser cultivars = new CultivarParser(graph);
				
		cultivars.parse(dir+File.separator+GENE_PROD_TO_CULT_FILE, 
				dir+File.separator+SPECIES_FILE, 
				dir+File.separator+CULT_FILE);
		
		DataSource elementOfGRAMENE = graph.getMetaData().getDataSource(MetaData.gramene);
		Parser.checkCreated(elementOfGRAMENE, MetaData.gramene);
		
		DataSource elementOfEC = graph.getMetaData().getDataSource(MetaData.ec);
		Parser.checkCreated(elementOfEC, MetaData.ec);
		
		DataSource uniprot = graph.getMetaData().getDataSource(MetaData.uniprot);
		Parser.checkCreated(uniprot, MetaData.uniprot);
		
		EvidenceType etIMPD = graph.getMetaData().getEvidenceType(MetaData.IMPD);
		Parser.checkCreated(etIMPD, MetaData.IMPD);
		
		AttributeName taxIdAttr = graph.getMetaData().getAttributeName(MetaData.taxID);
		Parser.checkCreated(taxIdAttr, MetaData.taxID);
		
		ConceptClass ccProtein = graph.getMetaData().getConceptClass(MetaData.protein);
		Parser.checkCreated(ccProtein, MetaData.protein);
		
		ConceptClass ccEnzyme = graph.getMetaData().getConceptClass(MetaData.enzyme);
		Parser.checkCreated(ccEnzyme, MetaData.enzyme);
		
		ConceptClass ccEC = graph.getMetaData().getConceptClass(MetaData.ec);
		Parser.checkCreated(ccEC, MetaData.ec);
		
		ConceptClass ccCellCompartment = graph.getMetaData().getConceptClass(MetaData.CelComp);
		Parser.checkCreated(ccCellCompartment, MetaData.CelComp);
		
		ConceptClass ccTissue = graph.getMetaData().getConceptClass(MetaData.Tissue);
		Parser.checkCreated(ccTissue, MetaData.Tissue);
		
		RelationType rtsIsA = graph.getMetaData().getRelationType(MetaData.is_a);
		Parser.checkCreated(rtsIsA, MetaData.is_a);
		
		RelationType rtsCatC = graph.getMetaData().getRelationType(MetaData.cat_c);
		Parser.checkCreated(rtsCatC, MetaData.cat_c);
		
		RelationType rtsEnBy = graph.getMetaData().getRelationType(MetaData.en_by);
		Parser.checkCreated(rtsEnBy, MetaData.en_by);
		
		RelationType rtsPubIn = graph.getMetaData().getRelationType(MetaData.pub_in);
		Parser.checkCreated(rtsPubIn, MetaData.pub_in);
		
		
		RelationType hasFunction = Parser.getRelationType(MetaData.hasFunction, graph);
		RelationType hasParticipant = Parser.getRelationType(MetaData.hasParticipant, graph);
		RelationType locIn = Parser.getRelationType(MetaData.locatedIn, graph);
		
		Pattern tabPattern = Pattern.compile("\t");
		HashSet<String> unknownDb = new HashSet<String>();
		
		HashMap<String, ONDEXConcept> cellularLocations = new HashMap<String, ONDEXConcept>();
		
		HashMap<Integer, Integer> proteinIdTocid = new HashMap<Integer, Integer>();
		
		HashMap<Integer, Object> proteinIdTopid = new HashMap<Integer, Object>();
		

		try {
			
			BufferedReader input = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(dir+File.separator+GENE_PRODUCT_2_PID_FILE),"UTF8"));
			while (input.ready()) {
				String inputLine = input.readLine();
				String[] columns = tabPattern.split(inputLine);
				if (columns.length != 3) { //can be 5 
					continue;
				}
				Integer proteinId = Integer.valueOf(columns[1]);
				
				Object value = proteinIdTopid.get(proteinId);
				if (value == null) {
					value = columns[2].trim();
				} else if (value instanceof String) {
					String[] multiple = new String[2];
					multiple[0] = (String) value;
					multiple[1] = columns[2].trim();
					value = multiple;
				} else if (value instanceof String[]) {
                                        String[] valueS = (String[]) value;
					valueS = Arrays.copyOf(valueS, valueS.length+1);
					valueS[valueS.length-1] = columns[2].trim();
                                        value = valueS;
				}
				proteinIdTopid.put(proteinId, value);
			}
			input.close();
			
			
			input = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(dir+File.separator+GENE_PRODUCT_FILE),"UTF8"));

			while (input.ready()) {
				String inputLine = input.readLine();
				
				String[] columns = tabPattern.split(inputLine);
				if (columns.length < 3) { //can be 5 
					continue;
				}
				
				HashSet<String> descriptions = new HashSet<String>();
				
				Integer id = Integer.parseInt(columns[0].trim());
				String name = columns[1].trim();
				
				ONDEXConcept protein = graph.getFactory().createConcept("PROT_"+id, elementOfGRAMENE, ccProtein, etIMPD);
				proteinIdTocid.put(id, protein.getId());
				
				Object pids = proteinIdTopid.get(id);
				if (pids != null) {
					if (pids instanceof String) {
						protein.createConceptAccession((String)pids, uniprot, true);
					} else if (pids instanceof String[]) {
						for (String pid: (String[])pids) {
							protein.createConceptAccession(pid, uniprot, true);
						}
					}
				}
				
				
				HashSet<String> locals = cellLocalities.getCellLocalization(id);
				if (locals != null) {
					Iterator<String> locResults = locals.iterator();
					while (locResults.hasNext()) {
						String location = locResults.next();
						if (!location.equalsIgnoreCase("na")) {
							ONDEXConcept locConcept = cellularLocations.get(location);
							if (locConcept == null) {
								locConcept = graph.getFactory().createConcept(location, elementOfGRAMENE, ccCellCompartment, etIMPD);
							}
							protein.addTag(locConcept);
							
							graph.getFactory().createRelation(protein, locConcept, locIn, etIMPD);
						}
					}
				}

				locals = tissuesLocalities.getTissueLocalization(id);
				if (locals != null) {
					Iterator<String> locResults = locals.iterator();
					while (locResults.hasNext()) {
						String location = locResults.next();
						if (!location.equalsIgnoreCase("na")) {
							ONDEXConcept locConcept = cellularLocations.get(location);
							if (locConcept == null) {
								locConcept = graph.getFactory().createConcept(location, elementOfGRAMENE, ccTissue, etIMPD);
							}
							protein.addTag(locConcept);
							
							graph.getFactory().createRelation(protein, locConcept, locIn, etIMPD);
						}
					}
				}

				Set<Integer> assocs = ontoParser.getProteinAssocations(id);
				if (assocs != null) {
					Iterator<Integer> assocsIt = assocs.iterator();
					while (assocsIt.hasNext()) {
						Integer assoc = assocsIt.next();
						ONDEXConcept assocConcept = graph.getConcept(assoc);
						
						if (assocConcept != null) {
							protein.addTag(assocConcept);
							if (assocConcept.getOfType().getId().equalsIgnoreCase(MetaData.BioProc)) {
								graph.getFactory().createRelation(protein, assocConcept, hasParticipant, etIMPD);
							} else if (assocConcept.getOfType().getId().equalsIgnoreCase(MetaData.CelComp)) {
								graph.getFactory().createRelation(protein, assocConcept, locIn, etIMPD);
							} else if (assocConcept.getOfType().getId().equalsIgnoreCase(MetaData.MolFunc)) {
								graph.getFactory().createRelation(protein, assocConcept, hasFunction, etIMPD);
							}
						} else {
							System.err.println("ontology id "+assoc+" is an invalid reference");
						}
							
					}
				}
				
				HashSet<String> names = new HashSet<String>();
				
				names.add(name);
				protein.createConceptName(name, true);
				if (columns.length > 4) {
					String desc = columns[4].trim();
					if (desc.equalsIgnoreCase("Hypothetical protein")) {
						descriptions.add(desc);
					} else if (!desc.equalsIgnoreCase(name) 
							&& !name.equalsIgnoreCase("not availible") 
							&& !names.contains(desc) ) {
						names.add(desc);
						protein.createConceptName(desc, false);
					}
				}
				
				Integer cultivarId = cultivars.getCultivarId(id);
				if (cultivarId != null) {
					Cultivar cultivar = cultivars.getCultivar(cultivarId);
					String taxId = cultivar.getTaxID();
					protein.createAttribute(taxIdAttr, taxId, false);
					protein.addTag(graph.getConcept(cultivar.getOndexConcept()));
				} else {
					System.err.println("protein has no cultivar?? "+id);
				}
				
				HashSet<String> cvsPassed = new HashSet<String>();
				HashMap<String, Integer> ecs = new HashMap<String, Integer>();
				
				HashSet<Integer> refs = object2xrefs.getXrefs(id);
				if (refs != null) {
					Iterator<Integer> refIt = refs.iterator();
					while (refIt.hasNext()) {
						Integer xrefid = refIt.next();
						AccessionReference accession = xrefs.getAccessionReference(xrefid);
						if (accession == null) {
							System.err.println("xref "+xrefid+" is invalid");
							continue;
						}
						if (accession.getDescription() != null) descriptions.add(accession.getDescription());
						
						String accName = accession.getAccession();
						String dbType = accession.getDbType().trim();
						if (dbType.toUpperCase().startsWith("GRAMENE")) {
							if (dbType.equalsIgnoreCase("gramene.gene")) {
								
								Integer geneCid = null;

								if (accession.getType().equalsIgnoreCase("id")) {
									geneCid = grameneGeneParser.getGrameneGeneConceptId(Integer.parseInt(accName.toUpperCase()));
								} else {
									geneCid = grameneGeneParser.getGrameneGeneConceptId(accName.toUpperCase());
								}
								
								if (geneCid != null) {
									ONDEXConcept geneConcept = graph.getConcept(geneCid);
									if (geneConcept != null) {
										graph.getFactory().createRelation(protein, geneConcept, rtsEnBy, etIMPD);
									} else {
										System.err.println(geneCid+" geneConcept missing");
									}
								} else {
									System.err.println(geneCid+" geneAccession missing");
								}
							} else if (dbType.equalsIgnoreCase("gramene.literature") && accession.getType().equalsIgnoreCase("id") ) {
								Integer pubId = litParser.getPublicationConcept(Integer.parseInt(accName));
								if (pubId != null) {
									ONDEXConcept publication = graph.getConcept(pubId);
									graph.getFactory().createRelation(protein, publication, rtsPubIn, etIMPD);
								}
							}
							continue;
						}
						
						if (accession.getDbType().equalsIgnoreCase("ENZYME") && accession.getType().equalsIgnoreCase("acc") ) {
							ONDEXConcept enzyme = graph.getFactory().createConcept("ENZ_"+id, elementOfGRAMENE, ccEnzyme, etIMPD);
							graph.getFactory().createRelation(protein, enzyme, rtsIsA, etIMPD);
							
							if (accession.getType().equalsIgnoreCase(MetaData.ec)) {
								
								ONDEXConcept ec = null;
								Integer existingEcConcept = ecs.get(accName);
								if (existingEcConcept != null) { //this should be unusual
									ec  = graph.getConcept(existingEcConcept);
								} else {
									ec = graph.getFactory().createConcept(accName, elementOfGRAMENE, ccEC, etIMPD);
									ec.createConceptAccession(accName, elementOfEC, false);
									ecs.put(accName, ec.getId());
								}
								graph.getFactory().createRelation(enzyme, ec, rtsCatC, etIMPD);
								continue;
							}
							//System.out.println(accession.getDbType()+" "+accession.getAccession());
						}
						
						String cvName = MetaData.getMapping(dbType);
						if (cvName != null && !cvsPassed.contains(cvName)) {
							cvsPassed.add(cvName);
							DataSource dataSource = graph.getMetaData().getDataSource(cvName);
							if (dataSource == null) System.err.println(cvName+" DataSource referenced in the gramene parser MetaData is invalid");
							protein.createConceptAccession(accName, dataSource, false);
						} else {
							if (!unknownDb.contains(dbType)) {
								unknownDb.add(dbType);
								System.out.println(dbType+": database unknown");
							}
							if (!names.contains(accName)) {
								names.add(name);
								protein.createConceptName(accName, false);
							}
						}
					}
				}
			}
			input.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			ProteinSequenceParser seq = new ProteinSequenceParser(proteinIdTocid, graph);
			seq.parseSequences(dir+File.separator+GENE_PRODUCT_2_SEQ_FILE, dir+File.separator+SEQ_FILE);
	}

	
}
