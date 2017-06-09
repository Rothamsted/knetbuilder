package net.sourceforge.ondex.parser2;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.collections15.iterators.IteratorChain;
import org.apache.jena.ontology.OntClass;

import com.sun.xml.internal.xsom.impl.scd.Iterators;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.utils.ONDEXElemWrapper;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 May 2017</dd></dl>
 *
 */
public abstract class TreeParserScanner<S> implements ParserScanner<S, Stream<ONDEXElemWrapper<ONDEXConcept>>>
{
	private ConceptMapper<S> conceptMapper;
	private RelationMapper relationMapper;
	private boolean isMappingDirectionUp = true; 
	
	
	@Override
	public Stream<ONDEXElemWrapper<ONDEXConcept>> scan ( S source )
	{
		final ConceptMapper<S> conceptMapper = this.getConceptMapper ();
		
		Iterator<ParserSourceItem<S>> result = Iterators.empty ();
		for ( Iterator<ParserSourceItem<S>> rootsItr = this.scanRoots ( source ); rootsItr.hasNext (); )
			result = new IteratorChain<ParserSourceItem<S>> ( result, scanTree ( rootsItr.next () ) );
		
		return StreamSupport
		.stream ( Spliterators.spliteratorUnknownSize ( result, Spliterator.IMMUTABLE ), false )
		.map ( srcItem -> conceptMapper.map ( srcItem ) );		
	}

	protected Iterator<ParserSourceItem<S>> scanTree ( ParserSourceItem<S> rootSourceItem )
	{
		final ConceptMapper<S> conceptMapper = this.getConceptMapper ();
		ONDEXElemWrapper<ONDEXConcept> wrootConcept = conceptMapper.map ( rootSourceItem );

		Iterator<ParserSourceItem<S>> result = Iterators.empty ();
		Iterable<ParserSourceItem<S>> itr = () -> scanChildren ( rootSourceItem );

		for ( ParserSourceItem<S> child: itr )
		{
			ONDEXElemWrapper<ONDEXConcept> wchildConcept = conceptMapper.map ( child );
			if ( isMappingDirectionUp )
				relationMapper.map ( wchildConcept, wrootConcept );
			else
				relationMapper.map ( wrootConcept, wchildConcept );
			
			result = new IteratorChain<ParserSourceItem<S>> ( result, scanTree ( child ) );
		}
		
		return result;
	}
	
	protected abstract Iterator<ParserSourceItem<S>> scanRoots ( S source );
	
	protected abstract Iterator<ParserSourceItem<S>> scanChildren ( ParserSourceItem<S> sourceItem );

	
	public ConceptMapper<S> getConceptMapper ()
	{
		return conceptMapper;
	}

	public void setConceptMapper ( ConceptMapper<S> conceptMapper )
	{
		this.conceptMapper = conceptMapper;
	}

	public RelationMapper getRelationMapper ()
	{
		return relationMapper;
	}

	public void setRelationMapper ( RelationMapper relationMapper )
	{
		this.relationMapper = relationMapper;
	}

	public boolean isMappingDirectionUp ()
	{
		return isMappingDirectionUp;
	}

	public void setMappingDirectionUp ( boolean isMappingDirectionUp )
	{
		this.isMappingDirectionUp = isMappingDirectionUp;
	}
	
	
}
