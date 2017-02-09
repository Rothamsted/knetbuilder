package net.sourceforge.ondex.ovtk2.config;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;

/**
 * Class provides methods to access the program configuration.
 * 
 * @author taubertj
 * 
 */
public class Config {

	/**
	 * contains path to last opened file
	 */
	public static String lastOpenedFile = null;

	/**
	 * contains path to last saved file
	 */
	public static String lastSavedFile = null;

	// contains all config properties
	public static Properties config = new Properties();

	// default color
	public static Color defaultColor = Color.BLUE;

	// default edge size
	public static int defaultEdgeSize = 2;

	// default node size
	public static int defaultNodeSize = 20;

	// default shape
	public static int defaultShape = 3;

	// containing all big help files
	public static String docuDir = null;

	// color of picked edge
	public static Color edgePickedColor = null;

	// contains all language properties
	public static Properties language = new Properties();

	// look and feel
	public static String lookAndFeel = null;

	// color of picked node
	public static Color nodePickedColor = null;

	// ovtkDir set as working director/config if not specified
	public static String ovtkDir = null;

	// same as in Ondex config
	public static boolean isApplet = false;

	// contains mapping of visual attributes
	public static final Properties visual = new Properties();

	static {
		// for dynamic location of help directory
		docuDir = System.getProperty("docu.dir");
		if (docuDir == null) {
			docuDir = new File("config").getAbsolutePath();
		}

		// finding the ovtk config
		ovtkDir = System.getProperty("ovtk.dir");
		if (ovtkDir == null) {
			ovtkDir = new File("config").getAbsolutePath();
			if (!(new File(ovtkDir)).exists()) {
				// DO NOT throw a RuntimeException here, causes Applet to crash
				// throw new RuntimeException("No valid OVTK directory found.");
				System.err.println("No valid OVTK directory found yet.");
			}
		}
	}

	/**
	 * Converts a comma separated RGB String to Color.
	 * 
	 * @param rgb
	 *            String
	 * @return Color
	 */
	public static Color convertToColor(String rgb) {
		String[] array = rgb.split(",");
		int r = Integer.parseInt(array[0]);
		int g = Integer.parseInt(array[1]);
		int b = Integer.parseInt(array[2]);
		return new Color(r, g, b);
	}

	/**
	 * Converts a Color to a comma separated RGB String.
	 * 
	 * @param color
	 *            Color
	 * @return String
	 */
	public static String convertToString(Color color) {
		StringBuffer buf = new StringBuffer();
		buf.append(color.getRed());
		buf.append(",");
		buf.append(color.getGreen());
		buf.append(",");
		buf.append(color.getBlue());
		return buf.toString();
	}

	/**
	 * Returns node color for a given ConceptClass.
	 * 
	 * @param cc
	 *            ConceptClass
	 * @return Color
	 */
	public static Color getColorForConceptClass(ConceptClass cc) {
		String key = "ConceptClass.Color." + cc.getId();
		if (visual.getProperty(key) != null) {
			return convertToColor(visual.getProperty(key));
		} else {
			// maybe parent concept class exist in mapping
			ConceptClass parent = cc.getSpecialisationOf();
			if (parent != null) {
				return getColorForConceptClass(parent);
			}
		}
		return defaultColor;
	}

	/**
	 * Returns node color for a given DataSource.
	 * 
	 * @param ds
	 *            DataSource
	 * @return Color
	 */
	public static Color getColorForDataSource(DataSource ds) {
		String key = "DataSource.Color." + ds.getId();
		if (visual.getProperty(key) != null) {
			return convertToColor(visual.getProperty(key));
		}
		return defaultColor;
	}

	/**
	 * Returns node and edge color for a given EvidenceType.
	 * 
	 * @param et
	 *            EvidenceType
	 * @return Color
	 */
	public static Color getColorForEvidenceType(EvidenceType et) {
		String key = "EvidenceType.Color." + et.getId();
		if (visual.getProperty(key) != null) {
			return convertToColor(visual.getProperty(key));
		}
		return defaultColor;
	}

	/**
	 * Returns edge color for a given RelationType.
	 * 
	 * @param rt
	 *            RelationType
	 * @return Color
	 */
	public static Color getColorForRelationType(RelationType rt) {
		String key = "RelationType.Color." + rt.getId();
		if (visual.getProperty(key) != null) {
			return convertToColor(visual.getProperty(key));
		} else {
			// maybe parent relation type exist in mapping
			RelationType parent = rt.getSpecialisationOf();
			if (parent != null) {
				return getColorForRelationType(parent);
			}
		}
		return defaultColor;
	}

	/**
	 * Returns default color loaded from config.
	 * 
	 * @return Color
	 */
	public static Color getDefaultColor() {
		return defaultColor;
	}

	/**
	 * Returns the operation system name we are running under.
	 * 
	 * @return OS name
	 */
	public static String getOsName() {
		String os = "";
		if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
			os = "windows";
		} else if (System.getProperty("os.name").toLowerCase().indexOf("linux") > -1) {
			os = "linux";
		} else if (System.getProperty("os.name").toLowerCase().indexOf("mac") > -1) {
			os = "mac";
		}

		return os;
	}

	/**
	 * Returns node shape code for a given ConceptClass.
	 * 
	 * @param cc
	 *            ConceptClass
	 * @return shape code
	 */
	public static int getShapeForConceptClass(ConceptClass cc) {
		String key = "ConceptClass.Shape." + cc.getId();
		if (visual.getProperty(key) != null) {
			return Integer.valueOf(visual.getProperty(key));
		} else {
			// maybe parent concept class exist in mapping
			ConceptClass parent = cc.getSpecialisationOf();
			if (parent != null) {
				return getShapeForConceptClass(parent);
			}
		}
		return defaultShape;
	}

	/**
	 * Returns node size for a given ConceptClass
	 * 
	 * @param cc
	 *            ConceptClass
	 * @return node size
	 */
	public static Integer getSizeForConceptClass(ConceptClass cc) {
		String key = "ConceptClass.Size." + cc.getId();
		if (visual.getProperty(key) != null) {
			return Integer.valueOf(visual.getProperty(key));
		} else {
			// maybe parent concept class exist in mapping
			ConceptClass parent = cc.getSpecialisationOf();
			if (parent != null) {
				return getSizeForConceptClass(parent);
			}
		}
		return defaultNodeSize;
	}

	/**
	 * Returns edge size for a given RelationType.
	 * 
	 * @param rt
	 *            RelationType
	 * @return edge size
	 */
	public static Integer getSizeForRelationType(RelationType rt) {
		String key = "RelationType.Size." + rt.getId();
		if (visual.getProperty(key) != null) {
			return Integer.valueOf(visual.getProperty(key));
		} else {
			// maybe parent relation type exist in mapping
			RelationType parent = rt.getSpecialisationOf();
			if (parent != null) {
				return getSizeForRelationType(parent);
			}
		}
		return defaultEdgeSize;
	}

	/**
	 * Reads the Proxy settings from the config.xml and sets them as system
	 * properties.
	 */
	public static void initProxySetting() {
		System.getProperties().put("proxySet", Config.config.getProperty("Proxy.Set"));
		System.getProperties().put("proxyHost", Config.config.getProperty("Proxy.Host"));
		System.getProperties().put("proxyPort", Config.config.getProperty("Proxy.Port"));
	}

	/**
	 * get properties from XML
	 * 
	 * @param inApplet
	 *            running in Applet mode?
	 */
	public static void loadConfig(boolean inApplet) {
		// turn off validation
		System.setProperty("http://xml.org/sax/features/validation", "false");

		System.setProperty("org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser");

		// checking again
		if (ovtkDir == null)
			ovtkDir = System.getProperty("ovtk.dir");

		// only override ondexDir if not yet set
		if (net.sourceforge.ondex.config.Config.ondexDir == null) {
			net.sourceforge.ondex.config.Config.ondexDir = System.getProperty("ondex.dir");
			if (net.sourceforge.ondex.config.Config.ondexDir == null) {
				net.sourceforge.ondex.config.Config.ondexDir = ovtkDir;
			}
		}

		// exception for applet, otherwise strange plugin dir will get created
		if (!inApplet) {
			try {
				OVTK2PluginLoader.getInstance();
			} catch (FileNotFoundException e) {
				// plugin dir ships with distribution, otherwise something is
				// wrong, exit and do not try to create something on the user
				// computer as that might compromise security
				System.err.println("Could not find plugins directory. Please create one.");
				e.printStackTrace();
				System.exit(1);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		try {
			// initialise producer loader at first to load config first
			// all values may be overriden by local files

			// load local config files next
			URL urlProperties, urlEnglish, urlVisual;
			if (isApplet) {
				// when loading from a server
				urlProperties = new URL(ovtkDir + "/" + "config.xml");
				urlEnglish = new URL(ovtkDir + "/" + "english.xml");
				urlVisual = new URL(ovtkDir + "/" + "visual.xml");
			} else {
				// loading from local disk, conform URL
				File file = new File(ovtkDir + File.separator + "config.xml");
				urlProperties = file.toURI().toURL();
				file = new File(ovtkDir + File.separator + "english.xml");
				urlEnglish = file.toURI().toURL();
				file = new File(ovtkDir + File.separator + "visual.xml");
				urlVisual = file.toURI().toURL();
			}
			System.out.println("Trying to load: " + urlProperties);
			config.loadFromXML((InputStream) urlProperties.getContent());
			System.out.println("Trying to load: " + urlEnglish);
			language.loadFromXML((InputStream) urlEnglish.getContent());
			System.out.println("Trying to load: " + urlVisual);
			visual.loadFromXML((InputStream) urlVisual.getContent());
			// only for local usage
			File file = new File(ovtkDir + File.separator + "user.xml");
			if (file.exists()) {
				System.out.println("Trying to load: " + file.getAbsolutePath());
				config.loadFromXML(new FileInputStream(file));
			}
			lookAndFeel = visual.getProperty("Default.LookAndFeel");
			defaultShape = Integer.parseInt(visual.getProperty("Default.Shape"));
			defaultNodeSize = Integer.parseInt(visual.getProperty("Default.NodeSize"));
			defaultEdgeSize = Integer.parseInt(visual.getProperty("Default.EdgeSize"));
			defaultColor = convertToColor(visual.getProperty("Default.Color"));
			nodePickedColor = convertToColor(visual.getProperty("Picked.Node"));
			edgePickedColor = convertToColor(visual.getProperty("Picked.Edge"));

			// parse last open and save locations from config
			if (config.getProperty("Program.LastOpened") != null) {
				File dir = new File(config.getProperty("Program.LastOpened"));
				if (dir.canRead())
					lastOpenedFile = dir.getAbsolutePath();
			}

			if (config.getProperty("Program.LastSaved") != null) {
				File dir = new File(config.getProperty("Program.LastSaved"));
				if (dir.canRead())
					lastSavedFile = dir.getAbsolutePath();
			}

		} catch (InvalidPropertiesFormatException ipfe) {
			System.err.println("Error in " + ovtkDir + File.separator + "config.xml " + ipfe.getMessage());
			ipfe.printStackTrace();
			System.exit(1);
		} catch (FileNotFoundException fnfe) {
			System.err.println("Error in " + ovtkDir + File.separator + "config.xml " + fnfe.getMessage());
			fnfe.printStackTrace();
			System.exit(1);
		} catch (IOException ioe) {
			System.err.println("Error in " + ovtkDir + File.separator + "config.xml " + ioe.getMessage());
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Saves changes made to the visual properties.
	 * 
	 */
	public static void saveVisual() {
		File file = new File(ovtkDir + File.separator + "visual.xml");
		System.out.println("Trying to save: " + file.getAbsolutePath());
		try {
			visual.storeToXML(new FileOutputStream(file), "Modified by " + config.getProperty("Program.Name") + " - Version " + config.getProperty("Program.Version"));
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
		}
	}

	/**
	 * Save changes made to the config properties.
	 * 
	 */
	public static void saveConfig() {
		if (lastOpenedFile != null)
			config.setProperty("Program.LastOpened", lastOpenedFile);
		if (lastSavedFile != null)
			config.setProperty("Program.LastSaved", lastSavedFile);

		File file = new File(ovtkDir + File.separator + "config.xml");
		System.out.println("Trying to save: " + file.getAbsolutePath());
		try {
			config.storeToXML(new FileOutputStream(file), "Modified by " + Config.config.getProperty("Program.Name") + " - Version " + Config.config.getProperty("Program.Version"));
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
		}
	}

}
