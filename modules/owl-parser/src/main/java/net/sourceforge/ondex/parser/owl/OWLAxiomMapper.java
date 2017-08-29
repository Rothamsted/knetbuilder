package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

/**
 * Scaffolding to map owl:Axiom constructs, i.e., something like: 
 * 
 * <pre>
 *   []
 *     a owl:Axiom ;
 *     owl:annotatedSource obo:TO_0000523>
 *     owl:annotatedProperty obo:IAO_0000115;
 *     owl:annotatedTarget "Stomatal resistance ..."; 
 *		 obo:hasDbXref "Wikipedia:Stomatal_conductance".
 *  </pre>
 *  
 *  Here {@link #getPropertyIri()} is used to match owl:annotatedProperty, while 
 *  {@link #getMappedPropertyIri()} is used to establish the value to return as result of the mapping (if null, 
 *  defaults to owl:annotatedTarget).
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Aug 2017</dd></dl>
 *
 */
public class OWLAxiomMapper extends OWLTextsMapper
{
	public static class TargetScanner extends RdfPropertyScanner
	{
		private String mappedPropertyIri;
		
		@Override
		public Stream<RDFNode> scan ( OntClass ontCls )
		{
			OntModel model = ontCls.getOntModel ();
			// any ? annSource fromCls
			ResIterator axiomsItr = model.listSubjectsWithProperty ( 
				model.getProperty ( iri ( "owl:annotatedSource" ) ), ontCls 
			);
			
			Stream<Resource> axioms = JENAUTILS.toStream ( axiomsItr )
			// annProperty must match
			.filter ( ax -> model.listStatements ( 
					ax, 
					model.getProperty ( iri ( "owl:annotatedProperty" ) ), 
					model.getProperty ( this.getPropertyIri () ) 
					).hasNext ()  
			)
			// and, just in case, let's check it's an axiom
			.filter ( ax -> model.listStatements ( ax, RDF.type, model.getProperty ( iri ( "owl:Axiom" ) ) ).hasNext () );
			
			// good, now take the mapped property value and we're done
			String mappedPropIri = Optional.ofNullable ( this.getMappedPropertyIri () ).orElse ( iri ( "owl:annotatedTarget" ) );
			
			return axioms
			.flatMap ( ax -> JENAUTILS.toStream ( ax.listProperties ( model.getProperty ( mappedPropIri ) ) ) )
			.map ( stmt -> stmt.getObject () );			
		}

		public String getMappedPropertyIri ()
		{
			return mappedPropertyIri;
		}

		public void setMappedPropertyIri ( String mappedPropertyIri )
		{
			this.mappedPropertyIri = mappedPropertyIri;
		}
	}

	public OWLAxiomMapper ()
	{
		super ( new TargetScanner (), new LiteralMapper () );
	}
	
	public String getMappedPropertyIri ()
	{
		return ( (TargetScanner) this.getScanner () ).getMappedPropertyIri ();
	}

	public void setMappedPropertyIri ( String mappedPropertyIri )
	{
		( (TargetScanner) this.getScanner () ).setMappedPropertyIri ( mappedPropertyIri );
	}
	
}
