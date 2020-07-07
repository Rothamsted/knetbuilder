package net.sourceforge.ondex.tools.tab.exporter;

import java.util.List;

import net.sourceforge.ondex.algorithm.pathmodel.Path;
import net.sourceforge.ondex.tools.tab.exporter.extractors.AttributeExtractor;

/**
 * An interface that allows the defining of attributes that the user required printed at each depth
 * @author hindlem
 *
 */
public interface AttributeExtractorModel {
	/**
	 * 
	 * @param depth the current depth includeing concepts and relations (additive depth)
	 * @param route the route that is being printed
	 * @return list of user defined Attributes
	 */
	public List<AttributeExtractor> getAttributes(int depth, Path route);
	
	public String[] getHeader(int depth, int headerLength);
	
}
