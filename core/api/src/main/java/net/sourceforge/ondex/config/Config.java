package net.sourceforge.ondex.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

/**
 * Class provides methods to access the program configuration.
 * 
 * @author taubertj
 */
public class Config {

	/**
	 * Path for other java libraries
	 */
	public static String libDir;

	/**
	 * The path to the ONDEX data directory.
	 */
	public static String ondexDir;

	/**
	 * used for replacing working directories
	 */
	public static final String ONDEXDIR = "$ONDEXDIR";

	/**
	 * Path which contains backend plugins
	 */
	public static String pluginDir;

	/**
	 * Contains all merged properties for ONDEX load from XMLs.
	 */
	public final static Properties properties = new Properties();
	
	/**
	 * indicates when used within applet 
	 */
	public static boolean isApplet = false;

	/**
	 * Get the properties for ONDEX from XML.
	 */
	static {
		ondexDir = System.getProperty("ondex.dir");
		if (ondexDir == null) {
			ondexDir = new File("data").getAbsolutePath();
			boolean found = false;
			if (new File(ondexDir).exists()) {
				for (String file : new File("data").getAbsoluteFile().list()) {
					// only load the config if the relative dir contains config
					if (file.equals("config.xml")) {
						try {
							loadConfig();
						} catch (IOException e) {
							System.err
									.println("Failed to initialise Config. Some parts of ONDEX may not work.");
							e.printStackTrace();
						}
						if (Config.properties.getProperty("flags.debug") != null
								&& Config.properties.getProperty("flags.debug")
										.length() > 0) {
							found = true;
						}
					}
				}
				if (!found) {
					System.err.println("Attempting to use wrong ondex.dir");
					// System.exit(1);
				}
			}
		} else {
			boolean found = false;
			try {
				loadConfig();
			} catch (IOException e) {
				System.err
						.println("Failed to initialise Config. Some parts of ONDEX may not work.");
				e.printStackTrace();
			}
			if (Config.properties.getProperty("flags.debug") != null
					&& Config.properties.getProperty("flags.debug").length() > 0) {
				found = true;
			}
			if (!found) {
				System.err.println("Attempting to use wrong ondex.dir");
				// System.exit(1);
			}
		}

		// for loading Ondex plugins
		pluginDir = System.getProperty("ondex.plugin.dir");
		if (pluginDir == null) {
			pluginDir = (new File(new File(ondexDir).getAbsoluteFile()
					.getParentFile(), "plugins")).getAbsolutePath();
		}

		// for getting other system dependencies
		libDir = System.getProperty("ondex.lib.dir");
		if (libDir == null) {
			libDir = (new File(new File(ondexDir).getAbsoluteFile()
					.getParentFile(), "lib")).getAbsolutePath();
		}
	}

	/**
	 * Load backend configuration from config.xml
	 * 
	 * @throws IOException
	 */
	public static void loadConfig() throws IOException {

		URL urlConfig;
		URL urlLog4j;

		// required for Ondex applet when loading from web server
		if (isApplet) {
			urlConfig = new URL(ondexDir + "/" + "config.xml");
			urlLog4j = new URL(ondexDir + "/" + "log4j.properties");
		} else {
			// should be the default on a desktop
			File file = new File(ondexDir + File.separator + "config.xml")
					.getAbsoluteFile();
			urlConfig = file.toURI().toURL();
			file = new File(ondexDir + File.separator + "log4j.properties")
					.getAbsoluteFile();
			urlLog4j = file.toURI().toURL();
		}
		System.out.println("[ONDEX - core] Trying to load config at: " + urlConfig);
		properties.loadFromXML((InputStream) urlConfig.getContent());

		System.out.println("[ONDEX - core] Trying to load log4j at: " + urlLog4j);
		PropertyConfigurator.configure(urlLog4j);
	}

	/**
	 * A method to revert the changes made by the workingDirToVar method
	 * 
	 * @param input
	 * @return path with substitution made
	 */
	public static String varToWorkingDir(String input) {
		if (input == null)
			return null;
		Config.ondexDir = Config.ondexDir.replace("\\", "/");
		String unterminated = Config.ondexDir;
		if (Config.ondexDir.endsWith(File.separator)) {
			unterminated = Config.ondexDir.substring(0,
					Config.ondexDir.length() - 1);
		}
		return input.replace(ONDEXDIR, unterminated);
	}

	/**
	 * A method to find and substitute ondex working directory in a path
	 * 
	 * @param input
	 *            - path that will be checked
	 * @return path with substitution made
	 */
	public static String workingDirToVar(String input) {
		if (input == null)
			return null;
		Config.ondexDir = Config.ondexDir.replace("\\", "/");
		String unterminated = Config.ondexDir;
		if (Config.ondexDir.endsWith(File.separator)) {
			unterminated = Config.ondexDir.substring(0,
					Config.ondexDir.length() - 1);
		}
		String slashVersion = unterminated.replace(File.separator, "/");
		input = input.replace(slashVersion, ONDEXDIR);
		return input;
	}

	/**
	 * prevent initialisation from outside
	 */
	private Config() {
	}

}
