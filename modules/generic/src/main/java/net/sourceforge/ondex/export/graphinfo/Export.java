package net.sourceforge.ondex.export.graphinfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.export.ONDEXExport;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;

import com.ctc.wstx.api.WstxOutputProperties;
import com.ctc.wstx.io.CharsetNames;
import com.ctc.wstx.stax.WstxOutputFactory;

/**
 * Exports information about meta data and other properties assigned to concepts
 * and relations in the graph.
 * 
 * @author taubertj
 * 
 */
public class Export extends ONDEXExport implements FieldNames {

	// the XML document to write to
	private OutputStream outStream;

	/**
	 * Make sure everything is written out.
	 * 
	 * @throws IOException
	 */
	protected void flushOutput() throws IOException {
		outStream.flush();
		outStream.close();
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[] { new FileArgumentDefinition(
				FileArgumentDefinition.EXPORT_FILE, "Data file to export",
				true, false, false, false) };
	}

	@Override
	public String getId() {
		return "graphinfo";
	}

	@Override
	public String getName() {
		return "Graph Information Export";
	}

	@Override
	public String getVersion() {
		return "27.05.2011";
	}

	/**
	 * Configures XML Output Factory
	 * 
	 * @return WstxOutputFactory
	 */
	protected WstxOutputFactory getXMLFactory() {
		// initialise outputs
		System.setProperty("javax.xml.stream.XMLOutputFactory",
				"com.ctc.wstx.stax.WstxOutputFactory");

		WstxOutputFactory xmlw = (WstxOutputFactory) WstxOutputFactory
				.newInstance();
		xmlw.configureForRobustness();

		// configure factory
		xmlw.setProperty(XMLOutputFactory2.IS_REPAIRING_NAMESPACES, false);
		xmlw.setProperty(WstxOutputProperties.P_OUTPUT_FIX_CONTENT, true);
		xmlw.setProperty(WstxOutputProperties.P_OUTPUT_VALIDATE_CONTENT, true);
		return xmlw;
	}

	/**
	 * Opens XML file to write to.
	 * 
	 * @param xmlw
	 *            WstxOutputFactory
	 * @return XMLStreamWriter2
	 * @throws InvalidPluginArgumentException
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	protected XMLStreamWriter2 getXMLStreamWriter2(WstxOutputFactory xmlw)
			throws InvalidPluginArgumentException, XMLStreamException,
			IOException {

		// get filename from arguments
		String fileName = ((String) args
				.getUniqueValue(FileArgumentDefinition.EXPORT_FILE)).trim();
		fireEventOccurred(new GeneralOutputEvent(
				"fileName set to: " + fileName, "[Export - start]"));

		if (!fileName.toLowerCase().endsWith(".xml")) {
			fileName += ".xml";
		}
		File file = new File(fileName);

		// output file writer
		outStream = new FileOutputStream(file);

		// wrap into XML stream
		XMLStreamWriter2 xmlWriteStream = (XMLStreamWriter2) xmlw
				.createXMLStreamWriter(outStream, CharsetNames.CS_UTF8);

		return xmlWriteStream;
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	@Override
	public void start() throws Exception {

		// configure XML factory
		WstxOutputFactory xmlw = getXMLFactory();

		// configure XML writer
		XMLStreamWriter2 xmlWriteStream = getXMLStreamWriter2(xmlw);

		fireEventOccurred(new GeneralOutputEvent("Ready to Export.",
				"[Export - start]"));

		// start document
		xmlWriteStream.writeStartDocument();

		// write document root element
		xmlWriteStream.writeStartElement(ROOT);

		// a graph over-seeds list of concepts / relations
		if (graph != null) {
			concepts = graph.getConcepts();
			relations = graph.getRelations();

			writeGraphInfo(xmlWriteStream);

			writeMetaDataInfo(xmlWriteStream);

			writeEachDataSourceMetaData(xmlWriteStream);

			writeConceptAccessions(xmlWriteStream);
		}

		// close XML document properly
		xmlWriteStream.writeEndElement();
		xmlWriteStream.writeEndDocument();
		xmlWriteStream.flush();
		xmlWriteStream.close();
		flushOutput();
		fireEventOccurred(new GeneralOutputEvent("Finished Graph Info Export.",
				"[Export - start]"));

	}

	/**
	 * Writes statistics about concept accession distributions and kinds
	 * 
	 * @param xmlWriteStream
	 * @throws XMLStreamException
	 */
	private void writeConceptAccessions(XMLStreamWriter2 xmlWriteStream)
			throws XMLStreamException {

		// start new section
		xmlWriteStream.writeStartElement(CONCEPTACCESSIONS);

		// iterate over all data sources
		for (DataSource ds : graph.getMetaData().getDataSources()) {
			Set<ONDEXConcept> conceptsDS = graph.getConceptsOfDataSource(ds);
			if (conceptsDS.size() > 0) {

				// start of new DATASOURCE
				xmlWriteStream.writeStartElement(DATASOURCE);

				// ID of data source
				xmlWriteStream.writeStartElement(ID);
				xmlWriteStream.writeCharacters(ds.getId());
				xmlWriteStream.writeEndElement();

				// iterate over all concept classes
				for (ConceptClass cc : graph.getMetaData().getConceptClasses()) {
					Set<ONDEXConcept> conceptsConceptClass = graph
							.getConceptsOfConceptClass(cc);
					Set<ONDEXConcept> conceptsCombined = BitSetFunctions
							.copy(conceptsConceptClass);
					conceptsCombined.retainAll(conceptsDS);
					if (conceptsCombined.size() > 0) {

						// start of new CONCEPTCLASS
						xmlWriteStream.writeStartElement(CONCEPTCLASS);

						// ID of concept class
						xmlWriteStream.writeStartElement(ID);
						xmlWriteStream.writeCharacters(cc.getId());
						xmlWriteStream.writeEndElement();

						Map<DataSource, Set<ConceptAccession>> accessions = new HashMap<DataSource, Set<ConceptAccession>>();

						final Map<DataSource, Set<ONDEXConcept>> accessionDataSources = new HashMap<DataSource, Set<ONDEXConcept>>();

						// iterate over all concepts
						boolean written = false;
						for (ONDEXConcept c : conceptsCombined) {

							// collect accessions by data source
							for (ConceptAccession acc : c
									.getConceptAccessions()) {
								DataSource elementOf = acc.getElementOf();

								// for counting overall number of accession of
								// particular type
								if (!accessions.containsKey(elementOf))
									accessions.put(elementOf,
											new HashSet<ConceptAccession>());
								accessions.get(elementOf).add(acc);

								// for counting number of concept that have
								// accession of particular type
								if (!accessionDataSources
										.containsKey(elementOf))
									accessionDataSources.put(elementOf,
											new HashSet<ONDEXConcept>());
								accessionDataSources.get(elementOf).add(c);
							}

							// sample concept name output
							Set<ConceptName> names = c.getConceptNames();
							if (!written && names.size() > 0) {
								// CONCEPTNAMES for random concept
								xmlWriteStream.writeStartElement(CONCEPTNAMES);

								// write all names
								for (ConceptName cn : names) {
									xmlWriteStream.writeStartElement(EXAMPLE);
									xmlWriteStream
											.writeCharacters(cn.getName());
									xmlWriteStream.writeEndElement();
								}

								xmlWriteStream.writeEndElement();
								written = true;
							}
						}

						// sort by size of accessions
						DataSource[] sorted = accessions.keySet().toArray(
								new DataSource[0]);
						Arrays.sort(sorted, new Comparator<DataSource>() {

							@Override
							public int compare(DataSource o1, DataSource o2) {
								int size1 = accessionDataSources.get(o1).size();
								int size2 = accessionDataSources.get(o2).size();
								return size2 - size1;
							}
						});

						// statistics per accession data source
						for (DataSource elementOf : sorted) {

							// count ambiguity of accession
							Set<ConceptAccession> set = accessions
									.get(elementOf);
							int ambiguous = 0;
							for (ConceptAccession acc : set) {
								if (acc.isAmbiguous())
									ambiguous++;
							}

							// count total number of concepts with accession
							Set<ONDEXConcept> concepts = accessionDataSources
									.get(elementOf);

							// start of new CONCEPTACCESSION
							xmlWriteStream.writeStartElement(CONCEPTACCESSION);

							// ID of concept accession data source
							xmlWriteStream.writeStartElement(ID);
							xmlWriteStream.writeCharacters(elementOf.getId());
							xmlWriteStream.writeEndElement();

							// COUNT of this concept accession type
							xmlWriteStream.writeStartElement(COUNT);
							xmlWriteStream.writeInt(set.size());
							xmlWriteStream.writeEndElement();

							// COUNTCONCEPT of this concept accession type
							xmlWriteStream.writeStartElement(COUNTCONCEPT);
							xmlWriteStream.writeInt(concepts.size());
							xmlWriteStream.writeEndElement();

							// PERCENTAGEDATASOURCE of concepts in concept class
							// and data source having this concept accession
							// type
							xmlWriteStream
									.writeStartElement(PERCENTAGEDATASOURCE);
							double percent = (double) concepts.size()
									/ (double) conceptsCombined.size();
							xmlWriteStream.writeCharacters(NumberFormat
									.getPercentInstance().format(percent));
							xmlWriteStream.writeEndElement();

							// PERCENTAGETOTAL of concepts in concept class
							// across all data sources having this concept
							// accession type
							xmlWriteStream.writeStartElement(PERCENTAGETOTAL);
							percent = (double) concepts.size()
									/ (double) conceptsConceptClass.size();
							xmlWriteStream.writeCharacters(NumberFormat
									.getPercentInstance().format(percent));
							xmlWriteStream.writeEndElement();

							// AMBIGUOUS count of this concept accession type
							xmlWriteStream.writeStartElement(AMBIGUOUS);
							xmlWriteStream.writeInt(ambiguous);
							xmlWriteStream.writeEndElement();

							// EXAMPLE for this concept accession type
							xmlWriteStream.writeStartElement(EXAMPLE);
							xmlWriteStream.writeCharacters(set.iterator()
									.next().getAccession());
							xmlWriteStream.writeEndElement();

							xmlWriteStream.writeEndElement();
						}

						// end section CONCEPTCLASS
						xmlWriteStream.writeEndElement();
					}
				}

				// end section CONCEPTCLASS
				xmlWriteStream.writeEndElement();
			}
		}

		// end section
		xmlWriteStream.writeEndElement();

	}

	/**
	 * Writes out statistics for each existing concept class
	 * 
	 * @param xmlWriteStream
	 * @throws XMLStreamException
	 */
	private void writeConceptClasses(XMLStreamWriter2 xmlWriteStream,
			Set<ONDEXConcept> concepts) throws XMLStreamException {

		// iterate over all concept classes
		for (ConceptClass cc : graph.getMetaData().getConceptClasses()) {

			// check this concept class has concepts
			Set<ONDEXConcept> set = graph.getConceptsOfConceptClass(cc);
			if (set.size() > 0) {
				// for data source specific case
				if (concepts != null) {
					set = BitSetFunctions.copy(set);
					set.retainAll(concepts);
				}

				// new CONCEPTCLASS
				xmlWriteStream.writeStartElement(CONCEPTCLASS);

				// write ID
				xmlWriteStream.writeStartElement(ID);
				xmlWriteStream.writeCharacters(cc.getId());
				xmlWriteStream.writeEndElement();

				// write COUNT
				xmlWriteStream.writeStartElement(COUNT);
				xmlWriteStream.writeInt(set.size());
				xmlWriteStream.writeEndElement();

				// end CONCEPTCLASS
				xmlWriteStream.writeEndElement();
			}
		}
	}

	/**
	 * Writes out statistics for each existing data source
	 * 
	 * @param xmlWriteStream
	 * @throws XMLStreamException
	 */
	private void writeDataSources(XMLStreamWriter2 xmlWriteStream)
			throws XMLStreamException {

		// iterate over all data sources
		for (DataSource ds : graph.getMetaData().getDataSources()) {

			// check this data source has concepts
			Set<ONDEXConcept> set = graph.getConceptsOfDataSource(ds);
			if (set.size() > 0) {
				// new DATASOURCE
				xmlWriteStream.writeStartElement(DATASOURCE);

				// write ID
				xmlWriteStream.writeStartElement(ID);
				xmlWriteStream.writeCharacters(ds.getId());
				xmlWriteStream.writeEndElement();

				// write COUNT
				xmlWriteStream.writeStartElement(COUNT);
				xmlWriteStream.writeInt(set.size());
				xmlWriteStream.writeEndElement();

				// end DATASOURCE
				xmlWriteStream.writeEndElement();
			}
		}
	}

	/**
	 * Writes meta data for each data sources
	 * 
	 * @param xmlWriteStream
	 * @throws XMLStreamException
	 */
	private void writeEachDataSourceMetaData(XMLStreamWriter2 xmlWriteStream)
			throws XMLStreamException {

		// start new section
		xmlWriteStream.writeStartElement(EACHDATASOURCE);

		// iterate over all data sources
		for (DataSource ds : graph.getMetaData().getDataSources()) {
			Set<ONDEXConcept> concepts = graph.getConceptsOfDataSource(ds);
			if (concepts.size() > 0) {
				Set<ONDEXRelation> relations = graph
						.getRelationsOfDataSource(ds);

				// start of new DATASOURCE
				xmlWriteStream.writeStartElement(DATASOURCE);

				// ID of data source
				xmlWriteStream.writeStartElement(ID);
				xmlWriteStream.writeCharacters(ds.getId());
				xmlWriteStream.writeEndElement();

				// sub section for concept classes
				xmlWriteStream.writeStartElement(CONCEPTCLASSES);
				writeConceptClasses(xmlWriteStream, concepts);
				xmlWriteStream.writeEndElement();

				// sub section for relation types
				xmlWriteStream.writeStartElement(RELATIONTYPES);
				writeRelationTypes(xmlWriteStream, relations);
				xmlWriteStream.writeEndElement();

				// sub section for evidence types
				xmlWriteStream.writeStartElement(EVIDENCETYPES);
				writeEvidenceTypes(xmlWriteStream, concepts, relations);
				xmlWriteStream.writeEndElement();

				// end section DATASOURCE
				xmlWriteStream.writeEndElement();
			}
		}

		// end section
		xmlWriteStream.writeEndElement();
	}

	/**
	 * Writes out statistics for each existing evidence type
	 * 
	 * @param xmlWriteStream
	 * @throws XMLStreamException
	 */
	private void writeEvidenceTypes(XMLStreamWriter2 xmlWriteStream,
			Set<ONDEXConcept> c, Set<ONDEXRelation> r)
			throws XMLStreamException {

		// iterate over all evidence types
		for (EvidenceType rt : graph.getMetaData().getEvidenceTypes()) {

			// check this evidence type has relations
			Set<ONDEXRelation> relations = graph.getRelationsOfEvidenceType(rt);
			Set<ONDEXConcept> concepts = graph.getConceptsOfEvidenceType(rt);
			if (concepts.size() > 0 || relations.size() > 0) {
				// for data source specific case
				if (c != null) {
					concepts = BitSetFunctions.copy(concepts);
					concepts.retainAll(c);
				}
				if (r != null) {
					relations = BitSetFunctions.copy(relations);
					relations.retainAll(r);
				}

				// new EVIDENCETYPE
				xmlWriteStream.writeStartElement(EVIDENCETYPE);

				// write ID
				xmlWriteStream.writeStartElement(ID);
				xmlWriteStream.writeCharacters(rt.getId());
				xmlWriteStream.writeEndElement();

				// write CONCEPTCOUNT
				xmlWriteStream.writeStartElement(COUNTCONCEPT);
				xmlWriteStream.writeInt(concepts.size());
				xmlWriteStream.writeEndElement();

				// write RELATIONCOUNT
				xmlWriteStream.writeStartElement(COUNTRELATION);
				xmlWriteStream.writeInt(relations.size());
				xmlWriteStream.writeEndElement();

				// end EVIDENCETYPE
				xmlWriteStream.writeEndElement();
			}
		}
	}

	/**
	 * Writes out general information about the graph
	 * 
	 * @param xmlWriteStream
	 * @throws XMLStreamException
	 */
	private void writeGraphInfo(XMLStreamWriter2 xmlWriteStream)
			throws XMLStreamException {

		// start new section
		xmlWriteStream.writeStartElement(GENERAL);

		// write graph name
		xmlWriteStream.writeStartElement(NAME);
		xmlWriteStream.writeCharacters(graph.getName());
		xmlWriteStream.writeEndElement();

		// write number of concepts
		xmlWriteStream.writeStartElement(NBCONCEPTS);
		xmlWriteStream.writeInt(concepts.size());
		xmlWriteStream.writeEndElement();

		// write number of relations
		xmlWriteStream.writeStartElement(NBRELATIONS);
		xmlWriteStream.writeInt(relations.size());
		xmlWriteStream.writeEndElement();

		// end section
		xmlWriteStream.writeEndElement();
	}

	/**
	 * Writes out statistics for every meta data element
	 * 
	 * @param xmlWriteStream
	 * @throws XMLStreamException
	 */
	private void writeMetaDataInfo(XMLStreamWriter2 xmlWriteStream)
			throws XMLStreamException {

		// start new section
		xmlWriteStream.writeStartElement(METADATA);

		// sub section for concept classes
		xmlWriteStream.writeStartElement(CONCEPTCLASSES);
		writeConceptClasses(xmlWriteStream, null);
		xmlWriteStream.writeEndElement();

		// sub section for data sources
		xmlWriteStream.writeStartElement(DATASOURCES);
		writeDataSources(xmlWriteStream);
		xmlWriteStream.writeEndElement();

		// sub section for relation types
		xmlWriteStream.writeStartElement(RELATIONTYPES);
		writeRelationTypes(xmlWriteStream, null);
		xmlWriteStream.writeEndElement();

		// sub section for evidence types
		xmlWriteStream.writeStartElement(EVIDENCETYPES);
		writeEvidenceTypes(xmlWriteStream, null, null);
		xmlWriteStream.writeEndElement();

		// end section
		xmlWriteStream.writeEndElement();
	}

	/**
	 * Writes out statistics for each existing relation type
	 * 
	 * @param xmlWriteStream
	 * @throws XMLStreamException
	 */
	private void writeRelationTypes(XMLStreamWriter2 xmlWriteStream,
			Set<ONDEXRelation> relations) throws XMLStreamException {

		// iterate over all relation types
		for (RelationType rt : graph.getMetaData().getRelationTypes()) {

			// check this relation type has relations
			Set<ONDEXRelation> set = graph.getRelationsOfRelationType(rt);
			if (set.size() > 0) {
				// data source specific case
				if (relations != null) {
					set = BitSetFunctions.copy(set);
					set.retainAll(relations);
				}

				// new RELATIONTYPE
				xmlWriteStream.writeStartElement(RELATIONTYPE);

				// write ID
				xmlWriteStream.writeStartElement(ID);
				xmlWriteStream.writeCharacters(rt.getId());
				xmlWriteStream.writeEndElement();

				// write COUNT
				xmlWriteStream.writeStartElement(COUNT);
				xmlWriteStream.writeInt(set.size());
				xmlWriteStream.writeEndElement();

				// end RELATIONTYPE
				xmlWriteStream.writeEndElement();
			}
		}
	}

}
