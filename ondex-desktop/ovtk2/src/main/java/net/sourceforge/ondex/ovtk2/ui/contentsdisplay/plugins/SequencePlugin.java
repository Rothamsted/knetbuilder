package net.sourceforge.ondex.ovtk2.ui.contentsdisplay.plugins;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AttributePlugin;

public class SequencePlugin extends AbstractContentDisplayPlugin implements AttributePlugin {

	private List<AttributeName> an_aa, an_na;

	public SequencePlugin(ONDEXGraph aog) {
		super(aog);

		an_na = new ArrayList<AttributeName>(2);
		an_aa = new ArrayList<AttributeName>(2);

		AttributeName at = aog.getMetaData().getAttributeName("AA");
		if (at != null) {
			an_aa.add(at);
		}

		for (int i = 1; i < Integer.MAX_VALUE; i++) {
			AttributeName at_var = aog.getMetaData().getAttributeName("AA" + ':' + i);
			if (at_var != null) {
				an_aa.add(at_var);
			} else {
				break;
			}
		}

		at = aog.getMetaData().getAttributeName("NA");
		if (at != null) {
			an_na.add(at);
		}

		for (int i = 1; i < Integer.MAX_VALUE; i++) {
			AttributeName at_var = aog.getMetaData().getAttributeName("NA" + ':' + i);
			if (at_var != null) {
				an_na.add(at_var);
			} else {
				break;
			}
		}
	}

	// ####METHODS####

	@Override
	public String compileContent(ONDEXEntity e) {
		StringBuffer b = new StringBuffer();
		if (e instanceof ONDEXConcept) {
			ONDEXConcept c = (ONDEXConcept) e;
			if (an_na.size() > 0) {
				for (AttributeName att : an_na) {
					Attribute attribute = c.getAttribute(att);
					if (attribute != null) {
						b.append("<h2>Nucleotide sequence (" + att.getId() + "):</h2>");
						String seq = (String) attribute.getValue();
						b.append(formatSeq(seq));
					}
				}
			}
			if (an_aa.size() > 0) {
				for (AttributeName att : an_aa) {
					Attribute attribute = c.getAttribute(att);
					if (attribute != null) {
						b.append("<h2>Aminoacid sequence (" + att.getId() + "):</h2>");
						String seq = (String) attribute.getValue();
						b.append(formatSeq(seq));
					}
				}
			}
		}
		return b.toString();
	}

	private String formatSeq(String seq) {
		StringBuffer b = new StringBuffer();
		char[] chars = seq.toCharArray();
		int i = 0;
		b.append("<code>");
		for (char c : chars) {
			if (i % 30 == 0 && i > 0)
				b.append("</code><br/><code>");
			else if (i % 10 == 0)
				b.append(" ");
			b.append(c);
			i++;
		}
		b.append("</code><br/>");
		return b.toString();
	}

	@Override
	public String getName() {
		return "Sequence";
	}

	@Override
	public String getVersion() {
		return "31.07.2008";
	}

	@Override
	public String[] getAttributeNames() {
		String[] atts = new String[an_aa.size() + an_na.size()];
		int i = 0;
		for (AttributeName att : an_aa) {
			atts[i] = att.getId();
			i++;
		}
		for (AttributeName att : an_na) {
			atts[i] = att.getId();
			i++;
		}

		return atts;
	}

}
