package net.sourceforge.ondex.parser.owl;

import java.util.Objects;
import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.ConstDataSourcesMapper;
import net.sourceforge.ondex.parser.DefaultAccessionsMapper;
import net.sourceforge.ondex.parser.TextsMapper;

/**
 * <p>This implements a {@link DefaultAccessionsMapper} considering the way accessions are represented in OBO/OWL
 * ontologies. For instance, GO accessions are represented through the property 'oboInOwl:id' associated to the 
 * GO class and each value given for this property has the 'GO:' prefix (e.g., 'GO:00002835'). You can map 
 * 'oboInOwl:id' through this mapper, using {@link #setPropertyIri(String) the property IRI setter} and telling 
 * about the 'GO:' prefix via {@link #setDataSourcePrefix(String)}. 
 * 
 * The ONDEX data source is a different entity and you can set it 
 * via {@link #setDataSourcesMapper(net.sourceforge.ondex.parser.DataSourcesMapper)} (possibly using a 
 * {@link ConstDataSourcesMapper}).</p>
 * 
 * <p>If the data source prefix is specified, this will be removed from the values associated to the ontology class
 * via the property URI. If {@link #getAddedPrefix()} is specified, this will be added to the final accession 
 * value. That means you can use the two to replace prefixes, or to keep the original prefix (by setting both
 * {@link #getDataSourcePrefix()} and {@link #getAddedPrefix()} to the same prefix value).</p>
 * 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Jul 2017</dd></dl>
 *
 */
public class OBOWLAccessionsMapper extends DefaultAccessionsMapper<OntClass> implements RdfPropertyConfigurator
{
	/**
	 * Implements the behaviour described above to extract accession values and consider value prefixes.
	 *
	 * @author brandizi
	 * <dl><dt>Date:</dt><dd>23 Aug 2017</dd></dl>
	 *
	 */
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
			
			Stream<String> result = this.getTextsMapper ()
				.map ( ontCls, graph )
				.filter ( Objects::nonNull );
			
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
		 * This is removed from the accession value (see above).
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

		public OWLTextsMapper getTextsMapper ()
		{
			return textsMapper;
		}

		public void setTextsMapper ( OWLTextsMapper textsMapper )
		{
			this.textsMapper = textsMapper;
		}
	}
	
	/**
	 * This sets up {@link Utils#OWL_PARSER_DATA_SOURCE} as default data source (using {@link ConstDataSourcesMapper}).
	 */
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
	 */
	public String getAddedPrefix ()
	{
		return  ( (AccessionValuesMapper) this.getAccessionValuesMapper () ).getAddedPrefix ();
	}

	public void setAddedPrefix ( String addedPrefix )
	{
		 ( (AccessionValuesMapper) this.getAccessionValuesMapper () ).setAddedPrefix ( addedPrefix );
	}

	/**
	 * The property that associates accession strings to OWL classes.
	 */
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
