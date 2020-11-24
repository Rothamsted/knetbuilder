package net.sourceforge.ondex.core.util;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * Utilities about {@link ONDEXGraph} operations like filtering or removals.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Nov 2020</dd></dl>
 *
 */
public class ONDEXGraphOperations
{
	private static Logger log = LoggerFactory.getLogger ( ONDEXGraphOperations.class );
	
	private ONDEXGraphOperations ()
	{
	}

	public static void removeConcepts ( ONDEXGraph graph, Predicate<ONDEXConcept> filter )
	{
		var toRemove = 
		graph.getConcepts ()
		.parallelStream ()
		.filter ( filter )
		.collect ( Collectors.toList () );

		toRemove.stream ()
		.forEach ( c -> graph.deleteConcept ( c.getId () ) );
	}
	
	public static void removeEmptyConcepts ( ONDEXGraph graph )
	{
		removeConcepts ( graph, c ->
			StringUtils.trimToNull ( c.getPID () ) == null
			&& c.getConceptAccessions ().isEmpty () 
			&& c.getConceptNames ().isEmpty ()
		);
	}
	
	/**
	 * Dump all the concepts/relations in the logger.
	 * WARNING: a graph might be HUGE, this is intended for small test cases only.
	 * 
	 * TODO: details to be added.
	 * 
	 */
	public static void dumpAll ( ONDEXGraph graph )
	{
		var concepts = graph.getConcepts ();
		var rels = graph.getRelations ();
		
		log.info ( "Concepts: " + concepts.size () );
		log.info ( "Relations: " + rels.size () );
		
		Function<ONDEXConcept, String> accsDumper = c -> 
		{
			var accs = c.getConceptAccessions ();
			if ( accs.isEmpty () ) return "[]";
			
			return accs.stream ()
			.map ( acc -> acc.getAccession () + ":" + acc.getElementOf ().getId () )
			.collect ( Collectors.joining ( ",", "[", "]" ) );
		};
		
		concepts.forEach ( c -> 
		{
		  log.info ( String.format ( 
		  	"Concept: '%d', '%s', %s",		  	
		  	c.getId (),
		  	c.getPID (),
		  	accsDumper.apply ( c )  
		  ));
		});
		
		graph.getRelations ().forEach ( r -> 
		{
			log.info ( String.format ( 
				"Relation: '%s', from: '%s', to: '%s'", 
				r.getOfType ().getId (),
				r.getFromConcept ().getPID (),
				r.getToConcept ().getPID ()
			));
			r.getAttributes ().forEach ( a -> log.info ( String.format ( 
				"\t attribute: '%s', '%s'", a.getOfType ().getFullname (), a.getValue ()  
			)));
		});		
	}

}
