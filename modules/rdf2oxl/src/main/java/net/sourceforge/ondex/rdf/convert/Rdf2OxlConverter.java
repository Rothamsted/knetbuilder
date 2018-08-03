package net.sourceforge.ondex.rdf.convert;

import static java.lang.String.format;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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
			ResourceHandler handler = null;
			
			try
			{
				ResourceProcessor processor = this.getResourceProcessor ();
				
				if ( handler == null ) {
					// After the first time, it's always the same
					handler = (ResourceHandler) processor.getConsumer ();
					handler.setOutWriter ( oxlOut );
				}

				String itemName = item.getName ();
				
				handler.setConstructTemplate ( 
					NamespaceUtils.asSPARQLProlog () 
					+ IOUtils.readResource ( this.templateClassPath + "/" + item.getConstructTemplateName () ) 
				);

				processor.setHeader ( item.getHeader () );
				processor.setTrailer ( item.getTrailer () );
				handler.setOxlTemplateName ( item.getGraphTemplateName () );
				
				processor.setLogPrefix ( "[" + itemName + " Processor]" );
				handler.setLogPrefix ( "[" + itemName + " Handler]" );
				
				processor.process (
					NamespaceUtils.asSPARQLProlog () 
					+ IOUtils.readResource ( this.templateClassPath + "/" + item.getResourcesQueryName () )
				);
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

	@Autowired
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
