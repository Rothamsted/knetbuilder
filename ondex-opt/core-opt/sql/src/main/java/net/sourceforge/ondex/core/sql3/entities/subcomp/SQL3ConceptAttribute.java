package net.sourceforge.ondex.core.sql3.entities.subcomp;

import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.sql3.SQL3Graph;
import net.sourceforge.ondex.core.sql3.entities.SQL3Concept;

public class SQL3ConceptAttribute extends SQL3Attribute {

	public SQL3ConceptAttribute(SQL3Graph s, int i) {
		super(s, i);
	}

	@Override
	public Class<? extends ONDEXEntity> getOwnerClass() {
		return SQL3Concept.class;
	}
}