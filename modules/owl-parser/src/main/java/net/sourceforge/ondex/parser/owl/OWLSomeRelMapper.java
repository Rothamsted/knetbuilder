package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;

import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.utils.CachedGraphWrapper;
import net.sourceforge.ondex.core.utils.ONDEXElemWrapper;
import net.sourceforge.ondex.parser.RelationsMapper;

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
public class OWLSomeRelMapper
  extends RdfPropertyMapper<Stream<ONDEXRelation>, ONDEXElemWrapper<ONDEXConcept>>
  implements RelationsMapper<OntClass, ONDEXElemWrapper<ONDEXConcept>>
{
	private OWLConceptClassMapper conceptClassMapper;
	private OWLConceptMapper conceptMapper;

	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Override
	public Stream<ONDEXRelation> map ( OntClass ontCls, ONDEXElemWrapper<ONDEXConcept> conceptw ) 
	{
		// Super classes and equivalents
		//
		Stream<OntClass> superClasses = JENAUTILS.toStream ( ontCls.listSuperClasses ( false ) ); 
		
		// And equivalents in intersections
		Stream<OntClass> eqMembers = JENAUTILS
		.toStream ( ontCls.listEquivalentClasses () )
		.filter ( eq -> eq.isIntersectionClass () )
		.map ( eq -> eq.asIntersectionClass () )
		.flatMap ( intrs -> JENAUTILS.toStream ( intrs.listOperands () ) );
		
		superClasses = Stream.concat ( superClasses, eqMembers );
		
		// And now filter the someValueFrom restrictions and process them
		//
		OWLConceptMapper conceptMapper = this.getConceptMapper ();
		if ( conceptMapper.getConceptClassMapper () == null ) 
			conceptMapper.setConceptClassMapper ( conceptClassMapper );

		ONDEXConcept concept = conceptw.getElement ();
		ONDEXGraph graph = conceptw.getGraph ();
		
		String propIri = this.getPropertyIri ();
		RelationType relType = this.getRelationType ( graph );
		EvidenceType evidence = this.getEvidenceType ( graph );

		return superClasses
		.filter ( sup -> sup.isRestriction () )
		.map ( sup -> sup.asRestriction () )
		.filter ( restr -> restr.isSomeValuesFromRestriction () )
		.map ( restr -> restr.asSomeValuesFromRestriction () )
		.filter ( someRestr -> propIri.equals ( someRestr.getOnProperty ().getURI () ) )
		.map ( someRestr -> someRestr.getSomeValuesFrom ().as ( OntClass.class ) )
		.map ( targetOntCls -> CachedGraphWrapper.getInstance ( graph ).getRelation ( 
			concept, conceptMapper.map ( targetOntCls, graph ), relType, evidence 
		));
	}
	
	/**
	 * @return part_of, which is attached to all of the links found by {@link #map(OntClass, ONDEXGraph)}.
	 * TODO: make it configurable.
	 */
	public RelationType getRelationType ( ONDEXGraph graph )
	{
		return CachedGraphWrapper.getInstance ( graph ).getRelationType ( 
			"part_of", 
			true, // isAntisymmetric, 
			true, // isReflexive, 
			false, // isSymmetric, 
			true // isTransitive 
		);
	}

	/**
	 * TODO: make it configurable, @return IMPD for the time being.
	 */
	public EvidenceType getEvidenceType ( ONDEXGraph graph )
	{
		return CachedGraphWrapper.getInstance ( graph ).getEvidenceType ( "IMPD", "IMPD", "" );
	}

	/**
	 * Used to map the root class and know the top level in the source ontology to start from.
	 */
	public OWLConceptClassMapper getConceptClassMapper ()
	{
		return this.conceptClassMapper;
	}

	public void setConceptClassMapper ( OWLConceptClassMapper conceptClassMapper )
	{
		this.conceptClassMapper = conceptClassMapper;
	}

	/**
	 * Every new owl:Class that is met by {@link #map(OntClass, ONDEXGraph)} is mapped to an {@link ONDEXConcept}
	 * by means of this mapper.
	 */
	public OWLConceptMapper getConceptMapper ()
	{
		return conceptMapper;
	}

	public void setConceptMapper ( OWLConceptMapper conceptMapper )
	{
		this.conceptMapper = conceptMapper;
	}
}
