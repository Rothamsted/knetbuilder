package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;

import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;

import net.sourceforge.ondex.core.utils.RelationTypePrototype;

/**
 * Maps OWL axioms like: A equivalent ( B and (part-of ( some C ) ) for the current owl class / ONDEX concept A.
 * This makes the ONDEX relation A is-a B, {@link OWLSomeRelMapper} can be used to also make A part-of C. 
 *
 * The relation (part-of in this example) is configurable via {@link #getPropertyIri()}, which is mapped 
 * to {@link #getRelationTypePrototype()}.
 *  
 * @author brandizi
 * <dl><dt>Date:</dt><dd>10 May 2017</dd></dl>
 *
 */
public class OWLEqIntersctRel extends OWLSimpleConceptRelMapper
{
	
	public OWLEqIntersctRel ()
	{
		super ();
		this.setRelationTypePrototype ( RelationTypePrototype.IS_A_PROTOTYPE );
	}

	@Override
	protected Stream<OntClass> getRelatedClasses ( OntClass fromCls )
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
		
		// That's it! The parent's code will do the rest
	}

}
