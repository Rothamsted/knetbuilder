package net.sourceforge.ondex.ovtk2.ui.stats;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;

/**
 * this class extracts the data for the selected variable from the graph
 * according to the given filter criteria.
 * 
 * @author Jochen Weile, B.Sc.
 */
public class DataExtractor implements Monitorable {

	// ####FIELDS####

	/**
	 * the graph.
	 */
	private ONDEXGraph aog;

	/**
	 * the set of concept class filters.
	 */
	private TreeSet<String> ccFilters;

	/**
	 * set of relation type filters.
	 */
	private TreeSet<String> rtFilters;

	/**
	 * the set of Attribute filter pairs: a mapping from attribute names to
	 * values.
	 */
	private HashMap<String, Object> gdsFilters;

	/**
	 * the variable to collect the values for.
	 */
	private Object variable;

	/**
	 * field for the monitorable interface.
	 */
	private boolean cancelled = false;

	/**
	 * the number of concepts that were relevent for the given variable and the
	 * given filter criteria.
	 */
	private int relevant_concepts;

	/**
	 * the number of relations that were relevent for the given variable and the
	 * given filter criteria.
	 */
	private int relevant_relations;

	/**
	 * the actual dataset. a collection of values for the given variable. can
	 * consist of any number type.
	 */
	private Collection<Number> dataset;

	/**
	 * fields for the monitorable interface.
	 */
	private int progress = 0, max_progress;

	/**
	 * fields for the monitorable interface.
	 */
	private String state = Monitorable.STATE_IDLE;

	/**
	 * the minimal and maximal value found for the dataset.
	 */
	private Number min_val, max_val;

	private Throwable caught = null;

	// ####CONSTRUCTOR####

	/**
	 * the constructor.
	 */
	public DataExtractor(ONDEXGraph aog) {
		this.aog = aog;
		clearAllFilters();
		max_progress = aog.getConcepts().size() + aog.getRelations().size();
	}

	// ####METHODS####

	/**
	 * sets all filters to an empty state.
	 */
	public void clearAllFilters() {
		ccFilters = new TreeSet<String>();
		rtFilters = new TreeSet<String>();
		gdsFilters = new HashMap<String, Object>();
	}

	/**
	 * adds a concept class filter to the list of filters.
	 * 
	 * @param cc
	 *            the concept class to filter for
	 */
	public void addCCFilter(ConceptClass cc) {
		ccFilters.add(cc.getId());
	}

	/**
	 * removes a concept class filter from the list of filters.
	 * 
	 * @param cc
	 *            the concept class to remove.
	 * @return if the operation was allowed.
	 */
	public boolean removeCCFilter(ConceptClass cc) {
		if (variable != null && variable instanceof ConceptClass)
			return false;
		ccFilters.remove(cc.getId());
		return true;
	}

	/**
	 * adds a relation type filter.
	 * 
	 * @param rt
	 *            the relation type to filter for.
	 * @return if the operation was allowed.
	 */
	public boolean addRTFilter(RelationType rt) {
		if (variable != null && variable instanceof RelationType)
			return false;
		rtFilters.add(rt.getId());
		return true;
	}

	/**
	 * removes the given relation type from the list of filters.
	 * 
	 * @param rt
	 *            the relation type to remove.
	 */
	public void removeRTFilter(RelationType rt) {
		rtFilters.remove(rt.getId());
	}

	/**
	 * adds a attribute filter criterium to the list of filters. the criterium
	 * consists of an attribute and a value.
	 * 
	 * @param an
	 *            the attribute name
	 * @param value
	 *            the value to filter for.
	 */
	public void addGDSFilter(AttributeName an, Object value) {
		gdsFilters.put(an.getId(), value);
	}

	/**
	 * removes a given attribute filter from the list of filters.
	 * 
	 * @param an
	 *            the attribute name that identfies the attribute filter
	 *            criterium.
	 */
	public void removeGDSFilter(AttributeName an) {
		gdsFilters.remove(an.getId());
	}

	/**
	 * sets the variable field.
	 * 
	 * @param v
	 *            the variable object to set.
	 */
	public void setVariable(Object v) {
		variable = v;
		if (v instanceof ConceptClass) {
			ccFilters = new TreeSet<String>();
		} else if (v instanceof RelationType) {
			rtFilters = new TreeSet<String>();
		}
	}

	/**
	 * performs the extraction process.
	 */
	public void extract() {
		try {
			progress = 0;
			state = "calculating...";

			relevant_concepts = 0;
			relevant_relations = 0;
			dataset = new Vector<Number>();

			max_val = null;
			min_val = null;

			if (variable instanceof AttributeName) {
				AttributeName an = (AttributeName) variable;
				if (Number.class.isAssignableFrom(an.getDataType())) {
					// do statistics
					for (ONDEXConcept c : aog.getConcepts()) {
						if (cancelled)
							break;
						progress++;
						if (!applyFilters(c))
							continue;
						if (c.getAttribute(an) != null) {
							Number val = (Number) c.getAttribute(an).getValue();
							dataset.add(val);
							if (min_val == null || (val.doubleValue() < min_val.doubleValue()))
								min_val = val;
							if (max_val == null || (val.doubleValue() > max_val.doubleValue()))
								max_val = val;
						} else
							continue;
						relevant_concepts++;
					}

					for (ONDEXRelation r : aog.getRelations()) {
						if (cancelled)
							break;
						progress++;
						if (!applyFilters(r))
							continue;
						if (r.getAttribute(an) != null) {
							Number val = (Number) r.getAttribute(an).getValue();
							dataset.add(val);
							if (min_val == null || (val.doubleValue() < min_val.doubleValue()))
								min_val = val;
							if (max_val == null || (val.doubleValue() > max_val.doubleValue()))
								max_val = val;
						} else
							continue;
						relevant_relations++;
					}
					if (dataset.size() == 0) {
						max_val = 1.0;
						min_val = 0.0;
					}
				} else {
					// just count cs and rs
					max_val = 1.0;
					min_val = 0.0;
					for (ONDEXConcept c : aog.getConcepts()) {
						if (cancelled)
							break;
						progress++;
						if (c.getAttribute(an) == null)
							continue;
						if (!applyFilters(c))
							continue;
						relevant_concepts++;
					}

					for (ONDEXRelation r : aog.getRelations()) {
						if (cancelled)
							break;
						progress++;
						if (r.getAttribute(an) == null)
							continue;
						if (!applyFilters(r))
							continue;
						relevant_relations++;
					}
				}
			} else if (variable instanceof ConceptClass) {
				ConceptClass cc = (ConceptClass) variable;
				max_progress = aog.getConcepts().size();
				// just counting
				for (ONDEXConcept c : aog.getConcepts()) {
					if (cancelled)
						break;
					progress++;
					if (!c.getOfType().equals(cc))
						continue;
					if (!applyFilters(c))
						continue;
					relevant_concepts++;
				}
				max_val = 1.0;
				min_val = 0.0;
			} else if (variable instanceof RelationType) {
				// just counting
				max_progress = aog.getRelations().size();
				RelationType rt = (RelationType) variable;
				for (ONDEXRelation r : aog.getRelations()) {
					if (cancelled)
						break;
					progress++;
					if (!r.getOfType().equals(rt))
						continue;
					if (!applyFilters(r))
						continue;
					relevant_relations++;
				}
				max_val = 1.0;
				min_val = 0.0;
			} else if (variable instanceof String) {
				if (variable.equals("degree")) {
					max_progress = aog.getConcepts().size();
					int deg;
					for (ONDEXConcept c : aog.getConcepts()) {

						if (!applyFilters(c))
							continue;
						relevant_concepts++;
						deg = 0;
						for (ONDEXRelation r : aog.getRelationsOfConcept(c)) {
							if (!applyFilters(r))
								continue;
							deg++;
						}
						dataset.add(deg);
						if (min_val == null || (deg < min_val.intValue()))
							min_val = deg;
						if (max_val == null || (deg > max_val.intValue()))
							max_val = deg;
						progress++;
					}
				}
			}
			state = Monitorable.STATE_TERMINAL;
		} catch (Throwable t) {
			caught = t;
		}
	}

	/**
	 * applies all filters to the given concept to find out whether it is
	 * accepted or not
	 * 
	 * @param c
	 *            the concept.
	 * @return whether the concept is accepted.
	 */
	private boolean applyFilters(ONDEXConcept c) {
		Iterator<String> it = ccFilters.iterator();
		String string;
		boolean found_one = false;
		while (it.hasNext()) {
			string = it.next();
			if (c.getOfType().getId().equals(string)) {
				found_one = true;
				break;
			}
		}
		if (!found_one && ccFilters.size() > 0)
			return false;

		it = gdsFilters.keySet().iterator();
		Object value;
		found_one = false;
		while (it.hasNext()) {
			string = it.next();
			value = gdsFilters.get(string);
			if (c.getAttribute(aog.getMetaData().getAttributeName(string)) != null) {
				if (c.getAttribute(aog.getMetaData().getAttributeName(string)).getValue().equals(value)) {
					found_one = true;
					break;
				}
			}
		}
		if (!found_one && gdsFilters.size() > 0)
			return false;

		return true;
	}

	/**
	 * applies all filters on the given relation.
	 * 
	 * @param r
	 *            the relation.
	 * @return whether the relation is accepted.
	 */
	private boolean applyFilters(ONDEXRelation r) {
		Iterator<String> it = rtFilters.iterator();
		String string;
		boolean found_one = false;
		while (it.hasNext()) {
			string = it.next();
			if (r.getOfType().getId().equals(string)) {
				found_one = true;
				break;
			}
		}
		if (!found_one && rtFilters.size() > 0)
			return false;

		found_one = false;
		it = ccFilters.iterator();
		while (it.hasNext()) {
			string = it.next();
			if (r.getFromConcept().getOfType().getId().equals(string)) {
				found_one = true;
				break;
			}
			if (r.getToConcept().getOfType().getId().equals(string)) {
				found_one = true;
				break;
			}
		}
		if (!found_one && ccFilters.size() > 0)
			return false;

		found_one = false;
		it = gdsFilters.keySet().iterator();
		Object value;
		while (it.hasNext()) {
			string = it.next();
			value = gdsFilters.get(string);
			if (r.getFromConcept().getAttribute(aog.getMetaData().getAttributeName(string)) != null) {
				if (r.getFromConcept().getAttribute(aog.getMetaData().getAttributeName(string)).getValue().equals(value)) {
					found_one = true;
					break;
				}
			}
			if (r.getFromConcept().getAttribute(aog.getMetaData().getAttributeName(string)) != null) {
				if (r.getFromConcept().getAttribute(aog.getMetaData().getAttributeName(string)).getValue().equals(value)) {
					found_one = true;
					break;
				}
			}
		}
		if (!found_one && gdsFilters.size() > 0)
			return false;

		return true;
	}

	/**
	 * returns the name of the current variable.
	 */
	public String getVariableName() {
		if (variable instanceof AttributeName)
			return ((AttributeName) variable).getFullname();
		else if (variable instanceof ConceptClass)
			return ((ConceptClass) variable).getFullname();
		else if (variable instanceof RelationType)
			return ((RelationType) variable).getFullname();
		else if (variable instanceof String)
			return (String) variable;
		else
			return null;
	}

	/**
	 * @return the number of concepts relevant according to the filters and the
	 *         variable.
	 */
	public int getNumConcepts() {
		return relevant_concepts;
	}

	/**
	 * @return the number of relations relevant according to the filters and the
	 *         variable.
	 */
	public int getNumRelations() {
		return relevant_relations;
	}

	/**
	 * @return the collected dataset.
	 */
	public Collection<Number> getDataSet() {
		return dataset;
	}

	/**
	 * @return the smallest value in the dataset.
	 */
	public Number getMinimalValue() {
		return min_val;
	}

	/**
	 * @return the greatest value in the dataset.
	 */
	public Number getMaximalValue() {
		return max_val;
	}

	/**
	 * @see net.sourceforge.ondex.tools.monitoring.Monitorable#getMaxProgress()
	 */
	@Override
	public int getMaxProgress() {
		return max_progress;
	}

	/**
	 * @see net.sourceforge.ondex.tools.monitoring.Monitorable#getMinProgress()
	 */
	@Override
	public int getMinProgress() {
		return 0;
	}

	/**
	 * @see net.sourceforge.ondex.tools.monitoring.Monitorable#getProgress()
	 */
	@Override
	public int getProgress() {
		return progress;
	}

	/**
	 * @see net.sourceforge.ondex.tools.monitoring.Monitorable#getState()
	 */
	@Override
	public String getState() {
		return state;
	}

	/**
	 * @see net.sourceforge.ondex.tools.monitoring.Monitorable#isIndeterminate()
	 */
	@Override
	public boolean isIndeterminate() {
		return false;
	}

	/**
	 * @see net.sourceforge.ondex.tools.monitoring.Monitorable#setCancelled(boolean)
	 */
	@Override
	public void setCancelled(boolean c) {
		cancelled = c;
	}

	@Override
	public Throwable getUncaughtException() {
		return caught;
	}

	public boolean isAbortable() {
		return false;
	}

}
