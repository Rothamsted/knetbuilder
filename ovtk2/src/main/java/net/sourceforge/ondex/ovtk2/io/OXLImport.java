package net.sourceforge.ondex.ovtk2.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.export.oxl.Export;
import net.sourceforge.ondex.export.oxl.XMLTagNames;
import net.sourceforge.ondex.ovtk2.config.OVTK2PluginLoader;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.parser.oxl.ConceptMetaDataParser;
import net.sourceforge.ondex.parser.oxl.ConceptParser;
import net.sourceforge.ondex.parser.oxl.GeneralMetaDataParser;
import net.sourceforge.ondex.parser.oxl.RelationMetaDataParser;
import net.sourceforge.ondex.parser.oxl.RelationParser;
import net.sourceforge.ondex.parser.oxl.XmlParser;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;
import net.sourceforge.ondex.tools.ziptools.ZipEndings;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import com.ctc.wstx.io.CharsetNames;

/**
 * Importer for the OXL format.
 * 
 * @author taubertj
 * 
 */
public class OXLImport implements Monitorable {

	/**
	 * Wrapped XmlParser which delegates separate tag parsing
	 */
	private XmlParser parser = null;

	/**
	 * ONDEX graph to import to
	 */
	private ONDEXGraph aog = null;

	/**
	 * Filename of file to load
	 */
	private String filename = null;

	/**
	 * Prevent import from continuing
	 */
	private boolean cancelled = false;

	/**
	 * Whether or not to output user warnings
	 */
	private boolean silent = false;

	/**
	 * Parsed from info tag
	 */
	private int numberOfConcepts = 0;

	/**
	 * Parsed from info tag
	 */
	private int numberOfRelations = 0;

	/**
	 * Sum of concepts and relations
	 */
	private int maxProgress = 1;

	/**
	 * Current progress
	 */
	private int progress = 0;

	/**
	 * for reporting progress on concepts
	 */
	private ConceptParser conceptParser = null;

	/**
	 * for reporting progress on relations
	 */
	private RelationParser relationParser = null;

	/**
	 * Saved graph name from OXL file, might be null
	 */
	private String graphName = null;

	/**
	 * Saved annotations from OXL file, might be null
	 */
	private Map<String, String> annotations = new HashMap<String, String>();

	/**
	 * Default old constructor
	 * 
	 * @param aog
	 *            ONDEXGraph to fill
	 * @param file
	 *            OXL file to load
	 */
	public OXLImport(ONDEXGraph aog, File file) {
		this(aog, file, false);
	}

	/**
	 * Set internal variables.
	 * 
	 * @param aog
	 *            ONDEXGraph to fill
	 * @param file
	 *            OXL file to load
	 * @param silent
	 *            silence user notifications
	 */
	public OXLImport(ONDEXGraph aog, File file, boolean silent) {
		this.aog = aog;
		this.silent = silent;
		this.filename = file.getAbsolutePath();

		// new XML parser from back-end
		parser = new XmlParser();
	}

	/**
	 * Set internal variables.
	 * 
	 * @param aog
	 *            ONDEXGraph to fill
	 * @param filename
	 *            filename to load
	 * @param silent
	 *            silence user notifications
	 */
	public OXLImport(ONDEXGraph aog, String filename, boolean silent) {
		this.aog = aog;
		this.silent = silent;
		this.filename = filename;

		// new XML parser from back-end
		parser = new XmlParser();
	}

	/**
	 * @return the graphName
	 */
	public String getGraphName() {
		return graphName;
	}

	/**
	 * @return the annotations
	 */
	public Map<String, String> getAnnotations() {
		return annotations;
	}

	/**
	 * Creates an appropriate InputStream from given file.
	 * 
	 * @param filename
	 *            filename to be used
	 * @return InputStream on File
	 * @throws IOException
	 */
	private InputStream getInStream(String filename) throws IOException {
		InputStream inStream = null;

		int detectedEnding = ZipEndings.getPostfix(filename);

		// do decided where file is coming from
		URL url;
		if (filename.startsWith("http:") || filename.startsWith("file:") || filename.startsWith("https")) {
			// when loading from a server
			url = new URL(filename);
			// when loading from a REST URL
			if (filename.endsWith("/oxl"))
				detectedEnding = ZipEndings.OXL;
		} else {
			File file = new File(filename);
			url = file.toURI().toURL();
		}

		// get decompression right
		switch (detectedEnding) {

		case ZipEndings.GZ:
			inStream = new GZIPInputStream((InputStream) url.getContent());
			System.out.println("Detected GZIP file");
			break;
		case ZipEndings.OXL:
			inStream = new GZIPInputStream((InputStream) url.getContent());
			System.out.println("Detected OXL file");
			break;
		case ZipEndings.XML:
			inStream = (InputStream) url.getContent();
			System.out.println("Detected Uncompressed file");
			break;
		default:
			System.err.println("Unsupported filetype");
		}

		return inStream;
	}

	/**
	 * More obvious throw exceptions upwards.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws ClassNotFoundException
	 * @throws JAXBException
	 * @throws InconsistencyException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void start() throws FileNotFoundException, IOException, XMLStreamException, ClassNotFoundException, JAXBException, InconsistencyException, InstantiationException, IllegalAccessException {
		now();
		System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory");
		XMLInputFactory2 xmlInput = (XMLInputFactory2) XMLInputFactory2.newInstance();
		xmlInput.configureForSpeed();

		InputStream inStream = getInStream(filename);

		if (inStream != null) {
			// configure Parser
			XMLStreamReader2 xmlr = (XMLStreamReader2) xmlInput.createXMLStreamReader(inStream, CharsetNames.CS_UTF8);

			// check version number in file, that should be quick as version is
			// on the very top of the file
			if (!silent) {
				while (xmlr.hasNext()) {
					int i = xmlr.next();
					if (i == XMLStreamConstants.START_ELEMENT) {
						String name = xmlr.getName().getLocalPart();
						// check versions, complain only
						if (name.equals(XMLTagNames.VERSION)) {
							String version = xmlr.getElementText();
							if (!version.equals(Export.version)) {
								int option = JOptionPane.showConfirmDialog(OVTK2Desktop.getInstance().getMainFrame(), "The OXL file you attempt to load is from a different version of Ondex." + "\nThis can result in the file not being loaded correctly." + "\nDo you want to try and continue anyway?", "Problems while loading", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
								if (option == JOptionPane.NO_OPTION) {
									setCancelled(true);
									return;
								}
							}
						}

						// this is number of concepts tag
						else if (name.equals(XMLTagNames.NUMBERCONCEPTS)) {
							numberOfConcepts = Integer.parseInt(xmlr.getElementText());
						}

						// this is number of relations tag
						else if (name.equals(XMLTagNames.NUMBERRELATIONS)) {
							numberOfRelations = Integer.parseInt(xmlr.getElementText());
						}

						// saved graph name, for future use
						else if (name.equals(XMLTagNames.GRAPHNAME)) {
							graphName = xmlr.getElementText();
						}

						// saved annotations if any
						else if (name.equals(XMLTagNames.GRAPHANNOTATIONS)) {
							while (xmlr.hasNext()) {
								int event = xmlr.next();
								if (event == XMLStreamConstants.START_ELEMENT) {
									String localname = xmlr.getName().getLocalPart();
									if (localname.equals(XMLTagNames.GRAPHANNOTATION)) {
										// index annotations
										annotations.put(xmlr.getAttributeValue(0), xmlr.getElementText());
									}
								}
							}
						}

						// only parse the header of the file
						else if (name.equals(XMLTagNames.ONDEXMETADATA) || name.equals(XMLTagNames.ONDEXDATASEQ)) {
							break;
						}
					}
				}
				xmlr.close();

				// open a new InputStream as the previous one is used up
				xmlr = (XMLStreamReader2) xmlInput.createXMLStreamReader(getInStream(filename), CharsetNames.CS_UTF8);
			}

			// calculate new max progress
			if (numberOfConcepts + numberOfRelations > 0) {
				maxProgress = numberOfConcepts + numberOfRelations;
				System.out.println(filename + " contains " + numberOfConcepts + " concepts and " + numberOfRelations + " relations.");
			}

			try {
				// for loading plugin based Attribute data types
				ClassLoader cl = OVTK2PluginLoader.getInstance().ucl;
				Thread.currentThread().setContextClassLoader(cl);
			} catch (FileNotFoundException fnfe) {
				// we are running as applet, so ignore
			}

			// hashtable for id mapping old to new concept IDs
			Map<Integer, Integer> table = new HashMap<Integer, Integer>();
			Map<Integer, Set<Integer>> context = new HashMap<Integer, Set<Integer>>();
			parser.registerParser("cv", new ConceptMetaDataParser(aog, "cv"));
			parser.registerParser("unit", new GeneralMetaDataParser(aog, "unit"));
			parser.registerParser("attrname", new GeneralMetaDataParser(aog, "attrname"));
			parser.registerParser("evidences", new GeneralMetaDataParser(aog, "evidences"));
			parser.registerParser("cc", new ConceptMetaDataParser(aog, "cc"));
			parser.registerParser("relation_type", new RelationMetaDataParser(aog, "relation_type"));
			parser.registerParser("relationtypeset", new RelationMetaDataParser(aog, "relationtypeset"));

			conceptParser = new ConceptParser(aog, table, context);
			parser.registerParser("concept", conceptParser);
			relationParser = new RelationParser(aog, table);
			parser.registerParser("relation", relationParser);

			// start parsing
			parser.parse(xmlr);
			if (!cancelled)
				ConceptParser.syncContext(aog, table, context);

			// catch exceptions and throw them upwards
			if (conceptParser.errorMessages.size() > 0) {

				// concatenate nicely
				StringBuffer sb = new StringBuffer();
				for (String s : conceptParser.errorMessages) {
					sb.append(s);
					sb.append("\n");
				}

				JTextPane message = new JTextPane();
				message.setText("The OXL file you attempt to load contains inconsistencies." + "\nNot all of these inconsistencies might have been fixed." + "\nThis could lead to problems when using certain functions later on." + "\nDo you want to try and continue anyway?" + "\n\nError messages:\n" + sb.toString());

				int option = JOptionPane.showConfirmDialog(OVTK2Desktop.getInstance().getMainFrame(), message, "Problems while loading", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (option == JOptionPane.NO_OPTION) {
					setCancelled(true);
					return;
				}
			}

			// close reader
			xmlr.close();

			// indicates final state if no info has been parsed
			if (maxProgress == 1)
				progress = 1;
		}
		now();
	}

	public static void now() {
		String dateFormat = "dd.MM.yyyy G 'at' HH:mm:ss z";
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		System.err.println("Time is:" + sdf.format(cal.getTime()));
	}

	@Override
	public int getMaxProgress() {
		return maxProgress;
	}

	@Override
	public int getMinProgress() {
		return 0;
	}

	@Override
	public int getProgress() {
		if (conceptParser != null && relationParser != null && maxProgress > 1)
			return conceptParser.getProgress() + relationParser.getProgress();
		else
			return progress;
	}

	@Override
	public String getState() {
		// check for aborted case
		if (cancelled)
			return Monitorable.STATE_TERMINAL;

		if (conceptParser == null || relationParser == null)
			return Monitorable.STATE_IDLE;

		if (conceptParser.getProgress() == 0 && relationParser.getProgress() == 0)
			return "Parsing meta data.";

		else if (conceptParser.getProgress() > 0 && relationParser.getProgress() == 0) {
			String message = "Parsed " + conceptParser.getProgress();
			if (numberOfConcepts > 0) {
				message = message + " of " + numberOfConcepts;
			}
			return message + " concepts.";
		}

		else {
			String message = "Parsed " + relationParser.getProgress();
			if (numberOfRelations > 0) {
				message = message + " of " + numberOfRelations;
			}
			return message + " relations.";
		}

	}

	@Override
	public Throwable getUncaughtException() {
		return null;
	}

	@Override
	public boolean isAbortable() {
		return true;
	}

	@Override
	public boolean isIndeterminate() {
		return false;
	}

	@Override
	public void setCancelled(boolean c) {
		cancelled = c;
		parser.setCancelled(c);
	}

	public boolean isCancelled() {
		return cancelled;
	}

}
