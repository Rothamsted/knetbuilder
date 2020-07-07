package net.sourceforge.ondex.parser;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * <p>The default accessions mapper extracts accession strings from a source, item, then, for each accession it extracts
 * a data source and an ambiguity flag. To do so, it uses the specific mappers {@link #getAccessionValuesMapper()}, 
 * {@link #getDataSourcesMapper()}, {@link #getAmbiguityMapper()}.</p>
 * 
 * <p>These 3 mappers are supposed to extract values
 * in coherent order (ie, all the first three values from each mapper are combined to create the first ONDEX accession). 
 * However, the data source and/or the ambiguity mappers can
 * return fewer values than the accession returned by the accession values mapper. In that case, these values are reused in 
 * order. This accommodates the case where there is only one constant data source and a constant ambiguity flag for all
 * the accessions.</p>
 * 
 * <p><b>WARNING</b>: All the mappers are supposed to return non-null values</p> 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Jul 2017</dd></dl>
 *
 */
public class DefaultAccessionsMapper<S> implements AccessionsMapper<S> 
{
	private TextsMapper<S> accessionValuesMapper;
	private DataSourcesMapper<S> dataSourcesMapper;
	private StreamMapper<S, Boolean> ambiguityMapper = new ConstStreamMapper<S, Boolean, Boolean> ( false );
		
	@Override
	public Stream<ConceptAccession> map ( S src, ONDEXConcept concept, ONDEXGraph graph )
	{
		List<String> accStrings = 
			Optional.ofNullable ( this.getAccessionValuesMapper () )
			.orElseThrow ( () -> new NullPointerException ( this.getClass ().getName () + " needs a accession values mapper" ) )
			.map ( src, graph )
			.collect ( Collectors.toList () );
		
		List<DataSource> dataSources = 
			Optional.ofNullable ( this.getDataSourcesMapper () )
			.orElseThrow ( () -> new NullPointerException ( this.getClass ().getName () + " needs a data sources mapper" ) )
			.map ( src, graph )
			.collect ( Collectors.toList () );
			
		List<Boolean> areAmbiguos = 
			Optional.ofNullable ( this.getAmbiguityMapper () )
			.orElseThrow ( () -> new NullPointerException ( this.getClass ().getName () + " needs an ambiguity mapper" ) )
			.map ( src, graph )
			.map ( Boolean.TRUE::equals )
			.collect ( Collectors.toList () );
				
		return IntStream.range ( 0, accStrings.size () )
		.mapToObj ( i ->  
		  concept.createConceptAccession ( 
		  	accStrings.get ( i ), 
		  	dataSources.get ( i % dataSources.size () ), // Just reuse them if they're too few
		  	areAmbiguos.get ( i % areAmbiguos.size () )  // same approach
		  )
		);
	}

	
	public TextsMapper<S> getAccessionValuesMapper ()
	{
		return accessionValuesMapper;
	}

	public void setAccessionValuesMapper ( TextsMapper<S> accessionValuesMapper )
	{
		this.accessionValuesMapper = accessionValuesMapper;
	}

	public DataSourcesMapper<S> getDataSourcesMapper ()
	{
		return dataSourcesMapper;
	}

	public void setDataSourcesMapper ( DataSourcesMapper<S> dataSourcesMapper )
	{
		this.dataSourcesMapper = dataSourcesMapper;
	}

	/**
	 * This defaults to a {@link ConstStreamMapper} that returns always false. 
	 */
	public StreamMapper<S, Boolean> getAmbiguityMapper ()
	{
		return ambiguityMapper;
	}

	public void setAmbiguityMapper ( StreamMapper<S, Boolean> ambiguityMapper )
	{
		this.ambiguityMapper = ambiguityMapper;
	}	
}
