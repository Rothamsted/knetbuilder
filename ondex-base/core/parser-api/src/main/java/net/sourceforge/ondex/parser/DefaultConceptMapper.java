package net.sourceforge.ondex.parser;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.CachedGraphWrapper;
import net.sourceforge.ondex.core.util.prototypes.EvidenceTypePrototype;

/**
 * The default concept mapper employs a set of mappers to the constituents of an {@link ONDEXConcept} and create a 
 * new concept using the results from these mappers. 
 * 
 * This should be enough in the most common cases.
 * 
 * TODO: add attributes mapper.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 May 2017</dd></dl>
 *
 */
public class DefaultConceptMapper<S> implements ConceptMapper<S> 
{
	private TextMapper<S> idMapper;
	private TextMapper<S> descriptionMapper;
	private TextMapper<S> annotationMapper;

	private TextMapper<S> preferredNameMapper;
	private TextsMapper<S> altNamesMapper;
	private AccessionsMapper<S> accessionsMapper;
	private EvidenceTypeMapper<S> evidenceMapper = new ConstEvidenceTypeMapper<> ( EvidenceTypePrototype.IMPD );
	private DataSourceMapper<S> dataSourceMapper;
	
	@Override
	public ONDEXConcept map ( S src, ConceptClass conceptClass, ONDEXGraph graph )
	{
		CachedGraphWrapper graphw = CachedGraphWrapper.getInstance ( graph );
		
		String id = this.getIdMapper ().map ( src, graph );
		String descr = Optional.ofNullable ( this.getDescriptionMapper () ).map ( m -> m.map ( src, graph ) ).orElse ( "" );
		String ann = Optional.ofNullable ( this.getAnnotationMapper () ).map ( m -> m.map ( src, graph ) ).orElse ( "" );
		EvidenceType evidence = this.getEvidenceMapper ().map ( src, graph );
		DataSource dataSrc = this.getDataSourceMapper ().map ( src, graph );

		ONDEXConcept result = graphw.getConcept ( id, ann, descr, dataSrc, conceptClass, evidence );
		if ( this.isVisited ( src ) ) return result;

		// Preferred name
		Optional
		.ofNullable ( this.getPreferredNameMapper () ) // if available,
		.map ( mapper -> mapper.map ( src, graph ) ) // get a value from it
		.filter ( name -> name.length () > 0 ) // Ondex doesn't accept empty names anyway, sometimes these appear in OBO ontos
		.ifPresent ( prefName -> result.createConceptName ( prefName, true ) ); // and use it to build a new name, if it's non-null

		// Same approach for names mapper, but this has multiple results.
		Optional.ofNullable ( this.getAltNamesMapper () )
		.map ( mapper -> mapper.map ( src, graph ) )
		.ifPresent ( names -> 
		  names
		  .filter ( name -> name.length () > 0 ) // empty names again
		  .forEach ( name -> result.createConceptName ( name, false ) )
		);

		// Accessions
		Optional.ofNullable ( this.getAccessionsMapper () )
		.ifPresent ( mapper -> 
			mapper.map ( src, result, graph )
			.forEach ( acc -> { /* To force stream consumption (count() doesn't work) */} ) 
		);

		return result;
	}


	public TextMapper<S> getIdMapper ()
	{
		return idMapper;
	}


	public void setIdMapper ( TextMapper<S> idMapper )
	{
		this.idMapper = idMapper;
	}


	public TextMapper<S> getDescriptionMapper ()
	{
		return descriptionMapper;
	}

	public TextMapper<S> getAnnotationMapper ()
	{
		return annotationMapper;
	}


	public void setAnnotationMapper ( TextMapper<S> annotationMapper )
	{
		this.annotationMapper = annotationMapper;
	}


	public void setDescriptionMapper ( TextMapper<S> descriptionMapper )
	{
		this.descriptionMapper = descriptionMapper;
	}


	public TextMapper<S> getPreferredNameMapper ()
	{
		return preferredNameMapper;
	}


	public void setPreferredNameMapper ( TextMapper<S> preferredNameMapper )
	{
		this.preferredNameMapper = preferredNameMapper;
	}


	public TextsMapper<S> getAltNamesMapper ()
	{
		return altNamesMapper;
	}


	public void setAltNamesMapper ( TextsMapper<S> altNamesMapper )
	{
		this.altNamesMapper = altNamesMapper;
	}

	
	public AccessionsMapper<S> getAccessionsMapper ()
	{
		return accessionsMapper;
	}

	public void setAccessionsMapper ( AccessionsMapper<S> accessionsMapper )
	{
		this.accessionsMapper = accessionsMapper;
	}


	public EvidenceTypeMapper<S> getEvidenceMapper ()
	{
		return evidenceMapper;
	}


	public void setEvidenceMapper ( EvidenceTypeMapper<S> evidenceMapper )
	{
		this.evidenceMapper = evidenceMapper;
	}


	public DataSourceMapper<S> getDataSourceMapper ()
	{
		return dataSourceMapper;
	}


	public void setDataSourceMapper ( DataSourceMapper<S> dataSourceMapper )
	{
		this.dataSourceMapper = dataSourceMapper;
	}
	
	
}
