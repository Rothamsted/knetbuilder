package net.sourceforge.ondex.core.sql3.metadata;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.sql3.SQL3Graph;

public class SQL3EvidenceType extends SQL3MetaData implements EvidenceType {
	
	/**
	 * Duplicate of the ones in SQL2, except accepting SQL3Graph rather than SQL2Graph
	 * 
	 * @author sckuo
	 */
	

	public SQL3EvidenceType(SQL3Graph s, String id) {
		super(s, id, "evidencetype");
	}

}
