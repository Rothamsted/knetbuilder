package net.sourceforge.ondex.ovtk2.ui.contentsdisplay;

import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * This abstract class is the template for all content display plugins. Derive
 * from this class for any content display producer you want to write.
 * 
 * @author Jochen Weile, B.Sc.
 * 
 */
public abstract class AbstractContentDisplayPlugin {

	/**
	 * The Graph.
	 */
	protected ONDEXGraph aog;

	/**
	 * The constructor.
	 * 
	 * @param aog
	 *            the graph.
	 */
	public AbstractContentDisplayPlugin(ONDEXGraph aog) {
		this.aog = aog;
	}

	/**
	 * Returns the producer's name.
	 * 
	 * @return plugin name.
	 */
	public abstract String getName();

	/**
	 * Returns the producer's version.
	 * 
	 * @return the version.
	 */
	public abstract String getVersion();

	/**
	 * takes a ONDEXEntity (either a concept or a relation) and generates HTML
	 * code about it according to whatever the function of your producer will
	 * be.
	 * 
	 * @param e
	 *            the concept or relation.
	 * @return a string containing valid HTML code.
	 */
	public abstract String compileContent(ONDEXEntity e);

}
