package net.sourceforge.ondex.rdf;

import org.apache.commons.lang3.StringUtils;

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
}
