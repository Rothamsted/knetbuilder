package net.sourceforge.ondex.ovtk2.ui.popup.custom;

import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.console.OVTKScriptingInitialiser;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;
import net.sourceforge.ondex.ovtk2.ui.popup.VertexMenu;
import net.sourceforge.ondex.ovtk2.util.OVTKProgressMonitor;
import net.sourceforge.ondex.scripting.ui.CommandEvent;
import net.sourceforge.ondex.scripting.ui.CommandLine;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;
import net.sourceforge.ondex.tools.threading.monitoring.SimpleMonitor;

public class CustomPopupItem extends EntityMenuItem<ONDEXConcept> {

	private class CustomPopupHelper extends EntityMenuItem<ONDEXConcept> {

		private CustomPopupItemBean itemBean;
		private final File xmlFile;

		public CustomPopupHelper(File file) {
			super();
			this.xmlFile = file;
			try {
				itemBean = CustomPopupItemBean.loadXML(xmlFile);
				item.setText(itemBean.getName());
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(item, "custom popup item file not found in customPopup/ directory");
				e.printStackTrace();
			} catch (NoSuchElementException e) {
				JOptionPane.showMessageDialog(item, "custom popup item xml has bad format");
				e.printStackTrace();
			}
		}

		public CustomPopupHelper(URL url) {
			super();
			xmlFile = null;
			try {
				itemBean = CustomPopupItemBean.loadXML(url);
				item.setText(itemBean.getName());
			} catch (NoSuchElementException e) {
				JOptionPane.showMessageDialog(item, "custom popup item xml has bad format");
				e.printStackTrace();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(item, "custom popup item file not found in customPopup/ directory");
				e.printStackTrace();
			}
		}

		@Override
		public boolean accepts() {
			for (ONDEXConcept n : entities) {
				ConceptClass cc = n.getOfType();
				if (isAcceptedConceptClass(cc))
					return true;
			}
			return false;
		}

		@Override
		protected void doAction() {

			// deselect all concepts that don't have a matching CC
			List<ONDEXConcept> list = new ArrayList<ONDEXConcept>();
			for (ONDEXConcept concept : viewer.getPickedNodes()) {
				if (!isAcceptedConceptClass(concept.getOfType()))
					list.add(concept);
			}
			for (ONDEXConcept concept : list)
				viewer.getVisualizationViewer().getPickedVertexState().pick(concept, false);

			// run in a new thread (avoid time consuming operations in AWT
			// EventQueue)
			new Thread(new Runnable() {
				@Override
				public void run() {
					// start up command interpreter
					final CommandLine c = (CommandLine) OVTKScriptingInitialiser.getCommandLine(viewer);
					final SimpleMonitor monitor = new SimpleMonitor("Initializing command line...", 3);

					OVTKProgressMonitor.start((Frame) parent, "Custom Popup", monitor);

					while (!c.isCommandInterpreterReady()) {
						// user cancelled operation?
						if (monitor.getState() == Monitorable.STATE_TERMINAL)
							return;
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// ignore
						}
					}

					// run "library" files
					monitor.next("running libraries");
					String[] libraries = itemBean.getLibraries().split(",");
					for (String library : libraries) {
						if ("".equals(library.trim()))
							continue;

						// different case when running in applet
						String popupPath;
						if (Config.isApplet) {
							popupPath = Config.ovtkDir + "/popupMenu/";
						} else {
							popupPath = CustomPopupItemBean.getPopupPath().toString() + File.separatorChar;
						}

						// load libraries
						System.out.println("Trying to load additional library: " + popupPath + library.trim());
						c.executeJavaScriptFile(popupPath + library.trim());
						c.waitForCommandCompletion();
					}

					monitor.next("running popup item script");
					c.printAndPrompt(itemBean.getCode());
					c.fireCommandEvent(new CommandEvent(this, itemBean.getCode(), c));
					c.waitForCommandCompletion();

					monitor.complete();

				}

			}).start();

		}

		public String getAcceptedConceptClasses() {
			return itemBean.getConceptClasses();
		}

		@Override
		public MENUCATEGORY getCategory() {
			return null;
		}

		@Override
		protected String getMenuPropertyName() {
			// LATER: language support for custom popups
			return "";
		}

		/**
		 * @return The full qualified name of this item, that is:
		 *         {@link CustomPopupItemBean#getQualifiedName()}
		 */
		public String getQualifiedName() {
			return itemBean.getName();
		}

		@Override
		protected String getUndoPropertyName() {
			return null;
		}

		public boolean isAcceptedConceptClass(ConceptClass cc) {
			String[] itemCCs = itemBean.getConceptClasses().split(",");
			for (String itemCC : itemCCs)
				if ("".equals(itemCC.trim()) || cc.getId().equals(itemCC.trim()))
					return true;
			return false;
		}
	}

	private Frame parent;

	public CustomPopupItem(Frame parent) {
		super();
		this.parent = parent;
		item = new JMenu("Custom Popups");
	}

	@Override
	public boolean accepts() {
		boolean accepts = false;

		// special case for applet
		if (Config.isApplet) {
			if (Config.config.getProperty("PopupEditor.Scripts") != null) {
				// System.out.println(Config.config
				// .getProperty("PopupEditor.Scripts"));
				String[] paths = Config.config.getProperty("PopupEditor.Scripts").split(",");
				for (String p : paths) {
					try {
						URL url = new URL(Config.ovtkDir + "/popupMenu/" + p);
						// System.out.println(url.toString());
						CustomPopupHelper menuItem = new CustomPopupHelper(url);
						menuItem.init(viewer, entities);
						if (menuItem.accepts()) {
							addToCustomPopup(menuItem);
							accepts = true;
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			for (File file : CustomPopupItemBean.getAvailablePopupItemFiles()) {
				// only add those that have a matching CC
				CustomPopupHelper menuItem = new CustomPopupHelper(file);
				menuItem.init(viewer, entities);
				if (menuItem.accepts()) {
					addToCustomPopup(menuItem);
					accepts = true;
				}
			}
		}

		return accepts;
	}

	/**
	 * Adds the given {@link CustomPopupHelper} to the JMenu nesting according
	 * to the subdirectory of its xml file.
	 * 
	 * @param menuItem
	 *            {@link CustomPopupHelper} to add to this {@link VertexMenu}.
	 */
	private void addToCustomPopup(CustomPopupHelper menuItem) {
		String regex = String.valueOf(File.separatorChar);
		if (regex.equals("\\"))
			regex = "\\\\";
		// special case for applet URL processing
		if (Config.isApplet)
			regex = "/";

		String[] split = menuItem.getQualifiedName().split(regex);

		// add [CC] information
		String cc = menuItem.getAcceptedConceptClasses();
		if ("".equals(cc))
			split[split.length - 1] = split[split.length - 1] + " [ all ]  ";
		else
			split[split.length - 1] = split[split.length - 1] + " [ " + menuItem.getAcceptedConceptClasses() + " ]  ";

		// put same subdirs into same menu containers
		JMenuItem container = null;
		MenuElement[] subs = item.getSubElements();
		if (subs.length == 1)
			subs = subs[0].getSubElements();
		int i;
		for (i = 0; i < split.length - 1; i++) {
			boolean didFindSubMenu = false;
			for (MenuElement sub : subs) {
				if (sub instanceof JMenu) {
					if (((JMenu) sub).getText().equals(split[i])) {
						container = (JMenu) sub;
						didFindSubMenu = true;
					}
				}
			}
			if (!didFindSubMenu)
				break;
			subs = container.getSubElements();
			if (subs[0] instanceof JPopupMenu)
				subs = subs[0].getSubElements();
		}

		// add missing submenus
		for (; i < split.length - 1; i++) {
			JMenu submenu = new JMenu(split[i]);
			if (container == null) {
				item.add(submenu);
			} else {
				container.add(submenu);
			}
			container = submenu;
		}

		// finally, container is parent of leaf
		menuItem.getItem().setText(split[i]);
		if (container == null)
			item.add(menuItem.getItem());
		else
			container.add(menuItem.getItem());

	}

	@Override
	protected void doAction() {
		// nothing
	}

	@Override
	public MENUCATEGORY getCategory() {
		return null;
	}

	@Override
	protected String getMenuPropertyName() {
		return "";
	}

	@Override
	protected String getUndoPropertyName() {
		return null;
	}
}
