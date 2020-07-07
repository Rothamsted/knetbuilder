package net.sourceforge.ondex.ovtk2.util;

import java.io.File;
import java.util.HashSet;

import javax.swing.filechooser.FileFilter;

/**
 * Simple FileFilter for a given extension.
 * 
 * @author taubertj
 * 
 */
public class CustomFileFilter extends FileFilter {

	// generated
	private static final long serialVersionUID = 1L;

	// extensions to use
	private HashSet<String> extensions = new HashSet<String>();

	/**
	 * Constructor takes custom extension.
	 * 
	 * @param extension
	 *            String
	 */
	public CustomFileFilter(String extension) {
		this.extensions.add(extension);
	}

	/**
	 * Constructor takes array of extensions.
	 * 
	 * @param extensions
	 *            String[]
	 */
	public CustomFileFilter(String[] extensions) {
		for (int i = 0; i < extensions.length; i++) {
			this.extensions.add(extensions[i]);
		}
	}

	/**
	 * Extract file extension from file name.
	 * 
	 * @param f
	 *            File
	 * @return String
	 */
	public String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	/**
	 * Filter files by extension.
	 * 
	 * @param f
	 *            File
	 * @return true if extension matches
	 */
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		String extension = getExtension(f);
		if (extension != null) {
			if (extensions.contains(extension)) {
				return true;
			} else {
				return false;
			}
		}

		return false;
	}

	/**
	 * Description of this FileFilter.
	 * 
	 * @return String
	 */
	public String getDescription() {
		return "." + extensions + " Files Only";
	}
}
