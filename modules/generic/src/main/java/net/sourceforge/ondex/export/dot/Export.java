package net.sourceforge.ondex.export.dot;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.export.ONDEXExport;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * Exports a whole ONDEX graph into the DOT language according to
 * http://www.graphviz.org/doc/info/lang.html
 * 
 * @author taubertj
 */
@Authors(authors = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Custodians(custodians = { "Jochen Weile" }, emails = { "jweile at users.sourceforge.net" })
public class Export extends ONDEXExport implements ArgumentNames {

	/**
	 * Trim Strings to this length
	 */
	private int trimLength = 20;

	/**
	 * Default node size
	 */
	private double defaultSize = 1.75;

	/**
	 * Use String trim function
	 */
	private boolean useTrim = false;

	/**
	 * Attribute to parse graphical attributes from
	 */
	private AttributeName aaShape, aaSize, aaColor, aaVisible;

	/**
	 * Initialises graph meta data for visual attributes
	 */
	private void initMetaData() {
		ONDEXGraphMetaData meta = graph.getMetaData();
		aaShape = meta.getAttributeName("shape");
		if (aaShape == null)
			aaShape = meta.getFactory().createAttributeName("shape",
					Integer.class);
		aaSize = meta.getAttributeName("size");
		if (aaSize == null)
			aaSize = meta.getFactory().createAttributeName("size",
					Integer.class);
		aaColor = meta.getAttributeName("color");
		if (aaColor == null)
			aaColor = meta.getFactory().createAttributeName("color",
					Color.class);
		aaVisible = meta.getAttributeName("visible");
		if (aaVisible == null)
			aaVisible = meta.getFactory().createAttributeName("visible",
					Boolean.class);
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		BooleanArgumentDefinition trim_arg = new BooleanArgumentDefinition(
				TRIM_ARG, TRIM_ARG_DESC, false, false);
		RangeArgumentDefinition<Integer> trimlength_arg = new RangeArgumentDefinition<Integer>(
				TRIMLENGTH_ARG, TRIMLENGTH_ARG_DESC, false, 20, 1,
				Integer.MAX_VALUE, Integer.class);
		RangeArgumentDefinition<Double> nodesize_arg = new RangeArgumentDefinition<Double>(
				NODESIZE_ARG, NODESIZE_ARG_DESC, false, 1.75, 0.01,
				Double.MAX_VALUE, Double.class);
		FileArgumentDefinition export_file_arg = new FileArgumentDefinition(
				FileArgumentDefinition.EXPORT_FILE, "dot formated output file",
				true, false, false, false);
		return new ArgumentDefinition<?>[] { trim_arg, trimlength_arg,
				nodesize_arg, export_file_arg };
	}

	@Override
	public String getName() {
		return "DOT Export";
	}

	@Override
	public String getVersion() {
		return "07.08.2009";
	}

	@Override
	public String getId() {
		return "dot";
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

		// get arguments
		if (args.getUniqueValue(TRIM_ARG) != null) {
			useTrim = (Boolean) args.getUniqueValue(TRIM_ARG);
			if (args.getUniqueValue(TRIMLENGTH_ARG) != null) {
				trimLength = (Integer) args.getUniqueValue(TRIMLENGTH_ARG);
			}
		}

		if (args.getUniqueValue(NODESIZE_ARG) != null) {
			defaultSize = (Double) args.getUniqueValue(NODESIZE_ARG);
		}

		// get export file
		File file = new File(
				(String) args
						.getUniqueValue(FileArgumentDefinition.EXPORT_FILE));

		// setup graph visual meta data
		initMetaData();

		// write to file
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write("digraph G {\n");

		// for proper scaling
		double minSize = Integer.MAX_VALUE;
		double maxSize = Integer.MIN_VALUE;

		// introduce concepts as nodes
		Set<ONDEXConcept> writtenConcepts = new HashSet<ONDEXConcept>();
		for (ONDEXConcept c : graph.getConcepts()) {
			// only export visible concepts if visibility known
			Attribute visible = c.getAttribute(aaVisible);
			if (visible == null || (Boolean) visible.getValue()) {
				writtenConcepts.add(c);
			}

			// capture size
			Attribute sizeAttribute = c.getAttribute(aaSize);
			if (sizeAttribute != null) {
				int s = (Integer) sizeAttribute.getValue();
				if (s < minSize)
					minSize = s;
				if (s > maxSize)
					maxSize = s;
			}
		}

		// iterate over all concepts to write
		for (ONDEXConcept c : writtenConcepts) {

			// use ONDEX ID for unique node names
			writer.write("\tnode" + c.getId() + " [");

			// write label or id as label
			ConceptName cn = c.getConceptName();
			if (cn != null) {
				writer.write("label=\"" + trimString(cn.getName()) + "\"");
			} else {
				String name = c.getOfType().getFullname();
				if (name == null || name.trim().length() == 0)
					name = c.getOfType().getId();
				writer.write("label=\"" + name + " " + c.getId() + "\"");
			}

			// write shape of concept
			writer.write(", shape=" + gds2ShapeName(c));

			// write colour of concept
			writer.write(", color="
					+ attribute2ColorString(c.getAttribute(aaColor)));

			// translate scaling factor as actual size
			Attribute sizeAttribute = c.getAttribute(aaSize);
			if (sizeAttribute != null) {
				double s = (Integer) sizeAttribute.getValue();
				double percentBase = ((s - minSize) / maxSize);
				double width = defaultSize + (percentBase * 3 * defaultSize);
				writer.write(", width=" + width + ", height=" + width);
			} else {
				writer.write(", width=" + defaultSize + ", height="
						+ defaultSize);
			}
			writer.write(", fixedsize=\"true\"");

			// close node statement
			writer.write("];\n");
		}

		// write relations as directed
		for (ONDEXRelation r : graph.getRelations()) {
			// only export visible relations if visibility known
			Attribute visible = r.getAttribute(aaVisible);
			if (visible == null || (Boolean) visible.getValue()) {

				// get all concepts from relation
				ONDEXConcept from = r.getFromConcept();
				ONDEXConcept to = r.getToConcept();

				// make sure all involved concept have been written
				if (writtenConcepts.contains(from)
						&& writtenConcepts.contains(to)) {

					// label edge with relation type
					RelationType rt = r.getOfType();
					String name = rt.getFullname();
					if (name == null || name.trim().length() == 0)
						name = rt.getId();
					name = trimString(name);

					// colour of relation
					String color = attribute2ColorString(r
							.getAttribute(aaColor));

					// just from -> to
					writer.write("\tnode" + from.getId() + " -> node"
							+ to.getId() + " [label=\"" + name + "\", color="
							+ color + "];\n");
				}
			}
		}

		// close writer when finished
		writer.write("}\n");
		writer.flush();
		writer.close();
	}

	/**
	 * Trim a String to a certain length.
	 * 
	 * @param s
	 *            String to trim
	 * @return trimmed String
	 */
	private String trimString(String s) {

		// trim to length
		if (useTrim && s.length() > trimLength) {
			boolean foundSplit = false;
			int half = trimLength / 2;
			int minPos = half - half / 3;
			int maxPos = half - half / 3;
			// first try split on middle space
			int space = s.indexOf(" ", minPos);
			while (space > 0 && s.indexOf(" ", space + 1) < maxPos) {
				space = s.indexOf(" ", space + 1);
			}
			if (space > 0) {
				s = s.substring(0, space) + "\\n"
						+ s.substring(space, s.length());
				foundSplit = true;
			} else {
				// try split on possible dash
				int dash = s.indexOf("-", minPos);
				while (dash > 0 && s.indexOf("-", dash + 1) < maxPos) {
					dash = s.indexOf("-", dash + 1);
				}
				if (dash > 0) {
					s = s.substring(0, dash) + "\\n"
							+ s.substring(dash, s.length());
					foundSplit = true;
				}
			}

			if (foundSplit) {
				int length = Math.min(2 * trimLength - 3, s.length());
				s = s.substring(0, length);
				if (length == 2 * trimLength - 3)
					s = s + "...";
			} else {
				s = s.substring(0, trimLength - 3) + "...";
			}
		}
		return s;
	}

	/**
	 * Returns the hex string representation for a Colour Attribute.
	 * 
	 * @param attribute
	 *            containing colour information
	 * @return hex string
	 */
	private String attribute2ColorString(Attribute attribute) {
		if (attribute != null) {
			Color color = (Color) attribute.getValue();
			String rgb = Integer.toHexString(color.getRGB());
			// for alpha channel cut out
			rgb = rgb.substring(2, rgb.length());
			return "\"#" + rgb + "\"";
		} else {
			return "white";
		}
	}

	/**
	 * Parses a shape of a concept and returns DOT name for it.
	 * 
	 * @param c
	 *            ONDEXConcept with shape Attribute
	 * @return shape name
	 */
	private String gds2ShapeName(ONDEXConcept c) {
		Attribute attribute = c.getAttribute(aaShape);
		if (attribute != null) {
			Integer shapeId = (Integer) attribute.getValue();
			switch (shapeId) {
			case 0:
				return "circle";
			case 1:
				return "rectangle";
			case 2:
				return "ellipse";
			case 3:
				return "triangle";
			case 4:
				return "pentagon";
			case 5:
				return "octagon";
			case 6:
				return "polygon,sides=5,peripheries=3";
			case 7:
				return "polygon,sides=7,peripheries=3";
			case 8:
				return "polygon,sides=9,peripheries=3";
			case 9:
				return "circle";
			default:
				return "box";
			}
		} else {
			return "box";
		}
	}
}
