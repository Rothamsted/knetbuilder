package net.sourceforge.ondex.transformer.graphpath;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

public class Transformer extends ONDEXTransformer {

	private int paths = 0;

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[0];
	}

	@Override
	public String getId() {
		return "graphpath";
	}

	@Override
	public String getName() {
		return "graphpath";
	}

	@Override
	public String getVersion() {
		return "test - not for production";
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	@Override
	public void start() throws Exception {

		BufferedReader in = new BufferedReader(new FileReader(
				"C:/temp/param.txt"));
		int depth = 10;
		int number = 100;
		String str;
		while ((str = in.readLine()) != null) {
			if (str.startsWith("depth=")) {
				String n = str.substring(6);
				depth = Integer.parseInt(n);
			}

			if (str.startsWith("number=")) {
				String n = str.substring(7);
				number = Integer.parseInt(n);
			}
		}

		BufferedWriter out;
		out = new BufferedWriter(new FileWriter("C:/temp/raw_pathsONDEX.txt"));

		Collection<ONDEXConcept> v = graph.getConceptsOfConceptClass(graph
				.getMetaData().getConceptClass("Gene"));
		int i = 0;
		for (ONDEXConcept g : v) {
			if (g.getElementOf().equals(
					graph.getMetaData().getDataSource("Poplar-JGI"))) {
				GraphPath a = new GraphPath(depth);
				a.addNode(g.getId());
				graphTravel(a, out);
				i++;
				System.out.println("Searched gene " + i + " - " + g.getId());
			}

			if (i > 100) {
				break;
			}
		}
		out.close();

	}

	private void graphTravel(GraphPath gp, BufferedWriter out) {

		int node = gp.getLastNode();
		boolean reached = false;

		ONDEXConcept lastNode = graph.getConcept(node);
		if (lastNode.getOfType().equals(
				graph.getMetaData().getConceptClass("BioProc"))) {
			reached = true;
		}

		if (reached) {

			paths++;

			if (paths % 50 == 0) {
				System.out.println("Paths found: " + paths);

				try {
					out.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			try {
				out.write(gp.toString());
				out.write("\n");
				out.write(typeToString(gp));
				out.write("\n");
				out.write("\n");
			} catch (Exception e) {
				// TODO: handle exception
			}

		} else if (!gp.atLimit()) {

			Collection<ONDEXRelation> v = graph.getRelationsOfConcept(lastNode);
			for (ONDEXRelation r : v) {
				ONDEXConcept targetNode = null;
				if (r.getFromConcept().equals(lastNode)) {
					targetNode = r.getToConcept();
				} else {
					targetNode = r.getFromConcept();
				}

				if (gp.containsEdge(r.getId())
						|| gp.containsNode(targetNode.getId())) {
					// cycle, do nothing

				} else if (targetNode != null) {

					GraphPath gpb = gp.clone();
					gpb.addEdge(r.getId());
					gpb.addNode(targetNode.getId());

					graphTravel(gpb, out);

				}
			}

		}

	}

	private String relationTypeLookup(int a) {

		ONDEXRelation r = graph.getRelation(a);
		RelationType rt = r.getOfType();

		return rt.getId();

	}

	private String conceptClassLookup(int a) {

		ONDEXConcept c = graph.getConcept(a);
		ConceptClass cs = c.getOfType();

		return cs.getId();

	}

	private String typeToString(GraphPath a) {

		ArrayList<Integer> nodes = a.getNodes();
		ArrayList<Integer> edges = a.getEdges();

		StringBuilder s = new StringBuilder();
		for (int t = 0; t < nodes.size() - 1; t++) {

			s.append(conceptClassLookup(nodes.get(t)));
			s.append(" - ");

			s.append(relationTypeLookup(edges.get(t)));
			s.append(" - ");
		}

		s.append(conceptClassLookup(nodes.get(nodes.size() - 1)));

		return s.toString();

	}

}
