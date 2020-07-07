package net.sourceforge.ondex.parser.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/**
 * Utility to replace a sub-string with another in a value.
 * 
 * This can be useful together with {@link ChainingMapper#setPostProcessor(Function)}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Jun 2019</dd></dl>
 *
 */
public class StringReplacer implements Function<String, String>
{
	private Map<String, String> map = new HashMap<> ();

	private boolean isRegExMode = false;
	
	public StringReplacer ()
	{
	}

	@Override
	public String apply ( String s )
	{
		if ( s == null ) return null;
		if ( this.map == null ) return s;
		
		for ( Entry<String, String> e: this.map.entrySet () ) 
		{
			String target = e.getKey ();
			String replacement = e.getValue ();
			
			s = this.isRegExMode 
				? s.replaceAll ( target, replacement )
				:	s.replace ( target, replacement );
		}
		return s;
	}


	public Map<String, String> getMap ()
	{
		return map;
	}

	public void setMap ( Map<String, String> map )
	{
		this.map = map;
	}

	public boolean isRegExMode ()
	{
		return isRegExMode;
	}

	public void setRegExMode ( boolean isRegExMode )
	{
		this.isRegExMode = isRegExMode;
	}
}
