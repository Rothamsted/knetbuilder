package net.sourceforge.ondex.parser.fasta;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.fasta.args.ArgumentNames;
import net.sourceforge.ondex.parser.fasta.args.FastaFileType;
import net.sourceforge.ondex.parser.fasta.custom.CustomParser;
import net.sourceforge.ondex.parser.fasta.ensembl.EnsemblParser;
import net.sourceforge.ondex.parser.fasta.gramene.GrameneParser;
import net.sourceforge.ondex.parser.fasta.ncbi.NCBIParser;
import net.sourceforge.ondex.parser.fasta.simple.SimpleParser;

@Status(description = "Tested March 2010 (Artem Lysenko)", status = StatusType.STABLE)
@Authors(authors = {"Matthew Hindle", "Berend Hoekman"}, emails = {"matthew_hindle at users.sourceforge.net", ""})
@Custodians(custodians = {"Keywan Hassani-pak"}, emails = {"keywan at users.sourceforge.net"})
public class Parser extends ONDEXParser
{

    private static Parser instance;

    public Parser() {
        instance = this;
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {

        return new ArgumentDefinition[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE, FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false),
                new FastaFileType(ArgumentNames.FASTA_FILE_TYPE_ARG, ArgumentNames.FASTA_FILE_TYPE_ARG_DESC, true, FastaFileType.SIMPLE, false),
                new StringArgumentDefinition(ArgumentNames.TAXID_TO_USE_ARG, ArgumentNames.FASTA_FILE_TYPE_ARG_DESC, false, null, false),
                new StringArgumentDefinition(ArgumentNames.CC_OF_SEQ_ARG, ArgumentNames.CC_OF_SEQ_ARG_DESC, false, null, false),
                new StringArgumentDefinition(ArgumentNames.CV_OF_SEQ_ARG, ArgumentNames.CV_OF_SEQ_ARG_DESC, false, "unknown", false),
                new StringArgumentDefinition(ArgumentNames.TO_ACC_ARG, ArgumentNames.TO_ACC_ARG_DESC, false, null, true),
                new StringArgumentDefinition(ArgumentNames.TYPE_OF_SEQ_ARG, ArgumentNames.TYPE_OF_SEQ_ARG_DESC, false, "AA", false),
                new StringArgumentDefinition(ArgumentNames.SEPARATOR_ARG, ArgumentNames.SEPARATOR_ARG_DESC, false, null, false),
                new StringArgumentDefinition(ArgumentNames.ACCESSION_ARG, ArgumentNames.ACCESSION_ARG_DESC, false, null, false)
        };
    }

    public String getName() {
        return "FASTA file parser";
    }

    public String getVersion() {
        return "03-10-2007";
    }

    @Override
    public String getId() {
        return "FASTA";
    }

    public void start() throws Exception {

        String fastaFileType = (String) args.getUniqueValue(ArgumentNames.FASTA_FILE_TYPE_ARG);

        if (fastaFileType.equalsIgnoreCase(FastaFileType.NCBI)) {
            NCBIParser parser = new NCBIParser(args);
            parser.setONDEXGraph(graph);
        } else if (fastaFileType.equalsIgnoreCase(FastaFileType.SIMPLE)) {
            SimpleParser parser = new SimpleParser(args);
            parser.setONDEXGraph(graph);
        } else if (fastaFileType.equalsIgnoreCase(FastaFileType.GRAMENE)) {
            GrameneParser parser = new GrameneParser(args);
            parser.setONDEXGraph(graph);
        } else if (fastaFileType.equalsIgnoreCase(FastaFileType.CUSTOM)) {
            CustomParser parser = new CustomParser(args);
            parser.setONDEXGraph(graph);
        } else if (fastaFileType.equalsIgnoreCase(FastaFileType.ENSEMBL)) {
            EnsemblParser parser = new EnsemblParser(args);
            parser.setONDEXGraph(graph);
        }else {
            throw new InvalidPluginArgumentException("FASTA file type argument is unknown " + fastaFileType);
        }
    }

    public static void propagateEventOccurred(EventType et) {
        if (instance != null)
            instance.fireEventOccurred(et);
    }

    @Override
    public String[] requiresValidators() {
        return new String[]{"cvregex", "taxonomy"};
    }

    /**
     * Convenience method for outputing the current method name in a dynamic way
     *
     * @return the calling method name
     */
    public static String getCurrentMethodName() {
        Exception e = new Exception();
        StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
        String name = trace.getMethodName();
        String className = trace.getClassName();
        int line = trace.getLineNumber();
        return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line + "]";
    }
}
