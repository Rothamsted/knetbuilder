package net.sourceforge.ondex.doclet;

import static java.lang.String.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

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
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.annotations.metadata.AttributeNameRequired;
import net.sourceforge.ondex.annotations.metadata.ConceptClassRequired;
import net.sourceforge.ondex.annotations.metadata.DataSourceRequired;
import net.sourceforge.ondex.annotations.metadata.EvidenceTypeRequired;
import net.sourceforge.ondex.annotations.metadata.RelationTypeRequired;
import net.sourceforge.ondex.init.PluginType;
import net.sourceforge.ondex.parser.ONDEXParser;
import uk.ac.ebi.utils.exceptions.UncheckedFileNotFoundException;
import uk.ac.ebi.utils.regex.RegEx;

/**
 * A doclet used together with the Maven Javadoc plugin, to produce XML descriptors for the 
 * plugins. Those descriptors are then packaged (see the module `ondex-knetminer-builder/modules/pom.xml`)
 * into the plugin jar, and they are the basis for the integrator to fetch plugin info.
 * 
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
		
		if ( options.get ( "-filename" ) != null ) 
			fileName = options.get ( "-filename" );
		else {
			reporter.print ( Diagnostic.Kind.NOTE, "-filename not specified, using default '" + fileName + "'" );
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
			URLClassLoader xClsLoader = URLClassLoader.newInstance ( urls, Thread.currentThread ().getContextClassLoader () );
			Thread.currentThread ().setContextClassLoader ( xClsLoader );
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

				String status = getLoadedAnnotation ( classPlugin, Status.class, "status.getValue" );
				if ( status != null )
				{
					status = status.trim ();
					String description = 
						Optional.ofNullable ( (String) getLoadedAnnotation ( classPlugin, Status.class, "description" ) )
						.map ( String::trim )
						.orElse ( "" );

					xtw.writeStartElement ( NAMESPACE, "status" );
					xtw.writeAttribute ( "type", status );
					writeXmlElemCDATA ( xtw, "description", description );
					xtw.writeEndElement ();
				} 
				else
					reporter.print ( 
						Diagnostic.Kind.WARNING, 
						classDoc.getQualifiedName () + " is missing annotation tag " + Status.class.getSimpleName () 
				);

				String dbname = getLoadedAnnotation ( classPlugin, DatabaseTarget.class, "name" );
				if ( dbname != null )
				{
					dbname = dbname.trim ();
					String description = getLoadedAnnotation ( classPlugin, DatabaseTarget.class, "name" );
					description = description.trim ();
					
					String url = getLoadedAnnotation ( classPlugin, DatabaseTarget.class, "url" );
					url = url.trim ();

					xtw.writeStartElement ( NAMESPACE, "database" );
					xtw.writeAttribute ( "name", dbname );

					writeXmlElemCDATA ( xtw, "url", url );
					writeXmlElemCDATA ( xtw, "description", description );

					xtw.writeEndElement (); // end database
				} 
				else if ( pluginType.getQualifiedName ().equals ( ONDEXParser.class.getCanonicalName () ) )
					reporter.print ( 
						Diagnostic.Kind.WARNING, 
						classDoc.getQualifiedName () + " is missing annotation tag " + DatabaseTarget.class.getSimpleName () 
				);

				String dburl = getLoadedAnnotation ( classPlugin, DataURL.class, "name" );
				if ( dburl != null )
				{
					dburl = dburl.trim ();
					String description = getLoadedAnnotation ( classPlugin, DataURL.class, "description" );
					description = description.trim ();
					String[] urls = getLoadedAnnotation ( classPlugin, DataURL.class, "urls" );

					xtw.writeStartElement ( NAMESPACE, "data_files" );
					xtw.writeAttribute ( "name", dburl );

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
						classDoc.getQualifiedName () + " is missing annotation tag " + DataURL.class.getSimpleName () 
				);
				
				
				// More details in the metadata section
				//
				xtw.writeStartElement ( NAMESPACE, "ondex-metadata" );
				annotation2Meta ( xtw, classPlugin, DataSourceRequired.class, "data_source" );
				annotation2Meta ( xtw, classPlugin, ConceptClassRequired.class, "concept_class" );
				annotation2Meta ( xtw, classPlugin, AttributeNameRequired.class, "attribute_name" );
				annotation2Meta ( xtw, classPlugin, RelationTypeRequired.class, "relation_type" );
				annotation2Meta ( xtw, classPlugin, EvidenceTypeRequired.class, "evidence_type" );
				xtw.writeEndElement (); // /ondex-metadata
				
				// Description (the Javadoc comment)
				//
				String commentTxt = elemUtils.getDocComment ( classDoc );
				commentTxt = commentTxt == null ? "" : cleanCommentText ( commentTxt );
				writeXmlElem ( xtw, "comment", commentTxt  );

				// Authors
				//
				xtw.writeStartElement ( NAMESPACE, "authors" );
				DocCommentTree docTree = docTreeUtils.getDocCommentTree ( classDoc );
				if ( docTree != null ) // null when the class isn't javadoc-commented
				{
					List<? extends DocTree> docTags = docTree.getBlockTags ();
					for ( DocTree tag: docTags )
					{
						if ( !tag.getKind ().equals ( DocTree.Kind.AUTHOR ) ) continue;
						String authorsTxt = tag.toString ();
						if ( authorsTxt == null ) continue;
						authorsTxt = authorsTxt.replaceFirst ( "^@author\\s", "" );
						String [] authors = authorsTxt.split ( "," );
						for ( String authorName : authors )
							writeXmlElem ( xtw, "author", authorName.trim () );
					}
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
	
	/**
	 * Cleans up a javadoc comment.
	 * 
	 * First removes authors and initial space in each line, then removes tail lines which are empty.
	 */
	private String cleanCommentText ( String commentTxt )
	{
		String[] commentLines = commentTxt.split ( "\n" );
		
		// Filter the author lines, clean the initial space
		//
		for ( int i = 0; i < commentLines.length; i++ )
		{
			String commentLine = commentLines [ i ];
			if ( commentLine.matches ( "^\\s*@author\\s+.*" )) commentLines [ i ] = null;
			else commentLines [ i ] = commentLine.replaceFirst ( "^ ", "" );
		}
		
		// Next, remove the tail lines left null or which were already blank
		int ncut = commentLines.length - 1;
		for ( ; 
						ncut >= 0 && ( commentLines [ ncut ] == null 
												 	 || commentLines [ ncut ].isEmpty () )
					; ncut-- );

		// Finally, rebuild the cleaned string
		StringBuilder newComment = new StringBuilder ();
		for ( int i = 0; i <= ncut; i++ )
			if ( commentLines [ i ] != null )
				newComment.append ( commentLines [ i ] ).append ( "\n" );
		
		return newComment.toString (); 
	}

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

		// Skip the first '-'
		writeXmlElem ( xtw, optName.substring ( 1 ), val );				
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
	
	/**
	 * Writes a metadata description block taking its ids from an annotation
	 * 
	 */
	private <T, A extends Annotation> void annotation2Meta ( 
		XMLStreamWriter xtw, Class<T> target, Class <A> ann, String tag 
	) throws XMLStreamException
	{
		String[] ids = getLoadedAnnotation ( target, ann, "ids" );
		if ( ids != null )
			writeMetaData ( xtw, tag, ids );
	}

	/**
	 * Adds jar files in classDir to the classpath used to fetch plugins.
	 */
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

			String fname = file.getName ();
			
			if ( !fname.endsWith ( ".jar" ) ) continue;

			// This is already in the classpath and reloading it causes problems with J11
			// if ( fname.startsWith ( "workflow-api" ) ) continue;
			
			classRegisterBuilder.add ( file.toURI ().toURL () );
			reporter.print (
				Diagnostic.Kind.NOTE, "Adding to PluginDoclet classpath: '" + file.toURI ().toURL () + "'"
			);
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
	
	/**
	 * <p>This is needed in Java >=9, since the annotations seen via the loaded .jars are considered different
	 * than the ones we see hereby, due to the fact the doclet and those loaded jars use different class loaders.</p>
	 * 
	 * <p>TODO: couldn't find a way to overcome this damn Java modules limit, let's see if some help arrive in 
	 * future (<a href = 'https://stackoverflow.com/questions/60978933'>see here</a>).</p>
	 * 
	 * @param propertyPath the dot-based chain of methods to be invoked from the annotation to get 
	 * a final value, eg, {@code "status.getValue" } and annCls = {@link Status}, will get the {@link Status#status()}
	 * property from the class annotation, and then its {@link StatusType#getValue() string representation} will be the
	 * final result. If such a chain is null at some point, the final result will be null, else the final result must 
	 * be compatibile with AV.
	 */
	@SuppressWarnings ( "unchecked" )
	private static <T, A, AV> AV getLoadedAnnotation ( Class<T> cls, Class<A> annCls, String propertyPath )
	{
		try
		{
			Class<Annotation> trueAnnCls = 
				(Class<Annotation>) Thread.currentThread ().getContextClassLoader ().loadClass ( annCls.getName () );
			Annotation trueAnn = cls.getAnnotation ( trueAnnCls );
			if ( trueAnn == null ) return null;

			Object result = trueAnn;
			for ( String prop: propertyPath.split ( "\\." ) )
			{
				Method propMethod = result.getClass ().getMethod ( prop );
				result = propMethod.invoke ( result );
				if ( result == null ) return null;
			}
			return (AV) result;
		}
		catch ( ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException ex )
		{
			throw new IllegalArgumentException ( 
				format ( "Error while fetching annotation %s.%s() for %s: %s", 
						annCls.getName (), propertyPath, cls, ex.getMessage () ), 
				ex 
			);
		}
	}
}
