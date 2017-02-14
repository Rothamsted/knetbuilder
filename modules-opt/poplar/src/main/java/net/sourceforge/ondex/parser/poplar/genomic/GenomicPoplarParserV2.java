package net.sourceforge.ondex.parser.poplar.genomic;

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
import net.sourceforge.ondex.parser.poplar.MetaData;
import net.sourceforge.ondex.parser.poplar.Parser;
import net.sourceforge.ondex.parser.poplar.Registry;
import net.sourceforge.ondex.tools.auxfunctions.Fasta;
import net.sourceforge.ondex.tools.auxfunctions.FastaObject;
import net.sourceforge.ondex.tools.auxfunctions.TabArrayObject;
import net.sourceforge.ondex.tools.auxfunctions.TabDelimited;

/**
 * Parser for poplar data (FASTA, GFF3) provided by phytozome
 * TODO: Make it more generic so it can parse any phytozome species
 * 
 * @author pakk
 *
 */
public class GenomicPoplarParserV2 {

	private DataSource dataSourcePHYTOZOME;
	private ConceptClass ccGene, ccProt;
	private ConceptClass ccChrom, ccScaff;
	private AttributeName anTaxID, anBegin, anStr, anEnd, anAA, anChro, anScaf;
	private EvidenceType etIMPD;
	private RelationType rt_encodedBy;

	private ONDEXGraph og;

	private Registry poplarReg;
	//ID=POPTR_0001s00200;
	private static final String idPattern = "ID=(.+?)(;|\\n)";

	public GenomicPoplarParserV2(Registry poplarReg) {
		this.poplarReg = poplarReg;
	}

	public void start(ONDEXGraph graph, ONDEXPluginArguments pa, Parser parser) throws InvalidPluginArgumentException {

		this.og = graph;

		GeneralOutputEvent goe = new GeneralOutputEvent("Parsing Genomic Data...", "[Parser - start()]");
		parser.fireEventOccurred(goe);

		// fetch metadata
		try {
			dataSourcePHYTOZOME = parser.requireDataSource(MetaData.CV_PHYTOZOME);
			ccGene = parser.requireConceptClass(MetaData.CC_GENE);
			ccProt = parser.requireConceptClass(MetaData.CC_PROTEIN);
			ccChrom = parser.requireConceptClass(MetaData.CHROMOSOME);
			ccScaff = parser.requireConceptClass(MetaData.SCAFFOLD);
			etIMPD = parser.requireEvidenceType(MetaData.ET_IMPD);
			anTaxID = parser.requireAttributeName(MetaData.AN_TAXID);
			anBegin = parser.requireAttributeName(MetaData.AN_BEGIN);
			anEnd = parser.requireAttributeName(MetaData.AN_END);
			anStr = parser.requireAttributeName(MetaData.AN_STR);
			anAA = parser.requireAttributeName(MetaData.AN_AA);
			anChro = parser.requireAttributeName(MetaData.CHROMOSOME);
			anScaf = parser.requireAttributeName(MetaData.SCAFFOLD);
			rt_encodedBy = parser.requireRelationType(MetaData.RT_ENCODEDBY);
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

		File inputDir = new File((String) pa.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

		
		// parsing genes
		String geneFastaFile = inputDir.getAbsolutePath() + File.separator + "Ptrichocarpa_156_cds.fa.gz";
		parseGenes(graph, geneFastaFile);

		// parsing proteins
		String proteinFastaFile = inputDir.getAbsolutePath() + File.separator + "Ptrichocarpa_156_peptide.fa.gz";
		parseProteins(graph, proteinFastaFile);

		// parsing positional information from GFF
		String gffFileName = inputDir.getAbsolutePath() + File.separator + "Ptrichocarpa_156_gene.gff3.gz";
		parseGFF(graph, gffFileName);
		
		// parsing poplar v1.1 accessions that were mapped to v2.0
        String accMappingFile = inputDir.getAbsolutePath() + File.separator + "Ptrichocarpa_156_synonym.txt.gz";
        parseSynonyms(accMappingFile);

	}
	
	/**
	 * Fasta file parser for gene information
	 * 
	 * @param graph
	 * @param fileName
	 */
	public void parseGenes(ONDEXGraph graph, String fileName){
		// parsing transcripts
		System.out.println("Parsing genes from FASTA file...");
		int count_genes_fasta = 0;
		Fasta fasta = new Fasta(fileName);
		FastaObject fo;

		// >POPTR_0001s20530.1 
		Pattern p = Pattern.compile(">(POPTR_.+)\\.(\\d+)");
		while ((fo = fasta.getNext()) != null) {
			Matcher m = p.matcher(fo.getHeader());
			if (m.find()) {
				String gName = m.group(1);
				if (!poplarReg.containsGene(gName)) {
					count_genes_fasta++;
					ONDEXConcept g = graph.getFactory().createConcept(gName, dataSourcePHYTOZOME, ccGene, etIMPD);
					g.createConceptAccession(gName, dataSourcePHYTOZOME, false);
					g.createConceptName(gName, true);
					g.createAttribute(anTaxID, Parser.POPLAR_TAX_ID, false);
					// g.createGDS(anNA, tfo.getSeq(), false);
					poplarReg.addGene(gName, g.getId());
				}
			} else {
				// TODO: ill formed header - throw error
				System.out.println(p.toString()+ " does not match...");
			}
		}
		System.out.println("Number of genes found in FASTA file: "+count_genes_fasta);          

	}
	
	/**
	 * Fasta file parser for proteins
	 * 
	 * @param graph
	 * @param proteinFastaFile
	 */
	public void parseProteins(ONDEXGraph graph, String proteinFastaFile){
		System.out.println("Parsing proteins from FASTA file...");
		int count_proteins_fasta = 0;
		Fasta fasta = new Fasta(proteinFastaFile);
		FastaObject fo;

		//	>POPTR_0003s04670.2 
		Pattern p = Pattern.compile(">(POPTR_.+)\\.(\\d+)");
		while ((fo = fasta.getNext()) != null) {
			Matcher m = p.matcher(fo.getHeader());
			if (m.find()) {
				String geneId = m.group(1);
				String splice = m.group(2);
				String pAcc = geneId + "." + splice;
				if (!poplarReg.containsProtein(pAcc)) {
					count_proteins_fasta++;
					ONDEXConcept c = graph.getFactory().createConcept(pAcc, dataSourcePHYTOZOME, ccProt, etIMPD);
					c.createConceptName(geneId, true);
					c.createConceptAccession(pAcc, dataSourcePHYTOZOME, false);
					c.createAttribute(anTaxID, Parser.POPLAR_TAX_ID, false);
					c.createAttribute(anAA, fo.getSeq(), false);
					poplarReg.addProtein(pAcc, c.getId());
				}

				ONDEXConcept protein = graph.getConcept(poplarReg.getProtein(pAcc));
				ONDEXConcept gene = graph.getConcept(poplarReg.getGene(geneId));
				graph.getFactory().createRelation(protein, gene, rt_encodedBy, etIMPD);

			} else {
				// TODO: ill formed header - throw error
				System.out.println(p.toString()+ " does not match...");
			}
		}
		System.out.println("Number of proteins found in FASTA file: "+count_proteins_fasta);        

	}
	
	/**
	 * GFF3 parser and positional enrichment of genes
	 * 
	 * @param graph
	 * @param fileName
	 */
	public void parseGFF(ONDEXGraph graph, String fileName){
		System.out.println("Parsing positional information for genes from GFF file...");
		// scaffold_1	Ptrichocarpav2_0	gene	12632	13612	.	+	.	ID=POPTR_0001s00200;Name=POPTR_0001s00200
		Class<?>[] gffTypes = {String.class, String.class, String.class, Integer.class, Integer.class, Character.class, Character.class, Character.class, String.class};
		TabDelimited td = new TabDelimited(fileName, gffTypes);

		TabArrayObject tao;
		//header line
		td.getNext();
		int count_genes_gff = 0;
		while ((tao = td.getNext()) != null) {

			// create if not exist chromosomes
			String lgName = (String) tao.getElement(0);
			// tax id added in getOrCreateChromosome
			int lg = getOrCreateChromosome(lgName);

			ONDEXConcept chrom = graph.getConcept(poplarReg.getChromosome(lg));

			// running through features
			// gene, mRNA, exon, 5'-UTR, CDS, 3'-UTR
			String type = (String) tao.getElement(2);

			if (type.equals("gene")) {

				count_genes_gff++;

				String geneId = extract((String) tao.getElement(8), idPattern);

				ONDEXConcept gene = graph.getConcept(poplarReg.getGene(geneId));

				// add context
				gene.addTag(chrom);

				// add strand
				gene.createAttribute(anStr, (Character) tao.getElement(6), false);

				if (lg > 20) {
					gene.createAttribute(anScaf, lg, false);
				} else {
					gene.createAttribute(anChro, lg, false);
				}

				int newstart = (Integer) tao.getElement(3);
				int newend = (Integer) tao.getElement(4);

				if (newstart < newend) {

					gene.createAttribute(anBegin, newstart, false);
					gene.createAttribute(anEnd, newend, false);

				} else {

					// else it's on the reverse strand and we invert the start and stops
					gene.createAttribute(anBegin, newend, false);
					gene.createAttribute(anEnd, newstart, false);

				}

				// not sure what to do with the rest of these...
				// we don't use them for anything at the moment
			} else if (type.equals("mRNA")) {

			} else if (type.equals("exon")) {

			} else if (type.equals("5'-UTR")) {

			} else if (type.equals("CDS")) {

			} else if (type.equals("3'-UTR")) {

			} else {
				System.err.println("Unknown type - " + type);
			}
		}
		System.out.println("Number of genes found in GFF file: "+count_genes_gff);
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
        			String[] synonyms = col[1].split("\t");
        			count_acc++;
        			for (String synonym : synonyms) {
        				// create synonyms for proteins and genes
        				Integer pID = poplarReg.getProtein(acc);
        				ONDEXConcept p = og.getConcept(pID);
        				p.createConceptName(synonym, false);

        				Integer gID = poplarReg.getGene(acc.substring(0, acc.lastIndexOf(".")));
        				ONDEXConcept g = og.getConcept(gID);
        				g.createConceptName(synonym, false);
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
			System.err.println("No pattern found? " + a);
			System.err.println("Pattern search type: " + pattern);
		}
		return b;
	}

	public int getOrCreateGeneID(String geneId) {

		if (poplarReg.containsGene(geneId)) {

			return poplarReg.getGene(geneId);

		} else {

			ONDEXConcept gene = og.getFactory().createConcept(geneId, dataSourcePHYTOZOME, ccGene, etIMPD);
			gene.createConceptAccession(geneId, dataSourcePHYTOZOME, false);
			gene.createConceptName(geneId, false);

			Integer ondexGeneId = gene.getId();
			gene.createAttribute(anTaxID, Parser.POPLAR_TAX_ID, false);
			poplarReg.addGene(geneId, ondexGeneId);
			return ondexGeneId;
		}

	}

	public int getOrCreateChromosome(String lgName) {

		Integer lg = Integer.parseInt(lgName.substring(9));

		if (!poplarReg.containsChromosome(lg)) {
			ONDEXConcept chrom;
			if (lg > 19) {
				chrom = og.getFactory().createConcept(lgName, dataSourcePHYTOZOME, ccScaff, etIMPD);
				chrom.createConceptName(lgName, true);
				chrom.createAttribute(anScaf, lg, false);
			} else {
				chrom = og.getFactory().createConcept(lgName, dataSourcePHYTOZOME, ccChrom, etIMPD);
				chrom.createConceptName(lgName, true);
				chrom.createConceptName("Chromosome_" + lg, false);
				chrom.createAttribute(anChro, lg, false);
			}

			chrom.createAttribute(anTaxID, Parser.POPLAR_TAX_ID, false);
			poplarReg.addChromosome(lg, chrom.getId()); // add to hashmap
		}

		return lg;

	}
	
    public static int convertLGToWellformed(String roman) {

        int linkageGroup;

        if (roman.equals("LG_I")) linkageGroup = 1;
        else if (roman.equals("LG_II")) linkageGroup = 2;
        else if (roman.equals("LG_III")) linkageGroup = 3;
        else if (roman.equals("LG_IV")) linkageGroup = 4;
        else if (roman.equals("LG_V")) linkageGroup = 5;
        else if (roman.equals("LG_VI")) linkageGroup = 6;
        else if (roman.equals("LG_VII")) linkageGroup = 7;
        else if (roman.equals("LG_VIII")) linkageGroup = 8;
        else if (roman.equals("LG_IX")) linkageGroup = 9;
        else if (roman.equals("LG_X")) linkageGroup = 10;
        else if (roman.equals("LG_XI")) linkageGroup = 11;
        else if (roman.equals("LG_XII")) linkageGroup = 12;
        else if (roman.equals("LG_XIII")) linkageGroup = 13;
        else if (roman.equals("LG_XIV")) linkageGroup = 14;
        else if (roman.equals("LG_XV")) linkageGroup = 15;
        else if (roman.equals("LG_XVI")) linkageGroup = 16;
        else if (roman.equals("LG_XVII")) linkageGroup = 17;
        else if (roman.equals("LG_XVIII")) linkageGroup = 18;
        else if (roman.equals("LG_XIX")) linkageGroup = 19;
        else {
            //e.g. scaffold_10001
            linkageGroup = Integer.parseInt(roman.split("_")[1]);
        }

        return linkageGroup;
    }

}
