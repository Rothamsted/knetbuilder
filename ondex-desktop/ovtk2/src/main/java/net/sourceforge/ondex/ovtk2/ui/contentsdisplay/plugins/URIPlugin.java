package net.sourceforge.ondex.ovtk2.ui.contentsdisplay.plugins;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AttributePlugin;

/**
 * Display URI
 * 
 * @author Keywan
 */
public class URIPlugin extends AbstractContentDisplayPlugin implements AttributePlugin {

	private AttributeName uri_an;
	private AttributeName url_an;

	/**
	 * Constructor.
	 */
	public URIPlugin(ONDEXGraph aog) {
		super(aog);
		uri_an = aog.getMetaData().getAttributeName("URI");
		url_an = aog.getMetaData().getAttributeName("URL");
	}

	/**
	 * @see AbstractContentDisplayPlugin.compileContent()
	 */
	@Override
	public String compileContent(ONDEXEntity e) {
		StringBuffer b = new StringBuffer("");

		if (uri_an != null) {
			// get attribute on concepts and relations
			Attribute attribute = e.getAttribute(uri_an);
			if (attribute != null) {
				String uri = attribute.getValue().toString();
				b.append("<h2>URI</h2>");
				b.append("<a href=\"" + uri + "\">" + uri + "</a>");
			}
		}

		if (url_an != null) {
			// get attribute on concepts and relations
			Attribute attribute = e.getAttribute(url_an);
			if (attribute != null) {
				String url = attribute.getValue().toString();
				b.append("<h2>URL</h2>");
				b.append("<a href=\"" + url + "\">" + url + "</a>");
			}
		}

		return b.toString();
	}

	/**
	 * guess what.
	 * 
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#getName()
	 */
	@Override
	public String getName() {
		return "URI ID";
	}

	/**
	 * da version.
	 * 
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#getVersion()
	 */
	@Override
	public String getVersion() {
		return "23.05.2011";
	}

	@Override
	public String[] getAttributeNames() {
		return new String[] { "URI", "URL" };
	}

}
