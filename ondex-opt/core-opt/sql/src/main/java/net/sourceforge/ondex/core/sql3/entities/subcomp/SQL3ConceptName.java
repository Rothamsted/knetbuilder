package net.sourceforge.ondex.core.sql3.entities.subcomp;

import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.sql3.SQL3Graph;
import net.sourceforge.ondex.core.sql3.helper.SubCompHelper;
import net.sourceforge.ondex.exception.type.AccessDeniedException;

public class SQL3ConceptName implements ConceptName {

	/**
	 * Duplicate of the ones in SQL2, except accepting SQL3Graph & stuff rather than SQL2Graph
	 * 
	 * @author sckuo
	 */

	private SQL3Graph sg;
	private int nameno;
	static final String tableName = "conceptname";
	
	public SQL3ConceptName (SQL3Graph s, int i) {
		
		sg = s;
		nameno = i;
	}
	
	@Override
	public int getOwnerId() {
		return SubCompHelper.fetchInteger(sg, tableName, "id", nameno);
	}
	
	@Override
	public long getSID() {
		return sg.getSID();
	}

	@Override
	public String getName() {
		return SubCompHelper.fetchString(sg, tableName, "name", nameno);
	}

	@Override
	public boolean isPreferred() {
		return SubCompHelper.fetchBoolean(sg, tableName, "pref", nameno);
	}

	@Override
	public void setPreferred(boolean isPreferred) throws AccessDeniedException {
	
		SubCompHelper.setBoolean(sg, tableName, "pref", nameno, isPreferred);
		
	}

}
