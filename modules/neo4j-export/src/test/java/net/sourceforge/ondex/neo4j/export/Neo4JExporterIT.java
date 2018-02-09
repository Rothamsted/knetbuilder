package net.sourceforge.ondex.neo4j.export;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;

import org.apache.jena.query.Dataset;
import org.apache.jena.system.Txn;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.oxl.Parser;
import net.sourceforge.ondex.rdf.export.RDFFileExporter;
import uk.ac.rothamsted.rdf.neo4j.load.MultiConfigCyLoader;
import uk.ac.rothamsted.rdf.neo4j.load.support.NeoDataManager;

/**
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Nov 2017</dd></dl>
 *
 */
public class Neo4JExporterIT
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
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
			try ( NeoDataManager dataMgr = new NeoDataManager ( tdbPath ); )
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
		
		try ( ConfigurableApplicationContext beanCtx = new ClassPathXmlApplicationContext ( "test_config.xml" ); )
		{			
			MultiConfigCyLoader mloader = beanCtx.getBean ( MultiConfigCyLoader.class );
			mloader.load ( tdbPath );
			// TODO: test
		}
	}
	
	@Test // @Ignore ( "TODO: re-enable later" )
	public void testBasics () throws Exception
	{
		String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + "/";
		String testResPath = mavenBuildPath + "test-classes/";
		
		this.exportOxl ( testResPath + "text_mining.oxl", null );
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
		this.exportOxl (
			null,
			"/Users/brandizi/Documents/Work/RRes/ondex_data/knet_miner_data/wheat_tdb_20180118"
		);
		/*
		this.exportOxl (
			"/Users/brandizi/Documents/Work/RRes/ondex_data/knet_miner_data/WheatKNET.oxl",
			null
		); */
	}
}
