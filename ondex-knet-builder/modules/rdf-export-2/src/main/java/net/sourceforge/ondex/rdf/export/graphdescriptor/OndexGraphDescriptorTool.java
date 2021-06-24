package net.sourceforge.ondex.rdf.export.graphdescriptor;

import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Jun 2021</dd></dl>
 *
 */
public class OndexGraphDescriptorTool
{
	private ONDEXGraph graph;
	
	/**
	 * Creates the descriptor without saving it in the graph.
	 *  
	 */
	public Model createDescriptor ( Map<String, Object> context, String rdfTemplate, String rdfLang )
	{
		return null;
	}

	public Model saveDescriptor ( Map<String, Object> context, String rdfTemplate, String rdfLang )
	{
		return null;
	}
	
	/**
	 * Fetches the descriptor from the graph.
	 */
	public Model getDescriptor ()
	{
		return null;
	}
	
	/**
	 * Fetches the descriptor from the graph and returns it as JSON simplified format. 
	 * 
	 */
	public Map<String, Object> getDescriptorAsJsonLd ()
	{
		return null;
	}

	/**
	 * Returns a by-type index of the descriptor.
	 */
	public Map<String, List<Map<String, Object>>> getDescriptorTypes ()
	{
		return null;
	}
	
	/**
	 * Returns the schema:Dataset instance from the saved descriptor
	 */
	public Map<String, Object> getDescriptorDataset () {
		return null;
	}
	
	/**
	 * Returns the schema:Organization instance from the saved descriptor
	 */
	public Map<String, Object> getDescriptorOrganization () {
		return null;
	}
}
