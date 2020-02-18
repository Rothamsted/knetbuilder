package net.sourceforge.ondex.doclet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.util.DocTrees;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import net.sourceforge.ondex.ONDEXPlugin;
import net.sourceforge.ondex.annotations.DataURL;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.metadata.AttributeNameRequired;
import net.sourceforge.ondex.annotations.metadata.ConceptClassRequired;
import net.sourceforge.ondex.annotations.metadata.DataSourceRequired;
import net.sourceforge.ondex.annotations.metadata.EvidenceTypeRequired;
import net.sourceforge.ondex.annotations.metadata.RelationTypeRequired;
import net.sourceforge.ondex.init.PluginType;
import net.sourceforge.ondex.parser.ONDEXParser;
import uk.ac.ebi.utils.exceptions.UncheckedFileNotFoundException;

/**
 * @author hindlem
 * @author Marco Brandizi, I rewrote this in 2020, to comply with the new JavaDoc APIs
 */
public class PluginDoclet implements Doclet
{
	private static final String NAMESPACE = "http://ondex.sourceforge.net/workflow_element";
	
	private static final String[] OPTION_NAMES = new String[] {
		"-filename", "-groupId", "-artifactId", "-version", "-classdir", "-libsdir"
	};
	
	private Map<String, String> options = new HashMap<>();
	
	private Reporter reporter;
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Override
	public void init ( Locale locale, Reporter reporter ) {
		this.reporter = reporter;
	}
	
	@Override
	public boolean run ( DocletEnvironment docletEnv )
	{
		try
		{
			return runWithExceptions ( docletEnv );
		}
		catch ( XMLStreamException ex )
		{
			throw new RuntimeException ( 
				"XML processing error while generating the plug-in descriptor: " + ex.getMessage (), 
				ex
			);
		}
	} 

	private boolean runWithExceptions ( DocletEnvironment docletEnv )
		throws XMLStreamException 
	{
		// Get some options and working vars
		//
		Types typeUtils = docletEnv.getTypeUtils ();
		Elements elemUtils = docletEnv.getElementUtils ();
		DocTrees docTreeUtils = docletEnv.getDocTrees ();
		
		String fileName = "workflow-component-description.xml";
		
		File f = new File ( "tmp.del" );
		f.deleteOnExit ();
		String classDir = f.getParent ();
		String libsDir = f.getParent ();

		if ( options.get ( "-classdir" ) != null ) classDir = options.get ( "-classdir" );
		if ( classDir == null ) {
			reporter.print ( Diagnostic.Kind.ERROR, "-classdir not specified" );
			return false;
		}
		if ( options.get ( "-classdir" ) != null ) classDir = options.get ( "-classdir" );
		if ( classDir == null ) {
			reporter.print ( Diagnostic.Kind.ERROR, "-classdir not specified" );
			return false;
		}
		if ( options.get ( "-libsdir" ) != null ) libsDir = options.get ( "-libsdir" );
		// TODO: libsDir wasn't originally checked, is it optional?
		
		
		// Prepare the plugin register
		// 
		Set<URL> jarRegisterBuilder = new HashSet<> ();
		File rootDir = new File ( classDir );
		
		try
		{
			addFiles ( new File ( libsDir ), jarRegisterBuilder );

			URL[] urls = jarRegisterBuilder.toArray ( new URL[ jarRegisterBuilder.size () + 1 ] );
			urls[ urls.length - 1 ] = rootDir.toURI ().toURL ();

			// NB you must extend the context class loader as JavaDoc uses its
			// own URLClassLoader
			URLClassLoader ucl = URLClassLoader.newInstance ( urls, Thread.currentThread ().getContextClassLoader () );
			Thread.currentThread ().setContextClassLoader ( ucl );
		}
		catch ( MalformedURLException ex )
		{
			throw new IllegalArgumentException ( 
				"Cannot set the new class path, got the error: " + ex.getMessage (),
				ex 
			);
		}

		reporter.print ( Diagnostic.Kind.NOTE, "Generating :" + fileName );
		
		
		// Scan the plugin classes
		//
		
		// Stand-alone version from http://sjsxp.dev.java.net:
		// TODO: doesn't seem relevant here, remove
		// System.setProperty("javax.xml.stream.XMLInputFactory", "com.sun.xml.stream.ZephyrParserFactory");
		System.setProperty ( "ondex.zephyr.javax.xml.stream.XMLOutputFactory", "com.sun.xml.stream.ZephyrWriterFactory" );
		XMLOutputFactory xof = XMLOutputFactory.newFactory ( "ondex.zephyr.javax.xml.stream.XMLOutputFactory",
				PluginDoclet.class.getClassLoader () );
		xof.setProperty ( "javax.xml.stream.isRepairingNamespaces", true );
		
		FileOutputStream xmlOut;
		try
		{
			xmlOut = new FileOutputStream ( fileName );
		}
		catch ( FileNotFoundException ex )
		{
			// TODO Auto-generated catch block
			throw new UncheckedFileNotFoundException ( 
				"Error while writing the plug-in descriptor '" + fileName + "'" +
				", error: " + ex.getMessage (),
				ex 
			);
		}
		
		XMLStreamWriter xtw = xof.createXMLStreamWriter ( xmlOut, "UTF8" );

		TypeElement pluginClass = elemUtils.getTypeElement ( ONDEXPlugin.class.getCanonicalName () );
		TypeElement validatorClass = elemUtils.getTypeElement ( ONDEXPlugin.class.getCanonicalName () );
		
		Set<TypeElement> pluginTypes = new HashSet<> ();
				
		for ( PluginType pt : PluginType.values () )
		{
			TypeElement ptd = elemUtils.getTypeElement ( pt.getPluginClass ().getCanonicalName () );
			if ( ptd != null )
				pluginTypes.add ( ptd );
		}
		
		xtw.writeStartDocument ();
		xtw.writeProcessingInstruction ( "xml-stylesheet type='text/xsl' href='workflow-component-description.xsl'" );
		xtw.writeStartElement ( "plugins" );
		xtw.setPrefix ( "wfc", NAMESPACE );

		writeOption2Xml ( xtw, "-artifactId" );
		writeOption2Xml ( xtw, "-groupId" );
		writeOption2Xml ( xtw, "-version" );
		
		Set<? extends Element> docElements = docletEnv.getIncludedElements ();
		for ( Element elem : docElements )
		{
			if ( ! ( elem instanceof TypeElement ) ) continue;
			
			TypeElement classDoc = (TypeElement) elem;
			TypeMirror classDocType = classDoc.asType ();
			
			// TODO: check that it's a concrete class is omitted, let's hope for the best...
			
			if ( ! ( typeUtils.isAssignable ( classDocType, pluginClass.asType () ) 
					|| validatorClass != null && typeUtils.isAssignable ( classDocType, validatorClass.asType () )
			)) continue;
				
			for ( TypeElement pluginType : pluginTypes )
			{
				if ( !typeUtils.isAssignable ( classDocType, pluginType.asType () ) ) continue;

				// XML header
				//
				xtw.writeStartElement ( NAMESPACE, "plugin" );
				xtw.writeAttribute ( "type", pluginType.getQualifiedName ().toString () );

				writeXmlElem ( xtw, "entryClass", classDoc.getQualifiedName ().toString () );

				// Plugin details
				//
				Class<?> classPlugin;
				try {
					classPlugin = Thread.currentThread ().getContextClassLoader ().loadClass ( classDoc.getQualifiedName ().toString () );
				}
				catch ( ClassNotFoundException|NoClassDefFoundError ex ) 
				{
					throw new IllegalArgumentException ( 
						"Cannot load the plugin class '" + classDoc.getQualifiedName () + "'" +
						", error: " + ex.getMessage (),
						ex
					);
				}

				if ( Modifier.isAbstract ( classPlugin.getModifiers () ) ) {
					reporter.print ( Diagnostic.Kind.WARNING, classPlugin.getName () + " is abstract and not a valid plugin: ignoring" );
					continue;
				} 
				if ( Modifier.isInterface ( classPlugin.getModifiers () ) ) {
					reporter.print ( Diagnostic.Kind.WARNING, classPlugin.getName () + " is an interface and not a valid plugin: ignoring" );
					continue;
				}				

				Status statusAnnotation = classPlugin.getAnnotation ( Status.class );
				if ( statusAnnotation != null )
				{
					String status = statusAnnotation.status ().getValue ().trim ();
					String description = statusAnnotation.description ().trim ();

					xtw.writeStartElement ( NAMESPACE, "status" );
					xtw.writeAttribute ( "type", status );
					writeXmlElemCDATA ( xtw, "description", description );
					xtw.writeEndElement ();
				} 
				else
					reporter.print ( 
						Diagnostic.Kind.WARNING, 
						classDoc.getQualifiedName () + " is missing annotation tag " + Status.class.getName () 
				);


				DatabaseTarget dbAnnotation = classPlugin.getAnnotation ( DatabaseTarget.class );
				if ( dbAnnotation != null )
				{
					String name = dbAnnotation.name ().trim ();
					String description = dbAnnotation.description ().trim ();
					String url = dbAnnotation.url ().trim ();

					xtw.writeStartElement ( NAMESPACE, "database" );
					xtw.writeAttribute ( "name", name );

					writeXmlElemCDATA ( xtw, "url", url );
					writeXmlElemCDATA ( xtw, "description", description );

					xtw.writeEndElement (); // end database
				} 
				else if ( pluginType.getQualifiedName ().equals ( ONDEXParser.class.getCanonicalName () ) )
					reporter.print ( 
						Diagnostic.Kind.WARNING, 
						classDoc.getQualifiedName () + " is missing annotation tag " + DatabaseTarget.class.getName () 
				);

				
				DataURL dburlAnnotation = classPlugin.getAnnotation ( DataURL.class );
				if ( dburlAnnotation != null )
				{
					String name = dburlAnnotation.name ().trim ();
					String description = dburlAnnotation.description ().trim ();
					String[] urls = dburlAnnotation.urls ();

					xtw.writeStartElement ( NAMESPACE, "data_files" );
					xtw.writeAttribute ( "name", name );

					xtw.writeStartElement ( NAMESPACE, "urls" );
					for ( String url : urls )
						writeXmlElemCDATA ( xtw, "url", url.trim () );
					xtw.writeEndElement (); // end urls

					writeXmlElemCDATA ( xtw, "description", description );

					xtw.writeEndElement (); // end database
				} 
				else if ( pluginType.getQualifiedName ().equals ( ONDEXParser.class.getCanonicalName () ) )
					reporter.print ( 
						Diagnostic.Kind.WARNING, 
						classDoc.getQualifiedName () + " is missing annotation tag " + DataURL.class.getName () 
				);
				
				
				// More details
				//
				
				xtw.writeStartElement ( NAMESPACE, "ondex-metadata" );

				DataSourceRequired cvAnnotation = classPlugin.getAnnotation ( DataSourceRequired.class );
				if ( cvAnnotation != null )
					writeMetaData ( xtw, "data_source", cvAnnotation.ids () );

				ConceptClassRequired conceptclassAnnotation = classPlugin.getAnnotation ( ConceptClassRequired.class );
				if ( conceptclassAnnotation != null )
					writeMetaData ( xtw, "concept_class", conceptclassAnnotation.ids () );

				AttributeNameRequired attributeAnnotation = classPlugin.getAnnotation ( AttributeNameRequired.class );
				if ( attributeAnnotation != null )
					writeMetaData ( xtw, "attribute_name", attributeAnnotation.ids () );

				RelationTypeRequired relationtypeAnnotation = classPlugin.getAnnotation ( RelationTypeRequired.class );
				if ( relationtypeAnnotation != null )
					writeMetaData ( xtw, "relation_type", relationtypeAnnotation.ids () );

				EvidenceTypeRequired evidencetypeAnnotatio = classPlugin.getAnnotation ( EvidenceTypeRequired.class );
				if ( evidencetypeAnnotatio != null )
					writeMetaData ( xtw, "evidence_type", evidencetypeAnnotatio.ids () );

				xtw.writeEndElement (); // end ondex-metadata
				
				writeXmlElem ( xtw, "comment", elemUtils.getDocComment ( classDoc ) );

				xtw.writeStartElement ( NAMESPACE, "authors" );

				// Authors
				//
				DocCommentTree docTree = docTreeUtils.getDocCommentTree ( classDoc );
				List<? extends DocTree> docTags = docTree.getBlockTags ();
				for ( DocTree tag: docTags )
				{
					if ( !tag.getKind ().equals ( DocTree.Kind.AUTHOR ) ) continue;
					String authorsTxt = tag.toString ();
					String [] authors = authorsTxt.split ( "," );
					for ( String authorName : authors )
						writeXmlElem ( xtw, "author", authorName.trim () );
				}
				
				xtw.writeEndElement (); // authors
				xtw.writeEndElement (); // plugin				
				
			} // for pluginType
		} // for elem 

		xtw.writeEndElement (); // end plugins
		xtw.writeEndDocument ();
		xtw.flush ();
		xtw.close ();
		
		return true;
		
	} // runWithExceptions()
	

	private static void _writeXmlElem ( 
		XMLStreamWriter xtw, String name, String value, boolean doCDATA, @SuppressWarnings ( "unchecked" ) Pair<String, String> ...attributes 
	) throws XMLStreamException
	{
		xtw.writeStartElement ( NAMESPACE, name );
		
		if ( attributes != null )
			for ( Pair<String, String> attr: attributes )
				xtw.writeAttribute ( attr.getKey (), attr.getValue () );
		
		if ( doCDATA ) xtw.writeCData ( value ); else xtw.writeCharacters ( value );
		xtw.writeEndElement ();
	}

	private static void writeXmlElem ( 
		XMLStreamWriter xtw, String name, String value, @SuppressWarnings ( "unchecked" ) Pair<String, String> ...attributes
	) throws XMLStreamException
	{
		_writeXmlElem ( xtw, name, value, false, attributes );
	}

	
	private static void writeXmlElem ( XMLStreamWriter xtw, String name, String value )
		throws XMLStreamException
	{
		writeXmlElem ( xtw, name, value, (Pair<String, String>[]) null );
	}

	private static void writeXmlElemCDATA (
		XMLStreamWriter xtw, String name, String value, @SuppressWarnings ( "unchecked" ) Pair<String, String> ...attributes )
	throws XMLStreamException
	{
		_writeXmlElem ( xtw, name, value, true, attributes );
	}

	private static void writeXmlElemCDATA ( XMLStreamWriter xtw, String name, String value )
		throws XMLStreamException
	{
		writeXmlElemCDATA ( xtw, name, value, (Pair<String, String>[]) null );
	}


	private void writeOption2Xml ( XMLStreamWriter xtw, String optName )
		throws XMLStreamException
	{
		String val = options.get ( optName );

		if ( val == null ) {
			reporter.print ( Diagnostic.Kind.WARNING, "Option '" + optName + "' not specified" );
			return;
		}
		
		writeXmlElem ( xtw, optName, val );				
	}
	

	/**
	 * writes a metadata description block
	 * 
	 * @param xtw
	 * @param type
	 * @param ids
	 * @throws XMLStreamException
	 */
	private static void writeMetaData ( XMLStreamWriter xtw, String type, String[] ids ) throws XMLStreamException
	{
		xtw.writeStartElement ( NAMESPACE, "metadata" );
		xtw.writeAttribute ( "type", type );

		for ( String id : ids )
		{
			xtw.writeStartElement ( NAMESPACE, "id" );
			xtw.writeCharacters ( id.trim () );
			xtw.writeEndElement (); // comment
		}

		xtw.writeEndElement (); // comment
	}

	private void addFiles ( File classDir, Set<URL> classRegisterBuilder )
		throws MalformedURLException
	{
		if ( !classDir.exists () )
			throw new RuntimeException ( "The file: " + classDir + " does not exist" );
		if ( !classDir.isDirectory () )
			throw new RuntimeException ( "The file: " + classDir + " is not a directory" );
		
		File[] files = classDir.listFiles ();
		if ( files == null )
			throw new NullPointerException ( "File.listFiles() returned null for an unknown reason" );
		
		for ( File file : classDir.listFiles () )
		{
			if ( file.isDirectory () ) addFiles ( file, classRegisterBuilder );
			// TODO: doesn't work in new version, is it needed?! 
			// && !file.getName ().startsWith ( "workflow-api" ) )
			else if ( file.isFile () && file.getName ().endsWith ( ".jar" ) )
			{
				classRegisterBuilder.add ( file.toURI ().toURL () );
				log.info ( "PlugInDoclet, adding classpath: '{}'", file.toURI ().toURL ().toString () );
			}
		}
	}


  public Set<? extends Option> getSupportedOptions()
  {
  	Set<Option> docletOpts = new HashSet<> ();
  	
		for ( String optName: OPTION_NAMES )
		{
			options.put ( optName, null );
			docletOpts.add ( new Option()
			{
				@Override
				public int getArgumentCount () {
					return 1;
				}

				@Override
				public String getDescription () {
					return ""; // TODO
				}

				@Override
				public Kind getKind () {
					return Kind.OTHER;
				}

				@Override
				public List<String> getNames () {
					return List.of ( optName );
				}

				@Override
				public String getParameters () {	
					return "<value>"; // TODO
				}

				@Override
				public boolean process ( String opt, List<String> vals )
				{
					// It seems that vals contains all the option list here?!
					if ( vals == null ) {
						reporter.print ( Diagnostic.Kind.ERROR, "Null args for the option '" + opt + "'" );
						return false;
					}
					if ( vals.size () < 1 ) {
						reporter.print ( Diagnostic.Kind.ERROR, "Zero args for the option '" + opt + "'" );
						return false;
					}
					
					options.put ( opt, vals.get ( 0 ) );
					return true;
				}
			});
		} // for optName
		
		return docletOpts;
  } // getSupportedOptions

	@Override
	public String getName () {
		return "Ondex Plug-In Doclet";
	}

	@Override
	public SourceVersion getSupportedSourceVersion ()
	{
		return SourceVersion.latestSupported();
	}	
}
