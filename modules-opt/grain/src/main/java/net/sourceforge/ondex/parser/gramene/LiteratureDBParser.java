package net.sourceforge.ondex.parser.gramene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.gramene.XrefParser.AccessionReference;

/**
 * Definition of Reference table in definition.txt
 * 0 `reference_id` int(11) NOT NULL default '0' PRIMARY KEY,
 * 1 `source_id` int(11) default NULL,
 * 2 `title` text NOT NULL,
 * 3 `volume` varchar(100) default NULL,
 * 4 `year` int(11) default NULL,
 * 5 `start_page` varchar(20) default NULL,
 * 6 `end_page` varchar(20) default NULL,
 * 7 `language` varchar(50) default NULL,
 * 8 `corresponding_author` int(11) default NULL,
 * 
 * Parser for creating Publication concepts from the gramene literature database
 * 
 * @author hindlem
 *
 */
public class LiteratureDBParser {


	private ONDEXGraph graph;

	private HashMap<Integer, Integer> publicationIdToConceptId = new HashMap<Integer, Integer>();
	
	/**
	 * 
	 * @param s current session
	 * @param graph current graph
	 */
	public LiteratureDBParser(ONDEXGraph graph) {

		this.graph = graph;
	}
	
	private static final String XREF_FILE = "dbxref.txt";
	private static final String REF2OBJECT_FILE = "objectxref.txt";
	
	private static final String LIT_FILE = "reference.txt";
	
	/**
	 * 
	 * @param dir the directory containing gramene literature db
	 */
	public void parseLiterature(String dir) {
		
		XrefParser xrefs = new XrefParser(dir+File.separator+XREF_FILE);
		Object2XrefParser object2xrefs = new Object2XrefParser(dir+File.separator+REF2OBJECT_FILE, "gramene.literature");
		
		EvidenceType etIMPD = graph.getMetaData().getEvidenceType(MetaData.IMPD);
		Parser.checkCreated(etIMPD, MetaData.IMPD);
		
		ConceptClass ccPub = graph.getMetaData().getConceptClass(MetaData.PUBLICATION);
		Parser.checkCreated(ccPub, MetaData.PUBLICATION);
		
		DataSource elementOfGRAMENE = graph.getMetaData().getDataSource(MetaData.gramene);
		Parser.checkCreated(elementOfGRAMENE, MetaData.gramene);
		
		try {
			Pattern tabPattern = Pattern.compile("\t");
//			Pattern nonNumerical = Pattern.compile("[^0-9]");
			
			BufferedReader input = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(dir+File.separator+LIT_FILE),"UTF8"));
			
			while (input.ready()) {
				String inputLine = input.readLine();
				String[] columns = tabPattern.split(inputLine);

				int id = Integer.parseInt(columns[0].trim());
//				Integer source = null;
//				
//				try {
//					source =Integer.parseInt(columns[1].trim());
//				} catch (NumberFormatException e) {
//					//ignore is \N
//				}
				
				String title = columns[2].trim();
				
//				String volume = null;
//				if (columns[3].trim().length() > 0) volume = columns[3].trim();
//				
//				String rawYear = columns[4].trim();
//				int end = rawYear.indexOf('-');
//				if (end > -1) {
//					rawYear = rawYear.substring(0, end); //take the first year in the range
//				}
//				Integer year = null;
//				if (rawYear.length() > 0) Integer.parseInt(rawYear);
//
//				
//				Integer startpage = null;
//				String startPageRaw = nonNumerical.matcher(columns[5].trim()).replaceAll("");
//				if (startPageRaw.length() > 0) startpage = Integer.parseInt(startPageRaw);
//				
//				Integer endpage = null;
//				String endPageRaw = nonNumerical.matcher(columns[6].trim()).replaceAll("");
//				if (endPageRaw.length() > 0) endpage = Integer.parseInt(endPageRaw);
//				
//				String language = null;
//				if (columns[7].trim().length() > 0) language = columns[7].trim();
//				
//				String correspondingAuthor = null;
//				if (columns.length >= 9) correspondingAuthor = columns[8].trim();
				
				ONDEXConcept publication = graph.getFactory().createConcept("PUB:"+id, elementOfGRAMENE, ccPub, etIMPD);
				
				publicationIdToConceptId.put(id, publication.getId());
				
				publication.createConceptName(title, true);
				publication.createConceptAccession(String.valueOf(id), elementOfGRAMENE, false);
				
				HashSet<Integer> xrefLinks = object2xrefs.getXrefs(id);
				if (xrefLinks != null) {
				Iterator<Integer> xrefLinksIt = xrefLinks.iterator();
				while (xrefLinksIt.hasNext()) {
					Integer link = xrefLinksIt.next();
					AccessionReference accession = xrefs.getAccessionReference(link);
					String mapping = MetaData.getMapping(accession.getDbType());
					if (mapping != null) {
						DataSource dataSource = graph.getMetaData().getDataSource(mapping);
						publication.createConceptAccession(accession.getAccession(), dataSource, false);
					}
				}
				}
			}
			input.close();
			System.out.println("created "+publicationIdToConceptId.size()+" publications");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param litId the gramene internal literature id
	 * @return the Integer id for the written publication concept
	 */
	public Integer getPublicationConcept(Integer litId) {
		return publicationIdToConceptId.get(litId);
	}
	
}
