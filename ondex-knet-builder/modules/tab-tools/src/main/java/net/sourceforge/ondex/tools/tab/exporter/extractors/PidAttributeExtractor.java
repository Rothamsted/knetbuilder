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
public class PidAttributeExtractor implements AttributeExtractor{

	public PidAttributeExtractor(){}

	public String getValue(ONDEXEntity cOrr) throws InvalidOndexEntityException {
		if(AbstractConcept.class.isAssignableFrom(cOrr.getClass())){
			return ((ONDEXConcept)cOrr).getPID();
		}
		if(AbstractRelation.class.isAssignableFrom(cOrr.getClass())){
			return ((ONDEXRelation)cOrr).getKey().toString();
		}
		throw new InvalidOndexEntityException(cOrr.getClass()+": is not an Ondex class for which a pid or key is known");
	}

	@Override
	public String getHeaderName() {
		return "ParserID";
	}
	
}
