package net.sourceforge.ondex.neo4j.export;

import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.oxl.Parser;
import uk.ac.ebi.utils.io.IOUtils;
import uk.ac.rothamsted.rdf.neo4j.load.support.CyNodeLoadingHandler;
import uk.ac.rothamsted.rdf.neo4j.load.support.CyRelationLoadingHandler;

/**
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Nov 2017</dd></dl>
 *
 */
public class Neo4JExporterIT
{
	private void exportOxl ( String oxlPath ) throws Exception
	{
		try (
			Driver neoDriver = GraphDatabase.driver( "bolt://127.0.0.1:7687", AuthTokens.basic ( "neo4j", "test" ) );
		) 
		{ 
			ONDEXGraph g = Parser.loadOXL ( oxlPath );
			
			Neo4jExporter neox = new Neo4jExporter ();

			CyNodeLoadingHandler cyNodehandler = neox.getCyNodeLoadingHandler ();
			CyRelationLoadingHandler cyRelhandler = neox.getCyRelationLoadingHandler ();

			String defaultLabel = "Concept";
			cyNodehandler.setDefaultLabel ( defaultLabel );
			cyRelhandler.setDefaultLabel ( defaultLabel );
			
			cyNodehandler.setNodeIrisSparql ( IOUtils.readResource ( "node_iris.sparql" ) );
			cyNodehandler.setLabelsSparql ( IOUtils.readResource ( "node_labels.sparql" ) );
			cyNodehandler.setNodePropsSparql ( IOUtils.readResource ( "node_props.sparql" ) );
			cyNodehandler.setNeo4jDriver ( neoDriver );
			
			cyRelhandler.setRelationTypesSparql ( IOUtils.readResource ( "rel_types.sparql" ) );
			cyRelhandler.setRelationPropsSparql ( IOUtils.readResource ( "rel_props.sparql" ) );
			cyRelhandler.setNeo4jDriver ( neoDriver );
			
			neox.setRDFChunkSize ( 500000 );
			neox.setCypherChunkSize ( 10000 );
			
			neox.export ( g );
		}		
	}
	
	@Test
	public void testBasics () throws Exception
	{
		String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + "/";
		String testResPath = mavenBuildPath + "test-classes/";
		
		this.exportOxl ( testResPath + "text_mining.oxl" );
	}

	/**
	 * 4Mb OXL 
	 */
	@Test //@Ignore ( "Not a real unit test, time consuming" )
	public void testAraCycBioPax () throws Exception
	{
		this.exportOxl (
			"/Users/brandizi/Documents/Work/RRes/ondex_data/fungi_net_miner/examples_20170314/aracyc/Protein-Pathway.oxl"
		);
	}
	
	/**
	 * 127Mb OXL 
	 */
	@Test //@Ignore ( "Not a real unit test, time consuming" )
	public void testAraKnetMiner () throws Exception
	{
		this.exportOxl (
			"/Users/brandizi/Documents/Work/RRes/ondex_data/knet_miner_data/ArabidopsisKNET_201708.oxl"
		);
	}
}
