package net.sourceforge.ondex.parser.owl;

import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.update.UpdateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * Pre-processes the original OWL dataset to add needed things.
 * 
 * See {@link #isWithExplicitOwlClasses()}, {@link #isWithOboIsInferred()}.
 * 
 * <b>Warning</b>: with recent Jena versions, you will likely want to use this with
 * {@link #isWithExplicitOwlClasses()}, and not {@link OWLMapper}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>31 May 2017</dd></dl>
 *
 */
public class OWLInfMapper extends OWLMapper
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	private boolean withExplicitOwlClasses = true,
		withOboIsInferred = false;
	
	@Override
	public Stream<ONDEXConcept> map ( OntModel model, ONDEXGraph graph )
	{
		this.doOboIsInferred ( model );
		this.doExplicitOwlClasses ( model );
		return super.map ( model, graph );
	}

	/**
	 * Called by {@link #map(OntModel, ONDEXGraph)}, check {@link #isWithOboIsInferred()}
	 * and possibly tweaks the model correspondingly, via SPARUL.
	 */
	private void doOboIsInferred ( OntModel model )
	{
		if ( !this.isWithOboIsInferred () ) return;

		log.info ( "Preprocessing inference annotations, please wait..." );
		String sparul =
			NamespaceUtils.asSPARQLProlog () +
			"""
					
			INSERT { ?child rdfs:subClassOf ?parent }
			WHERE { 
			  ?ax a owl:Axiom;
			      owl:annotatedProperty rdfs:subClassOf;
			      owl:annotatedSource ?child;
			      owl:annotatedTarget ?parent;
			      oboInOwl:is_inferred 'true'
			}""";
		UpdateAction.parseExecute ( sparul, model );
		log.info ( "Inference annotation processed." );
	}	
	
	/**
	 * Called by {@link #map(OntModel, ONDEXGraph)}, check {@link #isWithExplicitOwlClasses()}
	 * and possibly tweaks the model correspondingly, via SPARUL.
	 * 
	 * TODO: write specific tests for the OWL constructs.
	 */
	private void doExplicitOwlClasses ( OntModel model )
	{
		if ( !this.isWithExplicitOwlClasses () ) return;

		log.info ( "Adding explicit rdf:type owl:Class statements, please wait..." );
		
		Stream.of (
			Pair.of (
				"? subClassOf ?parent -> ?parent",
				"""
				INSERT { ?parent a owl:Class }
	      WHERE {
	        ?child rdfs:subClassOf ?parent.
	        FILTER NOT EXISTS { ?parent a owl:Class }
	      }				
				"""
			),
			
			Pair.of (
				"?child subClassOf ? -> ?child",
				"""
				INSERT { ?child a owl:Class }
	      WHERE {
	        ?child rdfs:subClassOf ?parent.
	        FILTER NOT EXISTS { ?child a owl:Class }
	      }				
				"""
			),
			
			Pair.of (
				"(some|all)ValuesFrom targets",
				"""
				INSERT { ?target a owl:Class }
        WHERE {
          ?r a owl:Restriction;
            # owl:onProperty ?p
				    owl:someValuesFrom|owl:allValuesFrom ?target

					FILTER ( isIRI ( ?target ) )
					FILTER ( !STRSTARTS ( STR ( ?target ), STR ( xsd: ) ) )

					FILTER NOT EXISTS { ?target a owl:Class }
        }
				"""
			),
			
			Pair.of (
				"intersectionOf targets",
				"""
				INSERT { ?target a owl:Class }
        WHERE {
          ?r owl:intersectionOf ?classes.
          ?classes rdf:rest*/rdf:first ?target 

					FILTER ( isIRI ( ?target ) )
					FILTER ( !STRSTARTS ( STR ( ?target ), STR ( xsd: ) ) )
					
					FILTER NOT EXISTS { ?target a owl:Class }
        }
				"""
			)
			
		)
		.map ( descr -> { 
			var title = descr.getLeft ();
			var sparul = descr.getRight ();
			return Pair.of ( title, NamespaceUtils.asSPARQLProlog () + "\n\n" + sparul ); 
		})
		.forEach ( descr -> {  
			var title = descr.getLeft ();
			var sparul = descr.getRight ();
			log.info ( "Processing: {}", title );
			UpdateAction.parseExecute ( sparul, model );
		});		
		log.info ( "owl:Class statements added." );
	}
	
	/**
	 * Looks for statements like {@code ?child rdfs:subClassOf ?parent} and adds 
	 * {@code ?child rdf:type owl:Class} if it doesn't already exists. This tweak is made
	 * necessary by recent releases of Jena, which, when used with {@link OntModelSpec#OWL_MEM},
	 * doesn't accept to create an {@link OntClass} from a URI, unless the URI is explicitly 
	 * qualified as an OWL class.
	 * 
	 * This flag is true by default, since you will need it in most cases.
	 */
	public boolean isWithExplicitOwlClasses ()
	{
		return withExplicitOwlClasses;
	}

	public void setWithExplicitOwlClasses ( boolean withExplicitOwlClasses )
	{
		this.withExplicitOwlClasses = withExplicitOwlClasses;
	}

	
	/**
	 * Pre-processes owl:Axiom constructs with oboInOwl:is_inferred, so that the corresponding inferred relations are
	 * available in the model to be mapped like the others.
	 * 
	 * Namely, it looks for this OBO-in-OWL specific pattern:
	 *
	 * <pre>
	 * ?ax a owl:Axiom;
	 *   owl:annotatedProperty rdfs:subClassOf;
	 *	 owl:annotatedSource ?child;
	 *	 owl:annotatedTarget ?parent;
	 *	 oboInOwl:is_inferred 'true'
	 * </pre>
	 * 
	 * and adds {@code ?child rdfs:subClassOf ?parent}.
	 * 
	 * This flag is false by default.
	 */
	public boolean isWithOboIsInferred ()
	{
		return withOboIsInferred;
	}

	public void setWithOboIsInferred ( boolean withOboIsInferred )
	{
		this.withOboIsInferred = withOboIsInferred;
	}

}
