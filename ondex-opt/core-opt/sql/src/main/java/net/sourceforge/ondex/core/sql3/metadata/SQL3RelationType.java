package net.sourceforge.ondex.core.sql3.metadata;

import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.sql3.SQL3Graph;
import net.sourceforge.ondex.core.sql3.helper.MetaDataHelper;

public class SQL3RelationType extends SQL3MetaData implements RelationType {
	
	/**
	 * Duplicate of the ones in SQL2, except accepting SQL3Graph rather than SQL2Graph
	 * 
	 * @author sckuo
	 */
	

	public SQL3RelationType(SQL3Graph s, String id) {
		super(s, id, "relationtype");
	}
	
	@Override
	public void setSpecialisationOf(RelationType specialisationOf) {
		
		MetaDataHelper.setString(sg, tableName, "specOf", mid, specialisationOf.getId());
		
	}
	
	@Override
	public RelationType getSpecialisationOf() {
		
		String rtName = MetaDataHelper.fetchString(sg, tableName, "specOf", mid);
		
		if (rtName == null || rtName.isEmpty()) {
			return null;
		}		
		return sg.getMetaData().getRelationType(rtName);
		
	}
	
	@Override
	public boolean isAssignableFrom(RelationType rt) {
    	RelationType curr = rt;
    	while (!curr.equals(this)) {
    		curr = curr.getSpecialisationOf();
    		if (curr == null) {
    			return false;
    		}
    	}
    	return true;
	}

	@Override
	public boolean isAssignableTo(RelationType rt) {
		return rt.isAssignableFrom(this);
	}
	
	@Override
	public boolean isAntisymmetric() {
		return MetaDataHelper.fetchBoolean(sg, tableName, "antisym", mid);
	}

	@Override
	public boolean isReflexive() {
		return MetaDataHelper.fetchBoolean(sg, tableName, "refl", mid);
	}

	@Override
	public boolean isSymmetric() {
		return MetaDataHelper.fetchBoolean(sg, tableName, "sym", mid);
	}

	@Override
	public boolean isTransitiv() {
		return MetaDataHelper.fetchBoolean(sg, tableName, "trans", mid);
	}
	
	@Override
	public String getInverseName() {
		return MetaDataHelper.fetchString(sg, tableName, "inverse", mid);
	}
	
	@Override
	public void setAntisymmetric(boolean isAntisymmetric) {
		
		MetaDataHelper.setBoolean(sg, tableName, "antisym", mid, isAntisymmetric);
		
	}

	@Override
	public void setInverseName(String inverseName) {
		
		MetaDataHelper.setString(sg, tableName, "inverse", mid, inverseName);
		
	}

	@Override
	public void setReflexive(boolean isReflexive) {
		
		MetaDataHelper.setBoolean(sg, tableName, "refl", mid, isReflexive);
		
	}

	@Override
	public void setSymmetric(boolean isSymmetric) {
		MetaDataHelper.setBoolean(sg, tableName, "sym", mid, isSymmetric);		
	}

	@Override
	public void setTransitiv(boolean isTransitiv) {
		MetaDataHelper.setBoolean(sg, tableName, "trans", mid, isTransitiv);	
		
	}
	
	
}
