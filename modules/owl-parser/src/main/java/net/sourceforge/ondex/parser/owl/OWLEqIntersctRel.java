package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;

import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;

import net.sourceforge.ondex.core.utils.RelationTypePrototype;

/**
 * TODO: comment me!
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
		// Take all equivalents's members
		Stream<OntClass> eqMembers = JENAUTILS
		.toStream ( fromCls.listEquivalentClasses () )
		.filter ( eq -> eq.isIntersectionClass () )
		.map ( eq -> eq.asIntersectionClass () )
		.flatMap ( intrs -> JENAUTILS.toStream ( intrs.listOperands () ) );
		
		// And filter those that are straight classes
		return eqMembers
		.filter ( eq -> !eq.isAnon () )
		.filter ( eq -> eq.getURI () != null );
	}

}
