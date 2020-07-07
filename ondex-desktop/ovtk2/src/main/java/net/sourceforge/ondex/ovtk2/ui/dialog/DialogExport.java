package net.sourceforge.ondex.ovtk2.ui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.util.CustomFileFilter;

public class DialogExport extends JFileChooser {

	private static final long serialVersionUID = -838051249430090419L;

	private HashSet<String> formats;

	private JList list;

	private JPanel options;

	private JPanel spinnerP;

	private JSpinner spinner = null;

	/**
	 * Constructor accepts a directory to start in.
	 * 
	 * @param dir
	 *            directory to use
	 */
	public DialogExport(File dir) {
		super(dir);

		// make sure we captured all plug-ins
		ImageIO.scanForPlugins();
		ImageIO.setUseCache(true);

		formats = new HashSet<String>();
		for (String format : ImageIO.getWriterFormatNames()) {
			// ignore WBMP it only causes problems
			if (!format.equalsIgnoreCase("wbmp") && !format.equalsIgnoreCase("gif"))
				formats.add(format.toLowerCase());
		}

		// custom eps exporter
		formats.add("eps");

		// formats.add("svg");
		// formats.add("pdf");
		// formats.add("ps");
		// formats.add("wmf");
		// formats.add("tiff");

		SpinnerModel magnifyModel = new SpinnerNumberModel(1, // initial value
				1, // min
				Integer.MAX_VALUE, // max
				0.2); // step
		spinner = new JSpinner(magnifyModel);

		spinnerP = new JPanel(new FlowLayout(FlowLayout.LEFT));
		spinnerP.add(new JLabel(Config.language.getProperty("Dialog.Export.ScaleFactor")));
		spinnerP.add(spinner);

		initOptions();

		// prevent spinner being selected for no image formats
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String type = (String) ((JList) e.getSource()).getSelectedValue();
				spinner.setEnabled(false);
				for (String scalabletype : ImageIO.getWriterFormatNames()) {
					if (type.equals(scalabletype)) {
						spinner.setEnabled(true);
						return;
					}
				}
			}
		});

		this.setAccessory(options);
	}

	private void initOptions() {

		// configure file filters
		for (FileFilter ff : this.getChoosableFileFilters()) {
			this.removeChoosableFileFilter(ff);
		}
		this.addChoosableFileFilter(new CustomFileFilter(formats.toArray(new String[formats.size()])));
		this.setAcceptAllFileFilterUsed(false);

		// add list of types
		list = new JList(formats.toArray(new String[formats.size()]));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(0);

		if (options == null)
			options = new JPanel(new BorderLayout());
		else
			options.removeAll();
		options.add(new JLabel("Export Type"), BorderLayout.NORTH);
		options.add(new JScrollPane(list), BorderLayout.CENTER);
		options.add(spinnerP, BorderLayout.SOUTH);
		options.updateUI();
	}

	public void removeAllFormats() {
		this.formats.clear();
		initOptions();
	}

	public void addFormat(String format) {
		this.formats.add(format);
		initOptions();
	}

	public File getFile() {
		File file = this.getSelectedFile();
		if (!file.getName().endsWith("." + list.getSelectedValue())) {
			file = new File(file.getAbsolutePath() + "." + list.getSelectedValue());
		}
		return file;
	}

	public float getScaleFactor() {
		return ((Double) this.spinner.getValue()).floatValue();
	}

	public String getSelectedFormat() {
		return list.getSelectedValue().toString();
	}
}
