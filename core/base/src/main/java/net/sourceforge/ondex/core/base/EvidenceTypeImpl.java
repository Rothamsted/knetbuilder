package net.sourceforge.ondex.core.base;

import net.sourceforge.ondex.core.EvidenceType;

/**
 * Convenience implementation of EvidenceType.
 * 
 * @author Matthew Pocock
 */
public class EvidenceTypeImpl extends AbstractMetaData implements EvidenceType {

	/**
	 * Constructor which sets the id and the description of this EvidenceType.
	 * 
	 * @param sid
	 *            unique id
	 * @param id
	 *            id of this EvidenceType
	 * @param fullname
	 *            fullname of this EvidenceType
	 * @param description
	 *            description of this EvidenceType
	 */
	protected EvidenceTypeImpl(long sid, String id, String fullname,
			String description) {
		super(sid, id, fullname, description);
	}
}
