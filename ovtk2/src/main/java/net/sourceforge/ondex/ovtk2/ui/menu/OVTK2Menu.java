package net.sourceforge.ondex.ovtk2.ui.menu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.SwingHelpUtilities;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;

import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.config.OVTK2PluginLoader;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeColors;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeColors.EdgeColorSelection;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeShapes;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeFillPaint;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeFillPaint.NodeFillPaintSelection;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes.NodeShapeSelection;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2MenuBar;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.ui.dialog.WelcomeDialog;
import net.sourceforge.ondex.ovtk2.ui.menu.FileHistory.IFileHistory;
import net.sourceforge.ondex.ovtk2.ui.menu.actions.AnnotatorMenuAction;
import net.sourceforge.ondex.ovtk2.ui.menu.actions.AppearanceMenuAction;
import net.sourceforge.ondex.ovtk2.ui.menu.actions.EditMenuAction;
import net.sourceforge.ondex.ovtk2.ui.menu.actions.FileMenuAction;
import net.sourceforge.ondex.ovtk2.ui.menu.actions.FilterMenuAction;
import net.sourceforge.ondex.ovtk2.ui.menu.actions.HelpMenuAction;
import net.sourceforge.ondex.ovtk2.ui.menu.actions.LayoutMenuAction;
import net.sourceforge.ondex.ovtk2.ui.menu.actions.NoTavernaMenuAction;
import net.sourceforge.ondex.ovtk2.ui.menu.actions.SelectingMenuAction;
import net.sourceforge.ondex.ovtk2.ui.menu.actions.ToolMenuAction;
import net.sourceforge.ondex.ovtk2.ui.menu.actions.ViewMenuAction;
import net.sourceforge.ondex.ovtk2.ui.mouse.OVTK2GraphMouse;
import net.sourceforge.ondex.ovtk2.ui.mouse.OVTK2PickingMousePlugin;
import net.sourceforge.ondex.ovtk2.ui.toolbars.OVTK2ToolBar;
import net.sourceforge.ondex.ovtk2.util.DesktopUtils;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.taverna.TavernaApi;

/**
 * Class creates main OVTK2 menu bar.
 * 
 * @author taubertj
 * @version 08.02.2010
 */
public class OVTK2Menu extends JMenuBar implements IFileHistory, OVTK2MenuBar {

	/**
	 * Sorts array alphabetically via lookup values in map
	 * 
	 * @author canevetc
	 * 
	 */
	class MapSorter implements Comparator<String> {

		private Map<String, String> map;

		public MapSorter(Map<String, String> map) {
			this.map = map;
		}

		@Override
		public int compare(String o1, String o2) {
			return map.get(o1).compareTo(map.get(o2));
		}

	}

	/**
	 * generated
	 */
	private static final long serialVersionUID = -4377849284840562245L;

	/**
	 * more excessive output
	 */
	private static boolean DEBUG = false;

	/**
	 * internal reference to OVTK2Desktop
	 */
	private OVTK2Desktop desktop = null;

	/**
	 * History of opened Files
	 */
	public FileHistory fileHistory;

	/**
	 * File menu with history function
	 */
	private JMenu fileMenu;

	/**
	 * delegate mouse clicks to help pages
	 */
	private CustomMouseListener mouseListener = null;

	/**
	 * displays undo message
	 */
	public JMenuItem undo = null;

	/**
	 * displays redo message
	 */
	public JMenuItem redo = null;

	/**
	 * Undo all possible undo's
	 */
	public JMenuItem undoAll = null;

	/**
	 * Redo all possible redo's
	 */
	public JMenuItem redoAll = null;

	/**
	 * Revert visibility to last save
	 */
	public JMenuItem revert = null;

	/**
	 * Setup the menubar for a given OVTK2Desktop.
	 * 
	 * @param desktop
	 *            current OVTK2Desktop acting as ActionListener
	 */
	public OVTK2Menu(OVTK2Desktop desktop) {

		// reference need for menu item creation functions
		this.desktop = desktop;

		// used for question marks
		mouseListener = new CustomMouseListener();

		// file menu
		fileMenu = makeMenu("Menu.File");
		this.add(fileMenu);
		populateFileMenu(fileMenu);

		// edit menu
		JMenu edit = makeMenu("Menu.Edit");
		this.add(edit);
		populateEditMenu(edit);

		// view menu
		JMenu view = makeMenu("Menu.View");
		this.add(view);
		populateViewMenu(view);

		// appearance sub-menu
		JMenu appearance = makeMenu("Menu.Appearance");
		this.add(appearance);
		populateAppearanceMenu(appearance);

		// tools menu
		JMenu tools = makeMenu("Menu.Tools");
		this.add(tools);
		populateToolsMenu(tools);

		// taverna menu
		addTavernaMenu();

		// help menu
		JMenu help = makeMenu("Menu.Help");
		this.add(help);
		populateHelpMenu(help);
	}

	@Override
	public String getApplicationName() {
		return "ovtk2";
	}

	@Override
	public JMenu getFileMenu() {
		return fileMenu;
	}

	@Override
	public JFrame getParentFrame() {
		return desktop.getMainFrame();
	}

	@Override
	public Dimension getParentSize() {
		return desktop.getMainFrame().getSize();
	}

	@Override
	public void loadFile(String pathname) {
		File file = new File(pathname);
		DesktopUtils.openFile(file);
		// close possible welcome dialog
		WelcomeDialog.getInstance(desktop).setVisible(false);
	}

	/**
	 * Create a JCheckBoxMenuItem labelled with text for the given property key.
	 * 
	 * @param key
	 *            property key
	 * @param actionCommand
	 *            internal command
	 * @return JCheckBoxMenuItem
	 */
	private JCheckBoxMenuItem makeCheckBoxMenuItem(String key, String actionCommand) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(Config.language.getProperty(key));
		item.setActionCommand(actionCommand);
		item.addActionListener(desktop);
		return item;
	}

	/**
	 * Create a JMenu labelled with text for the given property key.
	 * 
	 * @param key
	 *            property key
	 * @return JMenu
	 */
	private JMenu makeMenu(String key) {
		JMenu menu = new JMenu(Config.language.getProperty(key));
		String value = Config.language.getProperty(key);
		if (value == null) {
			throw new RuntimeException("Key \"" + key + "\" is missing from the language file");
		}
		menu.setMnemonic(value.charAt(0));
		return menu;
	}

	/**
	 * Create a JMenuItem labelled with text for the given property key.
	 * 
	 * @param key
	 *            property key
	 * @param actionCommand
	 *            internal command
	 * @return JMenuItem
	 */
	private JMenuItem makeMenuItem(String key, String actionCommand) {
		String value = Config.language.getProperty(key);
		if (value == null) {
			throw new RuntimeException("Key \"" + key + "\" is missing from the language file");
		}
		JMenuItem item = new JMenuItem(value);
		item.setActionCommand(actionCommand);
		item.addActionListener(desktop);
		return item;
	}

	/**
	 * Populates the annotation menu.
	 * 
	 * @param anno
	 *            JMenu "Annotator submenu"
	 * @throws MalformedURLException
	 */
	private void populateAnnoMenu(JMenu anno) throws MalformedURLException {

		Set<String> exceptions = new HashSet<String>();
		exceptions.add("net.sourceforge.ondex.ovtk2.annotator.scalecolorconcept.ScaleColorConceptAnnotator");
		exceptions.add("net.sourceforge.ondex.ovtk2.annotator.scaleconcept.ScaleConceptAnnotator");
		exceptions.add("net.sourceforge.ondex.ovtk2.annotator.colorcategory.ColorCategoryAnnotator");
		exceptions.add("net.sourceforge.ondex.ovtk2.annotator.scalecolorrelation.ScaleColorRelationAnnotator");
		exceptions.add("net.sourceforge.ondex.ovtk2.annotator.shapeconcept.ShapeConceptAnnotator");

		// make sure annotator menu action is handled
		AnnotatorMenuAction listener = new AnnotatorMenuAction();
		desktop.addActionListener(listener);
		desktop.addInternalFrameListener(listener);

		// add annotators from config.xml file
		ArrayList<String> entries = new ArrayList<String>();
		Enumeration<?> enu = Config.config.propertyNames();
		while (enu.hasMoreElements()) {
			String name = (String) enu.nextElement();
			if (name.startsWith("Menu.Annotator.")) {
				entries.add(name);
			}
			if (name.startsWith("Menu.Analysis.")) {
				entries.add(name);
			}
		}

		// question icon
		String path = "config/themes/" + Config.config.getProperty("Program.Theme") + "/icons/question.png";

		// order annotators by name
		String[] ordered = entries.toArray(new String[entries.size()]);

		HashMap<String, String> realNameToDisplayName = new HashMap<String, String>();
		for (String name : ordered) {
			String display = Config.language.getProperty("Name." + name);
			realNameToDisplayName.put(name, display);
		}

		Arrays.sort(ordered, new MapSorter(realNameToDisplayName));

		JMenu more = makeMenu("Menu.Annotator.More");

		for (String name : ordered) {
			String clazz = Config.config.getProperty(name);
			try {
				if (!OVTK2PluginLoader.getInstance().getAnnotatorClassNames().contains(clazz)) {
					if (!exceptions.contains(clazz))
						;
					// continue;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace(); // TODO: log?
			}

			String help = Config.language.getProperty("Help." + name);
			String display = realNameToDisplayName.get(name);
			JMenuItem item;
			if (help != null)
				item = new CustomJMenuItem(display, path, help);
			else
				item = new JMenuItem(display);
			item.setActionCommand(name);
			item.addActionListener(desktop);
			item.addMouseListener(mouseListener);
			if (!exceptions.contains(clazz)) {
				more.add(item);
			} else {
				anno.add(item);
			}
		}

		anno.add(more);
	}

	/**
	 * Populates the appearance sub-menu for tools.
	 * 
	 * @param appearance
	 *            JMenu "Appearance submenu"
	 */
	private void populateAppearanceMenu(JMenu appearance) {

		// make sure appearance menu action is handled
		AppearanceMenuAction listener = new AppearanceMenuAction();
		desktop.addActionListener(listener);

		// labels sub-menu
		JMenu labels = makeMenu("Menu.Appearance.Labels");
		appearance.add(labels);

		JCheckBoxMenuItem nodelabels = makeCheckBoxMenuItem("Menu.Appearance.ConceptLabels", "nodelabels");
		labels.add(nodelabels);

		JCheckBoxMenuItem edgelabels = makeCheckBoxMenuItem("Menu.Appearance.RelationLabels", "edgelabels");
		labels.add(edgelabels);

		JCheckBoxMenuItem bothlabels = makeCheckBoxMenuItem("Menu.Appearance.BothLabels", "bothlabels");
		labels.add(bothlabels);

		// layout sub-menu
		JMenu layout = makeMenu("Menu.Layout");
		appearance.add(layout);
		try {
			populateLayoutMenu(layout);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// sub-menu for colour concepts
		JMenu subMenuConceptColor = new JMenu(Config.language.getProperty("Menu.Appearance.ColourConcepts"));
		ButtonGroup groupConcept = new ButtonGroup();
		JRadioButtonMenuItem colorConceptOnDS = new JRadioButtonMenuItem(Config.language.getProperty("Menu.Appearance.Default.ColorOnCV"));
		colorConceptOnDS.setActionCommand(AppearanceMenuAction.COLOR_CONCEPT_BY_SOURCE);
		colorConceptOnDS.addActionListener(desktop);
		colorConceptOnDS.setSelected(Config.visual.getProperty(ONDEXNodeFillPaint.GRAPH_COLORING_CONCEPT_STRATEGY).equals(ONDEXNodeFillPaint.DS));
		groupConcept.add(colorConceptOnDS);
		subMenuConceptColor.add(colorConceptOnDS);
		JRadioButtonMenuItem colorConceptOnCC = new JRadioButtonMenuItem(Config.language.getProperty("Menu.Appearance.Default.ColorOnCC"));
		colorConceptOnCC.setActionCommand(AppearanceMenuAction.COLOR_CONCEPT_BY_CLASS);
		colorConceptOnCC.addActionListener(desktop);
		colorConceptOnCC.setSelected(Config.visual.getProperty(ONDEXNodeFillPaint.GRAPH_COLORING_CONCEPT_STRATEGY).equals(ONDEXNodeFillPaint.CC));
		groupConcept.add(colorConceptOnCC);
		subMenuConceptColor.add(colorConceptOnCC);
		JRadioButtonMenuItem colorConceptOnET = new JRadioButtonMenuItem(Config.language.getProperty("Menu.Appearance.Default.ColorOnET"));
		colorConceptOnET.setActionCommand(AppearanceMenuAction.COLOR_CONCEPT_BY_EVIDENCE);
		colorConceptOnET.addActionListener(desktop);
		colorConceptOnET.setSelected(Config.visual.getProperty(ONDEXNodeFillPaint.GRAPH_COLORING_CONCEPT_STRATEGY).equals(ONDEXNodeFillPaint.ET));
		groupConcept.add(colorConceptOnET);
		subMenuConceptColor.add(colorConceptOnET);
		appearance.add(subMenuConceptColor);

		// sub-menu for colour relations
		JMenu subMenuRelationColor = new JMenu(Config.language.getProperty("Menu.Appearance.ColourRelations"));
		ButtonGroup groupRelation = new ButtonGroup();
		JRadioButtonMenuItem colorRelationOnRT = new JRadioButtonMenuItem(Config.language.getProperty("Menu.Appearance.Default.ColorRelOnRT"));
		colorRelationOnRT.setActionCommand(AppearanceMenuAction.COLOR_RELATION_BY_TYPE);
		colorRelationOnRT.addActionListener(desktop);
		colorRelationOnRT.setSelected(Config.visual.getProperty(ONDEXEdgeColors.GRAPH_COLORING_RELATION_STRATEGY).equals(ONDEXEdgeColors.RT));
		groupRelation.add(colorRelationOnRT);
		subMenuRelationColor.add(colorRelationOnRT);
		JRadioButtonMenuItem colorRelationOnET = new JRadioButtonMenuItem(Config.language.getProperty("Menu.Appearance.Default.ColorRelOnET"));
		colorRelationOnET.setActionCommand(AppearanceMenuAction.COLOR_RELATION_BY_EVIDECE);
		colorRelationOnET.addActionListener(desktop);
		colorRelationOnET.setSelected(Config.visual.getProperty(ONDEXEdgeColors.GRAPH_COLORING_RELATION_STRATEGY).equals(ONDEXEdgeColors.ET));
		groupRelation.add(colorRelationOnET);
		subMenuRelationColor.add(colorRelationOnET);
		appearance.add(subMenuRelationColor);

		// sub-menu for shape relations
		JMenu subMenuEdgeShapes = new JMenu(Config.language.getProperty("Menu.Appearance.ShapeRelations"));
		ButtonGroup groupShape = new ButtonGroup();
		JRadioButtonMenuItem shapeQuad = new JRadioButtonMenuItem(Config.language.getProperty("Menu.Appearance.Default.ShapeQuad"));
		shapeQuad.setActionCommand(AppearanceMenuAction.SHAPE_QUAD);
		shapeQuad.addActionListener(desktop);
		shapeQuad.setSelected(Config.visual.getProperty(ONDEXEdgeShapes.GRAPH_EDGE_SHAPES).equals(ONDEXEdgeShapes.KEYQUAD));
		groupShape.add(shapeQuad);
		subMenuEdgeShapes.add(shapeQuad);
		JRadioButtonMenuItem shapeCubic = new JRadioButtonMenuItem(Config.language.getProperty("Menu.Appearance.Default.ShapeCubic"));
		shapeCubic.setActionCommand(AppearanceMenuAction.SHAPE_CUBIC);
		shapeCubic.addActionListener(desktop);
		shapeCubic.setSelected(Config.visual.getProperty(ONDEXEdgeShapes.GRAPH_EDGE_SHAPES).equals(ONDEXEdgeShapes.KEYCUBIC));
		groupShape.add(shapeCubic);
		subMenuEdgeShapes.add(shapeCubic);
		JRadioButtonMenuItem shapeBent = new JRadioButtonMenuItem(Config.language.getProperty("Menu.Appearance.Default.ShapeBent"));
		shapeBent.setActionCommand(AppearanceMenuAction.SHAPE_BENT);
		shapeBent.addActionListener(desktop);
		shapeBent.setSelected(Config.visual.getProperty(ONDEXEdgeShapes.GRAPH_EDGE_SHAPES).equals(ONDEXEdgeShapes.KEYBENT));
		groupShape.add(shapeBent);
		subMenuEdgeShapes.add(shapeBent);
		JRadioButtonMenuItem shapeLine = new JRadioButtonMenuItem(Config.language.getProperty("Menu.Appearance.Default.ShapeLine"));
		shapeLine.setActionCommand(AppearanceMenuAction.SHAPE_LINE);
		shapeLine.addActionListener(desktop);
		shapeLine.setSelected(Config.visual.getProperty(ONDEXEdgeShapes.GRAPH_EDGE_SHAPES).equals(ONDEXEdgeShapes.KEYLINE));
		groupShape.add(shapeLine);
		subMenuEdgeShapes.add(shapeLine);

		JCheckBoxMenuItem edgearrow = makeCheckBoxMenuItem("Menu.Appearance.EdgeArrow", "edgearrow");
		edgearrow.setSelected(true);
		subMenuEdgeShapes.add(edgearrow);
		appearance.add(subMenuEdgeShapes);

		JCheckBoxMenuItem antialiased = makeCheckBoxMenuItem("Menu.Appearance.SmoothRelations", "antialiased");
		appearance.add(antialiased);

		JCheckBoxMenuItem showMouseOver = makeCheckBoxMenuItem("Menu.Appearance.ShowMouseOver", AppearanceMenuAction.SHOWMOUSEOVER);
		appearance.add(showMouseOver);

		JMenuItem update = makeMenuItem("Menu.Appearance.UpdateDisplay", "update");
		appearance.add(update);

		// sub-menu for loading visual attributes on the graph
		JMenu load = makeMenu("Menu.Appearance.Load");
		appearance.add(load);

		JMenuItem loadAll = makeMenuItem("Menu.Appearance.LoadAll", AppearanceMenuAction.LOADAPPEARANCE);
		load.add(loadAll);

		JCheckBoxMenuItem loadNodeColor = makeCheckBoxMenuItem("Menu.Appearance.LoadConceptColours", AppearanceMenuAction.NODECOLOR);
		load.add(loadNodeColor);

		JCheckBoxMenuItem loadNodeShape = makeCheckBoxMenuItem("Menu.Appearance.LoadConceptShapes", AppearanceMenuAction.NODESHAPE);
		load.add(loadNodeShape);

		JCheckBoxMenuItem loadEdgeColor = makeCheckBoxMenuItem("Menu.Appearance.LoadRelationColours", AppearanceMenuAction.EDGECOLOR);
		load.add(loadEdgeColor);

		JCheckBoxMenuItem loadEdgeSize = makeCheckBoxMenuItem("Menu.Appearance.LoadRelationWidths", AppearanceMenuAction.EDGESIZE);
		load.add(loadEdgeSize);

		JMenuItem save = makeMenuItem("Menu.Appearance.Save", AppearanceMenuAction.SAVEAPPEARANCE);
		appearance.add(save);

		JMenuItem center = makeMenuItem("Menu.Appearance.CenterNetwork", "center");
		appearance.add(center);

		JMenuItem refresh = makeMenuItem("Menu.Appearance.RefreshLayout", "refresh");
		appearance.add(refresh);

		// zoom sub-menu
		JMenu zoom = makeMenu("Menu.Appearance.Zoom");
		appearance.add(zoom);

		JMenuItem zoomIn = makeMenuItem("Menu.Appearance.ZoomIn", "zoomin");
		zoom.add(zoomIn);

		JMenuItem zoomOut = makeMenuItem("Menu.Appearance.ZoomOut", "zoomout");
		zoom.add(zoomOut);
	}

	/**
	 * Populated the edit menu.
	 * 
	 * @param edit
	 *            JMenu "Edit"
	 */
	private void populateEditMenu(JMenu edit) {

		// make sure edit menu action is handled
		EditMenuAction listener = new EditMenuAction();
		desktop.addActionListener(listener);

		undo = makeMenuItem("Menu.Edit.Undo", "undo");
		undo.setEnabled(false);
		edit.add(undo);

		undoAll = makeMenuItem("Menu.Edit.UndoAll", "undoall");
		undoAll.setEnabled(false);
		edit.add(undoAll);

		redo = makeMenuItem("Menu.Edit.Redo", "redo");
		redo.setEnabled(false);
		edit.add(redo);

		redoAll = makeMenuItem("Menu.Edit.RedoAll", "redoall");
		redoAll.setEnabled(false);
		edit.add(redoAll);

		revert = makeMenuItem("Menu.Edit.Revert", "revert");
		revert.setEnabled(false);
		edit.add(revert);

		JMenu labels = makeMenu("Menu.Edit.Labels");
		edit.add(labels);

		JMenu labelsConcepts = makeMenu("Menu.Edit.LabelsConcepts");
		labels.add(labelsConcepts);

		JMenuItem nfont = makeMenuItem("Menu.Edit.ConceptLabelFonts", "Nfont");
		labelsConcepts.add(nfont);

		JMenuItem conceptLabel = makeMenuItem("Menu.Edit.CompositionConceptLabels", "ConceptLabel");
		labelsConcepts.add(conceptLabel);

		JMenu labelsRelations = makeMenu("Menu.Edit.LabelsRelations");
		labels.add(labelsRelations);

		JMenuItem efont = makeMenuItem("Menu.Edit.RelationLabelFonts", "Efont");
		labelsRelations.add(efont);

		JMenuItem sync = makeMenuItem("Menu.Edit.DeleteHidden", "sync");
		edit.add(sync);

		JMenuItem merge = makeMenuItem("Menu.Edit.Merge", "merge");
		edit.add(merge);

		JMenu actions = makeMenu("Menu.Edit.ConceptRelation");
		edit.add(actions);

		JMenuItem actionNew = makeMenuItem("Menu.Edit.ConceptRelationNew", "add");
		actions.add(actionNew);

		JMenuItem actionEdit = makeMenuItem("Menu.Edit.ConceptRelationEdit", "edit");
		actions.add(actionEdit);

		JMenuItem actionDelete = makeMenuItem("Menu.Edit.ConceptRelationDelete", "delete");
		actions.add(actionDelete);

		// sub menu for mouse mode
		JMenu subMenuMouse = new JMenu(Config.language.getProperty("Menu.Edit.MouseMode"));
		ButtonGroup groupConcept = new ButtonGroup();
		JRadioButtonMenuItem modeTransforming = new JRadioButtonMenuItem(Config.language.getProperty("Menu.Edit.MouseModeTransforming"));
		modeTransforming.setActionCommand(OVTK2ToolBar.TRANSFORMING_MODE);
		modeTransforming.addActionListener(desktop);
		groupConcept.add(modeTransforming);
		subMenuMouse.add(modeTransforming);
		JRadioButtonMenuItem modePicking = new JRadioButtonMenuItem(Config.language.getProperty("Menu.Edit.MouseModePicking"));
		modePicking.setActionCommand(OVTK2ToolBar.PICKING_MODE);
		modePicking.setSelected(true);
		modePicking.addActionListener(desktop);
		groupConcept.add(modePicking);
		subMenuMouse.add(modePicking);
		JRadioButtonMenuItem modeAnnotation = new JRadioButtonMenuItem(Config.language.getProperty("Menu.Edit.MouseModeAnnotating"));
		modeAnnotation.setActionCommand(OVTK2ToolBar.ANNOTATION_MODE);
		modeAnnotation.addActionListener(desktop);
		groupConcept.add(modeAnnotation);
		subMenuMouse.add(modeAnnotation);
		edit.add(subMenuMouse);

		JMenuItem clone = makeMenuItem("Menu.Edit.CloneNetwork", "copy");
		edit.add(clone);

		JMenuItem settings = makeMenuItem("Menu.Edit.Settings", "settings");
		edit.add(settings);
	}

	/**
	 * Populates the file menu.
	 * 
	 * @param file
	 *            JMenu "File"
	 */
	private void populateFileMenu(JMenu file) {

		// make sure file menu action is handled
		FileMenuAction listener = FileMenuAction.getInstance();
		desktop.addActionListener(listener);

		JMenuItem anew = makeMenuItem("Menu.File.New", "new");
		file.add(anew);

		JMenuItem open = makeMenuItem("Menu.File.Open", "open");
		file.add(open);

		// use config.xml to enable/disable webservice
		if (Boolean.parseBoolean(Config.config.getProperty("Webservice.Enable"))) {
			JMenuItem load = makeMenuItem("Menu.File.WebserviceLoad", "load");
			file.add(load);

			JMenuItem upload = makeMenuItem("Menu.File.WebserviceUpload", "upload");
			file.add(upload);
		}

		// use config.xml to enable/disable SQL graph
		if (Boolean.parseBoolean(Config.config.getProperty("SQL.Enable"))) {
			JMenuItem importsql2 = makeMenuItem("Menu.File.SqlLoad", "importSQL2");
			file.add(importsql2);
		}

		// use config.xml to enable/disable import wizard
		if (Boolean.parseBoolean(Config.config.getProperty("ImportWizard.Enable"))) {
			JMenuItem impo = makeMenuItem("Menu.File.ImportWizard", "importwizard");
			file.add(impo);
		}

		JMenuItem save = makeMenuItem("Menu.File.Save", "save");
		file.add(save);

		try {
			if (Boolean.parseBoolean(Config.config.getProperty("ImportWizard2.Enable"))) {
				final Class cls = Thread.currentThread().getContextClassLoader().loadClass("net.sourceforge.ondex.ovtk2.io.FlatFileNetworkImporter");
				JMenuItem item = new JMenuItem("Import network as flat file");
				file.add(item);
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							Object obj = cls.newInstance();
							JInternalFrame frame = (JInternalFrame) obj;
							Rectangle visible = OVTK2Desktop.getInstance().getDesktopPane().getVisibleRect();
							Dimension size = frame.getSize();
							frame.setLocation((visible.width / 2) - (size.width / 2), (visible.height / 2) - (size.height / 2));
							if (size.height > visible.height)
								frame.setLocation(frame.getX(), 0);
							frame.setVisible(true);

							desktop.getDesktopPane().add(frame);
							try {
								frame.setSelected(true);
							} catch (PropertyVetoException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						} catch (InstantiationException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IllegalAccessException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (SecurityException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IllegalArgumentException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				});
				Object o = cls.newInstance();
			}

		} catch (RuntimeException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}

		JMenuItem saveImage = makeMenuItem("Menu.File.SaveImage", "image");
		file.add(saveImage);

		JMenuItem m_import = makeMenuItem("Menu.File.Import", "import");
		file.add(m_import);

		JMenuItem export = makeMenuItem("Menu.File.Export", "export");
		file.add(export);

		JMenuItem print = makeMenuItem("Menu.File.Print", "print");
		file.add(print);

		JMenuItem exit = makeMenuItem("Menu.File.Exit", "exit");
		file.add(exit);

		fileHistory = new FileHistory(this); // init FileHistory
		fileHistory.initFileMenuHistory();
	}

	/**
	 * Populates the filter menu.
	 * 
	 * @param filter
	 *            JMenu "Filters submenu"
	 * @throws MalformedURLException
	 */
	private void populateFilterMenu(JMenu filter) throws MalformedURLException {

		Set<String> exceptions = new HashSet<String>();
		exceptions.add("net.sourceforge.ondex.ovtk2.filter.tag.TagFilter");
		exceptions.add("net.sourceforge.ondex.ovtk2.filter.relationneighbours.RelationNeighboursFilter");
		exceptions.add("net.sourceforge.ondex.ovtk2.filter.unconnected.UnconnectedFilter");

		// make sure filter menu action is handled
		FilterMenuAction listener = new FilterMenuAction();
		desktop.addActionListener(listener);
		desktop.addInternalFrameListener(listener);

		// add filter from config.xml file
		ArrayList<String> entries = new ArrayList<String>();
		Enumeration<?> enu = Config.config.propertyNames();
		while (enu.hasMoreElements()) {
			String name = (String) enu.nextElement();
			if (name.startsWith("Menu.Filter.")) {
				entries.add(name);
			}
		}

		if (DEBUG)
			System.err.println("Filters with menu entries: " + entries);

		// question icon
		String path = "config/themes/" + Config.config.getProperty("Program.Theme") + "/icons/question.png";

		// order filters by name
		String[] ordered = entries.toArray(new String[entries.size()]);

		HashMap<String, String> realNameToDisplayName = new HashMap<String, String>();
		for (String name : ordered) {
			String display = Config.language.getProperty("Name." + name);
			realNameToDisplayName.put(name, display);
		}
		if (DEBUG)
			System.err.println("real to display names: " + realNameToDisplayName);

		Arrays.sort(ordered, new MapSorter(realNameToDisplayName));

		JMenu more = makeMenu("Menu.Filter.More");
		JMenu paths = null;

		if (DEBUG)
			try {
				System.err.println("getFilterClassNames() " + OVTK2PluginLoader.getInstance().getFilterClassNames());
			} catch (FileNotFoundException e) {
				ErrorDialog.show(e);
			}

		ArrayList<JMenuItem> moreComp = new ArrayList<JMenuItem>();

		for (String name : ordered) {
			String clazz = Config.config.getProperty(name);
			if (DEBUG)
				System.err.println("class mapping: " + name + " ->  " + clazz);
			try {
				if (!OVTK2PluginLoader.getInstance().getFilterClassNames().contains(clazz)) {
					if (DEBUG)
						System.err.println("Not in getFilterClassNames");
					// exception case for included filters
					if (!exceptions.contains(clazz)) {
						if (DEBUG)
							System.err.println("no exception for filter: " + clazz);
						// continue;
					}
				}
			} catch (FileNotFoundException e) {
				ErrorDialog.show(e);
			}

			String help = Config.language.getProperty("Help." + name);
			String display = Config.language.getProperty("Name." + name);
			JMenuItem item;
			if (help != null)
				item = new CustomJMenuItem(display, path, help);
			else
				item = new JMenuItem(display);
			item.setActionCommand(name);
			item.addActionListener(desktop);
			item.addMouseListener(mouseListener);
			if (!exceptions.contains(clazz)) {
				if (clazz.contains("ShortestPath")) {
					if (paths == null) {
						paths = makeMenu("Menu.Filter.ShortestPaths");
						moreComp.add(paths);
					}
					paths.add(item);
				} else {
					moreComp.add(item);
				}
			} else {
				filter.add(item);
			}
		}

		// extra sorting in for Shortest Path menu
		JMenuItem[] comps = moreComp.toArray(new JMenuItem[0]);
		Arrays.sort(comps, new Comparator<JMenuItem>() {
			@Override
			public int compare(JMenuItem o1, JMenuItem o2) {
				return o1.getText().compareTo(o2.getText());
			}
		});
		for (JMenuItem c : comps) {
			more.add(c);
		}

		filter.add(more);
	}

	/**
	 * Populates the help menu.
	 * 
	 * @param help
	 *            JMenu "Help"
	 */
	private void populateHelpMenu(JMenu help) {

		// make sure help menu action is handled
		HelpMenuAction listener = new HelpMenuAction();
		desktop.addActionListener(listener);

		// about message
		JMenuItem about = makeMenuItem("Menu.Help.About", "about");
		help.add(about);

		// get language specific help file
		File hsFile = new File(Config.docuDir + "/help/" + Config.config.getProperty("Program.Language") + "/javahelp/jhelpset.hs");

		// initialise help system
		HelpSet hs = null;
		HelpBroker hb;
		try {
			URL hsURL = hsFile.toURI().toURL();
			hs = new HelpSet(null, hsURL);
		} catch (Exception ee) {
			System.out.println("HelpSet " + hsFile.getAbsolutePath() + " not found");
		}

		// create menu entry only when help found
		if (hs != null) {
			SwingHelpUtilities.setContentViewerUI("net.sourceforge.ondex.ovtk2.util.OVTKBasicNativeContentViewerUI");

			hb = hs.createHelpBroker();

			// for question marks in menus
			mouseListener.setHelpBroker(hb);

			// window level help "F1"
			JRootPane rootpane = desktop.getMainFrame().getRootPane();
			hb.enableHelpKey(rootpane, "top", null);

			// create menu entry for help system
			JMenuItem contents = new JMenuItem(Config.language.getProperty("Menu.Help.Contents"));
			contents.addActionListener(new CSH.DisplayHelpFromSource(hb));
			help.add(contents);
		}

		// error reporting dialog
		JMenuItem error = makeMenuItem("Menu.Help.Error", "error");
		help.add(error);

		// create menu entry for tutorial
		JMenuItem tutorial = makeMenuItem("Menu.Help.Tutorial", "tutorial");
		help.add(tutorial);

		// version information
		JMenuItem version = makeMenuItem("Menu.Help.Version", "version");
		help.add(version);

		// redisplay welcome dialog
		JMenuItem welcome = makeMenuItem("Menu.Help.Welcome", "welcome");
		help.add(welcome);
	}

	/**
	 * Populates the layout menu.
	 * 
	 * @param layout
	 *            JMenu "Layouts submenu"
	 * @throws MalformedURLException
	 */
	private void populateLayoutMenu(JMenu layout) throws MalformedURLException {

		Set<String> exceptions = new HashSet<String>();
		exceptions.add("net.sourceforge.ondex.ovtk2.layout.ConceptClassCircleLayout");
		exceptions.add("net.sourceforge.ondex.ovtk2.layout.StaticLayout");
		exceptions.add("net.sourceforge.ondex.ovtk2.layout.GEMLayout");

		// make sure layout menu action is handled
		LayoutMenuAction listener = new LayoutMenuAction();
		desktop.addActionListener(listener);
		desktop.addInternalFrameListener(listener);

		// add layouter from config.xml file
		ArrayList<String> entries = new ArrayList<String>();
		Enumeration<?> enu = Config.config.propertyNames();
		while (enu.hasMoreElements()) {
			String name = (String) enu.nextElement();
			if (name.startsWith("Menu.Layout.")) {
				entries.add(name);
			}
		}

		// question icon
		String path = "config/themes/" + Config.config.getProperty("Program.Theme") + "/icons/question.png";

		// order layouts by name
		String[] ordered = entries.toArray(new String[entries.size()]);

		HashMap<String, String> realNameToDisplayName = new HashMap<String, String>();
		for (String name : ordered) {
			String display = Config.language.getProperty("Name." + name);
			realNameToDisplayName.put(name, display);
		}

		Arrays.sort(ordered, new MapSorter(realNameToDisplayName));

		JMenu more = makeMenu("Menu.Layout.More");

		for (String name : ordered) {
			String clazz = Config.config.getProperty(name);
			try {
				if (!OVTK2PluginLoader.getInstance().getLayoutClassNames().contains(clazz)) {
					// exception case for included layouts
					if (!exceptions.contains(clazz))
						;// continue;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace(); // TODO: log?
			}

			String help = Config.language.getProperty("Help." + name);
			String display = Config.language.getProperty("Name." + name);
			JMenuItem item;
			if (help != null)
				item = new CustomJMenuItem(display, path, help);
			else
				item = new JMenuItem(display);
			item.setActionCommand(name);
			item.addActionListener(desktop);
			item.addMouseListener(mouseListener);
			if (!clazz.equals("net.sourceforge.ondex.ovtk2.layout.ConceptClassCircleLayout") && !clazz.equals("net.sourceforge.ondex.ovtk2.layout.GEMLayout")) {
				more.add(item);
			} else {
				layout.add(item);
			}
		}

		layout.add(more);

		JCheckBoxMenuItem options = makeCheckBoxMenuItem("Menu.Layout.Options", "options");
		layout.add(options);

	}

	/**
	 * Populates the selecting menu.
	 * 
	 * @param selecting
	 *            JMenu "Selecting Concepts/Relations submenu"
	 */
	private void populateSelectingMenu(JMenu selecting) {

		// make sure selecting menu action is handled
		SelectingMenuAction listener = new SelectingMenuAction();
		desktop.addActionListener(listener);

		JMenuItem allnodes = makeMenuItem("Menu.Selecting.SelectAllNodes", "allnodes");
		selecting.add(allnodes);

		JMenuItem alledges = makeMenuItem("Menu.Selecting.SelectAllEdges", "alledges");
		selecting.add(alledges);

		JMenuItem inversenodes = makeMenuItem("Menu.Selecting.InvertSelectionNodes", "inversenodes");
		selecting.add(inversenodes);

		JMenuItem inverseedges = makeMenuItem("Menu.Selecting.InvertSelectionEdges", "inverseedges");
		selecting.add(inverseedges);
	}

	/**
	 * Populates the tools menu.
	 * 
	 * @param tools
	 *            JMenu "Tools"
	 */
	private void populateToolsMenu(JMenu tools) {

		// make sure tool menu action is handled
		ToolMenuAction listener = new ToolMenuAction();
		desktop.addActionListener(listener);
		desktop.addInternalFrameListener(listener);

		try {
			JMenuItem m_launch = makeMenuItem("Menu.Tools.Integrator", "launcher");
			tools.add(m_launch);
		} catch (Exception e) {
			// No launcher then
		}

		// filter sub-menu
		JMenu filter = makeMenu("Menu.Filter");
		tools.add(filter);
		try {
			populateFilterMenu(filter);
		} catch (MalformedURLException e) {

			e.printStackTrace();
		}

		// annotator sub-menu
		JMenu anno = makeMenu("Menu.Annotator");
		tools.add(anno);
		try {
			populateAnnoMenu(anno);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		// selecting concepts and relations sub-menu
		JMenu selecting = makeMenu("Menu.Selecting");
		tools.add(selecting);
		populateSelectingMenu(selecting);

		JMenuItem console = makeMenuItem("Menu.Tools.Console", "console");
		tools.add(console);

		if (Boolean.parseBoolean(Config.config.getProperty("PopupEditor.Enable"))) {
			JMenuItem popupeditor = makeMenuItem("Menu.Tools.PopupEditor", "popupeditor");
			tools.add(popupeditor);
		}

		JMenuItem stats = makeMenuItem("Menu.Tools.Statistics", "stats");
		tools.add(stats);

	}

	/**
	 * Populates the taverna menu.
	 * 
	 * @param taverna
	 *            JMenu "Tools"
	 */
	private void addTavernaMenu() {

		if (!Boolean.parseBoolean(Config.config.getProperty("Taverna.Enable"))) {
			return;
		}
		String value = Config.language.getProperty("Menu.Taverna");
		if (value == null) {
			throw new RuntimeException("Key \"Menu.Taverna\" is missing from the language file");
		}
		JMenu tavernaMenu = new JMenu(value);
		this.add(tavernaMenu);
		// Use 'v' as 't; is in English for Tool
		tavernaMenu.setMnemonic('v');

		// TavernaMenuAction listener;

		Frame parentFrame = this.getParentFrame();

		Class<TavernaApi> clazz;
		try {
			URLClassLoader ucl = OVTK2PluginLoader.getInstance().ucl;
			Thread.currentThread().setContextClassLoader(ucl);
			String classname = "net.sourceforge.ondex.taverna.TavernaWrapper";
			clazz = (Class<TavernaApi>) ucl.loadClass(classname);
			// System.out.println(clazz);
			// Constructor[] constructors = clazz.getConstructors();
			// for (Constructor c: constructors){
			// System.out.println(c);
			// }
		} catch (Exception e) {
			// Ok no Taverna
			e.printStackTrace();
			System.err.println("No TavernaWrapper found. ");
			System.err.println(e.getMessage());
			JMenuItem missing = makeMenuItem("Menu.Taverna.Missing", "TavernaMissing");
			NoTavernaMenuAction helpListener = new NoTavernaMenuAction();
			desktop.addActionListener(helpListener);
			tavernaMenu.add(missing);
			return;
		}
		try {
			Constructor<TavernaApi> constructor = clazz.getConstructor(parentFrame.getClass());
			TavernaApi travernaApi = constructor.newInstance(parentFrame);
			// TavernaApi handles the loading of menu bar and the actions.
			travernaApi.attachMenu(tavernaMenu);
			// The Home files can be set from the config file.
			// tavernaApi ignore nulls and emptys
			// An Exception is throw is a non emplty string is incorrect.
			travernaApi.setTavernaHome(Config.config.getProperty("Taverna.TravenaHome"));
			travernaApi.setDataViewerHome(Config.config.getProperty("Taverna.DataViewerHomer"));
			File dataDir = new File(net.sourceforge.ondex.config.Config.ondexDir);
			travernaApi.setRootDirectory(dataDir);
			Icon icon = new ImageIcon("config/toolbarButtonGraphics/taverna/taverna.jpeg");
		} catch (Exception e) {
			ErrorDialog.show(e);
		}

	}

	/**
	 * Populates the view menu.
	 * 
	 * @param view
	 *            JMenu "View"
	 */
	private void populateViewMenu(JMenu view) {

		// make sure view menu action is handled
		ViewMenuAction listener = new ViewMenuAction();
		desktop.addActionListener(listener);
		desktop.addInternalFrameListener(listener);

		JCheckBoxMenuItem meta = makeCheckBoxMenuItem("Menu.View.Metagraph", "metagraph");
		view.add(meta);

		JCheckBoxMenuItem legend = makeCheckBoxMenuItem("Menu.View.Legend", "legend");
		view.add(legend);

		JCheckBoxMenuItem contentsdisplay = makeCheckBoxMenuItem("Menu.View.ItemInfo", "contentsdisplay");
		view.add(contentsdisplay);

		JMenu list = makeMenu("Menu.View.List");
		view.add(list);

		JCheckBoxMenuItem nodevisible = makeCheckBoxMenuItem("Menu.View.ConceptList", "nodevisible");
		list.add(nodevisible);

		JCheckBoxMenuItem edgevisible = makeCheckBoxMenuItem("Menu.View.RelationList", "edgevisible");
		list.add(edgevisible);

		JMenuItem editor = makeMenuItem("Menu.View.TabularEditor", "editor");
		view.add(editor);

		JCheckBoxMenuItem satellite = makeCheckBoxMenuItem("Menu.View.SatelliteView", "satellite");
		view.add(satellite);

		JInternalFrameSelector frameSelector = new JInternalFrameSelector(Config.language.getProperty("Menu.Windows"));
		view.add(frameSelector);
		try {
			frameSelector.setMnemonic(frameSelector.getText().charAt(0));
		} catch (StringIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates settings for undo and redo
	 * 
	 * @param activeViewer
	 */
	@Override
	public void updateUndoRedo(OVTK2PropertiesAggregator activeViewer) {

		if (activeViewer != null) {
			String text = Config.language.getProperty("Menu.Edit.Undo");
			if (activeViewer.getUndoManager().canUndo()) {
				text = activeViewer.getUndoManager().getUndoPresentationName();
				undo.setEnabled(true);
				undoAll.setEnabled(true);
			} else {
				undo.setEnabled(false);
				undoAll.setEnabled(false);
			}
			undo.setText(text);

			text = Config.language.getProperty("Menu.Edit.Redo");
			if (activeViewer.getUndoManager().canRedo()) {
				text = activeViewer.getUndoManager().getRedoPresentationName();
				redo.setEnabled(true);
				redoAll.setEnabled(true);
			} else {
				redo.setEnabled(false);
				redoAll.setEnabled(false);
			}
			redo.setText(text);

		}
	}

	/**
	 * Updates menu bar to represent state of activeViewer.
	 * 
	 */
	@Override
	public void updateMenuBar(OVTK2PropertiesAggregator activeViewer) {
		updateUndoRedo(activeViewer);

		if (activeViewer != null) {
			revert.setEnabled(activeViewer.getONDEXJUNGGraph().hasLastState());
			for (int i = 0; i < this.getMenuCount(); i++) {
				JMenu menu = this.getMenu(i);
				LinkedList<Component> components = new LinkedList<Component>();
				components.addAll(Arrays.asList(menu.getMenuComponents()));
				while (!components.isEmpty()) {
					Component c = components.pop();
					if (c instanceof JCheckBoxMenuItem) {
						JCheckBoxMenuItem item = (JCheckBoxMenuItem) c;
						String cmd = item.getActionCommand();
						if (cmd.equals("resize")) {
							item.setSelected(activeViewer.isRelayoutOnResize());
						} else if (cmd.equals("antialiased")) {
							item.setSelected(activeViewer.isAntiAliased());
						} else if (cmd.equals(AppearanceMenuAction.SHOWMOUSEOVER)) {
							// get current mouse over state
							OVTK2GraphMouse mouse = (OVTK2GraphMouse) activeViewer.getVisualizationViewer().getGraphMouse();
							OVTK2PickingMousePlugin picking = mouse.getOVTK2PickingMousePlugin();
							if (picking != null)
								item.setSelected(picking.isShowMouseOver());
						} else if (cmd.equals("nodelabels")) {
							item.setSelected(activeViewer.isShowNodeLabels());
						} else if (cmd.equals("edgelabels")) {
							item.setSelected(activeViewer.isShowEdgeLabels());
						} else if (cmd.equals("bothlabels")) {
							item.setSelected(activeViewer.isShowEdgeLabels() && activeViewer.isShowNodeLabels());
						} else if (cmd.equals(AppearanceMenuAction.EDGECOLOR)) {
							item.setSelected(activeViewer.getEdgeColors().getEdgeColorSelection() == EdgeColorSelection.MANUAL);
						} else if (cmd.equals(AppearanceMenuAction.EDGESIZE)) {
							item.setSelected(activeViewer.getEdgeStrokes().getEdgeSizeTransformer() != null);
						} else if (cmd.equals(AppearanceMenuAction.NODECOLOR)) {
							item.setSelected(activeViewer.getNodeColors().getFillPaintSelection() == NodeFillPaintSelection.MANUAL);
						} else if (cmd.equals(AppearanceMenuAction.NODESHAPE)) {
							item.setSelected(activeViewer.getNodeShapes().getNodeShapeSelection() == NodeShapeSelection.MANUAL);
						} else if (cmd.equals("edgearrow")) {
							// relation arrow
							item.setSelected(activeViewer.getEdgeArrows().isShowArrow());
						}
					}

					// process colour and shape by settings here
					if (c instanceof JRadioButtonMenuItem) {
						JRadioButtonMenuItem item = (JRadioButtonMenuItem) c;
						String cmd = item.getActionCommand();
						// concept colouring
						if (activeViewer.getNodeColors().getFillPaintSelection() == ONDEXNodeFillPaint.NodeFillPaintSelection.CONCEPTCLASS && cmd.equals(AppearanceMenuAction.COLOR_CONCEPT_BY_CLASS)) {
							item.setSelected(true);
						} else if (activeViewer.getNodeColors().getFillPaintSelection() == ONDEXNodeFillPaint.NodeFillPaintSelection.DATASOURCE && cmd.equals(AppearanceMenuAction.COLOR_CONCEPT_BY_SOURCE)) {
							item.setSelected(true);
						} else if (activeViewer.getNodeColors().getFillPaintSelection() == ONDEXNodeFillPaint.NodeFillPaintSelection.EVIDENCETYPE && cmd.equals(AppearanceMenuAction.COLOR_CONCEPT_BY_EVIDENCE)) {
							item.setSelected(true);
						}
						// relation colouring
						if (activeViewer.getEdgeColors().getEdgeColorSelection() == ONDEXEdgeColors.EdgeColorSelection.RELATIONTYPE && cmd.equals(AppearanceMenuAction.COLOR_RELATION_BY_TYPE)) {
							item.setSelected(true);
						} else if (activeViewer.getEdgeColors().getEdgeColorSelection() == ONDEXEdgeColors.EdgeColorSelection.EVIDENCETYPE && cmd.equals(AppearanceMenuAction.COLOR_RELATION_BY_EVIDECE)) {
							item.setSelected(true);
						}
						// relation shapes
						if (activeViewer.getEdgeShapes().getEdgeShape().equals(ONDEXEdgeShapes.EdgeShape.QUAD) && cmd.equals(AppearanceMenuAction.SHAPE_QUAD)) {
							item.setSelected(true);
						} else if (activeViewer.getEdgeShapes().getEdgeShape().equals(ONDEXEdgeShapes.EdgeShape.CUBIC) && cmd.equals(AppearanceMenuAction.SHAPE_CUBIC)) {
							item.setSelected(true);
						} else if (activeViewer.getEdgeShapes().getEdgeShape().equals(ONDEXEdgeShapes.EdgeShape.BENT) && cmd.equals(AppearanceMenuAction.SHAPE_BENT)) {
							item.setSelected(true);
						} else if (activeViewer.getEdgeShapes().getEdgeShape().equals(ONDEXEdgeShapes.EdgeShape.LINE) && cmd.equals(AppearanceMenuAction.SHAPE_LINE)) {
							item.setSelected(true);
						}
					}

					// enqueue for breadth first search
					if (c instanceof JMenu) {
						menu = (JMenu) c;
						components.addAll(Arrays.asList(menu.getMenuComponents()));
					}
				}
			}
		}

		for (int i = 0; i < this.getMenuCount(); i++) {
			JMenu menu = this.getMenu(i);
			LinkedList<Component> components = new LinkedList<Component>();
			components.addAll(Arrays.asList(menu.getMenuComponents()));
			while (!components.isEmpty()) {
				Component c = components.pop();
				if (c instanceof JCheckBoxMenuItem) {
					JCheckBoxMenuItem item = (JCheckBoxMenuItem) c;
					String cmd = item.getActionCommand();
					if (cmd.equals("satellite")) {
						item.setSelected(ViewMenuAction.isSatelliteShown());
					} else if (cmd.equals("options")) {
						item.setSelected(LayoutMenuAction.isOptionsShown());
					} else if (cmd.equals("nodevisible")) {
						item.setSelected(ViewMenuAction.isDialogNodesShown());
					} else if (cmd.equals("edgevisible")) {
						item.setSelected(ViewMenuAction.isDialogEdgesShown());
					} else if (cmd.equals("metagraph")) {
						item.setSelected(ViewMenuAction.isMetaGraphShown());
					} else if (cmd.equals("legend")) {
						item.setSelected(ViewMenuAction.isLegendShown());
					} else if (cmd.equals("contentsdisplay")) {
						item.setSelected(ViewMenuAction.isContentsDisplayShown());
					}
				}

				// process colour and shape by settings here
				if (c instanceof JRadioButtonMenuItem) {
					JRadioButtonMenuItem item = (JRadioButtonMenuItem) c;
					String cmd = item.getActionCommand();

					// mouse mode selection
					if (cmd.equals(OVTK2ToolBar.ANNOTATION_MODE) && OVTK2ToolBar.ANNOTATION_MODE.equals(OVTK2Desktop.getDesktopResources().getToolBar().getMouseMode())) {
						item.setSelected(true);
					} else if (cmd.equals(OVTK2ToolBar.PICKING_MODE) && OVTK2ToolBar.PICKING_MODE.equals(OVTK2Desktop.getDesktopResources().getToolBar().getMouseMode())) {
						item.setSelected(true);
					} else if (cmd.equals(OVTK2ToolBar.TRANSFORMING_MODE) && OVTK2ToolBar.TRANSFORMING_MODE.equals(OVTK2Desktop.getDesktopResources().getToolBar().getMouseMode())) {
						item.setSelected(true);
					}
				}

				// enqueue for breadth first search
				if (c instanceof JMenu) {
					menu = (JMenu) c;
					components.addAll(Arrays.asList(menu.getMenuComponents()));
				}
			}
		}
	}
}
