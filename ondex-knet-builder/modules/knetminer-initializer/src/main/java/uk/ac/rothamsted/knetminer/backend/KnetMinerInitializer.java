package uk.ac.rothamsted.knetminer.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.sourceforge.ondex.algorithm.graphquery.AbstractGraphTraverser;
import net.sourceforge.ondex.algorithm.graphquery.GraphTraverser;
import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.logging.ONDEXLogger;
import rres.knetminer.datasource.ondexlocal.service.utils.GeneHelper;
import uk.ac.ebi.utils.collections.OptionsMap;
import uk.ac.ebi.utils.exceptions.ExceptionUtils;
import uk.ac.ebi.utils.io.SerializationUtils;
import uk.ac.ebi.utils.runcontrol.PercentProgressLogger;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>13 Feb 2022</dd></dl>
 *
 */
public class KnetMinerInitializer
{
	private ONDEXGraph graph;
	
	private String dataPath;
	

	private String graphTraverserFQN;

	
	private String configXmlPath;

	
	private OptionsMap options = OptionsMap.create ();
	
	
	private LuceneEnv luceneMgr;
	
	private final Logger log = LogManager.getLogger ( getClass() );	
	
	private Map<Integer, Set<Integer>> genes2Concepts;
	private Map<Integer, Set<Integer>> concepts2Genes;
	private Map<Pair<Integer, Integer>, Integer> genes2PathLengths;
	private Map<Integer, Set<Integer>> genes2QTLs;
	
	public static final String OPT_SEED_GENES_FILE = "seedGenesFile";
	
	 private List<String> taxIds = null;
	
	public void createKnetMinerData ()
	{	
		this.loadOptions ();
		this.createLuceneData ();
		this.createTraverserData ();
	}
	
	/**
	 *  Populates {@link #getOptions()} with the variables defined in  
	 *  the {@link #getConfigXmlPath() KnetMiner config file}.
	 */
	private void loadOptions ()
	{
		try
		{
			Properties props = new Properties ();
			InputStream in = new FileInputStream ( this.configXmlPath );
			props.loadFromXML ( in );
			this.options = OptionsMap.from ( props );
		}
		catch ( IOException ex )
		{
			ExceptionUtils.throwEx ( 
				UncheckedIOException.class, ex,
				"Error while loading data source configuration from '%s': %s",
				configXmlPath,
				ex.getMessage ()
			);
		}		
	}
	
	/**
	 * Indexes the {@link #getGraph() graph} using {@link LuceneEnv}
	 */
	private void createLuceneData ()
	{
		// TODO with code from Knetminer see https://github.com/Rothamsted/knetbuilder/issues/55
		try 
		    {
		      // index the Ondex graph
		      File graphFile = new File ( configXmlPath );
		      File indexFile = Paths.get ( dataPath, "index" ).toFile();
		      if (indexFile.exists() && (indexFile.lastModified() < graphFile.lastModified())) {
		          log.info("Graph file updated since index last built, deleting old index");
		          FileUtils.deleteDirectory(indexFile);
		      }
		      log.info("Building Lucene Index: " + indexFile.getAbsolutePath());
		      luceneMgr = new LuceneEnv(indexFile.getAbsolutePath(), !indexFile.exists());
		      luceneMgr.addONDEXListener( new ONDEXLogger() ); // sends Ondex messages to the logger.
		      luceneMgr.setONDEXGraph ( graph );
		      luceneMgr.setReadOnlyMode ( true );

		      log.info ( "Ondex graph indexed");
		    } 
		    catch (Exception e)
		    {
		      log.error ( "Error while loading/creating graph index: " + e.getMessage (), e );
		      ExceptionUtils.throwEx (
		      	RuntimeException.class, e, "Error while loading/creating graph index: %s", e.getMessage ()
		      ); 
		    }
	}
	
	/**
	 * Gets the genes to start the {@link #createTraverserData() traverser from}.
	 * 
	 * This comes from either one of the two sources:
	 *  
	 * - the 'seedGenesFile' {@link #getOptions() option}, if this is defined
	 * - else the 'SpeciesTaxId' {@link #getOptions() option} 
	 *
	 */
	public Set<ONDEXConcept> getSeedGenes ()
	{
		// TODO: implement this, see the KnetMiner code.
		String seedGenesPath = StringUtils.trimToNull ( getOptions ().getString ( OPT_SEED_GENES_FILE ) );
		if ( seedGenesPath == null ) {
			log.info ( "Initialising seed genes from TAXID list" );
			return fetchSeedGenesFromTaxIds ();
		}
		
		log.info ( "Initialising seed genes from file: '{}' ", seedGenesPath );
		return AbstractGraphTraverser.ids2Genes ( getGraph (), seedGenesPath );
	}
	
	private Set<ONDEXConcept> fetchSeedGenesFromTaxIds ()
	{
		Set<String> myTaxIds = new HashSet<> ( taxIds );
		ONDEXGraphMetaData meta = graph.getMetaData ();
		ConceptClass ccGene = meta.getConceptClass ( "Gene" );
		AttributeName attTaxId = meta.getAttributeName ( "TAXID" );

		Set<ONDEXConcept> genes = graph.getConceptsOfConceptClass ( ccGene );
		return genes.parallelStream ().filter ( gene -> gene.getAttribute ( attTaxId ) != null )
			.filter ( gene -> myTaxIds.contains ( gene.getAttribute ( attTaxId ).getValue ().toString () ) )
			.collect ( Collectors.toSet () );
	}	
	
	
	/**
	 * Runs the {@link #getGraphTraverser() traverser} and saves the results.
	 */
	private void createTraverserData ()
	{
		// TODO with code from Knetminer see https://github.com/Rothamsted/knetbuilder/issues/55
		// Use this when needed:
		var traverser = this.initGraphTraverser ();
		traverser.setOption ( "ONDEXGraph", getGraph () );
		loadTaxIds();
		initSemanticMotifData(traverser,true);
	}

	/**
	 * The graph to work with. This has to be loaded separately, via 
	 * {@code net.sourceforge.ondex.parser.oxl.Parser}. See {@code KnetMinerInitializer}.
	 * 
	 */
	ONDEXGraph getGraph ()
	{
		return graph;
	}


	public void setGraph ( ONDEXGraph graph )
	{
		this.graph = graph;
	}

	/**
	 * The directory where data are saved. This overrides the 'DataPath' {@link #getOptions() option}. Hence, you 
	 * might either set this programmatically or use a {@link #getConfigXmlPath() config file}.  
	 *  
	 */
	public String getDataPath ()
	{
		if ( this.dataPath != null ) return dataPath;
		return this.options.getString ( "DataPath" );
	}


	public void setDataPath ( String dataPath )
	{
		this.dataPath = dataPath;
	}

	/**
	 * The FQN name for the {@link AbstractGraphTraverser graph traverser} to be used for the 
	 * {@link #getGraphTraverser()}. If left undefined, the 'GraphTraverserClass' {@link #getOptions() option}
	 * will be used, if this is null too, the {@link GraphTraverser default traverser} will be used.
	 *  
	 */
	public String getGraphTraverserFQN ()
	{
		if ( this.graphTraverserFQN != null ) return this.graphTraverserFQN;
		return this.options.getOpt ( "GraphTraverserClass" );
	}


	public void setGraphTraverserFQN ( String graphTraverserFQN )
	{
		this.graphTraverserFQN = graphTraverserFQN;
	}

	/**
	 * Uses {@link #getGraphTraverserFQN()} to initialise the graph traverser.
	 * This is used by {@link #createKnetMinerData()}.
	 */
	private AbstractGraphTraverser initGraphTraverser ()
	{
		var optsCopy = new HashMap<> ( this.options );
		if ( this.graphTraverserFQN != null ) options.put ( "GraphTraverserClass", graphTraverserFQN );
		return AbstractGraphTraverser.getInstance ( optsCopy );
	}
	
	/**
	 * The KnetMiner configuration file. This is is necessary to define various options related to 
	 * KnetMiner, including {@link #getDataPath() data output directory} 
	 * {@link #getSeedGenes() seed-related options} and traverser-related options like 'StateMachineFilePath'.
	 * 
	 * This is used by {@link #loadOptions()} to populate {@link #getOptions()}.
	 * 
	 * See the test directory in the Maven project for examples of this file.
	 * 
	 */
	public String getConfigXmlPath ()
	{
		return configXmlPath;
	}


	public void setConfigXmlPath ( String configXmlPath )
	{
		this.configXmlPath = configXmlPath;
	}


	/**
	 * The options coming from {@link #getConfigXmlPath()}. Once loaded, these can be overridden
	 * programmatically. For some of the options, there are explicit setters hereby, eg, 
	 * {@link #setDataPath(String)}. 
	 */
	public OptionsMap getOptions ()
	{
		return options;
	}
	
	@SuppressWarnings ( "rawtypes" )
	public void initSemanticMotifData (AbstractGraphTraverser graphTraverser,  boolean doReset )
	{
		log.info ( "Initializing semantic motif data" );
		
		File graphFile = new File ( graph.getName() );
		File fileConcept2Genes = Paths.get ( dataPath, "concepts2Genes" ).toFile ();
		File fileGene2Concepts = Paths.get ( dataPath, "genes2Concepts" ).toFile ();
		File fileGene2PathLength = Paths.get ( dataPath, "genes2PathLengths" ).toFile ();
		log.info ( "Generate HashMap files: concepts2Genes & genes2Concepts..." );

		var seedGenes = this.loadSeedGenes ();

		if ( doReset || fileConcept2Genes.exists () && ( fileConcept2Genes.lastModified () < graphFile.lastModified () ) )
		{
			log.info ( "(Re)creating semantic motif files, due to {}", doReset ? "reset flag invocation" : "outdated files" );
			fileConcept2Genes.delete ();
			fileGene2Concepts.delete ();
			fileGene2PathLength.delete ();
		}

		if ( !fileConcept2Genes.exists () )
		{
			log.info ( "Creating semantic motif data" );
			// We're going to need a lot of memory, so delete this in advance
			// (CyDebugger might trigger this multiple times)
			//
			concepts2Genes = new HashMap<> ();
			genes2Concepts = new HashMap<> ();
			genes2PathLengths = new HashMap<> ();

			// the results give us a map of every starting concept to every
			// valid path.
			Map<ONDEXConcept, List<EvidencePathNode>> traverserPaths = graphTraverser.traverseGraph ( graph, seedGenes, null );

			// Performance stats reporting about the Cypher-based traverser is disabled after the initial
			// traversal. This option has no effect when the SM-based traverser is used.
			graphTraverser.setOption ( "performanceReportFrequency", -1 );

			log.info ( "Also, generate geneID//endNodeID & pathLength in HashMap genes2PathLengths..." );
			var progressLogger = new PercentProgressLogger ( 
				"{}% of paths stored", traverserPaths.values ().size () 
			);
			for ( List<EvidencePathNode> paths : traverserPaths.values () )
			{
				// We dispose them after use, cause this is big and causing memory overflow issues
				paths.removeIf ( path -> {

					// search last concept of semantic motif for keyword
					ONDEXConcept gene = (ONDEXConcept) path.getStartingEntity ();

					// add all semantic motifs to the new graph
					// Set<ONDEXConcept> concepts = path.getAllConcepts();
					// Extract pathLength and endNode ID.
					int pathLength = ( path.getLength () - 1 ) / 2; // get Path Length
					ONDEXConcept con = (ONDEXConcept) path.getConceptsInPositionOrder ()
						.get ( path.getConceptsInPositionOrder ().size () - 1 );

					int lastConID = con.getId (); // endNode ID.
					int geneId = gene.getId ();
					var gplKey = Pair.of ( geneId, lastConID );
					genes2PathLengths.merge ( gplKey, pathLength, Math::min );

					genes2Concepts.computeIfAbsent ( geneId, thisGeneId -> new HashSet<> () )
					.add ( lastConID );
					
					concepts2Genes.computeIfAbsent ( lastConID, thisGeneId -> new HashSet<> () )
					.add ( geneId );

					// ALWAYS return this to clean up memory (see above)
					return true;
				} ); // paths.removeIf ()
				progressLogger.updateWithIncrement ();
			} // for traverserPaths.values()

			try
			{
				
				 SerializationUtils.serialize ( fileConcept2Genes, concepts2Genes );
				 SerializationUtils.serialize ( fileGene2Concepts, genes2Concepts );
				 SerializationUtils.serialize ( fileGene2PathLength, genes2PathLengths );
				 
			}
			catch ( Exception ex )
			{
				log.error ( "Failed while creating internal map files: " + ex.getMessage (), ex );
				ExceptionUtils.throwEx ( 
					RuntimeException.class, ex, "Failed while creating internal map files: %s", ex.getMessage () 
				);
			}
		} 
		else
		{
			// Files exist and are up-to-date, try to read them
			//
			log.info ( "Loading semantic motif data from existing support files" );
			
			try
			{
				
				 concepts2Genes = SerializationUtils.deserialize ( fileConcept2Genes );
				 genes2Concepts = SerializationUtils.deserialize ( fileGene2Concepts );
				 genes2PathLengths = SerializationUtils.deserialize ( fileGene2PathLength );
				 
			}
			catch ( Exception e )
			{
				log.error ( "Failed while reading internal map files: " + e.getMessage (), e );
				ExceptionUtils.throwEx ( 
					RuntimeException.class, e, "Failed while reading internal map files: %s", e.getMessage ()
				);
			}
		}
		
		log.info ( "Semantic motif data initialization ended, post-processing" );

		// Moving forward with putting motif data in place.
		postInit ( seedGenes );
	}
	
	private void postInit ( Set<ONDEXConcept> seedGenes )
	{
		ConceptClass ccQTL = graph.getMetaData ().getConceptClass ( "QTL" );
		Set<ONDEXConcept> qtls = ccQTL == null ? new HashSet<> () : graph.getConceptsOfConceptClass ( ccQTL );
		
		BiConsumer<String, Map<?,?>> nullChecker = (name, coll) -> 
		{
			if ( coll == null || coll.isEmpty () ) log.warn ( "{} is null", name );
			else log.info ( "{} populated with {} elements", name, coll.size () );
		};
		
		nullChecker.accept ( "genes2Concepts", genes2Concepts );
		nullChecker.accept ( "concepts2Genes", concepts2Genes );
		nullChecker.accept ( "genes2PathLengths", genes2PathLengths );

		
		log.info ( "Creating Gene2QTL map" );

		genes2QTLs = new HashMap<> ();
		PercentProgressLogger progressLogger = new PercentProgressLogger ( "{}% of genes processed", seedGenes.size () );
		
		for ( ONDEXConcept gene : seedGenes )
		{
			GeneHelper geneHelper = new GeneHelper ( graph, gene );
			String geneChromosome = geneHelper.getChromosome ();
			if ( geneChromosome == null ) continue;
			
			int gbegin = geneHelper.getBeginBP ( true );
			int gend = geneHelper.getEndBP ( true );

			for ( ONDEXConcept qtl: qtls )
			{
				GeneHelper qtlHelper = new GeneHelper ( graph, qtl );
				if ( ! ( gbegin >= qtlHelper.getBeginBP ( true ) ) ) continue;
				if ( ! ( gend <= qtlHelper.getEndBP ( true ) ) ) continue;
				
				genes2QTLs.computeIfAbsent ( gene.getId (), thisQtlId -> new HashSet<> () )
				.add ( qtl.getId () );
			}
			progressLogger.updateWithIncrement ();
		}

		log.info ( "Populated Gene2QTL with {} mapping(s)", genes2QTLs.size () );		
		log.info ( "End of semantic motif initialization post-processing" );		
	}
	
	private void loadTaxIds(){
		this.taxIds = this.options.getOpt ( "SpeciesTaxId", List.of (), s -> List.of ( s.split ( "," ) ) );
	}
	
	/**
	 * Gets the genes to seed the {@link AbstractGraphTraverser semantic motif traverser}.
	 * 
	 * If the {@link SemanticMotifDataService#OPT_SEED_GENES_FILE} is set in opts, gets such list from
	 * {@link AbstractGraphTraverser#ids2Genes(ONDEXGraph, java.io.File) the corresponding file}, else
	 * it gets all the genes in graph that have their TAXID attribute within the taxIds list, as per 
	 * {@link #getSeedGenesFromTaxIds(ONDEXGraph, List)}.
	 * 
	 */
	private Set<ONDEXConcept> loadSeedGenes ()
	{
		String seedGenesPath = StringUtils.trimToNull ( getOptions ().getString ( OPT_SEED_GENES_FILE ) );
		if ( seedGenesPath == null ) {
			log.info ( "Initialising seed genes from TAXID list" );
			return fetchSeedGenesFromTaxIds ();
		}
		
		log.info ( "Initialising seed genes from file: '{}' ", seedGenesPath );
		return AbstractGraphTraverser.ids2Genes ( getGraph (), seedGenesPath );
	}
	
	
	
 
}
