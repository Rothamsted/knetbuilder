package net.sourceforge.ondex.core.sql3.metadata;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.sql3.SQL3Graph;
import net.sourceforge.ondex.core.sql3.helper.MetaDataHelper;

public class SQL3ConceptClass extends SQL3MetaData implements ConceptClass {
	
	/**
	 * Duplicate of the ones in SQL2, except accepting SQL3Graph rather than SQL2Graph
	 * 
	 * @author sckuo
	 */
	

	public SQL3ConceptClass(SQL3Graph s, String id) {
		super(s, id, "conceptclass");
	}

	@Override
	public ConceptClass getSpecialisationOf() {
		
		String ccName = MetaDataHelper.fetchString(sg, tableName, "specOf", mid);
		
		if (ccName == null || ccName.isEmpty()) {
			return null;
		}
		return sg.getMetaData().getConceptClass(ccName);
		
	}



	@Override
	public void setSpecialisationOf(ConceptClass specialisationOf) {
		
		if (specialisationOf == null) {
			
		} else {
			MetaDataHelper.setString(sg, tableName, "specOf", mid, specialisationOf.getId());
		}
		
	}
		
	@Override
	public boolean isAssignableFrom(ConceptClass cc) {
    	ConceptClass curr = cc;
    	while (!curr.equals(this)) {
    		curr = curr.getSpecialisationOf();
    		if (curr == null) {
    			return false;
    		}
    	}
    	return true;
	}

	@Override
	public boolean isAssignableTo(ConceptClass cc) {
    	return cc.isAssignableFrom(this);
	}

}
