package net.sourceforge.ondex.parser.biocyc;

import net.sourceforge.ondex.annotations.DataURL;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.biocyc.handler.*;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.jena.JenaIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.*;

import java.io.*;

/**
 * PlantCyc parser based on BioPax representation parsed using paxtools.
 * 
 * @author taubertj
 */
@Status(description = "Tested with AraCyc, LycoCyc, HumanCyc and Reactome by Jan Taubert (January 2013)", status = StatusType.STABLE)
@DatabaseTarget(name = "BioCyc", description = "BioCyc databases", version = "BioPax version 2", url = "http://www.biocyc.org")
@DataURL(name = "BioCyc databases", description = "Place biopax-level2.owl or similar file in data folder with no directory structure", urls = { "" })
public class Parser extends ONDEXParser {

	// Default IO interface
	private static BioPAXIOHandler ioHandler;

	// what DataSource to use for data sources without mapping
	public static String cvToUse = "unknown";

	// what TAXID to use for entries without TAXID
	public static String taxidToUse = null;

	/*
	 * get model from file
	 */

	private static Model getModel(BioPAXIOHandler io, String fName)
			throws FileNotFoundException {
		FileInputStream file = new FileInputStream(fName);
		return io.convertFromOWL(file);
	}

	/*
	 * singleton pattern for IOHandler
	 */

	private static BioPAXIOHandler io() {
		if (ioHandler == null) {
			ioHandler = new JenaIOHandler();
		}
		return ioHandler;
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition[] {
				new StringArgumentDefinition(ArgumentNames.TAXID_TO_USE_ARG,
						ArgumentNames.TAXID_TO_USE_ARG_DESC, false, null, false),
				new StringArgumentDefinition(ArgumentNames.CV_ARG,
						ArgumentNames.CV_ARG_DESC, false, "unknown", false),
				new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
						FileArgumentDefinition.INPUT_FILE_DESC, true, true,
						false, false) };
	}

	@Override
	public String getId() {
		return "biocyc";
	}

	@Override
	public String getName() {
		return "BioCyc BioPAX parser";
	}

	@Override
	public String getVersion() {
		return "21.01.2010";
	}

	@Override
	public String[] requiresValidators() {
		// return new String[] { "taxonomy" };
		return new String[0];
	}

	/**
	 * Reads in translation.tab to populate DataSource mapping.
	 * 
	 * @param file
	 *            File for translation.tab
	 * @throws Exception
	 */
	private void populateMapping(File file) throws Exception {

		// open translation.tab
		BufferedReader reader = new BufferedReader(new FileReader(file));
		while (reader.ready()) {
			String line = reader.readLine();
			// skip comments
			if (!line.startsWith("#")) {
				// split at TAB and check length
				String[] split = line.split("\t");
				if (split.length == 2) {
					DefaultHandler.cvMap.put(split[0], split[1]);
				}
			}
		}
	}

	@Override
	public void start() throws Exception {
		File dir = new File(
				(String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE));

		File file = new File(dir.getParent() + File.separator
				+ "translation.tab");
		populateMapping(file);

		// get default DataSource from arguments
		if (args.getUniqueValue(ArgumentNames.CV_ARG) != null) {
			cvToUse = (String) args.getUniqueValue(ArgumentNames.CV_ARG);
			if (graph.getMetaData().getDataSource(cvToUse) == null) {
				graph.getMetaData().getFactory().createDataSource(cvToUse);
				fireEventOccurred(new DataSourceMissingEvent(
						"The specified DataSource is not part of the metadata, therefore creating new one.",
						"[Parser - start]"));
			}
		}

		// get default TAXID from arguments
		taxidToUse = (String) args
				.getUniqueValue(ArgumentNames.TAXID_TO_USE_ARG);

		// load model from BioPax file
		Model m = getModel(io(), dir.getAbsolutePath());
		fireEventOccurred(new GeneralOutputEvent("RDF Model parsing finished.",
				"[Parser - start]"));

		// add physicalEntity concepts
		new PhysicalEntityHandler(this.graph,
				m.getObjects(physicalEntity.class));
		fireEventOccurred(new GeneralOutputEvent(
				"Constructing physicalEntities finished.", "[Parser - start]"));

		// add smallMolecule concepts
		new CompoundHandler(this.graph, m.getObjects(smallMolecule.class));
		fireEventOccurred(new GeneralOutputEvent(
				"Constructing compounds finished.", "[Parser - start]"));

		// add protein concepts
		new ProteinHandler(this.graph, m.getObjects(protein.class));
		fireEventOccurred(new GeneralOutputEvent(
				"Constructing proteins finished.", "[Parser - start]"));

		// add protein complexes
		new ComplexHandler(this.graph, m.getObjects(complex.class));
		fireEventOccurred(new GeneralOutputEvent(
				"Constructing complexes finished.", "[Parser - start]"));

		// add reaction concepts
		new ReactionHandler(this.graph, m.getObjects(biochemicalReaction.class));
		fireEventOccurred(new GeneralOutputEvent(
				"Constructing reactions finished.", "[Parser - start]"));

		// add transport relationships
		new TransportHandler(this.graph, m.getObjects(transport.class));
		fireEventOccurred(new GeneralOutputEvent(
				"Constructing transports finished.", "[Parser - start]"));

		// add catalysis relationships
		new EnzymeHandler(this.graph, m.getObjects(catalysis.class));
		fireEventOccurred(new GeneralOutputEvent(
				"Constructing enzymes finished.", "[Parser - start]"));

		// turn modulation into relationships
		new ModulationHandler(this.graph, m.getObjects(modulation.class));
		fireEventOccurred(new GeneralOutputEvent(
				"Adding modulations finished.", "[Parser - start]"));

		// turn control into relationships
		new ControlHandler(this.graph, m.getObjects(control.class));
		fireEventOccurred(new GeneralOutputEvent("Adding controls finished.",
				"[Parser - start]"));

		// add pathway concepts
		new PathwayHandler(this.graph, m.getObjects(pathway.class));
		fireEventOccurred(new GeneralOutputEvent(
				"Constructing pathways finished.", "[Parser - start]"));

		// add pathway context
		new ContextHandler(this.graph, m.getObjects(pathway.class));
		fireEventOccurred(new GeneralOutputEvent("Adding context finished.",
				"[Parser - start]"));

	}

}
