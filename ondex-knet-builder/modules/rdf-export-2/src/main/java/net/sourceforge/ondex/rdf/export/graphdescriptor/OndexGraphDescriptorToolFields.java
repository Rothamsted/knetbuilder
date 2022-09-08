package net.sourceforge.ondex.rdf.export.graphdescriptor;

import java.util.Map;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.rdf.export.graphdescriptor.OndexGraphDescriptorTool.Builder;

/**
 * Simple base class of fields that are shared by both {@link OndexGraphDescriptorTool} and its
 * {@link Builder}
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>7 Dec 2021</dd></dl>
 *
 */
abstract class OndexGraphDescriptorToolFields
{
	protected ONDEXGraph graph;
	protected Map<String, Object> context;
	protected String rdfTemplate;
	protected String rdfLang;
	protected String oxlSourceURL;

	protected OndexGraphDescriptorToolFields ( 
		ONDEXGraph graph, Map<String, Object> context, String rdfTemplate, String rdfLang, String oxlSourceURL
	)
	{
		this ();
		this.graph = graph;
		this.context = context;
		this.rdfTemplate = rdfTemplate;
		this.rdfLang = rdfLang;
		this.oxlSourceURL = oxlSourceURL;
	}

	protected OndexGraphDescriptorToolFields ()
	{
		super ();
	}
}
