package net.sourceforge.ondex.parser2;

import java.util.Optional;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * The default accession mapper takes a couple of mappers to the constituents of an {@link ONDEXConcept} and uses them
 * to create a new accession. This is expected to be the accession mapper that you need in most common cases. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 */
public class DefaultAccessionMapper<S> implements AccessionMapper<S>
{
	TextMapper<S> accessionMapper;
	DataSourceMapper<S> dataSourceMapper;
	Mapper<S, Boolean> ambiguityMapper = new ConstMapper<S, Boolean, Boolean> ( true );
	
	@Override
	public ConceptAccession map ( S src, ONDEXConcept concept, ONDEXGraph graph )
	{
		String accession = this.getAccessionMapper ().map ( src, graph );
		
		DataSource dataSrc = 
			Optional.ofNullable ( this.getDataSourceMapper () )
			.map ( m -> m.map ( src, graph ) )
			.orElseThrow ( () -> new NullPointerException ( this.getClass ().getName () + " needs a data source mapper" ) );

		boolean isAmbiguous = 
			Optional.ofNullable ( this.getAmbiguityMapper () )
			.map ( m -> m.map ( src, graph ) )
			.map ( Boolean.TRUE::equals )
			.orElseThrow ( () -> new NullPointerException ( this.getClass ().getName () + " needs an ambiguity mapper" ) );
		
		return concept.createConceptAccession ( accession, dataSrc, isAmbiguous );
	}

	public TextMapper<S> getAccessionMapper ()
	{
		return accessionMapper;
	}
	
	public void setAccessionMapper ( TextMapper<S> accessionMapper )
	{
		this.accessionMapper = accessionMapper;
	}
	
	public DataSourceMapper<S> getDataSourceMapper ()
	{
		return dataSourceMapper;
	}
	
	public void setDataSourceMapper ( DataSourceMapper<S> dataSourceMapper )
	{
		this.dataSourceMapper = dataSourceMapper;
	}
	
	public Mapper<S, Boolean> getAmbiguityMapper ()
	{
		return ambiguityMapper;
	}
	
	public void setAmbiguityMapper ( Mapper<S, Boolean> ambiguityMapper )
	{
		this.ambiguityMapper = ambiguityMapper;
	}
	
}
