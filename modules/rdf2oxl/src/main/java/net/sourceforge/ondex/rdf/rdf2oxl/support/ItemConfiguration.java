package net.sourceforge.ondex.rdf.rdf2oxl.support;

/**
 * TODO: comment me!
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
		
	public <RH extends QuerySolutionHandler> ItemConfiguration ( 
		String name, String resourcesQueryName, String constructTemplateName, 
		String header, String graphTemplateName, String trailer
	) {
		this ( name, resourcesQueryName, constructTemplateName, header, graphTemplateName, trailer, null, null );
	}
	
	
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



	public String getName ()
	{
		return name;
	}

	public void setName ( String name )
	{
		this.name = name;
	}

	public String getResourcesQueryName ()
	{
		return resourcesQueryName;
	}

	public void setResourcesQueryName ( String resourcesQueryName )
	{
		this.resourcesQueryName = resourcesQueryName;
	}

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
