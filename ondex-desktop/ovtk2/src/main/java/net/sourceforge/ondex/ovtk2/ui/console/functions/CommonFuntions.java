package net.sourceforge.ondex.ovtk2.ui.console.functions;

import java.io.File;

import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.util.DesktopUtils;
import net.sourceforge.ondex.scripting.FunctionException;

public class CommonFuntions {

	private CommonFuntions() {
	}

	public static void closeViewer(OVTK2Viewer viewer) {
		viewer.dispose();
	}

	public static void openGraph(final String path) throws FunctionException {
		File file = new File(path);
		if (file.exists() && file.canRead()) {
			try {
				DesktopUtils.openFile(file);
			} catch (RuntimeException e) {
				throw new FunctionException("Could not open file - it does not appear to be of a valid format.", -1);
			}
		} else {
			throw new FunctionException("Could not read from the file - make sure the file name is correct.", -1);
		}
	}
}
