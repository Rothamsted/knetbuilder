package net.sourceforge.ondex.tools.tab.exporter.extractors;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.tools.tab.exporter.InvalidOndexEntityException;
import net.sourceforge.ondex.tools.tab.exporter.Label;

import java.util.Set;

public class ContextExtractor implements AttributeExtractor {

	private String cc;
	private Label type;

	public ContextExtractor(String cc, Label type){
		this.cc = cc;
		this.type = type;
	}

	public String getValue(ONDEXEntity cOrr) throws InvalidOndexEntityException {
		Set<ONDEXConcept> contexts = cOrr.getTags();
		return processContexts(contexts);
	}

	private StringBuilder builder = new StringBuilder();

	private String processContexts(Set<ONDEXConcept> contexts) {

		if (contexts.size() == 0) {
			return "";
		}
		for (ONDEXConcept abstractConcept : contexts) {
			if (cc != null && !abstractConcept.getOfType().getId().equals(cc)) {
				continue;
			}

			switch(type) {
			case NAME: 
				ConceptName name = abstractConcept.getConceptName();
				if (name != null) {
					builder.append(name.getName());
				}
				break;
			case ACCESSION:
				for (ConceptAccession conceptAccession : abstractConcept.getConceptAccessions()) {
                    builder.append(conceptAccession.getAccession()).append("(").append(conceptAccession.getElementOf().getId()).append(")");
					builder.append(',');
				}
				builder.setLength(builder.length()-1);
				break;
			case PID:
				builder.append(abstractConcept.getPID());
				break;
			case ID:
				builder.append(abstractConcept.getId());
				break;
			default: break;
			}
			builder.append(';');
		}
		String result = builder.toString().substring(0, builder.length());//cut of last semi colon
		builder.setLength(0);
		return result;
	}

	@Override
	public String getHeaderName() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
