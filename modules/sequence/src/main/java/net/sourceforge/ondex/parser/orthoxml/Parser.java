package net.sourceforge.ondex.parser.orthoxml;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.DataURL;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.annotations.metadata.AttributeNameRequired;
import net.sourceforge.ondex.annotations.metadata.DataSourceRequired;
import net.sourceforge.ondex.annotations.metadata.ConceptClassRequired;
import net.sourceforge.ondex.annotations.metadata.EvidenceTypeRequired;
import net.sourceforge.ondex.annotations.metadata.RelationTypeRequired;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.parser.ONDEXParser;

import org.codehaus.stax2.XMLInputFactory2;

import com.ctc.wstx.io.CharsetNames;

/**
 * Parser for the OrthoXML format
 * http://orthoxml.org/
 * 
 * This format is used especially by Inparanoid
 * http://inparanoid.sbc.su.se/download/current/orthoXML/
 * 
 * @author keywan
 *
 */
@Status(description = "Tested November 2010 (Keywan Hassani-Pak)", status = StatusType.EXPERIMENTAL)
@Authors(authors = {"Keywan Hassani-Pak"}, emails = {"keywan at users.sourceforge.net"})
@DatabaseTarget(name = "OrthoXML", description = "OrthoXML is designed broadly to allow the storage and comparison of orthology data from any ortholog database. This Ondex parser for OrthoXML was tested on OrthoXML files provided by Inparanoid.", version = "0.2", url = "http://orthoxml.org/")
@DataURL(name = "OrthoXML file",
        description = "This parser requires as input a single OrthoXML file. It was tested on the Arabidopsis-Rice ortholog file provided by Inparanoid:",
        urls = {"http://inparanoid.sbc.su.se/download/current/orthoXML/"})
@Custodians(custodians = {"Keywan Hassani-pak"}, emails = {"keywan at users.sourceforge.net"})
@DataSourceRequired(ids = {MetaData.cvInparanoid })
@ConceptClassRequired(ids = {MetaData.ccProtein })
@EvidenceTypeRequired(ids = {MetaData.etIMPD}) 
@RelationTypeRequired(ids = {MetaData.rtOrtholog, MetaData.rtParalog}) 
@AttributeNameRequired(ids = {MetaData.atCONF, MetaData.atTaxid})
public class Parser extends ONDEXParser {

	private HashMap<Integer,Integer> ondexConcepts;

	private ConceptClass ccProtein;
	private DataSource dataSourceInparanoid;
	private RelationType rtOrtholog;
	private RelationType rtParalog;
	private EvidenceType etIMPD;
	private AttributeName anTaxId;
	private AttributeName anConf;



	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[]{
				new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE, "Absolute path to an OrthoXML input file", true, true, false, false)
		};
	}

	@Override
	public String getId() {
		return "orthoxml";
	}

	@Override
	public String getName() {
		return "OrthoXML";
	}

	@Override
	public String getVersion() {
		return "05-11-2010";
	}

	@Override
	public String[] requiresValidators() {
		return new String[]{};
	}

	@Override
	public void start() throws Exception {

		ONDEXGraphMetaData md = graph.getMetaData();
		ccProtein = md.getConceptClass(MetaData.ccProtein);
		dataSourceInparanoid = md.getDataSource(MetaData.cvInparanoid);
		rtOrtholog = md.getRelationType(MetaData.rtOrtholog);
		rtParalog = md.getRelationType(MetaData.rtParalog);
		etIMPD = md.getEvidenceType(MetaData.etIMPD);
		anTaxId = md.getAttributeName(MetaData.atTaxid);
		anConf = md.getAttributeName(MetaData.atCONF);

		ondexConcepts = new HashMap<Integer,Integer>();

		File xmlFile = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE));

		// setup XMLStreamReader
		System.setProperty("javax.xml.stream.XMLInputFactory",
		"com.ctc.wstx.stax.WstxInputFactory");
		XMLInputFactory2 xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
		xmlif.configureForSpeed();
		FileInputStream in = new FileInputStream(xmlFile);
		XMLStreamReader xmlr = xmlif.createXMLStreamReader(in, CharsetNames.CS_UTF8);

		// main loop
		while (xmlr.hasNext()) {
			int event = xmlr.next();

			if (event == XMLStreamConstants.START_ELEMENT) {

				String element = xmlr.getLocalName();

				// delegate to species parser
				if (element.equals("species")) {
					parseSpecies(xmlr);
				}
				// delegate to ortholgGroup parser
				else if(element.equals("orthologGroup")){
					parseClusters(xmlr);
				}
			}
		} // eof
	}

	
	/**
	 * Parser for the xml species element. 
	 * Creates for each gene an Ondex concept.
	 * 
	 * @param xmlr
	 * @throws XMLStreamException
	 */
	private void parseSpecies(XMLStreamReader xmlr) throws XMLStreamException{
		//<species id="G.lamblia" name="Giardia lamblia" NCBITaxId="184922">
		String taxId = xmlr.getAttributeValue(null, "NCBITaxId");
		System.out.println(taxId);

		xmlr.nextTag(); //database
		// <database name="Sanger" version="TANN.GeneDB.pep" geneLink="http://www.genedb.org/genedb/Search?name=" protLink="http://www.genedb.org/genedb/Search?name=">
		String db = xmlr.getAttributeValue(null, "name");
		//map db to Ondex DataSource
		String cvId = mapDBtoOndexmetaData(db);
		
		DataSource accDataSource = graph.getMetaData().getDataSource(cvId);
		if(accDataSource == null){
			accDataSource = graph.getMetaData().createDataSource(cvId, cvId, "Unknown DataSource from Inparanoid");
		}

		xmlr.nextTag(); //genes

		int eventType = xmlr.getEventType();

		while (xmlr.hasNext() && eventType != XMLStreamConstants.END_ELEMENT) {

			if (eventType == XMLStreamConstants.START_ELEMENT
					&& xmlr.getLocalName().equals("gene")) {

				// example
				// <gene id="1" geneId="GL50803_17570" protId="GL50803_17570" /> 

				Integer id = Integer.parseInt(xmlr.getAttributeValue(0));
				String geneId = xmlr.getAttributeValue(1);
				String protId = xmlr.getAttributeValue(2);

				if(!ondexConcepts.containsKey(id)){
					ONDEXConcept c = graph.getFactory().createConcept(protId, dataSourceInparanoid, ccProtein, etIMPD);
					c.createAttribute(anTaxId, taxId, false);
					if(!geneId.isEmpty()){
						c.createConceptName(geneId, false);
						c.createConceptAccession(geneId, accDataSource, true);
					}	
					if(!protId.isEmpty()){
						c.createConceptName(protId, true);
						c.createConceptAccession(protId, accDataSource, false);
					}	

					ondexConcepts.put(id, c.getId());
				}


				xmlr.nextTag(); // skip end tag

			}
			eventType = xmlr.next();
		}
	}

	/**
	 * Parser for the xml orthologGroup element. 
	 * Creates for each seed-ortholog a relation of type ortholog and
	 * for each inparalog pair a relation of type paralog.
	 * 
	 * @param xmlr
	 * @throws XMLStreamException
	 */
	private void parseClusters(XMLStreamReader xmlr) throws XMLStreamException{

		// skip cluster score
		xmlr.nextTag();
		xmlr.nextTag();

		//cluster genes
		ONDEXConcept[] seed = new ONDEXConcept[2];
		int count = 0;

		while (xmlr.hasNext() && !xmlr.getLocalName().equals("orthologGroup")) {

			//go to geneRef
			xmlr.nextTag();

			if (xmlr.getEventType() == XMLStreamConstants.START_ELEMENT
					&& xmlr.getLocalName().equals("geneRef")) {

				// <geneRef id="1">
				//   <score id="inparalog" value="1"/>
				//   <score id="bootstrap" value="1.00" /> 
				// </geneRef>

				Integer id = Integer.parseInt(xmlr.getAttributeValue(null, "id"));
				ONDEXConcept gene = graph.getConcept(ondexConcepts.get(id));

				// go to score element
				xmlr.nextTag();

				//get the inparalog score
				Double scoreInparalog = Double.parseDouble(xmlr.getAttributeValue(1));

				ONDEXRelation r = null;
				if(count < 2){
					seed[count] = gene;
					count++;
					if(seed[0] != null && seed[1] != null){
						r = graph.getFactory().createRelation(seed[0], seed[1], rtOrtholog, etIMPD);
					}	

				}else{

					if(seed[0].getAttribute(anTaxId).getValue().equals(gene.getAttribute(anTaxId).getValue())){
						r = graph.getFactory().createRelation(seed[0], gene, rtParalog, etIMPD);
					}else{
						r = graph.getFactory().createRelation(seed[1], gene, rtParalog, etIMPD);
					}
				}

				if(r != null){
					r.createAttribute(anConf, scoreInparalog, false);
				}

			}

		}

	}
	
	/**
	 * Mapping of database names to DataSource ids used in the Ondex MetaData
	 * 
	 * @param name
	 * @return
	 */
	private String mapDBtoOndexmetaData(String name){
		
		String cvId = name;
		
		if(name.equalsIgnoreCase("Gramene")){
			cvId = "ENSEMBL";
		}	
		
		return cvId;
	}

}
