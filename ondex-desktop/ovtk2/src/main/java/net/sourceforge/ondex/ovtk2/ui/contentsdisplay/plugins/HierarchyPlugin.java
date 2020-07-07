package net.sourceforge.ondex.ovtk2.ui.contentsdisplay.plugins;

import java.text.NumberFormat;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin;

/**
 * @author Jochen Weile, B.Sc.
 */
public class HierarchyPlugin extends AbstractContentDisplayPlugin {

	// ####FIELDS####

	AttributeName an_theta, an_logl;

	// ####CONSTRUCTOR####

	/**
	 * @param s
	 * @param aog
	 */
	public HierarchyPlugin(ONDEXGraph aog) {
		super(aog);
		an_theta = aog.getMetaData().getAttributeName("THETA");
		an_logl = aog.getMetaData().getAttributeName("LOGL");
	}

	// ####METHODS####

	/**
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#compileContent(net.sourceforge.ondex.core.AbstractONDEXEntity)
	 */
	@Override
	public String compileContent(ONDEXEntity e) {
		if (an_theta != null && an_logl != null) {

			StringBuffer b = new StringBuffer();

			if (e instanceof ONDEXConcept) {

				ConceptClass cc_hier = aog.getMetaData().getConceptClass("HierarchyNode");

				ONDEXConcept c = (ONDEXConcept) e;
				if (c.getOfType().equals(cc_hier)) {

					NumberFormat nf = NumberFormat.getInstance();
					nf.setMaximumFractionDigits(4);

					b.append("<h2>Hierarchy information</h2>");
					Attribute attribute = c.getAttribute(an_theta);
					if (attribute != null) {
						double theta = (Double) attribute.getValue();
						b.append("<i>&theta;</i> = " + nf.format(theta) + "<br/>");
					}

					attribute = c.getAttribute(an_logl);
					if (attribute != null) {
						double f = (Double) attribute.getValue();
						b.append("<i>f<sub>i</sub></i> = " + nf.format(f) + "<br/>");
					}
				}

			}

			return b.toString();
		} else
			return "";
	}

	/**
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#getName()
	 */
	@Override
	public String getName() {
		return "Hierarchy";
	}

	/**
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#getVersion()
	 */
	@Override
	public String getVersion() {
		return "31.07.2008";
	}

}
