package net.sourceforge.ondex.parser.medline;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.EmptyStringEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.NullValueEvent;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.medline.data.Abstract;

/**
 * A class for writing concepts to the ONDEX backend
 * based on the data collected in Abstract objects.
 * 
 * @author keywan
 *
 */
public class AbstractWriter {
	
	private ONDEXGraph graph;
	
	private Map<Integer, Integer> pubmeds = new HashMap<Integer, Integer>(3*(10*6));

	private final static String BLANK = "";
	
	private DataSource dataSourceNLM;
	private EvidenceType et;
	private ConceptClass ccPublication;
	private AttributeName gds_abstract;
	private AttributeName gds_header;
	private AttributeName gds_authors;
	private AttributeName gds_year;
	private AttributeName gds_doi;
	private AttributeName gds_mesh;
	private AttributeName gds_chemicals;
	private AttributeName gds_journal;
	
	private int abstractsWritten = 0;
	private int headersWritten = 0;


	
	/**
	 * Writes the data of one publication to the ONDEX backend.
	 * 
	 * @param gr = ONDEXGraph
	 */
	public AbstractWriter(ONDEXGraph gr) throws MetaDataMissingException {
		this.graph = gr;
		
		ONDEXGraphMetaData metaData = graph.getMetaData();
		
		if (metaData.checkDataSource(MetaData.CV_NLM)) {
			dataSourceNLM = metaData.getDataSource(MetaData.CV_NLM);
		} else {
			DataSourceMissingEvent so = new DataSourceMissingEvent(MetaData.CV_NLM + " missing.", Parser.getCurrentMethodName());
			Parser.propagateEventOccurred(so);
            throw new DataSourceMissingException("DataSource " + MetaData.CV_NLM + " missing. Should have come from ondex_metadata.xml");
		}
		
		if (metaData.checkEvidenceType(MetaData.ET_IMPD)) {
			et = metaData.getEvidenceType(MetaData.ET_IMPD);
		}else {
			EvidenceTypeMissingEvent so = new EvidenceTypeMissingEvent(MetaData.ET_IMPD + " missing.", Parser.getCurrentMethodName());
			Parser.propagateEventOccurred(so);
            throw new EvidenceTypeMissingException("EvidenceType " + MetaData.ET_IMPD + " missing. Should have come from ondex_metadata.xml");
		}
		
		if (metaData.checkConceptClass(MetaData.CC_PUBLICATION)) {
			ccPublication = metaData.getConceptClass(MetaData.CC_PUBLICATION);
		} else {
			ConceptClassMissingEvent so = new ConceptClassMissingEvent(MetaData.CC_PUBLICATION + " missing.", Parser.getCurrentMethodName());
			Parser.propagateEventOccurred(so);
            throw new ConceptClassMissingException("ConceptClass " + MetaData.CC_PUBLICATION + " missing. Should have come from ondex_metadata.xml");
		}
		
		if (metaData.checkAttributeName(MetaData.ATT_NAME_ABSTRACT)) {
			gds_abstract = metaData.getAttributeName(MetaData.ATT_NAME_ABSTRACT);
		} else {
			AttributeNameMissingEvent so = new AttributeNameMissingEvent(MetaData.ATT_NAME_ABSTRACT + " missing.", Parser.getCurrentMethodName());
			Parser.propagateEventOccurred(so);
            throw new AttributeNameMissingException("AttributeName " + MetaData.ATT_NAME_ABSTRACT + " missing. Should have come from ondex_metadata.xml");
		}
		
		if (metaData.checkAttributeName(MetaData.ATT_NAME_ABSTRACT_HEADER)) {
			gds_header = metaData.getAttributeName(MetaData.ATT_NAME_ABSTRACT_HEADER);
		} else {
			AttributeNameMissingEvent so = new AttributeNameMissingEvent(MetaData.ATT_NAME_ABSTRACT_HEADER + " missing.", Parser.getCurrentMethodName());
			Parser.propagateEventOccurred(so);
            throw new AttributeNameMissingException("AttributeName " + MetaData.ATT_NAME_ABSTRACT + " missing. Should have come from ondex_metadata.xml");
		}
		
		if (metaData.checkAttributeName(MetaData.ATT_NAME_AUTHORS)) {
			gds_authors = metaData.getAttributeName(MetaData.ATT_NAME_AUTHORS);
		} else {
			AttributeNameMissingEvent so = new AttributeNameMissingEvent(MetaData.ATT_NAME_AUTHORS + " missing.", Parser.getCurrentMethodName());
			Parser.propagateEventOccurred(so);
            throw new AttributeNameMissingException("AttributeName " + MetaData.ATT_NAME_AUTHORS + " missing. Should have come from ondex_metadata.xml");
		}
		
		if (metaData.checkAttributeName(MetaData.ATT_NAME_DOI)) {
			gds_doi = metaData.getAttributeName(MetaData.ATT_NAME_DOI);
		} else {
			AttributeNameMissingEvent so = new AttributeNameMissingEvent(MetaData.ATT_NAME_DOI + " missing.", Parser.getCurrentMethodName());
			Parser.propagateEventOccurred(so);
            throw new AttributeNameMissingException("AttributeName " + MetaData.ATT_NAME_DOI + " missing. Should have come from ondex_metadata.xml");
		}
		
		if (metaData.checkAttributeName(MetaData.ATT_NAME_MESH)) {
			gds_mesh = metaData.getAttributeName(MetaData.ATT_NAME_MESH);
		} else {
			AttributeNameMissingEvent so = new AttributeNameMissingEvent(MetaData.ATT_NAME_MESH + " missing.", Parser.getCurrentMethodName());
			Parser.propagateEventOccurred(so);
            throw new AttributeNameMissingException("AttributeName " + MetaData.ATT_NAME_MESH + " missing. Should have come from ondex_metadata.xml");
		}
		
		if (metaData.checkAttributeName(MetaData.ATT_NAME_CHEMICAL)) {
			gds_chemicals = metaData.getAttributeName(MetaData.ATT_NAME_CHEMICAL);
		} else {
			AttributeNameMissingEvent so = new AttributeNameMissingEvent(MetaData.ATT_NAME_CHEMICAL + " missing.", Parser.getCurrentMethodName());
			Parser.propagateEventOccurred(so);
            throw new AttributeNameMissingException("AttributeName " + MetaData.ATT_NAME_CHEMICAL + " missing. Should have come from ondex_metadata.xml");
		}
		
		if (metaData.checkAttributeName(MetaData.ATT_NAME_YEAR)) {
			gds_year = metaData.getAttributeName(MetaData.ATT_NAME_YEAR);
		} else {
			AttributeNameMissingEvent so = new AttributeNameMissingEvent(MetaData.ATT_NAME_YEAR + " missing.", Parser.getCurrentMethodName());
			Parser.propagateEventOccurred(so);
            throw new AttributeNameMissingException("AttributeName " + MetaData.ATT_NAME_YEAR + " missing. Should have come from ondex_metadata.xml");
		}
		if (metaData.checkAttributeName(MetaData.ATT_NAME_JOURNAL)) {
			gds_journal = metaData.getAttributeName(MetaData.ATT_NAME_JOURNAL);
		} else {
			AttributeNameMissingEvent so = new AttributeNameMissingEvent(MetaData.ATT_NAME_JOURNAL + " missing.", Parser.getCurrentMethodName());
			Parser.propagateEventOccurred(so);
            throw new AttributeNameMissingException("AttributeName " + MetaData.ATT_NAME_JOURNAL + " missing. Should have come from ondex_metadata.xml");
		}
	}
	
	
	/**
	 * Writes a collection of Abstracts to the ONDEX backend
	 * 
	 * @param pabstract Collection of Abstracts
	 */
	public void writeConcepts(Collection<Abstract> pabstract) {
		
		Iterator<Abstract> abIt = pabstract.iterator();
		while (abIt.hasNext()) {
			writeConcept(abIt.next());
		}
	}
	
	
	/**
	 * Writes the data of one Abstract object to the ONDEX backend.
	 * 
	 * Creates for each Abstract a concept of type Publication.
	 * Stores the abstract's titel, body, authors, year, mesh terms, chemicals, doi as conceptGDS.
	 * 
	 * @param pabstract -
	 *            the Abstract object
	 */
	private void writeConcept(Abstract pabstract) {
		
		int cId = pabstract.getId();
		if (cId < 0) {
			NullValueEvent so = new NullValueEvent("Missing PMID, ignoring this publication", Parser.getCurrentMethodName());
			Parser.propagateEventOccurred(so);
			return;
		}
		
		//delete old entry, which has been created with this PubMed ID before
		if (pubmeds.containsKey(cId)) {
			graph.deleteConcept(pubmeds.get(cId));
			pubmeds.remove(cId);
		}
		
		//write abtract only if it is not marked as deleted
		if (!pabstract.isDeleted()) {
			
			String pmid = "NO_PMID";
			
			if (cId > 0) pmid = String.valueOf(cId);
			
			ONDEXConcept c = graph.getFactory().createConcept(pmid, BLANK, BLANK, dataSourceNLM, ccPublication, et);
			c.createConceptAccession(pmid, dataSourceNLM, false);
			c.createConceptName("PMID:"+pmid, true);
			
			// track new ids to orignial ids
			if (cId > 0) pubmeds.put(cId, c.getId());
			
			
			// write title into gds
			if (pabstract.getTitle() != null) {
				c.createAttribute(gds_header, pabstract.getTitle(), true);
				headersWritten++;
			}else {
				EmptyStringEvent so = new EmptyStringEvent("Missing abstract title "+c.getId(), Parser.getCurrentMethodName());
				Parser.propagateEventOccurred(so);
			}
			
			// write abstract into gds
			if (pabstract.getBody() != null) {
				c.createAttribute(gds_abstract, pabstract.getBody(), true);
				abstractsWritten++;
			}
			
			// write the list of authors into gds
			if (pabstract.getAuthors() != null) {
				c.createAttribute(gds_authors, pabstract.getAuthors(), false);
			}
			
			// write the journal title into gds
			if (pabstract.getJournal() != null) {
				c.createAttribute(gds_journal, pabstract.getJournal(), false);
			}
			
			// write the Date created into gds
			if (pabstract.getYear() > 0) {
				c.createAttribute(gds_year, pabstract.getYear(), false);
			}
			
			//write the DIO number into gds
			if (pabstract.getDoi() != null) {
				c.createAttribute(gds_doi, pabstract.getDoi(), false);
			}
			
			//	write all MeSH terms which could be parsed for this abstract
			if (pabstract.getMeSHs().size() > 0) {
				c.createAttribute(gds_mesh, pabstract.getMeSHs().toString(), false);
			}
			
			//	write all chemicals which could be parsed for this abstract
			if (pabstract.getChemicals().size() > 0) {
				c.createAttribute(gds_chemicals, pabstract.getChemicals().toString(), false);
			}

			pabstract.finalize();
		}
		
	}
	
	
	public int getAbstractsWritten() {
		return abstractsWritten;
	}
	
	public int getHeadersWritten() {
		return headersWritten;
	}
	
	
	public void finalize() {
		pubmeds = null;
		dataSourceNLM = null;
		et = null;
		ccPublication = null;
		gds_abstract = null;
		gds_header = null;
	}
	
	
}
