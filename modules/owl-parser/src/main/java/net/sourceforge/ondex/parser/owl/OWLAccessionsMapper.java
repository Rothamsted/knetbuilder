package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.RDFNode;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.utils.ONDEXElemWrapper;
import net.sourceforge.ondex.parser.AccessionsMapper;

/**
 * 
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>28 Apr 2017</dd></dl>
 *
 */
public class OWLAccessionsMapper
	extends RdfPropertyMapper<Stream<ConceptAccession>, ONDEXElemWrapper<ONDEXConcept>>
	implements AccessionsMapper<OntClass>
{
	private boolean isAmbiguous = false;
	
	@Override
	public Stream<ConceptAccession> map ( OntClass ontCls, ONDEXElemWrapper<ONDEXConcept> conceptw )
	{
		// TODO: attach the file?
		DataSource ds = conceptw.getGraphWrapper ().getDataSource ( "owlParser", "The OWL Parser", "" );
		ONDEXConcept concept = conceptw.getElement ();
		
		OntModel model = ontCls.getOntModel ();
		
		Stream<RDFNode> accNodes = StreamSupport.stream (
			Spliterators
			.spliteratorUnknownSize ( 
				ontCls.listPropertyValues ( model.getProperty ( this.getPropertyIri () ) ), Spliterator.IMMUTABLE 
			),
			true
		);
		
		return accNodes
		.map ( nodeVal -> JENAUTILS.literal2Value ( nodeVal ).get () )
		.map ( accession -> concept.createConceptAccession ( accession, ds, isAmbiguous () ) );
	}

	public boolean isAmbiguous ()
	{
		return isAmbiguous;
	}

	public void setAmbiguous ( boolean isAmbiguous )
	{
		this.isAmbiguous = isAmbiguous;
	}
}
