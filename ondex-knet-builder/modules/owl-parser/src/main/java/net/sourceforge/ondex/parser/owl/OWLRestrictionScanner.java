package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;

import java.util.Objects;
import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.rdf.model.Resource;

import net.sourceforge.ondex.parser.Scanner;

/**
 * Scans some-values-from or all-values-from axioms. It scans patterns like:  
 * 
 * {@code A subclassOf ( part-of ( some C ) ) or A equivalent ( B and (part-of ( some C ) )}
 * 
 * and returns classes like C. The scanning of the {@code equivalent B} is left to {@link OWLEqIntersctScanner}.
 * 
 * The OWL relation to follow (part-of in this example) is configurable via {@link #getPropertyIri()}.
 * 
 * This is typically used with {@link OWLMapper}, as a {@link Scanner} for one of the {@link OWLMapper#getLinkers()}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 May 2017</dd></dl>
 *
 */
public abstract class OWLRestrictionScanner<R extends Restriction> extends DefaultRdfPropertyConfigurator implements Scanner<OntClass, OntClass> 
{
	/**
	 * If this class is a restriction of type R, it returns it, else returns null. 
	 */
	protected abstract R asRestriction ( Restriction ontRestriction );
	
	/**
	 * Returns the target of the restriction, eg, for someValuesFrom A, returns A
	 * The code checks that this is a non anonymous {@link OntClass}.
	 *  
	 */
	protected abstract Resource getRestrictionClass ( R ontRestriction );
	
	public OWLRestrictionScanner () {
		super ();
	}

	public OWLRestrictionScanner ( String propertyIri ) {
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
		.map ( this::asRestriction )
		.filter ( Objects::nonNull )
		.filter ( restr -> propIri.equals ( restr.getOnProperty ().getURI () ) )
		.map ( restr -> getRestrictionClass ( restr ).as ( OntClass.class ) )
		.filter ( cls -> !cls.isAnon () )
		.filter ( cls -> cls.getURI () != null );
	}
		
}
