package net.sourceforge.ondex.parser.poplarqtl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.exception.type.RelationTypeMissingException;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.poplar.MetaData;
import net.sourceforge.ondex.parser.poplar.genomic.GenomicPoplarParserV2;

import org.apache.log4j.Level;

/**
 * Parser for Poplar QTL tab delimitted file
 *
 * @author keywan
 */
public class Parser extends ONDEXParser {
	
    private ConceptClass ccQTL, ccChromosome, ccPublication;
    private DataSource dataSourceUnknown, dataSourceNLM;
    private EvidenceType etIMPD;
    private AttributeName anTaxID, anBegin, anEnd, anChromosome, anYear;
    private RelationType pub_in;

    private HashMap<String, Integer> experimentToId = new HashMap<String, Integer>();

    public static final String POPLAR_TAX_ID = "3694";



    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                        FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false)
        };
    }

    @Override
    public String getName() {
        return "Poplar QTL";
    }

    @Override
    public String getVersion() {
        return "07/04/2011";
    }

    @Override
    public String getId() {
        return "poplarqtl";
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {
        GeneralOutputEvent goe = new GeneralOutputEvent("Start parsing Poplar QTLs...", "[Parser - start()]");
        goe.setLog4jLevel(Level.INFO);
        fireEventOccurred(goe);
        
        try {
            ccQTL = requireConceptClass(MetaData.CC_QTL);
            ccChromosome = requireConceptClass(MetaData.CHROMOSOME);
            ccPublication = requireConceptClass(MetaData.CC_PUBLICATION);
            pub_in = requireRelationType(MetaData.RT_PUBIN);
            dataSourceUnknown = requireDataSource("unknown");
            dataSourceNLM = requireDataSource(MetaData.CV_NLM);
            etIMPD = requireEvidenceType(MetaData.ET_IMPD);
            anTaxID = requireAttributeName(MetaData.AN_TAXID);
            anChromosome = requireAttributeName(MetaData.CHROMOSOME);
            anBegin = requireAttributeName(MetaData.AN_BEGIN);
            anEnd = requireAttributeName(MetaData.AN_END);
            anYear = requireAttributeName(MetaData.AN_YEAR);
        } catch (ConceptClassMissingException e) {
            e.printStackTrace();
        } catch (DataSourceMissingException e) {
            e.printStackTrace();
        } catch (EvidenceTypeMissingException e) {
            e.printStackTrace();
        } catch (AttributeNameMissingException e) {
            e.printStackTrace();
        } catch (RelationTypeMissingException e) {
            e.printStackTrace();
        }

        File file = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE));

        BufferedReader input;
        int qtlCount = 0;
        try {
            if (file.getName().endsWith(".gz")) {
                GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
                input = new BufferedReader(new InputStreamReader(gzip));
            } else {
                input = new BufferedReader(new FileReader(file));
            }
            
            String inputLine = input.readLine();
            inputLine = input.readLine(); //skip header line
            while (inputLine != null) {
            	qtlCount++;
                String[] col = inputLine.split("\t");
                String trait = col[0];
//				String lod = col[1];
//				String var = col[2];
//				String family = col[3];
//				String plant = col[4];
                String linkageGroup = "LG_" + col[5];
//				String chromNum = col[6];
//				String genus = col[7];
//				String privacy = col[8];
//				String marker1 = col[9];
//				String marker2 = col[10];
                Integer start = Integer.parseInt(col[11]);
                Integer end = Integer.parseInt(col[12]);
//				String lengths = col[13];
                String reference = col[14];
                String pmid = col[15];
                Integer year = Integer.parseInt(col[16]);

                Integer chrom = GenomicPoplarParserV2.convertLGToWellformed(linkageGroup);
//                Integer cId = poplarReg.getChromosome(chrom);

                //each line is a single QTL
                ONDEXConcept qtl = graph.getFactory().createConcept(trait, dataSourceUnknown, ccQTL, etIMPD);
                qtl.createConceptName(trait, true);
                qtl.createAttribute(anChromosome, chrom, false);
                qtl.createAttribute(anTaxID, Parser.POPLAR_TAX_ID, false);
                qtl.createAttribute(anBegin, start, false);
                qtl.createAttribute(anEnd, end, false);
//                if (cId != null)
//                    qtl.addTag(graph.getConcept(cId));


                if (!experimentToId.containsKey(pmid)) {
                    ONDEXConcept experiment = graph.getFactory().createConcept("PMID:" + pmid, dataSourceUnknown, ccPublication, etIMPD);
                    experiment.createConceptName(reference, true);
                    experiment.createConceptAccession(pmid, dataSourceNLM, false);
                    experiment.createAttribute(anYear, year, false);
                    experimentToId.put(pmid, experiment.getId());
                }
                ONDEXConcept experiment = graph.getConcept(experimentToId.get(pmid));
                ONDEXRelation pubIn = graph.getFactory().createRelation(qtl, experiment, pub_in, etIMPD);

                experiment.addTag(experiment);
                qtl.addTag(experiment);
                pubIn.addTag(experiment);


                inputLine = input.readLine();

            }
            input.close();

        } catch (FileNotFoundException fnfe) {
            fireEventOccurred(new DataFileMissingEvent(fnfe
                    .getMessage(), "[Parser - start()]"));
        } catch (IOException ioe) {
            fireEventOccurred(new DataFileErrorEvent(ioe
                    .getMessage(), "[Parser - start()]"));
        }
        
        goe = new GeneralOutputEvent("Successfully parsed "+qtlCount+" Poplar QTL.", "[Parser - start()]");
        fireEventOccurred(goe);
        
        goe = new GeneralOutputEvent("Mapping QTL to underlying genes...", "[Parser - start()]");
        fireEventOccurred(goe);

        //QTLs are mapped to underlying genes
        Set<ONDEXConcept> qtls = graph.getConceptsOfConceptClass(ccQTL);
        Set<ONDEXConcept> chroms = graph.getConceptsOfConceptClass(ccChromosome);
        Set<ONDEXConcept> others = BitSetFunctions.or(qtls, chroms);
        Set<ONDEXConcept> genes = BitSetFunctions.copy(graph.getConceptsOfAttributeName(anChromosome));
        genes.removeAll(others);
        // Has a runtime of O(n^2). Should be improved if necessary.
        for (ONDEXConcept qtl : qtls) {
            String qChrom = qtl.getAttribute(anChromosome).getValue().toString();
            Integer qBeg = (Integer) qtl.getAttribute(anBegin).getValue();
            Integer qEnd = (Integer) qtl.getAttribute(anEnd).getValue();
            int gCount = 0;
            for(ONDEXConcept gene: genes) {
                String gChrom = gene.getAttribute(anChromosome).getValue().toString();

                //if gene not on same chromosome as QTL
                if (!gChrom.equalsIgnoreCase(qChrom))
                    continue;

                // check if either beg or end of the gene is within the QTL region
                Integer gBeg = (Integer) gene.getAttribute(anBegin).getValue();
                Integer gEnd = (Integer) gene.getAttribute(anEnd).getValue();

                // ------|--------------|------  QTL (qBeg, qEnd)
                //    |-----> <--|   |------>    GENES (gBeg, gEnd)
                if ((gBeg > qBeg && gBeg < qEnd) || (gEnd > qBeg && gEnd < qEnd)) {
                    gene.addTag(qtl);
                    gCount++;
                }
            }
			System.out.println("QTL mapping: "+gCount+" genes mapped to "+qtl.getId()+" - "+qtl.getConceptName().getName());
        }



        goe = new GeneralOutputEvent("Successfully mapped Poplar QTL.", "[Parser - start()]");
        fireEventOccurred(goe);
    }



}
