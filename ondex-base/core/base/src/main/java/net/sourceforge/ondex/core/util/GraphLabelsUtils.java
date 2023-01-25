package net.sourceforge.ondex.core.util;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import uk.ac.ebi.utils.regex.RegEx;

/**
 * Methods to choose best labels for {@link ONDEXConcept}.
 * Was migrated from KnetMiner.
 */
public class GraphLabelsUtils
{
	/**
	 * Defaults to false.
	 */
	public static String getBestConceptLabel ( ONDEXConcept c )
	{
		return getBestConceptLabel ( c, false );
	}

	/**
	 * Defaults to a result abbreviated at 63 chars.
	 */
	public static String getBestConceptLabel ( ONDEXConcept c, boolean filterAccessionsFromNames )
	{
		return getBestConceptLabel ( c, filterAccessionsFromNames, 63 );
	}

	/**
	 * Invokes {@link #getBestConceptLabelCore(ONDEXConcept, boolean, int, boolean)} without 
	 * considering specie-prefixed names for genes.
	 * 
	 */
	public static String getBestConceptLabel ( ONDEXConcept c, boolean filterAccessionsFromNames, int maxLen )
	{
		return getBestConceptLabelCore ( c, filterAccessionsFromNames, maxLen, false );
	}
	
	
	/**
	 * Defaults to false.
	 */
	public static String getBestConceptLabelWithGeneSpeciePrefix ( ONDEXConcept c )
	{
		return getBestConceptLabelWithGeneSpeciePrefix ( c, false );
	}

	/**
	 * Defaults to a result abbreviated at 63 chars.
	 */
	public static String getBestConceptLabelWithGeneSpeciePrefix ( ONDEXConcept c, boolean filterAccessionsFromNames )
	{
		return getBestConceptLabelWithGeneSpeciePrefix ( c, filterAccessionsFromNames, 63 );
	}
	
	
	/**
	 * Invokes {@link #getBestConceptLabelCore(ONDEXConcept, boolean, int, boolean)} giving priority 
	 * to specie-prefixed names for genes (ie, passes down useGeneSpeciePrefix = true ).
	 * 
	 */
	public static String getBestConceptLabelWithGeneSpeciePrefix ( ONDEXConcept c, boolean filterAccessionsFromNames, int maxLen )
	{
		return getBestConceptLabelCore ( c, filterAccessionsFromNames, maxLen, true );
	}

	
	/**
	 * 
	 * Returns the best label for a concept, considering several criteria, including the concept type (eg,
	 * if it's a gene or not). This method is the core implementation for the other getBestConceptLabelXXX 
	 * wrappers above. 
	 * 
	 * @param filterAccessions removes accessions from names, if these are duplicated there from accessions,
	 *        as per {@link #getBestName(ONDEXConcept, boolean)}. 
	 *
	 * @param maxLen if >3, the result is {@link StringUtils#abbreviate(String, int) abbreviated} at 
	 *        maxLen - 3 and '...' is appended.
	 *   
	 * @param useGeneSpeciePrefix if this is true and the concept c is of type Gene, then it tries to 
	 *        select the name with the specie prefix, as explained in {@link #getBestNameCore(Set, boolean)}.
	 */
	private static String getBestConceptLabelCore (
		ONDEXConcept c, boolean filterAccessionsFromNames, int maxLen, boolean useGeneSpeciePrefix )
	{
		String typeId = c.getOfType ().getId ();
		
		Set<ConceptName> names = filterAccessionsFromNames 
			? filterAccessionsFromNames ( c )
			: c.getConceptNames ();
		
		String result = getBestNameCore ( names, useGeneSpeciePrefix && "Gene".equals ( typeId ) );
				
		if ( result.isEmpty () )
		{
			// non-ambiguous accession, discriminate by type
			result = ArrayUtils.contains ( new String [] { "Gene", "Protein" }, typeId )
				? getBestGeneAccession ( c )
				: getBestAccession ( c );
		}
			
		if ( result.isEmpty () ) result = StringUtils.trimToEmpty ( c.getPID () );
		if ( maxLen > 3 ) result = StringUtils.abbreviate ( result, maxLen - 3 );
		return result;
	}




	
	/**
	 * Just a wrapper of {@link #getBestAccession(Set)}, concept must be non-null
	 */
	public static String getBestAccession ( ONDEXConcept concept )
	{
		return getBestAccession ( concept.getConceptAccessions () );
	}
	
	/**
	 * Just a wrapper of {@link #getBestGeneAccession(Set)}
	 * 
	 */
	public static String getBestGeneAccession ( ONDEXConcept geneConcept )
	{
		return getBestGeneAccession ( geneConcept.getConceptAccessions () );
	}

	
	/**
	 * Best accession selector for genes.
	 * 
	 * This uses {@link #priorityByKnownSources(ConceptAccession)}, which is used in certain views
	 * for the accession field.
	 * 
	 * As for {@link #getBestAccession(ONDEXConcept)}, this also gives priority to non-ambiguous accessions, then it  
	 * possibly considers ambiguous ones.
	 * 
	 * @see #getBestGeneAccession(Set, boolean)
	 * @see #getBestAccession(Set, boolean, ToIntFunction)
	 * @see #priorityByKnownSources(ConceptAccession)
	 */
	public static String getBestGeneAccession ( Set<ConceptAccession> geneAccessions )
	{	
		String result = getBestGeneAccession ( geneAccessions, true );
		if ( !result.isEmpty () ) return result;		
		
		return getBestGeneAccession ( geneAccessions, false );
	}

		
	/**
	 * Uses {@link #getBestAccession(Set, boolean, ToIntFunction)} with {@link #priorityByKnownSources(ConceptAccession)}, 
	 * which is a special priority criterion we require for genes. 
	 */
	private static String getBestGeneAccession ( Set<ConceptAccession> geneAccs, boolean useUniques )
	{
		return getBestAccession ( 
			geneAccs, useUniques, GraphLabelsUtils::priorityByKnownSources
		);
	}
	
	
	/**
	 *  Yields the best accession in the set.
	 *  
	 *  You should use this for generic concepts and end-user visualisations of accessions. 
	 *  For generic labelling, which is also based on names, use {@link #getBestConceptLabel(ONDEXConcept)}.
	 *  
	 *  This tries to use a non-ambiguous accession first, and then possibly, it falls back to considering 
	 *  ambiguous accessions too.
	 *  
	 *  @see #getBestAccession(Set, boolean, ToIntFunction)
	 */
	public static String getBestAccession ( Set<ConceptAccession> accs )
	{
		String result = getBestAccession ( accs, true, null );
		if ( !result.isEmpty () ) return result;		
		
		return getBestAccession ( accs, false, null );
	}
	
	
	/**
	 * This is a low-level implementation of {@link #getBestAccession(Set, boolean)}, probably you don't
	 * want to use it, use the public versions instead.
	 * 
	 * Finds the best accession in a set. It mainly applies the criteria of shortest and lexicographically
	 * first value, making exceptions for a few special cases.
	 * 
	 * If the input is null or empty, returns ""
	 * 
	 * @param useUniques, if true, considers only accessions with {@link ConceptAccession#isAmbiguous()} not
	 * set (might return "" if none available), else consider only ambiguous accessions.  
	 * 
	 * @param priorityCriteria, if non-null, it gives priority to the accessions that have the lowest values for 
	 * this function. This is a low-level feature, use the other more abstract labelling criteria if you're in 
	 * doubt about it.
	 *  
	 */
	private static String getBestAccession (
		Set<ConceptAccession> accs, boolean useUniques, ToIntFunction<ConceptAccession> priorityCriteria 
	)
	{
		if ( accs == null || accs.size () == 0 ) return "";
						
		// Our own comparisons
		Comparator<String> accStrCmp = (acc1, acc2) -> compareByMaizeGenes ( acc1, acc2 );
		
		// In all the other cases, first compare the lengths and then the string values.
		accStrCmp = accStrCmp.thenComparingInt ( String::length )
			.thenComparing ( Comparator.naturalOrder () );
		
		// Then, prefix it with the priority criteria and string trimming
		Comparator<ConceptAccession> accCmp = Comparator.comparing ( 
			(ConceptAccession acc) -> acc.getAccession ().trim(), accStrCmp
		);
		if ( priorityCriteria != null ) 
			accCmp = Comparator.comparingInt ( priorityCriteria ).thenComparing ( accCmp );
		

		var accsStrm = accs.parallelStream ();
		accsStrm = accsStrm.filter ( acc -> useUniques ? !acc.isAmbiguous () : acc.isAmbiguous () );
		
		return accsStrm
		.filter ( acc -> StringUtils.trimToNull ( acc.getAccession () ) != null )
		.min ( accCmp )
		// Unfortunately, we have to do a final re-map, in order to be able to apply whole-ConceptAccession
		// comparisons
		.map ( ConceptAccession::getAccession )
		.map ( String::trim )
		.orElse ( "" );
	}	
	
	
	
	/**
	 * Defaults to false
	 */
	public static String getBestName ( ONDEXConcept concept )
	{
		return getBestName ( concept, false );
	}
	
	/**
	 * Similar to {@link #getBestName(Set)}, but with some extra logics.
	 * 
	 * @param filterAccessions if true, it tries to remove accessions from the result, using other available names, if
	 *   			available.
	 */
	public static String getBestName ( ONDEXConcept concept, boolean filterAccessions )
	{
		var names = concept.getConceptNames ();
		
		var bestName = getBestName( names );
		
		if ( !filterAccessions ) return bestName;
			
		var filteredNames = filterAccessionsFromNames ( concept, bestName );
		// No alternative
		if ( filteredNames == null ) return bestName;
						
		var filteredBestName = getBestName ( filteredNames );

		// Shouldn't happen, but just in case
		return "".equals ( filteredBestName ) ? bestName : filteredBestName;
	}
	
	/**
	 * This tries to use the preferred names first, and then, if none is available,
	 * it further tries the alternative names.
	 * 
	 * This is a wrapper of {@link #getBestNameCore(Set, boolean)} without the useGeneSpeciePrefix
	 * feature.
	 * 
	 */
	public static String getBestName ( Set<ConceptName> cns ) 
	{
		return getBestNameCore ( cns, false );
	}

	
	/**
	 * 
	 * Core function to select the best name for a set, giving priority based on a couple of criterion (see below).
	 * 
	 * By default, it chooses a name by considering this order of priority: 
	 * 
	 * <ol>
	 *   <li>the specie-prefixed names if useGeneSpeciePrefix is true (see below)</li>
	 *   <li>the preferred names</li>
	 *   <li>other aspects (shortest names, natural string order)</li>
	 * </ol>
	 * 
	 * @param useGeneSpeciePrefix use {@link #isGeneNameSpeciePrefixed(String)} as first criterion of name priority.
	 * That is, when this flag is set, names like TmABC are privileged. Then it uses the other sorting criteria. 
	 * 
	 * This method version is for internal use in this class, since you don't want to use the useGeneSpeciePrefix
	 * explicitly. In fact, this feature isn't used by default, it's only used by 
	 * {@link #getBestConceptLabelWithGeneSpeciePrefix(ONDEXConcept, boolean, int)} 
	 * 
	 */
	private static String getBestNameCore ( Set<ConceptName> cns, boolean useGeneSpeciePrefix ) 
	{
		// This is the common chain tail described above
	  Comparator<String> nameStrCmp = Comparator.comparing ( String::length )
			.thenComparing ( Comparator.naturalOrder () );
		
	  // This is the head of the tail, when !useGeneSpeciePrefix. We pass a pair to it, because it needs to use
	  // the isPreferred() flag first and then a normalised string, not the original name string (see the stream below)
	  //
		Comparator<Pair<String, Boolean>> namesCmp = // compares Pair<normalised name string, isPreferred>
	  	Comparator.comparing ( ( Pair<String, Boolean> nameElems ) -> nameElems.getRight () )
	  	.reversed () // isPreferred == true takes priority, so we need to reverse this, since false < true
	  	.thenComparing ( Pair::getLeft, nameStrCmp ); // tail appended
 
		// As said above, this is the first thing to consider when requested, and in particular, it takes priority 
		// over isPreferred (ie, specie-prefixed names are preferred independently on that flag)
		//
		if ( useGeneSpeciePrefix )
			namesCmp = Comparator.comparingInt ( 
				( Pair<String, Boolean> nameElems ) -> priorityBySpeciePrefixedName ( nameElems.getLeft () )
			).thenComparing ( namesCmp ); // again, the rest is appended
		
		// Now we can apply the priority sorting
		//
		
		return cns.parallelStream ()
		// As said above, first of all we normalise the string name and then we continue with this normalised string
		.map ( name -> Pair.of ( name.getName ().trim (), name.isPreferred () ) )
		// It should never happen, but just some sanity check
		.filter ( nameElems -> !nameElems.getLeft ().isEmpty () )
		// Eventually!
		.min ( namesCmp )
		// Of course, this is the normalised string again
		.map ( Pair::getLeft )
		.orElse ( "" );
	}	
	

	
	/**
	 * Filters accessions from names. This is sometime useful for visualisations where both are reported.
	 */
	public static Set<ConceptName> filterAccessionsFromNames ( ONDEXConcept concept )
	{
		return filterAccessionsFromNames ( concept, null );
	}
		
	
	/**
	 * Just a wrapper of {@link #filterAccessionsFromNamesAsStream(ONDEXConcept, String)}.
	 * 
	 */
	private static Set<ConceptName> filterAccessionsFromNames ( ONDEXConcept concept, String selectedName )
	{
		return Optional.ofNullable ( filterAccessionsFromNamesAsStream ( concept, selectedName ) )
			.map ( strm -> strm.collect ( Collectors.toSet () ) )
			.orElse ( null );
	}

	/**
	 * Same as {@link #filterAccessionsFromNames(ONDEXConcept), but returns the stream of filtered names, which 
	 * might be more efficient if you need to further process it.
	 */
	public static Stream<ConceptName> filterAccessionsFromNamesAsStream ( ONDEXConcept concept )
	{
		return filterAccessionsFromNamesAsStream ( concept, null );
	}

	
	/**
	 * This is the real implementation of {@link #filterAccessionsFromNames(ONDEXConcept)}, which is used by 
	 * {@link #getBestName(ONDEXConcept, boolean)}. It considers the case where a best name has already been selected 
	 * (eg, for displaying), but it might be one of the accessions, so, it goes on with filtering only if it is indeed
	 * an accession.
	 * 
	 * @param selectedName: a name that has been picked from the concept's names. This MUST be one of the names, else
	 *        the method won't work.
	 *        
	 * @return if selectedName is one of the concept's accessions, or the concept has only one name (ie, the already
	 *         selected one), returns null, else it returns the concept's names without those names that are equal to
	 *         some of the concept's accessions.
	 *         
	 */
	private static Stream<ConceptName> filterAccessionsFromNamesAsStream ( ONDEXConcept concept, String selectedName )
	{
		// We need to filter accessions, first let's see if the result is an accession
		//
		Set<String> accs = concept.getConceptAccessions ()
			.stream ()
			.map ( ConceptAccession::getAccession )
			.collect ( Collectors.toSet () );
		
		if ( selectedName != null && !accs.contains ( selectedName ) ) return null;

		Set<ConceptName> names = concept.getConceptNames ();
		
		// There is no alternative
		if ( selectedName != null && names.size () < 2 ) return null;
		
		return names.stream ()
			.filter ( name -> !accs.contains ( name.getName () ) );
	}
	
	
	
	/**
	 * Establish if a given (gene) name should have priority over other common criteria, based on the fact that
	 * has a format like TsABC, where Ts is a specie prefix.
	 * 
	 * This is used in {@link #getBestNameCore(Set, boolean)} and, indirectly, in
	 * {@link #getBestConceptLabelWithGeneSpeciePrefix(ONDEXConcept, boolean, int)}.
	 * 
	 * As in other cases, this is a single-scope sorting criterion, all other priorities have to be chained to it.
	 */
	private static int priorityBySpeciePrefixedName ( String name )
	{
		if ( name.length () <= 2 ) return 0;
		
		RegEx prefixRe = RegEx.of ( "^[A-Z][a-z][A-Z].*" );
		return prefixRe.matches ( name ) ? -1 : 0;
	}
	
	
	
	/**
	 * Uses special comparison/priority between accession strings that are possibly about maize.
	 * 
	 * It assumes, trimmed accessions. This is always included in the {@link #getBestAccession(Set, boolean, ToIntFunction)}
	 * comparisons.
	 */
	private static int compareByMaizeGenes ( String acc1, String acc2 )
	{
		// This is to privilege maize genes of type EB (#593)
		final var zmebRe = "^ZM.+EB[0-9].*";
		final var zmdRe = "^ZM.+D[0-9].*"; 
		if ( acc1.matches ( zmebRe ) && acc2.matches ( zmdRe  ) ) return -1;
		if ( acc2.matches ( zmebRe ) && acc1.matches ( zmdRe ) ) return 1;
		// I deal only with this aspect, the rest is left to the chain where I'm used
		return 0; 
	}
	
	
	/**
	 * This is used for the gene accession field in certain views, it gives priority to ENSEMBL and other
	 * sources.
	 * 
	 * @return -1 if the accession belongs to certain preferred sources, else returns 0
	 */
	private static int priorityByKnownSources ( ConceptAccession acc )
	{
		String accStr = acc.getAccession ();
		String accSrcId = acc.getElementOf ().getId ();
		if ( accSrcId.startsWith ( "ENSEMBL" ) ) return -1;
		if ( "PHYTOZOME".equals ( accSrcId ) ) return -1;
		if ( "TAIR".equals ( accSrcId ) && accStr.startsWith ( "AT" ) && accStr.indexOf ( "." ) == -1 ) return -1;

		// I only deal with the above, the rest comes from the comparison chain where I'm inserted
		return 0;
	}
	
	/**
	 * After you've found a name, with {@link #getBestName(ONDEXConcept, boolean)}, does
	 * a selection for {@link ONDEXConcept concepts} of type {@link ConceptClass#getId() Gene}:
	 * if 'name' is like ABCD and there is another name like "TaABCD", then it returns the 
	 * prefixed version. If that's not the case, it returns name. 
	 * 
	 * Sanity checks are done too, ie, considered names must have length = name.len + 2 
	 * and they must be like [A-Z][a-z].
	 * 
	 * TODO:remove (probably). This doesn't work very well when there is also a name like EFG. 
	 */
	@Deprecated
	private static String _getGenePrefixedBestName ( String name, ONDEXConcept concept ) 
	{
		if ( !"Gene".equals ( concept.getOfType ().getId () ) ) return name;
		
		Set<ConceptName> cns = concept.getConceptNames ();
				
		RegEx prefixRe = RegEx.of ( "^[A-Z][a-z][A-Z].*" );
			
		String newName = cns.stream ()
		.map ( ConceptName::getName )
		.map ( String::trim )
		// As said above, current name is like TaABC and name is ABC  
		.filter ( thisName -> thisName.length () == name.length () + 2 )
		.filter ( thisName -> thisName.endsWith ( name ) )
		.filter ( prefixRe::matches )
		.sorted ( Comparator.naturalOrder () )
		.findFirst ()
		.orElse ( name );
		
		return newName;
	}
}
