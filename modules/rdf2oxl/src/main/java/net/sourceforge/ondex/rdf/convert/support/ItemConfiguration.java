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
	
	public ItemConfiguration () {
		super ();
	}
	
	public ItemConfiguration ( 
		String name, String resourcesQueryName, String constructTemplateName, 
		String header, String graphTemplateName, String trailer 
	)
	{
		super ();
		this.name = name;
		this.resourcesQueryName = resourcesQueryName;
		this.constructTemplateName = constructTemplateName;
		this.header = header;
		this.graphTemplateName = graphTemplateName;
		this.trailer = trailer;
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
}
