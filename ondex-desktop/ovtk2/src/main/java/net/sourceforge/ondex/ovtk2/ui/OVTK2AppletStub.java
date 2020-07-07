package net.sourceforge.ondex.ovtk2.ui;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * OVTK2 applet stub. Can be used to run java applets within Ondex. Based on
 * http://java.sun.com/developer/technicalArticles/Programming/TurningAnApplet/
 * 
 * @author peschr
 * 
 */
public class OVTK2AppletStub implements AppletStub {
	// properties for the applet
	private HashMap<String, String> properties;
	// executed java applet
	private Applet applet;

	/**
	 * Creates a new OVTK2AppletStub instance and initializes their parameters.
	 * 
	 * @param applet
	 *            Applet instance
	 * @param properties
	 *            Hashtable with stored properties
	 */
	public OVTK2AppletStub(Applet applet, HashMap<String, String> properties) {
		this.applet = applet;
		System.out.println(properties);
		this.properties = properties;
	}

	/**
	 * Calls the applet's resize
	 * 
	 * @param width
	 * @param height
	 * @return void
	 */
	public void appletResize(int width, int height) {
		applet.resize(width, height);
	}

	/**
	 * Returns the applet's context, which is null in this case. This is an area
	 * where more creative programming work can be done to try and provide a
	 * context
	 * 
	 * @return AppletContext Always null
	 */
	public AppletContext getAppletContext() {
		return null;
	}

	/**
	 * Returns the CodeBase. If a host parameter isn't provided in the command
	 * line arguments, the URL is based on InetAddress.getLocalHost(). The
	 * protocol is "file:"
	 * 
	 * @return URL
	 */
	public java.net.URL getCodeBase() {
		String host;
		if ((host = getParameter("host")) == null) {
			try {
				host = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}

		java.net.URL u = null;
		try {
			u = new java.net.URL("file://" + host);
		} catch (Exception e) {
		}
		return u;
	}

	/**
	 * Returns getCodeBase
	 * 
	 * @return URL
	 */
	public java.net.URL getDocumentBase() {
		return getCodeBase();
	}

	/**
	 * Returns the corresponding command line value
	 * 
	 * @return String
	 */
	public String getParameter(String p) {
		return properties.get(p);
	}

	/**
	 * Applet is always true
	 * 
	 * @return boolean True
	 */
	public boolean isActive() {
		return true;
	}
}
