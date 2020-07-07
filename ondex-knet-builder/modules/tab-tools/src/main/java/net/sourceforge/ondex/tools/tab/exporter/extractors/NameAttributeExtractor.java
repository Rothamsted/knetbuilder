package net.sourceforge.ondex.tools.tab.exporter.extractors;

import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.base.AbstractConcept;
import net.sourceforge.ondex.tools.tab.exporter.InvalidOndexEntityException;

/**
 * 
 * @author lysenkoa
 * 
 */
public class NameAttributeExtractor implements AttributeExtractor{
	public NameAttributeExtractor(){}

	public String getValue(ONDEXEntity cOrr) throws InvalidOndexEntityException {
		if(AbstractConcept.class.isAssignableFrom(cOrr.getClass())){

			String preferedName = "";
			for(ConceptName cn : ((ONDEXConcept)cOrr).getConceptNames()){
				if(cn.isPreferred()){
					preferedName = cn.getName();	
				}
			}

			if (preferedName.length() ==0) {
				preferedName = ((ONDEXConcept)cOrr).getPID();
			}
			/**
			if (useLinks && preferedName.length() > 0 && ((AbstractConcept)cOrr).getElementOf().getId().endsWith("PlnTFDB")) {
				AbstractONDEXValidator validator = ONDEXRegistry.validators.get("htmlaccessionlink");
				Condition condition = new Condition(((AbstractConcept)cOrr).getElementOf().getId(),
						((AbstractConcept)cOrr).getOfType().getId());

				String url = (String) validator.validate(condition);
				if (url != null) {
					return "=HYPERLINK(\""+url+preferedName
					+"\",\""+preferedName+"\")";
				}
			}
			 */
			return preferedName;
		}
		throw new InvalidOndexEntityException(cOrr.getClass()+": is not an Ondex class for which a name is known");

	}

	@Override
	public String getHeaderName() {
		return "PrefferedName";
	}
}
