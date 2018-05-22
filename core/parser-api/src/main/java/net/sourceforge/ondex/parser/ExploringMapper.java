package net.sourceforge.ondex.parser;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;

/**
 * <p>The exploring mapper. This is based on the idea to start from a set of data items, which are provided by 
 * a {@link #getRootsScanner() roots scanner}.</p>
 * 
 * <p>Those items are then explored by means of a set of {@link #getLinkers() linkers}. Each linker contains a 
 * {@link Scanner} and a {@link RelationMapper}, the former is used to expand a root node into other nodes, the relation
 * mapper is used to create links from the initial root to each of the related nodes. A relation mapper can be a 
 * {@link ConceptBasedRelMapper concept-based relation mapper}, in which case the root and the related nodes are first 
 * mapped to {@link ONDEXConcept} by means of the explorer's {@link #getConceptMapper() explorer's concept mapper} and 
 * its {@link #getConceptClassMapper()} (both of which receives the discovered nodes as data source items to map. 
 * The nodes found to be related to the roots are then further explored by taking them as new roots and the process is 
 * recursively repeated in a top-down fashion, until the linkers return empty results.</p> 
 * 
 * <p>This kind of mapper is more generic than a mapper that explores a tree structure (such as an ontology), which is the 
 * typical case where this kind of mapper is expected to be used.</p>
 *
 * <p>Further options and details are available, see the method's comments below.</p>
 * 
 * @see InvertingRelationMapper, which can be useful when you follow relations like subclassOf and you want to map
 * them as is-a.
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
		private RelationMapper<?, ?> mapper;
		
		public LinkerConfiguration () {
			this ( null, null );
		}

		/**
		 * This is used to represent the components and options needed to discover nodes linked to others. See the class's
		 * method comments for details.
		 */
		public <T> LinkerConfiguration ( Scanner<SI, SI> scanner, RelationMapper<T, T> mapper )
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
		@SuppressWarnings ( "unchecked" )
		public <T> RelationMapper<T, T> getMapper ()
		{
			return (RelationMapper<T, T>) mapper;
		}

		public <T> void setMapper ( RelationMapper<T, T> mapper )
		{
			this.mapper = mapper;
		}
	}
	
	private ConceptClassMapper<SI> conceptClassMapper;
	private ConceptMapper<SI> conceptMapper;
	
	private boolean doMapRootsToConcepts = true;
	
	private Scanner<S, SI> rootsScanner;
	private List<LinkerConfiguration<SI>> linkers = Collections.emptyList ();

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
		.map ( root -> this.scanTree ( root, root, graph ) )
		.filter ( Objects::nonNull ); // might return null nodes when it decides not to explore them (eg, because already visited)
	}

	/**
	 * Starts from a root and recurse the exploration based on the {@link #getLinkers() linkers}, as explained above.
	 * the topItem is used to determine if we're at the first level of recursion and this is used to realise the behaviour of
	 * {@link LinkerConfiguration#isExcludeFirstLevel()}.
	 * 
	 */
	@SuppressWarnings ( "unchecked" )
	protected ONDEXConcept scanTree ( SI rootItem, SI topItem, ONDEXGraph graph )
	{			
		ConceptClassMapper<SI> ccmapper = this.getConceptClassMapper ();
		final ConceptMapper<SI> conceptMapper = this.getConceptMapper ();

		ConceptClass rootCC = ccmapper.map ( rootItem, graph );
		
		// Map top items only if required by configuration. 
		ONDEXConcept rootConcept = this.isDoMapRootsToConcepts () || !rootItem.equals ( topItem )
			? conceptMapper.map ( rootItem, rootCC, graph )
			: null;
		
		for ( LinkerConfiguration<SI> linker: this.getLinkers () )
		{
			Scanner<SI, SI> targetsScanner = linker.getScanner ();
			if ( targetsScanner.isVisited ( rootItem ) ) continue;
				
			RelationMapper<?, ?> relmap = linker.getMapper ();
			
			// so...
			targetsScanner
			.scan ( rootItem ) // get related nodes
			.filter ( targetItem -> !targetsScanner.isVisited ( targetItem ) ) // skip those already processed
			// and process the remaining ones recursively
			.forEach ( targetItem ->
			{
				ONDEXConcept targetConcept = conceptMapper.map ( targetItem, ccmapper, graph );
				
				// Again, make a relation from the top items only if required by configuration
				if ( this.isDoMapRootsToConcepts () || !rootItem.equals ( topItem ) )
				{
					if ( relmap instanceof ConceptBasedRelMapper )
					 ( (ConceptBasedRelMapper) relmap).map ( rootConcept, targetConcept, graph );
					else
					 ( (RelationMapper<SI, SI>) relmap ).map ( rootItem, targetItem, graph );
				}
				
				// And then keep recursing
				this.scanTree ( targetItem, topItem, graph );
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

	/**
	 * <p>If this is true, the links between the nodes returned by the {@link ExploringMapper#getRootsScanner() roots scanner}
	 * must not be mapped to {@link ONDEXConcept concepts} and the links from this to the immediate related nodes found by a 
	 * {@link #getLinkers() linker} must not be mapped to an {@link ONDEXRelation}. If that is the case, the root nodes 
	 * are just used to create {@link ConceptClass}es to be used to map concepts and to kick-start the exploration.</p>
	 * 
	 * <p>This is useful in tree-based mappers, where the root nodes are supposed to provide the category of concepts they 
	 * contain in the sub-tree, for instance you might decide that the root class 'Biological Process' in GeneOntology 
	 * corresponds to a concept class, to which all the biological processes are associated as concepts.</p>
	 * 
	 * <p><b>WARNING</b>: in real cases this might be very challenging to implement, for several reasons: hierarchies
	 * might not be well-separated and ONDEX doesn't support multiple inheritance. 
	 * 
	 * <p>This defaults to true</p>
	 */
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
