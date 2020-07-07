package net.sourceforge.ondex.core.base;

import net.sourceforge.ondex.core.RelationKey;

/**
 * Key data structure for AbstractRelation.
 * 
 * @author taubertj, Matthew Pocock
 * 
 */
public class RelationKeyImpl extends AbstractONDEXEntity implements RelationKey {

	/**
	 * The "from" AbstractConcept id.
	 */
	private int fromId;

	/**
	 * The "to" AbstractConcept id.
	 */
	private int toId;

	/**
	 * The involved relation type id.
	 */
	private String rtId;

	/**
	 * pre-computed hash code
	 */
	private int hashCode;

	/**
	 * Constructor which fills all fields of this class.
	 * 
	 * @param sid
	 *            unique session id
	 * @param fromConceptID
	 *            from concept ID
	 * @param toConceptID
	 *            to concept ID
	 * @param ofType
	 *            relation type
	 */
	public RelationKeyImpl(long sid, int fromConceptID, int toConceptID,
			String ofType) {
		this.sid = sid;
		this.fromId = fromConceptID;
		this.toId = toConceptID;
		this.rtId = ofType.intern();
		this.hashCode = 31 * fromId + toId + rtId.hashCode();
	}

	/**
	 * Impose an ordering on the relations.
	 * 
	 * @param o
	 *            Object
	 * @return -1,0,1
	 * @throws NullPointerException
	 */
	@Override
	public int compareTo(RelationKey o) {
		if (this == o)
			return 0;
		return o.hashCode() - this.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof RelationKeyImpl) {
			RelationKeyImpl rk = (RelationKeyImpl) o;
			return this.fromId == rk.fromId && this.toId == rk.toId
					&& this.rtId.equals(rk.rtId);
		}
		return false;
	}

	/**
	 * Returns the from concept integer id.
	 * 
	 * @return Integer
	 */
	@Override
	public int getFromID() {
		return fromId;
	}

	/**
	 * Returns the name of the relation type.
	 * 
	 * @return String
	 */
	@Override
	public String getRtId() {
		return rtId;
	}

	/**
	 * Returns the to concept integer id.
	 * 
	 * @return Integer
	 */
	@Override
	public int getToID() {
		return toId;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
}
