package net.sourceforge.ondex.core;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Key data structure for ONDEXRelation.
 * 
 * @author taubertj
 * 
 */
@XmlJavaTypeAdapter(net.sourceforge.ondex.webservice.Adapters.AnyTypeAdapter.class)
public interface RelationKey extends ONDEXAssociable, Comparable<RelationKey> {

	/**
	 * Returns the from concept integer id.
	 * 
	 * @return Integer
	 */
	public int getFromID();

	/**
	 * Returns the name of the relation type.
	 * 
	 * @return String
	 */
	public String getRtId();

	/**
	 * Returns the to concept integer id.
	 * 
	 * @return Integer
	 */
	public int getToID();

}
