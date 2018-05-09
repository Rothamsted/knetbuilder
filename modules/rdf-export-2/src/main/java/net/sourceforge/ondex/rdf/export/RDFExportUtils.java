package net.sourceforge.ondex.rdf.export;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import net.sourceforge.ondex.core.ConceptName;


/**
 * Misc utility functions for Ondex RDF exporting.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>8 May 2018</dd></dl>
 *
 */
public class RDFExportUtils
{
	/** 
	 * Normalises a collection of names/identifiers/etc into one single preferred name and a collection of
	 * alternative names, both returned as strings. 
	 * 
	 * @param names a collection of names. This can be of any type, at the moment we use this method for
	 * {@link ConceptName} only.
	 * 
	 * @param prefNameFilter a predicate over the N type, which establishes if a name is considered a potential
	 * preferred one. At the moment we pass {@link ConceptName#isPreferred()} to this.
	 * 
	 * @param nameMapper how N is turned into string, after {@code prefNameFilter}
	 * 
	 * @return a pair of a unique preferred name, an empty optional if none was found, plus a {@link Stream} of
	 * all the other names, which are considered alternative names.
	 * 
	 * <p>The method always reduces the initial collection to a single {@code prefName}, even if {@code prefNameFilter} 
	 * returns more than one name. This can be useful where you want to decide for one name only, even in presence of 
	 * ambiguity/wrong data modelling. {@code prefNameFilter} is only applied when searching for the a unique name, 
	 * when this is found, everything else is considered alternative, independently on this filter. 
	 * No matter which parameters you pass here, we always do some basic filtering, i.e., at the moment anything that is 
	 * {@link StringUtils#isEmpty(CharSequence) empty} is ignored (the string considered is the one returned by
	 * {@code nameMapper}.</p>
	 * 
	 * <p>When multiple names are marked as preferred, several criteria are applied to the their string representation,
	 * in order to come up with one only:
	 * 	<ul>
	 * 		<li>shorter names are preferred</li>
	 * 		<li>when two names have the same length, if one of the two is all upper case, it's preferred (the rationale being
	 *    that short identifiers or acronyms are often preferable for displaying.</li>
	 *    <li>if none of the above apply, i.e., two names have the same length and none is entirely upper case or both are,
	 *    then the fall-back choice is preferring the one coming first in lexicographical order.</li>
	 *  </ul>
	 *  
	 *  The above criteria are designed to find a unique name that reasonably the user wants to be displayed, before 
	 *  alternatives. They are also designed to be deterministic with respect to the order in which names in the collection
	 *  are scanned. Moreover, the selection depends only on the name's string representation (i.e., on whatever 
	 *  {@code prefNameFilter} returns. The criteria are also arbitrary (e.g., longer mixed-case names might be more 
	 *  significant than upper case acronyms. This is because the method is supposed to be used to fix ambiguities and
	 *  errors: ultimately, having two values for a property like "preferred Name" is an error and if you don't want 
	 *  unintended visualisations for your data, created by this method, you should fix the data (i.e., enforce that the 
	 *  property is used in a functional way).
	 * </p>
	 */
	public static <N> Pair<Optional<String>, Stream<String>> normalizeNames ( 
		final Collection<N> names, Predicate<? super N> prefNameFilter,  Function<N, String> nameMapper 
	)
	{
		if ( names == null || names.isEmpty () ) 
			return Pair.of ( Optional.empty (), Stream.empty () );
							
		// Now let's select the preferred one
		// Filter and transform as per parameters, then get rid of empty cases too
		Optional<String> prefName = names
		.stream ()
		.filter ( prefNameFilter )
		.map ( nameMapper )
		.filter ( name -> !StringUtils.isEmpty ( name ) )
		.min ( (name1, name2) -> 
		{
			// Shortest wins
			name1 = name1.trim ();
			name2 = name2.trim ();
			int ldiff = name1.length () - name2.length ();
			if ( ldiff != 0 ) return ldiff;
			
			// If same len, consider if one of the two (only) is all upper case, 
			// it is an acronym in many cases, so let's prefer it
			if ( name1.equals ( name1.toUpperCase () ) ) {
				if ( !name2.equals ( name2.toUpperCase () ) ) return -1;
			}
			else if ( name2.equals ( name2.toUpperCase () ) ) return +1;
			
			// Eventually, fallback into lexicographic order
			return name1.compareTo ( name2 );
		});
		
		// Now, let's build the result with <prefName, all the names minus the preferred name>
		Stream<String> altNames = names
		.stream ()
		.map ( nameMapper )
		.filter ( name -> !StringUtils.isEmpty ( name ) ); 
				
		if ( prefName.isPresent () )
		{
			String prefNameStr = prefName.get ();
			altNames = altNames.filter ( s -> !prefNameStr.equals ( s ) );
		}
		return Pair.of ( prefName, altNames );
	}
}
