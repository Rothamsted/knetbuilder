package net.sourceforge.ondex.tools.tab.exporter.extractors;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.tools.tab.exporter.InvalidOndexEntityException;

/**
 * @author lysenkoa
 */
public class EvidenceAttributeExtractor implements AttributeExtractor {

    public EvidenceAttributeExtractor() {
    }

    public String getValue(ONDEXEntity cOrr) throws InvalidOndexEntityException {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (EvidenceType evidence : cOrr.getEvidence()) {
            if (!first) sb.append(", ");
            sb.append(evidence.getId());
            first = false;
        }
        return sb.toString();
    }

    @Override
    public String getHeaderName() {
        return "Evidence";
    }

}
