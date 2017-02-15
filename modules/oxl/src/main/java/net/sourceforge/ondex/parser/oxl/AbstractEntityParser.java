package net.sourceforge.ondex.parser.oxl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.config.OndexJAXBContextRegistry;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.core.util.Holder;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.export.oxl.CollectionHolder;
import net.sourceforge.ondex.export.oxl.ColorHolder;
import net.sourceforge.ondex.export.oxl.ListHolder;
import net.sourceforge.ondex.export.oxl.MapHolder;
import net.sourceforge.ondex.export.oxl.RefManager;
import net.sourceforge.ondex.export.oxl.SetHolder;
import net.sourceforge.ondex.export.oxl.XMLTagNames;
import net.sourceforge.ondex.marshal.Marshaller;
import net.sourceforge.ondex.tools.data.ChemicalStructure;
import net.sourceforge.ondex.tools.data.ChemicalStructureHolder;
import net.sourceforge.ondex.tools.data.Protein3dStructure;
import net.sourceforge.ondex.tools.data.Protein3dStructureHolder;

/**
 * This abstract class provides parsing methods for general metadata, which is
 * contained in concepts, relations and meta data. The methods of this class are
 * used by ConceptParser, RelationParser, ConceptMetaDataParser,
 * RelationMetaDataParser and GeneralMetaDataParser.
 * 
 * @author sierenk
 */
public abstract class AbstractEntityParser implements XmlComponentParser {
	
	// current graph
	protected ONDEXGraph og;

	// list of Attribute to ignore during parsing
	private Set<AttributeName> ignoreAttributeNames;

	private RefManager<String, AttributeName> anRefManager;

	public Set<String> errorMessages = new HashSet<String>();

	protected Unmarshaller jaxbUnmarshaller;
	
	protected OndexJAXBContextRegistry jaxbRegistry;

	/**
	 * @param og
	 *            AbstractONDEXGraph
	 */
	public AbstractEntityParser(final ONDEXGraph og) throws JAXBException {
		// for storing parsed content
		this.og = og;
		jaxbRegistry = OndexJAXBContextRegistry.instance();
		jaxbRegistry.addClassBindings(ListHolder.class, ColorHolder.class,
				CollectionHolder.class, SetHolder.class, MapHolder.class,
				ChemicalStructureHolder.class, Protein3dStructureHolder.class);
		jaxbRegistry.addHolder(Map.class, MapHolder.class);
		jaxbRegistry.addHolder(Set.class, SetHolder.class);
		jaxbRegistry.addHolder(List.class, ListHolder.class);
		jaxbRegistry.addHolder(Color.class, ColorHolder.class);
		jaxbRegistry.addHolder(Collection.class, CollectionHolder.class);
		jaxbRegistry.addHolder(ChemicalStructure.class,
				ChemicalStructureHolder.class);
		jaxbRegistry.addHolder(Protein3dStructure.class,
				Protein3dStructureHolder.class);

		if (og != null)
			jaxbUnmarshaller = jaxbRegistry
					.createUnmarshaller(og.getMetaData());
		else
			jaxbUnmarshaller = jaxbRegistry.createUnmarshaller();

		anRefManager = new RefManager<String, AttributeName>() {
			@Override
			protected AttributeName resolveExternally(String id) {
				return og.getMetaData().getAttributeName(id);
			}
		};
	}

	/**
	 * Returns current list of ignore Attribute AttributeNames for import.
	 * 
	 * @return Set<String>
	 */
	public Set<AttributeName> getIgnoreAttributes() {
		return ignoreAttributeNames;
	}

	/**
	 * Sets list of Attribute AttributeNames to ignore for import.
	 * 
	 * @param ignoreAttributeNames
	 *            HashSet<String>
	 */
	public void setIgnoreAttributes(HashSet<AttributeName> ignoreAttributeNames) {
		this.ignoreAttributeNames = ignoreAttributeNames;
	}

	/**
	 * Parses a sequence of evidences which belong to a concept, a relation or
	 * the ONDEX metadata.
	 * 
	 * @param xmlr
	 *            XMLStreamReader
	 * @return a Collection of EvidenceTypes
	 * @throws XMLStreamException
	 */
	protected Collection<EvidenceType> parseEvidences(XMLStreamReader xmlr)
			throws XMLStreamException {

		ArrayList<EvidenceType> evidences = new ArrayList<EvidenceType>(1);
		int eventType = xmlr.getEventType();
		while (xmlr.hasNext()) {

			if (eventType == XMLStreamConstants.START_ELEMENT
					&& xmlr.getLocalName().equals(XMLTagNames.EVIDENCE)) {

				xmlr.nextTag(); // name
				String nameS = checkForSpace(xmlr.getElementText());

				EvidenceType ev;

				// create evidence on ONDEXGraph (if it does not exist)
				if (!og.getMetaData().checkEvidenceType(nameS)) {
					xmlr.nextTag(); // fullname
					String fullnameS = xmlr.getElementText();

					xmlr.nextTag(); // description
					String descriptionS = xmlr.getElementText();

					ev = og.getMetaData().createEvidenceType(nameS, fullnameS,
							descriptionS);
					xmlr.nextTag(); // skip end tag
				} else { // skip all else in this evidence
					ev = og.getMetaData().getEvidenceType(nameS);
					while (xmlr.hasNext()) {
						eventType = xmlr.next();

						if (eventType == XMLStreamConstants.END_ELEMENT
								&& xmlr.getLocalName().equals(
										XMLTagNames.EVIDENCE)) {
							break;
						}
					}
				}
				evidences.add(ev);
			}

			if (eventType == XMLStreamConstants.END_ELEMENT
					&& xmlr.getLocalName().equals(XMLTagNames.EVIDENCES)) {
				return evidences;
			}
			eventType = xmlr.next();
		}
		return evidences;
	}

	/**
	 * Parses a AttributeName, which can be contained in a RelationAttribute,
	 * ConceptAttribute or the ONDEX metadata
	 * 
	 * @param xmlr
	 *            XMLStreamReader
	 * @return an AttributName object
	 * @throws XMLStreamException
	 * @throws ClassNotFoundException
	 * @throws javax.xml.bind.JAXBException
	 * @throws UnsupportedOperationException
	 * @throws EmptyStringException
	 * @throws NullValueException
	 */
	protected RefManager.Ref<AttributeName> parseAttributeName(
			XMLStreamReader xmlr) throws XMLStreamException, JAXBException,
			NullValueException, EmptyStringException,
			UnsupportedOperationException, ClassNotFoundException {

		xmlr.nextTag(); // name
		String nameS = checkForSpace(xmlr.getElementText());
		final RefManager.Ref<AttributeName> anRef = anRefManager.makeRef(nameS);

		if (xmlr.getLocalName().equals(XMLTagNames.ID_REF)) {
			xmlr.nextTag();
		} else {
			xmlr.nextTag(); // fullname
			String fullname = xmlr.getElementText();

			xmlr.nextTag(); // description
			String descriptionS = xmlr.getElementText();

			xmlr.nextTag(); // datatype OR unitname

			Unit unit = null;

			// optional unitname tag
			if (xmlr.getEventType() == XMLStreamConstants.START_ELEMENT
					&& xmlr.getLocalName().equals(XMLTagNames.UNIT)) {
				unit = parseUnit(xmlr);
				xmlr.nextTag(); // datatype
			}
			// end optional

			String datatypeS = xmlr.getElementText();

			if (!og.getMetaData().checkAttributeName(nameS)) {
				AttributeName attrName;// =
				// og.getMetaData().getAttributeName(nameS);

				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				if (cl == null)
					cl = ClassLoader.getSystemClassLoader();
				attrName = og
						.getMetaData()
						.getFactory()
						.createAttributeName(nameS, fullname, descriptionS,
								unit, cl.loadClass(datatypeS));
				anRefManager.resolve(nameS, attrName);

				// add new created AttributeName to JAXBRegistry
				if (!jaxbRegistry.hasAttributeName(attrName))
					jaxbRegistry.addAttribute(attrName);
				jaxbUnmarshaller = jaxbRegistry.createUnmarshaller();

			} else {
				// nothing;
			}

			xmlr.nextTag(); // specialisationOf or end attrname/specialisationOf

			// optional specialisationOf
			if (xmlr.getEventType() == XMLStreamConstants.START_ELEMENT
					&& xmlr.getLocalName().equals(XMLTagNames.SPECIALISATIONOF)) {
				parseAttributeName(xmlr)
						.setCompletionAction(
								new RefManager.OnCompletion<net.sourceforge.ondex.core.AttributeName>() {
									@Override
									public void onCompletion(
											AttributeName attributeName) {
										anRef.get().setSpecialisationOf(
												attributeName);
									}
								});
				xmlr.nextTag();// end specialisationOf
			}
			// end optional
		}

		return anRef;
	}

	/**
	 * Parses a unit, which is a part of attribute names.
	 * 
	 * @param xmlr
	 *            XMLStreamReader
	 * @return Unit
	 * @throws XMLStreamException
	 */
	protected Unit parseUnit(XMLStreamReader xmlr) throws XMLStreamException {

		xmlr.nextTag(); // name
		String unitnameS = checkForSpace(xmlr.getElementText());

		Unit unit;

		// get/create unit
		if (!og.getMetaData().checkUnit(unitnameS)) {

			xmlr.nextTag(); // fullname
			String fullname = xmlr.getElementText();

			xmlr.nextTag(); // description
			String unitdescriptionS = xmlr.getElementText();

			unit = og.getMetaData().createUnit(unitnameS, fullname,
					unitdescriptionS);
			xmlr.nextTag(); // end unitname
		} else {
			unit = og.getMetaData().getUnit(unitnameS);
			while (xmlr.hasNext()) {
				int eventType = xmlr.next();

				if (eventType == XMLStreamConstants.END_ELEMENT
						&& xmlr.getLocalName().equals(XMLTagNames.UNIT)) {
					break;
				}
			}
		}

		return unit;
	}

	/**
	 * Ensures back compatibility with old OXL
	 * 
	 * @param id
	 * @return
	 */
	protected String checkForSpace(String id) {
		if (id.indexOf(' ') > -1) {
			id = id.replaceAll(" ", "");
		}
		return id;
	}

	/**
	 * @param list
	 * @throws JAXBException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	protected void processList2Holder(List<?> list) throws JAXBException,
			InstantiationException, IllegalAccessException {
		List added = new ArrayList(list.size());
		Iterator<?> it = list.iterator();
		while (it.hasNext()) {
			Object value = it.next();
			if (value instanceof Holder) {
				it.remove();
				value = ((Holder) value).getValue();
				added.add(value);
			}
			if (value instanceof Collection) {
				processCollection2Holder((Collection) value);
			} else if (value instanceof Map) {
				processMap2Holder((Map) value);
			} else if (value instanceof List) {
				processList2Holder((List) value);
			}
		}
		list.addAll(added); // add back the unwraped values
	}

	/**
	 * @param collection
	 * @throws JAXBException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	protected void processCollection2Holder(Collection<?> collection)
			throws JAXBException, InstantiationException,
			IllegalAccessException {
		List added = new ArrayList(collection.size());
		Iterator<?> it = collection.iterator();
		while (it.hasNext()) {
			Object value = it.next();
			if (value instanceof Holder) {
				it.remove();
				value = ((Holder) value).getValue();
				added.add(value);
			}
			if (value instanceof Collection) {
				processCollection2Holder((Collection) value);
			} else if (value instanceof Map) {
				processMap2Holder((Map) value);
			} else if (value instanceof List) {
				processList2Holder((List) value);
			}
		}
		collection.addAll(added); // add back the unwraped values
	}

	/**
	 * @param map
	 * @throws JAXBException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	protected void processMap2Holder(Map<?, ?> map) throws JAXBException,
			InstantiationException, IllegalAccessException {
		Map mapAdded = map.getClass().newInstance();
		Iterator<? extends Map.Entry> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = it.next();
			Object key = entry.getKey();
			if (key instanceof Holder) {
				key = ((Holder) key).getValue();
			}
			Object value = entry.getValue();
			if (value instanceof Holder) {
				value = ((Holder) value).getValue();
			}

			it.remove();
			mapAdded.put(key, value);
			if (value instanceof Collection) {
				processCollection2Holder((Collection) value);
			} else if (value instanceof Map) {
				processMap2Holder((Map) value);
			}
			if (key instanceof Collection) {
				processCollection2Holder((Collection) key);
			} else if (key instanceof Map) {
				processMap2Holder((Map) key);
			} else if (value instanceof List) {
				processList2Holder((List) value);
			}
		}
		map.putAll(mapAdded); // add back the unwraped values
	}

	/**
	 * Parses a sequence of attributes which belong to a given entity.
	 * 
	 * @param xmlr
	 *            XMLStreamReader
	 * @param entity
	 *            ONDEXEntity
	 * @throws XMLStreamException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	protected void parseAttributes(XMLStreamReader xmlr, ONDEXEntity entity)
			throws XMLStreamException, JAXBException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		int eventType = xmlr.getEventType();

		while (xmlr.hasNext() && eventType != XMLStreamConstants.END_ELEMENT) {

			if (eventType == XMLStreamConstants.START_ELEMENT
					&& (xmlr.getLocalName().equals(XMLTagNames.RELATIONGDS) || xmlr
							.getLocalName().equals(XMLTagNames.CONCEPTGDS))) {

				xmlr.nextTag(); // attrname
				AttributeName attrName = parseAttributeName(xmlr).get();

				if (this.ignoreAttributeNames != null
						&& this.ignoreAttributeNames.contains(attrName)) {
					xmlr.nextTag(); // value
					xmlr.nextTag(); // doindex
				} else {
					xmlr.nextTag(); // value or literal
					Object value;

					String className = null;
					for (int i = 0; i < xmlr.getAttributeCount(); i++) {
						if (xmlr.getAttributeLocalName(i).equals(
								XMLTagNames.JAVA_CLASS)) {
							className = xmlr.getAttributeValue(i);
						}
					}
					if (className == null) {
						value = Marshaller.getMarshaller().fromXML(xmlr.getElementText());
					} else {
						xmlr.nextTag(); // walk into literal

						// fixme: perhaps should get this via a classloader
						Class valueClass = Class.forName(className);
						value = jaxbUnmarshaller.unmarshal(xmlr, valueClass)
								.getValue();

						// unbox value from holder
						if (value instanceof Holder)
							value = ((Holder) value).getValue();

						if (value instanceof Collection) {
							processCollection2Holder((Collection) value);
						} else if (value instanceof Map) {
							processMap2Holder((Map) value);
						} else if (value instanceof List) {
							processList2Holder((List) value);
						}
					}

					xmlr.nextTag(); // doindex
					boolean doIndex = Boolean.parseBoolean(xmlr
							.getElementText());

					// sanity checks here for possible java class clash
					if (!(attrName.getDataType().isAssignableFrom(value
							.getClass()))) {
						this.errorMessages
								.add("Found class clash while parsing attributes with attribute name "
										+ attrName.toString()
										+ ".\nShould be "
										+ attrName.getDataTypeAsString()
										+ " but found "
										+ value.getClass()
										+ ". Trying to fix it.");

						// Conversion to String is easy
						if (attrName.getDataType().isAssignableFrom(
								String.class)) {
							value = value.toString();
						}

						// try to parse a double
						else if (attrName.getDataType().isAssignableFrom(
								Double.class)) {
							try {
								value = Double.valueOf(value.toString());
							} catch (NumberFormatException nfe) {
								this.errorMessages
										.add("Cannot convert "
												+ value.getClass()
												+ " to Double. Using default value 0.0.");
								value = Double.valueOf(0.0);
							}
						}

						// try to parse a integer
						else if (attrName.getDataType().isAssignableFrom(
								Integer.class)) {
							try {
								value = Integer.valueOf(value.toString());
							} catch (NumberFormatException nfe) {
								this.errorMessages
										.add("Cannot convert "
												+ value.getClass()
												+ " to Integer. Using default value 0.");
								value = Integer.valueOf(0);
							}
						}

						// try to parse a float
						else if (attrName.getDataType().isAssignableFrom(
								Float.class)) {
							try {
								value = Float.valueOf(value.toString());
							} catch (NumberFormatException nfe) {
								this.errorMessages.add("Cannot convert "
										+ value.getClass()
										+ " to Float. Using default value 0f.");
								value = Float.valueOf(0f);
							}
						}

						// all other
						else {
							this.errorMessages
									.add("No conversion possible. Trying to cast object.");
							value = attrName.getDataType().cast(value);
						}
					}

					entity.createAttribute(attrName, value, doIndex);
				}
				xmlr.nextTag(); // skip end tag
			}

			xmlr.nextTag();
			eventType = xmlr.getEventType();
		} // end while
	}
}
