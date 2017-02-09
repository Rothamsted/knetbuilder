package net.sourceforge.ondex.ovtk2.graph;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.config.Config;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;

/**
 * Provides a transformation from a given ONDEXConcept to a String as label.
 * 
 * @author taubertj
 * @author Matthew Pocock
 */
public class ONDEXNodeLabels implements Transformer<ONDEXConcept, String> {

	/**
	 * More sophisticated defining of label composition.
	 * 
	 * @author taubertj
	 * 
	 */
	public static class LabelCompositionRule {

		// DataSource of additional concept accession
		private DataSource accessionDataSource = null;

		// state of inclusion of parser ID
		private boolean includeParserID = false;

		// state of inclusion of preferred name
		private boolean includePreferredName = true;

		// max label length
		private int length = 0;

		// optional accession prefix
		private String prefix = null;

		// separator between accession and name
		private String separator = " | ";

		/**
		 * Default rule.
		 */
		public LabelCompositionRule() {
			// default
		}

		/**
		 * Defines one label composition rule
		 * 
		 * @param accessionDataSource
		 * @param includeParserID
		 * @param includePreferredName
		 * @param length
		 * @param prefix
		 * @param separator
		 */
		public LabelCompositionRule(DataSource accessionDataSource, boolean includeParserID, boolean includePreferredName, int length, String prefix, String separator) {
			super();
			this.accessionDataSource = accessionDataSource;
			this.includeParserID = includeParserID;
			this.includePreferredName = includePreferredName;
			this.length = length;
			this.prefix = prefix;
			this.separator = separator;
		}

		/**
		 * @return the accessionDataSource
		 */
		public DataSource getAccessionDataSource() {
			return accessionDataSource;
		}

		/**
		 * @return the length
		 */
		public int getLength() {
			return length;
		}

		/**
		 * @return the prefix
		 */
		public String getPrefix() {
			return prefix;
		}

		/**
		 * @return the separator
		 */
		public String getSeparator() {
			return separator;
		}

		/**
		 * @return the includeParserID
		 */
		public boolean isIncludeParserID() {
			return includeParserID;
		}

		/**
		 * @return the includePreferredName
		 */
		public boolean isIncludePreferredName() {
			return includePreferredName;
		}
	}

	/**
	 * Rule if nothing else is defined
	 */
	private static final LabelCompositionRule defaultRule = new LabelCompositionRule();

	/**
	 * ALL string used in searching rules
	 */
	private final String ALL;

	/**
	 * How to fill the label mask
	 */
	private Boolean defaultMask = Boolean.FALSE;

	/**
	 * To toggle HTML tags for concept labels
	 */
	private boolean includeHTML = true;

	/**
	 * Map contains mapping id to label
	 */
	private final Map<ONDEXConcept, String> labels;

	/**
	 * Map contains what concept labels to show
	 */
	private final Map<ONDEXConcept, Boolean> mask;

	/**
	 * Double map for LabelCompositionRules indexed by data source and concept
	 * class id of concept
	 */
	private final Map<String, Map<String, LabelCompositionRule>> rules;

	/**
	 * Initialises the labels for the nodes in the graph.
	 * 
	 * @param includeHTML
	 *            whether or not to include HTML tag in label
	 */
	public ONDEXNodeLabels(boolean includeHTML) {
		this.ALL = Config.language.getProperty("Dialog.ConceptLabel.All");
		this.includeHTML = includeHTML;
		this.labels = new HashMap<ONDEXConcept, String>();
		this.mask = LazyMap.decorate(new HashMap<ONDEXConcept, Boolean>(), new Factory<Boolean>() {

			@Override
			public Boolean create() {
				// return default mask
				return defaultMask;
			}
		});
		this.rules = new HashMap<String, Map<String, LabelCompositionRule>>();
	}

	/**
	 * Adds a LabelCompositionRule to the internal index structure.
	 * 
	 * @param dataSource
	 * @param conceptClass
	 * @param rule
	 */
	public void addRule(String dataSource, String conceptClass, LabelCompositionRule rule) {

		// add rule to index
		if (!rules.containsKey(dataSource))
			rules.put(dataSource, new HashMap<String, LabelCompositionRule>());
		rules.get(dataSource).put(conceptClass, rule);
	}

	/**
	 * Clears current label composition rules
	 * 
	 */
	public void clearRules() {
		rules.clear();
	}

	/**
	 * Mask fillMask
	 * 
	 * @param value
	 *            boolean
	 */
	public void fillMask(boolean value) {
		for (ONDEXConcept c : mask.keySet()) {
			mask.put(c, value);
		}
		// necessary for nodes not yet displayed
		defaultMask = value;
	}

	/**
	 * Extracts the label from a given ONDEXConcept.
	 * 
	 * @param node
	 *            ONDEXConcept
	 * @return String
	 */
	public String getLabel(ONDEXConcept node) {

		String label = null;
		String dataSourceID = node.getElementOf().getId();
		String conceptClassID = node.getOfType().getId();
		LabelCompositionRule rule = defaultRule;

		// check for possible rules
		if (rules.containsKey(dataSourceID)) {
			Map<String, LabelCompositionRule> byConceptClass = rules.get(dataSourceID);
			if (byConceptClass.containsKey(conceptClassID)) {
				// exact matching rule
				rule = byConceptClass.get(conceptClassID);
			} else if (byConceptClass.containsKey(ALL)) {
				// rule matches data source, but for all concept classes
				rule = byConceptClass.get(ALL);
			}
		} else if (rules.containsKey(ALL)) {
			Map<String, LabelCompositionRule> byConceptClass = rules.get(ALL);
			if (byConceptClass.containsKey(conceptClassID)) {
				// rule matches concept class, but for all data sources
				rule = byConceptClass.get(conceptClassID);
			} else if (byConceptClass.containsKey(ALL)) {
				// most generic ALL by ALL rule
				rule = byConceptClass.get(ALL);
			}
		}

		// take preferred name or try to synthesise
		if (rule.isIncludePreferredName()) {
			// get first preferred name as label
			for (ConceptName cn : node.getConceptNames()) {
				if (cn.isPreferred()) {
					label = cn.getName();
					break;
				}
			}
			// next try annotation
			if (label == null || label.trim().length() == 0) {
				label = node.getAnnotation();
			}
			// next try description
			if (label == null || label.trim().length() == 0) {
				label = node.getDescription();
			}
			// next try pid
			if (label == null || label.trim().length() == 0) {
				label = node.getPID();
			}
			// last resort to concept id
			if (label == null || label.trim().length() == 0) {
				label = String.valueOf(node.getId());
			}
		}

		if (rule.getAccessionDataSource() != null) {
			// search for first accession of DataSource
			ConceptAccession accession = null;
			for (ConceptAccession current : node.getConceptAccessions()) {
				if (current.getElementOf().equals(rule.getAccessionDataSource())) {
					accession = current;
					break;
				}
			}

			// accession is present
			if (accession != null) {
				// separate from previous label or start new label
				if (label != null) {
					label = label + rule.getSeparator();
				} else {
					label = "";
				}

				// optional prefix here
				if (rule.getPrefix() != null && rule.getPrefix().length() > 0) {
					label = label + rule.getPrefix() + ":";
				}

				// append accession
				label = label + accession.getAccession();
			}
		}

		// add parser ID as last part of label
		if (rule.isIncludeParserID() && node.getPID() != null) {
			if (label != null && label.length() > 0)
				label = label + rule.getSeparator() + node.getPID();
			else
				label = node.getPID();
		}

		// just to not have "null" written on graph
		if (label == null)
			label = "";

		// trim label to length
		if (rule.getLength() > 0 && label.length() > rule.getLength())
			label = label.substring(0, rule.getLength()) + "...";

		return label;
	}

	/**
	 * Returns the mask
	 * 
	 */
	public Map<ONDEXConcept, Boolean> getMask() {
		return mask;
	}

	/**
	 * Returns current HTML tags inclusion scheme.
	 * 
	 * @return
	 */
	public boolean isIncludeHTML() {
		return includeHTML;
	}

	/**
	 * Sets inclusion of HTML tags.
	 * 
	 * @param includeHTML
	 */
	public void setIncludeHTML(boolean includeHTML) {
		this.includeHTML = includeHTML;
	}

	/**
	 * Returns result of transformation.
	 * 
	 * @param node
	 *            ONDEXConcept
	 * @return String
	 */
	public String transform(ONDEXConcept node) {
		if (!mask.get(node)) {
			return "";
		} else {
			if (!labels.containsKey(node))
				updateLabel(node);
			String label = labels.get(node);
			if (includeHTML)
				return "<html>" + label + "</html>";
			else
				return label;
		}
	}

	/**
	 * Update all labels from the graph.
	 * 
	 */
	public void updateAll() {
		labels.clear();
	}

	/**
	 * Update the label of a given node.
	 * 
	 * @param node
	 *            ONDEXConcept
	 */
	public void updateLabel(ONDEXConcept node) {
		labels.put(node, getLabel(node));
	}

}
