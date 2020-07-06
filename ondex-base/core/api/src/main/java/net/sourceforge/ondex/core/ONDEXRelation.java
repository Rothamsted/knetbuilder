package net.sourceforge.ondex.core;

/**
 * This class defines a Relation between two concepts.
 * 
 * @author sierenk, taubertj
 */
public interface ONDEXRelation extends ONDEXEntity, ONDEXAssociable,
		Instantiation<RelationType> {

	/**
	 * Returns the from Concept of this instance of Relation.
	 * 
	 * @return ONDEXConcept
	 */
	public ONDEXConcept getFromConcept();

	/**
	 * Returns the unique Key of this Relation.
	 * 
	 * @return RelationKey
	 */
	public RelationKey getKey();

	/**
	 * Returns the to Concept of this instance of Relation.
	 * 
	 * @return ONDEXConcept
	 */
	public ONDEXConcept getToConcept();

}
