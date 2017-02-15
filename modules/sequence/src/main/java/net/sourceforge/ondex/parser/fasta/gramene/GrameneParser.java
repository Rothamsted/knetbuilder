package net.sourceforge.ondex.parser.fasta.gramene;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.fasta.FastaBlock;
import net.sourceforge.ondex.parser.fasta.ReadFastaFiles;
import net.sourceforge.ondex.parser.fasta.WriteFastaFile;
import net.sourceforge.ondex.parser.fasta.args.ArgumentNames;
import org.apache.log4j.Level;

import java.util.List;
import java.util.regex.Pattern;

/**
 * NCBI FASTA FILE parser
 *
 * @author hindlem
 */
public class GrameneParser {

    private ONDEXPluginArguments pa;

    private ConceptClass ccGene;
    private ConceptClass ccProt;
    private EvidenceType etIMPD;
    private AttributeName taxIdAttr;
    private AttributeName naAttr;
    private AttributeName aaAttr;
    private DataSource dataSourceGramene;

    public GrameneParser(ONDEXPluginArguments pa) {

        this.pa = pa;
    }

    public void setONDEXGraph(ONDEXGraph graph) throws Exception {

        ccGene = graph.getMetaData().getConceptClass(MetaData.gene);
        ccProt = graph.getMetaData().getConceptClass(MetaData.protein);
        dataSourceGramene = graph.getMetaData().getDataSource(MetaData.gramene);
        etIMPD = graph.getMetaData().getEvidenceType(MetaData.IMPD);
        taxIdAttr = graph.getMetaData().getAttributeName(MetaData.taxID);
        naAttr = graph.getMetaData().getAttributeName(MetaData.nucleicAcid);
        aaAttr = graph.getMetaData().getAttributeName(MetaData.aminoAcid);

        GeneralOutputEvent so = new GeneralOutputEvent("Starting Gramene Fasta File parsing...", "setONDEXGraph(AbstractONDEXGraph graph)");
        so.setLog4jLevel(Level.INFO);
        ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(so);

        List<String> fileList = (List<String>) pa.getObjectValueList(FileArgumentDefinition.INPUT_FILE);

        for (String fileName : fileList) {
            WriteFastaFile writeFastaFileSimple = new WriteFastaFileSimple();
            ReadFastaFiles.parseFastaFile(graph, fileName, writeFastaFileSimple);
        }

        so = new GeneralOutputEvent("Finished Gramene Fasta File parsing...", "setONDEXGraph(AbstractONDEXGraph graph)");
        so.setLog4jLevel(Level.INFO);
        ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(so);
    }

    //TODO: some DNA sequences may contain some N for an arbitry nucleotide
    private Pattern patternNotNA = Pattern.compile("[^A|T|G|C|U|a|t|g|c|u]");
    private Pattern barPatter = Pattern.compile("[|]");

    private class WriteFastaFileSimple extends WriteFastaFile {

        @Override
        public void parseFastaBlock(ONDEXGraph graph, FastaBlock fasta) throws InvalidPluginArgumentException {

            //Q2PT50 | TREMBL | 5-enolpyruvylshikimate 3-phosphate synthase | Lolium rigidum | NCBI_TaxID=89674

            // digest header
            String header = fasta.getHeader().trim();

            String[] values = barPatter.split(header);

            String accession = values[0].trim();
            String accessionType = values[1].trim();
            String description = values[2].trim();
            String tax = values[values.length - 1];

            int eqindex = tax.indexOf("=");
            if (eqindex > -1) {
                tax = tax.substring(eqindex + 1).trim();
            } else {
                System.err.println("unknown tax id " + tax);
            }

            String sequence = fasta.getSequence();

            DataSource acDataSource = null;

            if (accessionType.equalsIgnoreCase("TREMBL") || accessionType.equalsIgnoreCase("SWISSPROT")) {
                acDataSource = graph.getMetaData().getDataSource(MetaData.u_protkb);
            }

            if (acDataSource == null) {
                System.err.println("unknow db " + accessionType);
            }

            boolean isNotNA = patternNotNA.matcher(sequence).find();

            ConceptClass ccType = ccProt;
            AttributeName seqAt;
            if (isNotNA) {
                ccType = ccProt;
                seqAt = aaAttr;
            } else {
                ccType = ccGene;
                seqAt = naAttr;
            }

            //overwrite SeqType (seqAt) if parameter SeqType is set and valid
            String attr_name = (String) pa.getUniqueValue(ArgumentNames.TYPE_OF_SEQ_ARG);
            if (attr_name != null) {
                if (graph.getMetaData().getAttributeName(attr_name) != null) {
                    seqAt = graph.getMetaData().getAttributeName(attr_name);
                } else {
                    AttributeNameMissingEvent ge = new AttributeNameMissingEvent("Missing: " + attr_name, "parseFastaBlock(AbstractONDEXGraph graph, FastaBlock fasta)");
                    ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(ge);
                }
            }

            //overwrite CC if parameter CC is set and valid

            ONDEXConcept ac = graph.getFactory().createConcept(accession, description, dataSourceGramene, ccType, etIMPD);

            if (acDataSource != null) ac.createConceptAccession(accession, acDataSource, false);
            if (tax != null) ac.createAttribute(taxIdAttr, tax, true);
            ac.createAttribute(seqAt, sequence, false);
        }
    }
}