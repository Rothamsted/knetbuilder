package net.sourceforge.ondex.config;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.core.ONDEXGraph;

public class ONDEXGraphRegistry {

	/**
	 * Mapping of sid to instance of ONDEXGraph. The is the global ONDEXGraph
	 * registry.
	 */
	public static Map<Long, ONDEXGraph> graphs = new HashMap<Long, ONDEXGraph>();

}
