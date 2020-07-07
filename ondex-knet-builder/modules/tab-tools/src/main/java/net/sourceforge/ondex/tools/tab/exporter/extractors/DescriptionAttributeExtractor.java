package net.sourceforge.ondex.tools.tab.exporter.extractors;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.base.AbstractConcept;
import net.sourceforge.ondex.tools.tab.exporter.InvalidOndexEntityException;

/**
 * 
 * @author lysenkoa
 * 
 */
public class DescriptionAttributeExtractor implements AttributeExtractor{

	public DescriptionAttributeExtractor(){}

	public String getValue(ONDEXEntity cOrr) throws InvalidOndexEntityException {
		if(AbstractConcept.class.isAssignableFrom(cOrr.getClass())){
			return ((ONDEXConcept)cOrr).getDescription();
		}
		throw new InvalidOndexEntityException(cOrr.getClass()+": is not an Ondex class for which a description is known");
	}

	@Override
	public String getHeaderName() {
		return "Description";
	}

}
