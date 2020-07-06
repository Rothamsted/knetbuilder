package net.sourceforge.ondex.ovtk2.util;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.io.OXLExport;
import net.sourceforge.ondex.ovtk2.io.OXLImport;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop.Position;
import net.sourceforge.ondex.ovtk2.ui.OVTK2ResourceAssesor;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.tools.threading.monitoring.IndeterminateProcessAdapter;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;

/**
 * Utility functions mainly used in OVTK2Desktop related functionality.
 * 
 * @author taubertj
 * 
 */
public class DesktopUtils {

	/**
	 * Adds and displays the given graph on the desktop.
	 * 
	 * @param aog
	 *            ONDEXGraph to display
	 */
	public static void displayGraphOnDesktop(ONDEXGraph aog) {
		displayGraphOnDesktop(aog, null);
	}

	/**
	 * Adds and displays the given graph on the desktop.
	 * 
	 * @param aog
	 *            ONDEXGraph to display
	 * @param annotations
	 *            XML String
	 */
	public static void displayGraphOnDesktop(ONDEXGraph aog, Map<String, String> annotations) {
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		OVTK2ResourceAssesor resources = OVTK2Desktop.getDesktopResources();

		// put graph into viewer
		OVTK2Viewer viewer = DesktopUtils.initViewer(aog, annotations);

		// set viewer as active viewer
		resources.setSelectedViewer(viewer);

		// check for appearance attributes on graph
		AttributeName anVisible;
		int option = JOptionPane.NO_OPTION;
		if ((anVisible = aog.getMetaData().getAttributeName(AppearanceSynchronizer.VISIBLE)) != null && aog.getConceptsOfAttributeName(anVisible).size() > 0) {
			option = JOptionPane.showConfirmDialog(OVTK2Desktop.getDesktopResources().getParentPane(), Config.language.getProperty("Dialog.Appearance.Found"), Config.language.getProperty("Dialog.Appearance.FoundTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		}

		// load appearance attributes
		if (option == JOptionPane.YES_OPTION) {
			AppearanceSynchronizer.loadAppearance(desktop, viewer);

			// display viewer centred
			desktop.display(viewer, Position.centered);

			// centre graph
			viewer.center();
		} else {

			// get number of concepts
			Set<ONDEXConcept> viewC = aog.getConcepts();
			int concepts = viewC.size();

			// get number of relations
			Set<ONDEXRelation> viewR = aog.getRelations();
			int relations = viewR.size();

			// decide when to display viewer immediately
			if (Math.max(concepts, relations) > 2000) {

				// display viewer centred and iconified
				desktop.display(viewer, Position.centeredIconified);

			} else {
				// we can display everything here
				viewer.getONDEXJUNGGraph().setEverythingVisible();

				// display viewer centred
				desktop.display(viewer, Position.centered);

				// make sure layout is set
				VisualisationUtils.relayout(viewer, desktop.getMainFrame());
			}
		}

		// update all visual attributes of graph
		viewer.updateViewer(null);
		try {
			viewer.setSelected(true);
		} catch (PropertyVetoException e) {
			ErrorDialog.show(e);
		}

		// hack to show meta graph
		JCheckBoxMenuItem item = new JCheckBoxMenuItem();
		item.setSelected(true);
		desktop.actionPerformed(new ActionEvent(item, ActionEvent.ACTION_PERFORMED, "metagraph"));
	}

	/**
	 * Extracts the Hudson build number from the ovtk2.jar
	 * 
	 * @return build number
	 */
	public static String extractBuildNumber() {
		try {
			File dir = new File(Config.ovtkDir + File.separator + ".." + File.separator + "lib");
			if (dir.exists() && dir.canRead()) {
				for (File child : dir.listFiles()) {
					if (child.getName().startsWith("ovtk2-")) {

						// create a buffer to improve copy performance later.
						byte[] buffer = new byte[2048];

						ZipInputStream stream = new ZipInputStream(new FileInputStream(child));
						try {
							// now iterate through each item in the stream. The
							// get next entry call will return a ZipEntry for
							// each file in the stream
							ZipEntry entry;
							while ((entry = stream.getNextEntry()) != null) {

								if (entry.getName().endsWith("MANIFEST.MF")) {
									ByteArrayOutputStream output = new ByteArrayOutputStream();
									int len = 0;
									while ((len = stream.read(buffer)) > 0) {
										output.write(buffer, 0, len);
									}

									// open a reader on that entry
									BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(output.toByteArray())));
									String line;
									String hudson = null;
									String implver = null;
									while ((line = br.readLine()) != null) {
										if (line.startsWith("Hudson-Build-Number:")) {
											String[] split = line.split(":");
											hudson = split[1];
										} else if (line.startsWith("Implementation-Version:")) {
											String[] split = line.split(":");
											implver = split[1];
										}
									}
									br.close();
									if (hudson != null)
										return hudson;
									if (implver != null)
										return implver;
								}
							}
						} finally {
							// we must always close the zip file.
							stream.close();
						}
					}
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Setup a ONDEXGraph for a given name.
	 * 
	 * @param name
	 *            Name of ONDEXGraph
	 * @return ONDEXGraph
	 */
	public static ONDEXGraph initGraph(String name) {
		// construct new ONDEXGraph
		ONDEXGraph og = new MemoryONDEXGraph(name);
		return og;
	}

	/**
	 * Setup a new OVTK2Viewer.
	 * 
	 * @param aog
	 *            ONDEXGraph
	 * @return OVTK2Viewer
	 */
	public static OVTK2Viewer initViewer(ONDEXGraph aog) {
		return initViewer(aog, null);
	}

	/**
	 * Setup a new OVTK2Viewer.
	 * 
	 * @param aog
	 *            ONDEXGraph
	 * @param annotations
	 *            XML String
	 * @return OVTK2Viewer
	 */
	public static OVTK2Viewer initViewer(ONDEXGraph aog, Map<String, String> annotations) {

		OVTK2Desktop desktop = OVTK2Desktop.getInstance();

		ONDEXEventHandler.getEventHandlerForSID(aog.getSID()).addONDEXONDEXListener(OVTK2Desktop.getDesktopResources().getLogger());

		OVTK2Viewer viewer = new OVTK2Viewer(aog, annotations);
		viewer.addInternalFrameListener(desktop);
		viewer.addVetoableChangeListener(desktop);

		// for listening to changes in ModalGraphMouse
		viewer.addItemListener(OVTK2Desktop.getDesktopResources().getToolBar());
		desktop.viewers.add(viewer);

		return viewer;
	}

	/**
	 * Opens an ondex graph from the specified file and loads it in a new viewer
	 * 
	 * @param file
	 *            a valid file to open
	 */
	public static void openFile(File file) {
		if (file != null && file.exists() && file.canRead()) {

			// name of new graph
			String name = file.getName();

			// get a new empty graph from file
			final ONDEXGraph aog = DesktopUtils.initGraph(name);

			// import knows about its progress
			final OXLImport imp = new OXLImport(aog, file);
			Monitorable p = imp;
			OVTKProgressMonitor.start(OVTK2Desktop.getInstance().getMainFrame(), Config.language.getProperty("Progress.ReadingFile"), p);

			// wrap into a process
			Thread t = new Thread() {
				public void run() {
					try {
						// start OXL import process
						imp.start();
						if (imp.isCancelled())
							return;

						// wrap into indefinite process
						IndeterminateProcessAdapter p = new IndeterminateProcessAdapter() {
							public void task() {
								// display the graph
								DesktopUtils.displayGraphOnDesktop(aog, imp.getAnnotations());
							}
						};

						// display graph
						OVTKProgressMonitor.start(OVTK2Desktop.getInstance().getMainFrame(), Config.language.getProperty("Progress.AddingToDesktop"), p);
						p.start();
					} catch (Exception e) {
						ErrorDialog.show(e);
					}
				}
			};
			// start processing and monitoring
			t.start();
		} else {
			JOptionPane.showMessageDialog(OVTK2Desktop.getInstance().getMainFrame(), Config.language.getProperty("Dialog.File.NotFound"), Config.language.getProperty("Dialog.File.NotFoundTitle"), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Saves an ondex graph to the specified file and from the given graph
	 * 
	 * @param file
	 *            a valid file to open
	 * @param viewer
	 *            viewer with graph to save
	 * @throws JAXBException
	 */
	public static void saveFile(File file, OVTK2Viewer viewer) {
		if (file != null) {

			if (file.exists() && Boolean.parseBoolean(Config.config.getProperty("Overwrite.Set"))) {
				int answer = JOptionPane.showInternalConfirmDialog(OVTK2Desktop.getInstance().getDesktopPane(), Config.language.getProperty("Dialog.Save.Warning.Text"), Config.language.getProperty("Dialog.Save.Warning.Title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (answer == JOptionPane.NO_OPTION)
					return;
			}

			// export knows about its progress
			final OXLExport exp = new OXLExport(viewer.getONDEXJUNGGraph(), file);
			Monitorable p = exp;
			OVTKProgressMonitor.start(OVTK2Desktop.getInstance().getMainFrame(), Config.language.getProperty("Progress.SavingFile"), p);

			// wrap into a process
			Thread t = new Thread() {
				public void run() {
					try {
						// start OXL export process
						exp.start();
					} catch (Exception e) {
						ErrorDialog.show(e);
					}
				}
			};
			// start processing and monitoring
			t.start();

			// OVTK-202
			String title = viewer.getTitle();
			title = title.substring(0, title.indexOf("-") + 2) + file.getName();
			viewer.setTitle(title);
			viewer.setName(file.getName());
		}
	}

	/**
	 * Shows a file open dialog with the given filter file extensions.
	 * 
	 * @param extensions
	 *            used to filter filenames
	 * @return File
	 */
	public static File showOpenDialog(String[] extensions) {
		File dir = (Config.lastOpenedFile == null) ? new File(System.getProperty("user.dir")) : new File(Config.lastOpenedFile);
		JFileChooser fc = new JFileChooser(dir);
		fc.addChoosableFileFilter(new CustomFileFilter(extensions));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

		OVTK2Desktop desktop = OVTK2Desktop.getInstance();

		// in response to a button click:
		int returnVal = fc.showOpenDialog(desktop.getMainFrame());

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			Config.lastOpenedFile = file.getAbsolutePath();
			if (Config.lastSavedFile == null)
				Config.lastSavedFile = Config.lastOpenedFile;
			System.out.println("Opening: " + file.getName() + ".");
			return file;
		}
		System.out.println("Open command cancelled by user.");
		return null;
	}

	/**
	 * Shows a file save dialog with the given filter file extensions.
	 * 
	 * @param extensions
	 *            used to filter filenames
	 * @return File
	 */
	public static File showSaveDialog(String[] extensions) {
		File dir = (Config.lastSavedFile == null) ? new File(System.getProperty("user.dir")) : new File(Config.lastSavedFile);
		JFileChooser fc = new JFileChooser(dir);
		fc.addChoosableFileFilter(new CustomFileFilter(extensions));

		OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		ChooserAccessory accessory = new ChooserAccessory();
		fc.setAccessory(accessory);

		// in response to a button click:
		int returnVal = fc.showSaveDialog(desktop.getMainFrame());

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			accessory.perform(desktop);
			File file = fc.getSelectedFile();
			Config.lastSavedFile = file.getAbsolutePath();
			System.out.println("Saving: " + file.getName() + ".");
			return file;
		}
		System.out.println("Save command cancelled by user.");
		return null;
	}

	/**
	 * Simple check boxes for saving appearance.
	 * 
	 * @author taubertj
	 * 
	 */
	public static class ChooserAccessory extends JComponent {

		/**
		 * generated
		 */
		private static final long serialVersionUID = 6609419104997083350L;

		JCheckBox visible = null;

		JCheckBox invisible = null;

		JCheckBox appearance = null;

		public ChooserAccessory() {
			this.setBorder(BorderFactory.createLoweredBevelBorder());
			BoxLayout layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
			this.setLayout(layout);
			visible = new JCheckBox(Config.language.getProperty("Dialog.Save.Visible"));
			visible.setSelected(true);
			visible.setEnabled(false);
			this.add(visible);
			invisible = new JCheckBox(Config.language.getProperty("Dialog.Save.InVisible"));
			invisible.setSelected(true);
			this.add(invisible);
			appearance = new JCheckBox(Config.language.getProperty("Dialog.Save.Appearance"));
			this.add(appearance);
		}

		public void perform(OVTK2Desktop desktop) {
			if (!invisible.isSelected()) {
				desktop.actionPerformed(new ActionEvent(this, 0, "sync"));

				// wait here for sync to finish
				synchronized (this) {
					while (!desktop.getRunningProcess().equals("none")) {
						try {
							this.wait(100);
						} catch (InterruptedException ie) {
							ErrorDialog.show(ie);
						}
					}
				}
			}
			if (appearance.isSelected()) {
				// this should not be in a thread
				desktop.actionPerformed(new ActionEvent(this, 0, "saveappearance"));
			}
		}
	}

	/**
	 * Class to simply compare IDs of MetaData case insensitive.
	 * 
	 * @author taubertj
	 * 
	 */
	public static class CaseInsensitiveMetaDataComparator implements Comparator<MetaData> {

		@Override
		public int compare(MetaData o1, MetaData o2) {
			return o1.getId().toUpperCase().compareTo(o2.getId().toUpperCase());
		}
	}
}
