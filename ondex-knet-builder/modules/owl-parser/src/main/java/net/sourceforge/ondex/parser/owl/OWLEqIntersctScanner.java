package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;

import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;

import net.sourceforge.ondex.parser.Scanner;

/**
 * Scans OWL axioms like: A equivalent ( B and (part-of ( some C ) ) for the current owl class / ONDEX concept A.
 * This makes the ONDEX relation A is-a B, {@link OWLSomeScanner} can be used to also make A part-of C.
 * 
 * This is typically used with {@link OWLMapper}, as a {@link Scanner} for one of the {@link OWLMapper#getLinkers()}.
 *  
 * @author brandizi
 * <dl><dt>Date:</dt><dd>10 May 2017</dd></dl>
 *
 */
public class OWLEqIntersctScanner implements Scanner<OntClass, OntClass>
{
	@Override
	public Stream<OntClass> scan ( OntClass fromCls )
	{
		// Take all equivalents's members that are intersections of other classses
		Stream<OntClass> eqMembers = JENAUTILS
		.toStream ( fromCls.listEquivalentClasses () )
		.filter ( eq -> eq.isIntersectionClass () )
		.map ( eq -> eq.asIntersectionClass () )
		.flatMap ( intrs -> JENAUTILS.toStream ( intrs.listOperands () ) );
		
		// Now filter those that are straight classes, the rest is typically made of restrictions and is 
		// left to other mappers
		return eqMembers
		.filter ( eq -> !eq.isAnon () )
		.filter ( eq -> eq.getURI () != null );
	}
}
