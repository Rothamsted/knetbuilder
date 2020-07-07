package net.sourceforge.ondex.tools.tab.exporter.extractors;

import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.tools.tab.exporter.InvalidOndexEntityException;

/**
 * 
 * @author hindlem, lysenkoa
 * 
 */
public interface AttributeExtractor {

	/**
	 * Gets the string value to print to tabdelim on this Entity
	 * 
	 * @param cOrr
	 * @return the value to be printed
	 * @throws NullValueException
	 * @throws AccessDeniedException
	 */
	public String getValue(ONDEXEntity cOrr) throws NullValueException,
			AccessDeniedException, InvalidOndexEntityException;

	/**
	 * Gets the header name for this extractor
	 * 
	 * @return header name
	 */
	public String getHeaderName();
	
}
