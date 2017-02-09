package net.sourceforge.ondex.ovtk2.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.init.Initialisation;

/**
 * Imports the Network Workbench format
 * 
 * @author lysenkoa
 */
public class NWBImport implements OVTK2IO {
	public static final String asterisk = "*";

	public static final String nodes = "*Nodes";

	public static final String directededges = "*DirectedEdges";

	public static final String undirectededges = "*UndirectedEdges";

	public static final String id = "id";

	public static final String SOURCE = "source";

	public static final String TARGET = "target";

	public static final String weight = "weight";

	public static final String nwint = "int";

	public static final String nwstring = "string";

	public static final String nwfloat = "float";

	public static final String PID = "id";

	public static final String NAME = "label";

	public static final String NAME1 = "name";

	public static final String CLASS = "class";

	public static final String TYPE = "type";

	public static final String WEIGHT = "weight";

	public static final String EVIDENCE = "evidence";

	public static final String NWBWeight = "NWBWeight";

	private ConceptClass thing;

	private RelationType rtype;

	private ONDEXGraph aog;

	private boolean applet = false;

	public NWBImport() {
	}

	/**
	 * Set applet mode, not load metadata
	 * 
	 * @param applet
	 */
	public NWBImport(boolean applet) {
		this.applet = applet;
	}

	private EvidenceType getEvType(ONDEXGraph aog, String str) {
		EvidenceType ev = aog.getMetaData().getEvidenceType(str);
		if (ev == null) {
			ev = aog.getMetaData().getFactory().createEvidenceType(str);
		}
		return ev;
	}

	private AttributeName getAttributeName(ONDEXGraph aog, String str,
			Class<?> cls) {
		if (str.equals(WEIGHT))
			str = NWBWeight;
		AttributeName an = aog.getMetaData().getAttributeName(str);
		if (an == null) {
			an = aog.getMetaData().getFactory().createAttributeName(str, cls);
		} else if (!an.getDataType().isAssignableFrom(cls)
				&& !Number.class.isAssignableFrom(cls)) {
			System.err
					.println("Error: Attribute name "
							+ str
							+ " is already part of Ondex controlled vocabulary with "
							+ an.getDataType()
							+ ". Change NWB "
							+ cls
							+ " to conform or choose a differnt name for this attribute.");
			return null;
		}

		return an;
	}

	private Object castData(Class<?> cls, String str) {
		Object data = null;
		if (str.equals(asterisk))
			return data;
		if (cls.equals(Double.class)) {
			data = Double.valueOf(str.trim());
		} else if (cls.equals(Integer.class)) {
			try {
				data = Integer.valueOf(str.trim());
			} catch (NumberFormatException e) {
				System.err.println("Not a valid Integer: " + str.trim());
				data = null;
			}
		} else if (cls.equals(Float.class)) {
			data = Float.valueOf(str.trim());
		} else {
			data = str;
		}
		return data;
	}

	private ConceptClass getConceptClass(ONDEXGraph aog, String str) {
		ConceptClass cc;
		cc = aog.getMetaData().getConceptClass(str);
		if (cc == null) {
			cc = thing;
		}
		return cc;
	}

	private RelationType getRelationType(ONDEXGraph aog, String str) {
		RelationType rt;
		rt = aog.getMetaData().getRelationType(str);
		if (rt == null) {
			rt = rtype;
		}
		return rt;
	}

	private Class<?> getClass(String name) {
		if (name.equals(nwstring)) {
			return String.class;
		} else if (name.equals(nwfloat)) {
			return Double.class;
		} else if (name.equals(nwint)) {
			return Integer.class;
		}
		return Object.class;
	}

	public void parseFile(InputStream inStream) throws Exception {
		Map<String, Integer> nodeKey = new HashMap<String, Integer>();
		Map<String, Integer> edgeKey = new HashMap<String, Integer>();
		Map<AttributeName, Integer> gds = new HashMap<AttributeName, Integer>();
		List<Integer> names = new ArrayList<Integer>();
		Map<Integer, DataSource> cvs = new HashMap<Integer, DataSource>();
		List<Integer> evidence = new ArrayList<Integer>();
		Map<String, ONDEXConcept> pidToc = new HashMap<String, ONDEXConcept>();
		Class<?>[] dataTypes = new Class<?>[0];
		boolean nodeMode = true;
		DataSource dataSource = null;
		EvidenceType ev = null;

		if (!applet) {
			try {
				File f = new File("ondex_metadata.xml");
				if (!f.exists())
					f = new File("data" + File.separator + "ondex_metadata.xml");
				if (!f.exists())
					f = new File("data" + File.separator + "xml"
							+ File.separator + "ondex_metadata.xml");
				if (f.exists()) {
					File xsd = new File(f.getAbsolutePath()
							.substring(
									0,
									f.getAbsolutePath().lastIndexOf(
											File.separator) + 1)
							+ "ondex.xsd");
					Initialisation init = new Initialisation(f, xsd);
					init.initMetaData(aog);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
		String strLine;
		dataSource = aog.getMetaData().getDataSource("UC");
		if (dataSource == null) {
			dataSource = aog.getMetaData().getFactory().createDataSource("UC");
		}
		ev = aog.getMetaData().getEvidenceType("IC");
		if (ev == null) {
			ev = aog.getMetaData().getFactory().createEvidenceType("IC");
		}
		thing = aog.getMetaData().getConceptClass("Thing");
		if (thing == null) {
			thing = aog.getMetaData().getFactory().createConceptClass("Thing");
		}
		rtype = aog.getMetaData().getRelationType("r");
		if (rtype == null) {
			rtype = aog.getMetaData().getFactory().createRelationType("r");
		}
		while ((strLine = br.readLine()) != null) {
			strLine = strLine.trim();
			if (strLine.startsWith("#") || strLine.equals(""))
				continue;
			if (strLine.startsWith(nodes)) {
				nodeMode = true;
				while ((strLine = br.readLine()) != null
						&& (strLine.startsWith("#") || strLine.equals("")))
					;
				if (strLine == null)
					break;
				gds.clear();
				names.clear();
				cvs.clear();
				evidence.clear();
				final String[] rawKeys = strLine.split("\t");
				dataTypes = new Class<?>[rawKeys.length];
				for (int i = 0; i < rawKeys.length; i++) {
					final String key = rawKeys[i];
					dataTypes[i] = getClass(key.split("\\*")[1]);
					if (key.startsWith(PID)) {
						nodeKey.put(PID, i);
					} else if (key.startsWith(CLASS)) {
						nodeKey.put(CLASS, i);
					} else if (key.startsWith(EVIDENCE)) {
						evidence.add(i);
					} else if (key.startsWith(NAME) || key.startsWith(NAME1)) {
						names.add(i);
					} else if (aog.getMetaData().getDataSource(
							key.split("\\*")[0]) != null) {
						cvs.put(i,
								aog.getMetaData().getDataSource(
										key.split("\\*")[0]));
					} else {
						final AttributeName an = getAttributeName(aog,
								key.split("\\*")[0], dataTypes[i]);
						if (an != null) {
							gds.put(an, i);
						}
					}
				}
			} else if (strLine.startsWith(directededges)
					|| strLine.startsWith(undirectededges)) {
				nodeMode = false;
				while ((strLine = br.readLine()) != null
						&& (strLine.startsWith("#") || strLine.equals("")))
					;
				if (strLine == null)
					break;
				edgeKey.clear();
				gds.clear();
				names.clear();
				cvs.clear();
				evidence.clear();
				final String[] rawKeys = strLine.split("\t");
				dataTypes = new Class<?>[rawKeys.length];
				for (int i = 0; i < rawKeys.length; i++) {
					final String key = rawKeys[i];
					dataTypes[i] = getClass(key.split("\\*")[1]);
					if (key.startsWith(SOURCE)) {
						edgeKey.put(SOURCE, i);
					} else if (key.startsWith(TYPE)) {
						edgeKey.put(TYPE, i);
					} else if (key.startsWith(TARGET)) {
						edgeKey.put(TARGET, i);
					} else if (key.startsWith(EVIDENCE)) {
						evidence.add(i);
					} else {
						final AttributeName an = getAttributeName(aog,
								key.split("\\*")[0], dataTypes[i]);
						if (an != null) {
							gds.put(an, i);
						}
					}
				}
			} else {
				String[] line = strLine.split("\t");
				for (int i = 0; i < line.length; i++) {
					if (dataTypes[i].equals(String.class)) {
						line[i] = line[i].trim();
						if (!line[i].equals("") && line[i].endsWith("\"")
								&& line[i].startsWith("\"")) {
							line[i] = line[i]
									.substring(1, line[i].length() - 1);
						}
					}
				}
				if (nodeMode) {
					String pid = line[nodeKey.get(PID)];
					ConceptClass cc;
					if (nodeKey.get(CLASS) == null) {
						cc = thing;
					} else {
						cc = getConceptClass(aog, line[nodeKey.get(CLASS)]);
					}
					ONDEXConcept c = aog.getFactory().createConcept(pid,
							dataSource, cc, ev);
					pidToc.put(pid, c);
					for (Entry<AttributeName, Integer> ent : gds.entrySet()) {
						AttributeName an = ent.getKey();
						c.createAttribute(
								an,
								castData(an.getDataType(), line[ent.getValue()]),
								false);
					}
					for (Integer e : evidence) {
						c.addEvidenceType(getEvType(aog, line[e]));
					}
					for (Integer n : names) {
						c.createConceptName(line[n], true);
					}
					for (Entry<Integer, DataSource> ent : cvs.entrySet()) {
						c.createConceptAccession(line[ent.getKey()],
								ent.getValue(), false);
					}
				} else {
					RelationType rts;
					if (edgeKey.get(TYPE) == null) {
						rts = rtype;
					} else {
						rts = getRelationType(aog, line[edgeKey.get(TYPE)]);
					}
					ONDEXRelation r = aog.getFactory().createRelation(
							pidToc.get(line[edgeKey.get(SOURCE)]),
							pidToc.get(line[edgeKey.get(TARGET)]), rts, ev);
					for (Entry<AttributeName, Integer> ent : gds.entrySet()) {
						AttributeName an = ent.getKey();
						r.createAttribute(
								an,
								castData(an.getDataType(), line[ent.getValue()]),
								false);
					}
					for (Integer e : evidence) {
						r.addEvidenceType(getEvType(aog, line[e]));
					}
				}
			}
		}
		br.close();

	}

	@Override
	public void setGraph(ONDEXGraph graph) {
		this.aog = graph;
	}
	
	@Override
	public void start(File file) throws Exception {
		parseFile(new FileInputStream(file));
	}

	@Override
	public String getExt() {
		return "nwb";
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
