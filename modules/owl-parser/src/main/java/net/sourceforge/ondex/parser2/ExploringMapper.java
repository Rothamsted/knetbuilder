package net.sourceforge.ondex.parser2;

import java.util.List;
import java.util.stream.Stream;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;

/**
 * The exploring mapper. This is based on the idea to start from a set of data items, which are provided by 
 * a {@link #getRootsScanner() roots scanner}. Those items are then explored by means of a set of 
 * {@link #getLinkers() linkers}. Each linker contains a {@link Scanner} and a {@link RelationMapper}, the former is
 * used to expand a root node into other nodes, the relation mapper is used to create links from the initial root to 
 * each of the related nodes. A relation mapper can be a {@link ConceptBasedRelMapper concept-based relation mapper}, 
 * in which case the root and the related nodes are first mapped to {@link ONDEXConcept} by means of the explorer's
 * {@link #getConceptMapper() explorer's concept mapper} and its {@link #getConceptClassMapper()} (both of which receives
 * the discovered nodes as data source items to map. The nodes found to be related to the roots are then further explored
 * by taking them as new roots and the process is recursively repeated in a top-down fashion, until the linkers return 
 * empty results. 
 * 
 * This kind of mapper is more generic of a mapper that explores a tree structure, such as an ontology, which is the 
 * typical case where this kind of mapper is expected to be used.
 *
 * Further options and details are available, see the method's comments below.
 * 
 * @see InvertingPairMapper.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 May 2017</dd></dl>
 *
 */
public class ExploringMapper<S, SI> implements StreamMapper<S, ONDEXConcept>
{
	public static class LinkerConfiguration<SI>
	{
		private Scanner<SI, SI> scanner;
		private RelationMapper<SI, SI> mapper;
		private boolean excludeFirstLevel = false;
		
		/**
		 * This is used to represent the components and options needed to discover nodes linked to others. See the class's
		 * method comments for details.
		 */
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

		/**
		 * Nodes related to a given node are found by means of this scanner. 
		 */
		public Scanner<SI, SI> getScanner ()
		{
			return scanner;
		}

		public void setScanner ( Scanner<SI, SI> scanner )
		{
			this.scanner = scanner;
		}

		/**
		 * a node and its related nodes, found by the {@link #getScanner() scanner associated to this linker}, are mapped
		 * to an {@link ONDEXRelation} by means of this mapper. This can be a more specific {@link ConceptBasedRelMapper}, 
		 * in which case the explorer's {@link ExploringMapper#getConceptMapper()} and 
		 * {@link ExploringMapper#getConceptClassMapper()} are used to get the concepts to be passed to this relation mapper,
		 * starting from the root/related nodes.
		 * 
		 * An example of this mappers might be a mapper to represent the 'is a' relation in ONDEX (probably you will need
		 * {@link InvertingPairMapper} for such a case, since the explorer's works top-down).
		 * 
		 */
		public RelationMapper<SI, SI> getMapper ()
		{
			return mapper;
		}

		public void setMapper ( RelationMapper<SI, SI> mapper )
		{
			this.mapper = mapper;
		}

		/**
		 * If this is true, the links between the nodes returned by the {@link ExploringMapper#getRootsScanner() roots scanner}
		 * and the immediate related nodes found by this linker are not mapped to an {@link ONDEXRelation}. The root nodes 
		 * are just used to create {@link ConceptClass}es to be used to map concepts. This is useful in tree-based mappers, 
		 * where the root nodes are supposed to provide the category of concepts they contain in the sub-tree, for instance
		 * you might decide that the root class 'Biological Process' in GeneOntology corresponds to a concept class, to which
		 * all the biological processes are associated as concepts.
		 */
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

	/**
	 * The mapper starts here, root nodes are extracted from the source, by means of the {@link #getRootsScanner() roots scanner},
	 * then these are passed to {@link #scanTree(Object, Object, ONDEXGraph)} one by one, which will recursively explore
	 * the data in a top-down fashion.
	 * 
	 * Note that the mapper returns only the concepts associated to the roots and not all the concepts that are created during
	 * the exploration. This makes the process less computationally intensive and we don't see much the need to return more
	 * than that (you can still get the whole set of created concepts later, from the graph parameter).
	 * 
	 */
	@Override
	public Stream<ONDEXConcept> map ( S source, ONDEXGraph graph )
	{
		return this.getRootsScanner ()
		.scan ( source )
		.map ( root -> this.scanTree ( root, root, graph ) );
	}

	/**
	 * Starts from a root and recurse the exploration based on the {@link #getLinkers() linkers}, as explained above.
	 * the topItem is used to determine if we're at the first level of recursion and this is used to realise the behaviour of
	 * {@link LinkerConfiguration#isExcludeFirstLevel()}.
	 * 
	 */
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
