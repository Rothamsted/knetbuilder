package net.sourceforge.ondex.rdf.export.mappers;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.jena.rdf.model.Model;

import info.marcobrandizi.rdfutils.commonsrdf.CommonsRDFUtils;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.memory.MemoryONDEXConcept;
import uk.ac.ebi.fg.java2rdf.mapping.RdfMapperFactory;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Nov 2017</dd></dl>
 *
 */
public class RDFXFactory extends RdfMapperFactory
{
	public RDFXFactory ( Model model )
	{
		super ( null );

		JenaRDF rdf = (JenaRDF) CommonsRDFUtils.COMMUTILS.getRDF ();
		this.setGraphModel ( rdf.asGraph ( model ) );

		this.setMapper ( RelationType.class, new RelationTypeMapper () );
		this.setMapper ( ONDEXConcept.class, new ConceptMapper () );
		this.setMapper ( ConceptClass.class, new ConceptClassMapper () );
	}

}
