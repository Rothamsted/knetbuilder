package net.sourceforge.ondex.parser.fasta.ncbi;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.config.ValidatorRegistry;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.affymetrix.MetaData;
import net.sourceforge.ondex.parser.fasta.FastaBlock;
import net.sourceforge.ondex.parser.fasta.Parser;
import net.sourceforge.ondex.parser.fasta.ReadFastaFiles;
import net.sourceforge.ondex.parser.fasta.WriteFastaFile;
import org.apache.log4j.Level;

import java.util.List;
import java.util.regex.Pattern;

/**
 * NCBI FASTA FILE parser
 *
 * @author hindlem
 */
public class NCBIParser {

    private ONDEXPluginArguments pa;
    private ConceptClass ccGene;
    private ConceptClass ccProt;
    private EvidenceType etIMPD;
    private AttributeName taxIdAttr;
    private AttributeName naAttr;
    private AttributeName aaAttr;
    private DataSource dataSourceNCNM;
    private DataSource dataSourceNCNP;
    private DataSource dataSourceGenbank;
    private DataSource dataSourceNCGI;
    private DataSource dataSourceProid;
    private DataSource dataSourceUProt;


    public NCBIParser(ONDEXPluginArguments pa) {

        this.pa = pa;
    }

    public String getName() {
        return new String("NCBI_FASTA_FILE_PARSER");
    }

    public String getVersion() {
        return new String("30.05.2007");
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE, FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, true)
        };
    }

    public ONDEXPluginArguments getArguments() {
        return this.pa;
    }

    public void setArguments(ONDEXPluginArguments pa) {
        this.pa = pa;
    }

    public void setONDEXGraph(ONDEXGraph graph) throws Exception {

        ccGene = graph.getMetaData().getConceptClass(MetaData.gene);
        ccProt = graph.getMetaData().getConceptClass(MetaData.protein);
        dataSourceProid = graph.getMetaData().getDataSource(MetaData.proid);
        dataSourceNCGI = graph.getMetaData().getDataSource(MetaData.nc_gi);
        dataSourceNCNM = graph.getMetaData().getDataSource(MetaData.ncnm);
        dataSourceNCNP = graph.getMetaData().getDataSource(MetaData.ncnm);
        dataSourceGenbank = graph.getMetaData().getDataSource(MetaData.ncnm);
        dataSourceUProt = graph.getMetaData().getDataSource(MetaData.u_prot);
        etIMPD = graph.getMetaData().getEvidenceType(MetaData.IMPD);
        taxIdAttr = graph.getMetaData().getAttributeName(MetaData.taxID);
        naAttr = graph.getMetaData().getAttributeName(MetaData.nucleicAcid);
        aaAttr = graph.getMetaData().getAttributeName(MetaData.aminoAcid);

        String methodName = "setONDEXGraph(AbstractONDEXGraph graph)";

        GeneralOutputEvent so = new GeneralOutputEvent("Starting NCBI Fasta File parsing...", methodName);
        so.setLog4jLevel(Level.INFO);
        Parser.propagateEventOccurred(so);

        List<String> fileList = (List<String>) pa.getObjectValueList(FileArgumentDefinition.INPUT_FILE);

        for (String fileName : fileList) {
            WriteFastaFile writeFastaFileNCBI = new WriteFastaFileNCBI();
            ReadFastaFiles.parseFastaFile(graph, fileName, writeFastaFileNCBI);
        }

        so = new GeneralOutputEvent("Finished NCBI Fasta File parsing...", methodName);
        so.setLog4jLevel(Level.INFO);
        Parser.propagateEventOccurred(so);
    }

    private Pattern patternNotNA = Pattern.compile("[^A|T|G|C|U|a|t|g|c|u]");

    private class WriteFastaFileNCBI extends WriteFastaFile {

        @Override
        public void parseFastaBlock(ONDEXGraph graph, FastaBlock fasta) {

            String header = fasta.getHeader();
            String[] fields = header.split("\\|");

            String firstAccession;
            if (fields.length > 1) {
                firstAccession = fields[1];
            } else {
                System.err.println("No accession found can not parse this FASTA seq");
                return;
            }

            String secondAccession = null;
            if (fields.length > 3) {
                secondAccession = fields[3];
            }
            String desc = fields[fields.length - 1].trim();

            String sequence = fasta.getSequence();

            boolean isNotNA = patternNotNA.matcher(sequence).find();

            ConceptClass cc;
            AttributeName seqAt;
            DataSource dataSource;
            if (isNotNA) {
                cc = ccProt;
                seqAt = aaAttr;
                dataSource = dataSourceNCNP;
            } else {
                cc = ccGene;
                seqAt = naAttr;
                dataSource = dataSourceNCNM;
            }

            ONDEXConcept ac = graph.getFactory().createConcept(firstAccession, "", dataSource, cc, etIMPD);
            ac.setDescription(desc);
            if (ac != null) {

                if (fields[0].equalsIgnoreCase("gi")) {
                    ac.createConceptAccession(firstAccession, dataSourceNCGI, false);
                }
                if (secondAccession != null) {
                    DataSource dataSource2 = null;
                    if (fields[2].equalsIgnoreCase("gb")) {
                        dataSource2 = dataSourceGenbank;
                    } else if (fields[2].equalsIgnoreCase("ref")) {
                        dataSource2 = dataSource;
                    } else if (fields[2].equalsIgnoreCase("dbj") || fields[2].equalsIgnoreCase("emb")) {
                        dataSource2 = dataSourceProid;
                    } else if (fields[2].equalsIgnoreCase("sp")) {
                        dataSource2 = dataSourceUProt;
                    } else {
                        System.err.println("Unrecognized code :" + fields[2] + " " + header);
                    }

                    int i = secondAccession.indexOf(".");
                    if (i > 0) {
                        secondAccession = secondAccession.substring(0, i);
                    }

                    if (dataSource2 != null) ac.createConceptAccession(secondAccession, dataSource2, false);
                }

                String taxId = parseTaxID(desc);
                if (taxId != null && taxId.length() > 0) {
                    ac.createAttribute(taxIdAttr, taxId, true);
                } else {
                    System.err.println("failed to get taxid from " + desc);
                }
                ac.createAttribute(seqAt, fasta.getSequence(), false);
            } else {
                System.err.println("header not parsed succesfully: (" + header
                        + ")");
            }
        }
    }

    public String parseTaxID(String annot) {
        String species = null;
        int start = annot.indexOf('[');
        int end = annot.indexOf(']');
        if (start > -1 && end > -1) {
            species = annot.substring(start + 1, end).trim();
        }
        String taxId = null;

        if (species != null && species.length() > 0) {
            taxId = (String) ValidatorRegistry.validators.get("taxonomy").validate(species);
        }
        if (taxId == null || taxId.length() == 0) {
            String[] spaceSplit = annot.split(" +");
            String id = spaceSplit[0].trim();
            if (id.endsWith("ATH")) {
                taxId = (String) ValidatorRegistry.validators.get("taxonomy").validate("arabidopsis thaliana");
            } else if (id.endsWith("ORYSJ")) {
                taxId = (String) ValidatorRegistry.validators.get("taxonomy").validate("Oryza sativa subsp. japonica");
            } else if (id.endsWith("RAT")) {
                taxId = (String) ValidatorRegistry.validators.get("taxonomy").validate("rattus norvegicus");
            }
        }

        //hack to get a free text species in
        if (taxId == null || taxId.length() == 0 && annot.contains("Arabidopsis thaliana")) {
            taxId = (String) ValidatorRegistry.validators.get("taxonomy").validate("arabidopsis thaliana");
        }

        if (taxId == null || taxId.length() == 0) {
            System.err.println("failed to get taxid from " + annot + " predicted species==" + species);
        }
        return taxId;
    }
}