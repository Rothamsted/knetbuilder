package uk.ac.rothamsted.knetminer.backend;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import rres.knetminer.datasource.ondexlocal.config.KnetminerConfiguration;
import uk.ac.ebi.utils.collections.OptionsMap;
import uk.ac.ebi.utils.exceptions.ExceptionUtils;
import uk.ac.ebi.utils.exceptions.UnexpectedValueException;
import uk.ac.ebi.utils.io.SerializationUtils;
import uk.ac.ebi.utils.runcontrol.PercentProgressLogger;

/**
 * 
 * The core functionality.
 * 
 * This realises the core functions of creating the Lucene Index for an OXL, running a 
 * {@link AbstractGraphTraverser graph traverser} and saving the corresponding output.
 * 
 * As described in the POM, these are off-line data initialisations that benefit KnetMiner. 
 *
 * TODO: probably some methods require thread safety (get {@link #getGraph()}.
 * 
 * @author brandizi
 * @author jojicunnunni
 * 
 * <dl><dt>Date:</dt><dd>13 Feb 2022</dd></dl>
 *
 */

public class KnetMinerInitializer
{
	
	private ONDEXGraph graph;
	
	private String configYmlPath;
	private KnetminerConfiguration config = null ;
	
	private LuceneEnv luceneMgr;
	
	private AbstractGraphTraverser graphTraverser;	
	
	private Map<Integer, Set<Integer>> genes2Concepts;
	private Map<Integer, Set<Integer>> concepts2Genes;
	private Map<Pair<Integer, Integer>, Integer> genes2PathLengths;
	
	
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	/**
	 * Does all the initialisation work, by calling initXXX() methods.
	 * 
	 * @param overridingConfig if not null, sets up the Knetminer configuration to work with with this 
	 * object and proceeds with the initialisation. If it's null, it gets the configuration from 
	 * {@link #loadKnetminerConfiguration()}, that is, uses {@link #getConfigYmlPath() the configuration path}.
	 * 
	 */
	public void initKnetMinerData ( KnetminerConfiguration overridingConfig )
	{	
		if ( overridingConfig == null ) this.loadKnetminerConfiguration (); 
		else this.config = overridingConfig;
		
		this.initLuceneData ();
		this.initSemanticMotifData ();
	}
	
	/**
	 * Defaults to null, ie, loads the configuration from {@link #getConfigYmlPath()}.
	 */
	public void initKnetMinerData ()
	{
		this.initKnetMinerData ( null );
	}

	/**
	 *	Loads a {@link KnetminerConfiguration} from {@link #getConfigYmlPath()}.
	 *	The method has no effect if {@link #getKnetminerConfiguration()} is null.
	 *  
	 */
	public void loadKnetminerConfiguration ()
	{
		if ( this.configYmlPath == null ) return;
		config  = KnetminerConfiguration.load ( configYmlPath );
	}
	
	/**
	 * Defaults to false.
	 */
	public void initLuceneData ()
	{
		initLuceneData ( false );
	}

	
	/**
	 * Indexes the {@link #getGraph() graph} using {@link LuceneEnv}.
	 * Requires {@link #loadOptions()} to be invoked before this.
	 * 
	 * @param doReset, when true, forces the recreation of the index files, even if they exist. Else, re-index
	 * only if the index directory doesn't exist yet. 
	 * 
	 */
	public void initLuceneData ( boolean doReset )
	{
		try 
		{
			String dataDirPath = this.config.getDataDirPath ();
			if( dataDirPath == null ) throw new UnexpectedValueException ( "dataDirPath is null" );
	
			File indexFile = Paths.get ( dataDirPath, "index" ).toFile();

      // We don't have the OXL file path here, so, let's give up with checking its date. 
      // we can do that in the data building pipeline
      //
      if ( indexFile.exists() )
      {
    	  if ( !doReset ) {
    		log.info ( "Skipping Ondex/Lucene indexing and reusing existing index files" );
    		return;
    	  }
        log.info("Graph file updated since index last built, deleting old index");
        FileUtils.deleteDirectory ( indexFile );
      }
  
      log.info ( "Building Lucene Index: " + indexFile.getAbsolutePath() );
      this.luceneMgr = new LuceneEnv ( indexFile.getAbsolutePath(), !indexFile.exists() );
      luceneMgr.addONDEXListener( new ONDEXLogger() ); // sends Ondex messages to the logger.
      luceneMgr.setONDEXGraph ( graph );
      luceneMgr.setReadOnlyMode ( true );
		
		  log.info ( "Ondex graph indexed" );
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
		String seedGenesPath = StringUtils.trimToNull ( getKnetminerConfiguration ().getSeedGenesFilePath () );
		if ( seedGenesPath == null ) {
			log.info ( "Initialising seed genes from TAXID list: {}", this.config.getServerDatasetInfo ()
					.getTaxIds () );
			return fetchSeedGenesFromTaxIds ();
		}
		
		log.info ( "Initialising seed genes from file: '{}' ", seedGenesPath );
		return AbstractGraphTraverser.ids2Genes ( this.graph, seedGenesPath );
	}
	
	private Set<ONDEXConcept> fetchSeedGenesFromTaxIds ()
	{
		ONDEXGraphMetaData meta = graph.getMetaData ();
		ConceptClass ccGene = meta.getConceptClass ( "Gene" );
		AttributeName attTaxId = meta.getAttributeName ( "TAXID" );

		Set<ONDEXConcept> genes = graph.getConceptsOfConceptClass ( ccGene );
		
		Set<String> taxIds = this.config.getServerDatasetInfo ()
				.getTaxIds ();
		
		return genes
			.parallelStream ()
			.filter ( gene -> gene.getAttribute ( attTaxId ) != null )
			.filter ( gene -> taxIds.contains ( gene.getAttribute ( attTaxId ).getValue ().toString () ) )
			.collect ( Collectors.toSet () );
	}	
	

	/**
	 * Defaults to false
	 */
	public void initSemanticMotifData ()
	{
		initSemanticMotifData ( false );
	}


  /**
   * Populates internal data about semantic motif paths, either using the configured {@link AbstractGraphTraverser}
   * or loading previously saved data from files.
   * 
   * This is made public for the purpose of re-running a traverser with different options (eg, the CypherDebugger
   * sets new queries and then re-runs).
   * 
   * If doReset is true it always regenerate the data, without considering existing computed
   * files. This is used by components like CypherDebugger.
   * 
   * This can be called independently of {@link #initKnetMinerData()}, to trigger a new traversal, however,
   * that requires proper initialisation already in place, ie, {@link #loadOptions()}.
   * 
   */
	@SuppressWarnings ( "rawtypes" )
	public void initSemanticMotifData ( boolean doReset )
	{
		log.info ( "Initializing semantic motif data" );
		
		var dataPath = this.getDataDirPath ();
		
		File fileConcept2Genes = Paths.get ( dataPath, "concepts2Genes.ser" ).toFile ();
		File fileGene2Concepts = Paths.get ( dataPath, "genes2Concepts.ser" ).toFile ();
		File fileGene2PathLength = Paths.get ( dataPath, "genes2PathLengths.ser" ).toFile ();
		

		// Again, we don't have the OXL path here, so let's not check it.
		if ( doReset )
		{
			log.info ( "(Re)creating semantic motif files, as per doReset flag" );
			fileConcept2Genes.delete ();
			fileGene2Concepts.delete ();
			fileGene2PathLength.delete ();
		}

		if ( !fileConcept2Genes.exists () )
		{
			log.info ( "Generating semantic motif result files" );
			var seedGenes = this.getSeedGenes ();
			
			
			log.info ( "Creating semantic motif data" );
			
			// We're going to need a lot of memory, so delete this in advance
			// (CyDebugger might trigger this multiple times)
			//
			this.concepts2Genes = new HashMap<> ();
			this.genes2Concepts = new HashMap<> ();
			this.genes2PathLengths = new HashMap<> ();

			// the results give us a map of every starting concept to every
			// valid path.
			var graphTraverser = this.getGraphTraverser ();
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
				}); // paths.removeIf ()
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
				ExceptionUtils.throwEx ( 
					RuntimeException.class, e, "Failed while reading internal map files: %s", e.getMessage ()
				);
			}
		}
		
		BiConsumer<String, Map<?,?>> nullChecker = (name, coll) -> 
		{
			if ( coll == null || coll.isEmpty () ) log.warn ( "{} is null/empty", name );
			else log.info ( "{} populated with {} elements", name, coll.size () );
		};
		
		nullChecker.accept ( "genes2Concepts", genes2Concepts );
		nullChecker.accept ( "concepts2Genes", concepts2Genes );
		nullChecker.accept ( "genes2PathLengths", genes2PathLengths );
		
		log.info ( "Semantic motif data initialization ended." );
		
		// this remains in KnetMiner for the moment. Pushing it down to Ondex will require:
		// 1) That GeneHelper is also moved to some utility class (which Knetminer module? To be decided)
		// 2) that this is also saved on disk: Map<Integer, Set<Integer>> genes2QTLs
		// postInit ( seedGenes );
	}
	 
	
	/**
	 * The graph to work with. This has to be loaded separately, via 
	 * {@code net.sourceforge.ondex.parser.oxl.Parser}. See {@code KnetMinerInitializer}.
	 * 
	 */
	public ONDEXGraph getGraph ()
	{
		return graph;
	}


	public void setGraph ( ONDEXGraph graph )
	{
		this.graph = graph;
	}
	
	/**
	 * The path to the KnetMiner configuration file. This is used by {@link #loadKnetminerConfiguration()} and
	 * indirectly, by {@link #initKnetMinerData()}. 
	 * 
	 * See the test directory in the Maven project for examples of this file.
	 * 
	 */
	public String getConfigYmlPath ()
	{
		return configYmlPath;
	}


	public void setConfigYmlPath ( String configYmlPath )
	{
		this.configYmlPath = configYmlPath;
	}


	/**
	 * The Knetminer configuration used to manage the initialisation process.
	 * 
	 * @see {@link #initKnetMinerData(KnetminerConfiguration)}. This can be either passed at initialisation time, or
	 * loaded from {@link #getConfigYmlPath()}.
	 * 
	 */
	public KnetminerConfiguration getKnetminerConfiguration ()
	{
		return config;
	}	

	/**
	 * Uses {@link #getGraphTraverserFQN()} to initialise the graph traverser.
	 * This is used by {@link #initSemanticMotifData(boolean)}.
	 * 
	 */
	public AbstractGraphTraverser getGraphTraverser ()
	{
		if ( this.graphTraverser != null ) return graphTraverser;
		
		OptionsMap traverserOpts = this.config.getGraphTraverserOptions ();
		return graphTraverser =  AbstractGraphTraverser.getInstance ( traverserOpts );
	}

	public LuceneEnv getLuceneMgr ()
	{
		return luceneMgr;
	}

	public Map<Integer, Set<Integer>> getGenes2Concepts ()
	{
		return genes2Concepts;
	}

	public Map<Integer, Set<Integer>> getConcepts2Genes ()
	{
		return concepts2Genes;
	}

	public Map<Pair<Integer, Integer>, Integer> getGenes2PathLengths ()
	{
		return genes2PathLengths;
	}
	
}
