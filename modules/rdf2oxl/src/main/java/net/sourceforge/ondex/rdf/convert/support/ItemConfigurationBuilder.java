package net.sourceforge.ondex.rdf.convert.support;

import java.util.function.Consumer;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>9 Oct 2018</dd></dl>
 *
 */
public class ItemConfigurationBuilder
{
	private ItemConfiguration instance = new ItemConfiguration ();
	
	public ItemConfigurationBuilder () {
	}

	public ItemConfigurationBuilder ( String name ) 
	{
		this.withName ( name );
	}
	
	protected ItemConfigurationBuilder set ( Runnable setter )
	{
		setter.run ();
		return this;
	}
	
	public ItemConfigurationBuilder withName ( String name ) {
		return set ( () -> instance.setName ( name ) );
	}

	public ItemConfigurationBuilder withResourcesQueryName ( String resourcesQueryName ) {
		return set ( () -> instance.setResourcesQueryName ( resourcesQueryName ) );
	}

	public ItemConfigurationBuilder withConstructTemplateName ( String constructTemplateName ) {
		return set ( () -> instance.setConstructTemplateName ( constructTemplateName ) );
	}

	public ItemConfigurationBuilder withGraphTemplateName ( String graphTemplateName ) {
		return set ( () -> instance.setGraphTemplateName ( graphTemplateName ) );
	}
	
	public ItemConfigurationBuilder withHeader ( String header ) {
		return set ( () -> instance.setHeader ( header ) );
	}
	
	public ItemConfigurationBuilder withTrailer ( String trailer ) {
		return set ( () -> instance.setTrailer ( trailer ) );
	}

	public ItemConfigurationBuilder withQueryProcessor ( QueryProcessor queryProcessor ) {
		return set ( () -> instance.setQueryProcessor ( queryProcessor ) );
	}

	public ItemConfigurationBuilder withQuerySolutionHandler ( QuerySolutionHandler querySolutionHandler ) {
		return set ( () -> instance.setQuerySolutionHandler ( querySolutionHandler ) );
	}
	
	public ItemConfiguration build () 
	{
		if ( instance == null ) throw new IllegalStateException ( 
			this.getClass ().getName () + ".build() can be invoked once only per instance" 
		);
		
		ItemConfiguration result = instance;
		instance = null;
		return result;
	}
}
