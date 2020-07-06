package net.sourceforge.ondex.parser.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A simple utility to map configured strings to others. 
 * 
 * This can be useful together with {@link ChainingMapper#setPostProcessor(Function)}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 May 2018</dd></dl>
 *
 */
public class StringMapper implements Function<String, String>
{
	private Map<String, String> map = new HashMap<> ();
	
	@Override
	public String apply ( String s )
	{
		if ( map == null ) return s;
		return map.getOrDefault ( s, s );
	}

	public Map<String, String> getMap ()
	{
		return map;
	}

	public void setMap ( Map<String, String> map )
	{
		this.map = map;
	}
}
