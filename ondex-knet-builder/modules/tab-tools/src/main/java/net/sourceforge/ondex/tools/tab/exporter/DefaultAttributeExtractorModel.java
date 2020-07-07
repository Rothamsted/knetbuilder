package net.sourceforge.ondex.tools.tab.exporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.ondex.algorithm.pathmodel.Path;
import net.sourceforge.ondex.tools.tab.exporter.extractors.AttributeExtractor;

/**
 * A default attribute extractor model that acts a basic depth specific attribute extractor and demonstrates how the interface is extended to Transform Route positions to Attribute
 * @author hindlem
 *
 */
public class DefaultAttributeExtractorModel implements AttributeExtractorModel{

	private Map<Integer, List<AttributeExtractor>> depthToExtractor;
	private String[] headers;

	/**
	 * 
	 * @param depthToExtractor maps of depths to extractors
	 */
	public DefaultAttributeExtractorModel(Map<Integer, List<AttributeExtractor>> depthToExtractor, 
			String[] headers) {
		this.depthToExtractor = depthToExtractor;
		this.headers = headers;
	}
	
	/**
	 * 
	 */
	public DefaultAttributeExtractorModel(String[] headers) {
		this.depthToExtractor = new HashMap<Integer, List<AttributeExtractor>>();
		this.headers = headers;
	}
	
	/**
	 * 
	 * @param depth depth where this extractor applies
	 * @param extractor the extractor at this level
	 */
	public void addAttributeExtractor(int depth, AttributeExtractor extractor) {
		List<AttributeExtractor> list = depthToExtractor.get(depth);
		if (list == null){
			list = new ArrayList<AttributeExtractor>();
			depthToExtractor.put(depth, list);
		}
		list.add(extractor);
	}
	
	@Override
	public List<AttributeExtractor> getAttributes(int depth, Path route) {
		return depthToExtractor.get(depth);
	}

	@Override
	public String[] getHeader(int depth, int headerLength) {
		return headers;
	}

}
