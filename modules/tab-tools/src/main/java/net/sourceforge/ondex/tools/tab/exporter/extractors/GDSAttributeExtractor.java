package net.sourceforge.ondex.tools.tab.exporter.extractors;

import net.sourceforge.ondex.config.ValidatorRegistry;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.tools.tab.exporter.InvalidOndexEntityException;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author lysenkoa
 */
public class GDSAttributeExtractor implements AttributeExtractor {

    private String TAXID = "TAXID";

    private AttributeName an;
    private boolean translateTaxID;

    public GDSAttributeExtractor(AttributeName at, boolean translateTaxID) throws NullValueException, EmptyStringException {
        this.an = at;
        this.translateTaxID = translateTaxID;
    }

    @SuppressWarnings("rawtypes")
	public String getValue(ONDEXEntity cOrr) throws NullValueException, AccessDeniedException, InvalidOndexEntityException {
        Attribute attribute = cOrr.getAttribute(an);
        if (attribute != null) {
            List<String> values = new ArrayList<String>();

            if (attribute.getValue() instanceof Collection) {
                for (Object value : (Collection) attribute.getValue()) {
                    values.add(value.toString());
                }
            } else if (attribute.getValue().getClass().isArray()) {
                for (Object value : (Object[]) attribute.getValue()) {
                    values.add(value.toString());
                }
            } else {
                values.add(attribute.getValue().toString());
            }

            if (translateTaxID && attribute.getOfType().getId().equals(TAXID)) {
                AbstractONDEXValidator validator = ValidatorRegistry.validators.get("scientificspeciesname");
                StringBuilder sb = new StringBuilder();
                for (String value : values) {
                    String scientificName = (String) validator.validate(value);
                    if (scientificName != null) {
                        if (scientificName.length() > 0)
                            sb.append(scientificName + " (" + value + "), ");
                    }
                }
                if (sb.length() > 2)
                    sb.setLength(sb.length() - 2);
                return sb.toString();
            } else {
                StringBuilder sb = new StringBuilder();
                for (String value : values) {
                    if (value.length() > 0)
                        sb.append(value + ", ");
                }
                if (sb.length() > 2)
                    sb.setLength(sb.length() - 2);
                return sb.toString();
            }
        }
        return "";
    }

    @Override
    public String getHeaderName() {
        return "Attribute:" + an.getId();
    }

}
