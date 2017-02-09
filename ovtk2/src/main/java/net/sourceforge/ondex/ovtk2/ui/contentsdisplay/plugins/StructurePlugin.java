package net.sourceforge.ondex.ovtk2.ui.contentsdisplay.plugins;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AttributePlugin;

/**
 * Displays the structure String of a CarbBank entry.
 * 
 * @author taubertj
 */
public class StructurePlugin extends AbstractContentDisplayPlugin implements AttributePlugin {

	// ####FIELDS####

	AttributeName an;

	// ####CONSTRUCTOR####

	/**
	 * Constructor.
	 */
	public StructurePlugin(ONDEXGraph aog) {
		super(aog);
		an = aog.getMetaData().getAttributeName("structure");
	}

	// ####METHODS####

	/**
	 * @see AbstractContentDisplayPlugin.compileContent()
	 */
	@Override
	public String compileContent(ONDEXEntity e) {

		if (an != null) {
			StringBuffer b = new StringBuffer("");
			if (e instanceof ONDEXConcept) {
				ONDEXConcept c = (ONDEXConcept) e;
				Attribute attribute = c.getAttribute(an);
				displayStructure(b, attribute);
			} else if (e instanceof ONDEXRelation) {
				ONDEXRelation r = (ONDEXRelation) e;
				Attribute attribute = r.getAttribute(an);
				displayStructure(b, attribute);
			}
			return b.toString();
		} else
			return "";
	}

	private void displayStructure(StringBuffer b, Attribute attribute) {
		if (attribute != null) {
			String string = attribute.getValue().toString();
			b.append("<h2>Glycostructure</h2>");
			b.append("<pre>");
			b.append(string);
			b.append("</pre>");
		}
	}

	/**
	 * guess what.
	 * 
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#getName()
	 */
	@Override
	public String getName() {
		return "Structure";
	}

	/**
	 * da version.
	 * 
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#getVersion()
	 */
	@Override
	public String getVersion() {
		return "17.06.2009";
	}

	@Override
	public String[] getAttributeNames() {
		return new String[] { "structure" };
	}

}
