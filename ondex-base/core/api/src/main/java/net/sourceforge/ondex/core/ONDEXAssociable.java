package net.sourceforge.ondex.core;

import java.io.Serializable;

/**
 * Classes implementing this interface are associable to a graph by providing
 * SuperID (SID). The SID links all instances of the class to one and only one
 * ONDEXGraph.
 * 
 * @author Jochen Weile, M.Sc.
 * 
 */
public interface ONDEXAssociable extends Serializable {

	/**
	 * @return the SuperID (SID) identifying the graph.
	 */
	public long getSID();
}
