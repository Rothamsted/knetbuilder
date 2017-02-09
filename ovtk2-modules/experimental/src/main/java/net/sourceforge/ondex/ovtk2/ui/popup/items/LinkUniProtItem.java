package net.sourceforge.ondex.ovtk2.ui.popup.items;

import java.awt.Color;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;
import net.sourceforge.ondex.ovtk2.util.LayoutNeighbours;
import net.sourceforge.ondex.parser.uniprot.ArgumentNames;
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
import net.sourceforge.ondex.parser.uniprot.xml.filter.ValueFilter;

import com.ctc.wstx.stax.WstxInputFactory;

/**
 * Ondex queries the UniProt webservice with the accession numbers for proteins,
 * and then creates concepts from the results and adds them to the graph
 * 
 * @author taubertj
 * 
 */
public class LinkUniProtItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {

		// get meta data
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		DataSource dsUNIPROTKB = graph.getMetaData().getDataSource("UNIPROTKB");

		// look at all selected concepts
		for (ONDEXConcept c : entities) {
			for (ConceptAccession ca : c.getConceptAccessions()) {
				// at least one accession with source UNIPROT
				if (ca.getElementOf().equals(dsUNIPROTKB)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	protected void doAction() {

		// get meta data
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		ONDEXConcept center = null;
		DataSource dsUNIPROTKB = graph.getMetaData().getDataSource("UNIPROTKB");

		// parse all accessions contained in graph
		Map<String, Set<ONDEXConcept>> accessions = new HashMap<String, Set<ONDEXConcept>>();
		for (ONDEXConcept c : entities) {
			for (ConceptAccession ca : c.getConceptAccessions()) {
				if (ca.getElementOf().equals(dsUNIPROTKB)) {
					if (!accessions.containsKey(ca.getAccession()))
						accessions.put(ca.getAccession(),
								new HashSet<ONDEXConcept>());
					accessions.get(ca.getAccession()).add(c);
					center = c;
				}
			}
		}

		// init metadata
		EvidenceType evidencetype = graph.getMetaData().getEvidenceType("IMPD");
		if (evidencetype == null)
			evidencetype = graph.getMetaData().getFactory()
					.createEvidenceType("IMPD");

		RelationType ofType = graph.getMetaData().getRelationType("is_a");
		if (ofType == null)
			ofType = graph.getMetaData().getFactory()
					.createRelationType("is_a");

		// Populate XML parsing delegates
		HashMap<String, ComponentParser> delegates = new HashMap<String, ComponentParser>();
		HashMap<FilterEnum, ValueFilter> filter = new HashMap<FilterEnum, ValueFilter>();
		delegates.put(
				"dbReference",
				new DbReferenceBlockParser(filter
						.get(FilterEnum.DatabaseReferenceFilter)));
		delegates.put("entry", new EntryStartParser());
		delegates.put("comment", new CommentBlockParser());
		delegates.put("sequence", new SequenceBlockParser());
		delegates.put(
				"accession",
				new AccessionBlockParser(filter
						.get(FilterEnum.DatabaseReferenceFilter)));// .AccessionFilter)));
		delegates
				.put("organism",
						new TaxonomieBlockParser(filter
								.get(FilterEnum.TaxonomieFilter)));
		delegates.put("reference", new PublicationBlockParser());
		delegates.put("protein", new ProteinNameBlockParser());
		delegates.put("gene", new GeneBlockParser());

		try {

			// setup UniProt parser arguments and transformer
			ONDEXPluginArguments args = new ONDEXPluginArguments(
					new ArgumentDefinition<?>[] { new BooleanArgumentDefinition(
							ArgumentNames.HIDE_LARGE_SCALE_PUBLICATIONS_ARG,
							ArgumentNames.HIDE_LARGE_SCALE_PUBLICATIONS_ARG_DESC,
							false, true) });
			args.addOption(ArgumentNames.HIDE_LARGE_SCALE_PUBLICATIONS_ARG,
					Boolean.TRUE);
			Transformer transformer = new Transformer(graph, args, false, null);

			// for xml parsing
			WstxInputFactory factory = (WstxInputFactory) WstxInputFactory
					.newInstance();
			factory.configureForSpeed();

			Set<ONDEXConcept> created = new HashSet<ONDEXConcept>();

			for (String accession : accessions.keySet()) {

				// UniProt URL
				URL url = new URL("http://www.uniprot.org/uniprot/" + accession
						+ ".xml");

				InputStream stream = url.openStream();

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
					// transform one protein into Ondex data structure
					else if (event == XMLStreamConstants.END_ELEMENT) {
						String element = staxXmlReader.getLocalName();
						if (element.equalsIgnoreCase("entry")) {

							// get new UniProt protein
							ONDEXConcept toConcept = transformer
									.transform(Protein.getInstance());
							created.add(toConcept);

							// add is_a relation between source and protein
							for (ONDEXConcept fromConcept : accessions
									.get(accession)) {
								graph.getFactory().createRelation(fromConcept,
										toConcept, ofType, evidencetype);
							}

							// create a new instance
							Protein.getInstance(true, false);
						}
					}
				}
				staxXmlReader.close();
			}

			System.out.println("Added " + created.size() + " new concepts.");

			// make new concepts visible
			viewer.getONDEXJUNGGraph().setVisibility(created, true);
			for (ONDEXConcept c : created) {
				// set something like default attributes
				viewer.getNodeColors().updateColor(c,
						Config.getColorForConceptClass(c.getOfType()));
				viewer.getNodeDrawPaint().updateColor(c, Color.BLACK);
				viewer.getNodeShapes().updateShape(c);

				// make all relations visible
				viewer.getONDEXJUNGGraph().setVisibility(
						graph.getRelationsOfConcept(c), true);
			}

			// layout nodes on big circle
			LayoutNeighbours.layoutNodes(viewer.getVisualizationViewer(),
					center, created);

			if (viewer.getMetaGraph() != null)
				viewer.getMetaGraph().updateMetaData();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.LINK;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.LinkUniProt";
	}

	@Override
	protected String getUndoPropertyName() {
		return "";
	}

}
