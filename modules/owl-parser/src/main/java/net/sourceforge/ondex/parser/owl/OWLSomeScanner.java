package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;

import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;

import net.sourceforge.ondex.parser.Scanner;

/**
 * Scans OWL axioms like A subclassOf ( part-of ( some C ) ) or A equivalent ( B and (part-of ( some C ) )
 * and returns classes like C. The scanning of of the equivalent B is left to {@link OWLEqIntersctScanner}.
 * 
 * The OWL relation to follow (part-of in this example) is configurable via {@link #getPropertyIri()}.
 * 
 * This is typically used with {@link OWLMapper}, as a {@link Scanner} for one of the {@link OWLMapper#getLinkers()}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 May 2017</dd></dl>
 *
 */
public class OWLSomeScanner extends DefaultRdfPropertyConfigurator implements Scanner<OntClass, OntClass> 
{
	public OWLSomeScanner () {
		super ();
	}

	public OWLSomeScanner ( String propertyIri ) {
		super ( propertyIri );
	}

	@Override
	public Stream<OntClass> scan ( OntClass fromCls )
	{
		Stream<OntClass> superClasses = JENAUTILS.toStream ( fromCls.listSuperClasses ( false ) ); 
		
		// Add up members of intersections
		Stream<OntClass> eqMembers = JENAUTILS
		.toStream ( fromCls.listEquivalentClasses () )
		.filter ( eq -> eq.isIntersectionClass () )
		.map ( eq -> eq.asIntersectionClass () )
		.flatMap ( intrs -> JENAUTILS.toStream ( intrs.listOperands () ) );
		
		superClasses = Stream.concat ( superClasses, eqMembers );
		
		// And now filter the someValueFrom restrictions 
		//
		String propIri = this.getPropertyIri ();

		return superClasses
		.filter ( sup -> sup.isRestriction () )
		.map ( sup -> sup.asRestriction () )
		.filter ( restr -> restr.isSomeValuesFromRestriction () )
		.map ( restr -> restr.asSomeValuesFromRestriction () )
		.filter ( someRestr -> propIri.equals ( someRestr.getOnProperty ().getURI () ) )
		.map ( someRestr -> someRestr.getSomeValuesFrom ().as ( OntClass.class ) )
		.filter ( cls -> !cls.isAnon () )
		.filter ( cls -> cls.getURI () != null );
	}
		
}
