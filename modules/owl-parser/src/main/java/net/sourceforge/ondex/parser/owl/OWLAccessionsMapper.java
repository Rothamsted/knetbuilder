package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;

import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.RDFNode;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.utils.CachedGraphWrapper;
import net.sourceforge.ondex.core.utils.DataSourcePrototype;
import net.sourceforge.ondex.core.utils.ONDEXElemWrapper;
import net.sourceforge.ondex.parser.AccessionsMapper;

/**
 * Maps OWL annotation properties corresponding to the the accessions of a class onto ONDEX accessions of 
 * the corresponding concept. 
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
	private DataSourcePrototype dataSourcePrototype = DataSourcePrototype.OWL_PARSER;
	private String dataSourcePrefix = null;
	
	
	@Override
	public Stream<ConceptAccession> map ( OntClass ontCls, ONDEXElemWrapper<ONDEXConcept> conceptw )
	{
		ONDEXConcept concept = conceptw.getElement ();
		ONDEXGraph graph = conceptw.getGraph ();
		CachedGraphWrapper graphw = CachedGraphWrapper.getInstance ( graph );
										
		DataSourcePrototype dsProto = this.getDataSourcePrototype ();
		String dsPrefix = this.getDataSourcePrefix ();
		
		return this.getAccessionStrings ( ontCls )
		.filter ( accStr -> dsPrefix == null || accStr.startsWith ( dsPrefix ) )
		.map ( accStr -> 
		{
			if ( dsPrefix != null ) accStr = accStr.substring ( dsPrefix.length () );
			
			DataSource ds = graphw.getDataSource ( dsProto );
			
			// We don't need to check for duplicates, since it already does it well.  
			return concept.createConceptAccession ( accStr, ds, isAmbiguous () );
		});
	}

	/**
	 * Helper that gets the accession values from the current OWL class. Can be useful for extensions.
	 *  
	 */
	protected Stream<String> getAccessionStrings ( OntClass ontCls )
	{
		OntModel model = ontCls.getOntModel ();
		
		Stream<RDFNode> accNodes = JENAUTILS.toStream ( 
			ontCls.listPropertyValues ( model.getProperty ( this.getPropertyIri () ) ), true 
		);

		return accNodes.map ( accNode -> JENAUTILS.literal2Value ( accNode ).get () );
	}
	
	
	/**
	 * This is usually known from the ontology you're importing
	 */
	public boolean isAmbiguous ()
	{
		return isAmbiguous;
	}

	public void setAmbiguous ( boolean isAmbiguous )
	{
		this.isAmbiguous = isAmbiguous;
	}

	/**
	 * This is added to every accession.
	 */
	public DataSourcePrototype getDataSourcePrototype ()
	{
		return dataSourcePrototype;
	}

	public void setDataSourcePrototype ( DataSourcePrototype dataSourcePrototype )
	{
		this.dataSourcePrototype = dataSourcePrototype;
	}

	/**
	 * Accessions might be in forms like "EZ:12345". Set a prefix like "EZ:" here, to process only accessions of interest
	 * and have their prefix removed from the final ID (the {@link #getDataSourcePrototype() data source} still tracks
	 * the type). 
	 */
	public String getDataSourcePrefix ()
	{
		return dataSourcePrefix;
	}

	public void setDataSourcePrefix ( String dataSourcePrefix )
	{
		this.dataSourcePrefix = dataSourcePrefix;
	}

}
