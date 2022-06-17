package net.sourceforge.ondex.parser.oxl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.bind.JAXBException;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.codehaus.stax2.XMLInputFactory2;

import com.ctc.wstx.io.CharsetNames;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.EventType.Level;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.ParsingErrorEvent;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.exception.type.ParsingFailedException;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.logging.ONDEXLogger;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.tools.ziptools.ZipEndings;
import net.sourceforge.ondex.workflow.InvalidPluginArgumentException;
import net.sourceforge.ondex.workflow.ONDEXPluginArguments;
import uk.ac.ebi.utils.exceptions.ExceptionUtils;
import uk.ac.ebi.utils.exceptions.UnexpectedValueException;

/**
 * Parser for OXL files.
 * 
 * @author sierenk, taubertj
 * @author Matthew Pocock
 */
@Status(description = "Tested March 2010 (Artem Lysenko)", status = StatusType.STABLE)
@Authors(authors = { "Matthew Pocock", "Jan Taubert", "K Sieren" }, emails = {
		"drdozer at users.sourceforge.net",
		"jantaubert at users.sourceforge.net", "" })
@Custodians(custodians = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
public class Parser extends ONDEXParser {

	private final XMLInputFactory2 xmlif;
	private static final long KILOBYTE = 1024L;
	private static final long MEGABYTE = KILOBYTE * KILOBYTE;

	/**
	 * Constructor initializes the XMLInputFactory2
	 */
	public Parser() {
		// setup XMLStreamReader
    System.setProperty( "ondex.javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory" );
    xmlif = (XMLInputFactory2) XMLInputFactory2.newFactory (
    	"ondex.javax.xml.stream.XMLInputFactory", this.getClass ().getClassLoader ()
    );
		xmlif.configureForSpeed();
		xmlif.setProperty ( XMLInputFactory2.IS_COALESCING, true );
	}

	private HashSet<AttributeName> ignoreGDSAttributeGDS = null;

	/**
	 * Returns name of parser.
	 * 
	 * @return String
	 */
	@Override
	public String getName() {
		return "OXL Parser";
	}

	/**
	 * Returns version of parser.
	 * 
	 * @return String
	 */
	@Override
	public String getVersion() {
		return "20.03.2007";
	}

	@Override
	public String getId() {
		return "oxl";
	}

	/**
	 * Defines the arguments that can be passed to this export.
	 * 
	 * @return ArgumentDefinition[]
	 */
	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		ArgumentDefinition<?>[] options = getOptionArgumentDefinitions();
		ArgumentDefinition<?>[] outputs = getInputArgumentDefinitions();
		ArgumentDefinition<?>[] combined = new ArgumentDefinition<?>[options.length
				+ outputs.length];
		for (int i = 0; i < options.length; i++) {
			combined[i] = options[i];
		}
		for (int i = 0; i < outputs.length; i++) {
			combined[i + options.length] = outputs[i];
		}
		return combined;
	}

	private ArgumentDefinition<?>[] getOptionArgumentDefinitions() {
		ArgumentDefinition<String> ignoreGDS = new StringArgumentDefinition(
				ArgumentNames.IGNORE_ATTRIBUTE_ARG,
				ArgumentNames.IGNORE_ATTRIBUTE_ARG_DESC, false, null, true);
		return new ArgumentDefinition[] { ignoreGDS };
	}

	protected ArgumentDefinition<?>[] getInputArgumentDefinitions() {
		FileArgumentDefinition fileArg = new FileArgumentDefinition(
				FileArgumentDefinition.INPUT_FILE, "OXL file to load", true,
				true, false, false);
		return new ArgumentDefinition[] { fileArg };
	}

	@Override
	public void start() throws PluginConfigurationException {

		setOptionalArguements();

		getInputAndStart();

		GeneralOutputEvent so1 = new GeneralOutputEvent(
				"OXL parsing finished.", "[Parser - start]");
		so1.setLog4jLevel(Level.INFO);
		fireEventOccurred(so1);

	}

	protected void getInputAndStart() throws PluginConfigurationException {
		// get file list to parse
		File file = new File(
				(String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE));
		start(file);
	}

	private void setOptionalArguements() throws InvalidPluginArgumentException {
		EventType so = new GeneralOutputEvent("Starting OXL parsing...",
				"[Parser - start]");
		so.setLog4jLevel(Level.INFO);
		fireEventOccurred(so);

		String[] gdss = (String[]) args
				.getObjectValueArray(ArgumentNames.IGNORE_ATTRIBUTE_ARG);
		for (String gds : gdss) {
			AttributeName attname = graph.getMetaData().getAttributeName(gds);
			if (attname != null) {
				if (ignoreGDSAttributeGDS == null) {
					ignoreGDSAttributeGDS = new HashSet<AttributeName>();
				}
				ignoreGDSAttributeGDS.add(attname);
			} else {
				fireEventOccurred(new AttributeNameMissingEvent(gds
						+ " is not found in the metadata (ignoring parameter)",
						"[Parser - start]"));
			}

		}
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	/**
	 * Performs parsing of OXL file.
	 * 
	 * @param fileToRead
	 *            String
	 * @throws net.sourceforge.ondex.exception.type.PluginConfigurationException
	 *             if the file could not be opened, unzipped, or the contained
	 *             xml could not be processed
	 */
	private void start(File fileToRead) throws PluginConfigurationException {

		fireEventOccurred(new GeneralOutputEvent("Parsing "
				+ fileToRead.getAbsolutePath(), "[Parser - start]"));

		// check if file exists
		if (!fileToRead.exists()) {
			fireEventOccurred(new DataFileMissingEvent(
					fileToRead.getAbsolutePath() + " can not be found.",
					"[Parser - start]", Level.ERROR));
			return;
		}

		// check if file can be read
		if (!fileToRead.canRead()) {
			fireEventOccurred(new DataFileMissingEvent(
					fileToRead.getAbsolutePath() + " can not be read.",
					"[Parser - start]"));
			return;
		}

		NumberFormat format = NumberFormat.getInstance();

		long sizeInMegs = fileToRead.length() / MEGABYTE;

		fireEventOccurred(new GeneralOutputEvent("File to be parsed is "
				+ format.format(sizeInMegs) + " Mb", "[Parser - start]"));

		XMLStreamReader xmlr;
		try {
			int detectedEnding = ZipEndings.getPostfix(fileToRead);
			InputStream in = null;

			// detect ending and open file accordingly
			switch (detectedEnding) {

			case ZipEndings.XML:
				in = new FileInputStream(fileToRead);
				fireEventOccurred(new GeneralOutputEvent(
						"Detected uncompressed file", "[Parser - start]"));
				break;
			case ZipEndings.OXL:
				in = new GZIPInputStream(new FileInputStream(fileToRead));
				fireEventOccurred(new GeneralOutputEvent("Detected OXL file",
						"[Parser - start]"));
				break;
			case ZipEndings.GZ:
				in = new GZIPInputStream(new FileInputStream(fileToRead));
				fireEventOccurred(new GeneralOutputEvent("Detected GZIP file",
						"[Parser - start]"));
				break;
			case ZipEndings.ZIP:
				ZipFile zipFile = new ZipFile(fileToRead);
				if (zipFile.size() > 1) {
					fireEventOccurred(new DataFileErrorEvent(
							"There are multiple files in this zip file: can not parse",
							"[Parser - start]"));
				}
				in = zipFile.getInputStream(zipFile.entries().nextElement());
				fireEventOccurred(new GeneralOutputEvent("Detected ZIP file",
						"[Parser - start]"));
				break;
			default:
				fireEventOccurred(new GeneralOutputEvent(
						"Unsupported filetype", "[Parser - start]"));
				return;
			}

			xmlr = xmlif.createXMLStreamReader(in, CharsetNames.CS_UTF8);

		} catch (FileNotFoundException e) {
			throw new PluginConfigurationException(e);
		} catch (ZipException e) {
			throw new ParsingFailedException(e);
		} catch (IOException e) {
			throw new ParsingFailedException(e);
		} catch (XMLStreamException e) {
			throw new ParsingFailedException(e);
		}

		start(xmlr);
	}

	/**
	 * Parses a OXL file from an stream
	 * 
	 * @param stream
	 *            the stream to parse XML from
	 * @throws net.sourceforge.ondex.exception.type.ParsingFailedException
	 *             if the oxl xml could not be processed
	 * @throws InconsistencyException
	 */
	public void start(InputStream stream) throws ParsingFailedException,
			InconsistencyException {
		try {
			start ( xmlif.createXMLStreamReader ( stream, CharsetNames.CS_UTF8 ) );
		} catch (XMLStreamException e) {
			throw new ParsingFailedException(e);
		}
	}

	public void start(Reader reader) throws ParsingFailedException {
		try {
			XMLStreamReader xmlr;
			xmlr = xmlif.createXMLStreamReader ( reader );
			start(xmlr);
		} catch (XMLStreamException e) {
			fireEventOccurred(new ParsingErrorEvent(e.getMessage(),
					"[Parser - start]"));
			throw new ParsingFailedException(e);
		}
	}

	/**
	 * Parses OXL from an XMLStreamReader.
	 * 
	 * Beware that this method wraps the parameter reader with a filter that strips all extra-whitespaces away
	 * from the input (See <a href='https://goo.gl/wirWjg'>here</a> for details). This was made necessary by 
	 * the rdf2oxl tool in 2018, which doesn't spawn a compact XML. 
	 * 
	 * @param xmlr
	 *            the stream to parse XML from
	 * @throws net.sourceforge.ondex.exception.type.ParsingFailedException
	 *             if the oxl xml could not be processed
	 * @throws InconsistencyException
	 */
	public void start(XMLStreamReader xmlr) throws ParsingFailedException 
	{
		try 
		{
			// We need to ignore the extra-whitespaces, our parsers rely on that heavily
			xmlr = xmlif.createFilteredReader ( xmlr,
				new StreamFilter() 
				{
					@Override
					public boolean accept ( XMLStreamReader reader ) {
						return !reader.isWhiteSpace ();
					}
			});

			if ( this.graph.isLoadingModeSupported () )
				// If the graph is empty, we can retain the IDs in the OXL (if this is supported).
				this.graph.setLoadingMode ( graph.getConcepts ().size () == 0 ); 
					
			XmlParser parser = new XmlParser(this);

			// hashtable for id mapping old to new concept ids
			// This returns identities if we're in loading mode (see above).
			Map<Integer, Integer> table = new HashMap<>();
			Map<Integer, Set<Integer>> context = new HashMap<>();

			parser.registerParser("cv", new ConceptMetaDataParser(graph, "cv"));
			parser.registerParser("unit", new GeneralMetaDataParser(graph,
					"unit"));
			parser.registerParser("attrname", new GeneralMetaDataParser(graph,
					"attrname"));
			parser.registerParser("evidences", new GeneralMetaDataParser(graph,
					"evidences"));
			parser.registerParser("cc", new ConceptMetaDataParser(graph, "cc"));
			parser.registerParser("relation_type", new RelationMetaDataParser(
					graph, "relation_type"));
			parser.registerParser("relationtypeset",
					new RelationMetaDataParser(graph, "relationtypeset"));

			ConceptParser cp = new ConceptParser(graph, table, context);
			parser.registerParser("concept", cp);
			cp.setIgnoreAttributes(ignoreGDSAttributeGDS);

			RelationParser rp = new RelationParser(graph, table);
			parser.registerParser("relation", rp);
			rp.setIgnoreAttributes(ignoreGDSAttributeGDS);

			parser.parse(xmlr);

			ConceptParser.syncContext(graph, table, context);

			// catch exceptions and throw them upwards
			if (cp.errorMessages.size() > 0) {
				fireEventOccurred(new ParsingErrorEvent(
						cp.errorMessages.toString(), "[Parser - start]"));
				throw new ParsingFailedException(cp.errorMessages.toString());
			}

			// close reader
			xmlr.close();
		} 
		catch (InconsistencyException|XMLStreamException|JAXBException|ClassNotFoundException|InstantiationException|IllegalAccessException e) 
		{
			fireEventOccurred(new ParsingErrorEvent(e.getMessage(),
					"[Parser - start]"));
			throw new ParsingFailedException(e);
		} 
		finally {
			if ( this.graph.isLoadingMode () ) this.graph.setLoadingMode ( false );
		}
	}
	
	/**
	 * Utility to use this plug-in to load a graph outside of ONDEX, ONDEX Mini or alike. This is mainly useful for 
	 * testing.
	 * 
	 * It populates the graph parameter and returns it. When the graph is null, creates a {@link MemoryONDEXGraph}.
	 * 
	 */
	public static ONDEXGraph loadOXL ( String filePath, ONDEXGraph graph )
	{
		try
		{
			if ( graph == null )
				graph = new MemoryONDEXGraph ( "default" );
			
			Parser parser = new Parser ();
			parser.setONDEXGraph ( graph );
			parser.addONDEXListener ( new ONDEXLogger () );
			
			ONDEXPluginArguments args = new ONDEXPluginArguments ( parser.getArgumentDefinitions () );
			args.setOption ( FileArgumentDefinition.INPUT_FILE, filePath );
			
			parser.setArguments ( args );
			parser.start ();
			return graph;
		}
		catch ( PluginConfigurationException ex )
		{
			throw ExceptionUtils.buildEx ( UnexpectedValueException.class, ex,
			  "Internal error while loading '%s': %s", filePath, ex.getMessage ()		
			);
		}
	}
	
	/**
	 * Default to null.
	 */
	public static ONDEXGraph loadOXL ( String filePath )
	{
		return loadOXL ( filePath, null );
	}	
}
