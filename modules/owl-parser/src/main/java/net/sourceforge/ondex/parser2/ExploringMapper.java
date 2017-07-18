package net.sourceforge.ondex.parser2;

import java.util.List;
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
public abstract class ExploringMapper<S, SI> implements StreamMapper<S, ONDEXConcept>
{
	public static class LinkerConfiguration<SI>
	{
		private Scanner<SI, SI> scanner;
		private RelationMapper<SI, SI> mapper;
		private boolean excludeFirstLevel = false;
		
		public LinkerConfiguration ( Scanner<SI, SI> scanner, RelationMapper<SI, SI> mapper, boolean excludeFirstLevel )
		{
			super ();
			this.scanner = scanner;
			this.mapper = mapper;
			this.excludeFirstLevel = excludeFirstLevel;
		}

		public LinkerConfiguration ( Scanner<SI, SI> scanner, RelationMapper<SI, SI> mapper )
		{
			super ();
			this.scanner = scanner;
			this.mapper = mapper;
		}

		public Scanner<SI, SI> getScanner ()
		{
			return scanner;
		}

		public void setScanner ( Scanner<SI, SI> scanner )
		{
			this.scanner = scanner;
		}

		public RelationMapper<SI, SI> getMapper ()
		{
			return mapper;
		}

		public void setMapper ( RelationMapper<SI, SI> mapper )
		{
			this.mapper = mapper;
		}

		public boolean isExcludeFirstLevel ()
		{
			return excludeFirstLevel;
		}

		public void setExcludeFirstLevel ( boolean excludeFirstLevel )
		{
			this.excludeFirstLevel = excludeFirstLevel;
		}
	}
	
	private ConceptClassMapper<SI> conceptClassMapper;
	private ConceptMapper<SI> conceptMapper;
	
	private boolean doMapRootsToConcepts = true;
	
	private Scanner<S, SI> rootsScanner;
	private List<LinkerConfiguration<SI>> linkers;

	
	@Override
	public Stream<ONDEXConcept> map ( S source, ONDEXGraph graph )
	{
		return this.getRootsScanner ()
		.scan ( source )
		.map ( root -> this.scanTree ( root, root, graph ) );
	
		// TODO: document that we're returning the root concepts
	}

	protected ONDEXConcept scanTree ( SI rootItem, SI topItem, ONDEXGraph graph )
	{			
		ConceptClassMapper<SI> ccmapper = this.getConceptClassMapper ();
		final ConceptMapper<SI> conceptMapper = this.getConceptMapper ();

		// Map top items only if required by configuration. 
		ONDEXConcept rootConcept = this.isDoMapRootsToConcepts () || !rootItem.equals ( topItem )
			? conceptMapper.map ( rootItem, ccmapper, graph )
			: null;
		
		for ( LinkerConfiguration<SI> linker: this.getLinkers () )
		{
			Scanner<SI, SI> targetsScanner = linker.getScanner ();
			if ( targetsScanner.isVisited ( rootItem ) ) continue;
				
			RelationMapper<SI, SI> relmap = linker.getMapper ();
			
			targetsScanner
			.scan ( rootItem )
			.filter ( targetsScanner::isVisited )
			.forEach ( targetItem ->
			{
				ONDEXConcept targetConcept = conceptMapper.map ( targetItem, ccmapper, graph );
				
				// Again, make a relation from the top items only if required by configuration
				if ( !this.isDoMapRootsToConcepts () && rootItem.equals ( topItem ) ) return;
				
				if ( relmap instanceof ConceptBasedRelMapper )
				 ( (ConceptBasedRelMapper) relmap).map ( rootConcept, targetConcept, graph );
				else
				 relmap.map ( rootItem, targetItem );
			});
			
			targetsScanner.setVisited ( rootItem );
		}
		
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
	
	public List<LinkerConfiguration<SI>> getLinkers ()
	{
		return linkers;
	}

	public void setLinkers ( List<LinkerConfiguration<SI>> linkers )
	{
		this.linkers = linkers;
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
}
