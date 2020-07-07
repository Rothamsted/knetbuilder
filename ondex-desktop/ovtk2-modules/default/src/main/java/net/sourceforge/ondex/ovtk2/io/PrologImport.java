package net.sourceforge.ondex.ovtk2.io;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.parser.prolog.Parser;

public class PrologImport implements OVTK2IO {

	/**
	 * Track errors during import.
	 */
	private JTextPane errorReport = new JTextPane();

	private Parser parser = new Parser();

	public PrologImport() {
	}

	@Override
	public void setGraph(ONDEXGraph graph) {
		parser.setONDEXGraph(graph);
	}

	@Override
	public void start(File file) throws Exception {

		ONDEXPluginArguments args = new ONDEXPluginArguments(parser
				.getArgumentDefinitions());
		args.addOption(FileArgumentDefinition.INPUT_FILE, file
				.getAbsolutePath());
		parser.setArguments(args);

		parser.start();
		getErrors(parser.getErrorReport());
	}

	/**
	 * Returns the component containing the accumulated error messages.
	 */
	private void getErrors(String errors) {
		errorReport.setText(errors.substring(1, errors.length()));
		// show possible errors
		if (errorReport.getText().length() > 0) {
			JOptionPane.showMessageDialog(OVTK2Desktop.getInstance()
					.getMainFrame(), new JScrollPane(errorReport),
					"The following warnings were created during import...",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	@Override
	public String getExt() {
		return "pro";
	}

	@Override
	public boolean isImport() {
		return true;
	}

}
