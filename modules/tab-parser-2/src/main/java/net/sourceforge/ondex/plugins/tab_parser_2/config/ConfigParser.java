package net.sourceforge.ondex.plugins.tab_parser_2.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Resources;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.tools.subgraph.AttributePrototype;
import net.sourceforge.ondex.tools.subgraph.DefConst;
import net.sourceforge.ondex.tools.tab.importer.ConceptPrototype;
import net.sourceforge.ondex.tools.tab.importer.DelimitedReader;
import net.sourceforge.ondex.tools.tab.importer.GraphEntityPrototype;
import net.sourceforge.ondex.tools.tab.importer.PathParser;
import net.sourceforge.ondex.tools.tab.importer.RelationPrototype;

/**
 * The parser of the XML mapping schema. 
 * 
 * This has the purpose of taking an XML mapping file (see project README) and instantiate an ONDEX {@link PathParser}
 * according to the declarations in the XML. 
 * 
 * The entry point for doing this is {@link #parseConfigXml(String, ONDEXGraph, String)} (or its variants).
 * 
 * Note that many methods here are package-visible and not private, in order to allow for tests.
 * 
 * TODO: migrate {@link Optional} to Java8.
 * 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>14 Dec 2016</dd></dl>
 *
 */
public class ConfigParser
{
	private static XPath xpath = null;
	
	private static Logger log = Logger.getLogger ( ConfigParser.class );

	/**
	 * TODO: Not sure parsed XPATH(s) are cached, so let's do it.  
	 */
	private static LoadingCache<String, XPathExpression> xpathCache = 
		CacheBuilder.
		newBuilder ().
		build (
		  new CacheLoader<String, XPathExpression> () {
				@Override
				public XPathExpression load ( String xPathExpr ) throws XPathExpressionException {
					return xpath.compile ( xPathExpr );
		}});
	
	static 
	{
		XPathFactory xpf = XPathFactory.newInstance ();
		xpath = xpf.newXPath ();
	}
		


	
	/**
	 * Wrapper for {@link #parseConfigXml(Reader, ONDEXGraph, String)}.
	 * 
	 */
	public static PathParser parseConfigXml ( String configXmlPath, ONDEXGraph graph, String tabInputPath ) throws Exception
	{
		return parseConfigXml ( new BufferedReader ( new FileReader ( configXmlPath ) ), graph, tabInputPath );
	}
	
	/**
	 * This variant first XML-validates the document coming from the reader (using /tab_parser.xsd) and then
	 * invokes {@link #parseConfigXml(Element, ONDEXGraph, String)} with the resulting DOM. 
	 */
	public static PathParser parseConfigXml ( Reader configXmlReader, ONDEXGraph graph, String tabInputPath ) throws Exception
	{
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance ();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder ();
		Document xmlDoc = docBuilder.parse ( new InputSource ( configXmlReader ) );

		
		// XSD-validation
		SchemaFactory factory = SchemaFactory.newInstance ( XMLConstants.W3C_XML_SCHEMA_NS_URI );
    Schema schema = factory.newSchema ( 
    	new StreamSource ( Resources.getResource ( ConfigParser.class, "/tab_parser.xsd" ).openStream () ) 
    );
    Validator validator = schema.newValidator();
    validator.validate ( new DOMSource ( xmlDoc.getParentNode () ) );
		
		return parseConfigXml ( xmlDoc.getDocumentElement (), graph, tabInputPath );
	}

	
	/**
	 * Parses the XML root element &lt;parser&gt;.
	 */
	public static PathParser parseConfigXml ( Element node, ONDEXGraph graph, String inputPath ) throws Exception
	{
		String delim = parseOptionElem ( node, "delimiter" ).or ( "\t" );
		String startLineStr = parseOptionElem ( node, "start-line" ).or ( "0" );
		int startLine = 0;
		try {
			startLine = Integer.parseInt ( startLineStr );
		}
		catch ( NumberFormatException ex ) {
			throw new IllegalArgumentException ( String.format ( "Invalid value %s for the element <start-line>", startLineStr ));
		}
		
		// TODO: quote/encoding not supported at this time
		
		DelimitedReader reader = new DelimitedReader ( inputPath, delim, startLine );
		PathParser pp = new PathParser ( graph, reader );

		Map<String, ConceptPrototype> concepts = new HashMap<String, ConceptPrototype> ();
		
		// Multiple <concept> elements 
		NodeList conceptNodes = (NodeList) xpath ( node, "concept", XPathConstants.NODESET ).orNull ();
		if ( conceptNodes != null ) for ( int i = 0; i < conceptNodes.getLength (); i++ )
		{
			Pair<String, ConceptPrototype> concept = collectConcept ( (Element) conceptNodes.item ( i ), pp );
			concepts.put ( concept.getKey (), concept.getValue () );
		}

		// Multiple <relation> elements 
		NodeList relationNodes = (NodeList) xpath ( node, "relation", XPathConstants.NODESET ).orNull ();
		if ( relationNodes != null ) for ( int i = 0; i < relationNodes.getLength (); i++ )
			collectRelation ( (Element) relationNodes.item ( i ), pp, concepts ); 
		
		return pp;
	}
	
	/**
	 * Parses a &lt;concept&gt; element and add the resulting {@link ConceptPrototype} to the {@link PathParser} parameter.
	 * Returns the concept and its ID (as defined by the XML id attribute). 
	 */
	static Pair<String, ConceptPrototype> collectConcept ( Element conceptNode, PathParser pp )
	{
		String id = StringUtils.trimToNull ( conceptNode.getAttribute ( "id" ) );
		if ( id == null ) throw new IllegalArgumentException (
			"Concept \"" + StringUtils.abbreviate ( conceptNode.toString (), 15 ) + "\" has no id attribute!" 
		);
			
		ConceptPrototype concept = pp.newConceptPrototype ();

		collectAttrPrototype ( concept, conceptNode, "class", "defCC" );
		invokeOnChildren ( conceptNode, "accession", e -> collectAccession ( concept, e ) );
		invokeOnChildren ( conceptNode, "name", e -> collectName ( concept, e ) );
		collectAttrPrototype ( concept, conceptNode, "data-source", "defDataSource" );
		collectAttrPrototype ( concept, conceptNode, "parser-id", "defPID" );
		invokeOnChildren ( conceptNode, "evidence", e -> collectAttrPrototype ( concept, e, "defEvidence" ) );
		invokeOnChildren ( conceptNode, "attribute", e -> collectONDEXAttribute ( concept, e ) );
				
		return Pair.of ( id, concept );
	}
	
	
	/**
	 * Parses a &lt;relation&gt; element and add the resulting {@link RelationPrototype} to the {@link PathParser} parameter.
	 * The concepts map is built via calls to {@link #collectConcept(Element, PathParser)} and here it's used 
	 * to know source/target concepts in a relationship (referred to by source-ref/target-ref attributes). 
	 */
	static RelationPrototype collectRelation ( 
		Element relationNode, PathParser pp, Map<String, ConceptPrototype> concepts
	)
	{
		String sourceId = StringUtils.trimToNull ( relationNode.getAttribute ( "source-ref" ) );
		if ( sourceId == null ) throw new IllegalArgumentException ( "relation node without source-ref attribute" );
		ConceptPrototype source = concepts.get ( sourceId );
		if ( source == null ) throw new IllegalArgumentException ( "Concept ID '" + sourceId + "' not defined" );

		String targetId = StringUtils.trimToNull ( relationNode.getAttribute ( "target-ref" ) );
		if ( targetId == null ) throw new IllegalArgumentException ( "relation node without target-ref attribute!" );
		ConceptPrototype target = concepts.get ( targetId );
		if ( target == null ) throw new IllegalArgumentException ( "Concept ID '" + targetId + "' not defined" );
		
		RelationPrototype rt = pp.newRelationPrototype ( source, target );
		collectAttrPrototype ( rt, relationNode, "type", "defRT" );
		invokeOnChildren ( relationNode, "evidence", e -> collectAttrPrototype ( rt, e, "defEvidence" ) );
		invokeOnChildren ( relationNode, "attribute", e -> collectONDEXAttribute ( rt, e ) );
				
		return rt;
	}
	
	
	/**
	 * Parses an &lt;accession&gt; element and adds it to the parameter concept. 
	 * Doesn't do anything if no accession element is present under the parentNode.
	 */
	static Optional<AttributePrototype> collectAccession ( ConceptPrototype concept, Element accessionNode )
	{
		String isAmbiguousStr = "true".equals ( accessionNode.getAttribute ( "ambiguous" ) ) ? "true" : "false";
		String dataSource = (String) accessionNode.getAttribute ( "data-source" );
		
		// Child element is going to be extracted again, but that's not very critical
		return collectAttrPrototype ( 
			concept,
			accessionNode,
			"defAccession", 
			new Object[] { dataSource, isAmbiguousStr },
			new Class<?>[] { String.class, String.class } 
		);
	}

	/**
	 * Works similarly to {@link #collectAccession(ConceptPrototype, Element)}. for a name ONDEX attribute.
	 */
	static Optional<AttributePrototype> collectName ( ConceptPrototype concept, Element nameNode )
	{
		String isPreferredStr = "true".equals ( nameNode.getAttribute ( "preferred" ) ) ? "true" : "false";
		
		// Child element is going to be extracted again, but that's not very critical
		return collectAttrPrototype ( 
			concept,
			nameNode,
			"defName", 
			new Object[] { isPreferredStr },
			new Class<?>[] { String.class } 
		);
	}
	
	/** 
	 * Parses an &lt;attribute&gt; node and adds the corresponding {@link AttributePrototype} to the parent parameter
	 * (which must be either {@link ONDEXConcept} or {@link ONDEXRelation}). This expects attrNode to be non-null.
	 */
	static AttributePrototype collectONDEXAttribute ( GraphEntityPrototype parent, Element attrNode )
	{
		// node-specific XML attributes becomes parameters for the ONDEX attribute
		String name = (String) attrNode.getAttribute ( "name" );
		String type = (String) attrNode.getAttribute ( "type" );
		String indexed = "true".equals ( attrNode.getAttribute ( "indexed" ) ) ? "true" : "false";
		
		// Child element is going to be extracted again, but that's not very critical
		return collectAttrPrototype ( 
			parent,
			attrNode,
			".", // we want just this node
			"defAttribute", 
			new Object[] { name, type, indexed },
			new Class<?>[] { String.class, String.class, String.class } 
		).get (); 
	}	
	
	/**
	 * A wrapper to {@link #collectAttrPrototype(GraphEntityPrototype, Element, String, Object[], Class[])}
	 * that takes the child element from its element name (expects only one).
	 * 
	 */
	static Optional<AttributePrototype> collectAttrPrototype ( 
		GraphEntityPrototype parent, Element parentNode, String childElemName, final String methodName,
		Object[] moreArgs,
		Class<?>[] moreArgsClasses
	)
	{
		Element childElem = (Element) xpath ( parentNode, childElemName, XPathConstants.NODE ).orNull ();
		if ( childElem == null ) return Optional.absent ();
		
		return collectAttrPrototype ( parent, childElem, methodName, moreArgs, moreArgsClasses );
	}

	/**
	 * Wrapper without additional args.
	 */
	static Optional<AttributePrototype> collectAttrPrototype ( 
		GraphEntityPrototype parent, Element parentNode, String childElemName, final String methodName
	)
	{
		return collectAttrPrototype ( parent, parentNode, childElemName, methodName, null, null );
	}

	static Optional<AttributePrototype> collectAttrPrototype ( 
		GraphEntityPrototype parent, Element childElem, final String methodName
	)
	{
		return collectAttrPrototype ( parent, childElem, methodName, null, null );
	}

	/**
	 * Extracts an AttributePrototype definition from the XML and, if not null, adds it to a concept.
	 * This is used by {@link #collectConcept(Element, PathParser)}.
	 *  
	 * @param parent the concept/relationType to which the AttributePrototype is added
	 * @param childElem the child XML element (of xml type tab:ColumnOrValue) that defines the attribute 
	 * (e.g., "class") 
	 * @param methodName the method in {@link DefConst} to invoke to define the new AttributePrototype 
	 * (e.g., "defCC")
	 * @param moreArgs, optional additional arguments for the {@link DefConst} method you're calling
	 * @param moreArgsClasses and their classes
	 * 
	 * @return the extracted attribute.
	 */
	static Optional<AttributePrototype> collectAttrPrototype ( 
		GraphEntityPrototype parent, Element childElem, final String 
		methodName, Object[] moreArgs, Class<?>[] moreArgsClasses
	)
	{
		try
		{
			Object colOrVal = parseColumnOrValue ( childElem ).orNull ();
			if ( colOrVal == null ) return Optional.absent ();
						
			Object args[] = null;
			Class<?> argsClasses[] = null;
			
			if ( moreArgs == null || moreArgsClasses == null )
			{
				args = new Object[] { colOrVal };
				argsClasses = new Class[] { colOrVal.getClass () };				
			}
			else 
			{
				if ( moreArgs.length != moreArgsClasses.length ) throw new IllegalArgumentException (
					"Internal error: collectAttrPrototype()'s moreArgs and moreArgsClasses of different lengths"
				);
					
				args = new Object [ moreArgs.length + 1 ];
				argsClasses = new Class<?> [ moreArgsClasses.length + 1 ];
				args [ 0 ] = colOrVal;
				argsClasses [ 0 ] = colOrVal.getClass ();
				System.arraycopy ( moreArgs, 0, args, 1, moreArgs.length );
				System.arraycopy ( moreArgsClasses, 0, argsClasses, 1, moreArgsClasses.length );
			}
			
			AttributePrototype result = (AttributePrototype) MethodUtils.invokeStaticMethod ( 
				DefConst.class, methodName, args, argsClasses
			);		
			
			if ( parent instanceof ConceptPrototype )  ( (ConceptPrototype) parent).addAttributes ( result );
			else if ( parent instanceof RelationPrototype )  ( (RelationPrototype) parent).addAttributes ( result );
			else throw new IllegalArgumentException (
				"Internal error: Java class " + parent.getClass ().getSimpleName () + " not valid here" 
			);
			
			return Optional.of ( result );
		}
		catch ( NoSuchMethodException ex ) {
			throw new IllegalArgumentException ( "Internal error: " + ex.getMessage (), ex );
		}
		catch ( IllegalAccessException ex ) {
			throw new IllegalArgumentException ( "Internal error: " + ex.getMessage (), ex );
		}
		catch ( InvocationTargetException ex ) {
			throw new IllegalArgumentException ( "Internal error: " + ex.getMessage (), ex );
		}		
	}
	
	/**
	 * This invokes {@link #parseColumnOrValue(Element)} after having taken the node specified by elemName
	 * (only one is expected).
	 */
	static Optional<? extends Object> parseColumnOrValue ( Element parentNode, String elemName )
	{
		Element elem = (Element) xpath ( parentNode, elemName, XPathConstants.NODE ).orNull ();
		if ( elem == null ) return Optional.absent ();
		
		return parseColumnOrValue ( elem ); 
	}

	/**
	 * This is used for those elements (e.g. attributes, accession) that can have a mapping to a column or a straight
	 * value. Checks if the child element has a text node, takes this as straight ONDEX value for the entity at issue 
	 * if it exists. Else, sees if a &lt;column&gt; exists within the child node and, if yes, 
	 * invokes {@link #parseColumn(Element)} to know which header is mapped to the elemName element. 
	 * If none of the above happens, raises an error.
	 * 
	 * @return an empty {@link Optional} if no elemName child node exists, a string value if the mapping is based on 
	 * a straight/constant value, the result of {@link #parseColumn(Element)} if the mapping is done via the 
	 * column sub-element.  
	 */
	static Optional<? extends Object> parseColumnOrValue ( Element elem )
	{
		if ( elem == null ) throw new IllegalArgumentException ( "Child element is null" );
		
		Element colElem = (Element) xpath ( elem, "column", XPathConstants.NODE ).orNull ();
		if ( colElem != null ) return Optional.of ( parseColumn ( colElem ) );
				
		String value = StringUtils.trimToNull ( parseOptionElem ( elem, "." ).orNull () );
		if ( value == null ) throw new IllegalArgumentException ( 
			"Wrong syntax for '" + elem.getTagName () + "'"
		);
		return Optional.of ( value );
	}
	
	/**
	 * Parses a &lt;column&gt; node and returns either its index (as an integer) or header XML attribute. TODO: the latter
	 * is not supported yet and for the time being generates an error if the XML defines it.
	 *   
	 */
	static Object parseColumn ( Element colNode )
	{
		try
		{
			String idxStr = StringUtils.trimToNull ( colNode.getAttribute ( "index" ) );
			if ( idxStr == null ) throw new IllegalArgumentException ( 
				"<column> element must have an 'index' attribute, 'header' not supported yet" 
			);
			return Integer.valueOf ( idxStr );
		}
		catch ( NumberFormatException ex ) {
			throw new IllegalArgumentException ( "<column index = '...'> must be integer", ex );
		}
	}
	
	/**
	 * Checks if XML parentNode has a child XML element containing only a text node.
	 * 
	 * @return such text if yes, an empty
	 * {@link Optional} if such XML structure is not present or the text is empty.
	 *   
	 */
	static Optional<String> parseOptionElem ( Node parentNode, String elemName )
	{
		NodeList list = (NodeList) xpath ( parentNode, elemName + "/text()", XPathConstants.NODESET ).orNull ();
		if ( list == null || list.getLength () == 0 ) return Optional.absent ();
		
		return Optional.fromNullable ( StringUtils.trimToNull ( list.item ( 0 ).getTextContent () ) );
	}
	
	/**
	 * Facility to evaluate an XPATH expression against a root node. This also caches the XPATH.
	 * TODO: move to a utility lib.
	 * 
	 */
	@SuppressWarnings ( "unchecked" )
	static <T> Optional<T> xpath ( Node node, String xpathExpr, QName returnType ) 
	{
		try {
			return Optional.fromNullable ( (T) xpathCache.getUnchecked ( xpathExpr ).evaluate ( node, returnType ) );
		}
		catch ( XPathExpressionException ex ) {
			throw new IllegalArgumentException ( "Internal error: " + ex.getMessage (), ex );
		}
	}
	
	
	/**
	 * Facility to take the children of parentElem of type childElemsName and invokes nodeFunc on all of them.
	 */
	static void invokeOnChildren (
		Element parentElem, String childElemsName, Consumer<Element> nodeFunc )
	{
		NodeList attrNodes = (NodeList) xpath ( parentElem, childElemsName, XPathConstants.NODESET ).orNull ();
		if ( attrNodes != null ) for ( int i = 0; i < attrNodes.getLength (); i++ )
			nodeFunc.accept ( (Element) attrNodes.item ( i ) );
	}
}
