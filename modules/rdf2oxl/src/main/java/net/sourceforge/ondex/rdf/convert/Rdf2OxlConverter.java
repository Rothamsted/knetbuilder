package net.sourceforge.ondex.rdf.convert;

import static java.lang.String.format;
import static uk.ac.ebi.utils.io.IOUtils.readResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.machinezoo.noexception.Exceptions;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.rdf.convert.support.ItemConfiguration;
import net.sourceforge.ondex.rdf.convert.support.ResourceHandler;
import net.sourceforge.ondex.rdf.convert.support.ResourceProcessor;
import uk.ac.ebi.utils.io.IOUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>1 Aug 2018</dd></dl>
 *
 */
@Component
public class Rdf2OxlConverter
{
	private List<ItemConfiguration> items;
	
	private ResourceProcessor resourceProcessor;
	
	private String templateClassPath;
	
	public void convert ( Writer oxlOut )
	{				
		for ( ItemConfiguration item: getItems () )
		{
			try
			{
				ResourceProcessor processor = item.getResourceProcessor ();
				if ( processor == null ) processor = this.getResourceProcessor ();

				ResourceHandler handler = item.getResourceHandler ();
				if ( handler == null ) handler = (ResourceHandler) processor.getConsumer ();
					else processor.setConsumer ( handler );
					
				processor.setConsumer ( handler );				
				handler.setOutWriter ( oxlOut );

				String itemName = item.getName ();
				
				String constructTemplate = item.getConstructTemplateName ();
				if ( constructTemplate != null ) constructTemplate = 
					NamespaceUtils.asSPARQLProlog () 
					+ readResource ( this.templateClassPath + "/" + constructTemplate ); 
				handler.setConstructTemplate ( constructTemplate );

				processor.setHeader ( item.getHeader () );
				processor.setTrailer ( item.getTrailer () );
				handler.setOxlTemplateName ( item.getGraphTemplateName () );
				
				processor.setLogPrefix ( "[" + itemName + " Processor]" );
				handler.setLogPrefix ( "[" + itemName + " Handler]" );
				
				// Some processors get URIs from previously fetched data, so this might be null  
				String resourcesSparql = item.getResourcesQueryName ();
				if ( resourcesSparql != null ) resourcesSparql = 
						NamespaceUtils.asSPARQLProlog () + readResource ( this.templateClassPath + "/" + resourcesSparql );
							
				processor.process ( resourcesSparql );
			}
			catch ( IOException ex ) {
				throw new UncheckedIOException ( 
					format ( "I/O error while processing %s: %s", item.getName (), ex.getMessage () ),
					ex
				);
			}				
		}
	}
	
	public List<ItemConfiguration> getItems ()
	{
		return items;
	}

	@Autowired @Qualifier ( "itemConfigurations" )
	public void setItems ( List<ItemConfiguration> items )
	{
		this.items = items;
	}

	public ResourceProcessor getResourceProcessor ()
	{
		return resourceProcessor;
	}

	/**
	 * This is {@link ResourceProcessor} by default, {@link ItemConfiguration} can override it.
	 */
	@Autowired @Qualifier ( "resourceProcessor" )
	public void setResourceProcessor ( ResourceProcessor resourceProcessor )
	{
		this.resourceProcessor = resourceProcessor;
	}

	public String getTemplateClassPath ()
	{
		return templateClassPath;
	}

	@Autowired @Qualifier ( "templateClassPath" )
	public void setTemplateClassPath ( String templateClassPath )
	{
		this.templateClassPath = templateClassPath;
	}	
}
