package net.sourceforge.ondex.mapping;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.AbstractONDEXPlugin;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPlugin;
import net.sourceforge.ondex.RequiresGraph;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.exception.type.RelationTypeMissingException;
import net.sourceforge.ondex.exception.type.UnitMissingException;
import net.sourceforge.ondex.exception.type.WrongParameterException;
import net.sourceforge.ondex.init.ArgumentDescription;

/**
 * Abstract implementation of an ONDEX mapping, manages listener handling.
 * 
 * @author taubertj
 */
public abstract class ONDEXMapping extends AbstractONDEXPlugin implements
		ONDEXPlugin, RequiresGraph {

	public final boolean DEBUG = false;

	// matching AttributeNames for equal Attributes
	private HashSet<AttributeName> attributeEqualsAttributeNames = null;

	// stores ConceptClass mappings
	private Map<String, Set<ConceptClass>> cc2ccEquals = null;

	// for self CC mappings
	private Map<String, Set<ConceptClass>> cc2ccSelf = new HashMap<String, Set<ConceptClass>>();

	/**
	 * Constructor for ONDEXTransformer with given session.
	 */
	public ONDEXMapping() {
	}

	// last concept evaluated
	private ONDEXConcept conceptLast;

	// cache for AttributeName to ConceptAttribute
	private Map<AttributeName, Attribute> attributeLast = new HashMap<AttributeName, Attribute>();

	/**
	 * Returns true if Mapping adherts to mapping restrictions and false
	 * otherwise.
	 * 
	 * @param graph
	 *            ONDEXGraph
	 * @param c1
	 *            ONDEXConcept
	 * @param c2
	 *            ONDEXConcept
	 * @param mapWithinDataSource
	 * @return boolean
	 */
	protected boolean evaluateMapping(ONDEXGraph graph, ONDEXConcept c1,
			ONDEXConcept c2) throws InvalidPluginArgumentException {

		boolean mapWithinDataSource = false;
		if (args.hasArgument(ArgumentNames.WITHIN_DATASOURCE_ARG)
				&& args.getUniqueValue(ArgumentNames.WITHIN_DATASOURCE_ARG) != null) {
			mapWithinDataSource = (Boolean) args
					.getUniqueValue(ArgumentNames.WITHIN_DATASOURCE_ARG);
		}

		if (DEBUG)
			System.out.println("Equal: " + c1.equals(c2));
		if (c1.equals(c2)) {
			return false;
		}

		if (conceptLast == null || !conceptLast.equals(c1)) {
			conceptLast = c1;
			attributeLast.clear();
		}

		// check DataSource different
		if (DEBUG)
			System.out.println("DataSource: "
					+ (!mapWithinDataSource && c1.getElementOf().equals(
							c2.getElementOf())));
		if (!mapWithinDataSource && c1.getElementOf().equals(c2.getElementOf()))
			return false;

		// check for allowed CCs
		if (DEBUG)
			System.out
					.println("ConceptClasses: "
							+ (getCCtoMapTo(graph, c1.getOfType()) != null && !getCCtoMapTo(
									graph, c1.getOfType()).contains(
									c2.getOfType())));
		if (getCCtoMapTo(graph, c1.getOfType()) != null
				&& !getCCtoMapTo(graph, c1.getOfType())
						.contains(c2.getOfType())) {
			return false;
		}

		// get Attribute Arguments, lazy initialization
		if (attributeEqualsAttributeNames == null
				&& args.hasArgument(ArgumentNames.ATTRIBUTE_EQUALS_ARG)
				&& args.getOptions().get(ArgumentNames.ATTRIBUTE_EQUALS_ARG) != null
				&& args.getObjectValueList(ArgumentNames.ATTRIBUTE_EQUALS_ARG)
						.size() > 0) {

			attributeEqualsAttributeNames = new HashSet<AttributeName>();
			for (Object o : args
					.getObjectValueList(ArgumentNames.ATTRIBUTE_EQUALS_ARG)) {
				String gds = (String) o;
				AttributeName attr;
				try {
					attr = graph.getMetaData().getAttributeName(gds);
				} catch (WrongParameterException e) {
					continue;
				}
				if (attr != null) {
					attributeEqualsAttributeNames.add(attr);
					fireEventOccurred(new GeneralOutputEvent(
							ArgumentNames.ATTRIBUTE_EQUALS_ARG + " on " + gds,
							getCurrentMethodName()));
				} else {
					fireEventOccurred(new AttributeNameMissingEvent(
							"Specified Attribute AttributeName is unrecognized in metadata "
									+ gds, getCurrentMethodName()));
				}
			}

		}

		// check for equal Attribute
		if (attributeEqualsAttributeNames != null) {
			for (AttributeName gdsEqualsAttributeName : attributeEqualsAttributeNames) {
				Attribute hitequAttribute = attributeLast
						.get(gdsEqualsAttributeName);
				if (hitequAttribute == null) {
					try {
						hitequAttribute = c1
								.getAttribute(gdsEqualsAttributeName);
					} catch (WrongParameterException e) {
						hitequAttribute = null;
					}
					attributeLast.put(gdsEqualsAttributeName, hitequAttribute);
				}
				Attribute conequAttribute;
				try {
					conequAttribute = c2.getAttribute(gdsEqualsAttributeName);
				} catch (WrongParameterException e) {
					conequAttribute = null;
				}
				// all three of the following conditions must be false to fail
				if (!(conequAttribute == null || hitequAttribute == null
				// if one is null then pass
				|| conequAttribute.getValue()
						.equals(hitequAttribute.getValue()))
				// if there both not null then are they equal? if there not then
				// fail
				) {
					if (DEBUG) {
						fireEventOccurred(new GeneralOutputEvent(
								"Attribute of two mappable concepts not equal between "
										+ c1.getPID() + " and " + c2.getPID()
										+ " (" + hitequAttribute.getValue()
										+ " vs " + conequAttribute.getValue()
										+ ")", getCurrentMethodName()));
					}
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Returns a map of allowed from and to ConceptClasses for relations.
	 * 
	 * @param graph
	 *            ONDEXGraph to handle meta data
	 * @return map of allowed from and to ConceptClasses
	 */
	protected Map<ConceptClass, ConceptClass> getAllowedCCs(ONDEXGraph graph)
			throws InvalidPluginArgumentException {
		Object[] ccs = args
				.getObjectValueArray(ArgumentNames.CONCEPTCLASS_RESTRICTION_ARG);

		// add CC restriction pairs
		HashMap<ConceptClass, ConceptClass> ccMapping = new HashMap<ConceptClass, ConceptClass>();
		if (ccs != null && ccs.length > 0) {
			for (Object cc : ccs) {
				String pair = ((String) cc).trim();
				String[] values = pair.split(",");

				if (values.length != 2) {
					ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
							.fireEventOccurred(
									new WrongParameterEvent(
											"Ignoring Invalid Format for ConceptClass pair "
													+ pair,
											getCurrentMethodName()));
					continue;
				}

				ConceptClass fromConceptClass;
				ConceptClass toConceptClass;

				try {
					fromConceptClass = graph.getMetaData().getConceptClass(
							values[0]);
					toConceptClass = graph.getMetaData().getConceptClass(
							values[1]);
				} catch (WrongParameterException e) {
					continue;
				}

				if (fromConceptClass != null && toConceptClass != null) {
					ccMapping.put(fromConceptClass, toConceptClass);
					fireEventOccurred(new GeneralOutputEvent(
							"Added ConceptClass restriction for "
									+ fromConceptClass.getId() + " ==> "
									+ toConceptClass.getId(),
							getCurrentMethodName()));
				} else {
					if (fromConceptClass == null)
						ONDEXEventHandler
								.getEventHandlerForSID(graph.getSID())
								.fireEventOccurred(
										new WrongParameterEvent(
												values[0]
														+ " is not a valid from ConceptClass.",
												getCurrentMethodName()));
					if (toConceptClass == null)
						ONDEXEventHandler
								.getEventHandlerForSID(graph.getSID())
								.fireEventOccurred(
										new WrongParameterEvent(
												values[1]
														+ " is not a valid to ConceptClass.",
												getCurrentMethodName()));
				}
			}
		}
		return ccMapping;
	}

	/**
	 * Returns a map of allowed from and to DataSources for relations.
	 * 
	 * @param graph
	 *            ONDEXGraph to handle meta data
	 * @return map of allowed from and to DataSources
	 */
	protected Map<DataSource, DataSource> getAllowedDataSources(ONDEXGraph graph)
			throws InvalidPluginArgumentException {
		Object[] dataSources = args
				.getObjectValueArray(ArgumentNames.DATASOURCE_RESTRICTION_ARG);

		// add DataSource restriction pairs
		HashMap<DataSource, DataSource> dataSourceMapping = new HashMap<DataSource, DataSource>();
		if (dataSources != null && dataSources.length > 0) {
			for (Object cv : dataSources) {
				String pair = ((String) cv).trim();
				String[] values = pair.split(",");

				if (values.length != 2) {
					ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
							.fireEventOccurred(
									new WrongParameterEvent(
											"Invalid Format for DataSource pair "
													+ pair,
											getCurrentMethodName()));
				}
				DataSource fromDataSource;
				DataSource toDataSource;
				try {
					fromDataSource = graph.getMetaData().getDataSource(
							values[0]);
					toDataSource = graph.getMetaData().getDataSource(values[1]);
				} catch (WrongParameterException e) {
					continue;
				}
				if (fromDataSource != null && toDataSource != null) {
					dataSourceMapping.put(fromDataSource, toDataSource);
					ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
							.fireEventOccurred(
									new GeneralOutputEvent(
											"Added DataSource restriction for "
													+ fromDataSource.getId()
													+ " ==> "
													+ toDataSource.getId(),
											getCurrentMethodName()));
				} else {
					if (fromDataSource == null)
						ONDEXEventHandler
								.getEventHandlerForSID(graph.getSID())
								.fireEventOccurred(
										new WrongParameterEvent(
												values[0]
														+ " is not a valid from DataSource.",
												getCurrentMethodName()));
					if (toDataSource == null)
						ONDEXEventHandler
								.getEventHandlerForSID(graph.getSID())
								.fireEventOccurred(
										new WrongParameterEvent(
												values[1]
														+ " is not a valid to DataSource.",
												getCurrentMethodName()));
				}
			}
		}
		return dataSourceMapping;
	}

	/**
	 * Returns the set of CC to which are equivalent to the current
	 * ConceptClass.
	 * <p/>
	 * By default the "from" conceptClass is included in the result.
	 * 
	 * @param graph
	 *            AbstractONDEXGraph
	 * @param from
	 *            ConceptClass
	 * @return Set<ConceptClass>
	 */
	protected Set<ConceptClass> getCCtoMapTo(ONDEXGraph graph, ConceptClass from)
			throws InvalidPluginArgumentException {

		// parse mapping arguments for CCs
		if (args.getOptions().get(ArgumentNames.EQUIVALENT_CC_ARG) != null
				&& cc2ccEquals == null) {
			cc2ccEquals = new HashMap<String, Set<ConceptClass>>(5);
			for (Object o : args
					.getObjectValueList(ArgumentNames.EQUIVALENT_CC_ARG)) {
				String[] mapping = ((String) o).split(",");
				if (mapping.length == 2) {
					ConceptClass cc0;
					ConceptClass cc1;
					try {
						cc0 = graph.getMetaData().getConceptClass(mapping[0]);
						cc1 = graph.getMetaData().getConceptClass(mapping[1]);
					} catch (WrongParameterException e) {
						continue;
					}
					if (!cc2ccEquals.containsKey(mapping[0])) {
						cc2ccEquals
								.put(mapping[0], new HashSet<ConceptClass>());
					}
					if (!cc2ccEquals.containsKey(mapping[1])) {
						cc2ccEquals
								.put(mapping[1], new HashSet<ConceptClass>());
					}

					cc2ccEquals.get(mapping[0]).add(cc0);
					cc2ccEquals.get(mapping[0]).add(cc1);
					cc2ccEquals.get(mapping[1]).add(cc0);
					cc2ccEquals.get(mapping[1]).add(cc1);
				}
			}
		}

		// return equivalent CCs or only selfmatch
		if (cc2ccEquals != null && cc2ccEquals.containsKey(from.getId())) {
			return cc2ccEquals.get(from.getId());
		}
		if (!cc2ccSelf.containsKey(from.getId())) {
			cc2ccSelf.put(from.getId(), new HashSet<ConceptClass>());
			cc2ccSelf.get(from.getId()).add(from);
		}
		return cc2ccSelf.get(from.getId());
	}

	/**
	 * Starts the mapping process.
	 * 
	 * @throws EmptyStringException
	 * @throws NullValueException
	 */
	public abstract void start() throws Exception;

	/**
	 * fetches the required metadata or throws a respective runtime exception if
	 * it doesn't exist.
	 * 
	 * @param id
	 *            the id of the metadata object.
	 */
	public ConceptClass requireConceptClass(String id)
			throws ConceptClassMissingException {
		ConceptClass cc = graph.getMetaData().getConceptClass(id);
		if (cc == null)
			throw new ConceptClassMissingException("concept class " + id
					+ " missing");
		else
			return cc;
	}

	/**
	 * fetches the required metadata or throws a respective runtime exception if
	 * it doesn't exist.
	 * 
	 * @param id
	 *            the id of the metadata object.
	 */
	public RelationType requireRelationType(String id)
			throws RelationTypeMissingException {
		RelationType rt = graph.getMetaData().getRelationType(id);
		if (rt == null)
			throw new RelationTypeMissingException("relation type " + id
					+ " missing");
		else
			return rt;
	}

	/**
	 * fetches the required metadata or throws a respective runtime exception if
	 * it doesn't exist.
	 * 
	 * @param id
	 *            the id of the metadata object.
	 */
	public DataSource requireDataSource(String id)
			throws DataSourceMissingException {
		DataSource dataSource = graph.getMetaData().getDataSource(id);
		if (dataSource == null)
			throw new DataSourceMissingException("DataSource " + id
					+ " missing");
		else
			return dataSource;
	}

	/**
	 * fetches the required metadata or throws a respective runtime exception if
	 * it doesn't exist.
	 * 
	 * @param id
	 *            the id of the metadata object.
	 */
	public EvidenceType requireEvidenceType(String id)
			throws EvidenceTypeMissingException {
		EvidenceType e = graph.getMetaData().getEvidenceType(id);
		if (e == null)
			throw new EvidenceTypeMissingException("Evidence Type " + id
					+ " missing");
		else
			return e;
	}

	/**
	 * fetches the required metadata or throws a respective runtime exception if
	 * it doesn't exist.
	 * 
	 * @param id
	 *            the id of the metadata object.
	 */
	public AttributeName requireAttributeName(String id)
			throws AttributeNameMissingException {
		AttributeName a = graph.getMetaData().getAttributeName(id);
		if (a == null)
			throw new AttributeNameMissingException("Attribute Name " + id
					+ " missing");
		else
			return a;
	}

	/**
	 * fetches the required metadata or throws a respective runtime exception if
	 * it doesn't exist.
	 * 
	 * @param id
	 *            the id of the metadata object.
	 */
	public Unit requireUnit(String id) throws UnitMissingException {
		Unit u = graph.getMetaData().getUnit(id);
		if (u == null)
			throw new UnitMissingException("Unit " + id + " missing");
		else
			return u;
	}

	@Override
	public Collection<ArgumentDescription> getArgumentDescriptions(int position) {
		ArgumentDescription ab = new ArgumentDescription();
		ab.setCls("net.sourceforge.ondex.core.ONDEXGraph");
		ab.setName("Graph id");
		ab.setInteranlName("graphId");
		ab.setInputId(position);
		ab.setDescription("Graph that will be operated on by this plugin.");
		ab.setIsRequired(true);
		ab.setIsInputObject(true);
		return Collections.singleton(ab);
	}
}
