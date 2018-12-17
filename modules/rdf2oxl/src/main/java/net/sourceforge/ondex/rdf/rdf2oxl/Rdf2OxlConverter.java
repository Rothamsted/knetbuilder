package net.sourceforge.ondex.rdf.rdf2oxl;

import static java.lang.String.format;
import static uk.ac.ebi.utils.exceptions.ExceptionUtils.throwEx;
import static uk.ac.ebi.utils.io.IOUtils.readResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Component;

import info.marcobrandizi.rdfutils.jena.TDBEndPointHelper;
import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.rdf.rdf2oxl.support.ItemConfiguration;
import net.sourceforge.ondex.rdf.rdf2oxl.support.QueryProcessor;
import net.sourceforge.ondex.rdf.rdf2oxl.support.QuerySolutionHandler;
import net.sourceforge.ondex.rdf.rdf2oxl.support.Resettable;

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
	/* For some reason a bean defined as util:list is only fit in here when @Resource is used, and not
	 * @Autowired as cases below (<a href = "https://stackoverflow.com/questions/8618612">ref</a>). 
	 */
	@Resource ( name = "itemConfigurations" )
	private List<ItemConfiguration> itemConfigurations;
	
	@Autowired @Qualifier ( "templateClassPath" )	
	private String templateClassPath;

	/**
	 * This is {@link QueryProcessor} by default, {@link ItemConfiguration} can override it.
	 */
	@Autowired @Qualifier ( "resourceProcessor" )	
	private QueryProcessor defaultQueryProcessor;
		
	/**
	 * Same as {@link #defaultQueryProcessor}
	 */
	@Autowired @Qualifier ( "resourceHandler" )	
	private QuerySolutionHandler defaultQuerySolutionHandler;
	
	
	@Autowired ( required = false )
	private List<Resettable> toBeResetComponents;
	
	
	protected void convertRaw ( Writer oxlOut )
	{		
		try 
		{
			if ( this.toBeResetComponents != null ) toBeResetComponents.forEach ( Resettable::reset );
			
			for ( ItemConfiguration itemCfg: this.itemConfigurations )
			{
				try
				{
					QueryProcessor processor = itemCfg.getQueryProcessor ();
					if ( processor == null ) processor = this.defaultQueryProcessor;
	
					QuerySolutionHandler handler = itemCfg.getQuerySolutionHandler ();
					if ( handler == null ) handler = this.defaultQuerySolutionHandler;
						
					processor.setConsumer ( handler );				
					handler.setOutWriter ( oxlOut );
	
					String itemName = itemCfg.getName ();
					
					String constructTemplate = itemCfg.getConstructTemplateName ();
					if ( constructTemplate != null ) constructTemplate = 
						NamespaceUtils.asSPARQLProlog () 
						+ readResource ( this.templateClassPath + "/" + constructTemplate ); 
					handler.setConstructTemplate ( constructTemplate );
	
					processor.setHeader ( itemCfg.getHeader () );
					processor.setTrailer ( itemCfg.getTrailer () );
					handler.setOxlTemplateName ( itemCfg.getGraphTemplateName () );
					
					processor.setLogPrefix ( "[" + itemName + " Processor]" );
					handler.setLogPrefix ( "[" + itemName + " Handler]" );
					
					// Some processors get URIs from previously fetched data, so this might be null  
					String resourcesSparql = itemCfg.getResourcesQueryName ();
					if ( resourcesSparql != null ) resourcesSparql = 
							NamespaceUtils.asSPARQLProlog () + readResource ( this.templateClassPath + "/" + resourcesSparql );
								
					processor.process ( resourcesSparql );
				}
				catch ( IOException ex ) {
					throw new UncheckedIOException ( 
						format ( "I/O error while processing %s: %s", itemCfg.getName (), ex.getMessage () ),
						ex
					);
				}				
			} // for
		} // main try
		finally 
		{
			try {
				if ( oxlOut != null ) oxlOut.close ();
			}
			catch ( IOException ex ) {
				throwEx ( UncheckedIOException.class, ex, "Error while converting to OXL: %s", ex.getMessage () );
			}			
		}
	}
	
	
	public void convert ( OutputStream oxlOut, boolean compress )
	{
		try	
		{
			Writer oxlw = compress
				? new OutputStreamWriter ( new GZIPOutputStream (	oxlOut, (int) 1E6 ) )
				: new OutputStreamWriter ( oxlOut );
			convertRaw ( oxlw );
		}
		catch ( IOException ex ) {
			throwEx ( UncheckedIOException.class, ex, 
				"I/O Error while running compresses rdf2oxl: %s", ex.getMessage () 
			);
		}
	}
		
	public void convert ( File file, boolean compress )
	{
		try {
			convert ( new FileOutputStream ( file ), compress );
		}
		catch ( IOException ex ) {
			throwEx ( UncheckedIOException.class, ex, "I/O Error while running rdf2oxl: %s", ex.getMessage () );
		}
	}
	
	public void convert ( String oxlPath, boolean compress )
	{
		try {
			convert ( new File ( oxlPath ), compress );
		}
		catch ( UncheckedIOException ex ) {
			throwEx ( UncheckedIOException.class, ex.getCause (), "I/O Error while making OXL file '%s': %s", oxlPath, ex.getMessage () );
		}
	}

	
	public static ConfigurableApplicationContext getConverterConfiguration () {
		return getConverterConfiguration ( null );
	}
	
	public static ConfigurableApplicationContext getConverterConfiguration ( String springXmlPath )
	{
		return springXmlPath == null
				? new ClassPathXmlApplicationContext ( "default_beans.xml" )
				: new FileSystemXmlApplicationContext ( springXmlPath );
	}
	
	public static void convert ( ConfigurableApplicationContext springContext, String tdbPath, String oxlPath, boolean compress ) 
	{
		TDBEndPointHelper sparqlHelper = springContext.getBean ( TDBEndPointHelper.class );
		sparqlHelper.open ( tdbPath );

		Rdf2OxlConverter converter = springContext.getBean ( Rdf2OxlConverter.class );
		converter.convert ( oxlPath, compress );
	}

	public static void convert ( String springXmlPath, String tdbPath, String oxlPath, boolean compress ) 
	{
		convert ( getConverterConfiguration ( springXmlPath ), tdbPath, oxlPath, compress );
	}

	public static void convert ( String springXmlPath, String tdbPath, String oxlPath ) 
	{
		convert ( getConverterConfiguration ( springXmlPath ), tdbPath, oxlPath, true );
	}

	public static void convert ( String tdbPath, String oxlPath, boolean compress ) {
		convert ( getConverterConfiguration (), tdbPath, oxlPath, compress );
	}

	public static void convert ( String tdbPath, String oxlPath ) {
		convert ( tdbPath, oxlPath, true );
	}
}
