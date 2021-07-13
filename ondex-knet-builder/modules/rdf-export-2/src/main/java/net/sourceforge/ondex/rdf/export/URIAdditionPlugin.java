package net.sourceforge.ondex.rdf.export;

import static net.sourceforge.ondex.core.util.ONDEXGraphUtils.getOrCreateAttributeName;
import static uk.ac.ebi.utils.exceptions.ExceptionUtils.buildEx;
import static uk.ac.ebi.utils.exceptions.ExceptionUtils.throwEx;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.rdf.export.mappers.ConceptMapper;
import net.sourceforge.ondex.rdf.export.mappers.RDFXFactory;
import net.sourceforge.ondex.rdf.export.mappers.RelationMapper;
import net.sourceforge.ondex.transformer.ONDEXTransformer;
import uk.ac.ebi.fg.java2rdf.mapping.rdfgen.RdfUriGenerator;
import uk.ac.ebi.utils.runcontrol.PercentProgressLogger;

/**
 * <h2>The URI Addition Plugin</h2>
 * 
 * Simple transformation plug-in to add URI attributes to all the {@link ONDEXConcept concepts} and {@link ONDEXRelation relations} of
 * and {@link ONDEXGraph}.  
 * 
 * <p>We use this to bridge the Ondex-based objects with the new KnetMiner architecture, based on RDF and graph databases.</p>
 * 
 * <p><b>DO NOT CHANGE THE DEFAULT PARAMETERS</b> when you run this plug-in, unless you know what you're doing. We use this plugin for integrating 
 * KnetMiner with Neo4j and our components rely on this like the 'iri' identifier.</p>  
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>11 Jan 2019</dd></dl>
 *
 */
@Status ( status = StatusType.STABLE )
public class URIAdditionPlugin extends ONDEXTransformer
{
	private String uriAttributeId = "iri";
	private String uriAttributeFullName = "Entity IRI";
	private String uriAttributeDescription = 
		"IRI/URI associated to the concept or relation, so that Ondex tools can be " +
		"bridged with RDF or other graph database tools.";
	
	private String instanceNamespace = NamespaceUtils.ns ( "bkr" );
	private RdfUriGenerator<ONDEXConcept> conceptUriGenerator; 
	private RdfUriGenerator<ONDEXRelation> relationUriGenerator; 
	private boolean uriIndexingEnabled = false;
		
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	{
		// Initialising them this way forces the setup of necessary internal stuff (eg, mapper factory, see the setter)
		this.setConceptUriGenerator ( ConceptMapper.UriGenerator.class.getName () );
		this.setRelationUriGenerator ( RelationMapper.UriGenerator.class.getName () );
	}
	
	
	@Override
	public String getId ()
	{
		return "URIAdditionPlugin";
	}

	@Override
	public String getName ()
	{
		return "URI Addition Plugin";
	}

	@Override
	public String getVersion ()
	{
		return "1.0";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions ()
	{
		return new ArgumentDefinition[]
		{
	    new StringArgumentDefinition ( 
		    "instanceNamespace", 
				"The URI namespace to be used as a prefix for concept/relation instances.", 
				false, // required
				this.instanceNamespace, // default
				false // canBeMultiple
	    ),
	    new StringArgumentDefinition ( 
		    "uriAttributeId", 
				"An ID for the concept/relation attribute type to be used to create URIs.", 
				false, // required
				this.uriAttributeId, // default
				false // canBeMultiple
	    ),
	    new StringArgumentDefinition ( 
		    "uriAttributeFullName", 
				"A name for the concept/relation attribute type to be used to create URIs.", 
				false, // required
				this.uriAttributeFullName, // default
				false // canBeMultiple
	    ),
	    new StringArgumentDefinition ( 
		    "uriAttributeDescription", 
				"A description for the concept/relation attribute type to be used to create URIs.", 
				false, // required
				this.uriAttributeDescription, // default
				false // canBeMultiple
	    ),
	    new StringArgumentDefinition ( 
		    "conceptUriGenerator", 
				"A subclass of the internal class " + RdfUriGenerator.class.getCanonicalName () + ", which is used to generate " +
				"URIs from concepts. Must be the Java FQN of a class available from the classpath. Must accept ONDEXConcept as input (see the code).", 
				false, // required
				this.conceptUriGenerator.getClass ().getName (), // default
				false // canBeMultiple
	    ),
	    new StringArgumentDefinition ( 
		    "relationUriGenerator", 
				"A subclass of the internal class " + RdfUriGenerator.class.getCanonicalName () + ", which is used to generate " +
				"URIs from relations. Must be the Java FQN of a class available from the classpath. Must accept ONDEXRelation as input (see the code).", 
				false, // required
				this.relationUriGenerator.getClass ().getName (), // default
				false // canBeMultiple
	    ),
	    new BooleanArgumentDefinition ( 
	    	"uriIndexingEnabled",
	    	"Should the created URI attribute be indexed? Our Ondex index stores a special 'iri' field anyway for internal " +
	    	"purposes, so this is needed only if you want to do things like enabling URI-based searches in KnetMiner (unlikely).",
	    	false, // required
	    	false // default
	    )
		};
	}

	@Override
	public void start () throws Exception
	{
		BeanUtilsBean butil = BeanUtilsBean2.getInstance ();
		ONDEXPluginArguments args = getArguments ();		
		for ( ArgumentDefinition<?> argDef: this.getArgumentDefinitions () )
		{
			String argName = argDef.getName ();
			
			// Empty strings means take the default. Apparently the wf engine passes empty strings when a parameter 
			// is not set at all (not the defaults!)
			//
			Object aval = Optional.ofNullable ( args.getUniqueValue ( argName ) )
				.map ( v -> v instanceof String ? StringUtils.trimToNull ( (String) v ) : v )
				.orElse ( null );
			
			if ( aval == null ) continue;
			
			// we cannot use butil for overloaded setters (it takes the one with class parameter only)
			if ( "conceptUriGenerator".equals ( argName ) ) this.setConceptUriGenerator ( (String) aval );
			else if ( "relationUriGenerator".equals ( argName ) ) this.setRelationUriGenerator ( (String) aval );
			else butil.setProperty ( this, argName, aval );
		}	
		
		run ();
	}

	public void run ( ONDEXGraph graph )
	{
		this.setONDEXGraph ( graph );
		this.run ();
	}

	public void run ()
	{		
		log.info ( 
			"Adding URIs to {} concept(s) and {} relation(s)", 
			graph.getConcepts ().size (), graph.getRelations ().size () 
		);
		this.processConcepts ();
		this.processRelations ();		
	}
	
	@Override
	public boolean requiresIndexedGraph () {
		return false;
	}

	@Override
	public String[] requiresValidators () {
		return null;
	}
	
	private void processConcepts () {
		this.processEntities ( this.graph.getConcepts (), this.conceptUriGenerator );
	}
	
	private void processRelations () {
		this.processEntities ( this.graph.getRelations (), this.relationUriGenerator );
	}
	
	
	private <E extends ONDEXEntity> void processEntities ( Set<E> odxEntities, RdfUriGenerator<E> uriGenerator )
	{		
		String typeStr = odxEntities.iterator ().next () instanceof ONDEXConcept ? "concept" : "relation";
		log.info ( "Start processing {}s", typeStr );
		
		PercentProgressLogger progressLogger = new PercentProgressLogger (
		  "{}% of " + typeStr + "s done", odxEntities.size ()		
		);
		
		AttributeName uriAttributeType = getOrCreateAttributeName ( 
			graph, this.uriAttributeId, this.uriAttributeFullName, this.uriAttributeDescription, String.class 
		);
		
		// This is how the URI generator receives the instance namespace
		Map<String, Object> nsParam = new HashMap<> ();
		nsParam.put ( "instanceNamespace", this.instanceNamespace );
		
		odxEntities
		.stream ()
		.sequential () // The parallel stream gives issues with the attr value setter.
		.forEach ( entity -> 
		{
			Attribute uriAttr = entity.getAttribute ( uriAttributeType );
			// Default URI generators return the existing attribute, so let's reset
			if ( uriAttr != null ) entity.deleteAttribute ( uriAttributeType );
			
			String uri = uriGenerator.getUri ( entity, nsParam );
			entity.createAttribute ( uriAttributeType, uri, this.uriIndexingEnabled );
			progressLogger.updateWithIncrement ();
		});
	}

	/**
	 * See {@link #getArgumentDefinitions()} for details on the plugin parameters.
	 */
	public String getUriAttributeId ()
	{
		return uriAttributeId;
	}

	public void setUriAttributeId ( String uriAttributeId )
	{
		this.uriAttributeId = uriAttributeId;
	}

	/**
	 * See {@link #getArgumentDefinitions()} for details on the plugin parameters.
	 */	
	public String getUriAttributeFullName ()
	{
		return uriAttributeFullName;
	}

	public void setUriAttributeFullName ( String uriAttributeFullName )
	{
		this.uriAttributeFullName = uriAttributeFullName;
	}

	/**
	 * See {@link #getArgumentDefinitions()} for details on the plugin parameters.
	 */	
	public String getUriAttributeDescription ()
	{
		return uriAttributeDescription;
	}

	public void setUriAttributeDescription ( String uriAttributeDescription )
	{
		this.uriAttributeDescription = uriAttributeDescription;
	}

	/**
	 * See {@link #getArgumentDefinitions()} for details on the plugin parameters.
	 */	
	public String getInstanceNamespace ()
	{
		return instanceNamespace;
	}

	public void setInstanceNamespace ( String instanceNamespace )
	{
		this.instanceNamespace = instanceNamespace;
	}

	/**
	 * See {@link #getArgumentDefinitions()} for details on the plugin parameters.
	 */	
	public RdfUriGenerator<ONDEXConcept> getConceptUriGenerator ()
	{
		return conceptUriGenerator;
	}

	public void setConceptUriGenerator ( RdfUriGenerator<ONDEXConcept> conceptUriGenerator )
	{
		this.conceptUriGenerator = conceptUriGenerator;
	}

	public void setConceptUriGenerator ( String fqn ) {
		this.setConceptUriGenerator ( this.getUriGenerator ( fqn ) );
	}
		
	/**
	 * See {@link #getArgumentDefinitions()} for details on the plugin parameters.
	 */	
	public RdfUriGenerator<ONDEXRelation> getRelationUriGenerator ()
	{
		return relationUriGenerator;
	}

	public void setRelationUriGenerator ( RdfUriGenerator<ONDEXRelation> relationUriGenerator )
	{
		this.relationUriGenerator = relationUriGenerator;
	}

	public void setRelationUriGenerator ( String fqn ) {
		this.setRelationUriGenerator ( this.getUriGenerator ( fqn ) );
	}

	/**
	 * This is used by the public setters, to setup an {@link RdfUriGenerator} from the FQN of its Java class.
	 * 
	 * We create the generator dynamically and also initialise it with necessary stuff, eg, 
	 * {@link RdfUriGenerator#setMapperFactory(uk.ac.ebi.fg.java2rdf.mapping.RdfMapperFactory)}.
	 * 
	 */
	@SuppressWarnings ( { "null", "unchecked", "static-access" } )
	private <E extends ONDEXEntity> RdfUriGenerator<E> getUriGenerator ( String fqn )
	{
		Class<?> clazz = null;
				
		try {
			clazz = this.getClass ().forName ( fqn );
		}
		catch ( ClassNotFoundException ex ) 
		{
			throwEx ( 
				IllegalArgumentException.class, ex, "Error while trying to create URI generator '%s': %s", fqn, ex.getMessage () 
			);
		}
		
		if ( !RdfUriGenerator.class.isAssignableFrom ( clazz ) ) 
		throwEx ( 
			IllegalArgumentException.class,
			"Class %s is not a subclass of %s",
			clazz.getSimpleName (),
			RdfUriGenerator.class.getSimpleName () 
		);
		
		RdfUriGenerator<E> result;
		try {
			result = (RdfUriGenerator<E>) clazz.getConstructor ().newInstance ();
		}
		catch ( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException ex )
		{
			throw buildEx ( 
				IllegalArgumentException.class, ex, "Error while trying to create URI generator '%s': %s", fqn, ex.getMessage () 
			);
		}
		
		/* This is used internally by our URI generators. The model is used by this factory, but here it will be left empty */
		result.setMapperFactory ( new RDFXFactory ( ModelFactory.createDefaultModel () ) );
		
		return result;
	}
	
	/**
	 * See {@link #getArgumentDefinitions()} for details on the plugin parameters.
	 */	
	public boolean isUriIndexingEnabled ()
	{
		return uriIndexingEnabled;
	}

	public void setUriIndexingEnabled ( boolean uriIndexingEnabled )
	{
		this.uriIndexingEnabled = uriIndexingEnabled;
	}
	
}
