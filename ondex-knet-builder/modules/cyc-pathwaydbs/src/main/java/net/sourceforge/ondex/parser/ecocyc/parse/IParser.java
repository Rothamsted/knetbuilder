package net.sourceforge.ondex.parser.ecocyc.parse;

import net.sourceforge.ondex.parser.ecocyc.objects.AbstractNode;

/**
 * Interface for the concret parsers
 * @author peschr
 *
 */
public interface IParser {
	/**
	 * process the information for each line.
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	public void distribute(String key,String value) throws Exception;
	public void distributeCore(String key,String value) throws Exception;
	/**
	 * returns the concret node
	 * @return
	 */
	public AbstractNode getNode();
	/**
	 * will be invoked, if a new entry is parsed
	 * @param uniqueId
	 * @throws Exception
	 */
	public void start(String uniqueId) throws Exception;
}
