package net.sourceforge.ondex.config;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.core.searchable.LuceneEnv;

/**
 * Registers all available Lucene graphs.
 * 
 * @author taubertj
 * 
 */
public class LuceneRegistry {

	public final static Map<Long, LuceneEnv> sid2luceneEnv = new HashMap<Long, LuceneEnv>();

}
