package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;

import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.RDFNode;

import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.utils.ONDEXElemWrapper;
import net.sourceforge.ondex.parser.NamesMapper;

/**
 * 
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>28 Apr 2017</dd></dl>
 *
 */
public class OWLNamesMapper
	extends RdfPropertyMapper<Stream<ConceptName>, ONDEXElemWrapper<ONDEXConcept>>
	implements NamesMapper<OntClass>
{
	@Override
	public Stream<ConceptName> map ( OntClass ontCls, ONDEXElemWrapper<ONDEXConcept> conceptw )
	{
		ONDEXConcept concept = conceptw.getElement ();
		
		OntModel model = ontCls.getOntModel ();
				
		Stream<RDFNode> nameNodes = JENAUTILS.toStream ( 
			ontCls.listPropertyValues ( model.getProperty ( this.getPropertyIri () ) ), true 
		);
				
		return nameNodes
		.map ( nameNode -> JENAUTILS.literal2Value ( nameNode ).get () )
		.map ( name -> concept.createConceptName ( name, false ) );
	}
}
