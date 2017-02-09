package net.sourceforge.ondex.ovtk2.ui.popup;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.QuerySetParser;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.config.OVTK2PluginLoader;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * Menu shown on right click on edges.
 * 
 * @author taubertj
 * 
 */
public class EdgeMenu extends JPopupMenu implements EdgeMenuListener<ONDEXConcept, ONDEXRelation>, MenuPointListener {

	private static final boolean DEBUG = false;

	// generated
	private static final long serialVersionUID = -7378048264697994730L;

	private DialogRelationItem relationInfo = null;

	private OVTK2Viewer viewer = null;

	/**
	 * Constructor for a given OVTK2Viewer.
	 * 
	 * @param viewer
	 *            OVTK2Viewer
	 */
	public EdgeMenu(OVTK2Viewer viewer) {
		this.viewer = viewer;
	}

	private Set<ONDEXRelation> getMultipleEdges(OVTK2Viewer viewer, ONDEXRelation edge) {
		Set<ONDEXRelation> pickedEdges = viewer.getPickedEdges();
		if (!pickedEdges.contains(edge))
			pickedEdges = Collections.singleton(edge);
		return pickedEdges;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setEdgeAndView(ONDEXRelation e, VisualizationViewer<ONDEXConcept, ONDEXRelation> visComp) {

		removeAll();

		// new change menu
		JMenu change = new JMenu(Config.language.getProperty("Viewer.EdgeMenu.ChangeBy"));

		// new hide menu
		JMenu hide = new JMenu(Config.language.getProperty("Viewer.EdgeMenu.HideBy"));

		// new show menu
		JMenu show = new JMenu(Config.language.getProperty("Viewer.EdgeMenu.ShowBy"));

		Set<ONDEXRelation> edges = getMultipleEdges(viewer, e);

		// add menu items from config.xml file
		ArrayList<String> entries = new ArrayList<String>();
		Enumeration<?> enu = Config.config.propertyNames();
		while (enu.hasMoreElements()) {
			String name = (String) enu.nextElement();
			if (name.startsWith("Viewer.EdgeMenu.")) {
				entries.add(name);
			}
		}

		// for coping with plug-in Attribute data types
		try {
			Thread.currentThread().setContextClassLoader(OVTK2PluginLoader.getInstance().ucl);
		} catch (FileNotFoundException fnfe) {
			ErrorDialog.show(fnfe);
		} catch (MalformedURLException mue) {
			ErrorDialog.show(mue);
		}

		// order filters by name
		String[] ordered = entries.toArray(new String[entries.size()]);
		Arrays.sort(ordered);
		try {
			for (String name : ordered) {
				String clazz = Config.config.getProperty(name);
				// get new instance of menu item
				int index = clazz.lastIndexOf(".");
				name = clazz.substring(index + 1, clazz.length());
				if (DEBUG)
					System.err.println("class mapping: " + name + " ->  " + clazz);
				EntityMenuItem<ONDEXRelation> item = OVTK2PluginLoader.getInstance().loadPopupItem(name);
				item.init(viewer, edges);
				if (item.accepts()) {
					switch (item.getCategory()) {
					case HIDE:
						hide.add(item.getItem());
						break;
					case SHOW:
						show.add(item.getItem());
						break;
					case CHANGE:
						change.add(item.getItem());
						break;
					}
				}
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		// add non-empty menus to popup
		if (change.getSubElements().length > 0)
			add(change);
		if (hide.getSubElements().length > 0)
			add(hide);
		if (show.getSubElements().length > 0)
			add(show);

		addSeparator();

		// always present
		relationInfo = new DialogRelationItem();
		relationInfo.init(viewer, edges);
		add(relationInfo.getItem());

		// SWAT4LS - 2010 demo
		boolean selectedRelations = (viewer.getPickedEdges().size() != 0 || edges != null) ? true : false;
		;
		boolean selectedConcepts = viewer.getPickedNodes().size() == 0 ? false : true;

		boolean empty = true;
		JMenu querry = new JMenu("Query");
		File f = new File(Config.ovtkDir + "/sparql.ini");
		Set<String> layoutAndCenter = new HashSet<String>();
		if (f.exists()) {
			Map<String, List<String>> options = new HashMap<String, List<String>>();
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
				String line;
				String name = null;
				List<String> commands = new ArrayList<String>();
				StringBuffer command = new StringBuffer();
				boolean readingCommand = false;
				while ((line = br.readLine()) != null) {
					if (line.startsWith("#ITEM")) {
						boolean add = line.contains("*");
						readingCommand = false;
						line = br.readLine();
						name = line.trim();
						if (add) {
							layoutAndCenter.add(name);
						}
					} else if (line.startsWith("#COMMAND")) {
						if (command.length() > 0) {
							commands.add(command.toString());
							command = new StringBuffer();
						}
						readingCommand = true;
					} else if (line.startsWith("#END_ITEM")) {
						commands.add(command.toString());
						command = new StringBuffer();
						options.put(name, commands);
						name = null;
						commands = new ArrayList<String>();
						readingCommand = false;
					} else if (readingCommand) {
						command.append(" ");
						command.append(line);
					}
				}
				Map<String, List<String>> edgeOptions = new HashMap<String, List<String>>();
				Map<String, List<String>> nodeOptions = new HashMap<String, List<String>>();
				Map<String, String> cvToUrl = new TreeMap<String, String>();
				try {
					Class<?> clsInterp = Thread.currentThread().getContextClassLoader().loadClass("net.sourceforge.ondex.scripting.sparql.SPARQLInterpreter");
					Object instanceInterp = clsInterp.getMethod("getCurrentInstance", new Class<?>[0]).invoke(clsInterp, new Object[0]);
					if (instanceInterp != null) {
						boolean success = (Boolean) clsInterp.getMethod("configure", new Class<?>[0]).invoke(instanceInterp, new Object[0]);
						if (!success)
							throw new Exception("Could not configure SPARQL interpreter!");
						QuerySetParser qs = (QuerySetParser) clsInterp.getMethod("getQuerySetParser", new Class<?>[0]).invoke(instanceInterp, new Object[0]);
						File file = new File(qs.getQuerySetLocation().getAbsolutePath() + File.separator + "uri.sqs");
						if (file.exists()) {
							BufferedReader br1 = new BufferedReader(new FileReader(file));
							while ((line = br1.readLine()) != null) {
								if (line.startsWith("#")) {
									continue;
								}
								if (!line.contains("\t")) {
									continue;
								}
								String[] temp = line.split("\t");
								cvToUrl.put(temp[0], temp[1]);
							}
							br1.close();
						}
					}
				} catch (Exception e1) {
					if (DEBUG) {
						e1.printStackTrace();
					}
				}

				for (Entry<String, List<String>> ent : options.entrySet()) {
					boolean isEdge = false;
					boolean isNode = false;
					for (String s : ent.getValue()) {
						if (EntityURIMenuItem.PRIMARY_URI.matcher(s).find()) {
							isNode = true;
						} else if (EntityURIMenuItem.FROM_URI.matcher(s).find()) {
							isEdge = true;
						} else if (EntityURIMenuItem.TO_URI.matcher(s).find()) {
							isEdge = true;
						} else if (EntityURIMenuItem.TYPE.matcher(s).find()) {
							isEdge = true;
						}
					}
					if (isEdge) {
						edgeOptions.put(ent.getKey(), ent.getValue());
					} else if (isNode) {
						nodeOptions.put(ent.getKey(), ent.getValue());
					}

				}

				if (selectedConcepts) {
					for (Entry<String, List<String>> ent : nodeOptions.entrySet()) {
						EntityURIMenuItem vm = EntityURIMenuItem.getMenuItem(viewer, null, e, ent.getKey(), ent.getValue(), Collections.EMPTY_LIST, layoutAndCenter, cvToUrl);
						if (vm != null) {
							empty = false;
							querry.add(vm);
						}
					}
				}
				if (selectedConcepts && selectedRelations) {
					querry.addSeparator();
				}
				if (selectedRelations) {
					for (Entry<String, List<String>> ent : edgeOptions.entrySet()) {
						EntityURIMenuItem vm = EntityURIMenuItem.getMenuItem(viewer, null, e, ent.getKey(), Collections.EMPTY_LIST, ent.getValue(), layoutAndCenter, cvToUrl);
						if (vm != null) {
							empty = false;
							querry.add(vm);
						}
					}
				}
				if (selectedConcepts) {
					querry.addSeparator();
					EntityURIMenuItem vm = VertexURLResolverMenuItem.getMenuItem(viewer, null, e, "Resolve URL");
					querry.add(vm);
				}

				if (!empty) {
					add(querry);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void setPoint(Point point) {
		relationInfo.setPoint(point);
	}
}
