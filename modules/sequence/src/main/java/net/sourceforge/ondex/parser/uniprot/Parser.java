package net.sourceforge.ondex.parser.uniprot;

import com.ctc.wstx.stax.WstxInputFactory;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.algorithm.annotationquality.GOTreeParser;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.DataURL;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.uniprot.sink.Protein;
import net.sourceforge.ondex.parser.uniprot.transformer.Transformer;
import net.sourceforge.ondex.parser.uniprot.xml.ComponentParser;
import net.sourceforge.ondex.parser.uniprot.xml.component.AccessionBlockParser;
import net.sourceforge.ondex.parser.uniprot.xml.component.CommentBlockParser;
import net.sourceforge.ondex.parser.uniprot.xml.component.DbReferenceBlockParser;
import net.sourceforge.ondex.parser.uniprot.xml.component.EntryStartParser;
import net.sourceforge.ondex.parser.uniprot.xml.component.GeneBlockParser;
import net.sourceforge.ondex.parser.uniprot.xml.component.ProteinNameBlockParser;
import net.sourceforge.ondex.parser.uniprot.xml.component.PublicationBlockParser;
import net.sourceforge.ondex.parser.uniprot.xml.component.SequenceBlockParser;
import net.sourceforge.ondex.parser.uniprot.xml.component.TaxonomieBlockParser;
import net.sourceforge.ondex.parser.uniprot.xml.filter.FilterEnum;
import net.sourceforge.ondex.parser.uniprot.xml.filter.IntegerValueFilter;
import net.sourceforge.ondex.parser.uniprot.xml.filter.StringValueFilter;
import net.sourceforge.ondex.parser.uniprot.xml.filter.ValueFilter;

/**
 * Parser for the UniProt Knowledgebase database. Implements this
 * http://www.devx.com/Java/Article/30298/0/page/2 way of parsing a xml file.
 *
 * @author peschr
 */
@Status(description = "Tested December 2013 (Jacek Grzebyta), Nov. 2016", status = StatusType.STABLE)
@Authors(authors = {"Robert Pesch", "Keywan Hassani-Pak", "Matthew Hindle"}, emails = {"", "keywan at users.sourceforge.net", "matthew_hindle at users.sourceforge.net"})
@DatabaseTarget(name = "UniProt", description = "A comprehensive, high-quality and freely accessible resource of protein sequence and functional information.", version = "UniProt release 2011_06", url = "http://www.uniprot.org")
@DataURL(name = "UniProt XML",
        description = "Any UniProt (Swiss-Prot or TrEMBL) XML file, avoid parsing everything if possible, select your species in UniProt and download a subset.",
        urls = {"ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/complete/uniprot_sprot.xml.gz",
                "ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/complete/uniprot_trembl.xml.gz",
                "http://www.uniprot.org/uniprot/?query=taxonomy%3A9606+AND+reviewed%3Ayes"})
@Custodians(custodians = {"Keywan Hassani-Pak"}, emails = {"keywan at users.sourceforge.net"})
public class Parser extends ONDEXParser
{
    private Transformer transformer;

    private int count;

    private HashMap<String, ComponentParser> delegates = new HashMap<String, ComponentParser>();

    private HashMap<FilterEnum, ValueFilter> filter = new HashMap<FilterEnum, ValueFilter>();

    private static String manuallyCuratedFileInfix = "sprot";

    private static Parser instance;

    public boolean inManuallyCuratedFile = false;

    private GOTreeParser goTree;
    
	private int itemParsedCount = 0;
	private int skippedCount = 0;
	private int debugOutputSize = 10000;
	private long time = System.currentTimeMillis();
	

    public Parser() {
        instance = this;
    }

    @Override
    public String[] requiresValidators() {
        return null;
    }

    public void setGraph(AbstractONDEXGraph graph) {
        this.graph = graph;
    }

    public ONDEXGraph getGraph() {
        return graph;
    }

    public String getName() {
        return "UniProt";
    }

    public String getVersion() {
        return "19.12.07";
    }

    @Override
    public String getId() {
        return "uniprot";
    }

    /**
     * initialize the delegators
     */
    private void init() throws InvalidPluginArgumentException {

        if (args.getUniqueValue(ArgumentNames.GO_OBO_FILE_ARG) != null) {
            String goOboFile = (String) args
                    .getUniqueValue(ArgumentNames.GO_OBO_FILE_ARG);
            try {
                this.goTree = new GOTreeParser(goOboFile);
                goTree.parseOboFile();
                System.out.println("GO index created with "
                        + goTree.getNumEntries() + " entries");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.transformer = new Transformer(graph, args, false, goTree);

        delegates.put("dbReference", new DbReferenceBlockParser(filter
                .get(FilterEnum.DatabaseReferenceFilter)));
        delegates.put("entry", new EntryStartParser());
        delegates.put("comment", new CommentBlockParser());
        delegates.put("sequence", new SequenceBlockParser());
        delegates.put("accession", new AccessionBlockParser(filter
                .get(FilterEnum.DatabaseReferenceFilter)));// .AccessionFilter)));
        delegates.put("organism", new TaxonomieBlockParser(filter
                .get(FilterEnum.TaxonomieFilter)));
        delegates.put("reference", new PublicationBlockParser());
        delegates.put("protein", new ProteinNameBlockParser());
        delegates.put("gene", new GeneBlockParser());
    }

    /**
     * setups the filters
     */
    private void setupFilters(Set<ONDEXConcept> concepts)
            throws InvalidPluginArgumentException {

        Object[] taxids = args.getObjectValueArray(ArgumentNames.TAXID_ARG);
        if (taxids != null && taxids.length > 0) {
            filter.put(FilterEnum.TaxonomieFilter, new IntegerValueFilter(
                    taxids));
        }

        if (args.getUniqueValue(ArgumentNames.ACCESSION_ARG) != null
                && args.getUniqueValue(ArgumentNames.ACCESSION_ARG).toString()
                .trim().length() > 0) {
            String[] accessions = args
                    .getUniqueValue(ArgumentNames.ACCESSION_ARG).toString()
                    .split(",");
            ValueFilter accessionFilter = new StringValueFilter(accessions);
            filter.put(FilterEnum.DatabaseReferenceFilter, accessionFilter);
        }

        if (args.getUniqueValue(ArgumentNames.ACCESSION_FILE_ARG) != null
                && args.getUniqueValue(ArgumentNames.ACCESSION_FILE_ARG)
                .toString().trim().length() > 0) {

            String fileName = (String) args
                    .getUniqueValue(ArgumentNames.ACCESSION_FILE_ARG);
            String accs = "";

            try {
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                while (br.ready()) {
                    String inputline = br.readLine();
                    accs += inputline + ",";
                }
                br.close();
                String[] accessions = accs.split(",");
                ValueFilter accessionFilter = new StringValueFilter(accessions);
                filter.put(FilterEnum.DatabaseReferenceFilter, accessionFilter);
                fireEventOccurred(new GeneralOutputEvent("Set accession file: "
                        + fileName, "[Parser - setupFilters]"));

            } catch (FileNotFoundException fnfe) {
                fireEventOccurred(new DataFileMissingEvent(fnfe.getMessage(),
                        "[Parser - setupFilters]"));
            } catch (IOException ioe) {
                fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
                        "[Parser - setupFilters]"));
            }

        }

        if (args.getUniqueValue(ArgumentNames.REFERENCE_ACCESSION_ARG) != null
                && (Boolean) args
                .getUniqueValue(ArgumentNames.REFERENCE_ACCESSION_ARG)) {
            StringValueFilter reffilter = new StringValueFilter();
            for (ONDEXConcept concept : concepts) {
                for (ConceptAccession accession : concept.getConceptAccessions()) {
                    // Why is accession merged with DataSource?
                    // reffilter.addFilterValues(accession.getElementOf(s).getId(s)
                    // + accession.getAccession(s));
                    reffilter.addFilterValues(accession.getAccession());
                }

            }
            filter.put(FilterEnum.DatabaseReferenceFilter, reffilter);
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                    .fireEventOccurred(
                            new GeneralOutputEvent("Found "
                                    + reffilter.getSize() + " references",
                                    Parser.class.toString()));
        }
    }

    /**
     * searchs for all .gz-files in the inputDirectory
     */
    public void start() throws InvalidPluginArgumentException {

        WstxInputFactory factory = (WstxInputFactory) WstxInputFactory
                .newInstance();
        factory.configureForSpeed();

        setupFilters(graph.getConceptsOfConceptClass(graph.getMetaData()
                .getConceptClass("Protein")));
        try {
            init();
            File file = new File((String) args
                    .getUniqueValue(FileArgumentDefinition.INPUT_FILE));
            
            fireEventOccurred(
            		new GeneralOutputEvent("Parsing uniprot file "
            				+ file.getAbsolutePath(), Parser.class
            				.toString()));

            if (file.getName().contains(manuallyCuratedFileInfix)) {
            	inManuallyCuratedFile = true;
            } else {
            	inManuallyCuratedFile = false;
            }
            InputStream stream;
            if (file.toString().endsWith(".gz")) {
            	stream = new BufferedInputStream(new GZIPInputStream(
            			new FileInputStream(file), 512 * 5));
            } else {
            	stream = new BufferedInputStream(new FileInputStream(file));
            }

            XMLStreamReader staxXmlReader = (XMLStreamReader) factory
            .createXMLStreamReader(stream);

            while (staxXmlReader.hasNext()) {
            	int event = staxXmlReader.next();
            	// parse one protein information and store in sink objects
            	if (event == XMLStreamConstants.START_ELEMENT) {
            		String element = staxXmlReader.getLocalName();

            		if (delegates.containsKey(element)) {
            			ComponentParser parser = (ComponentParser) delegates
            			.get(element);
            			parser.parseElement(staxXmlReader);
            		}
            	}
            	// transform one protein into Ondex datastructure
            	else if (event == XMLStreamConstants.END_ELEMENT) {
            		String element = staxXmlReader.getLocalName();
            		if(element.equalsIgnoreCase("entry")){
            			transformData();
            		}
            	}
            }
            staxXmlReader.close();



        } catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println("DbLinks (CVs) NOT parsed: "+Transformer.unknownCVs.toString());
        fireEventOccurred(new GeneralOutputEvent(
                "parsed " + count + " entries", Parser.class.toString()));
    }

    /**
     * transformation and filter step
     * this method is called after each "entry" end element
     * it transforms the information collected about one single protein
     * 
     */
    public void transformData()throws InvalidPluginArgumentException {

    	boolean skip = false;

    	for (ValueFilter i : Parser.this.filter.values()) {
    		if (i.getState() == false)
    			skip = true;
    		i.resetState();
    	}
    	if (skip == false) {
    		transformer.transform(Protein.getInstance());
    		count++;

    	} else
    		skippedCount++;
    	if (++itemParsedCount % debugOutputSize == 0) {
    		GeneralOutputEvent ge = new GeneralOutputEvent("parsed "
    				+ debugOutputSize + " entries, skipped " + skippedCount
    				+ " " + "transformed proteins "
    				+ (debugOutputSize - skippedCount) + " in "
    				+ (System.currentTimeMillis() - time) + " ms ("
    				+ itemParsedCount + ")", Parser.class.toString());
    		fireEventOccurred(ge);
    		time = System.currentTimeMillis();
    		skippedCount = 0;
    	}
    	// create a new instance
    	Protein.getInstance(true, Parser.this.inManuallyCuratedFile);
    }
    

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        StringArgumentDefinition taxId = new StringArgumentDefinition(
                ArgumentNames.TAXID_ARG, ArgumentNames.TAXID_ARG_DESC, false,
                null, true);
        StringArgumentDefinition accessions = new StringArgumentDefinition(
                ArgumentNames.ACCESSION_ARG, ArgumentNames.ACCESSION_ARG_DESC,
                false, null, false);
        StringArgumentDefinition accessionFile = new StringArgumentDefinition(
                ArgumentNames.ACCESSION_FILE_ARG,
                ArgumentNames.ACCESSION_FILE_ARG_DESC, false, null, false);
        BooleanArgumentDefinition reference = new BooleanArgumentDefinition(
                ArgumentNames.REFERENCE_ACCESSION_ARG,
                ArgumentNames.REFERENCE_ACCESSION_ARG_DESC, false, false);
        BooleanArgumentDefinition contexts = new BooleanArgumentDefinition(
                ArgumentNames.TAG_INFORMATION_ARG,
                ArgumentNames.TAG_INFORMATION_ARG_DESC, false, true);
        BooleanArgumentDefinition hideLargeScaleRefs = new BooleanArgumentDefinition(
                ArgumentNames.HIDE_LARGE_SCALE_PUBLICATIONS_ARG,
                ArgumentNames.HIDE_LARGE_SCALE_PUBLICATIONS_ARG_DESC, false,
                true);
        FileArgumentDefinition goFile = new FileArgumentDefinition(
                ArgumentNames.GO_OBO_FILE_ARG,
                ArgumentNames.GO_OBO_FILE_ARG_DESC, false, true, false, false);
        FileArgumentDefinition inputDir = new FileArgumentDefinition(
                FileArgumentDefinition.INPUT_FILE,
                "UniProt XML file", true, true, false, false);
        return new ArgumentDefinition<?>[]{inputDir, taxId, goFile, reference,
                accessions, accessionFile, contexts, hideLargeScaleRefs};
    }

    /**
     * File name filter for gz and xml
     *
     * @author peschr
     */
    class XMLGzFileNameFilter implements FileFilter {
        public boolean accept(File file) {
            String name = file.getName().toLowerCase();
            return (name.endsWith("gz") || name.endsWith("xml"))
                    && file.isFile();
        }
    }

    public static void propagateEventOccurred(EventType et) {
        if (instance != null)
            instance.fireEventOccurred(et);
    }

    /**
     * Convenience method for outputting the current method name in a dynamic
     * way
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
