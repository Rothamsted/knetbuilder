package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;

import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;

/**
 * 
 * TODO: comment me!
 *
 * TODO: add the class mentioned in equivalent.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 May 2017</dd></dl>
 *
 */
public class OWLSomeRelMapper extends OWLSimpleConceptRelMapper
{
	private String propertyIri;

	@Override
	protected Stream<OntClass> getRelatedClasses ( OntClass fromCls )
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
		.map ( someRestr -> someRestr.getSomeValuesFrom ().as ( OntClass.class ) );
	}
	

	/**
	 * The property that this mapper deals with. Examples are rdfs:label, rdfs:comment, skos:label.
	 */
	public String getPropertyIri ()
	{
		return propertyIri;
	}

	public void setPropertyIri ( String propertyIri )
	{
		this.propertyIri = propertyIri;
	}
	
}
