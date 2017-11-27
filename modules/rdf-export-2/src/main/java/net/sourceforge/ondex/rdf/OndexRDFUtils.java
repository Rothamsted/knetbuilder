package net.sourceforge.ondex.rdf;

import static org.apache.commons.lang3.StringUtils.replaceChars;

import java.net.URLEncoder;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import uk.ac.ebi.utils.ids.IdUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>26 Nov 2017</dd></dl>
 *
 */
public class OndexRDFUtils
{
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
			
			final String badChars = " :.;,+-()'\\=*|,<>~!@[]{}^&/?";
			idPart = StringUtils.replaceChars ( idPart, badChars, StringUtils.repeat ( '_', badChars.length () ) );
			idPart = IdUtils.urlEncode ( idPart );
		}
		
		StringBuilder sb = new StringBuilder ( ns );
		if ( classPart != null ) sb.append ( classPart ).append ( '_' );
		sb.append ( idPart );
		
		return sb.toString ();
	}
}
