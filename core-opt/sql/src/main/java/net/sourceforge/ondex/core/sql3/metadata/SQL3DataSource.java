package net.sourceforge.ondex.core.sql3.metadata;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.sql3.SQL3Graph;

public class SQL3DataSource extends SQL3MetaData implements DataSource {
	
	/**
	 * Duplicate of the ones in SQL2, except accepting SQL3Graph rather than SQL2Graph
	 * 
	 * @author sckuo
	 */
	

	public SQL3DataSource(SQL3Graph s, String id) {
		super(s, id, "cv");
	}

}
