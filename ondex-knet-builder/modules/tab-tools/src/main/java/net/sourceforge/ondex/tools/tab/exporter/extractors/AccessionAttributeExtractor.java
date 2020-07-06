package net.sourceforge.ondex.tools.tab.exporter.extractors;

import net.sourceforge.ondex.config.ValidatorRegistry;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;
import net.sourceforge.ondex.validator.htmlaccessionlink.Condition;

public class AccessionAttributeExtractor implements AttributeExtractor{

	private String cv;
	private boolean useLinks;

	/**
	 * 
	 * @param cv restrict cv to a given cv only
	 * @param useLinks
	 */
	public AccessionAttributeExtractor(String cv, boolean useLinks){
		this.cv = cv;
		this.useLinks = useLinks;
	}

	public String getValue(ONDEXEntity cOrr) {
		if(ONDEXConcept.class.isAssignableFrom(cOrr.getClass())){
			for(ConceptAccession ca : ((ONDEXConcept)cOrr).getConceptAccessions()){
				if(ca.getElementOf().getId().equals(cv)){

					if (useLinks) {
						AbstractONDEXValidator validator = ValidatorRegistry.validators.get("htmlaccessionlink");
						Condition condition = new Condition(ca.getElementOf().getId(),
								((ONDEXConcept)cOrr).getOfType().getId());

						String url = (String) validator.validate(condition);
						if (url != null) {
							return "=HYPERLINK(\""+url+ca.getAccession()
							+"\",\""+ca.getAccession()+"\")";
						}
					}

					return ca.getAccession();
				}
			}
			return "";
		}
		return null;
	}

	@Override
	public String getHeaderName() {
		return "Accession:"+cv;
	}
}
