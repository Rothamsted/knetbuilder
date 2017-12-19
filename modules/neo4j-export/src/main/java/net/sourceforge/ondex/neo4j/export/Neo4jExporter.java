package net.sourceforge.ondex.neo4j.export;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.rdf.export.RDFExporter;
import uk.ac.rothamsted.rdf.neo4j.load.CypherLoader;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>15 Dec 2017</dd></dl>
 *
 */
public class Neo4jExporter extends CypherLoader<ONDEXGraph>
{
	public Neo4jExporter ()
	{
		super ();
		this.setRdfProcessor ( new RDFExporter () );
	}

	public void export ( ONDEXGraph graph ) {
		super.process ( graph );
	}	
}
