package net.sourceforge.ondex.rdf.export.mappers;

import org.apache.commons.rdf.jena.JenaGraph;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.jena.rdf.model.Model;

import info.marcobrandizi.rdfutils.commonsrdf.CommonsRDFUtils;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.rdf.export.RDFExporter;
import uk.ac.ebi.fg.java2rdf.mapping.BeanRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.RdfMapperFactory;

/**
 * The {@link RdfMapperFactory} for ONDEX/RDF exporting.
 * 
 * Here we define all the {@link BeanRdfMapper} needed for the job. and then {@link RDFExporter} uses 
 * instance of this factory. 
 * 
 * Note that every new chunks generated in 
 * {@link RDFExporter#export(net.sourceforge.ondex.core.ONDEXGraph)} uses a new {@link RDFXFactory}, 
 * so possible state information stored here doesn't last for the whole lifetime of the exporter (the 
 * {@link RdfMapperFactory} tracking of already-visited objects isn't very relevant here, since ONDEX 
 * objects are visited once only).
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

		this.setMapper ( ConceptClass.class, new ConceptClassMapper () );
		this.setMapper ( RelationType.class, new RelationTypeMapper () );
		this.setMapper ( AttributeName.class, new AttributeNameMapper () );
		this.setMapper ( EvidenceType.class, new EvidenceMapper () );
		this.setMapper ( DataSource.class, new DataSourceMapper () );
		this.setMapper ( Unit.class, new UnitMapper () );
		this.setMapper ( ONDEXConcept.class, new ConceptMapper () );
		this.setMapper ( ONDEXRelation.class, new RelationMapper () );
		this.setMapper ( ConceptAccession.class, new AccessionMapper () );
	}

	public Model getJenaModel ()
	{
		return ( (JenaGraph) this.getGraphModel () ).asJenaModel ();
	}
	
}
