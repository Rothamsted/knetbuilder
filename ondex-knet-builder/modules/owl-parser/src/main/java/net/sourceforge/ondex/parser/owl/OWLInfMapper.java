package net.sourceforge.ondex.parser.owl;

import java.util.stream.Stream;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.update.UpdateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * Pre-process owl:Axiom constructs with oboInOwl:is_inferred, so that the corresponding inferred relations are
 * available in the model to be mapped like the others 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>31 May 2017</dd></dl>
 *
 */
public class OWLInfMapper extends OWLMapper
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	@Override
	public Stream<ONDEXConcept> map ( OntModel model, ONDEXGraph graph )
	{
		log.info ( "Preprocessing inference annotations, please wait..." );
		String sparul =
			NamespaceUtils.asSPARQLProlog ()
			+ "\n\nINSERT { ?child rdfs:subClassOf ?parent }\n"
		  + "WHERE { \n"
		  + "  ?ax a owl:Axiom;\n"
		  + "      owl:annotatedProperty rdfs:subClassOf;\n"
		  + "      owl:annotatedSource ?child;\n"
		  + "			 owl:annotatedTarget ?parent;\n"
		  + "      oboInOwl:is_inferred 'true'"
		  + "}";
		UpdateAction.parseExecute ( sparul, model );
		return super.map ( model, graph );
	}

}
