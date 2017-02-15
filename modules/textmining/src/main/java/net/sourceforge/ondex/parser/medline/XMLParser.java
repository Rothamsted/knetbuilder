package net.sourceforge.ondex.parser.medline;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipFile;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.parser.medline.data.Abstract;
import net.sourceforge.ondex.tools.ziptools.ZipEndings;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import com.ctc.wstx.stax.WstxInputFactory;

public class XMLParser {

	private WstxInputFactory factory;

	private final static Pattern tabPattern = Pattern.compile("[\\t|\\n|\\r]");

	public XMLParser() {
		System.setProperty("javax.xml.stream.XMLInputFactory",
				"com.ctc.wstx.stax.WstxInputFactory");
		factory = (WstxInputFactory) WstxInputFactory.newInstance();
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
/*
	private final static String textType = "0";

	private final static String EC = "EC";

	private final static String CAS = "CAS";

	private final static String TEXT = "TEXT";
*/

	private final static String PUBMED_CIT = "PubmedArticle";

	private final static String MEDLINE_CIT = "MedlineCitation";

	private final static String DEL_CIT = "DeleteCitation";

	private final static String PMID = "PMID";

	private final static String TITLE = "ArticleTitle";

	private final static String ABSTRACTTXT = "AbstractText";
	
	private final static String AUTHORLIST = "AuthorList";
	
	private final static String AUTHOR = "Author";
	
	private final static String JOURNAL = "Journal";
	
	private final static String JOURNAL_TITLE = "Title";

	private final static String CHEM = "Chemical";
	
	private final static String DATE = "DateCreated";

	private final static String REG_NUM = "RegistryNumber";

	private final static String SUBS_NAME = "NameOfSubstance";

	private final static String MESH = "MeshHeading";

	private final static String MESHDESCR = "DescriptorName";

	private final static String ARTICLEID = "ArticleId";
	

	/**
	 * Reads one single MEDLINE XML file. Parses for Abstracts by PubMed IDs as
	 * specified in given Import Session. Return all matching abstracts in an
	 * ArrayList.
	 * 
	 * @param file -
	 *            MEDLINE XML file to be parsed
	 * @param importsession -
	 *            defining abstracts of interest
	 * @return Array of matching abstracts (Abstract)
	 */
	public List<Abstract> parseMEDLINE(String file, ImportSession importsession) throws IOException, XMLStreamException {

//		System.out.println("Parsing file: " + file);
		List<Abstract> abstracts = new ArrayList<Abstract>(35000);
		
			XMLStreamReader2 staxXmlReader;

			// detecting whether input file is zipped or not
			int detectedEnding = ZipEndings.getPostfix(file);

			switch (detectedEnding) {

			case ZipEndings.GZ:
				GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(file));
				staxXmlReader = (XMLStreamReader2) factory.createXMLStreamReader(gzis);
				break;
			case ZipEndings.ZIP:
				ZipFile zipFile = new ZipFile(file);
				if (zipFile.size() > 1) {
					System.err.println("There are multiple files in this zip file: can not parse");
				}
				staxXmlReader = (XMLStreamReader2) factory
						.createXMLStreamReader(zipFile.getInputStream(zipFile
								.entries().nextElement()));
				break;
			default:
				staxXmlReader = factory.createXMLStreamReader(new File(file));
			}

			abstracts = this.parse(staxXmlReader, importsession);
		return abstracts;

	}

	/**
	 * Reads one single MEDLINE XML file. Parses for Abstracts by PubMed IDs as
	 * specified in given Import Session. Return all matching abstracts in an
	 * ArrayList.
	 * 
	 * @param file -
	 *            MEDLINE XML file to be parsed
	 * @param importsession -
	 *            defining abstracts of interest
	 * @return Array of matching abstracts (Abstract)
	 */
	public List<Abstract> parsePUBMED(HashSet<String> ids,
			ImportSession importsession) throws MalformedURLException, IOException, XMLStreamException {

		System.out.println("Parsing xml online for " + ids.size() + " IDs..");
		List<Abstract> allabstracts = new ArrayList<Abstract>(
				35000);
		

			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection.setDefaultAllowUserInteraction(false);

			XMLStreamReader2 staxXmlReader;

			String accession = "";

			String id;

			int threshold = 100;
			int limit = 1;

			ArrayList<String> accsessions = new ArrayList<String>();
			
			Iterator<String> idsIt = ids.iterator();

			while (idsIt.hasNext()) {

				limit++;

				id = idsIt.next();
				accession = accession + id.trim() + ",";
				
				// create packages of 100 comma separeted PMIDs
				if (limit == threshold) {

					accession = accession.substring(0, accession.length() - 1);
					accsessions.add(accession);
					accession = "";
					limit = 1;
				}
			}

			if (limit != 1) {

				accession = accession.substring(0, accession.length() - 1);
				accsessions.add(accession);
			}

			Iterator<String> it = accsessions.iterator();
			int i = 0;

			while (it.hasNext()) {

				i++;
				List<Abstract> abstracts = new ArrayList<Abstract>(
						35000);

				accession = it.next();

				String efetchString = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id="
						+ accession + "&retmode=xml";
				
				URL efetch = new URL(efetchString);
				HttpURLConnection httpConn = (HttpURLConnection) efetch.openConnection();
				httpConn.setConnectTimeout(0);

				staxXmlReader = (XMLStreamReader2) factory
						.createXMLStreamReader(httpConn.getInputStream());
				abstracts = this.parse(staxXmlReader, importsession);
				allabstracts.addAll(abstracts);
				
				if(i % 10 == 0){
					System.out.println("Retrieved "+ i * 100 + " out of "+ids.size()+" PMIDs.");
				}
			}

		return allabstracts;

	}

	/**
	 * Reads one single MEDLINE XML file. Parses for Abstracts by PubMed IDs as
	 * specified in given Import Session. Return all matching abstracts in an
	 * ArrayList.
	 * 
	 * @param file -
	 *            MEDLINE XML file to be parsed
	 * @param importsession -
	 *            defining abstracts of interest
	 * @return Array of matching abstracts (Abstract)
	 */
	private List<Abstract> parse(XMLStreamReader2 staxXmlReader,
			ImportSession importsession) throws IOException, XMLStreamException {

		List<Abstract> abstracts = new ArrayList<Abstract>(35000);

		// containing all information that can be parsed from XML
		Abstract pabstract = null;

		// is it pubmed or medline xml?
		boolean pubmed = false;

		// delete flag. will be set if <PMID> tag is within <DeleteCitation> tag
		boolean delete = false;

		// flag wich will be set, once a valid PMID could be parsed for the
		// given citation
		boolean iDAssigned = false;

		// flag set, once the chemical section for given citation has been
		// entered
		boolean chemical = false;

		// flag set, once the mesh section for given citation has been entered
		boolean mesh = false;
		
		// flag set, once the journal section for given citation has been entered
		boolean journal = false;
		
		boolean pubdate = false;
		
		boolean isAuthorList = false;
		boolean isAuthor = false;
		boolean extractDOI = false;

		String chemicalType = null;
		String chemicalId = null;
		String meshDescr = null;
		String lastName = "";
		String foreName = "";
		String authors = "";
		String journalTitle = "";
		int pubYear = 0;
		

		while (staxXmlReader.hasNext()) {

			int event = staxXmlReader.next();
			String element;

			switch (event) {

			case XMLStreamConstants.START_DOCUMENT:
				break;

			case XMLStreamConstants.START_ELEMENT:
				element = staxXmlReader.getLocalName();

				// a Pubmed citation starts
				if (element.equals(PUBMED_CIT)) {
					pabstract = new Abstract();
					delete = false;
					iDAssigned = false;
					pubmed = true;
				}

				// a MEDLINE citation starts
				if (element.equals(MEDLINE_CIT)) {
					pabstract = new Abstract();
					delete = false;
					iDAssigned = false;
				}

				// a Delete section starts here
				if (element.equals(DEL_CIT)) {
					delete = true;
				}

				// found a PubMed ID
				if (element.equals(PMID)) {

					// could be part of a MEDLINE citation
					if (!delete && !iDAssigned) {
						pabstract.setID(Integer.parseInt((staxXmlReader.getElementText())));
						iDAssigned = true;
					}

					// could be part of a DeleteCitation area
					if (delete) {
						pabstract = null;
						pabstract = new Abstract();
						pabstract.setID(Integer.parseInt((staxXmlReader.getElementText())));
						pabstract.setDelete(true);
						break;
					}
				}

				if (element.equals(TITLE)) {
					pabstract.setTitle(staxXmlReader.getElementText());
				}

				if (element.equals(ABSTRACTTXT)) {
					String text = staxXmlReader.getElementText().trim();
					
//					String keyword = "Arabidopsis thaliana";
//					if(!text.contains(keyword)){
//						pabstract.setDelete(true);
//					}

					// look for doi accession, which is (if available)
					// at the end of the abstract
					
					if(extractDOI){
						if (text.indexOf("doi:") > -1) {
							// System.out.println(text);
							String doi = text.substring(text.indexOf("doi:") + 4);
							if (doi.indexOf("</Ab") > -1) {
								doi = doi.substring(0, doi.indexOf("</Ab"));
							}
							if (doi.endsWith(".")) {
								// System.out.println("DOI ends with .:"+doi);
								doi = doi.substring(0, doi.length() - 1);
							}
							pabstract.setDoi(doi);
						}
					}

					text = tabPattern.matcher(text).replaceAll(" ");
					if (text.length() > 0)
						pabstract.setBody(text);
				}
				
				if (element.equals(DATE)){
					pubdate = true;
				}
				
				if (pubdate && element.equals("Year")){
					pubYear = Integer.parseInt(staxXmlReader.getElementText());
					pabstract.setYear(pubYear);
				}
				
				if (element.equals(AUTHORLIST)){
					isAuthorList = true;
				}
				
				if (isAuthorList && element.equals(AUTHOR)){
					isAuthor = true;
				}
				
				if (isAuthor && element.equals("LastName")){
					lastName = staxXmlReader.getElementText();
				}
				
				if (isAuthor && element.equals("ForeName")){
					foreName = staxXmlReader.getElementText();
				}
				
				if(element.equals(JOURNAL)){
					journal = true;
				}
				
				if(journal && element.equals(JOURNAL_TITLE)){
					journalTitle = staxXmlReader.getElementText();
					pabstract.setJournal(journalTitle);
				}

				if (element.equals(CHEM)) {
					chemical = true;
				}

				if (element.equals(MESH)) {
					mesh = true;
				}

				if (mesh && element.equals(MESHDESCR)) {
					meshDescr = staxXmlReader.getElementText();
				}

				if (chemical && element.equals(REG_NUM)) {
					chemicalType = staxXmlReader.getElementText();
				}

				if (chemical && element.equals(SUBS_NAME)) {
					chemicalId = staxXmlReader.getElementText();
				}

				if (chemical && chemicalType != null && chemicalId != null) {
					/*
					String DataSource;
					if (chemicalType.equals(textType)) {
						DataSource = TEXT;
					} else {
						if (chemicalId.startsWith(EC)) {
							DataSource = EC;
							chemicalId = chemicalId.substring(3, chemicalId.length());
						} else {
							DataSource = CAS;
						}
					}*/
					pabstract.addChemical(chemicalId);
				}

				if (element.equals(ARTICLEID)) {

					int count = staxXmlReader.getAttributeCount();

					for (int i = 0; i < count; i++) {

//						System.out.println("DOI:"+ staxXmlReader.getAttributeName(i).getLocalPart());

						if (staxXmlReader.getAttributeName(i).getLocalPart()
								.equalsIgnoreCase("IdType")) {

							if (staxXmlReader.getAttributeValue(i).equals("doi")) {
								String doi = staxXmlReader.getElementText();
								pabstract.setDoi(doi);
							}
						}
					}
				}

				break;

			case XMLStreamConstants.END_ELEMENT:
				element = staxXmlReader.getLocalName();
				
//				if(element.equals("Article")){ 
//					if(pabstract.getBody() == null){
//						pabstract.setDelete(true);
//					}
//				}
						

				if (!pubmed && element.equals(MEDLINE_CIT)) {

					if (importsession.applyFilter(pabstract) && !delete) {
						abstracts.add(pabstract);
					}
					pabstract = null;
				}

				if (element.equals(PUBMED_CIT)) {

					abstracts.add(pabstract);

					pabstract = null;
					pubmed = false;
				}

				if (element.equals(PMID)) {

					// if part of the delete section:
					// add abstract which is marked as to be deleted
					if (delete) {
						abstracts.add(pabstract);
						pabstract = null;
						break;
					}
				}

				if (element.equals(DEL_CIT)) {
					delete = false;
				}
				
				if (element.equals(DATE)){
					pubdate = false;
					pubYear = 0;
				}
				
				if(element.equals(JOURNAL)){
					journal = false;
					journalTitle = "";
				}
				
				if (element.equals(AUTHOR)){
					authors += foreName+" "+lastName+"; ";
					isAuthor = false;
					foreName = "";
					lastName = "";
				}
				
				if (element.equals(AUTHORLIST)){
					pabstract.setAuthors(authors.substring(0, authors.lastIndexOf(";")));
					isAuthorList = false;
					authors = "";
				}

				if (element.equals(CHEM)) {
					chemical = false;
					chemicalType = null;
					chemicalId = null;
				}

				if (element.equals(MESH)) {

					if (mesh && meshDescr != null) {
						pabstract.addMeSH(meshDescr);
					}
					mesh = false;
					meshDescr = null;
				}

				break;

			case XMLStreamConstants.END_DOCUMENT:
				staxXmlReader.close();
				break;

			default:
				break;
			}
		}

		staxXmlReader.close();
		return abstracts;
	}

}
