package net.sourceforge.ondex.parser.ecocyc;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.ecocyc.objects.AbstractNode;
import net.sourceforge.ondex.parser.ecocyc.objects.SinkFactory;
import net.sourceforge.ondex.parser.ecocyc.parse.*;
import net.sourceforge.ondex.parser.ecocyc.parse.readers.AbstractReader;
import net.sourceforge.ondex.parser.ecocyc.parse.readers.ColFileReader;
import net.sourceforge.ondex.parser.ecocyc.parse.readers.DatFileReader;
import net.sourceforge.ondex.parser.ecocyc.parse.transformers.*;

import java.io.File;
import java.util.*;

/**
 * Parser for the ecocyc flatfile database. The data flow is Parser ->
 * AbstractReader -> AbstractParser -> AbstractTransformer
 *
 * @author peschr
 * @TODO this needs consolatading into a CYC lib for parsing, not just a clone of AraCyc modified slightly
 */
public class Parser extends ONDEXParser implements MetaData {

    public static Map<String, String> speciesNames = new Hashtable<String, String>();

    static {
        //speciesNames.put("Aromatoleum aromaticum EbN1", "Rhodocyclaceae");
        //speciesNames.put("a non specified bacterium (Condo 77)", "bacteria");
        //speciesNames.put("Blautia producta", "clostridial firmicutes");
    }

    /**
     * Its used to define a configuration. A configuration consists of a parser,
     * a transformer, a filename and the name of the sink class.
     *
     * @author peschr
     */
    class Configuration {
        private AbstractParser parser;

        private Class<? extends AbstractTransformer> transformer;

        private String fileName;

        private SinkName sinkName;

        public SinkName getSinkName() {
            return this.sinkName;
        }

        public String getFileName() {
            return fileName;
        }

        public AbstractParser getParser() {
            return parser;
        }

        public Class<? extends AbstractTransformer> getTransformer() {
            return transformer;
        }

        public Configuration(AbstractParser parser,
                             Class<? extends AbstractTransformer> transformer,
                             String fileName, SinkName sinkName) {
            this.parser = parser;
            this.transformer = transformer;
            this.fileName = fileName;
            this.sinkName = sinkName;
        }
    }

    /**
     * enum which defines all possible sink names
     *
     * @author peschr
     */
    enum SinkName {
        Pathway, Reaction, Compound, Enzyme, Protein, Gene, Publication, ECNumber
    }

    // holds the configurations which are used for the parser
    private ArrayList<Configuration> configuration;

    // definies the order, this is needed for the transformation from sink -> to
    // concept/relation
    private SinkName[] pathwayOrder = {SinkName.Pathway, SinkName.Reaction,
            SinkName.Compound, SinkName.Enzyme, SinkName.Protein,
            SinkName.Gene, SinkName.Publication};

    private static Parser instance;

    /**
     * initialize the parser
     */
    public Parser() {

        instance = this;
        configuration = new ArrayList<Configuration>();
        configuration
                .add(new Configuration(new PublicationParser(),
                        PublicationTransformer.class, "pubs.dat",
                        SinkName.Publication));
        configuration.add(new Configuration(new GeneParser(),
                GeneTransformer.class, "genes.dat", SinkName.Gene));
        configuration.add(new Configuration(new GeneParser(),
                GeneTransformer.class, "genes.col", SinkName.Gene));
        configuration.add(new Configuration(new ProteinParser(),
                ProteinTransformer.class, "proteins.dat", SinkName.Protein));
        configuration.add(new Configuration(new EnzymeParser(),
                EnzymeTransformer.class, "enzrxns.dat", SinkName.Enzyme));
        configuration.add(new Configuration(new CompoundParser(),
                CompoundTransformer.class, "compounds.dat", SinkName.Compound));
        configuration.add(new Configuration(new ReactionParser(),
                ReactionTransformer.class, "reactions.dat", SinkName.Reaction));
        configuration.add(new Configuration(new PathwayParser(),
                PathwayTransformer.class, "pathways.dat", SinkName.Pathway));
        configuration.add(new Configuration(null, ECNumberTransformer.class,
                null, SinkName.ECNumber));
    }

    public String getName() {
        return new String("EcoCyc");
    }

    public String getVersion() {
        return new String("22.04.2009");
    }

    @Override
    public String getId() {
        return "ecocyc";
    }


    /**
     * sets the ONDEXGraph and starts the parser
     */
    public void start() {
        try {
            ReadAllFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a reader for a specific file extension
     *
     * @param fileName
     * @param inputDir
     * @param listener
     * @return AbstractReader - an abstract reader
     * @throws Exception
     */
    private AbstractReader getReader(String fileName, String inputDir,
                                     IParser listener) throws Exception {
        String wholeFileName = inputDir + "/" + fileName;
        if (fileName.substring(fileName.length() - 3).equals("dat"))
            return new DatFileReader(wholeFileName, listener);
        else
            return new ColFileReader(wholeFileName, listener);
    }

    /**
     * iterates over the fileNames array. In the first step it reads the files
     * and stores the information in sink objects. In the second step it
     * transforms the sink objects into concepts and relations.
     *
     * @throws Exception
     */
    private void ReadAllFiles() throws Exception {
        File dir = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

        // file -> sink
        for (Configuration c : configuration) {
            if (c.getFileName() == null)
                continue;
            String fileName = c.getFileName();
            IParser listener = c.getParser();
            AbstractReader reader = getReader(fileName, dir.getAbsolutePath(),
                    listener);
            fireEventOccurred(new GeneralOutputEvent("parse " + fileName,
                    getCurrentMethodName()));
            while (reader.hasNext()) {
                reader.next();
            }
        }
        // sink -> concept
        fireEventOccurred(new GeneralOutputEvent("creating concepts",
                getCurrentMethodName()));
        Iterator<AbstractNode> iterator1 = SinkFactory.getInstance().iterator();
        while (iterator1.hasNext()) {
            AbstractNode node = iterator1.next();
            getTransformer(node.getClass().getSimpleName());
            AbstractTransformer transformer = TransformerFactory.getInstance(
                    getTransformer(node.getClass().getSimpleName()), this);
            transformer.nodeToConcept(node);
            transformer.addCommonDetailsToConcept(node.getConcept(), node);
        }
        // sink -> relations
        fireEventOccurred(new GeneralOutputEvent(
                "creating relations and adding context information",
                getCurrentMethodName()));
        for (SinkName str : pathwayOrder) {
            Iterator<AbstractNode> iterator = SinkFactory.getInstance()
                    .typeIterator(str.name());
            while (iterator.hasNext()) {
                AbstractNode node = iterator.next();
                AbstractTransformer transformer = TransformerFactory
                        .getInstance(getTransformer(node.getClass()
                                .getSimpleName()), this);
                transformer.pointerToRelationsCore(node);
            }
        }
    }

    /**
     * returns a transformer for a sink object.
     *
     * @param nodeName
     * @return
     * @throws NoSuchElementException
     */
    private Class<? extends AbstractTransformer> getTransformer(String nodeName)
            throws NoSuchElementException {
        //
        for (Configuration c : this.configuration) {
            if (c.getSinkName().toString().equals(nodeName))
                return c.getTransformer();
        }

        throw new NoSuchElementException();
    }

    /**
     * returns the ONDEXGraph
     *
     * @return
     */
    public ONDEXGraph getGraph() {
        return graph;
    }

    /**
     * one validators are used
     */
    @Override
    public String[] requiresValidators() {
        return new String[]{"taxonomy"};
    }

    /**
     * no arguments are used
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR, FileArgumentDefinition.INPUT_DIR_DESC, true, true, true, false)
        };
    }

    public static void propagateEventOccurred(EventType et) {
        if (instance != null)
            instance.fireEventOccurred(et);
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
        return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line
                + "]";
    }

}
