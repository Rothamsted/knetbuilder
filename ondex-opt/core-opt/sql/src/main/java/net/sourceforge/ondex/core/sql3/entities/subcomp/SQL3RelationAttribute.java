package net.sourceforge.ondex.core.sql3.entities.subcomp;

import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.sql3.SQL3Graph;
import net.sourceforge.ondex.core.sql3.entities.SQL3Relation;

public class SQL3RelationAttribute extends SQL3Attribute {

	public SQL3RelationAttribute(SQL3Graph s, int i) {
		super(s, i);
	}

	@Override
	public Class<? extends ONDEXEntity> getOwnerClass() {
		return SQL3Relation.class;
	}
}