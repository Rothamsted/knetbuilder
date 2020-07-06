package net.sourceforge.ondex.tools.tab.importer;

import java.util.Collection;

/**
 * An interface to define a generic input
 * @author lysenkoa
 *
 */
public interface TaggedInput {
	public static final String CURRNET_SCOPE="CURRENT_INPUT_SCOPE";
	
	/**
	 * Get an identifier of this piece of information - used to pass it to the right
	 * processor 
	 * @return identifier of information type 
	 */
	public Tag getTag();
	/**
	 * Retrieve the actual information
	 * The resource may be null  
	 * @return data
	 */
	public String getInput();
	/**
	 * When the scope is entered all of processors associated scope will be start receiving data.
	 * @return the set of scopes that are now entered
	 */
	public Collection<Object> getScopesEntered();
	/**
	 * When the scope is exited all of the processors associated
	 * with it are forced to parse data they have received, if able. If the parent scope of a scope
	 * is exited, all of the sub-scopes are exited as well. The last input in the reader must contain
	 * CURRNET_SCOPE constant in its 'scopes exited' clause to indicate that there is no more data
	 * to come from this reader.   
	 * @return the set of scopes that is now left.
	 */
	public Collection<Object> getScopesExited();
}
