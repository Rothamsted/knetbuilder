package net.sourceforge.ondex.parser.poplar.genomic;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.*;
import net.sourceforge.ondex.parser.poplar.MetaData;
import net.sourceforge.ondex.parser.poplar.Parser;
import net.sourceforge.ondex.parser.poplar.Registry;
import net.sourceforge.ondex.tools.auxfunctions.Fasta;
import net.sourceforge.ondex.tools.auxfunctions.FastaObject;
import net.sourceforge.ondex.tools.auxfunctions.TabArrayObject;
import net.sourceforge.ondex.tools.auxfunctions.TabDelimited;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the Poplar data from the JGI.
 *
 * @author Shao Chih Kuo, Keywan
 */

public class GenomicPoplarParser {

    private DataSource dataSourceJGI;
    private ConceptClass ccGene, ccProt;
    private ConceptClass ccChrom, ccScaff;
    private AttributeName anTaxID, anBegin, anStr, anEnd, anAA, anNA, anChro, anScaf;
    private EvidenceType etIMPD;
    private RelationType rt_encodedBy;

    private Registry poplarReg;

    public GenomicPoplarParser(Registry poplarReg) {
        this.poplarReg = poplarReg;
    }


    public void start(ONDEXGraph graph, ONDEXPluginArguments pa, Parser parser) throws InvalidPluginArgumentException {
        GeneralOutputEvent goe = new GeneralOutputEvent("Parsing Genomic Data...", "[Parser - start()]");
        parser.fireEventOccurred(goe);

        try {
            dataSourceJGI = parser.requireDataSource(MetaData.CV_JGI);
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
            anNA = parser.requireAttributeName(MetaData.AN_NA);
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


        System.out.println("GFF");
        // Deal with GFF first

        File inputDir = new File((String) pa.getUniqueValue(FileArgumentDefinition.INPUT_DIR));


        String gffFileName = inputDir.getAbsolutePath() + File.separator + "Poptr1_1.JamboreeModels.gff.gz";
        //LG_I	JGI	exon	10805	11064	.	-	.	name "grail3.0116000101"; transcriptId 639579

        Class<?>[] gffTypes = {String.class, String.class, String.class, Integer.class, Integer.class, Character.class, Character.class, Character.class, String.class};

        TabDelimited td = new TabDelimited(gffFileName, gffTypes);

        Pattern getNameP = Pattern.compile("name\\s+\"(.+?)\"");

        TabArrayObject tao;
        while ((tao = td.getNext()) != null) {

            int v3 = (Integer) tao.getElement(3);
            int v4 = (Integer) tao.getElement(4);

            Matcher getNameLoc = getNameP.matcher(tao.getElement(8).toString());

            getNameLoc.find();

            String geneName = getNameLoc.group(1);
            String lgName = tao.getElement(0).toString();

            Integer lg = convertLGToWellformed_old(lgName);

            if (!poplarReg.containsChromosome(lg)) {

                ONDEXConcept chrom;
                if (lgName.startsWith("scaffold")) {
                    chrom = graph.getFactory().createConcept(lgName, dataSourceJGI, ccScaff, etIMPD);
                    chrom.createConceptName(lgName, true);
                    chrom.createAttribute(anScaf, lg, false);
                } else {
                    chrom = graph.getFactory().createConcept(lgName, dataSourceJGI, ccChrom, etIMPD);
                    chrom.createConceptName(lgName, true);
                    chrom.createConceptName("Chromosome_" + lg, false);
                    chrom.createAttribute(anChro, lg, false);
                }

                chrom.createAttribute(anTaxID, Parser.POPLAR_TAX_ID, false);
                poplarReg.addChromosome(lg, chrom.getId()); // add to hashmap
            }

            ONDEXConcept chrom = graph.getConcept(poplarReg.getChromosome(lg));

            if (!poplarReg.containsGene(geneName)) {

                ONDEXConcept g = graph.getFactory().createConcept(geneName, dataSourceJGI, ccGene, etIMPD);
                g.createConceptAccession(geneName, dataSourceJGI, false); // add name
                g.createConceptName(geneName, false);
                g.addTag(chrom);
                if (lgName.startsWith("scaffold")) {
                    g.createAttribute(anScaf, lg, false);
                } else {
                    g.createAttribute(anChro, lg, false);
                }
                g.createAttribute(anTaxID, Parser.POPLAR_TAX_ID, false);
                g.createAttribute(anStr, (Character) tao.getElement(6), false);

                if (v3 < v4) {

                    g.createAttribute(anBegin, v3, false);
                    g.createAttribute(anEnd, v4, false);

                } else {

                    g.createAttribute(anBegin, v4, false);
                    g.createAttribute(anEnd, v3, false);

                }

                poplarReg.addGene(geneName, g.getId()); // add to hashmap

            }


            ONDEXConcept g = graph.getConcept(poplarReg.getGene(geneName));

            // TODO What if no start or stop is available?
            Integer beginValue = (Integer) g.getAttribute(anBegin).getValue();
            Integer endValue = (Integer) g.getAttribute(anEnd).getValue();

            if (v3 < v4 && v3 < beginValue) {

                g.getAttribute(anBegin).setValue(v3);

            } else if (v4 > v3 && v4 > endValue) {

                g.getAttribute(anEnd).setValue(v4);

            }
        }

        System.out.println("Transcripts");
        String transFN = inputDir.getAbsolutePath() + File.separator + "transcripts.Poptr1_1.JamboreeModels.fasta.gz";

        Fasta transFasta = new Fasta(transFN);
        FastaObject tfo;

        Pattern getAccessions = Pattern.compile("^>jgi\\|Poptr1_1\\|(\\d+)\\|(.+)$");

        while ((tfo = transFasta.getNext()) != null) {

            Matcher getAcc = getAccessions.matcher(tfo.getHeader());

            if (getAcc.find()) {

                String gName = getAcc.group(2);

                if (!poplarReg.containsGene(gName)) {
                    ONDEXConcept g = graph.getFactory().createConcept(gName, dataSourceJGI, ccGene, etIMPD);
                    poplarReg.addGene(gName, g.getId());
                }

                ONDEXConcept g = graph.getConcept(poplarReg.getGene(gName));
                g.createAttribute(anNA, tfo.getSeq(), false);

            } else {

                // TODO: ill formed header - throw error
            }

        }

        System.out.println("Proteins");
        String proteinFN = inputDir.getAbsolutePath() + File.separator + "proteins.Poptr1_1.JamboreeModels.fasta.gz";

        Fasta protFasta = new Fasta(proteinFN);
        FastaObject fo;

        while ((fo = protFasta.getNext()) != null) {

            Matcher getAcc = getAccessions.matcher(fo.getHeader());

            if (getAcc.find()) {

                String pName = getAcc.group(2);
                String pID = getAcc.group(1);

                if (!poplarReg.containsProtein(pName)) {
                    ONDEXConcept p = graph.getFactory().createConcept(pID, dataSourceJGI, ccProt, etIMPD);
                    p.createConceptName(pName, false);
                    p.createConceptAccession(pID, dataSourceJGI, false);
                    p.createAttribute(anTaxID, Parser.POPLAR_TAX_ID, false);
                    poplarReg.addProtein(pID, p.getId());
                }

                ONDEXConcept p = graph.getConcept(poplarReg.getProtein(pID));
                p.createAttribute(anAA, fo.getSeq(), false);

                ONDEXConcept g = graph.getConcept(poplarReg.getGene(pName));
                graph.getFactory().createRelation(p, g, rt_encodedBy, etIMPD);


            } else {

                // TODO: ill formed header - throw error

            }

        }


    }

    public static int convertLGToWellformed_old(String roman) {

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
