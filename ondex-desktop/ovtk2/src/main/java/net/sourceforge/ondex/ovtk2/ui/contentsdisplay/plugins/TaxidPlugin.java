package net.sourceforge.ondex.ovtk2.ui.contentsdisplay.plugins;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AttributePlugin;

/**
 * Extracts taxids and links them to NCBI Taxonomy webpage.
 * 
 * @author Jochen Weile, B.Sc.
 */
public class TaxidPlugin extends AbstractContentDisplayPlugin implements AttributePlugin {

	// ####FIELDS####

	AttributeName taxid_an;

	// ####CONSTRUCTOR####

	/**
	 * Constructor.
	 */
	public TaxidPlugin(ONDEXGraph aog) {
		super(aog);
		taxid_an = aog.getMetaData().getAttributeName("TAXID");
	}

	// ####METHODS####

	/**
	 * @see AbstractContentDisplayPlugin.compileContent()
	 */
	@Override
	public String compileContent(ONDEXEntity e) {
		StringBuffer b = new StringBuffer();

		if (taxid_an != null) {
			// get attribute on both concepts and relations
			Attribute attribute = e.getAttribute(taxid_an);
			if (attribute != null) {
				String taxid = attribute.getValue().toString();
				b.append("<h2>Taxonomy</h2>");
				b.append("<a href=\"http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Info&id=" + taxid + "\">" + taxid + "</a>");
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
		return "Taxonomy ID";
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
		return new String[] { "TAXID" };
	}

}
