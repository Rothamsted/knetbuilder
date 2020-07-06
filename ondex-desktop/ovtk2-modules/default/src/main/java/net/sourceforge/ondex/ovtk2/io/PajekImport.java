/*
 * Created on 28-Apr-2005
 *
 */
package net.sourceforge.ondex.ovtk2.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;

/**
 * This class parses files in PAJEK format.
 * 
 * @author taubertj
 */
public class PajekImport implements OVTK2IO {

	private ONDEXGraph og;

	public PajekImport() {
	}

	/**
	 * Parses the content of file in PAJEK format.
	 */
	public void parseFile(InputStream inStream) throws Exception {

		// check for/create cv
		DataSource dataSource = og.getMetaData().getDataSource("PAJEK");
		if (dataSource == null)
			dataSource = og.getMetaData().getFactory()
					.createDataSource("PAJEK");

		// check for/create cc
		ConceptClass cc = og.getMetaData().getConceptClass("Thing");
		if (cc == null)
			cc = og.getMetaData().getFactory().createConceptClass("Thing");

		// check for/create rt
		RelationType rt = og.getMetaData().getRelationType("r");
		if (rt == null)
			rt = og.getMetaData().getFactory().createRelationType("r");

		// check for/create attrname
		AttributeName attrName = og.getMetaData().getAttributeName("labels");
		if (attrName == null)
			attrName = og
					.getMetaData()
					.getFactory()
					.createAttributeName("labels", "splitted edge labels",
							String.class);

		// check for/create et
		EvidenceType et = og.getMetaData().getEvidenceType("IMPD");
		if (et == null)
			et = og.getMetaData().getFactory().createEvidenceType("IMPD");

		// init states
		boolean inVertices = false;
		boolean inArcs = false;

		// mapping from PAJEK file ids to concept ids
		Map<Integer, Integer> idMapping = new Hashtable<Integer, Integer>();

		// open reader for file
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inStream));
		while (reader.ready()) {
			String line = reader.readLine();

			// first we start with nodes
			if (line.startsWith("*") && !inVertices && !inArcs) {
				line = line.substring(0, line.indexOf(" "));
				inVertices = true;
			}

			// the second * is for edges
			else if (line.startsWith("*") && inVertices) {
				inVertices = false;
				inArcs = true;
			}

			// ignore third * for now
			else if (line.startsWith("*") && inArcs) {
				break;
			}

			// parse nodes from file
			else if (inVertices) {

				// parse pajek id
				line = line.trim();
				String number = line.substring(0, line.indexOf(" "));
				int id_int = Integer.parseInt(number);

				// get name of node
				line = line.substring(number.length(), line.length()).trim();
				String name = line.substring(1, line.lastIndexOf('"'));

				// create concept
				ONDEXConcept c = og.getFactory().createConcept(id_int + "",
						dataSource, cc, et);
				c.createConceptName(name, true);

				// add to mapping
				idMapping.put(id_int, c.getId());
			}

			// parse edges from file
			else if (inArcs) {

				// parse from pajek id
				line = line.trim();
				String number = line.substring(0, line.indexOf(" "));
				int fromID = Integer.parseInt(number);

				// parse to pajek id
				line = line.substring(number.length(), line.length()).trim();
				if (line.indexOf(" ") > -1)
					number = line.substring(0, line.indexOf(" "));
				else
					number = line.substring(0, line.length());
				int toID = Integer.parseInt(number);

				// names associated with relation
				line = line.substring(number.length(), line.length()).trim();
				String name = null;
				if (line.length() > 0 && line.contains("\\\""))
					name = line.substring(line.indexOf('"') + 1,
							line.lastIndexOf('"'));
				else if (line.length() > 0)
					name = line;

				// get concepts and create relation
				ONDEXConcept from = og.getConcept(idMapping.get(fromID));
				ONDEXConcept to = og.getConcept(idMapping.get(toID));
				ONDEXRelation r = og.getFactory().createRelation(from, to, rt,
						et);

				// process name
				if (name != null) {
					// split name and add as Attribute
					String[] result = name.split(" ");
					for (int i = 0; i < result.length; i++) {
						r.createAttribute(attrName, result[i], true);
					}
				}
			}
		}

		reader.close();

	}

	@Override
	public void setGraph(ONDEXGraph graph) {
		this.og = graph;
	}

	@Override
	public void start(File file) throws Exception {
		parseFile(new FileInputStream(file));
	}

	@Override
	public String getExt() {
		return "net";
	}

	@Override
	public boolean isImport() {
		return true;
	}

	/**
	 * Work-around method for reuse in applet.
	 * 
	 * @param filename
	 * @throws Exception
	 */
	public void start(String filename) throws Exception {
		// do decided where file is coming from
		URL url;
		if (filename.startsWith("http:") || filename.startsWith("file:")
				|| filename.startsWith("https:")) {
			// when loading from a server
			url = new URL(filename);
		} else {
			File file = new File(filename);
			url = file.toURI().toURL();
		}

		InputStream inStream = (InputStream) url.getContent();
		
		parseFile(inStream);
	}
}
