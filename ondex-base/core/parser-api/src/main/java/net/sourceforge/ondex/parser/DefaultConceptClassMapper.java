package net.sourceforge.ondex.parser;

import java.util.Optional;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.CachedGraphWrapper;

/**
 * The default class mapper invokes single-component mappers, such as {@link #getIdMapper()} or {@link #getFullNameMapper()}, 
 * and use their result to build {@link ConceptClass}es.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 Aug 2017</dd></dl>
 *
 */
public class DefaultConceptClassMapper<S> implements ConceptClassMapper<S>
{	
	private TextMapper<S> idMapper;
	private TextMapper<S> fullNameMapper;
	private TextMapper<S> descriptionMapper;
	private ConceptClassMapper<S> parentMapper;
	
	
	@Override
	public ConceptClass map ( S src, ONDEXGraph graph )
	{
		String id = this.getIdMapper ().map ( src ); 
		String fullName = Optional.ofNullable ( this.getFullNameMapper () ).map ( m -> m.map ( src, graph ) ).orElse ( "" );
		String description = Optional.ofNullable ( this.getDescriptionMapper () ).map ( m -> m.map ( src, graph ) ).orElse ( "" );
		ConceptClass parent = Optional.ofNullable ( this.getParentMapper () ).map ( m -> m.map ( src, graph ) ).orElse ( null );
		
		ConceptClass cc = CachedGraphWrapper.getInstance ( graph ).getConceptClass ( id, fullName, description, parent );
		
		return cc;
	}

	public TextMapper<S> getIdMapper ()
	{
		return idMapper;
	}

	public void setIdMapper ( TextMapper<S> idMapper )
	{
		this.idMapper = idMapper;
	}

	public TextMapper<S> getFullNameMapper ()
	{
		return fullNameMapper;
	}

	public void setFullNameMapper ( TextMapper<S> fullNameMapper )
	{
		this.fullNameMapper = fullNameMapper;
	}

	public TextMapper<S> getDescriptionMapper ()
	{
		return descriptionMapper;
	}

	public void setDescriptionMapper ( TextMapper<S> descriptionMapper )
	{
		this.descriptionMapper = descriptionMapper;
	}
	
	public ConceptClassMapper<S> getParentMapper ()
	{
		return parentMapper;
	}

	public void setParentMapper ( ConceptClassMapper<S> parentMapper )
	{
		this.parentMapper = parentMapper;
	}
	
}
