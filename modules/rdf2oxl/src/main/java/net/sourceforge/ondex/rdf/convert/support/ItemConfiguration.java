package net.sourceforge.ondex.rdf.convert.support;

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
	private ResourceProcessor resourceProcessor;
	private ResourceHandler resourceHandler;
		
	public ItemConfiguration () {
		super ();
	}
		
	public <RH extends ResourceHandler> ItemConfiguration ( 
		String name, String resourcesQueryName, String constructTemplateName, 
		String header, String graphTemplateName, String trailer
	) {
		this ( name, resourcesQueryName, constructTemplateName, header, graphTemplateName, trailer, null, null );
	}
	
	
	public <RH extends ResourceHandler> ItemConfiguration ( 
		String name, String resourcesQueryName, String constructTemplateName, 
		String header, String graphTemplateName, String trailer,
		ResourceProcessor resourceProcessor,
		ResourceHandler resourceHandler
	)
	{
		super ();
		this.name = name;
		this.resourcesQueryName = resourcesQueryName;
		this.constructTemplateName = constructTemplateName;
		this.header = header;
		this.graphTemplateName = graphTemplateName;
		this.trailer = trailer;
		this.resourceProcessor = resourceProcessor;
		this.resourceHandler = resourceHandler;
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
	
	
	public ResourceProcessor getResourceProcessor ()
	{
		return resourceProcessor;
	}

	public void setResourceProcessor ( ResourceProcessor resourceProcessor )
	{
		this.resourceProcessor = resourceProcessor;
	}

	public ResourceHandler getResourceHandler ()
	{
		return resourceHandler;
	}

	public void setResourceHandler ( ResourceHandler ResourceHandler )
	{
		this.resourceHandler = ResourceHandler;
	}

}
