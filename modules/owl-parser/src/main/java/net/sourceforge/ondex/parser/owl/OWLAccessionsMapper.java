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
	private DataSourcePrototype dataSourcePrototype = DataSourcePrototype.OWL_PARSER;
	private String dataSourcePrefix = null;
	
	
	@Override
	public Stream<ConceptAccession> map ( OntClass ontCls, ONDEXElemWrapper<ONDEXConcept> conceptw )
	{
		ONDEXConcept concept = conceptw.getElement ();
		ONDEXGraph graph = conceptw.getGraph ();
		CachedGraphWrapper graphw = CachedGraphWrapper.getInstance ( graph );
		
		OntModel model = ontCls.getOntModel ();
				
		Stream<RDFNode> accNodes = JENAUTILS.toStream ( 
			ontCls.listPropertyValues ( model.getProperty ( this.getPropertyIri () ) ), true 
		);
				
		DataSourcePrototype dsProto = this.getDataSourcePrototype ();
		String dsPrefix = this.getDataSourcePrefix ();
		
		return accNodes
		.map ( accNode -> JENAUTILS.literal2Value ( accNode ).get () )
		.filter ( accStr -> dsPrefix == null || accStr.startsWith ( dsPrefix ) )
		.map ( accStr -> 
		{
			if ( dsPrefix != null ) accStr = accStr.substring ( dsPrefix.length () );
			
			DataSource ds = graphw.getDataSource ( dsProto );
			return concept.createConceptAccession ( accStr, ds, isAmbiguous () );
		});
	}

	public boolean isAmbiguous ()
	{
		return isAmbiguous;
	}

	public void setAmbiguous ( boolean isAmbiguous )
	{
		this.isAmbiguous = isAmbiguous;
	}

	public DataSourcePrototype getDataSourcePrototype ()
	{
		return dataSourcePrototype;
	}

	public void setDataSourcePrototype ( DataSourcePrototype dataSourcePrototype )
	{
		this.dataSourcePrototype = dataSourcePrototype;
	}

	public String getDataSourcePrefix ()
	{
		return dataSourcePrefix;
	}

	public void setDataSourcePrefix ( String dataSourcePrefix )
	{
		this.dataSourcePrefix = dataSourcePrefix;
	}

}
