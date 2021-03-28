package net.sourceforge.ondex.neo4j.export;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;

import org.apache.jena.query.Dataset;
import org.apache.jena.system.Txn;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.oxl.Parser;
import net.sourceforge.ondex.rdf.export.RDFFileExporter;
import uk.ac.rothamsted.kg.rdf2pg.neo4j.load.MultiConfigNeo4jLoader;
import uk.ac.rothamsted.kg.rdf2pg.neo4j.load.support.Neo4jDataManager;
import uk.ac.rothamsted.kg.rdf2pg.pgmaker.spring.PGMakerSessionScope;
import uk.ac.rothamsted.kg.rdf2pg.pgmaker.support.rdf.RdfDataManager;
import uk.ac.rothamsted.neo4j.utils.test.CypherTester;

/**
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Nov 2017</dd></dl>
 *
 */
public class Neo4JExporterIT
{
	private ConfigurableApplicationContext springContext = null;
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	/**
	 * Gets the Spring configuration from a test file and uses it throughout the tests.
	 */
	@Before
	public void initSpringContext () {
		springContext = new ClassPathXmlApplicationContext ( "test_config.xml" );
	}
	
	@After
	public void closeSpringContext () {
		if ( springContext != null ) springContext.close ();
	}
	
	private void exportOxl ( String oxlPath, String tdbPath ) throws Exception
	{
		if ( tdbPath == null )
			// Non-null when you're loading from an existing one (and you don't need to populate it)
			tdbPath = "target/test_tdb";
		
		if ( oxlPath != null )
		{
			// export an OXL as RDF
			log.info ( "Loading OXL file {}", oxlPath );
			ONDEXGraph graph = Parser.loadOXL ( oxlPath );

			String rdfPath = "target/export_test.ttl";
			log.info ( "Export RDF to {}", rdfPath );
			RDFFileExporter rdfx = new RDFFileExporter ();
			rdfx.export ( graph, rdfPath, "TURTLE_BLOCKS" );

			log.info ( "Loading the RDF into {}", tdbPath );
			Reader rdfReader = new BufferedReader ( new FileReader ( rdfPath ), (int) 1E6 );
			try ( var dataMgr = new RdfDataManager ( tdbPath ); )
			{
				Dataset dataSet = dataMgr.getDataSet ();
				Txn.executeWrite ( 
					dataSet, 
					() -> dataSet.getDefaultModel ().read ( 
						rdfReader,
						null,
						"TURTLE"
					)
				);
			}
		}
		
		log.info ( "Exporting to Neo4j from the TDB" );			
			
		try ( var mloader = springContext.getBean ( MultiConfigNeo4jLoader.class ) ) {
			mloader.load ( tdbPath );
		}
	}
	
	@Test // @Ignore ( "TODO: re-enable later" )
	public void testBasics () throws Exception
	{
		String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + "/";
		String testResPath = mavenBuildPath + "test-classes/";
		
		this.exportOxl ( testResPath + "text_mining.oxl", null );
		
		// Re-open a data manager 
		springContext.getBean ( PGMakerSessionScope.class ).startSession ();
		var neoDataMgr = springContext.getBean ( Neo4jDataManager.class );

		// And hand it off the Cypher tester
		CypherTester tester = new CypherTester ( neoDataMgr.getDelegateMgr () );
		tester.setCypherHeader ( NamespaceUtils.getNamespaces () );
		
		long cyFilesCount = tester.askFromDirectory ( 
			f -> Assert.fail ( "Cypher test '" + f.getName () + "' not passed!" ),
			testResPath + "text_mining_cytests" 
		);
		
		Assert.assertEquals ( "Wrong no. of Test Cypher files!", 1, cyFilesCount );
	}

	/**
	 * 4Mb OXL 
	 */
	@Test @Ignore ( "Not a real unit test, time consuming" )
	public void testAraCycBioPax () throws Exception
	{
		this.exportOxl (
			"/Users/brandizi/Documents/Work/RRes/ondex_data/fungi_net_miner/examples_20170314/aracyc/Protein-Pathway.oxl",
			null
		);
	}
	
	/**
	 * 127Mb OXL 
	 */
	@Test @Ignore ( "Not a real unit test, time consuming" )
	public void testAraKnetMiner () throws Exception
	{
		/*this.exportOxl (
			"/Users/brandizi/Documents/Work/RRes/ondex_data/knet_miner_data/ArabidopsisKNET_201708.oxl",
			null	
		);*/
		this.exportOxl (
			null,
			"/Users/brandizi/Documents/Work/RRes/ondex_data/knet_miner_data/ara_tdb_20180119"
		);
	}

	/**
	 * 279Mb OXL 
	 */
	@Test @Ignore ( "Not a real unit test, time consuming" )
	public void testWheatKnetMiner () throws Exception
	{
		String src = "/Users/brandizi/Documents/Work/RRes/ondex_data/knet_miner_data/wheat_tdb_20180118";
		src = "/tmp/wheat_tdb_20180118";
		
		this.exportOxl ( null, src );
	}
}
