package net.sourceforge.ondex.parser.medline2.xml;


import static uk.ac.ebi.utils.exceptions.ExceptionUtils.buildEx;
import static uk.ac.ebi.utils.exceptions.ExceptionUtils.throwEx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipFile;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.collections15.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctc.wstx.stax.WstxInputFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.sourceforge.ondex.parser.medline2.Parser;
import net.sourceforge.ondex.parser.medline2.sink.Abstract;
import net.sourceforge.ondex.tools.ziptools.ZipEndings;
import uk.ac.ebi.utils.regex.RegEx;
import uk.ac.ebi.utils.runcontrol.MultipleAttemptsExecutor;
import uk.ac.ebi.utils.runcontrol.PercentProgressLogger;
import uk.ac.ebi.utils.runcontrol.ProgressLogger;
import uk.ac.ebi.utils.xml.XmlFilterUtils;

/**
 * MEDLINE XML parser
 * 
 * @author keywan
 *
 */
public class XMLParser {

	private WstxInputFactory factory;

	private final static Pattern tabPattern = Pattern.compile("[\\t|\\n|\\r]");

	public XMLParser() {
		System.setProperty ( "ondex.javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory" );
		factory = (WstxInputFactory) WstxInputFactory.newFactory (
			"ondex.javax.xml.stream.XMLInputFactory", this.getClass ().getClassLoader ()
		);
		factory.configureForLowMemUsage();
		factory.setProperty(XMLInputFactory2.IS_COALESCING, true);
		factory.setProperty(XMLInputFactory2.P_PRESERVE_LOCATION, false);
		factory.setProperty(XMLInputFactory2.P_REPORT_PROLOG_WHITESPACE, false);
		factory.setProperty(XMLInputFactory2.IS_VALIDATING, true);
		factory.setProperty(XMLInputFactory2.IS_SUPPORTING_EXTERNAL_ENTITIES,
				false);
		factory.setProperty(XMLInputFactory2.P_AUTO_CLOSE_INPUT, true);
		//		otherwise it will follow to link
		factory.setProperty(XMLInputFactory2.SUPPORT_DTD, false); 
	}


//	private final static String PUBMED_CIT = "PubmedArticle";

	private final static String MEDLINE_CIT = "MedlineCitation";

	private final static String PMID = "PMID";

	private final static String TITLE = "ArticleTitle";

	private final static String ABSTRACT = "Abstract";
	
	private final static String ABSTRACTTXT = "AbstractText";

//	private final static String AUTHORLIST = "AuthorList";

	private final static String AUTHOR = "Author";

//	private final static String JOURNAL = "Journal";

	private final static String JOURNAL_TITLE = "Title";

	private final static String CHEM = "Chemical";

	private final static String MESHDESCR = "DescriptorName";

	
	/** The XML content of these elements is wrapped with CDATA blocks, to avoid XML parser problems */
	private final static String [] CDATA_ELEMENTS = new String[] { "ArticleTitle", "AbstractText" };

	/** Matches stuff like {@code <MedlineDate>2000 Spring</MedlineDate>} in the hope to extrat the year */
	private final static RegEx MEDLINE_YEAR_RE = new RegEx ( "^([0-9]{4}).*$" );
	

	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	/**
	 * Reads one single PubMed/MEDLINE XML file.
	 * 
	 * @param file -
	 *            PubMed XML file to be parsed
	 * @return List of publications (Abstract)
	 */
	public Set<Abstract> parseMedlineXML ( File file ) throws IOException, XMLStreamException {

		//		System.out.println("Parsing file: " + file);
		Set<Abstract> abstracts = new HashSet<>();

		InputStream xmlin = null;

		// detecting whether input file is zipped or not
		int detectedEnding = ZipEndings.getPostfix(file);

		switch (detectedEnding) {

		case ZipEndings.GZ:
			xmlin = new GZIPInputStream ( new FileInputStream( file ) );
			break;
		case ZipEndings.ZIP:
			ZipFile zipFile = new ZipFile(file);
			if (zipFile.size() > 1) {
				System.err.println("There are multiple files in this zip file: can not parse");
			}
			xmlin = zipFile.getInputStream ( zipFile.entries().nextElement() );
			break;
		default:
			xmlin = new FileInputStream ( file );
		}

		// Wrap these elements with CDATA
		xmlin = XmlFilterUtils.cdataWrapper ( xmlin, CDATA_ELEMENTS );
		XMLStreamReader2 staxXmlReader = (XMLStreamReader2) factory.createXMLStreamReader( xmlin );

		abstracts = this.parseMedlineXML ( staxXmlReader );
		return abstracts;
	}

	/**
	 * Reads a set of PubMed IDs and uses web-services to retrieve the full entity
	 * 
	 * @param Set of PubMed IDs
	 * @return List of publications (Abstract)
	 */
	public Set<Abstract> parsePMIDs ( Set<String> ids ) throws MalformedURLException, IOException, XMLStreamException 
	{
		// PubMED connections are hectic and need to be tried multiple times
    MultipleAttemptsExecutor attempter = new MultipleAttemptsExecutor ( 5, 500, 5000, IOException.class );

		final Set<Abstract> allAbstracts = Collections.synchronizedSet ( new HashSet<>() );

		HttpURLConnection.setFollowRedirects(false);
		HttpURLConnection.setDefaultAllowUserInteraction(false);

		List<String> idList = new ArrayList<> ( ids );
		
		// e-fetch receives comma-separated lists of IDs
		Stream<String> accessionGroups = Lists
		.partition ( idList, 100 ) // groups of 100 IDs
		.stream ()
		.map ( idGroup -> String.join ( ",", idGroup ) ); // Joined together

		
		log.info ( "Retrieving {} PubMed entries", ids.size () );
		
		PercentProgressLogger progressTracker = new PercentProgressLogger ( 
			"Retrieved {}% of PubMed entries", ids.size ()
		);
		
		boolean hasErrors[] = { false };
		int [] i = { 0 };
		accessionGroups.forEach ( accessionGroup -> 
		{
			try 
			{
				String efetchString = Parser.EFETCH_WS + accessionGroup;
				URL efetch = new URL ( efetchString );

				// As said above, this needs to be tried multiple times.
				attempter.executeChecked ( () -> 
				{
					HttpURLConnection httpConn = (HttpURLConnection) efetch.openConnection();
					httpConn.setConnectTimeout( 10*60*1000 );
		
					// Wrap certain elements with CDATA			
					InputStream xmlin = XmlFilterUtils.cdataWrapper ( httpConn.getInputStream(), CDATA_ELEMENTS );
					XMLStreamReader2 staxXmlReader = (XMLStreamReader2) factory.createXMLStreamReader( xmlin );
					Set<Abstract> thisResult = this.parseMedlineXML ( staxXmlReader );
					allAbstracts.addAll ( thisResult );
					progressTracker.updateWithIncrement ( thisResult.size () );
				});
			}
			catch ( Exception ex ) 
			{
				log.error ( 
					"Exception while parsing PMIDs via e-fetch:" + ex.getMessage (),
					ex
				);
				hasErrors [ 0 ] = true;
			}
		}); // PMID loop
		
		if ( hasErrors [ 0 ] ) throw new XMLStreamException (
			"Errors while parsing multiple PMIDs, see the log file for details" 
		);

		return allAbstracts;
	}

	/**
	 * Reads one single MEDLINE XML file. Parses for MedlineCitation and 
	 * return a set.
	 * 
	 * @param file -
	 *            MEDLINE XML file to be parsed
	 * @param importsession -
	 *            defining abstracts of interest
	 * @return Array of matching abstracts (Abstract)
	 */
	private Set<Abstract> parseMedlineXML ( XMLStreamReader2 staxXmlReader ) throws IOException, XMLStreamException
	{
		Set<Abstract> medlineCitationSet = new HashSet<>();
		
		while ( staxXmlReader.hasNext() )
		{
			// skip DOCTYPE
			/*if(staxXmlReader.getEventType() == XMLStreamConstants.DTD) { System.out.println("skip DOCTYPE..."); staxXmlReader.nextTag(); } */

			if ( staxXmlReader.next() != XMLStreamConstants.START_ELEMENT ) continue;
			if ( !matchXMLElem ( staxXmlReader, MEDLINE_CIT ) ) continue;
			
			Abstract medlineCit = parseMedlineCitation ( staxXmlReader );
			medlineCitationSet.add ( medlineCit );
		}
		staxXmlReader.close();
		return medlineCitationSet;
	}
	
	
	public Abstract parseMedlineCitation ( XMLStreamReader2 staxXmlReader )
		throws NumberFormatException, XMLStreamException
	{
		Abstract medlineCitation = new Abstract();
		
		// PMID element expected
		staxXmlReader.nextTag();
		
		//	<!ELEMENT	PMID (#PCDATA)>
		int pmid = parseTextNode ( staxXmlReader, PMID, 
			s -> {
				try {
					return Integer.parseInt ( s );
				}
				catch ( NumberFormatException ex ) {
					throw buildEx ( ex, "Cannot parse the XML \"%s\" to a PMID: %s", s, ex );
				}
		});
		medlineCitation.setID ( pmid );

		try 
		{
			while ( staxXmlReader.hasNext() ) 
			{
				int event = staxXmlReader.next();
	
				if ( event == XMLStreamConstants.START_ELEMENT )
				{
					parseTitleFrag ( staxXmlReader, medlineCitation );
					parseAbstractFrag ( staxXmlReader, medlineCitation );
					parseJournalFrag ( staxXmlReader, medlineCitation );
					parseAuthorFrag ( staxXmlReader, medlineCitation );
					parseJournalTitleFrag ( staxXmlReader, medlineCitation ); 
					parseChemFrag ( staxXmlReader, medlineCitation );
					parseMESHFrag ( staxXmlReader, medlineCitation );
				}
				else if ( event == XMLStreamConstants.END_ELEMENT && matchXMLElem ( staxXmlReader, MEDLINE_CIT ) )
					break;
			} // while staxXmlReader
		}
		catch ( XMLStreamException ex ) {
			throwEx ( 
				ex, 
				"Error while parsing PMID:%s: %s", 
				medlineCitation.getId (), ex.getMessage () 
			);
		}
		catch ( RuntimeException ex ) {
			throwEx ( 
				ex, 
				"Error while parsing PMID:%s: %s", 
				medlineCitation.getId (), ex.getMessage () 
			);
		}

		return medlineCitation;
	}
	
	
	private void parseTitleFrag ( XMLStreamReader2 staxXmlReader, Abstract medlineCitation ) throws XMLStreamException
	{
		parseTextNodeVoid ( staxXmlReader, TITLE, medlineCitation::setTitle );
	}

	private void parseAbstractFrag ( XMLStreamReader2 staxXmlReader, Abstract medlineCitation ) throws XMLStreamException
	{
		if ( !matchXMLElem ( staxXmlReader, ABSTRACT ) ) return;

		//				<!ELEMENT	Abstract (AbstractText+,CopyrightInformation?)>
		//				<!ELEMENT	AbstractText (#PCDATA)>
		//				<!ATTLIST       AbstractText
		//				                Label CDATA #IMPLIED
		//				                NlmCategory (UNLABELLED | BACKGROUND | OBJECTIVE | METHODS |
		//				                             RESULTS | CONCLUSIONS) #IMPLIED>

		String text = parseAbstractText(staxXmlReader);
		medlineCitation.setBody(text);	
	}
	
	private String parseAbstractText ( XMLStreamReader2 staxXmlReader ) throws XMLStreamException
	{
		String out = "";
		while(staxXmlReader.hasNext()){
            int event = staxXmlReader.next();
            if (event == XMLStreamConstants.START_ELEMENT
                    && staxXmlReader.getLocalName().equals(ABSTRACTTXT)) {
            	
				String text = staxXmlReader.getElementText().trim();
				text = tabPattern.matcher(text).replaceAll(" ");
				out += text+" ";
            	
            }else if (event == XMLStreamConstants.END_ELEMENT
                    && staxXmlReader.getLocalName().equals(ABSTRACT)) {
                break;
            }
			
		}
		
		return out;
	}
	

	private void parseJournalFrag ( XMLStreamReader2 staxXmlReader, Abstract medlineCitation ) throws XMLStreamException
	{
		if ( !matchXMLElem ( staxXmlReader, "Journal" ) ) return;

		// Journal/JournalIssue/*		
		
		String trackedElem = "Journal";
		String element = "Journal";
		
		while ( staxXmlReader.hasNext() )
		{
			int event = staxXmlReader.next ();
			if ( event == XMLStreamConstants.END_ELEMENT && "Journal".equals ( element ) ) break;
			if ( event != XMLStreamConstants.START_ELEMENT ) continue;

			element = staxXmlReader.getLocalName ();						
									
			if ( "Journal".equals ( trackedElem ) && "JournalIssue".equals ( element ) )
				trackedElem = element;
			
			if ( "JournalIssue".equals ( trackedElem ) && "PubDate".equals ( element ) )
				trackedElem = element;
			
			if ( "PubDate".equals ( trackedElem ) )
			{
				Integer pubYear = null;

				// We can have either a Y/M/D date or a string element
				if ( "Year".equals ( element ) )
					pubYear = Integer.parseInt ( staxXmlReader.getElementText() );
				else if ( "MedlineDate".equals ( element ) ) 
				{
					pubYear = -1;
					
					// In case of string, we still hope to get the year from the string begin
					String dateStr = StringUtils.trimToNull ( staxXmlReader.getElementText () );
					if ( dateStr != null ) 
					{
						Matcher rem = MEDLINE_YEAR_RE.matcher ( dateStr );
						if ( rem.matches () )
							pubYear = Integer.parseInt ( rem.group ( 1 ) );
					}
				}
				if ( pubYear != null )
				{								
					// TODO: check for a sensible range, like >=1800 ?
					if ( pubYear >= 0 )	medlineCitation.setYear ( pubYear );

					// If non-null, we have met the relevant Year/Medline tags, so we can stop
					break;
				}
			} // if PubDate
		} // while on Journal		
	}
	
	private void parseAuthorFrag ( XMLStreamReader2 staxXmlReader, Abstract medlineCitation ) throws XMLStreamException
	{
		if ( !matchXMLElem ( staxXmlReader, AUTHOR ) ) return;
		
		//				<!ELEMENT	Author (((LastName, ForeName?, Initials?, Suffix?) | 
		//                        CollectiveName),NameID*)>
		staxXmlReader.nextTag();
		String lastName = staxXmlReader.getElementText();
		staxXmlReader.nextTag();
		String foreName = matchXMLElem ( staxXmlReader, "ForeName" )
			? staxXmlReader.getElementText() : "";
		medlineCitation.addAuthors(foreName+" "+lastName);
	}
	
	private void parseJournalTitleFrag ( XMLStreamReader2 staxXmlReader, Abstract medlineCitation ) throws XMLStreamException
	{
		//				<!ELEMENT	Title (#PCDATA)>
		parseTextNodeVoid ( staxXmlReader, JOURNAL_TITLE, medlineCitation::setJournal );
	}
	
	private void parseChemFrag ( XMLStreamReader2 staxXmlReader, Abstract medlineCitation ) throws XMLStreamException
	{
		if ( !matchXMLElem ( staxXmlReader, CHEM ) ) return;
		
		//				<!ELEMENT	Chemical (RegistryNumber,NameOfSubstance)>
		staxXmlReader.nextTag();
		String chemicalRegNum = staxXmlReader.getElementText();
		staxXmlReader.nextTag();
		String chemicalName = staxXmlReader.getElementText();
		
		if(chemicalRegNum.equals("0"))
			medlineCitation.addChemical(chemicalName);
		else
			medlineCitation.addChemical(chemicalRegNum+" "+chemicalName);
	}
	
	private void parseMESHFrag ( XMLStreamReader2 staxXmlReader, Abstract medlineCitation ) throws XMLStreamException
	{
		//	<!ELEMENT	MeshHeading (DescriptorName, QualifierName*)>
		parseTextNodeVoid ( staxXmlReader, MESHDESCR, medlineCitation::addMeSH );
	}
	
	/**
	 * Extracts the text value of a given XML element and passes it to the consumer.
	 * @return the value that the consumer has mapped from the text node. 
	 */
	private <T> T parseTextNode ( 
		XMLStreamReader2 staxXmlReader, String targetElemName, Function<String, T> consumer
	) throws XMLStreamException
	{
		if ( !matchXMLElem ( staxXmlReader, targetElemName ) ) return null;
		String value = staxXmlReader.getElementText();
		return consumer.apply ( value );
	}

	/**
	 * A wrapper of {@link #parseTextNode(XMLStreamReader2, String, Function)} to be used when you don't have
	 * transformed value to return from the text node.
	 * 
	 * @return the string value of the XML node instead.
	 *  
	 */
	private String parseTextNodeVoid ( 
			XMLStreamReader2 staxXmlReader, String targetElemName, Consumer<String> consumer
		) throws XMLStreamException
	{
		return parseTextNode ( staxXmlReader, targetElemName, v -> { consumer.accept ( v ); return v; } );
	}
	
	/**
	 * Facility to tell if an XML element is of the given type. Assumes the reader is in the
	 * START_ELEMENT or END_ELEMENT state.
	 */
	private boolean matchXMLElem ( XMLStreamReader2 staxXmlReader, String targetElemName )
	{
		return staxXmlReader.getLocalName().equals( targetElemName );
	}
}
