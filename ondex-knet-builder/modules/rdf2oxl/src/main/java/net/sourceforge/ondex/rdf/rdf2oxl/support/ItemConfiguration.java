package net.sourceforge.ondex.rdf.rdf2oxl.support;

import net.sourceforge.ondex.rdf.rdf2oxl.Rdf2OxlConverter;

/**
 * # The Item Configuration class
 * 
 * Used in Spring (see `main/resources/default_beans.xml) to define the configuration list passed to 
 * {@link Rdf2OxlConverter#itemConfigurations}. Each configuration is typically about an OXL type (eg, concept, 
 * relation) and puts together the {@link QueryProcessor} that gets URIs of the type instances with the
 * {@link QuerySolutionHandler handler} that should process/render those instances.  
 * 
 * See the [package description](package-summary.html) for details.  
 *
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>1 Aug 2018</dd></dl>
 *
 */
public class ItemConfiguration
{
	private String name;
	private String resourcesQueryName;
	private String constructTemplateName;
	private String header;
	private String graphTemplateName;
	private String trailer;
	private QueryProcessor queryProcessor;
	private QuerySolutionHandler querySolutionHandler;
		
	public ItemConfiguration () {
		super ();
	}
		
	/**
	 * These are facilities used for writing tests.
	 */
	public <RH extends QuerySolutionHandler> ItemConfiguration ( 
		String name, String resourcesQueryName, String constructTemplateName, 
		String header, String graphTemplateName, String trailer
	) {
		this ( name, resourcesQueryName, constructTemplateName, header, graphTemplateName, trailer, null, null );
	}
	
	/**
	 * These are facilities used for writing tests.
	 */	
	public <RH extends QuerySolutionHandler> ItemConfiguration ( 
		String name, String resourcesQueryName, String constructTemplateName, 
		String header, String graphTemplateName, String trailer,
		QueryProcessor queryProcessor,
		QuerySolutionHandler querySolutionHandler
	)
	{
		super ();
		this.name = name;
		this.resourcesQueryName = resourcesQueryName;
		this.constructTemplateName = constructTemplateName;
		this.header = header;
		this.graphTemplateName = graphTemplateName;
		this.trailer = trailer;
		this.queryProcessor = queryProcessor;
		this.querySolutionHandler = querySolutionHandler;
	}


	/**
	 * Usually the type of entity a configuration deals with (eg, Concept, Relation).
	 */
	public String getName ()
	{
		return name;
	}

	public void setName ( String name )
	{
		this.name = name;
	}

	/**
	 * The SPARQL query that gets URIs for the instances of the resource type the configuration deals with (eg, all the 
	 * Concept instances).
	 */
	public String getResourcesQueryName ()
	{
		return resourcesQueryName;
	}

	public void setResourcesQueryName ( String resourcesQueryName )
	{
		this.resourcesQueryName = resourcesQueryName;
	}

	/**
	 * A SPARQL `CONSTRUCT` query to retrieve resource details. See {@link QuerySolutionHandler} for details.  
	 * 
	 */
	public String getConstructTemplateName ()
	{
		return constructTemplateName;
	}

	public void setConstructTemplateName ( String constructTemplateName )
	{
		this.constructTemplateName = constructTemplateName;
	}
	
	public String getHeader ()
	{
		return header;
	}

	public void setHeader ( String header )
	{
		this.header = header;
	}

	public String getGraphTemplateName ()
	{
		return graphTemplateName;
	}

	public void setGraphTemplateName ( String graphTemplateName )
	{
		this.graphTemplateName = graphTemplateName;
	}

	public String getTrailer ()
	{
		return trailer;
	}

	public void setTrailer ( String trailer )
	{
		this.trailer = trailer;
	}
	
	
	public QueryProcessor getQueryProcessor ()
	{
		return queryProcessor;
	}

	public void setQueryProcessor ( QueryProcessor queryProcessor )
	{
		this.queryProcessor = queryProcessor;
	}

	public QuerySolutionHandler getQuerySolutionHandler ()
	{
		return querySolutionHandler;
	}

	public void setQuerySolutionHandler ( QuerySolutionHandler querySolutionHandler )
	{
		this.querySolutionHandler = querySolutionHandler;
	}

}
