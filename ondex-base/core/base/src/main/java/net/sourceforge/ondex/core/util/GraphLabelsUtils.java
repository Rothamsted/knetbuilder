package net.sourceforge.ondex.core.util;

import java.util.Comparator;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;

public class GraphLabelsUtils
{

	/**
	 * 
	 * Returns the best label for a concept, considering several criteria, including the concept type (eg,
	 * if it's a gene or not). 
	 * 
	 */
	public static String getBestConceptLabel ( ONDEXConcept c )
	{
		String typeId = c.getOfType ().getId ();
		
		String result = getBestName ( c.getConceptNames () ); // priority to the shortest preferred name
				
		if ( result.isEmpty () )
		{
			// non-ambiguous accession, discriminate by type
			result = ArrayUtils.contains ( new String [] { "Gene", "Protein" }, typeId )
				? getBestGeneAccession ( c )
				: getBestAccession ( c );
		}
			
		if ( result.isEmpty () ) result = StringUtils.trimToEmpty ( c.getPID () );

		return StringUtils.abbreviate ( result, 63 );
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
		Comparator<String> accStrCmp = (acc1, acc2) -> specialAccessionCompare ( acc1, acc2 );
		
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
	 * Uses special comparison/priority between accession strings.
	 * 
	 * It assumes, trimmed accessions. This is always included in the {@link #getBestAccession(Set, boolean, ToIntFunction)}
	 * comparisons.
	 */
	private static int specialAccessionCompare ( String acc1, String acc2 )
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
	 */
	private static int getKnownSourcesAccessionPriority ( ConceptAccession acc )
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
	 * Just a wrapper, concept must be non-null
	 */
	public static String getBestAccession ( ONDEXConcept concept )
	{
		return getBestAccession ( concept.getConceptAccessions () );
	}
	
	/**
	 * Best accession selector for genes.
	 * 
	 * This uses {@link #getKnownSourcesAccessionPriority(ConceptAccession)}, which is used in certain views
	 * for the accession field.
	 * 
	 * As for {@link #getBestAccession(ONDEXConcept)}, this also gives priority to non-ambiguous accessions, then it  
	 * possibly considers ambiguous ones.
	 * 
	 * @see #getBestGeneAccession(Set, boolean)
	 * @see #getBestAccession(Set, boolean, ToIntFunction)
	 * @see #getKnownSourcesAccessionPriority(ConceptAccession)
	 */
	public static String getBestGeneAccession ( Set<ConceptAccession> geneAccessions )
	{	
		String result = getBestGeneAccession ( geneAccessions, true );
		if ( !result.isEmpty () ) return result;		
		
		return getBestGeneAccession ( geneAccessions, false );
	}

	
	/**
	 * Just a wrapper of {@link #getBestGeneAccession(ONDEXConcept)}
	 * 
	 */
	public static String getBestGeneAccession ( ONDEXConcept geneConcept )
	{
		return getBestGeneAccession ( geneConcept.getConceptAccessions () );
	}
	
	
	
	/**
	 * Uses {@link #getBestAccession(Set, boolean, ToIntFunction)} with {@link #getKnownSourcesAccessionPriority(ConceptAccession)}, 
	 * which is a special priority criterion we require for genes. 
	 */
	private static String getBestGeneAccession ( Set<ConceptAccession> geneAccs, boolean useUniques )
	{
		return getBestAccession ( 
			geneAccs, useUniques, GraphLabelsUtils::getKnownSourcesAccessionPriority
		);
	}
	
	
	/**
	 * Selects the best name for a set, giving priority to the shortest first and then 
	 * to the canonical string order.
	 * 
	 * @param usePreferredOrAltNames whether to use {@link ConceptName#isPreferred() preferred name} or not. This is 
	 * supposed to be unique, but data are often dirty, hence we don't trust that and we still prioritise 
	 * possible multiple preferred names.
	 * 
	 * This method version is for internal use in this class, typically, you don't want to ignore non-preferred ones, 
	 * so use {@link #getBestName(Set)} instead, which is public.
	 * 
	 */
	private static String getBestName ( Set<ConceptName> cns, boolean usePreferredOrAltNames ) 
	{	
		var cnsStrm = cns.parallelStream ()
		.filter ( cname -> usePreferredOrAltNames ? cname.isPreferred () : !cname.isPreferred () );
		
		return cnsStrm
		.map ( ConceptName::getName )
		.map ( String::trim )
		.filter ( nameStr -> !nameStr.isEmpty () )
		.min (
			Comparator.comparing ( String::length )
			.thenComparing ( Comparator.naturalOrder () ) 
		)
		.orElse ( "" );		
	}
	
	/**
	 * This tries to use {@link #getBestName(Set, boolean) the best preferred name} first, and then, if none is available,
	 * it further tries the alternative names.
	 * 
	 */
	public static String getBestName ( Set<ConceptName> cns ) 
	{
		String result = getBestName ( cns, true );
		if ( !result.isEmpty () ) return result;
		
		return getBestName ( cns, false );
	}

	/**
	 * Just a wrapper, the concept is assumed to be non-null.
	 * 
	 * @param filterAccessions if true, it tries to remove accessions from the result, using other available names, if
	 *   			available.
	 * 
	 */
	public static String getBestName ( ONDEXConcept concept, boolean filterAccessions )
	{
		var names = concept.getConceptNames ();
		var bestName = getBestName ( names );
		
		if ( !filterAccessions ) return bestName;
			
		
		// We need to filter accessions, first let's see if the result is an accession
		//
		Set<String> accs = concept.getConceptAccessions ()
			.stream ()
			.map ( ConceptAccession::getAccession )
			.collect ( Collectors.toSet () );
		
		if ( !accs.contains ( bestName ) ) return bestName;

		// There isn't any alternative
		if ( names.size () < 2 ) return bestName;
		
		// OK, let's recompute the best name without accessions in the way
		//
		names = names.stream ()
			.filter ( name -> !accs.contains ( name.getName () ) )
			.collect ( Collectors.toSet () );
				
		var filteredBestName = getBestName ( names );
		// Shouldn't happen, but just in case
		return "".equals ( filteredBestName ) ? bestName : filteredBestName;
	}

	/**
	 * Defaults to false
	 */
	public static String getBestName ( ONDEXConcept concept )
	{
		return getBestName ( concept, false );
	}

}