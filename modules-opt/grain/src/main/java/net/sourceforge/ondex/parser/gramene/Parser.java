package net.sourceforge.ondex.parser.gramene;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.*;
import net.sourceforge.ondex.parser.ONDEXParser;
import org.apache.log4j.Level;

import java.io.File;

import static net.sourceforge.ondex.parser.gramene.ArgumentNames.PARSE_LITERATURE_ARG;
import static net.sourceforge.ondex.parser.gramene.ArgumentNames.PARSE_LITERATURE_ARG_DESC;

/**
 * Gramene Parser
 * <p/>
 * Info: http://www.gramene.org/
 * ftp: ftp://ftp.gramene.org/pub/gramene/CURRENT_RELEASE/data/database_dump/mysql-dumps/
 * (downloading during daytime is slow at night the speed is a lot better)
 *
 * @author hoekmanb, hindlem
 */
@Custodians(custodians = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
public class Parser extends ONDEXParser
{

    private static Parser instance = null;

    /**
     * Parser for GO.
     */
    public Parser() {

        instance = this;
    }

    public String getName() {
        return new String("Gramene");
    }

    public String getVersion() {
        return new String("10.01.2008");
    }

    @Override
    public String getId() {
        return "gramene";
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition[]{
                new BooleanArgumentDefinition(PARSE_LITERATURE_ARG, PARSE_LITERATURE_ARG_DESC, false, true),
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR,
                        FileArgumentDefinition.INPUT_DIR_DESC, true, true, true, false),

        };
    }

    private static final String GENEONTOASSOC_FILE = "gene_ontology_association.txt";
    private static final String PROTEINONTOASSOC_FILE = "association.txt";

    public void start() throws InvalidPluginArgumentException {
        GeneralOutputEvent so = new GeneralOutputEvent("Starting gramene parsing...", "");
        so.setLog4jLevel(Level.INFO);
        fireEventOccurred(so);

//		get importdata dir
        String inFilesDir = (String) args.getUniqueValue(FileArgumentDefinition.INPUT_DIR);

        String genesDir = null;
        String proteinsDir = null;
        String literatureDir = null;

        //this adds a little flexibility of naming for versions e.g. genes7
        String[] files = new File(inFilesDir).list();
        for (String file : files) {

            if (new File(inFilesDir + File.separator + file).isDirectory()) {

                //look for genes dir
                if (file.equalsIgnoreCase("gene") || file.equalsIgnoreCase("genes")) {
                    genesDir = file;
                    continue;
                } else if (genesDir == null && file.toLowerCase().startsWith("gene")) {
                    genesDir = file;
                }

                //look for proteins dir
                if (file.equalsIgnoreCase("protein") || file.equalsIgnoreCase("proteins")) {
                    proteinsDir = file;
                    continue;
                } else if (proteinsDir == null && file.toLowerCase().startsWith("protein")) {
                    proteinsDir = file;
                }

                //look for lit dir
                if (file.equalsIgnoreCase("literature")) {
                    literatureDir = file;
                    continue;
                } else if (literatureDir == null && file.toLowerCase().startsWith("literature")) {
                    literatureDir = file;
                }

            }
        }

        if (genesDir == null) {
            fireEventOccurred(new DataFileMissingEvent("The Genes directory is missing from the specified gramene data directory: dir should begin " + inFilesDir + File.separator + "gene* ", getCurrentMethodName()));
            return;
        } else {
            fireEventOccurred(new GeneralOutputEvent("Genes directory: " + genesDir, getCurrentMethodName()));
        }

        if (proteinsDir == null) {
            fireEventOccurred(new DataFileMissingEvent("The Proteins directory is missing from the specified gramene data directory: dir should begin " + inFilesDir + File.separator + "protein* ", getCurrentMethodName()));
            return;
        } else {
            fireEventOccurred(new GeneralOutputEvent("Proteins directory: " + proteinsDir, getCurrentMethodName()));
        }

        if (literatureDir == null) {
            fireEventOccurred(new DataFileMissingEvent("The Literature directory is missing from the specified gramene data directory: dir should begin " + inFilesDir + File.separator + "literature* ", getCurrentMethodName()));
        } else {
            fireEventOccurred(new GeneralOutputEvent("Literature directory: " + literatureDir, getCurrentMethodName()));
        }


        LiteratureDBParser litParser = new LiteratureDBParser(graph);
        if (args.getUniqueValue(PARSE_LITERATURE_ARG) == null ||
                (Boolean) args.getUniqueValue(PARSE_LITERATURE_ARG) == true)
            litParser.parseLiterature(inFilesDir + File.separator + literatureDir);

        System.out.println("parsing ontologies");
        OntologyParser ontoParser = new OntologyParser(
                inFilesDir + File.separator + genesDir + File.separator + GENEONTOASSOC_FILE,
                inFilesDir + File.separator + proteinsDir + File.separator + PROTEINONTOASSOC_FILE,
                graph);
        ontoParser.parseOntologies();

        System.out.println("parsing genes");
//		Parse the Genes
        GenesDBParser geneDBParser = new GenesDBParser(graph, ontoParser);
        geneDBParser.parseGenes(inFilesDir + File.separator + genesDir);

        System.out.println("parsing proteins");
        //Parser the proteins
        ProteinDBParser proteinDBParser = new ProteinDBParser(graph, geneDBParser, ontoParser, litParser);
        proteinDBParser.parseProteins(inFilesDir + File.separator + proteinsDir);

        //Create a "temporaly" dir. (inputDir/tab/
        //File temp = new File();

        so = new GeneralOutputEvent("Gramene parsing finished!", "");
        so.setLog4jLevel(Level.INFO);
        fireEventOccurred(so);
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    public static void propagateEventOccurred(EventType e) {
        if (instance != null) {
            instance.fireEventOccurred(e);
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

    public static void main(String[] args) {
        System.out.println(getCurrentMethodName());
    }

    /**
     * Provides a convenience method for checking metadata is created
     *
     * @param md   metadata object
     * @param name the desired object name
     * @return
     */
    public static boolean checkCreated(Object md, String name) {
        if (md == null) {
            if (md instanceof DataSource) {
                propagateEventOccurred(new DataSourceMissingEvent(name, getCurrentMethodName()));
            } else if (md instanceof EvidenceType) {
                propagateEventOccurred(new EvidenceTypeMissingEvent(name, getCurrentMethodName()));
            } else if (md instanceof ConceptClass) {
                propagateEventOccurred(new ConceptClassMissingEvent(name, getCurrentMethodName()));
            } else if (md instanceof RelationType) {
                propagateEventOccurred(new RelationTypeMissingEvent(name, getCurrentMethodName()));
            } else if (md instanceof RelationType) {
                propagateEventOccurred(new RelationTypeMissingEvent(name, getCurrentMethodName()));
            } else if (md instanceof AttributeName) {
                propagateEventOccurred(new AttributeNameMissingEvent(name, getCurrentMethodName()));
            } else {
                propagateEventOccurred(new GeneralOutputEvent(name + " is missing in MetaData", getCurrentMethodName()));
            }
            return false;
        } else {
            return true;
        }
    }

    public static RelationType getRelationType(String name, ONDEXGraph aog) {
        RelationType rts = aog.getMetaData()
                .getRelationType(name);
        if (rts != null) {
            return rts;
        }
        RelationType rt = aog.getMetaData().getRelationType(name);
        if (checkCreated(rt, name)) {
            return aog.getMetaData().getFactory().createRelationType(rt.getId(), rt);
        } else {
            System.out.println("Entity Parser Missing RelationType: " + name);
        }
        return null;
    }

}
