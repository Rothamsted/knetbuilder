package net.sourceforge.ondex.ovtk2.ui.popup;

import java.lang.reflect.Method;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.tools.ondex.MdHelper;

public class VertexURLResolverMenuItem extends EntityURIMenuItem {
	private static final long serialVersionUID = 1L;

	public static VertexURLResolverMenuItem getMenuItem(OVTK2Viewer viewer, ONDEXConcept vertex, ONDEXRelation edge, String name) {
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
		return new VertexURLResolverMenuItem(viewer, vertex, edge, name);
	}

	public static String getCurrentName() {
		return currentName;
	}

	protected VertexURLResolverMenuItem(OVTK2Viewer viewer, ONDEXConcept vertex, ONDEXRelation edge, String name) {
		super(viewer, vertex, edge, name, null, null, null, new HashMap<String, String>());
	}

	@Override
	protected void doAction() {
		ONDEXJUNGGraph graph = null;
		try {
			graph = viewer.getONDEXJUNGGraph();
		} catch (Exception e1) {
			System.err.println("Error - no empty graph found");
			return;
		}
		AttributeName att = MdHelper.createAttName(graph, "URI", String.class);
		Set<ONDEXConcept> cs = viewer.getPickedNodes();
		if (n != null) {
			cs.add(n);
		}
		for (ONDEXConcept concept : cs) {
			Attribute attribute = concept.getAttribute(att);
			if (attribute == null) {
				continue;
			}
			String uri = attribute.getValue().toString();
			if (uri.contains("uniprot") && !uri.endsWith(".rdf")) {
				uri = uri + ".rdf";
			}
			try {
				Object instanceInterp = clsInterp.getMethod("getCurrentInstance", new Class<?>[0]).invoke(null, new Object[0]);
				Method m = clsInterp.getMethod("loadFromURL", new Class[] { String.class });
				m.invoke(instanceInterp, new Object[] { uri });
				Method z = clsInterp.getMethod("getAffectedConcepts", new Class[] {});
				BitSet set = (BitSet) z.invoke(instanceInterp, new Object[0]);
				Set<ONDEXConcept> selectedConcepts = BitSetFunctions.create(graph, ONDEXConcept.class, set);
				graph.setVisibility(selectedConcepts, true);
				Set<ONDEXRelation> rs = new HashSet<ONDEXRelation>();
				for (ONDEXConcept c : selectedConcepts) {
					rs.addAll(graph.getRelationsOfConcept(c));
				}
				viewer.getVisualizationViewer().getModel().fireStateChanged();
				doLayout(viewer, concept.getId(), set);
				viewer.center();
				viewer.getVisualizationViewer().getModel().fireStateChanged();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
