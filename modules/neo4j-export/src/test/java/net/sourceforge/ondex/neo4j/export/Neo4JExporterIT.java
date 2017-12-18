package net.sourceforge.ondex.neo4j.export;

import org.junit.Test;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.oxl.Parser;
import uk.ac.ebi.utils.io.IOUtils;

/**
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Nov 2017</dd></dl>
 *
 */
public class Neo4JExporterIT
{
	@Test
	public void testBasics () throws Exception
	{
		try (
			Driver neoDriver = GraphDatabase.driver( "bolt://127.0.0.1:7687", AuthTokens.basic ( "neo4j", "test" ) );
		) 
		{ 
			String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + "/";
			String testResPath = mavenBuildPath + "test-classes/";
			
			ONDEXGraph g = Parser.loadOXL ( testResPath + "text_mining.oxl" );
			
			Neo4jExporter neox = new Neo4jExporter ();
			neox.setLabelSparqlQuery ( IOUtils.readResource ( "node_labels.sparql" ) );
			neox.setNodePropSparqlQuery ( IOUtils.readResource ( "node_props.sparql" ) );
			neox.setNeo4jDriver ( neoDriver );

			neox.export ( g );
		}
	}
}
