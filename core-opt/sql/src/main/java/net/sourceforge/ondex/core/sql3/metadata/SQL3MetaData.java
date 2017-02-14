package net.sourceforge.ondex.core.sql3.metadata;

import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.sql3.SQL3Graph;
import net.sourceforge.ondex.core.sql3.helper.MetaDataHelper;

public class SQL3MetaData implements MetaData {

	/**
	 * Duplicate of the ones in SQL2, except accepting SQL3Graph rather than SQL2Graph
	 * 
	 * @author sckuo
	 */
	
	
	protected SQL3Graph sg;
	protected String mid;
	protected String tableName;

	public SQL3MetaData (SQL3Graph s, String id, String tn) {
		
		sg = s;
		mid = id;
		tableName = tn;
		
	}
	
	
	@Override
	public String getId() {

		return mid;
		
	}
	
	@Override
	public long getSID() {
		return sg.getSID();
	}
	
	@Override
	public int compareTo(MetaData o) {
		
		return mid.compareTo(o.getId());
		
	}
	

	@Override
	public String getDescription() {
		
		return MetaDataHelper.fetchString(sg, tableName, "description", mid);
		
		
	}

	@Override
	public String getFullname() {
		return MetaDataHelper.fetchString(sg, tableName, "fullname", mid);
	}



	@Override
	public void setDescription(String v) {
		
		MetaDataHelper.setString(sg, tableName, "description", mid, v);
		
	}

	@Override
	public void setFullname(String v) {
	
		MetaDataHelper.setString(sg, tableName, "fullname", mid, v);
		
	}
	
	public String getTableName() {
	
		return tableName;
		
	}
	
	public boolean equals(Object o) {
		
		if (o instanceof SQL3MetaData) {
			
			SQL3MetaData p = (SQL3MetaData) o;
			String tmid = p.getId();
			long tsid = p.getSID();
			String ttn = p.getTableName();
			
			if (tmid.equals(getId()) && tsid == getSID() && ttn.equals(tableName)) {
				
				return true;
				
			}
			
		}
	
		return false;
	}
	
	public int hashCode() {
		return 0;	
	}

}
