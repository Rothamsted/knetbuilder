package net.sourceforge.ondex.ovtk2.io;

import java.io.File;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * Interface to streamline IO functions of OVTK2.
 * 
 * @author taubertj
 * 
 */
public interface OVTK2IO {

	/**
	 * Current ONDEXGraph to work with.
	 * 
	 * @param graph
	 */
	public void setGraph(ONDEXGraph graph);

	/**
	 * Starts the process of import or export.
	 * 
	 * @param file
	 *            file to export to or import from.
	 * @throws Exception
	 */
	public void start(File file) throws Exception;

	/**
	 * Whether or not this IO is an importer.
	 * 
	 * @return
	 */
	public boolean isImport();

	/**
	 * Filename extension to filter files with.
	 * 
	 * @return
	 */
	public String getExt();

}
