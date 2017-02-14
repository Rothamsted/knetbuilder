package net.sourceforge.ondex.core.sql3.entities.subcomp;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.sql3.SQL3Graph;
import net.sourceforge.ondex.core.sql3.helper.SubCompHelper;
import net.sourceforge.ondex.core.sql3.metadata.SQL3DataSource;
import net.sourceforge.ondex.exception.type.AccessDeniedException;

public class SQL3ConceptAccession implements ConceptAccession {
	
	/**
	 * Duplicate of the ones in SQL2, except accepting SQL3Graph & stuff rather than SQL2Graph
	 * 
	 * @author sckuo
	 */
	
	private static final String tableName = "conceptaccession";
	
	private SQL3Graph sg;
	private int accno;
		
	public SQL3ConceptAccession(SQL3Graph s, int i) {
	
		sg = s;
		accno = i;
		
	}	
	
	public int hashCode() {
	
		return 0;
		
	}
	
	public boolean equals(Object o) {
		
		if (o instanceof SQL3ConceptAccession) {
			
			SQL3ConceptAccession p = (SQL3ConceptAccession) o;
			String tacc = p.getAccession();
			DataSource tdataSource = p.getElementOf();
			long tsid = p.getSID();
			int toid = p.getOwnerId();
			
			if (tacc.equals(getAccession()) && tdataSource.equals(getElementOf()) && tsid == getSID() && toid == getOwnerId()) {
				return true;
			}
			
		}
		
		return false;
		
		
	}
	
	@Override
	public int getOwnerId() {
		return SubCompHelper.fetchInteger(sg, tableName, "id", accno);
	}
	
	@Override
	public String getAccession() {
		
		return SubCompHelper.fetchString(sg, tableName, "accession", accno);
		
	}
	
	@Override
	public boolean isAmbiguous() {
		return SubCompHelper.fetchBoolean(sg, tableName, "ambi", accno);
	}	
	
	@Override
	public long getSID() {
		return sg.getSID();
	}
	
	@Override
	public void setAmbiguous(boolean ambiguous) throws AccessDeniedException {
		
		SubCompHelper.setBoolean(sg, tableName, "ambi", accno, ambiguous);
		
	}
	
	@Override
	public DataSource getElementOf() {
		String cvid = SubCompHelper.fetchString(sg, tableName, "cv", accno);
		SQL3DataSource c = new SQL3DataSource(sg, cvid);
		return c;
		
	}
	
}
