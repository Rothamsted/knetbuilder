package uk.ac.ncl.cs.psimi2ondex;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipFile;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.exception.type.ParsingFailedException;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.exception.type.PluginException;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.tools.MetaDataTopup;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.ncl.cs.intbio.psimi.PSIMIEngine;
import uk.ac.ncl.cs.intbio.psimi.PSIMIProcessor;

/**
 * Ondex parser for molecular interactions data. Aims to be compatible with
 * all HUPO PSI-MI 2.5 formatted files.
 *
 * @author jweile
 */
public class PsiMiParser extends ONDEXParser {

    //###### PARSER ARGUMENTS ######

    //### argument definition constants ###

    //TODO: Include lookup file in jar and make this optional.
    public static final String LOOKUP_FILE = "MappingFile";
    public static final String LOOKUP_FILE_DESC = "Location of the PSI-MI to Ondex mapping file";
    
    public static final String PARSE_SEQ = "ParseSequences";
    public static final String PARSE_SEQ_DESC = "Whether to parse contained DNA or AA sequences or not";

    public static final String IT_FALLBACK = "InteractionTypeFallBack";
    public static final String IT_FALLBACK_DESC = "Id of concept class to use for untyped interactions";

    //### Argument definition method ###

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {

        return new ArgumentDefinition<?>[] {

            new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE, 
                    FileArgumentDefinition.INPUT_FILE_DESC,
                    true, true, false, false),

            new FileArgumentDefinition(LOOKUP_FILE, LOOKUP_FILE_DESC,
                    false, true, false, false),

            new BooleanArgumentDefinition(PARSE_SEQ, PARSE_SEQ_DESC, false, false),

            new StringArgumentDefinition(IT_FALLBACK, IT_FALLBACK_DESC, false, "Interaction", false)

        };

    }

    //### Argument fields and getters ###

    /**
     * Whether to parse contained DNA or AA sequences or not.
     */
    private boolean parseSequences;

    /**
     * Id of concept class to use for untyped interactions.
     */
    private String itFallBack;

    /**
     * Location of the PSI-MI to Ondex mapping file
     */
    private File lookupFile = null;

    /**
     * Gets the ID of concept class to use for untyped interactions.
     *
     * @return the ID of concept class to use for untyped interactions.
     */
    public String getItFallBack() {
        return itFallBack;
    }

    /**
     * Returns whether to parse contained DNA or AA sequences or not.
     *
     * @return Whether to parse contained DNA or AA sequences or not.
     */
    public boolean isParsingSequences() {
        return parseSequences;
    }

    /**
     * Returns the location of the PSI-MI to Ondex mapping file.
     * @return Location of the PSI-MI to Ondex mapping file.
     */
    public File getLookupFile() {
        return lookupFile;
    }



    //####### PARSER START METHOD #######

    /**
     * Overrides OndexParser.start(). Starts the parsing process.
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {

        //process the parser's arguments
        processParserArguments();

        /*
         * Top-up the metadata with the internally stored metadata file
         */
        InputStream mdin = this.getClass().getClassLoader()
                .getResourceAsStream("psimi2ondex_metadata.xml");
        MetaDataTopup mdTopup = new MetaDataTopup(graph, mdin);
        mdTopup.topup();
        


        File inputFile = new File((String) getArguments()
                .getUniqueValue(FileArgumentDefinition.INPUT_FILE));
        String fileSuffix = inputFile.getName().substring(inputFile.getName().length()-4);

        if (fileSuffix.equalsIgnoreCase(".zip")) {

            log("Reading ZIP file: "+inputFile.getAbsolutePath());
            ZipFile zf = new ZipFile(inputFile);
            ZipHandler zh = new ZipHandler(zf) {
                @Override
                public void handleZipStream(InputStream in) throws Exception{
                    parsePsimiStream(in, -1L);
                }
            };
            zh.process();

        } else if (fileSuffix.equalsIgnoreCase(".xml")) {
            /*
             * Package up the input file as stream and parse it.
             */
            logger.log(Level.INFO,"Parsing file: "+inputFile.getAbsolutePath());

            InputStream in = new FileInputStream(inputFile);

            parsePsimiStream(in, inputFile.length());

            
        } else {
            throw new ParsingFailedException("Unsupported input file format: \""+fileSuffix+"\"." +
                    "Supported formats are: \".xml\" and \".zip\".");
        }

    }

    private void parsePsimiStream(InputStream in, long fileLength) throws Exception {

        /*
         * Prepare the PSIMIProcessor. The processor is the core component of
         * this parser which receives PSIMI entities from the parser's input and
         * decides what to do with them.
         */

        PSIMIProcessor processor = new OndexPsimiProcessor(graph, this);

        /*
         * Then prepare the PSIMIEngine, which pumps entities from the
         * input stream into the processor.
         * Give the engine access to the file handle, so it can monitor the
         * parsing progress. Finally, start the engine.
         */
        PSIMIEngine engine = new PSIMIEngine(new BufferedInputStream(in), processor);

        if (fileLength > 0) {
            engine.setFileChannel(((FileInputStream)in).getChannel());
            engine.defineStreamLength(fileLength);
        }

        engine.start();
    }

    /**
     * Processes the parser's arguments by making them available as fields.
     * @throws PluginException
     */
    private void processParserArguments() throws PluginException {

        if (getArguments().getUniqueValue(LOOKUP_FILE) != null) {

            lookupFile = new File((String) getArguments()
                    .getUniqueValue(LOOKUP_FILE));
            if (!lookupFile.exists()) {
                throw new PluginConfigurationException("Specified metadata " +
                        "mapping file does not exist: " +
                        lookupFile.getAbsolutePath());
            }
        } 


        if (getArguments().getUniqueValue(PARSE_SEQ) != null) {
            parseSequences = (Boolean) getArguments().getUniqueValue(PARSE_SEQ);
        } else {
            parseSequences = false;
        }

        if (getArguments().getUniqueValue(IT_FALLBACK) != null) {
            itFallBack = (String) getArguments().getUniqueValue(IT_FALLBACK);
        } else {
            itFallBack = "Interaction";
        }
    }

    //##### INTERFACING METHODS #####

    /**
     * Overrides ONDEXParser.getId(). Returns the identifier of this parser.
     * @return The identifier of this parser.
     */
    @Override
    public String getId() {
        return "psimi25";
    }

    /**
     * Overrides ONDEXParser.getName(). Returns the name of this parser.
     * @return The name of this parser.
     */
    @Override
    public String getName() {
        return "PSI-MI v2.5 stream parser";
    }

    /**
     * Overrides ONDEXParser.getVersion(). Returns the version of this parser.
     * @return The version of this parser.
     */
    @Override
    public String getVersion() {
        return "2.0 beta";
    }

    /**
     * Overrides ONDEXParser.requiresValidators(). Returns the IDs of the
     * required validators for this parser. As this parser requires none, an
     * empty array is returned
     * @return Returns the IDs of the required validators for this parser.
     * As this parser requires none, an empty array is returned
     */
    @Override
    public String[] requiresValidators() {
        return new String[0];
    }


    private Logger logger = Logger.getLogger(this.getClass());

    public void log(String msg) {
        logger.log(Level.INFO, msg);
    }

    public void complain(String msg) {
        logger.log(Level.WARN, msg);
    }

}
