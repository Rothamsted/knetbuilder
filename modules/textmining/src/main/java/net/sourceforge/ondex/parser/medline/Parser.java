package net.sourceforge.ondex.parser.medline;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.DataURL;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.*;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.medline.args.ArgumentNames;
import org.apache.log4j.Level;

import java.util.Hashtable;

/**
 * @author rwinnenb
 * @modified keywan
 */
@Status(description = "This parser is obsolete. Please use the new medline parser instead. Parses MEDLINE/PubMed XML files or uses EFetch web-service to load publications. Tested June 2011 (Keywan Hassani-Pak)", status = StatusType.EXPERIMENTAL)
@Authors(authors = {"Keywan Hassani-Pak"}, emails = {"keywan at users.sourceforge.net"})
@DatabaseTarget(name = "MEDLINE", description = "Biomedical Literature", version = "MEDLINE 2011", url = "http://www.ncbi.nlm.nih.gov/pubmed/")
@DataURL(name = "MEDLINE XML",
        description = "Any MEDLINE XML file. Use either PubMed to search for keywords and save results to XML or use Parser's integrated web-service functionality to fetch cited publications online.",
        urls = {"http://www.ncbi.nlm.nih.gov/pubmed?term=hassani-pak",
                "http://www.ncbi.nlm.nih.gov/pubmed?term=Arabidopsis"})
@Custodians(custodians = {"Keywan Hassani-pak"}, emails = {"keywan at users.sourceforge.net"})
public class Parser extends ONDEXParser
{

//	public static AbstractONDEXGraph graph;
    public Hashtable<String, Integer> writtenConcepts = new Hashtable<String, Integer>();
    private static Parser instance;

    public Parser() {
        instance = this;
    }

    public String getName() {
        return new String("Medline parser (obsolete)");
    }

    public String getVersion() {
        return new String("01.08.2006");
    }

    @Override
    public String getId() {
        return "medline_obs";
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {

        FileArgumentDefinition XMLfileArg = new FileArgumentDefinition(ArgumentNames.XMLFILES_ARG, ArgumentNames.XMLFILES_DESC, false, true, false, true);
        FileArgumentDefinition PubMedFileArg = new FileArgumentDefinition(ArgumentNames.PUBMEDFILE_ARG, ArgumentNames.PUBMEDFILE_DESC, false, true, false, true);
        RangeArgumentDefinition<Integer> pmidArgs = new RangeArgumentDefinition<Integer>(ArgumentNames.IDLIST_ARG, ArgumentNames.IDLIST_DESC, false, null, 0, Integer.MAX_VALUE, Integer.class);

        return new ArgumentDefinition[]{
                new StringArgumentDefinition(ArgumentNames.PREFIX_ARG, ArgumentNames.PREFIX_DESC, false, "medline09n", false),
                new StringArgumentDefinition(ArgumentNames.COMPRESSION_ARG, ArgumentNames.COMPRESSION_DESC, false, "gz", false),
                pmidArgs,
                XMLfileArg,
                PubMedFileArg,
                new BooleanArgumentDefinition(ArgumentNames.IMP_ONLY_CITED_PUB_ARG, ArgumentNames.IMP_ONLY_CITED_PUB_DESC, false, false),
                new RangeArgumentDefinition<Integer>(ArgumentNames.LOWERXMLBOUNDARY_ARG, ArgumentNames.LOWERXMLBOUNDARY_DESC, false, 300, 0, Integer.MAX_VALUE, Integer.class),
                new RangeArgumentDefinition<Integer>(ArgumentNames.UPPERXMLBOUNDARY_ARG, ArgumentNames.UPPERXMLBOUNDARY_DESC, false, 563, 0, Integer.MAX_VALUE, Integer.class),
                new BooleanArgumentDefinition(ArgumentNames.USE_EFETCH_WS, ArgumentNames.USE_EFETCH_WS_DESC, false, false),
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR, "Directory with MEDLINE XML files.", false, false, true, false)
        };
    }

    public static void propagateEventOccurred(EventType et) {
        if (instance != null)
            instance.fireEventOccurred(et);
    }

    @Override
    public String[] requiresValidators() {
        return null;
    }

    public void start() throws Exception {
        GeneralOutputEvent so = new GeneralOutputEvent("Starting MEDLINE importing...", Parser.getCurrentMethodName());
        so.setLog4jLevel(Level.INFO);
        fireEventOccurred(so);

        try {
            new Extractor().parse(args, graph, this);
            GeneralOutputEvent so1 = new GeneralOutputEvent("MEDLINE importing finished with success!", Parser.getCurrentMethodName());
            so1.setLog4jLevel(Level.INFO);
            fireEventOccurred(so1);
        } catch (Exception e) {
            GeneralOutputEvent so1 = new GeneralOutputEvent("MEDLINE importing not finished! " + e.getMessage(), Parser.getCurrentMethodName());
            so1.setLog4jLevel(Level.INFO);
            fireEventOccurred(so1);
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Convenience method for outputting the current method name in a dynamic way
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
