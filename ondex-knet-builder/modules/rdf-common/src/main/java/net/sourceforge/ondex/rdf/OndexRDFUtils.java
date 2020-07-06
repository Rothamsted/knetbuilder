package net.sourceforge.ondex.rdf;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import net.sourceforge.ondex.core.ConceptName;
import uk.ac.ebi.utils.ids.IdUtils;

/**
 * Utilities to deal with RDF in ONDEX.
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>26 Nov 2017</dd></dl>
 *
 */
public class OndexRDFUtils
{	
	/**
	 * Encodes an ID (like an accession in a proper way). This consists of replacing certain characters
	 * (eg, punctuations, brackets) with '_' and then invoking {@link IdUtils#urlEncode(String)}, to properly
	 * encode possible residual special characters. Note that there isn't any lower-case conversion here.
	 * 
	 * If the id is null, the method returns null. There is no trimming.
	 * 
	 */
	public static String idEncode ( String id )
	{
		if ( id == null ) return null;
		
		final String badChars = " :.;,+-()'\\=*|,<>~!@[]{}^&/?";
		id = StringUtils.replaceChars ( id, badChars, StringUtils.repeat ( '_', badChars.length () ) );
		return IdUtils.urlEncode ( id );
	}
	
	
	/**
	 * makes an uri using its main parts and doing some massaging of them.
	 * 
	 * @param ns: the namespace, the first part of the URI
	 * @param classPart: the classPart is turned into lower-case and then prefixed to id. If it's null 
	 * (after {@link StringUtils#trimToNull(String)}), "generic" is used as the class part of the IRI.
	 * @param acc. This is concatenated to classPart using '_' as separator, after processing 
	 * via {@link #idEncode(String)} and lower-case conversion.
	 *  
	 */
	public static String iri ( String ns, String classPart, String acc, int id )
	{
		classPart = StringUtils.trimToNull ( classPart );
		if ( classPart == null ) 
			classPart = "generic";
		else 
			classPart = classPart.toLowerCase ();
		
		String idPart = StringUtils.trimToNull ( acc );
		if ( idPart == null ) 
			idPart = String.valueOf ( id );
		else
		{
			idPart = idPart.toLowerCase ();
			// To prevent cases like GO_GO:1234
			if ( idPart.startsWith ( classPart ) ) classPart = null;
			idPart = idEncode ( idPart );
		}
		
		StringBuilder sb = new StringBuilder ( ns );
		if ( classPart != null ) sb.append ( classPart ).append ( '_' );
		sb.append ( idPart );
		
		return sb.toString ();
	}
	

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
	 * @param nameMapper how N is turned into string, after {@code prefNameFilter}. At the moment we pass
	 * {@link ConceptName#getName()} to this.
	 * 
	 * @return a pair of a unique preferred name, an empty optional if none was found, plus a {@link Stream} of
	 * all the other names, which are considered alternative names.
	 * 
	 * <p>The method always reduces the initial collection to a single {@code prefName}, even if {@code prefNameFilter} 
	 * returns more than one name. This can be useful where you want to decide for one name only, even in presence of 
	 * ambiguity/wrong data modelling. {@code prefNameFilter} is only applied when searching for a unique name, and
	 * when this is found, everything else is considered alternative, independently on this filter. 
	 * No matter which parameters you pass here, we always do some basic filtering, i.e., at the moment anything that is 
	 * {@link StringUtils#isEmpty(CharSequence) empty} is ignored (the string considered is the one returned by
	 * {@code nameMapper}.</p>
	 * 
	 * <p>When multiple names are marked as preferred, several criteria are applied to the their string representation,
	 * in order to come up with one only, e.g., shorter names, upper case names (see the method implementation). 
	 * Those criteria are designed to find a unique name that reasonably the user wants to be displayed, before 
	 * alternatives. They are also designed to be deterministic with respect to the order in which names in the input 
	 * collection are scanned.</p>
	 * 
	 * <p>Moreover, the selection depends only on the name's string representation (i.e., on whatever 
	 * {@code nameMapper} returns. The criteria are also arbitrary somehow (e.g., longer mixed-case names might 
	 * be more significant than upper case acronyms, while we select the latter). This is because the method is supposed 
	 * to be used to fix ambiguities and errors. Ultimately, having two values for a property like "preferred Name" 
	 * is an inconsistency and, if you don't want this method to cause unintended visualisations for your data, you should fix 
	 * the data (i.e., ensure that the property is used in a functional way).
	 * </p>
	 */
	public static <N> Pair<Optional<String>, Stream<String>> normalizeNames ( 
		final Collection<N> names, Predicate<? super N> prefNameFilter,  Function<N, String> nameMapper 
	)
	{
		if ( names == null || names.isEmpty () ) 
			return Pair.of ( Optional.empty (), Stream.empty () );
							
		// Now let's select the preferred one		
		// First, filter and transform as per parameters, then get rid of empty cases too
		Optional<String> prefName = names
		.stream ()
		.parallel ()
		.filter ( prefNameFilter )
		.map ( nameMapper )
		.filter ( name -> !StringUtils.isEmpty ( name ) )
		// And then the "most preferable"
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
		
		// Now let's build the result with <prefName, all the names minus the preferred name>
		Stream<String> altNames = names
		.stream ()
		.parallel()
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
