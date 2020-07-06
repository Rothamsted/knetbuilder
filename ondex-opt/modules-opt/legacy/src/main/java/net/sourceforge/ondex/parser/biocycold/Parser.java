package net.sourceforge.ondex.parser.biocycold;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.biocycold.objects.AbstractNode;
import net.sourceforge.ondex.parser.biocycold.objects.SinkFactory;
import net.sourceforge.ondex.parser.biocycold.parse.AbstractParser;
import net.sourceforge.ondex.parser.biocycold.parse.CompoundParser;
import net.sourceforge.ondex.parser.biocycold.parse.EnzymeParser;
import net.sourceforge.ondex.parser.biocycold.parse.GeneParser;
import net.sourceforge.ondex.parser.biocycold.parse.IParser;
import net.sourceforge.ondex.parser.biocycold.parse.PathwayParser;
import net.sourceforge.ondex.parser.biocycold.parse.ProteinParser;
import net.sourceforge.ondex.parser.biocycold.parse.PublicationParser;
import net.sourceforge.ondex.parser.biocycold.parse.ReactionParser;
import net.sourceforge.ondex.parser.biocycold.parse.readers.AbstractReader;
import net.sourceforge.ondex.parser.biocycold.parse.readers.ColFileReader;
import net.sourceforge.ondex.parser.biocycold.parse.readers.DatFileReader;
import net.sourceforge.ondex.parser.biocycold.parse.transformers.AbstractTransformer;
import net.sourceforge.ondex.parser.biocycold.parse.transformers.CompoundTransformer;
import net.sourceforge.ondex.parser.biocycold.parse.transformers.ECNumberTransformer;
import net.sourceforge.ondex.parser.biocycold.parse.transformers.EnzymeTransformer;
import net.sourceforge.ondex.parser.biocycold.parse.transformers.GeneTransformer;
import net.sourceforge.ondex.parser.biocycold.parse.transformers.PathwayTransformer;
import net.sourceforge.ondex.parser.biocycold.parse.transformers.ProteinTransformer;
import net.sourceforge.ondex.parser.biocycold.parse.transformers.PublicationTransformer;
import net.sourceforge.ondex.parser.biocycold.parse.transformers.ReactionTransformer;
import net.sourceforge.ondex.parser.biocycold.parse.transformers.TransformerFactory;

/**
 * Parser for the aracyc flatfile database. The data flow is Parser ->
 * AbstractReader -> AbstractParser -> AbstractTransformer
 * 
 * @author peschr
 */
public class Parser extends ONDEXParser implements MetaData {

	public static String cvToUse = "unknown";

	public static String taxidToUse = null;

	/**
	 * Its used to define a configuration. A configuration consists of a parser,
	 * a transformer, a filename and the name of the sink class.
	 * 
	 * @author peschr
	 */
	class Configuration {
		private AbstractParser parser;

		private Class<? extends AbstractTransformer> transformer;

		private String fileName;

		private SinkName sinkName;

		public SinkName getSinkName() {
			return this.sinkName;
		}

		public String getFileName() {
			return fileName;
		}

		public AbstractParser getParser() {
			return parser;
		}

		public Class<? extends AbstractTransformer> getTransformer() {
			return transformer;
		}

		public Configuration(AbstractParser parser,
				Class<? extends AbstractTransformer> transformer,
				String fileName, SinkName sinkName) {
			this.parser = parser;
			this.transformer = transformer;
			this.fileName = fileName;
			this.sinkName = sinkName;
		}
	}

	/**
	 * enum which defines all possible sink names
	 * 
	 * @author peschr
	 */
	enum SinkName {
		Pathway, Reaction, Compound, Enzyme, Protein, Gene, Publication, ECNumber
	}

	// holds the configurations which are used for the parser
	private ArrayList<Configuration> configuration;

	// definies the order, this is needed for the transformation from sink -> to
	// concept/relation
	private SinkName[] pathwayOrder = { SinkName.Pathway, SinkName.Reaction,
			SinkName.Compound, SinkName.Enzyme, SinkName.Protein,
			SinkName.Gene, SinkName.Publication };

	private static Parser instance;

	/**
	 * initialize the parser
	 */
	public Parser() {

		instance = this;
		configuration = new ArrayList<Configuration>();
		configuration
				.add(new Configuration(new PublicationParser(),
						PublicationTransformer.class, "pubs.dat",
						SinkName.Publication));
		configuration.add(new Configuration(new GeneParser(),
				GeneTransformer.class, "genes.dat", SinkName.Gene));
		configuration.add(new Configuration(new GeneParser(),
				GeneTransformer.class, "genes.col", SinkName.Gene));
		configuration.add(new Configuration(new ProteinParser(),
				ProteinTransformer.class, "proteins.dat", SinkName.Protein));
		configuration.add(new Configuration(new EnzymeParser(),
				EnzymeTransformer.class, "enzrxns.dat", SinkName.Enzyme));
		configuration.add(new Configuration(new CompoundParser(),
				CompoundTransformer.class, "compounds.dat", SinkName.Compound));
		configuration.add(new Configuration(new ReactionParser(),
				ReactionTransformer.class, "reactions.dat", SinkName.Reaction));
		configuration.add(new Configuration(new PathwayParser(),
				PathwayTransformer.class, "pathways.dat", SinkName.Pathway));
		configuration.add(new Configuration(null, ECNumberTransformer.class,
				null, SinkName.ECNumber));
	}

	public String getName() {
		return new String("BioCyc");
	}

	public String getVersion() {
		return new String("22.04.2009");
	}

	@Override
	public String getId() {
		return "biocycold";
	}

	/**
	 * sets the ONDEXGraph and starts the parser
	 */
	public void start() throws InvalidPluginArgumentException {

		if (args.getUniqueValue(ArgumentNames.CV_ARG) != null) {
			cvToUse = (String) args.getUniqueValue(ArgumentNames.CV_ARG);
			if (graph.getMetaData().getDataSource(cvToUse) == null) {
				graph.getMetaData().getFactory().createDataSource(cvToUse);
				fireEventOccurred(new DataSourceMissingEvent(
						"The specified DataSource is not part of the metadata, therefore creating new one.",
						"[Parser - start]"));
			}
		}

		taxidToUse = (String) args.getUniqueValue(ArgumentNames.TAXID_TO_USE_ARG);

		try {
			ReadAllFiles();
			// clean up to enable execute from scratch
			SinkFactory.getInstance().clearCache();
			TransformerFactory.instanceRegister.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a reader for a specific file extension
	 * 
	 * @param fileName
	 * @param inputDir
	 * @param listener
	 * @return AbstractReader - an abstract reader
	 * @throws Exception
	 */
	private AbstractReader getReader(String fileName, String inputDir,
			IParser listener) throws Exception {
		String wholeFileName = inputDir + "/" + fileName;
		if (fileName.substring(fileName.length() - 3).equals("dat"))
			return new DatFileReader(wholeFileName, listener);
		else
			return new ColFileReader(wholeFileName, listener);
	}

	/**
	 * iterates over the fileNames array. In the first step it reads the files
	 * and stores the information in sink objects. In the second step it
	 * transforms the sink objects into concepts and relations.
	 * 
	 * @throws Exception
	 */
	private void ReadAllFiles() throws Exception {
		// file -> sink
		for (Configuration c : configuration) {
			if (c.getFileName() == null)
				continue;
			String fileName = c.getFileName();
			IParser listener = c.getParser();
			File dir = new File((String) args
					.getUniqueValue(FileArgumentDefinition.INPUT_DIR));
			AbstractReader reader = getReader(fileName, dir.getAbsolutePath(),
					listener);
			fireEventOccurred(new GeneralOutputEvent("parse " + fileName,
					getCurrentMethodName()));
			while (reader.hasNext()) {
				reader.next();
			}
		}
		// sink -> concept
		fireEventOccurred(new GeneralOutputEvent("creating concepts",
				getCurrentMethodName()));
		Iterator<AbstractNode> iterator1 = SinkFactory.getInstance().iterator();
		while (iterator1.hasNext()) {
			AbstractNode node = iterator1.next();
			getTransformer(node.getClass().getSimpleName());
			AbstractTransformer transformer = TransformerFactory.getInstance(
					getTransformer(node.getClass().getSimpleName()), this);
			transformer.nodeToConcept(node);
			transformer.addCommonDetailsToConcept(node.getConcept(), node);
		}
		// sink -> relations
		fireEventOccurred(new GeneralOutputEvent(
				"creating relations and adding context information",
				getCurrentMethodName()));
		for (SinkName str : pathwayOrder) {
			Iterator<AbstractNode> iterator = SinkFactory.getInstance()
					.typeIterator(str.name());
			while (iterator.hasNext()) {
				AbstractNode node = iterator.next();
				AbstractTransformer transformer = TransformerFactory
						.getInstance(getTransformer(node.getClass()
								.getSimpleName()), this);
				transformer.pointerToRelationsCore(node);
			}
		}
	}

	/**
	 * returns a transformer for a sink object.
	 * 
	 * @param nodeName
	 * @return
	 * @throws NoSuchElementException
	 */
	private Class<? extends AbstractTransformer> getTransformer(String nodeName)
			throws NoSuchElementException {
		//
		for (Configuration c : this.configuration) {
			if (c.getSinkName().toString().equals(nodeName))
				return c.getTransformer();
		}

		throw new NoSuchElementException();
	}

	/**
	 * returns the ONDEXGraph
	 * 
	 * @return
	 */
	public ONDEXGraph getGraph() {
		return graph;
	}

	/**
	 * no validators are used
	 */
	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	/**
	 * one arguments are used
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition[] {
				new StringArgumentDefinition(ArgumentNames.TAXID_TO_USE_ARG,
						ArgumentNames.TAXID_TO_USE_ARG_DESC, true, null, false),
				new StringArgumentDefinition(ArgumentNames.CV_ARG,
						ArgumentNames.CV_ARG_DESC, false, "unknown", false),
				new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR,
						"directory with BioCyc files", true, true, true, false)

		};
	}

	public static void propagateEventOccurred(EventType et) {
		if (instance != null)
			instance.fireEventOccurred(et);
	}

	/**
	 * Convenience method for outputing the current method name in a dynamic way
	 * 
	 * @return the calling method name
	 */
	public static String getCurrentMethodName() {
		Exception e = new Exception();
		StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
		String name = trace.getMethodName();
		String className = trace.getClassName();
		int line = trace.getLineNumber();
		return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line
				+ "]";
	}
}
