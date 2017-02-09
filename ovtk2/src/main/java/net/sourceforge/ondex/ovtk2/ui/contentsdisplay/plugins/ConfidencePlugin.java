/**
 *
 */
package net.sourceforge.ondex.ovtk2.ui.contentsdisplay.plugins;

import java.text.NumberFormat;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AttributePlugin;

/**
 * @author Jochen Weile, B.Sc.
 */
public class ConfidencePlugin extends AbstractContentDisplayPlugin implements AttributePlugin {

	public ConfidencePlugin(ONDEXGraph aog) {
		super(aog);
	}

	/**
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#compileContent(net.sourceforge.ondex.core.AbstractONDEXEntity)
	 */
	@Override
	public String compileContent(ONDEXEntity e) {
		StringBuffer b = new StringBuffer();

		// check for attribute name
		AttributeName confAN = aog.getMetaData().getAttributeName("CONF");
		if (confAN != null) {

			// get attribute of either concept or relation
			Attribute g = e.getAttribute(confAN);
			if (g != null) {
				double conf = (Double) g.getValue();
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMaximumFractionDigits(4);
				b.append("<h2>Confidence information</h2>");
				b.append("<i>c</i> = " + nf.format(conf) + "<br/>");
			}
		}

		return b.toString();
	}

	/**
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#getName()
	 */
	@Override
	public String getName() {
		return "Confidence";
	}

	/**
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#getVersion()
	 */
	@Override
	public String getVersion() {
		return "23.05.2011";
	}

	@Override
	public String[] getAttributeNames() {
		return new String[] { "CONF" };
	}
}
