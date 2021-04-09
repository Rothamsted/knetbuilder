package net.sourceforge.ondex.neo4j.export;

import static java.lang.System.out;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

import info.marcobrandizi.rdfutils.jena.ModelEndPointHelper;
import uk.ac.ebi.utils.io.IOUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>9 Jan 2018</dd></dl>
 *
 */
public class TdbIT
{
	@Test @Ignore ( "Not a real Unit test" )
	public void testRelationMismatches () throws IOException
	{
		// This comes from Maven/failsafe configuration
		String boltPort = System.getProperty ( "neo4j.server.boltPort", "7687" );
		try (
			Driver neoDriver = GraphDatabase.driver( "bolt://127.0.0.1:" + boltPort, AuthTokens.basic ( "neo4j", "test" ) );
		) 
		{
			Set<String> types = new HashSet<> ();
			Dataset dataSet = TDBFactory.createDataset ( "/var/folders/kb/8ld58jt15jl04b73rkwys9140000gn/T/neo2rdf_tdb_9203877680116318599" );
			Model model = dataSet.getDefaultModel ();
			var sparqlHelper = new ModelEndPointHelper ( model );
			dataSet.begin ( ReadWrite.READ );
			try {
				sparqlHelper.select ( IOUtils.readResource ( "rel_types.sparql" ) )
				.forEachRemaining ( qs -> {
					String from = qs.getResource ( "fromIri" ).getURI (),
						to = qs.getResource ( "toIri" ).getURI (),
						rel = qs.getResource ( "iri" ).getURI (),
						type = qs.getResource ( "type" ).getURI ();
					try ( Session session = neoDriver.session () )
					{
						var cyCursor = session.run ( 
								"MATCH p=(:Node{iri:$from}) - [r] - (:Node{iri:$to})\n"
							+ "WHERE r.iri = $rel\n"
							+ "RETURN p LIMIT 1",
							Values.parameters ( 
								"from",from,
								"to", to,
								"rel", rel
							)
						);
						if ( !cyCursor.hasNext () ) {
							String bk = "http://knetminer.org/data/rdf/terms/biokno/";
							out.format ( "(%s, %s, %s, %s)\n", 
								type.replace ( bk, "bk:" ), 
								from.replace ( bk, "bk:" ), 
								to.replace ( bk, "bk:" ), 
								rel.replace ( bk, "bk:" ) );
							types.add ( type );
						}
					}
				});
				
				out.println ( "\n\n\n ---- TYPES ----" );
				types.stream ()
				.sorted ()
				.forEach ( ev -> out.println ( ev ) );
			}
			finally {
				dataSet.end ();
			}
		}
	}
}
