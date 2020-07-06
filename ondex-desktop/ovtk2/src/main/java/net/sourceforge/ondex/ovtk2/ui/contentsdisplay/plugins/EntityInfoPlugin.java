package net.sourceforge.ondex.ovtk2.ui.contentsdisplay.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.config.ValidatorRegistry;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin;
import net.sourceforge.ondex.ovtk2.util.OVTKProgressMonitor;
import net.sourceforge.ondex.tools.threading.monitoring.IndeterminateProcessAdapter;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;
import net.sourceforge.ondex.workflow.engine.Engine;

/**
 * The most important contents display producer. It assembles infos about the
 * concepts' and relations' basic properties.
 * 
 * @author Jochen Weile, B.Sc.
 */
public class EntityInfoPlugin extends AbstractContentDisplayPlugin {

	// ####FIELDS####
	AttributeName anTaxid = null;

	private static boolean loadNCBI = true;

	// ####CONSTRUCTOR####

	/**
	 * Constructor. Duh.
	 */
	public EntityInfoPlugin(final ONDEXGraph aog) {
		super(aog);
		// make sure there is a taxId in the graph
		anTaxid = aog.getMetaData().getAttributeName("TAXID");

		if (!Config.isApplet && !ValidatorRegistry.validators.containsKey("scientificspeciesname")) {
			if (loadNCBI && anTaxid != null) {

				Object[] options = { "Yes", "No", "No, don't ask again" };
				int n = JOptionPane.showInternalOptionDialog(OVTK2Desktop.getInstance().getDesktopPane(), "Do you want to load NCBI Taxonomy from web?", "Taxonomy lookup", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);

				if (n == JOptionPane.YES_OPTION) {

					// wrap into a process
					IndeterminateProcessAdapter p = new IndeterminateProcessAdapter() {
						public void task() {
							Engine engine = Engine.getEngine();
							try {
								engine.initializeValidators(new String[] { "scientificspeciesname" }, aog);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					};
					// start processing and monitoring
					p.start();
					OVTKProgressMonitor.start(OVTK2Desktop.getInstance().getMainFrame(), "Initialising taxonomy lookup", p);
				} else if (n == JOptionPane.CANCEL_OPTION) {
					loadNCBI = false;
				}
			}
		}
	}

	// ####METHODS####

	@Override
	public String compileContent(ONDEXEntity e) {
		StringBuffer b = new StringBuffer();
		if (e instanceof ONDEXConcept) {
			ONDEXConcept c = (ONDEXConcept) e;

			b.append("<h2>General Information</h2>");

			// optional preferred concept name
			if (c.getConceptName() != null) {
				String name = c.getConceptName().getName();
				if (name.trim().length() > 0)
					b.append("Name: " + name + "<br/>");
			}

			// optional parser ID
			String pid = c.getPID();
			if (pid != null && pid.trim().length() > 0)
				b.append("Parser ID: " + pid + "<br/>");

			// concept class full name
			b.append("Type: " + getMetaDataName(c.getOfType()) + "<br/>");

			// data source full name
			b.append("Source: " + getMetaDataName(c.getElementOf()) + "<br/>");

			// all evidence types
			writeList(extract(c.getEvidence()), "Evidence: ", b);

			// check if organism can be derived
			if (!Config.isApplet && ValidatorRegistry.validators.containsKey("scientificspeciesname") && anTaxid != null) {

				Attribute attr = c.getAttribute(anTaxid);
				if (attr != null) {
					String value = attr.getValue().toString();
					AbstractONDEXValidator validator = ValidatorRegistry.validators.get("scientificspeciesname");

					try {
						String scientificName = (String) validator.validate(value);
						if (scientificName != null)
							b.append("Organism: " + scientificName + "<br/>");

					} catch (NullPointerException npe) {
						// this happens when initialisation not yet complete
						b.append("Organism: Initialising...<br/>");
					}
				}
			}

			// option description
			String desc = c.getDescription();
			if (desc != null && desc.trim().length() > 0) {
				b.append("<h3>Description</h3>");
				b.append(desc + "<br/>");
			}

			// optional annotation
			String annot = c.getAnnotation();
			if (annot != null && annot.trim().length() > 0) {
				b.append("<h3>Annotation</h3>");
				b.append(annot + "<br/>");
			}

			// list all tags with hyperlinks
			b.append("<h2>Tags</h2>");
			for (ONDEXConcept context : c.getTags()) {
				String n = String.valueOf(context.getId());
				ConceptName cname = context.getConceptName();
				if (cname != null && cname.getName().trim().length() > 0)
					n = cname.getName();
				b.append("<a href=\"ovtk2://showtag" + context.getId() + "\">");
				b.append(n + "</a><br/>");
			}

			// list all concept names
			b.append("<h2>Synonyms</h2>");
			Collection<ConceptName> names = c.getConceptNames();
			ConceptName[] array = new ConceptName[names.size()];
			Iterator<ConceptName> namesI = names.iterator();
			for (int i = 0; i < names.size(); i++) {
				ConceptName cn = namesI.next();
				array[i] = cn;
			}
			// sort concepts names alphabetically
			Arrays.sort(array, new ConceptNameComparator());
			for (ConceptName cn : array) {
				if (cn.isPreferred()) {
					// green colour to indicate preferred names,
					// no (Preferred) tag required)
					b.append("<div style=\"color:green\">" + cn.getName() + " </div>");
				} else {
					b.append(cn.getName() + "<br/>");
				}
			}

		} else if (e instanceof ONDEXRelation) {
			ONDEXRelation r = (ONDEXRelation) e;

			b.append("<h2>General Information</h2>");

			// relation type full name
			b.append("Type: " + getMetaDataName(r.getOfType()) + "<br/>");

			// both participants in relations
			String name = extractName(r.getFromConcept());
			b.append("From: " + name + "<br/>");
			name = extractName(r.getToConcept());
			b.append("To: " + name + "<br/>");

			// all evidence types
			writeList(extract(r.getEvidence()), "Evidence: ", b);

			// list all tags with hyperlinks
			b.append("<h2>Tags</h2>");
			for (ONDEXConcept context : r.getTags()) {
				String n = String.valueOf(context.getId());
				ConceptName cname = context.getConceptName();
				if (cname != null && cname.getName().trim().length() > 0)
					n = cname.getName();
				b.append("<a href=\"ovtk2://showtag" + context.getId() + "\">");
				b.append(n + "</a><br/>");
			}

		}

		return b.toString();
	}

	/**
	 * Make sure that always the fullname is preferred.
	 * 
	 * @param m
	 *            MetaData
	 * @return fullname or ID
	 */
	private String getMetaDataName(MetaData m) {
		String name = m.getFullname();
		if (name == null || name.trim().length() == 0)
			name = m.getId();
		return name;
	}

	/**
	 * helper method to extract a usable concept name.
	 * 
	 * @param c
	 *            a concept.
	 * @return a usable name.
	 */
	private String extractName(ONDEXConcept c) {
		String str = "";
		if (c.getConceptName() != null)
			str = c.getConceptName().getName();
		if (str.trim().length() == 0)
			str = c.getPID();
		if (str.trim().length() == 0)
			str = c.getId() + "";
		return str;
	}

	/**
	 * helper method to write a html list of a collection
	 * 
	 * @param list
	 *            the list to be written.
	 * @param title
	 *            the title of the list.
	 * @param b
	 *            the buffer to write into.
	 */
	private void writeList(String[] array, String title, StringBuffer b) {
		if (array.length > 0) {
			b.append(title);
			for (String str : array)
				b.append(str + "<br/>");
		}
	}

	/**
	 * extracts all names for an evidence set.
	 * 
	 * @param set
	 *            the evidence set.
	 * @return sorted array of evidence names
	 */
	private String[] extract(Set<EvidenceType> set) {

		// extracts the full names of all evidence types
		List<String> temp = new ArrayList<String>();
		for (EvidenceType e : set) {
			temp.add(getMetaDataName(e));
		}

		// sort evidence names alphabetically
		String[] names = temp.toArray(new String[0]);
		Arrays.sort(names);

		return names;
	}

	/**
	 * guess what this does ;)
	 * 
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#getName()
	 */
	@Override
	public String getName() {
		return "General Information";
	}

	/**
	 * you don't ask this seriously, do you?!
	 * 
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#getVersion()
	 */
	@Override
	public String getVersion() {
		return "23.05.2011";
	}

	/**
	 * Ignore case for comparison of concept names.
	 * 
	 * @author taubertj
	 * 
	 */
	private class ConceptNameComparator implements Comparator<ConceptName> {

		@Override
		public int compare(ConceptName o1, ConceptName o2) {
			return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
		}
	}

}
