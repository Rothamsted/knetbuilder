package net.sourceforge.ondex.parser.gaf;

import java.io.FileNotFoundException;
import java.io.IOException;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.DataURL;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.gaf.tab.GAFFormatParser;
import net.sourceforge.ondex.parser.gaf.transformer.GAFTransformer;
import org.apache.log4j.Level;

/**
 * <p/>
 * Gene Annotation File Parser
 * <p/>
 * Info: http://geneontology.org/GO.format.annotation.shtml
 * Example Download:
 * ftp://ftp.ebi.ac.uk/pub/databases/GO/goa/
 * http://www.geneontology.org/GO.current.annotations.shtml
 * ftp://ftp.arabidopsis.org/home/tair/Ontologies/Plant_Ontology/
 * ftp://ftp.gramene.org/pub/gramene/CURRENT_RELEASE/data/ontology/po/
 *
 * @author hoekmanb, keywan
 */
@Status(description = "Parser for the Gene Association Format (GAF). Tested June 2011 (Keywan Hassani-Pak)", status = StatusType.EXPERIMENTAL)
@Authors(authors = {"Keywan Hassani-Pak"}, emails = {"keywan at users.sourceforge.net"})
@DatabaseTarget(name = "GOA", description = "Gene Association Files (GAF)", version = "GAF 1.0 and 2.0", url = "http://geneontology.org/GO.format.annotation.shtml")
@DataURL(name = "GAF files",
        description = "Any GAF file, e.g gene annotations to GO, PO or TO.",
        urls = {"ftp://ftp.ebi.ac.uk/pub/databases/GO/goa/",
                "http://www.geneontology.org/GO.current.annotations.shtml",
                "ftp://ftp.gramene.org/pub/gramene/CURRENT_RELEASE/data/ontology/",
                "ftp://ftp.arabidopsis.org/home/tair/Ontologies/Plant_Ontology/"})
@Custodians(custodians = {"Keywan Hassani-pak"}, emails = {"keywan at users.sourceforge.net"})
public class Parser extends ONDEXParser {

    public String getName() {
        return new String("GeneAnnotationFormat");
    }

    public String getVersion() {
        return new String("06/2011");
    }

    @Override
    public String getId() {
        return "gaf";
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                        FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false),
                new FileArgumentDefinition(ArgumentNames.TRANSLATION_ARG,
                        ArgumentNames.TRANSLATION_DESC, false, true, false, false),
        };

    }

    public void start() throws InvalidPluginArgumentException {
        // get importdata dir
        String file = (String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE);
        
        GeneralOutputEvent so = new GeneralOutputEvent("Parsing GAF file: "+file, "GAF.start()");
        fireEventOccurred(so);
        
        GAFTransformer gafTransformer = new GAFTransformer(graph, args);
        writeGAFFile(graph, file, gafTransformer);
        
        so = new GeneralOutputEvent("Finished parsing GAF file", "");
        fireEventOccurred(so);
    }

    public void writeGAFFile(ONDEXGraph graph, String fileToParse, GAFTransformer gafTransformer) { 
    	
        GAFFormatParser goAnnotationParser = new GAFFormatParser();

        try {
            goAnnotationParser.getFileContent(fileToParse, gafTransformer);
        } catch (FileNotFoundException e) {
            DataFileMissingEvent dfm = new DataFileMissingEvent(
                    "Could not find flat file " + fileToParse + "("
                            + e.getLocalizedMessage() + ")", this.getClass().toString());
            dfm.setLog4jLevel(Level.ERROR);
            fireEventOccurred(dfm);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Override
    public String[] requiresValidators() {
        return new String[]{"cvregex"};
    }

}
