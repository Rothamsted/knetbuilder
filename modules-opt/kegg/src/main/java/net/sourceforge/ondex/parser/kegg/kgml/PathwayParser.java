package net.sourceforge.ondex.parser.kegg.kgml;

import javax.xml.stream.XMLStreamException;

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
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.parser.kegg.Parser;

import org.codehaus.stax2.XMLStreamReader2;

/**
 * For parsing the name of the current pathway map
 * 
 * @author taubertj
 * 
 */
public class PathwayParser extends KgmlComponentParser {

	// Attribute for link URL
	private AttributeName anURL;

	// current ONDEX graph
	private ONDEXGraph aog;

	// data sources
	private DataSource dataSourceMap, dataSourceKEGG, dataSourceEC;

	// EvidenceType = IMPD
	private EvidenceType et;

	// stop parser from progressing
	private boolean ready = true;

	// the pathway concept
	private ONDEXConcept concept;

	// graphical attributes
	private AttributeName attrVisible;

	public PathwayParser(ONDEXGraph aog, Parser parser) {
		super(parser, "pathway");
		this.aog = aog;

		// check meta data
		dataSourceMap = aog.getMetaData().getDataSource("KEGGMAP");
		if (dataSourceMap == null)
			dataSourceMap = aog.getMetaData().getFactory()
					.createDataSource("KEGGMAP");
		dataSourceKEGG = aog.getMetaData().getDataSource("KEGG");
		dataSourceEC = aog.getMetaData().getDataSource("EC");
		et = aog.getMetaData().getEvidenceType("IMPD");
		anURL = aog.getMetaData().getAttributeName("URL");

		// sanity checks
		if (dataSourceKEGG == null) {
			parser.fireEventOccurred(new DataSourceMissingEvent("KEGG",
					"[EntryParser - constructor]"));
			ready = false;
		}
		if (dataSourceEC == null) {
			parser.fireEventOccurred(new DataSourceMissingEvent("EC",
					"[EntryParser - constructor]"));
			ready = false;
		}
		if (et == null) {
			parser.fireEventOccurred(new EvidenceTypeMissingEvent("IMPD",
					"[EntryParser - constructor]"));
			ready = false;
		}
		if (anURL == null) {
			parser.fireEventOccurred(new AttributeNameMissingEvent("URL",
					"[EntryParser - constructor]"));
			ready = false;
		}

		// get meta data
		ONDEXGraphMetaData meta = aog.getMetaData();
		if ((attrVisible = meta.getAttributeName("visible")) == null)
			attrVisible = meta.getFactory().createAttributeName("visible",
					java.lang.Boolean.class);
	}

	@Override
	public void parse(XMLStreamReader2 xmlStreamReader)
			throws XMLStreamException {

		if (ready) {

			// get all attributes for pathway element
			String name = xmlStreamReader.getAttributeValue(null, "name");
			String org = xmlStreamReader.getAttributeValue(null, "org");
			String number = xmlStreamReader.getAttributeValue(null, "number");
			String title = xmlStreamReader.getAttributeValue(null, "title");
			String link = xmlStreamReader.getAttributeValue(null, "link");

			// get entry type mapping for concepts
			String ccID = EntityMapping.getConceptClassMapping("MAP");
			if (ccID != null) {
				ConceptClass cc = aog.getMetaData().getConceptClass(ccID);
				if (cc == null) {
					parser.fireEventOccurred(new ConceptClassMissingEvent(ccID,
							"[EntryParser - parse]"));
					return;
				}

				// create new concept for entry
				concept = aog.getFactory().createConcept(org + number,
						dataSourceMap, cc, et);
				concept.setAnnotation(title);
				concept.setDescription(name);

				// default set to visible
				concept.createAttribute(attrVisible, Boolean.FALSE, false);

				// add possible URL
				if (checkNotEmpty(link)) {
					concept.createAttribute(anURL, link, false);
				}

				// add possible name
				if (checkNotEmpty(name)) {
					concept.createConceptName(name, true);

					// transform into KEGG accessions
					String[] split = name.split(" ");
					for (String s : split) {
						concept.createConceptAccession(s.toUpperCase(),
								dataSourceKEGG, true);
						//if (s.contains(":")) {
						//	s = s.substring(s.indexOf(":") + 1);
						//	concept.createConceptAccession(s.toUpperCase(),
						//			dataSourceKEGG, true);
						//}
					}
				}
			}
		}
	}

	/**
	 * Returns parsed pathway concept.
	 * 
	 * @return ONDEXConcept
	 */
	public ONDEXConcept getConcept() {
		return concept;
	}

	/**
	 * Simply check for null or empty Strings.
	 * 
	 * @param s
	 *            String to check
	 * @return true if not empty
	 */
	private boolean checkNotEmpty(String s) {
		return s != null && s.trim().length() > 0;
	}

}
