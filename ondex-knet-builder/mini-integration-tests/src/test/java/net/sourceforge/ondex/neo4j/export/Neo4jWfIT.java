package net.sourceforge.ondex.neo4j.export;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.neo4j.driver.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import info.marcobrandizi.rdfutils.jena.SparqlBasedTester;
import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.mini.test.MiniInvoker;
import uk.ac.rothamsted.kg.rdf2pg.cli.Rdf2PGCli;
import uk.ac.rothamsted.neo4j.utils.test.CypherTester;

/**
 * Some integration tests based on test text mining workflows.
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Sep 2017</dd></dl>
 *
 */
@FixMethodOrder ( MethodSorters.NAME_ASCENDING )
public class Neo4jWfIT
{
	private String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + "/";
	private String wfPath = mavenBuildPath + "test-classes/textmining_wf/";		

	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@BeforeClass
	public static void initRDF2Neo4j ()
	{
		// Prevents the CLI from invoking System.exit()
		System.setProperty ( Rdf2PGCli.NO_EXIT_PROP, "true" );
	}
		
	/**
	 * This runs a workflow that loads an OXL and then exports to RDF.
	 * The resulting RDF is used by the Neo4j exporter test. That's why we use the {@link FixMethodOrder} 
	 * annotation.
	 * 
	 */
	@Test
	public void test10_RdfWf ()
	{
		MiniInvoker invoker = new MiniInvoker ();
		invoker.setMiniStartDirPath ( mavenBuildPath + "ondex-mini" );
		invoker.invoke ( wfPath + "tm_rdf_wf.xml" );
		
		// Test the RDF
		Model m = ModelFactory.createDefaultModel ();
		m.read ( mavenBuildPath + "text_mining.ttl" );
				
		SparqlBasedTester tester = new SparqlBasedTester ( m, NamespaceUtils.asSPARQLProlog () );
		
		long nfiles = tester.askFromDirectory ( 
			mavenBuildPath + "/ext-test-files/rdf-export-2/text_mining_tests"
		);
		
		assertTrue ( "No SPARQL test found!", nfiles > 0 );
	}
	
	/**
	 * Loads some RDF into Neo4j and verifies results, using test cases from the Neo4j exporter.
	 */
	@Test
	public void test20_Cypher () throws IOException
	{
		String neoxPath = mavenBuildPath + "ondex-mini/tools/neo4j-exporter/";
		String neoxLocalConfig = mavenBuildPath + "test-classes/textmining_wf/rdf2neo_config.xml";

		// Prepare RDF TDB
		/*
		Dataset tdbDs = TDBFactory.createDataset ( mavenBuildPath + "text_mining_tdb" );
		Model m = tdbDs.getDefaultModel ();
		Txn.executeWrite ( tdbDs, () -> m.read ( mavenBuildPath + "text_mining.ttl" ) );
		*/
		
		// Run the Neo4j importer
		Rdf2PGCli.main ( 
			"--config", "file://" + neoxLocalConfig, 
			"--tdb", mavenBuildPath + "text_mining_tdb",
			mavenBuildPath + "text_mining.ttl"
		);	
		
		// And eventually verify with Cypher
		try ( 
			FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext ( 
				"file://" + neoxLocalConfig
			);
		)
		{
			Driver neoDriver = (Driver) ctx.getBean ( "neoDriver" );
			CypherTester tester = new CypherTester ( neoDriver );
			tester.setCypherHeader ( NamespaceUtils.getNamespaces () );
			long cyCount = tester.askFromDirectory ( 
				f -> Assert.fail ( "Cypher test '" + f.getName () + "' failed!" ), 
				mavenBuildPath + "/ext-test-files/neo4j-exporter/text_mining_cytests"
			);
			assertTrue ( "No Cypher test found!", cyCount > 0 );
		} // try ctx
	} // test20_Cypher
}
