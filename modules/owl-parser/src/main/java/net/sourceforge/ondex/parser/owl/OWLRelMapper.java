package net.sourceforge.ondex.parser.owl;

import org.apache.jena.ontology.OntClass;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.utils.EvidenceTypePrototype;
import net.sourceforge.ondex.core.utils.RelationTypePrototype;
import net.sourceforge.ondex.parser.RelationsMapper;

/**
 * Generic class that links the mapping of an OWL/RDF relation to two ONDEX concepts, instances of an ONDEX concept 
 * class and mapped to an ONDEX relation type. Additional attributes might be set, such as evidence type.  
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>8 May 2017</dd></dl>
 *
 */
public abstract class OWLRelMapper<S, OT> implements RelationsMapper<S, OT>
{
	private OWLConceptClassMapper conceptClassMapper;
	private OWLConceptMapper conceptMapper;
	private RelationTypePrototype relationTypePrototype;
	private EvidenceTypePrototype evidenceTypePrototype;

	public OWLRelMapper ()
	{
		super ();
	}

	public RelationTypePrototype getRelationTypePrototype ()
	{
		return relationTypePrototype;
	}

	public void setRelationTypePrototype ( RelationTypePrototype relationTypePrototype )
	{
		this.relationTypePrototype = relationTypePrototype;
	}

	public EvidenceTypePrototype getEvidenceTypePrototype ()
	{
		return evidenceTypePrototype;
	}

	public void setEvidenceTypePrototype ( EvidenceTypePrototype evidenceTypePrototype )
	{
		this.evidenceTypePrototype = evidenceTypePrototype;
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