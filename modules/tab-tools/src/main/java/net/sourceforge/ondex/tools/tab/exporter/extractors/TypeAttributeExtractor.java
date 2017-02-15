package net.sourceforge.ondex.tools.tab.exporter.extractors;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.base.AbstractConcept;
import net.sourceforge.ondex.core.base.AbstractRelation;
import net.sourceforge.ondex.tools.tab.exporter.InvalidOndexEntityException;

/**
 * 
 * @author lysenkoa
 * 
 */
public class TypeAttributeExtractor implements AttributeExtractor {

	public TypeAttributeExtractor(){}

	public String getValue(ONDEXEntity cOrr) throws InvalidOndexEntityException {
		if(AbstractConcept.class.isAssignableFrom(cOrr.getClass())){
			return ((ONDEXConcept)cOrr).getOfType().getId();
		}
		if(AbstractRelation.class.isAssignableFrom(cOrr.getClass())){
			return ((ONDEXRelation)cOrr).getOfType().getId();
		}
		throw new InvalidOndexEntityException(cOrr.getClass()+": is not an Ondex class for which type is known");
	}

	@Override
	public String getHeaderName() {
		return "Type";
	}
	
}
