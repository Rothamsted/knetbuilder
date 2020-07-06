package net.sourceforge.ondex.ovtk2.ui.popup;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JMenuItem;
import javax.swing.RootPaneContainer;
import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.util.LayoutNeighbours;
import net.sourceforge.ondex.tools.ondex.MdHelper;

/**
 * Interactive SPARQL querry
 * 
 * @author lysenkoa
 * 
 */
public class EntityURIMenuItem extends JMenuItem {
	protected static final long serialVersionUID = 1L;
	public static final Pattern PRIMARY_URI = Pattern.compile("~~URI~~");
	public static final Pattern FROM_URI = Pattern.compile("~~FROM_URI~~");
	public static final Pattern TO_URI = Pattern.compile("~~TO_URI~~");
	public static final Pattern TYPE = Pattern.compile("~~TYPE~~");
	protected final String name;
	protected final List<String> commandsNode;
	protected final List<String> commandsEdge;
	protected static Class<?> tmp;
	protected static Class<?> clsInterp;
	protected static Class<?> clsOutput;
	protected static String currentName;
	protected ONDEXRelation e;
	protected ONDEXConcept n;
	protected final OVTK2Viewer viewer;
	protected Set<String> layoutAndCenter;
	protected final Map<String, String> cvToUrl;

	public static EntityURIMenuItem getMenuItem(OVTK2Viewer viewer, ONDEXConcept vertex, ONDEXRelation edge, String name, List<String> commands, List<String> commandsEdge, Set<String> layoutAndCenter, Map<String, String> cvToUrl) {
		currentName = name;
		if (tmp == null) {
			try {
				tmp = Thread.currentThread().getContextClassLoader().loadClass("net.sourceforge.ondex.scripting.OutputPrinter");
				clsInterp = Thread.currentThread().getContextClassLoader().loadClass("net.sourceforge.ondex.scripting.sparql.SPARQLInterpreter");
				clsOutput = Thread.currentThread().getContextClassLoader().loadClass("net.sourceforge.ondex.scripting.ui.CommandLine");
			} catch (ClassNotFoundException e) {
				return null;
			}
		}
		return new EntityURIMenuItem(viewer, vertex, edge, name, commands, commandsEdge, layoutAndCenter, cvToUrl);
	}

	public static String getCurrentName() {
		return currentName;
	}

	protected EntityURIMenuItem(final OVTK2Viewer viewer, ONDEXConcept vertex, ONDEXRelation edge, String name, List<String> commandsNode, List<String> commandsEdge, Set<String> layoutAndCenter, Map<String, String> cvToUrl) {
		this.viewer = viewer;
		this.n = vertex;
		this.e = edge;
		this.name = name;
		this.commandsNode = commandsNode;
		this.commandsEdge = commandsEdge;
		this.layoutAndCenter = layoutAndCenter;
		this.cvToUrl = cvToUrl;
		this.setText(name);
		addActionListener(new ActionListener() {
			public final void actionPerformed(ActionEvent e) {
				Cursor cursor = viewer.getVisualizationViewer().getCursor();
				viewer.getVisualizationViewer().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				RootPaneContainer root = findRoot(viewer);
				root.getGlassPane().setVisible(true);
				root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				StateEdit edit = null;
				try {
					doAction();
				} finally {
					if (edit != null) {
						viewer.getVisualizationViewer().repaint();
						edit.end();
					}
					root.getGlassPane().setVisible(false);
					viewer.getVisualizationViewer().setCursor(cursor);
				}
				viewer.getVisualizationViewer().repaint();
			}
		});
	}

	protected void doAction() {
		ONDEXJUNGGraph graph = null;
		try {
			graph = viewer.getONDEXJUNGGraph();
		} catch (Exception e1) {
			System.err.println("Error - no empty graph found");
			return;
		}
		AttributeName att = MdHelper.createAttName(graph, "URI", String.class);
		AttributeName attType = MdHelper.createAttName(graph, "TYPE_URI", String.class);

		Set<ONDEXConcept> cs = new HashSet<ONDEXConcept>(viewer.getPickedNodes());
		if (n != null) {
			cs.add(n);
		}
		Set<ONDEXRelation> rs = new HashSet<ONDEXRelation>(viewer.getPickedEdges());
		if (e != null) {
			rs.add(e);
		}

		for (ONDEXConcept concept : cs) {
			Attribute attribute = concept.getAttribute(att);
			String uri = null;
			if (attribute != null) {
				uri = attribute.getValue().toString();
			} else {
				for (ConceptAccession ca : concept.getConceptAccessions()) {
					String cv = ca.getElementOf().getId();
					String base = cvToUrl.get(cv);
					if (base != null) {
						uri = base + ca.getAccession();
						concept.createAttribute(att, uri, false);
						break;
					}
				}
			}
			if (uri == null) {
				continue;
			}

			try {
				Object instanceInterp = clsInterp.getMethod("getCurrentInstance", new Class<?>[0]).invoke(null, new Object[0]);
				Object instanceOutput = clsOutput.getMethod("getCurrentInstance", new Class<?>[0]).invoke(null, new Object[0]);
				Method m = clsInterp.getMethod("silentProcess", new Class[] { String.class, tmp });

				for (String command : commandsNode) {
					String com = new String(command);
					com = PRIMARY_URI.matcher(com).replaceAll(uri);
					m.invoke(instanceInterp, new Object[] { com, instanceOutput });
				}

				Method z = clsInterp.getMethod("getAffectedConcepts", new Class[] {});
				BitSet set = (BitSet) z.invoke(instanceInterp, new Object[0]);
				Set<ONDEXConcept> selectedConcepts = BitSetFunctions.create(graph, ONDEXConcept.class, set);
				graph.setVisibility(selectedConcepts, true);
				Set<ONDEXRelation> relations = new HashSet<ONDEXRelation>();
				for (ONDEXConcept c : selectedConcepts) {
					rs.addAll(graph.getRelationsOfConcept(c));
				}
				graph.setVisibility(relations, true);
				viewer.getVisualizationViewer().getModel().fireStateChanged();
				if (layoutAndCenter.contains(this.name)) {
					doLayout(viewer, concept.getId(), set);
					viewer.center();
					viewer.getVisualizationViewer().getModel().fireStateChanged();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (ONDEXRelation relation : rs) {
			Attribute attributeFrom = relation.getFromConcept().getAttribute(att);
			if (attributeFrom == null) {
				continue;
			}
			String uriFrom = attributeFrom.getValue().toString();

			Attribute attributeTo = relation.getToConcept().getAttribute(att);
			if (attributeTo == null) {
				continue;
			}
			String uriTo = attributeTo.getValue().toString();

			Attribute typeURI = relation.getAttribute(attType);
			if (typeURI == null) {
				continue;
			}
			String type = typeURI.getValue().toString();

			try {
				Object instanceInterp = clsInterp.getMethod("getCurrentInstance", new Class<?>[0]).invoke(null, new Object[0]);
				Object instanceOutput = clsOutput.getMethod("getCurrentInstance", new Class<?>[0]).invoke(null, new Object[0]);
				Method m = clsInterp.getMethod("process", new Class[] { String.class, tmp });

				for (String command : commandsEdge) {
					String com = new String(command);
					com = FROM_URI.matcher(com).replaceAll(uriFrom);
					com = TO_URI.matcher(com).replaceAll(uriTo);
					com = TYPE.matcher(com).replaceAll(type);
					m.invoke(instanceInterp, new Object[] { com, instanceOutput });
				}

				Method z = clsInterp.getMethod("getAffectedConcepts", new Class[] {});
				BitSet set = (BitSet) z.invoke(instanceInterp, new Object[0]);
				Set<ONDEXConcept> selectedConcepts = BitSetFunctions.create(graph, ONDEXConcept.class, set);
				graph.setVisibility(selectedConcepts, true);
				Set<ONDEXRelation> relations = new HashSet<ONDEXRelation>();
				for (ONDEXConcept c : selectedConcepts) {
					relations.addAll(graph.getRelationsOfConcept(c));
				}
				graph.setVisibility(relations, true);
				viewer.getVisualizationViewer().getModel().fireStateChanged();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void doLayout(OVTK2Viewer viewer, int c, BitSet cs) {
		ONDEXJUNGGraph jung = viewer.getONDEXJUNGGraph();

		ONDEXConcept n = jung.getConcept(c);
		Set<ONDEXConcept> nodes = new HashSet<ONDEXConcept>();
		for (int i = cs.nextSetBit(0); i >= 0; i = cs.nextSetBit(i + 1)) {
			nodes.add(jung.getConcept(i));
		}
		LayoutNeighbours.layoutNodes(viewer.getVisualizationViewer(), n, nodes);
	}

	private static RootPaneContainer findRoot(Component c) {
		if (c instanceof RootPaneContainer)
			return (RootPaneContainer) c;
		return findRoot(c.getParent());
	}
}
