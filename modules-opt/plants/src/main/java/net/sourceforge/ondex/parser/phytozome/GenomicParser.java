package net.sourceforge.ondex.parser.phytozome;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.exception.type.RelationTypeMissingException;
import net.sourceforge.ondex.tools.auxfunctions.Fasta;
import net.sourceforge.ondex.tools.auxfunctions.FastaObject;
import net.sourceforge.ondex.tools.auxfunctions.TabArrayObject;
import net.sourceforge.ondex.tools.auxfunctions.TabDelimited;

/**
 * Parser for Phytozome annotation folder (FASTA, GFF3, Synonyms)
 * Tested for phytozome 9.1 release
 * 
 * @author keywan
 *
 */
public class GenomicParser {

	private DataSource dataSourcePHYTOZOME, dataSourceENSEMBL, accDataSource;
	private ConceptClass ccGene, ccProt, ccCDS;
	private ConceptClass ccChrom, ccScaff;
	private AttributeName anTaxID, anBegin, anStr, anEnd, anAA, anNA, anChro, anScaf;
	private EvidenceType etIMPD;
	private RelationType rt_encodes;

	private ONDEXGraph graph;

	private Registry speciesReg;
	private String taxID;
	private Integer numChromosomes;
	private Boolean isPrefSynonym;
	private static final String gffGeneID = "ID=(.+?)(;|\\n)";
	private static final Pattern patChroNum = Pattern.compile("(\\d+$)");

	public GenomicParser(Registry poplarReg) {
		this.speciesReg = poplarReg;
	}

	public void start(ONDEXGraph graph, ONDEXPluginArguments pa, Parser parser) throws InvalidPluginArgumentException {

		this.graph = graph;

		GeneralOutputEvent goe = new GeneralOutputEvent("Parsing Genomic Data...", "[Parser - start()]");
		parser.fireEventOccurred(goe);

		// fetch metadata
		try {
			dataSourcePHYTOZOME = parser.requireDataSource(MetaData.DS_PHYTOZOME);
			dataSourceENSEMBL = parser.requireDataSource(MetaData.DS_ENSEMBL);
			ccGene = parser.requireConceptClass(MetaData.CC_GENE);
			ccProt = parser.requireConceptClass(MetaData.CC_PROTEIN);
			ccCDS = parser.requireConceptClass(MetaData.CC_CDS);
			ccChrom = parser.requireConceptClass(MetaData.CHROMOSOME);
			ccScaff = parser.requireConceptClass(MetaData.SCAFFOLD);
			etIMPD = parser.requireEvidenceType(MetaData.ET_IMPD);
			anTaxID = parser.requireAttributeName(MetaData.AN_TAXID);
			anBegin = parser.requireAttributeName(MetaData.AN_BEGIN);
			anEnd = parser.requireAttributeName(MetaData.AN_END);
			anStr = parser.requireAttributeName(MetaData.AN_STR);
			anAA = parser.requireAttributeName(MetaData.AN_AA);
			anNA = parser.requireAttributeName(MetaData.AN_NA);
			anChro = parser.requireAttributeName(MetaData.CHROMOSOME);
			anScaf = parser.requireAttributeName(MetaData.SCAFFOLD);
			rt_encodes = parser.requireRelationType(MetaData.RT_ENCODES);
		} catch (DataSourceMissingException e) {
			e.printStackTrace();
		} catch (ConceptClassMissingException e) {
			e.printStackTrace();
		} catch (EvidenceTypeMissingException e) {
			e.printStackTrace();
		} catch (AttributeNameMissingException e) {
			e.printStackTrace();
		} catch (RelationTypeMissingException e) {
			e.printStackTrace();
		}
		
		taxID = (String) pa.getUniqueValue(ArgumentNames.TAXID_ARG);
		numChromosomes = Integer.parseInt(pa.getUniqueValue(ArgumentNames.NUM_CHROMOSOMES_ARG).toString());
		isPrefSynonym = (Boolean) pa.getUniqueValue(ArgumentNames.SYNONYMS_PREF_ARG);
		String dsName = (String) pa.getUniqueValue(ArgumentNames.ACC_DATASOURCE_ARG);
		if(graph.getMetaData().checkDataSource(dsName)){
			accDataSource = graph.getMetaData().getDataSource(dsName);
		}else{
			accDataSource = dataSourceENSEMBL;
		}
		
		File inputDir = new File((String) pa.getUniqueValue(FileArgumentDefinition.INPUT_DIR));
		
		for(File f : inputDir.listFiles()){
			if(f.getAbsolutePath().contains("gene.gff3")){
				parseGFF(f.getAbsolutePath());
			}
		}
		
		for(File f : inputDir.listFiles()){
			if(f.getAbsolutePath().contains("cds.fa")){
				parseCDS(f.getAbsolutePath());
			}
		}
		
		for(File f : inputDir.listFiles()){
			if(f.getAbsolutePath().contains("protein.fa")){
				parseProteins(f.getAbsolutePath());
			}
		}
		
		for(File f : inputDir.listFiles()){
			if(f.getAbsolutePath().contains("synonym.txt")){
				parseSynonyms(f.getAbsolutePath());
			}
		}
	}
	
	
	/**
	 * GFF3 parser
	 * 
	 * Creates Gene and Chromosome/Scaffold concepts
	 * 
	 * @param graph
	 * @param fileName
	 */
	public void parseGFF(String fileName){
		System.out.println("Parsing positional information for genes from GFF file...");
		// scaffold_1	Ptrichocarpav2_0	gene	12632	13612	.	+	.	ID=POPTR_0001s00200;Name=POPTR_0001s00200
		Class<?>[] gffTypes = {String.class, String.class, String.class, Integer.class, Integer.class, Character.class, Character.class, Character.class, String.class};
		TabDelimited td = new TabDelimited(fileName, gffTypes);

		TabArrayObject tao;
		//header line
		td.getNext();
		int geneCount = 0;
		int chroCount = 0;
		int scafCount = 0;
		int genesWithNoChro = 0;

		while ((tao = td.getNext()) != null) {

			// running through features
			// gene, mRNA, exon, 5'-UTR, CDS, 3'-UTR
			String type = (String) tao.getElement(2);

			if (!type.equals("gene")) {
				continue;
			}	

			// ID=LOC_Os01g01010;Name=LOC_Os01g01010;
			String gName = extract((String) tao.getElement(8), gffGeneID);
			int gBeg = (Integer) tao.getElement(3);
			int gEnd = (Integer) tao.getElement(4);
			char gStrand = (Character) tao.getElement(6);

			if (!speciesReg.containsGene(gName)) {
				geneCount++;
				ONDEXConcept g = graph.getFactory().createConcept(gName, dataSourcePHYTOZOME, ccGene, etIMPD);
				g.createConceptAccession(gName, accDataSource, false);
				g.createConceptName(gName, true);
				g.createAttribute(anTaxID, taxID, false);
				speciesReg.addGene(gName, g.getId());
				if (gBeg < gEnd) {
					g.createAttribute(anBegin, gBeg, false);
					g.createAttribute(anEnd, gEnd, false);

				} else {
					// else it's on the reverse strand and we invert the start and stops
					g.createAttribute(anBegin, gEnd, false);
					g.createAttribute(anEnd, gBeg, false);

				}
				// add strand
				g.createAttribute(anStr, gStrand, false);
			}

			ONDEXConcept gene = graph.getConcept(speciesReg.getGene(gName));

			String lgName = (String) tao.getElement(0);
			
			// convert linkage information into chromosome number
			
			Matcher match = patChroNum.matcher(lgName);
			int chromNum = 0;
			if (match.find()) {
				chromNum = Integer.parseInt(match.group(1));
			}else{
//				System.out.println("Can not extract chromosome number from: "+lgName);
			}
			
			// extraction of chromosome number was successful
			if(!speciesReg.containsChromosome(lgName) && chromNum != 0){
				
				// create chromosome/scaffold concepts;					
				ONDEXConcept chrom;
				if (chromNum > numChromosomes) {
					scafCount++;
					chrom = graph.getFactory().createConcept(lgName, dataSourcePHYTOZOME, ccScaff, etIMPD);
					chrom.createAttribute(anScaf, chromNum, false);
				} else {
					chroCount++;
					chrom = graph.getFactory().createConcept(lgName, dataSourcePHYTOZOME, ccChrom, etIMPD);
					chrom.createConceptName("Chromosome_" + chromNum, false);
					chrom.createAttribute(anChro, chromNum, false);
				}
				chrom.createConceptName(lgName, true);
				chrom.createAttribute(anTaxID, taxID, false);
				speciesReg.addChromosome(lgName, chrom.getId()); // add to hashmap
				
			}
				
			if(chromNum != 0){
				ONDEXConcept chrom = graph.getConcept(speciesReg.getChromosome(lgName));
				// add location to genes as attribute					
				if (chromNum > numChromosomes) {
					gene.createAttribute(anScaf, chromNum, false);
				} else {
					gene.createAttribute(anChro, chromNum, false);
				}
				// add location to genes as context
				gene.addTag(chrom);
			}else{
				genesWithNoChro++;
//				System.out.println("Error :: Gene: "+gName+" was not mapped to LinkageGroup: "+lgName);
			}
		}

		System.out.println("Number of genes found in GFF file: "+geneCount);
		System.out.println("Number of chromosomes found in GFF file: "+chroCount);
		System.out.println("Number of scaffolds found in GFF file: "+scafCount);
		System.out.println("Number of genes with no linkage group: "+genesWithNoChro);
	}	
	
	
	/**
	 * Fasta file parser for gene information
	 * 
	 * @param graph
	 * @param fileName
	 */
	public void parseCDS(String fileName){
		// parsing transcripts
		System.out.println("Parsing CDS from FASTA file...");
		int count_cds_fasta = 0;
		int count_no_locus = 0;
		Fasta fasta = new Fasta(fileName);
		FastaObject fo;

		while ((fo = fasta.getNext()) != null) {
			// >POPTR_1446s00200.1|PACid:18205974
			// In rice also possible: >13114.m00474|PACid:16897097
			String acc = fo.getHeader().split("\\|")[0].replaceFirst(">", "");
			String locus = acc.substring(0, acc.lastIndexOf('.'));
			
			String seq = fo.getSeq().trim();
			if(seq.endsWith("*")){
				seq = seq.substring(0, seq.length()-1);
			}

			if (!speciesReg.containsCDS(acc)) {
				count_cds_fasta++;
				ONDEXConcept c = graph.getFactory().createConcept(acc, dataSourcePHYTOZOME, ccCDS, etIMPD);
				c.createConceptName(locus, true);
				c.createConceptAccession(acc, accDataSource, false);
				c.createAttribute(anTaxID, taxID, false);
				c.createAttribute(anNA, seq, false);
				speciesReg.addCDS(acc, c.getId());
			}
			
			ONDEXConcept gene = null;
			if (speciesReg.containsGene(acc)) {
				gene = graph.getConcept(speciesReg.getGene(acc));
			}else if (speciesReg.containsGene(locus)) {
				gene = graph.getConcept(speciesReg.getGene(locus));
			}else{
				count_no_locus++;
			}

			ONDEXConcept cds = graph.getConcept(speciesReg.getCDS(acc));
			if(gene != null){
				graph.getFactory().createRelation(gene, cds, rt_encodes, etIMPD);
			}
		} 
		
		System.out.println("Number of CDS found in FASTA file: "+count_cds_fasta);
		System.out.println("Number of CDS without locus information: "+count_no_locus);
	}
	
	
	/**
	 * Fasta file parser for proteins
	 * 
	 * @param graph
	 * @param proteinFastaFile
	 */
	public void parseProteins(String proteinFastaFile){
		System.out.println("Parsing proteins from FASTA file...");
		int count_proteins_fasta = 0;
		int count_no_locus = 0;
		Fasta fasta = new Fasta(proteinFastaFile);
		FastaObject fo;

		while ((fo = fasta.getNext()) != null) {
			String acc = fo.getHeader().split("\\|")[0].replaceFirst(">", "");
			String locus = acc.substring(0, acc.lastIndexOf('.'));
			
			String seq = fo.getSeq().trim();
			if(seq.endsWith("*")){
				seq = seq.substring(0, seq.length()-1);
			}

			if (!speciesReg.containsProtein(acc)) {
				count_proteins_fasta++;
				ONDEXConcept c = graph.getFactory().createConcept(acc, dataSourcePHYTOZOME, ccProt, etIMPD);
				c.createConceptName(locus, true);
				c.createConceptAccession(acc, accDataSource, false);
				c.createAttribute(anTaxID, taxID, false);
				c.createAttribute(anAA, seq, false);
				speciesReg.addProtein(acc, c.getId());
			}
			
			ONDEXConcept protein = graph.getConcept(speciesReg.getProtein(acc));
			ONDEXConcept cds = null;
			ONDEXConcept gene = null;
			
			if (speciesReg.containsGene(acc)) {
				gene = graph.getConcept(speciesReg.getGene(acc));
			}else if (speciesReg.containsGene(locus)) {
				gene = graph.getConcept(speciesReg.getGene(locus));
			}else{
				count_no_locus++;
			}
			
			if (speciesReg.containsCDS(acc)) {
				cds = graph.getConcept(speciesReg.getCDS(acc));
			}	

			if(gene != null){
				graph.getFactory().createRelation(gene, protein, rt_encodes, etIMPD);
			}
			
			if(cds != null){
				graph.getFactory().createRelation(cds, protein, rt_encodes, etIMPD);
			}
		} 

		System.out.println("Number of proteins found in FASTA file: "+count_proteins_fasta);        
		System.out.println("Number of proteins without locus information: "+count_no_locus);
	}
	
	
	/**
	 * Parser for gene mapping file provided by Phytozome/JGI
	 * 
	 * @param fileName
	 */
    public void parseSynonyms(String fileName){
        	System.out.println("Parse synonym file...");
        	BufferedReader input;
        	try {

        		if(fileName.endsWith(".gz")) {
        			GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(fileName));
        			input = new BufferedReader(new InputStreamReader(gzip));
        		}
        		else{		
        			input = new BufferedReader(new FileReader(fileName));
        		}

        		String inputLine = input.readLine();
        		int count_acc = 0;
        		while (inputLine != null) {
        			String[] col = inputLine.split("\t");
        			String acc = col[0];
        			String locus = acc.substring(0, acc.lastIndexOf('.'));
        			
//        			String[] synonyms = col[1].split("\t");
        			count_acc++;
        			int x = 0;
        			for (String synonym : col) {
        				x++;
        				if(x == 1) 
        					continue;
        				// create synonyms for proteins and genes
        				Integer pID = speciesReg.getProtein(acc);
        				if(pID != null){
        					ONDEXConcept p = graph.getConcept(pID);
        					p.createConceptName(synonym, isPrefSynonym);
        				}	
        				
        				Integer cID = speciesReg.getCDS(acc);
        				if(cID != null){
        					ONDEXConcept c = graph.getConcept(cID);
        					c.createConceptName(synonym, isPrefSynonym);
        				}
        				
        				ONDEXConcept gene = null;
        				if (speciesReg.containsGene(acc)) {
        					gene = graph.getConcept(speciesReg.getGene(acc));
        				}else if (speciesReg.containsGene(locus)) {
        					gene = graph.getConcept(speciesReg.getGene(locus));
        				}
        				
        				if(gene != null){
        					gene.createConceptName(synonym, isPrefSynonym);
        				}
        				
        				
        			}

        			inputLine = input.readLine();
        		}
        		input.close();
        		System.out.println("Number of genes/proteins having synonym information "+count_acc);
        	} catch (FileNotFoundException e) {
        		e.printStackTrace();
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
    }

	/**
	 *  This function extracts a single string from a regex pattern
	 *
	 */
	public static String extract(String a, String pattern) {
		Pattern pat = Pattern.compile(pattern);
		Matcher match = pat.matcher(a);
		String b = null;
		if (match.find()) {
			b = match.group(1);
		} else {
			System.out.println("No pattern "+pattern+" found in " + a);
			
		}
		return b;
	}

}
