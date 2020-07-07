package net.sourceforge.ondex.algorithm.graphquery;

import java.util.List;

import net.sourceforge.ondex.algorithm.pathmodel.Path;

/**
 * Interface for class that defines method for ranking paths
 * 
 * @author hindlem
 *
 */
public interface FilterPaths<P extends Path> {

	
	public List<P> filterPaths(List<P> paths);
	
}
