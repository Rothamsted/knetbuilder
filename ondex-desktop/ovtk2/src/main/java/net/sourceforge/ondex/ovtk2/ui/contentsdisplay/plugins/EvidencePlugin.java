package net.sourceforge.ondex.ovtk2.ui.contentsdisplay.plugins;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AttributePlugin;

/**
 * For specific evidence attributes used in PPI networks.
 * 
 * @author taubertj
 * 
 */
public class EvidencePlugin extends AbstractContentDisplayPlugin implements AttributePlugin {

	private static final int count = 10;

	public EvidencePlugin(ONDEXGraph aog) {
		super(aog);
	}

	@Override
	public String[] getAttributeNames() {
		String[] attrNames = new String[count];
		for (int i = 0; i < count; i++) {
			if (i == 0)
				attrNames[i] = "evidence";
			else
				attrNames[i] = "evidence_" + i;
		}
		return attrNames;
	}

	@Override
	public String getName() {
		return "evidence";
	}

	@Override
	public String getVersion() {
		return "23.05.2011";
	}

	@Override
	public String compileContent(ONDEXEntity e) {
		StringBuffer b = new StringBuffer();

		// check for attribute on both concepts and relations
		AttributeName confAN = aog.getMetaData().getAttributeName("evidence");
		if (confAN != null) {
			Attribute g = e.getAttribute(confAN);
			if (g != null) {
				b.append("<h2>Evidence</h2>");
				b.append(compileSplit(g));

				// add additional possible evidence
				for (int i = 1; i < count; i++) {
					confAN = aog.getMetaData().getAttributeName("evidence_" + i);
					if (confAN != null) {
						g = e.getAttribute(confAN);
						if (g != null)
							b.append(compileSplit(g));
					}
				}
			}
		}

		return b.toString();
	}

	/**
	 * This is very specific code to the value of an evidence attribute.
	 * 
	 * @param g
	 * @return
	 */
	private String compileSplit(Attribute g) {
		StringBuffer b = new StringBuffer();
		String s = (String) g.getValue();
		String[] split = s.split("/");
		if (split.length == 3) {
			// right composition
			b.append("<b>Experiment:</b> " + split[0] + "<br/>");
			b.append("<b>Data Source:</b> " + split[1] + "<br/>");
			b.append("<b>Organism:</b> " + split[2] + "<br/><br/>");
		} else {
			// any other type
			b.append(s + "<br/>");
		}
		return b.toString();
	}

}
