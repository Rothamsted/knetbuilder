package net.sourceforge.ondex.tools.tab.exporter.extractors;

import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.tools.tab.exporter.InvalidOndexEntityException;

/**
 * @author hindlem
 *         Created 06-May-2010 13:45:47
 */
public class IdExtractor implements AttributeExtractor {
    @Override
    public String getValue(ONDEXEntity cOrr) throws NullValueException, AccessDeniedException, InvalidOndexEntityException {
        return Integer.toString(cOrr.getId());
    }

    @Override
    public String getHeaderName() {
        return "ONDEX_ID";
    }
}
