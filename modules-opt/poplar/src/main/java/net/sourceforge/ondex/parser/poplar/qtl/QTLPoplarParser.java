package net.sourceforge.ondex.parser.poplar.qtl;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.*;
import net.sourceforge.ondex.parser.poplar.MetaData;
import net.sourceforge.ondex.parser.poplar.Parser;
import net.sourceforge.ondex.parser.poplar.Registry;
import net.sourceforge.ondex.parser.poplar.genomic.GenomicPoplarParserV2;

import java.io.*;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Parses the PoplarQTLs.txt file from Steve Hanley.
 * Creates a concept (CC: QTL) for each QTL and links it to the referenced journal.
 * A Post-processing step links the QTLs to the underlying poplar genes (Context: QTL).
 *
 * @author keywan
 */
public class QTLPoplarParser {

    private ConceptClass ccQTL, ccChromosome, ccPublication;
    private DataSource dataSourceJGI, dataSourceNLM;
    private EvidenceType etIMPD;
    private AttributeName anTaxID, anBegin, anEnd, anChromosome, anYear;
    private RelationType pub_in;

    private HashMap<String, Integer> experimentToId = new HashMap<String, Integer>();
    private Registry poplarReg;

    public QTLPoplarParser(Registry poplarReg) {
        this.poplarReg = poplarReg;
    }

    public void start(ONDEXGraph graph, ONDEXPluginArguments pa, Parser parser) throws InvalidPluginArgumentException {
        GeneralOutputEvent goe = new GeneralOutputEvent("Parsing QTL Data...", "[Parser - start()]");
        parser.fireEventOccurred(goe);

        try {
            ccQTL = parser.requireConceptClass(MetaData.CC_QTL);
            ccChromosome = parser.requireConceptClass(MetaData.CHROMOSOME);
            ccPublication = parser.requireConceptClass(MetaData.CC_PUBLICATION);
            pub_in = parser.requireRelationType(MetaData.RT_PUBIN);
            dataSourceJGI = parser.requireDataSource(MetaData.CV_PHYTOZOME);
            dataSourceNLM = parser.requireDataSource(MetaData.CV_NLM);
            etIMPD = parser.requireEvidenceType(MetaData.ET_IMPD);
            anTaxID = parser.requireAttributeName(MetaData.AN_TAXID);
            anChromosome = parser.requireAttributeName(MetaData.CHROMOSOME);
            anBegin = parser.requireAttributeName(MetaData.AN_BEGIN);
            anEnd = parser.requireAttributeName(MetaData.AN_END);
            anYear = parser.requireAttributeName(MetaData.AN_YEAR);
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

        File inputDir = new File((String) pa.getUniqueValue(FileArgumentDefinition.INPUT_DIR));


        String filename = inputDir.getAbsolutePath() + File.separator + "PoplarQTLs.txt";

        BufferedReader input;


        try {
            if (filename.endsWith(".gz")) {
                GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(filename));
                input = new BufferedReader(new InputStreamReader(gzip));
            } else {
                input = new BufferedReader(new FileReader(filename));
            }

            String inputLine = input.readLine();
            inputLine = input.readLine(); //skip header line
            while (inputLine != null) {

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
                Integer cId = poplarReg.getChromosome(chrom);

                //each line is a single QTL
                ONDEXConcept qtl = graph.getFactory().createConcept(trait, dataSourceJGI, ccQTL, etIMPD);
                qtl.createConceptName(trait, true);
                qtl.createAttribute(anChromosome, chrom, false);
                qtl.createAttribute(anTaxID, Parser.POPLAR_TAX_ID, false);
                qtl.createAttribute(anBegin, start, false);
                qtl.createAttribute(anEnd, end, false);
                if (cId != null)
                    qtl.addTag(graph.getConcept(cId));


                if (!experimentToId.containsKey(pmid)) {
                    ONDEXConcept experiment = graph.getFactory().createConcept("PMID:" + pmid, dataSourceJGI, ccPublication, etIMPD);
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
            parser.fireEventOccurred(new DataFileMissingEvent(fnfe
                    .getMessage(), "[Parser - start()]"));
        } catch (IOException ioe) {
            parser.fireEventOccurred(new DataFileErrorEvent(ioe
                    .getMessage(), "[Parser - start()]"));
        }

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
//			System.out.println("QTL mapping: "+gCount+" genes mapped to "+qtl.getId()+" - "+qtl.getConceptName().getName());
        }
    }

}
