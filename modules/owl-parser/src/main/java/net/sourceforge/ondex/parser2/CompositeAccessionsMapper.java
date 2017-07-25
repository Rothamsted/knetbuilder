package net.sourceforge.ondex.parser2;

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
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Jul 2017</dd></dl>
 *
 */
public class CompositeAccessionsMapper<S> implements AccessionsMapper<S> 
{
	private TextsMapper<S> accessionValuesMapper;
	private StreamMapper<S, DataSource> dataSourcesMapper = new ConstDataSourcesMapper<> ();
	private StreamMapper<S, Boolean> ambiguityMapper = new ConstStreamMapper<S, Boolean, Boolean> ();
		
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
			.orElseThrow ( () -> new NullPointerException ( this.getClass ().getName () + " needs a data source mapper" ) )
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
		  	dataSources.get ( i % dataSources.size () ),
		  	areAmbiguos.get ( i % areAmbiguos.size () ) 
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

	public StreamMapper<S, DataSource> getDataSourcesMapper ()
	{
		return dataSourcesMapper;
	}

	public void setDataSourcesMapper ( StreamMapper<S, DataSource> dataSourcesMapper )
	{
		this.dataSourcesMapper = dataSourcesMapper;
	}

	public StreamMapper<S, Boolean> getAmbiguityMapper ()
	{
		return ambiguityMapper;
	}

	public void setAmbiguityMapper ( StreamMapper<S, Boolean> ambiguityMapper )
	{
		this.ambiguityMapper = ambiguityMapper;
	}	
}
