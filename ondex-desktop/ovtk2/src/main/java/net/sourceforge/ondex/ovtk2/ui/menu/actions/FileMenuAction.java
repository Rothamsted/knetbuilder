package net.sourceforge.ondex.ovtk2.ui.menu.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.config.OVTK2PluginLoader;
import net.sourceforge.ondex.ovtk2.io.OVTK2IO;
import net.sourceforge.ondex.ovtk2.io.OXLImport;
import net.sourceforge.ondex.ovtk2.io.WizardImport;
import net.sourceforge.ondex.ovtk2.io.importwizard.ConfigTool;
import net.sourceforge.ondex.ovtk2.io.importwizard.ImportWizard;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop.Position;
import net.sourceforge.ondex.ovtk2.ui.OVTK2ResourceAssesor;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogExport;
import net.sourceforge.ondex.ovtk2.ui.dialog.WelcomeDialog;
import net.sourceforge.ondex.ovtk2.ui.menu.OVTK2Menu;
import net.sourceforge.ondex.ovtk2.util.DesktopUtils;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.OVTKProgressMonitor;
import net.sourceforge.ondex.ovtk2.util.SVGExport;
import net.sourceforge.ondex.tools.threading.monitoring.IndeterminateProcessAdapter;

/**
 * Listens to action events specific to the file menu.
 * 
 * @author taubertj
 */
public class FileMenuAction implements ActionListener {

	private static FileMenuAction instance = null;

	private FileMenuAction() {
		instance = this;
	}

	public static FileMenuAction getInstance() {
		if (instance == null)
			new FileMenuAction();
		return instance;
	}


	@Override
	public void actionPerformed(ActionEvent ae) {

		String cmd = ae.getActionCommand();
		final OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		final OVTK2ResourceAssesor resources = OVTK2Desktop.getDesktopResources();

		// for coping with plug-in Attribute data types
		try {
			Thread.currentThread().setContextClassLoader(OVTK2PluginLoader.getInstance().ucl);
		} catch (FileNotFoundException e) {
			ErrorDialog.show(e);
		} catch (MalformedURLException e) {
			ErrorDialog.show(e);
		}

		// new empty graph in OVTK2Viewer
		if (cmd.equals("new")) {

			// name of new graph
			String name = String.valueOf(OVTK2Viewer.instances);

			// get a empty new graph
			ONDEXGraph graph = DesktopUtils.initGraph(name);

			// put new graph into viewer
			OVTK2Viewer viewer = DesktopUtils.initViewer(graph);

			// set viewer as active viewer
			resources.setSelectedViewer(viewer);

			// display viewer centred
			desktop.display(viewer, Position.centered);

			// close possible welcome dialog
			WelcomeDialog.getInstance(desktop).setVisible(false);
		}

		// open data from file into new OVTK2Viewer
		else if (cmd.equals("open")) {

			// ask user what file to open
			File file = DesktopUtils.showOpenDialog(new String[] { "oxl", "gz", "xml" });

			if (file != null) {
				DesktopUtils.openFile(file);

				OVTK2Menu menu = (OVTK2Menu) desktop.getMainFrame().getJMenuBar();
				String path = file.getAbsolutePath();
				// hook into FileHistory class
				menu.fileHistory.insertPathname(path);

				// close possible welcome dialog
				WelcomeDialog.getInstance(desktop).setVisible(false);
			}
		}

		// uses import wizard to parse delimited files
		else if (cmd.equals("importwizard")) {
			if (resources.getSelectedViewer() != null) {
				final ImportWizard iw = new ImportWizard(ConfigTool.loadFromFile(Config.ovtkDir + File.separator + "iw_config.xml"));

				// add to JDesktopPane
				desktop.getDesktopPane().add(iw);
				iw.setVisible(true);

				// busy waiting for ImportWizard to close
				Thread thread = new Thread() {
					public void run() {
						synchronized (this) {
							while (iw.isVisible()) {
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
								}
							}
							Vector<Object> r = iw.getReturnContent();
							if (r.size() > 0) {

								// get current viewer
								OVTK2Viewer viewer = (OVTK2Viewer) resources.getSelectedViewer();
								viewer.getONDEXJUNGGraph().updateLastState();

								// graph to export
								ONDEXGraph aog = viewer.getONDEXJUNGGraph();

								// start loading of data
								new WizardImport(aog, r, null);
							}
						}
					}
				};
				// for coping with plug-in Attribute data types
				try {
					thread.setContextClassLoader(OVTK2PluginLoader.getInstance().ucl);
				} catch (FileNotFoundException e) {
					ErrorDialog.show(e);
				} catch (MalformedURLException e) {
					ErrorDialog.show(e);
				}
				thread.start();
			} else {
				JOptionPane.showInternalMessageDialog(OVTK2Desktop.getInstance().getDesktopPane(), "No graph loaded or available. Cannot continue with operation.", "Graph not found", JOptionPane.ERROR_MESSAGE);
			}
		}

		// save data to file from active OVTK2Viewer
		else if (cmd.equals("save")) {
			if (resources.getSelectedViewer() != null) {
				// ask user which file to save to
				final File file = DesktopUtils.showSaveDialog(new String[] { "oxl", "xml", "gz" });
				if (file != null) {

					// get current viewer
					OVTK2Viewer viewer = (OVTK2Viewer) resources.getSelectedViewer();
					viewer.getONDEXJUNGGraph().updateLastState();

					// trigger export
					DesktopUtils.saveFile(file, viewer);
				}
			} else {
				JOptionPane.showInternalMessageDialog(OVTK2Desktop.getInstance().getDesktopPane(), "No graph loaded or available. Cannot continue with operation.", "Graph not found", JOptionPane.ERROR_MESSAGE);
			}
		}

		// save graph as an image
		else if (cmd.equals("image")) {
			if (resources.getSelectedViewer() != null) {
				File dir = (Config.lastSavedFile == null) ? new File(System.getProperty("user.dir")) : new File(Config.lastSavedFile);
				DialogExport chooser = new DialogExport(dir);
				// chooser.addFormat("graphml");

				int i = chooser.showSaveDialog((OVTK2Viewer) resources.getSelectedViewer());
				if (i == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getFile();
					Config.lastSavedFile = file.getAbsolutePath();
					OVTK2Viewer viewer = (OVTK2Viewer) resources.getSelectedViewer();
					new SVGExport(viewer, file, chooser.getSelectedFormat());
					// ImageWriterUtil<ONDEXConcept, ONDEXRelation> iw = new
					// ImageWriterUtil<ONDEXConcept,
					// ONDEXRelation>(resources.getSelectedViewer());
					// iw.writeImage(file, chooser.getSelectedFormat(),
					// chooser.getScaleFactor());
				}
			} else {
				JOptionPane.showInternalMessageDialog(OVTK2Desktop.getInstance().getDesktopPane(), "No graph loaded or available. Cannot continue with operation.", "Graph not found", JOptionPane.ERROR_MESSAGE);
			}
		}

		// import
		else if (cmd.equals("import")) {

			// list of file extensions of all loaded plugins
			ArrayList<String> extensions = new ArrayList<String>();

			// get extensions of importer plugins
			final Map<String, OVTK2IO> ext2io = new Hashtable<String, OVTK2IO>();
			try {
				Set<String> names = OVTK2PluginLoader.getInstance().getAvailableIOIDs();
				for (String name : names) {
					try {
						OVTK2IO io = OVTK2PluginLoader.getInstance().loadIO(name);
						if (io == null) {
							continue;
						}
						if (io.isImport()) {
							extensions.add(io.getExt());
							ext2io.put(io.getExt(), io);
						}
					} catch (Exception e) {
						ErrorDialog.show(e);
					}
				}
			} catch (FileNotFoundException e) {
				ErrorDialog.show(e);
			} catch (MalformedURLException e) {
				ErrorDialog.show(e);
			}

			// handle error
			if (extensions.size() == 0) {
				JOptionPane.showInternalMessageDialog(desktop.getDesktopPane(), "No importer plugins have been loaded.", "No importer found", JOptionPane.ERROR_MESSAGE);
				return;
			}

			// show file open dialog
			final File file = DesktopUtils.showOpenDialog(extensions.toArray(new String[0]));
			if (file != null) {
				final String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();

				// name of new graph
				String name = file.getName();

				// get new empty graph
				final ONDEXGraph aog = DesktopUtils.initGraph(name);

				// case of Prolog import
				if (ext.equals("pro")) {
					IndeterminateProcessAdapter p = new IndeterminateProcessAdapter() {
						public void task() {

							// for coping with plug-in Attribute data types
							try {
								Thread.currentThread().setContextClassLoader(OVTK2PluginLoader.getInstance().ucl);
							} catch (FileNotFoundException e) {
								ErrorDialog.show(e);
							} catch (MalformedURLException e) {
								ErrorDialog.show(e);
							}

							// location of ondex meta data
							String ondexDir = System.getProperty("ondex.dir");

							File metadata = new File(ondexDir + File.separator + "xml" + File.separator + "ondex_metadata.xml");

							// load meta data
							try {
								OXLImport i = new OXLImport(aog, metadata, true);
								i.start();
							} catch (Exception e) {
								ErrorDialog.show(e);
							}

							// new prolog import
							OVTK2IO prolog = ext2io.get(ext);
							prolog.setGraph(aog);

							try {
								// start import process
								prolog.start(file);
							} catch (Exception e) {
								ErrorDialog.show(e);
							}

							// display the graph
							DesktopUtils.displayGraphOnDesktop(aog);

							// check if appearance attributes present
							AttributeName an = aog.getMetaData().getAttributeName("shape");
							if (aog.getConceptsOfAttributeName(an).size() > 0) {
								int option = JOptionPane.showInternalConfirmDialog(desktop.getDesktopPane(), "Detected appearance attributes. Do you like to load appearance now?", "Load appearance...", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
								if (option == JOptionPane.YES_OPTION) {
									FileMenuAction.getInstance().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "loadappearance"));
								}
							}
						}
					};
					// start processing and monitoring
					p.start();
					OVTKProgressMonitor.start(OVTK2Desktop.getInstance().getMainFrame(), "Importing " + file, p);

					// close possible welcome dialog
					WelcomeDialog.getInstance(desktop).setVisible(false);
				}

				// all other cases
				else if (ext2io.containsKey(ext)) {
					IndeterminateProcessAdapter p = new IndeterminateProcessAdapter() {
						public void task() {

							// for coping with plug-in Attribute data types
							try {
								Thread.currentThread().setContextClassLoader(OVTK2PluginLoader.getInstance().ucl);
							} catch (FileNotFoundException e) {
								ErrorDialog.show(e);
							} catch (MalformedURLException e) {
								ErrorDialog.show(e);
							}

							// new generic import
							OVTK2IO io = ext2io.get(ext);
							io.setGraph(aog);

							try {
								// start import process
								io.start(file);
							} catch (Exception e) {
								ErrorDialog.show(e);
							}

							// display the graph
							DesktopUtils.displayGraphOnDesktop(aog);
						}
					};
					// start processing and monitoring
					p.start();
					OVTKProgressMonitor.start(OVTK2Desktop.getInstance().getMainFrame(), "Importing " + file, p);

					// close possible welcome dialog
					WelcomeDialog.getInstance(desktop).setVisible(false);
				}

				// extension not found
				else {
					JOptionPane.showInternalMessageDialog(desktop.getDesktopPane(), "No importer for this file extension available: " + ext, "No importer matching", JOptionPane.ERROR_MESSAGE);
				}
			}
		}

		// export
		else if (cmd.equals("export")) {
			if (resources.getSelectedViewer() != null) {
				File dir = (Config.lastSavedFile == null) ? new File(System.getProperty("user.dir")) : new File(Config.lastSavedFile);
				DialogExport chooser = new DialogExport(dir);
				chooser.removeAllFormats();

				// get current viewer
				final OVTK2Viewer viewer = (OVTK2Viewer) resources.getSelectedViewer();

				// get extensions of exporter plugins
				final Map<String, OVTK2IO> ext2io = new Hashtable<String, OVTK2IO>();
				try {
					Set<String> names = OVTK2PluginLoader.getInstance().getAvailableIOIDs();
					for (String name : names) {
						try {
							OVTK2IO io = OVTK2PluginLoader.getInstance().loadIO(name);
							if (io == null) {
								continue;
							}
							if (!io.isImport()) {
								chooser.addFormat(io.getExt());
								ext2io.put(io.getExt(), io);
							}
						} catch (Exception e) {
							ErrorDialog.show(e);
						}
					}
				} catch (FileNotFoundException e) {
					ErrorDialog.show(e);
				} catch (MalformedURLException e) {
					ErrorDialog.show(e);
				}

				// handle error
				if (ext2io.size() == 0) {
					JOptionPane.showInternalMessageDialog(desktop.getDesktopPane(), "No exporter plugins have been loaded.", "No exporter found", JOptionPane.ERROR_MESSAGE);
					return;
				}

				int i = chooser.showSaveDialog((OVTK2Viewer) resources.getSelectedViewer());
				if (i == JFileChooser.APPROVE_OPTION) {
					final File file = chooser.getFile();

					if (file.exists() && Boolean.parseBoolean(Config.config.getProperty("Overwrite.Set"))) {
						int answer = JOptionPane.showInternalConfirmDialog(OVTK2Desktop.getInstance().getDesktopPane(), Config.language.getProperty("Dialog.Save.Warning.Text"), Config.language.getProperty("Dialog.Save.Warning.Title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
						if (answer == JOptionPane.NO_OPTION)
							return;
					}

					Config.lastSavedFile = file.getAbsolutePath();
					final String selection = chooser.getSelectedFormat();

					IndeterminateProcessAdapter p = new IndeterminateProcessAdapter() {
						public void task() {

							// for coping with plug-in Attribute data types
							try {
								Thread.currentThread().setContextClassLoader(OVTK2PluginLoader.getInstance().ucl);
							} catch (FileNotFoundException e) {
								ErrorDialog.show(e);
							} catch (MalformedURLException e) {
								ErrorDialog.show(e);
							}

							// graph to export
							ONDEXGraph aog = viewer.getONDEXJUNGGraph();

							// new generic export
							OVTK2IO io = ext2io.get(selection);
							io.setGraph(aog);

							try {
								// start export process
								io.start(file);
							} catch (Exception e) {
								ErrorDialog.show(e);
							}
						}
					};
					// start processing and monitoring
					p.start();
					OVTKProgressMonitor.start(OVTK2Desktop.getInstance().getMainFrame(), "Exporting file", p);
				}
			} else {
				JOptionPane.showInternalMessageDialog(OVTK2Desktop.getInstance().getDesktopPane(), "No graph loaded or available. Cannot continue with operation.", "Graph not found", JOptionPane.ERROR_MESSAGE);
			}
		}

		// print visible graph
		else if (cmd.equals("print")) {
			if (resources.getSelectedViewer() != null) {
				// setup new print job
				final PrinterJob printJob = PrinterJob.getPrinterJob();
				printJob.setPrintable((OVTK2Viewer) resources.getSelectedViewer());
				if (printJob.printDialog()) {
					IndeterminateProcessAdapter p = new IndeterminateProcessAdapter() {
						public void task() {
							try {
								// start print job
								printJob.print();
							} catch (Exception ex) {
								ErrorDialog.show(ex);
							}
						}
					};
					// start processing and monitoring
					p.start();
					OVTKProgressMonitor.start(OVTK2Desktop.getInstance().getMainFrame(), "Printing...", p);
				}
			} else {
				JOptionPane.showInternalMessageDialog(OVTK2Desktop.getInstance().getDesktopPane(), "No graph loaded or available. Cannot continue with operation.", "Graph not found", JOptionPane.ERROR_MESSAGE);
			}
		}

		// check for unsaved data before exit
		else if (cmd.equals("exit")) {
			// if there is at least one open graph ask to really exit
			if (resources.getSelectedViewer() != null) {
				int option = JOptionPane.showInternalConfirmDialog(desktop.getDesktopPane(), Config.language.getProperty("Dialog.Exit.Warning.Text"), Config.language.getProperty("Dialog.Exit.Warning.Title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (option == JOptionPane.NO_OPTION) {
					return;
				}
			}
			// save entries for next session
			OVTK2Menu menu = (OVTK2Menu) desktop.getMainFrame().getJMenuBar();
			menu.fileHistory.saveHistoryEntries();
			Config.saveConfig();
			System.exit(0);
		}
	}
}
