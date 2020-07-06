package net.sourceforge.ondex.ovtk2.ui.contentsdisplay.plugins.generic;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin;

/**
 * @author hindlem
 */
public class StringPlugin extends AbstractContentDisplayPlugin {

	private AttributeName att;

	public StringPlugin(ONDEXGraph aog, AttributeName att) {
		super(aog);
		this.att = att;
	}

	/**
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#compileContent(net.sourceforge.ondex.core.AbstractONDEXEntity)
	 */
	@Override
	public String compileContent(ONDEXEntity e) {
		StringBuffer b = new StringBuffer();

		// get attribute from concept and relation
		Attribute g = e.getAttribute(att);
		if (g != null && g.getValue() instanceof String) {
			String string = (String) g.getValue();
			// make hyper-links explicit for PLUGINS-52
			if (string.startsWith("http://") || string.startsWith("https://")) {
				string = "<a href=\"" + string + "\">" + string + "</a>";
			}
			String name = att.getFullname();
			if (name == null || name.trim().length() == 0)
				name = att.getId();
			b.append("<h2>" + name + "</h2>");
			b.append(string + "<br/>");
		}

		return b.toString();
	}

	/**
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#getName()
	 */
	@Override
	public String getName() {
		return "String";
	}

	/**
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#getVersion()
	 */
	@Override
	public String getVersion() {
		return "23.05.2011";
	}

}
