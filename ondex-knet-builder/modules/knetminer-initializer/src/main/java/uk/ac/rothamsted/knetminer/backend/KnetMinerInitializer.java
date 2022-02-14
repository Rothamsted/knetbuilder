package uk.ac.rothamsted.knetminer.backend;

import java.util.HashMap;
import java.util.Set;

import net.sourceforge.ondex.algorithm.graphquery.AbstractGraphTraverser;
import net.sourceforge.ondex.algorithm.graphquery.GraphTraverser;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import uk.ac.ebi.utils.collections.OptionsMap;

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
		// TODO, see KnetMiner code
	}
	
	/**
	 * Indexes the {@link #getGraph() graph} using {@link LuceneEnv}
	 */
	private void createLuceneData ()
	{
		// TODO with code from Knetminer see https://github.com/Rothamsted/knetbuilder/issues/55
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
		return null;
	}
	
	
	/**
	 * Runs the {@link #getGraphTraverser() traverser} and saves the results.
	 */
	private void createTraverserData ()
	{
		// TODO with code from Knetminer see https://github.com/Rothamsted/knetbuilder/issues/55
		// Use this when needed:
		var traverser = this.initGraphTraverser ();
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
 
}
