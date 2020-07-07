package net.sourceforge.ondex.core.sql3.metadata;

import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.core.sql3.SQL3Graph;

public class SQL3Unit extends SQL3MetaData implements Unit {
	
	/**
	 * Duplicate of the ones in SQL2, except accepting SQL3Graph rather than SQL2Graph
	 * 
	 * @author sckuo
	 */
	

	public SQL3Unit(SQL3Graph s, String id) {
		super(s, id, "unit");
	}

}
