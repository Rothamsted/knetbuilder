package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;

import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.RDFNode;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.utils.CachedGraphWrapper;
import net.sourceforge.ondex.core.utils.DataSourcePrototype;
import net.sourceforge.ondex.core.utils.ONDEXElemWrapper;
import net.sourceforge.ondex.parser.AccessionsMapper;
import net.sourceforge.ondex.parser2.AbstractAccessionsMapper;
import net.sourceforge.ondex.parser2.CompositeAccessionsMapper;
import net.sourceforge.ondex.parser2.ConstDataSourcesMapper;
import net.sourceforge.ondex.parser2.TextsMapper;

/**
 * 
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Jul 2017</dd></dl>
 *
 */
public class OBOWLAccessionsMapper extends CompositeAccessionsMapper<OntClass> implements RdfPropertyConfigurator
{
	public static class AccessionValuesMapper implements RdfPropertyConfigurator, TextsMapper<OntClass>
	{
		private String dataSourcePrefix = null;
		private String addedPrefix = null;
		
		private OWLTextsMapper textsMapper = new OWLTextsMapper ();
		
		@Override
		public Stream<String> map ( OntClass ontCls, ONDEXGraph graph )
		{
			String dsPrefix = this.getDataSourcePrefix ();
			String addedPrefix = this.getAddedPrefix ();
			
			Stream<String> result = this.textsMapper.map ( ontCls, graph );
			
			if ( dsPrefix != null )
			{
				result = result.filter ( accStr -> accStr.startsWith ( dsPrefix ) );
				// if added prefix is the same, then you simply want to keep the one used for filtering
				if ( !dsPrefix.equals ( addedPrefix ) )
				{
					// else, strip the original prefix away and add a non-null addedPrefix
					result = addedPrefix == null 
						? result.map ( accStr -> accStr.substring ( dsPrefix.length () ) )
						: result.map ( accStr -> addedPrefix + accStr.substring ( dsPrefix.length () ) );
				}
			}
			else if ( addedPrefix != null )
				// No prefix to remove, non-null addedPrefix to add
				result = result.map ( accStr -> addedPrefix + accStr );
		
			return result;
		}

		/**
		 * Accessions might be in forms like "EZ:12345". Set a prefix like "EZ:" here, to process only accessions of interest
		 * and have their prefix removed from the final ID (the {@link OBOWLAccessionsMapper#getDataSourcesMapper() data source} 
		 * still tracks the type). 
		 */		
		public String getDataSourcePrefix () {
			return dataSourcePrefix;
		}

		public void setDataSourcePrefix ( String dataSourcePrefix ) {
			this.dataSourcePrefix = dataSourcePrefix;
		}

		/**
		 * This is added to the final ONDEX result, after that the {@link #getDataSourcePrefix() possible original prefix} 
		 */		
		public String getAddedPrefix () {
			return addedPrefix;
		}

		public void setAddedPrefix ( String addedPrefix ) {
			this.addedPrefix = addedPrefix;
		}

		public String getPropertyIri ()
		{
			return this.textsMapper.getPropertyIri ();
		}

		public void setPropertyIri ( String propertyIri )
		{
			textsMapper.setPropertyIri ( propertyIri );
		}
		
		
	}
	
	
	public OBOWLAccessionsMapper ()
	{
		super ();
		this.setDataSourcesMapper ( new ConstDataSourcesMapper<> ( Utils.OWL_PARSER_DATA_SOURCE ) );
		this.setAccessionValuesMapper ( new AccessionValuesMapper () );
	}
	
	@Override
	public void setAccessionValuesMapper ( TextsMapper<OntClass> accessionValuesMapper ) {
		throw new IllegalArgumentException ( this.getClass ().getName () + " requires a AccessionValuesMapper as accessions mapper" );
	}

	protected void setAccessionValuesMapper ( AccessionValuesMapper accessionValuesMapper )
	{
		super.setAccessionValuesMapper ( accessionValuesMapper );
	}

	/**
	 * Wraps {@link AccessionValuesMapper#getDataSourcePrefix()}
	 */
	public String getDataSourcePrefix ()
	{
		return ( (AccessionValuesMapper) this.getAccessionValuesMapper () ).getDataSourcePrefix ();
	}

	public void setDataSourcePrefix ( String dataSourcePrefix )
	{
		 ( (AccessionValuesMapper) this.getAccessionValuesMapper () ).setDataSourcePrefix ( dataSourcePrefix );
	}

	/**
	 * Wraps {@link AccessionValuesMapper#getAddedPrefix()}
	 * @return
	 */
	public String getAddedPrefix ()
	{
		return  ( (AccessionValuesMapper) this.getAccessionValuesMapper () ).getAddedPrefix ();
	}

	public void setAddedPrefix ( String addedPrefix )
	{
		 ( (AccessionValuesMapper) this.getAccessionValuesMapper () ).setAddedPrefix ( addedPrefix );
	}

	@Override
	public String getPropertyIri ()
	{
		return ( (AccessionValuesMapper) this.getAccessionValuesMapper () ).getPropertyIri ();
	}

	@Override
	public void setPropertyIri ( String propertyIri )
	{
		( (AccessionValuesMapper) this.getAccessionValuesMapper () ).setPropertyIri ( propertyIri );		
	}	
}
