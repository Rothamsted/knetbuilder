package net.sourceforge.ondex.tools.tab.exporter.extractors;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.tools.tab.exporter.InvalidOndexEntityException;

/**
 * 
 * @author lysenkoa
 * 
 */
public class DefinedEvidenceAttributeExtractor implements AttributeExtractor{

	private EvidenceType ofEv;

	public DefinedEvidenceAttributeExtractor(EvidenceType ev) throws NullValueException, EmptyStringException{
		ofEv = ev;
	}

	public String getValue(ONDEXEntity cOrr) throws InvalidOndexEntityException {
		for (EvidenceType et : cOrr.getEvidence()){
			if(et.equals(ofEv))return ofEv.getId();
		}
		return "";
	}

	@Override
	public String getHeaderName() {
		return ofEv.getId()+"?";
	}
}
