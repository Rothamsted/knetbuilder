package net.sourceforge.ondex.parser.fasta.ensembl;

import java.util.HashMap;
import java.util.List;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.fasta.FastaBlock;
import net.sourceforge.ondex.parser.fasta.ReadFastaFiles;
import net.sourceforge.ondex.parser.fasta.WriteFastaFile;
import net.sourceforge.ondex.parser.fasta.args.ArgumentNames;

import org.apache.log4j.Level;

/**
 * Ensembl FASTA FILE parser, input a peptide file
 * creates protein, gene, scaffold and chromosome concepts
 * gene concepts carry positional information and are tagged with chromosome/scaffold
 * protein concepts contain the AA sequence
 * 
 * FIXME: if chromosome is X,Y,MT it needs to be converted into a number
 * atm it converts X,Y,M to chromosome 19,20,21 (hack for pig data)
 * 
 * FIXME: scaffold id in ensembl are strings that can not easily be converted to
 * integer which is required for the Ondex attribute 'Scaffold', therefore
 * a new attribute 'Scaffold2' is created of type String
 *
 * @author keywan
 */
public class EnsemblParser {

    private ONDEXPluginArguments pa;

    private ConceptClass ccGene;
    private ConceptClass ccProt;
    private ConceptClass ccChro;
    private ConceptClass ccScaffold;
    private EvidenceType etIMPD;
    private AttributeName attTaxId;
    private AttributeName attAA;
    private AttributeName attChromosome;
    private AttributeName attScaffold;
    private AttributeName attBegin;
    private AttributeName attEnd;
    private RelationType rtEncodes;
    private DataSource dataSourceEnsembl;
    private String TAXID;
    private String attSeqType;

    public EnsemblParser(ONDEXPluginArguments pa) {

        this.pa = pa;
    }

    public void setONDEXGraph(ONDEXGraph graph) throws Exception {

        ccGene = graph.getMetaData().getConceptClass(MetaData.ccGene);
        ccProt = graph.getMetaData().getConceptClass(MetaData.ccProtein);
        ccChro = graph.getMetaData().getConceptClass(MetaData.ccChromosome);
        ccScaffold = graph.getMetaData().getConceptClass(MetaData.ccScaffold);
        dataSourceEnsembl = graph.getMetaData().getDataSource(MetaData.cvEnsembl);
        etIMPD = graph.getMetaData().getEvidenceType(MetaData.etIMPD);
        attTaxId = graph.getMetaData().getAttributeName(MetaData.attTaxId);
        attAA = graph.getMetaData().getAttributeName(MetaData.attAA);
        attChromosome = graph.getMetaData().getAttributeName(MetaData.attChromosome);
        //TODO: change to metadata Scaffold (type is Integer!)
        attScaffold = graph.getMetaData().getFactory().createAttributeName(MetaData.attScaffold, String.class);
        attBegin = graph.getMetaData().getAttributeName(MetaData.attBegin);
        attEnd = graph.getMetaData().getAttributeName(MetaData.attEnd);
        rtEncodes = graph.getMetaData().getRelationType(MetaData.rtEncodes);
        
        TAXID = (String) pa.getUniqueValue(ArgumentNames.TAXID_TO_USE_ARG);
        attSeqType = (String) pa.getUniqueValue(ArgumentNames.TYPE_OF_SEQ_ARG);
        if (attSeqType != null) {
            AttributeNameMissingEvent ge = new AttributeNameMissingEvent("Missing: " + attSeqType, "Fasta Ensembl Parser");
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(ge);
        }

        GeneralOutputEvent so = new GeneralOutputEvent("Starting Ensembl Fasta File parsing...", "setONDEXGraph(AbstractONDEXGraph graph)");
        so.setLog4jLevel(Level.INFO);
        ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(so);

        List<String> fileList = (List<String>) pa.getObjectValueList(FileArgumentDefinition.INPUT_FILE);

        for (String fileName : fileList) {
            WriteFastaFile writeFastaFileSimple = new WriteFastaFileSimple();
            ReadFastaFiles.parseFastaFile(graph, fileName, writeFastaFileSimple);
        }

        so = new GeneralOutputEvent("Finished Ensembl Fasta File parsing...", "setONDEXGraph(AbstractONDEXGraph graph)");
        so.setLog4jLevel(Level.INFO);
        ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(so);
    }


    private class WriteFastaFileSimple extends WriteFastaFile {
    	
    	HashMap<Integer,ONDEXConcept> chrom2concept = new HashMap<Integer, ONDEXConcept>();
    	HashMap<String,ONDEXConcept> scaffold2concept = new HashMap<String, ONDEXConcept>();
    	HashMap<String,ONDEXConcept> gene2concept = new HashMap<String, ONDEXConcept>();

        @Override
        public void parseFastaBlock(ONDEXGraph graph, FastaBlock fasta) throws InvalidPluginArgumentException {

        	//chromosome line
        	//>ENSSSCP00000022608 pep:novel chromosome:Sscrofa10.2:9:4542369:4543289:-1 gene:ENSSSCG00000025880 transcript:ENSSSCT00000027616 gene_biotype:protein_coding transcript_biotype:protein_coding
        	//scaffold line
        	//>ENSSSCP00000027906 pep:known scaffold:Sscrofa10.2:GL894571.1:2663:4960:1 gene:ENSSSCG00000023032 transcript:ENSSSCT00000024662 gene_biotype:protein_coding transcript_biotype:protein_coding
        	
            // digest header
            String header = fasta.getHeader().trim();

            String[] col = header.split(" ");

            String accession = col[0].trim();
            String protType = col[1].trim();
            String loci = col[2].trim();
            String geneAcc = col[3].split(":")[1].trim();
//            String transcriptAcc = col[4].split(":")[1].trim();

            String[] lociCol = loci.split(":");
            Integer chromNum = null;
            String chromLabel = "";
            String scaffoldNum = null;
            if(lociCol[0].equals("chromosome")){
            	chromLabel = lociCol[2];
            	if(lociCol[2].equals("X")){
            		chromNum = 19;
            	}
            	else if(lociCol[2].equals("Y")){
            		chromNum = 20;
            	}
            	else if(lociCol[2].equals("MT")){
            		chromNum = 21;
            	}	
            	else{
            		chromNum = Integer.parseInt(lociCol[2]);
            	}
            }else if(lociCol[0].equals("scaffold")){
            	scaffoldNum = lociCol[2];
            }
            
            
            Integer start = Integer.parseInt(lociCol[3]);
            Integer end = Integer.parseInt(lociCol[4]);
            String version = lociCol[1];
            String sequence = fasta.getSequence();
            
            if(chromNum != null && !chrom2concept.containsKey(chromNum)){
            	ONDEXConcept conChrom = graph.getFactory().createConcept("Chromosome:"+chromLabel, dataSourceEnsembl, ccChro, etIMPD);
            	conChrom.createConceptName("Chromosome "+chromLabel, true);
            	conChrom.createAttribute(attChromosome, chromNum, false);
            	chrom2concept.put(chromNum, conChrom);
            }
            
            if(scaffoldNum != null && !scaffold2concept.containsKey(scaffoldNum)){
            	ONDEXConcept conScaf = graph.getFactory().createConcept("Scaffold:"+scaffoldNum, dataSourceEnsembl, ccScaffold, etIMPD);
            	conScaf.createConceptName(scaffoldNum, true);
            	conScaf.createAttribute(attScaffold, scaffoldNum, false);
            	scaffold2concept.put(scaffoldNum, conScaf);
            }
            
            if(!gene2concept.containsKey(geneAcc)){
            	ONDEXConcept c = graph.getFactory().createConcept(geneAcc, protType.split(":")[1], dataSourceEnsembl, ccGene, etIMPD);
                if(TAXID != null) c.createAttribute(attTaxId, TAXID, true);
                c.setDescription(version);
                c.createConceptAccession(geneAcc, dataSourceEnsembl, false);
                c.createConceptName(geneAcc, true);
                if(chromNum != null)
                	c.createAttribute(attChromosome, chromNum, false);
                else if(scaffoldNum != null)
                	c.createAttribute(attScaffold, scaffoldNum, false);
                
	            c.createAttribute(attBegin, start, false);
	            c.createAttribute(attEnd, end, false);
            	gene2concept.put(geneAcc, c);
            }

            ONDEXConcept conGene = gene2concept.get(geneAcc);
            ONDEXConcept conProt = graph.getFactory().createConcept(accession, protType, dataSourceEnsembl, ccProt, etIMPD);

            //add tag
            if(chromNum != null)
            	conGene.addTag(chrom2concept.get(chromNum));
            else if(scaffoldNum != null)
            	conGene.addTag(scaffold2concept.get(scaffoldNum));
            
            if(TAXID != null) conProt.createAttribute(attTaxId, TAXID, true);
            conProt.createConceptAccession(accession, dataSourceEnsembl, false);
            conProt.createConceptName(accession, true);
            conProt.createAttribute(attAA, sequence, false);
            conProt.setDescription(version);
            
            graph.getFactory().createRelation(conGene, conProt, rtEncodes, etIMPD);
            

            
        }
    }
}