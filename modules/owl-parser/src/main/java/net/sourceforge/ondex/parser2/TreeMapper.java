package net.sourceforge.ondex.parser2;

import java.util.stream.Stream;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 May 2017</dd></dl>
 *
 */
public abstract class TreeMapper<S, SI> implements StreamMapper<S, ONDEXConcept>
{
	private ConceptClassMapper<SI> conceptClassMapper;
	private ConceptMapper<SI> conceptMapper;
	private RelationMapper relationMapper;
	private boolean isMappingDirectionUp = true;
	private boolean doMapRootsToConcepts = true;
	
	private Scanner<S, SI> rootsScanner;
	private Scanner<SI, SI> childrenScanner;
	
	
	@Override
	public Stream<ONDEXConcept> map ( S source, ONDEXGraph graph )
	{
		return this.getRootsScanner ()
		.scan ( source )
		.map ( root -> this.scanTree ( root, graph ) );

		// More TODO: doMapRootsToConcepts flag
		// TODO: document that we're returning the root concepts
	}

	protected ONDEXConcept scanTree ( SI rootSourceItem, ONDEXGraph graph )
	{
		ConceptClassMapper<SI> ccmapper = this.getConceptClassMapper ();
		final ConceptMapper<SI> conceptMapper = this.getConceptMapper ();
		ONDEXConcept rootConcept = conceptMapper.map ( rootSourceItem, ccmapper, graph );
				
		this.getChildrenScanner ()
		.scan ( rootSourceItem )
		.forEach ( child -> 
		{
			ONDEXConcept childConcept = conceptMapper.map ( child, ccmapper, graph );
			if ( isMappingDirectionUp )
				relationMapper.map ( childConcept, rootConcept, graph );
			else
				relationMapper.map ( rootConcept, childConcept );
		});
		
		return rootConcept;
	}
	
	
	public ConceptClassMapper<SI> getConceptClassMapper ()
	{
		return conceptClassMapper;
	}

	public void setConceptClassMapper ( ConceptClassMapper<SI> conceptClassMapper )
	{
		this.conceptClassMapper = conceptClassMapper;
	}

	public ConceptMapper<SI> getConceptMapper ()
	{
		return conceptMapper;
	}

	public void setConceptMapper ( ConceptMapper<SI> conceptMapper )
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

	public boolean isDoMapRootsToConcepts ()
	{
		return doMapRootsToConcepts;
	}

	public void setDoMapRootsToConcepts ( boolean doMapRootsToConcepts )
	{
		this.doMapRootsToConcepts = doMapRootsToConcepts;
	}

	public Scanner<S, SI> getRootsScanner ()
	{
		return rootsScanner;
	}

	public void setRootsScanner ( Scanner<S, SI> rootsScanner )
	{
		this.rootsScanner = rootsScanner;
	}

	public Scanner<SI, SI> getChildrenScanner ()
	{
		return childrenScanner;
	}

	public void setChildrenScanner ( Scanner<SI, SI> childrenScanner )
	{
		this.childrenScanner = childrenScanner;
	}
}
