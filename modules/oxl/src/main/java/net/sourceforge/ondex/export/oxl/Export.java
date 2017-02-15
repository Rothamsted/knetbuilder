package net.sourceforge.ondex.export.oxl;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.CompressResultsArguementDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.config.OndexJAXBContextRegistry;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.export.ONDEXExport;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;

import com.ctc.wstx.api.WstxOutputProperties;
import com.ctc.wstx.io.CharsetNames;
import com.ctc.wstx.stax.WstxOutputFactory;

/**
 * Builds complete XML exchange format documents and/or ONDEX relations and
 * ONDEX concepts or other important ONDEX XML elements.
 * 
 * @author sierenk, taubertj
 * @author Matthew Pocock
 */
@Status(description = "Tested March 2010 (Artem Lysenko)", status = StatusType.STABLE)
@Authors(authors = { "Matthew Pocock", "Jan Taubert", "K Sieren" }, emails = {
		"drdozer at users.sourceforge.net",
		"jantaubert at users.sourceforge.net", "" })
@Custodians(custodians = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
public class Export extends ONDEXExport implements Monitorable {

	// current version of OXL
	public static final String version = "1.8";

	// toggle debugging
	private static final boolean DEBUG = true;

	// toggle pretty printing
	private boolean prettyPrint = false;

	// final pattern
	private final String newline = "\n";

	// parameter to exclude all of selected feature
	private static final String ALL = "ALL";

	// static XML variable definitions
	private final static String ONDEXNAMESPACE = "http://ondex.sourceforge.net";

	private final static String SCHEMANAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";

	private final static String SCHEMALOCATION = "http://ondex.sourceforge.net/ondex.xsd";

	// the registry used to create the JAXB marshaller
	private static OndexJAXBContextRegistry jaxbRegistry;

	// export concepts that do not have relations, default true
	private boolean writeIsolatedConcepts = true;

	// mapping concepts to relation counts
	private Map<Integer, Integer> conIdToRelationCounts = null;

	// A list of the Attributes of Attribute values to exclude. String ALL =>
	// Excludes all attributes.
	private Set<String> excludeGDSSet = new HashSet<String>();

	// A list of the ConceptClasses to be excluded in the export.
	private Set<String> excludeCCSet = new HashSet<String>();

	// A list of the RelationTypes to be excluded in the export.
	private Set<String> excludeRt = new HashSet<String>();

	private RefOrVal nestedRule = RefOrVal.REF;

	private int maxProgress = 1;

	private int minProgress = 0;

	private int progress = 0;

	private boolean cancelled = false;

	private String state = Monitorable.STATE_IDLE;

	private OutputStream outStream;

	/**
	 * Other application dependent annotation data for this export, can be null
	 */
	protected Map<String, String> annotations = null;

	static {
		jaxbRegistry = OndexJAXBContextRegistry.instance();
		jaxbRegistry.addClassBindings(ListHolder.class, ColorHolder.class,
				CollectionHolder.class, SetHolder.class, MapHolder.class);
		jaxbRegistry.addHolder(Map.class, MapHolder.class);
		jaxbRegistry.addHolder(Set.class, SetHolder.class);
		jaxbRegistry.addHolder(List.class, ListHolder.class);
		jaxbRegistry.addHolder(Color.class, ColorHolder.class);
		jaxbRegistry.addHolder(Collection.class, CollectionHolder.class);
	}

	public void setLegacyMode(boolean legacy) {
		if (legacy)
			nestedRule = RefOrVal.VAL;
		else
			nestedRule = RefOrVal.REF;
	}

	public boolean getLegacyMode() {
		switch (nestedRule) {
		case REF:
			return false;
		case VAL:
			return true;
		}

		throw new AssertionError("nestedRule was neither REF nor VAL: "
				+ nestedRule);
	}

	private Marshaller jaxbMarshaller;

	/**
	 * @return the marshaller (will create if not initialized)
	 * @throws JAXBException
	 *             error createing Marshaller
	 */
	public Marshaller getMarshaller(boolean rebuild) throws JAXBException {
		if (jaxbMarshaller == null || rebuild) {
			if (graph != null)
				jaxbMarshaller = jaxbRegistry.createMarshaller(graph
						.getMetaData());
			else
				jaxbMarshaller = jaxbRegistry.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
			jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
		}
		return jaxbMarshaller;
	}

	/**
	 * This method is optimised as a validation algorithm...strings without
	 * invalid chars will speed through fast and replacement will take more time
	 * 
	 * @param in
	 *            String to correct
	 * @return corrected string with invalid characters replaced by a single
	 *         space
	 */
	public static String stripInvalidXMLCharacters(final String in) {
		String out;

		if (in == null || in.length() == 0) {
			out = in; // vacancy test.
		} else {
			Set<Character> invalidChars = null;

			for (int i = 0; i < in.length(); i++) {
				char current = in.charAt(i);

				// get all UTF8 chars
				if ((current == 0x9) || (current == 0xA) || (current == 0xD)
						|| ((current >= 0x20) && (current <= 0xD7FF))
						|| ((current >= 0xE000) && (current <= 0xFFFD))
						|| ((current >= 0x10000) && (current <= 0x10FFFF))) {
					// ok its a valid char
				} else { // its invalid
					if (invalidChars == null) {
						invalidChars = new HashSet<Character>();
					}
					invalidChars.add(current);
				}
			}
			out = in;
			if (invalidChars != null) {
				for (Character invalidChar : invalidChars) {
					out = out.replace(invalidChar, ' ');
				}
			}
		}
		return out;
	}

	protected void buildIdRef(XMLStreamWriter2 xmlw, MetaData md)
			throws XMLStreamException {
		xmlw.writeStartElement(XMLTagNames.ID_REF);
		xmlw.writeCharacters(md.getId());
		xmlw.writeEndElement();
	}

	/**
	 * Builds a AttributName tag.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param attr
	 *            an AttributeName object
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	protected void buildAttributeName(XMLStreamWriter2 xmlw,
			AttributeName attr, RefOrVal rov) throws XMLStreamException {

		xmlw.writeStartElement(XMLTagNames.ATTRIBUTENAME);
		buildAttributeNameContent(xmlw, attr, rov);
		xmlw.writeEndElement();
	}

	/**
	 * Builds the content of the AttributName object.
	 * 
	 * @param xmlw
	 *            the stream to write in
	 * @param attr
	 *            an AttributeName object
	 * @param rov
	 *            write as a reference (idRef) or a full value
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	private void buildAttributeNameContent(XMLStreamWriter2 xmlw,
			AttributeName attr, RefOrVal rov) throws XMLStreamException {

		switch (rov) {
		case REF:
			buildIdRef(xmlw, attr);
			break;
		case VAL:
			xmlw.writeStartElement(XMLTagNames.ID);
			xmlw.writeCharacters(attr.getId());
			xmlw.writeEndElement();

			xmlw.writeStartElement(XMLTagNames.FULLNAME);
			xmlw.writeCharacters(attr.getFullname());
			xmlw.writeEndElement();

			xmlw.writeStartElement(XMLTagNames.DESCRIPTION);
			xmlw.writeCharacters(attr.getDescription());
			xmlw.writeEndElement();

			Unit unit = attr.getUnit();
			// unitname is optional
			if (unit != null) {
				xmlw.writeStartElement(XMLTagNames.UNIT);
				buildUnitContent(xmlw, unit, RefOrVal.VAL);
				xmlw.writeEndElement();
			}

			xmlw.writeStartElement(XMLTagNames.DATATYPE);
			xmlw.writeCharacters(attr.getDataTypeAsString());
			xmlw.writeEndElement();

			// specialisationOf is optional
			AttributeName spec = attr.getSpecialisationOf();
			if (spec != null) {
				xmlw.writeStartElement(XMLTagNames.SPECIALISATIONOF);
				buildAttributeNameContent(xmlw, spec, nestedRule);
				xmlw.writeEndElement();
			}
		}
	}

	/**
	 * Builds lists of attributenametype with related tag.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param alist
	 *            list of attributenames
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	protected void buildAttributeNames(XMLStreamWriter2 xmlw,
			Set<AttributeName> alist) throws XMLStreamException {

		if (prettyPrint)
			xmlw.writeCharacters(newline);
		xmlw.writeStartElement(XMLTagNames.ATTRIBUTENAMES);
		for (AttributeName an : alist) {
			buildAttributeName(xmlw, an, RefOrVal.VAL);
		}
		xmlw.writeEndElement(); // end AttributeNames
	}

	/**
	 * Writes a concept tag in a xml stream writer.
	 * 
	 * @param xmlw
	 *            the stream to write in
	 * @param c
	 *            an ONDEX Concept object
	 * @throws XMLStreamException
	 *             if xml writing fails
	 * @throws javax.xml.bind.JAXBException
	 *             if writing Attribute values fails
	 */
	protected void buildConcept(XMLStreamWriter2 xmlw, ONDEXConcept c)
			throws XMLStreamException, JAXBException {

		xmlw.writeStartElement(XMLTagNames.CONCEPT);
		xmlw.writeStartElement(XMLTagNames.ID);
		xmlw.writeCharacters(String.valueOf(c.getId()));
		xmlw.writeEndElement();

		xmlw.writeStartElement(XMLTagNames.PID);
		// may have invalid chars
		xmlw.writeCharacters(stripInvalidXMLCharacters(c.getPID()));
		xmlw.writeEndElement();

		xmlw.writeStartElement(XMLTagNames.ANNOTATION);
		// may have invalid chars
		xmlw.writeCharacters(stripInvalidXMLCharacters(c.getAnnotation()));
		xmlw.writeEndElement();

		// may have invalid chars
		xmlw.writeStartElement(stripInvalidXMLCharacters(XMLTagNames.DESCRIPTION));
		xmlw.writeCharacters(c.getDescription());
		xmlw.writeEndElement();

		xmlw.writeStartElement(XMLTagNames.ELEMENTOF);
		buildDataSourceContent(xmlw, c.getElementOf(), nestedRule);
		xmlw.writeEndElement();

		xmlw.writeStartElement(XMLTagNames.OFTYPE);
		buildConceptClassContent(xmlw, c.getOfType(), nestedRule);
		xmlw.writeEndElement();

		// build evidence type list
		Set<EvidenceType> et = c.getEvidence();
		if (prettyPrint)
			xmlw.writeCharacters(newline);
		buildEvidences(xmlw, et, nestedRule);
		// end evidence type list

		// build concept names list
		Set<ConceptName> cn = c.getConceptNames();
		if (prettyPrint)
			xmlw.writeCharacters(newline);
		buildConceptNames(xmlw, cn);
		// end conames list

		// build concept acs list
		Set<ConceptAccession> ca = c.getConceptAccessions();
		if (prettyPrint)
			xmlw.writeCharacters(newline);
		buildConceptAccessions(xmlw, ca);
		// end coaccessions list

		// build cogds list
		Set<Attribute> attribute = c.getAttributes();
		if (prettyPrint)
			xmlw.writeCharacters(newline);
		buildConceptAttributes(xmlw, attribute);
		// end cogds list

		// build context list
		Set<ONDEXConcept> context = c.getTags();
		if (context.size() > 0) {
			if (prettyPrint)
				xmlw.writeCharacters(newline);
			buildContexts(xmlw, context);
		}
		// end context list

		xmlw.writeEndElement(); // end tag CONCEPT
	}

	/**
	 * Writes a concept_accession tag in a xml stream writer.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param ca
	 *            a ConceptAccession object
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	protected void buildConceptAccession(XMLStreamWriter2 xmlw,
			ConceptAccession ca) throws XMLStreamException {

		xmlw.writeStartElement(XMLTagNames.CONCEPTACCESSION);

		xmlw.writeStartElement(XMLTagNames.ACCESSION);
		// may have invalid chars
		xmlw.writeCharacters(stripInvalidXMLCharacters(ca.getAccession()));
		xmlw.writeEndElement();

		xmlw.writeStartElement(XMLTagNames.ELEMENTOF);
		buildDataSourceContent(xmlw, ca.getElementOf(), nestedRule);
		xmlw.writeEndElement();

		Boolean ambiguous = ca.isAmbiguous();
		xmlw.writeStartElement(XMLTagNames.AMBIGUOUS);
		xmlw.writeCharacters(ambiguous.toString());
		xmlw.writeEndElement();

		xmlw.writeEndElement(); // end tag concept_accession
	}

	/**
	 * Builds a coaccessions tag which contains a list of ConceptAccessions
	 * 
	 * @param xmlw
	 *            the stream to write in
	 * @param calist
	 *            ConceptAccessions
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	protected void buildConceptAccessions(XMLStreamWriter2 xmlw,
			Set<ConceptAccession> calist) throws XMLStreamException {

		xmlw.writeStartElement(XMLTagNames.COACCESSIONS);
		for (ConceptAccession ca : calist) {
			buildConceptAccession(xmlw, ca);
		}
		xmlw.writeEndElement(); // end coaccessions
	}

	/**
	 * Writes concept_class tag in a xml stream writer.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param cc
	 *            a ConceptClass object
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	protected void buildConceptClass(XMLStreamWriter2 xmlw, ConceptClass cc)
			throws XMLStreamException {

		xmlw.writeStartElement(XMLTagNames.CC);
		buildConceptClassContent(xmlw, cc, RefOrVal.VAL);
		xmlw.writeEndElement();
	}

	/**
	 * Writes concept_class content tags in a xml stream writer.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param cc
	 *            a ConceptClass object
	 * @param rov
	 *            write as a reference (idRef) or a full value
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	private void buildConceptClassContent(XMLStreamWriter2 xmlw,
			ConceptClass cc, RefOrVal rov) throws XMLStreamException {

		switch (rov) {
		case REF:
			buildIdRef(xmlw, cc);
			break;
		case VAL:
			xmlw.writeStartElement(XMLTagNames.ID);
			xmlw.writeCharacters(cc.getId());
			xmlw.writeEndElement();

			xmlw.writeStartElement(XMLTagNames.FULLNAME);
			xmlw.writeCharacters(cc.getFullname());
			xmlw.writeEndElement();

			xmlw.writeStartElement(XMLTagNames.DESCRIPTION);
			xmlw.writeCharacters(cc.getDescription());
			xmlw.writeEndElement();

			// specialisationOf is optional
			ConceptClass spec = cc.getSpecialisationOf();
			if (spec != null) {
				xmlw.writeStartElement(XMLTagNames.SPECIALISATIONOF);
				buildConceptClassContent(xmlw, spec, nestedRule);
				xmlw.writeEndElement();
			}
		}
	}

	/**
	 * Builds lists of conceptclasstype with related tag.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param cclist
	 *            list of conceptclasses
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	protected void buildConceptClasses(XMLStreamWriter2 xmlw,
			Set<ConceptClass> cclist) throws XMLStreamException {

		if (prettyPrint)
			xmlw.writeCharacters(newline);
		xmlw.writeStartElement(XMLTagNames.CONCEPTCLASSES);
		for (ConceptClass cc : cclist) {
			buildConceptClass(xmlw, cc);
		}
		xmlw.writeEndElement(); // end conceptclasses
	}

	/**
	 * Writes Attribute tag in a xml stream writer.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param attribute
	 *            Attribute object
	 * @throws XMLStreamException
	 *             if xml writing fails if something fails in the underlying xml
	 *             stream
	 * @throws javax.xml.bind.JAXBException
	 *             if the gds value could not be written
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void buildAttribute(XMLStreamWriter2 xmlw, Attribute attribute,
			String tag) throws XMLStreamException, JAXBException {

		// determines if we discover a new class that needs registering with
		// JAXB
		boolean rebuildMarshall = false;

		if (!excludeGDSSet.contains(ALL)
				&& !excludeGDSSet.contains(attribute.getOfType().getId())) {
			xmlw.writeStartElement(tag);
			// fixme: this may be in the wrong place

			buildAttributeName(xmlw, attribute.getOfType(), nestedRule);

			xmlw.writeStartElement(XMLTagNames.VALUE);

			Set<Class> classes = null;

			Object gdsValue = jaxbRegistry.applyHolder(attribute.getValue()
					.getClass(), attribute.getValue());

			if (gdsValue instanceof CollectionHolder) {
				// wrap all elements in the collection recursively with holders
				classes = processCollection2Holder((CollectionHolder) gdsValue);
			} else if (gdsValue instanceof MapHolder) {
				// wrap all elements in the map recursively with holders
				classes = processMap2Holder((MapHolder) gdsValue);
			} else if (gdsValue instanceof ListHolder) {
				// wrap all elements in the list recursively with holders
				classes = processList2Holder((ListHolder) gdsValue);
			}

			if (classes != null) {
				for (Class c : classes) {
					// go through classes inside data structure and check they
					// are registered
					if (!jaxbRegistry.isClassRegistered(c)) {
						jaxbRegistry.addClassBindings(c);
						rebuildMarshall = true;
					}
				}
			}

			if (jaxbRegistry.hasAttributeName(attribute.getOfType())) {
				jaxbRegistry.addAttribute(attribute.getOfType());
			}

			Class<?> gdsClass = gdsValue.getClass();

			if (!jaxbRegistry.isClassRegistered(gdsValue.getClass())) {
				jaxbRegistry.addClassBindings(gdsValue.getClass());
				rebuildMarshall = true;
			}

			xmlw.writeAttribute(XMLTagNames.JAVA_CLASS, gdsClass.getName());

			JAXBElement el = new JAXBElement(
					new QName("", XMLTagNames.LITERAL), gdsClass, gdsValue);

			getMarshaller(rebuildMarshall).marshal(el, xmlw);
			xmlw.writeEndElement(); // value

			boolean doindex = attribute.isDoIndex();
			xmlw.writeStartElement(XMLTagNames.DOINDEX);
			xmlw.writeCharacters(String.valueOf(doindex));
			xmlw.writeEndElement();

			xmlw.writeEndElement(); // end tag gds concept
		}
	}

	/**
	 * Recursively wraps a data structure elements in Holders starting at a list
	 * 
	 * @param listH
	 *            some list that needs processing
	 * @return any non holder classes in this data structure that may need
	 *         registering
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Set<Class> processList2Holder(ListHolder<?> listH)
			throws JAXBException {
		try {
			Set classes = new HashSet();
			List list = listH.getValue();
			List added = list.getClass().newInstance();
			Iterator<?> it = list.iterator();
			while (it.hasNext()) {
				Object value = it.next();
				Object gdsValue = jaxbRegistry.applyHolder(value.getClass(),
						value);
				if (value.equals(gdsValue)) {
					classes.add(value.getClass());
					// its not a holder class so potentially needs registering
					continue;
				}
				it.remove();
				added.add(gdsValue);
				if (gdsValue instanceof CollectionHolder) {
					classes.addAll(processCollection2Holder((CollectionHolder) gdsValue));
				} else if (gdsValue instanceof MapHolder) {
					classes.addAll(processMap2Holder((MapHolder) gdsValue));
				} else if (gdsValue instanceof ListHolder) {
					classes.addAll(processList2Holder((ListHolder) gdsValue));
				}
			}
			list.addAll(added); // add back the holders
			listH.setValue(list);
			return classes;
		} catch (InstantiationException e) {
			throw new JAXBException(e); // TODO make a nicer exception here
		} catch (IllegalAccessException e) {
			throw new JAXBException(e); // TODO make a nicer exception here
		}
	}

	/**
	 * Recursively wraps a data structure elements in Holders starting at a
	 * collection
	 * 
	 * @param collectionH
	 *            some collection that needs processing
	 * @return any non holder classes in this data structure that may need
	 *         registering
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Set<Class> processCollection2Holder(CollectionHolder<?> collectionH)
			throws JAXBException {
		try {
			Set classes = new HashSet();
			Collection collection = collectionH.getValue();
			Collection added = collection.getClass().newInstance();
			Iterator<?> it = collection.iterator();
			while (it.hasNext()) {
				Object value = it.next();
				Object gdsValue = jaxbRegistry.applyHolder(value.getClass(),
						value);
				if (value.equals(gdsValue)) {
					classes.add(value.getClass());
					// its not a holder class so potentially needs registering
					continue;
				}
				it.remove();
				added.add(gdsValue);
				if (gdsValue instanceof CollectionHolder) {
					classes.addAll(processCollection2Holder((CollectionHolder) gdsValue));
				} else if (gdsValue instanceof MapHolder) {
					classes.addAll(processMap2Holder((MapHolder) gdsValue));
				} else if (gdsValue instanceof ListHolder) {
					classes.addAll(processList2Holder((ListHolder) gdsValue));
				}
			}
			collection.addAll(added); // add back the holders
			collectionH.setValue(collection);
			return classes;
		} catch (InstantiationException e) {
			throw new JAXBException(e); // TODO make a nicer exception here
		} catch (IllegalAccessException e) {
			throw new JAXBException(e); // TODO make a nicer exception here
		}
	}

	/**
	 * Recursively wraps a data structure elements in Holders starting at a map
	 * 
	 * @param mapH
	 *            some collection that needs processing
	 * @return any non holder classes in this data structure that may need
	 *         registering
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Set<Class> processMap2Holder(MapHolder<?, ?> mapH)
			throws JAXBException {
		try {
			Set classes = new HashSet();
			Map map = mapH.getValue();
			Map mapAdded = map.getClass().newInstance();
			Iterator<? extends Map.Entry> it = map.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = it.next();
				Object key = entry.getKey();
				Object value = entry.getValue();

				Object newkey = jaxbRegistry.applyHolder(entry.getKey()
						.getClass(), key);
				if (newkey.equals(key)) {
					classes.add(newkey.getClass());
					// its not a holder class so potentially needs registering
				}
				Object newvalue = jaxbRegistry.applyHolder(entry.getValue()
						.getClass(), value);
				if (newvalue.equals(value)) {
					classes.add(newvalue.getClass());
					// its not a holder class so potentially needs registering
				}
				if (newvalue instanceof CollectionHolder) {
					classes.addAll(processCollection2Holder((CollectionHolder) newvalue));
				} else if (newvalue instanceof MapHolder) {
					classes.addAll(processMap2Holder((MapHolder) newvalue));
				} else if (newvalue instanceof ListHolder) {
					classes.addAll(processList2Holder((ListHolder) newvalue));
				}
				if ((newkey) instanceof CollectionHolder) {
					classes.addAll(processCollection2Holder((CollectionHolder) newkey));
				} else if (newkey instanceof MapHolder) {
					classes.addAll(processMap2Holder((MapHolder) newkey));
				} else if (newkey instanceof ListHolder) {
					classes.addAll(processList2Holder((ListHolder) newkey));
				}
				it.remove();
				mapAdded.put(newkey, newvalue);
			}
			map.putAll(mapAdded); // add back the holders
			mapH.setValue(map);
			return classes;
		} catch (InstantiationException e) {
			throw new JAXBException(e); // TODO make a nicer exception here
		} catch (IllegalAccessException e) {
			throw new JAXBException(e); // TODO make a nicer exception here
		}
	}

	/**
	 * Builds a cogds tag which contains a list of ConceptAttribute
	 * 
	 * @param xmlw
	 *            the stream to write in
	 * @param cgdslist
	 *            ConceptAttribute
	 * @throws XMLStreamException
	 *             if xml writing fails
	 * @throws javax.xml.bind.JAXBException
	 *             if writing Attribute values fails
	 */
	protected void buildConceptAttributes(XMLStreamWriter2 xmlw,
			Set<Attribute> cgdslist) throws XMLStreamException, JAXBException {

		xmlw.writeStartElement(XMLTagNames.COGDS);
		for (Attribute attribute : cgdslist) {
			buildAttribute(xmlw, attribute, XMLTagNames.CONCEPTGDS);
		}
		xmlw.writeEndElement(); // end cogds
	}

	/**
	 * Writes concept_name tag in a xml stream writer.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param cn
	 *            a ConceptName object
	 * @throws XMLStreamException
	 *             if xml writing fails if writing the xml failed
	 */
	protected void buildConceptName(XMLStreamWriter2 xmlw, ConceptName cn)
			throws XMLStreamException {

		xmlw.writeStartElement(XMLTagNames.CONCEPTNAME);

		xmlw.writeStartElement(XMLTagNames.NAME);
		xmlw.writeCharacters(stripInvalidXMLCharacters(cn.getName()));
		xmlw.writeEndElement();

		boolean isPreferred = cn.isPreferred();
		xmlw.writeStartElement(XMLTagNames.ISPREFERRED);
		xmlw.writeCharacters(String.valueOf(isPreferred));
		xmlw.writeEndElement();

		xmlw.writeEndElement(); // end tag CONCEPTNAME
	}

	/**
	 * Builds a conames tag which contains a list of concept names.
	 * 
	 * @param xmlw
	 *            the stream to write in
	 * @param cnlist
	 *            ConceptNames
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	protected void buildConceptNames(XMLStreamWriter2 xmlw,
			Set<ConceptName> cnlist) throws XMLStreamException {

		xmlw.writeStartElement(XMLTagNames.CONAMES);
		for (ConceptName cn : cnlist) {
			buildConceptName(xmlw, cn);
		}
		xmlw.writeEndElement(); // end CONAMES
	}

	/**
	 * Builds a list of concepts within the related tag.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param cit
	 *            ONDEXConcepts
	 * @throws XMLStreamException
	 *             if xml writing fails
	 * @throws javax.xml.bind.JAXBException
	 *             if writing Attribute values fails
	 */
	protected void buildConcepts(XMLStreamWriter2 xmlw, Set<ONDEXConcept> cit)
			throws XMLStreamException, JAXBException {

		// update max progress
		progress = 0;
		maxProgress = cit.size();

		NumberFormat formatter = new DecimalFormat(".00");
		NumberFormat format = NumberFormat.getInstance();

		// concepts list
		if (prettyPrint)
			xmlw.writeCharacters(newline);
		xmlw.writeStartElement(XMLTagNames.CONCEPTS);
		if (DEBUG)
			System.out.println("Total concepts to export: "
					+ format.format(cit.size()));
		int i = 0;
		for (ONDEXConcept concept : cit) {
			progress++;
			state = "Building concept " + progress + " of " + maxProgress;
			if (cancelled)
				break;
			if (prettyPrint)
				xmlw.writeCharacters(newline);
			buildConcept(xmlw, concept);
			i++;
			if (i % 5000 == 0) {
				System.out.println(format.format(i)
						+ " concepts written ("
						+ (formatter.format((double) i / (double) cit.size()
								* 100)) + "%).");
			}
		}
		xmlw.writeEndElement(); // end concepts
		if (prettyPrint)
			xmlw.writeCharacters(newline);
		// end concepts list
	}

	/**
	 * Writes context tag in a xml stream writer.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param context
	 *            the actual context
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	private void buildContext(XMLStreamWriter2 xmlw, ONDEXConcept context)
			throws XMLStreamException {

		xmlw.writeStartElement(XMLTagNames.CONTEXT);

		xmlw.writeStartElement(XMLTagNames.ID);
		xmlw.writeCharacters(String.valueOf(context.getId()));
		xmlw.writeEndElement();

		xmlw.writeEndElement();
	}

	/**
	 * Builds a contexts tag which contains a list of context
	 * 
	 * @param xmlw
	 *            the stream to write in
	 * @param contexts
	 *            list of context
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	private void buildContexts(XMLStreamWriter2 xmlw, Set<ONDEXConcept> contexts)
			throws XMLStreamException {

		xmlw.writeStartElement(XMLTagNames.CONTEXTS);
		for (ONDEXConcept c : contexts) {
			buildContext(xmlw, c);
		}
		xmlw.writeEndElement();

	}

	/**
	 * Writes DataSource tag in a xml stream writer.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param dataSource
	 *            Cv object
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	protected void buildDataSource(XMLStreamWriter2 xmlw, DataSource dataSource)
			throws XMLStreamException {

		xmlw.writeStartElement(XMLTagNames.CV);
		buildDataSourceContent(xmlw, dataSource, RefOrVal.VAL);
		xmlw.writeEndElement();
	}

	/**
	 * Writes DataSource content in a xml stream writer.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param dataSource
	 *            DataSource object
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	private void buildDataSourceContent(XMLStreamWriter2 xmlw,
			DataSource dataSource, RefOrVal rov) throws XMLStreamException {

		switch (rov) {
		case REF:
			buildIdRef(xmlw, dataSource);
			break;
		case VAL:
			xmlw.writeStartElement(XMLTagNames.ID);
			xmlw.writeCharacters(dataSource.getId());
			xmlw.writeEndElement();

			xmlw.writeStartElement(XMLTagNames.FULLNAME);
			xmlw.writeCharacters(dataSource.getFullname());
			xmlw.writeEndElement();

			xmlw.writeStartElement(XMLTagNames.DESCRIPTION);
			xmlw.writeCharacters(dataSource.getDescription());
			xmlw.writeEndElement();
		}
	}

	/**
	 * Builds lists of cvtype with related tag.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param dataSourceList
	 *            list of cvs
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	protected void buildDataSources(XMLStreamWriter2 xmlw,
			Set<DataSource> dataSourceList) throws XMLStreamException {

		if (prettyPrint)
			xmlw.writeCharacters(newline);
		xmlw.writeStartElement(XMLTagNames.CVS);
		for (DataSource dataSource : dataSourceList) {
			buildDataSource(xmlw, dataSource);
		}
		xmlw.writeEndElement(); // end cvs
	}

	/**
	 * Builds a complete ONDEX XML exchange format document.
	 * 
	 * @param xmlw
	 *            streamwriter to write the document in
	 * @param graph
	 *            a ONDEXGraph which contains the data
	 * @throws XMLStreamException
	 *             if xml writing fails
	 * @throws javax.xml.bind.JAXBException
	 *             if writing Attribute values fails
	 */
	public void buildDocument(XMLStreamWriter2 xmlw, ONDEXGraph graph)
			throws XMLStreamException, JAXBException {

		this.graph = graph;
		xmlw.writeStartDocument(CharsetNames.CS_UTF8, "1.0");

		buildOndexDataTag(xmlw, filterConcepts(graph.getConcepts()),
				filterRelations(graph.getRelations()));

		xmlw.writeEndDocument();
	}

	/**
	 * Builds a complete ONDEX XML exchange format document with these concepts
	 * and these relations.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param concepts
	 *            amount of concepts
	 * @param relations
	 *            amount of relations
	 * @throws XMLStreamException
	 *             if xml writing fails
	 * @throws javax.xml.bind.JAXBException
	 *             if writing Attribute values fails
	 */
	public void buildDocument(XMLStreamWriter2 xmlw,
			Set<ONDEXConcept> concepts, Set<ONDEXRelation> relations)
			throws XMLStreamException, JAXBException {

		xmlw.writeStartDocument(CharsetNames.CS_UTF8, "1.0");

		buildOndexDataTag(xmlw, filterConcepts(concepts),
				filterRelations(relations));

		xmlw.writeEndDocument();
	}

	/**
	 * Builds a evidences tag which conatins a list of evidences.
	 * 
	 * @param xmlw
	 *            the stream to write in
	 * @param etlist
	 *            EvidenceTypes
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	protected void buildEvidences(XMLStreamWriter2 xmlw,
			Set<EvidenceType> etlist, RefOrVal rov) throws XMLStreamException {

		if (prettyPrint)
			xmlw.writeCharacters(newline);
		xmlw.writeStartElement(XMLTagNames.EVIDENCES);
		for (EvidenceType et : etlist) {
			buildEvidenceType(xmlw, et, rov);
		}
		xmlw.writeEndElement(); // end evidences
	}

	/**
	 * Writes evidence tag in a xml stream writer.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param et
	 *            EvidenceType object
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	protected void buildEvidenceType(XMLStreamWriter2 xmlw, EvidenceType et,
			RefOrVal rov) throws XMLStreamException {

		xmlw.writeStartElement(XMLTagNames.EVIDENCE);

		switch (rov) {
		case REF:
			buildIdRef(xmlw, et);
			break;
		case VAL:
			xmlw.writeStartElement(XMLTagNames.ID);
			xmlw.writeCharacters(et.getId());
			xmlw.writeEndElement();

			xmlw.writeStartElement(XMLTagNames.FULLNAME);
			xmlw.writeCharacters(et.getFullname());
			xmlw.writeEndElement();

			xmlw.writeStartElement(XMLTagNames.DESCRIPTION);
			xmlw.writeCharacters(et.getDescription());
			xmlw.writeEndElement();
		}

		xmlw.writeEndElement(); // end tag evidence type
	}

	/**
	 * Builds a complete ONDEX XML exchange format document.
	 * 
	 * @param xmlw
	 *            streamwriter to write the document in
	 * @param md
	 *            ONDEX Meta Data
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	public void buildMetaDataDocument(XMLStreamWriter2 xmlw,
			ONDEXGraphMetaData md) throws XMLStreamException {

		xmlw.writeStartDocument(CharsetNames.CS_UTF8, "1.0");
		buildOndexMetaDataOndexTag(xmlw, md);
		xmlw.writeEndDocument();
	}

	/**
	 * Builds an XML ondexdataseq XML Element.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param concepts
	 *            Collection<AbstractConcept>
	 * @param relations
	 *            Collection<AbstractRelation>
	 * @throws XMLStreamException
	 *             if xml writing fails
	 * @throws javax.xml.bind.JAXBException
	 *             if writing Attribute values fails
	 */
	private void buildOndexDataSequenceTag(XMLStreamWriter2 xmlw,
			Set<ONDEXConcept> concepts, Set<ONDEXRelation> relations)
			throws XMLStreamException, JAXBException {

		xmlw.writeStartElement(XMLTagNames.ONDEXDATASEQ);

		// first build concepts
		buildConcepts(xmlw, concepts);

		// second build relations
		buildRelations(xmlw, relations);

		xmlw.writeEndElement(); // end ondexdatseq

		state = Monitorable.STATE_TERMINAL;
	}

	/**
	 * Builds an XML ondexdata XML Element.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param graphdata
	 *            AbstractONDEXGraphMetaData
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	private void buildOndexMetaDataOndexTag(XMLStreamWriter2 xmlw,
			ONDEXGraphMetaData graphdata) throws XMLStreamException {

		xmlw.setPrefix("ondex", ONDEXNAMESPACE);
		xmlw.writeStartElement(ONDEXNAMESPACE, "ondex");
		xmlw.writeNamespace("ondex", ONDEXNAMESPACE);
		xmlw.writeNamespace("xsi", SCHEMANAMESPACE);
		xmlw.writeNamespace("schemaLocation", SCHEMALOCATION);

		xmlw.writeStartElement(XMLTagNames.VERSION);
		xmlw.writeCharacters(version);
		xmlw.writeEndElement();

		buildOndexMetaDataTag(xmlw, graphdata);

		xmlw.writeEndElement(); // end ondexdata
	}

	/**
	 * Builds an XML ondexdata XML Element.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param concepts
	 *            AbstractConcepts
	 * @param relations
	 *            AbstractRelations
	 * @throws XMLStreamException
	 *             if xml writing fails
	 * @throws javax.xml.bind.JAXBException
	 *             if writing Attribute values fails
	 */
	private void buildOndexDataTag(XMLStreamWriter2 xmlw,
			Set<ONDEXConcept> concepts, Set<ONDEXRelation> relations)
			throws XMLStreamException, JAXBException {

		xmlw.setPrefix("ondex", ONDEXNAMESPACE);
		xmlw.writeStartElement(ONDEXNAMESPACE, "ondexdata");
		xmlw.writeNamespace("ondex", ONDEXNAMESPACE);
		xmlw.writeNamespace("xsi", SCHEMANAMESPACE);
		xmlw.writeNamespace("schemaLocation", SCHEMALOCATION);

		xmlw.writeStartElement(XMLTagNames.VERSION);
		xmlw.writeCharacters(version);
		xmlw.writeEndElement();

		// output graph info tag
		xmlw.writeStartElement(XMLTagNames.INFO);
		xmlw.writeStartElement(XMLTagNames.NUMBERCONCEPTS);
		xmlw.writeInt(concepts.size());
		xmlw.writeEndElement(); // end number of concepts
		xmlw.writeStartElement(XMLTagNames.NUMBERRELATIONS);
		xmlw.writeInt(relations.size());
		xmlw.writeEndElement(); // end number of relations
		if (graph != null) {
			// for a given graph write its name
			xmlw.writeStartElement(XMLTagNames.GRAPHNAME);
			xmlw.writeCharacters(graph.getName());
			xmlw.writeEndElement();
		}
		if (annotations != null) {
			// if there are annotations for a graph set
			xmlw.writeStartElement(XMLTagNames.GRAPHANNOTATIONS);
			for (String key : annotations.keySet()) {
				xmlw.writeStartElement(XMLTagNames.GRAPHANNOTATION);
				xmlw.writeAttribute(XMLTagNames.GRAPHANNOTATIONKEY, key);
				xmlw.writeCData(annotations.get(key));
				xmlw.writeEndElement();
			}
			xmlw.writeEndElement();
		}
		xmlw.writeEndElement(); // end info tag

		// add meta data on top only if by reference
		if (nestedRule.equals(RefOrVal.REF) && graph != null)
			buildOndexMetaDataTag(xmlw, graph.getMetaData());
		else if (nestedRule.equals(RefOrVal.REF) && graph == null)
			throw new IllegalArgumentException(
					"Cannot run in REF mode without a graph.");

		buildOndexDataSequenceTag(xmlw, concepts, relations);

		xmlw.writeEndElement(); // end ondexdata
	}

	/**
	 * Builds an XML ondex meta data XML Element.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param graphdata
	 *            AbstractONDEXGraphMetaData
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	private void buildOndexMetaDataTag(XMLStreamWriter2 xmlw,
			ONDEXGraphMetaData graphdata) throws XMLStreamException {

		Set<DataSource> itDataSource = graphdata.getDataSources();
		Set<Unit> itUnit = graphdata.getUnits();
		Set<AttributeName> itAn = graphdata.getAttributeNames();
		Set<EvidenceType> itEt = graphdata.getEvidenceTypes();
		Set<ConceptClass> itCc = graphdata.getConceptClasses();
		Set<RelationType> itRt = graphdata.getRelationTypes();

		buildOndexMetaDataTag(xmlw, itDataSource, itUnit, itAn, itEt, itCc,
				itRt);
	}

	/**
	 * Builds an XML ondex meta data XML Element.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param dslist
	 *            Control Vocabularies
	 * @param ulist
	 *            Units
	 * @param alist
	 *            AttributNames
	 * @param elist
	 *            Evidences
	 * @param cclist
	 *            Concept Classes
	 * @param rtlist
	 *            Relation Types
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	private void buildOndexMetaDataTag(XMLStreamWriter2 xmlw,
			Set<DataSource> dslist, Set<Unit> ulist, Set<AttributeName> alist,
			Set<EvidenceType> elist, Set<ConceptClass> cclist,
			Set<RelationType> rtlist) throws XMLStreamException {

		xmlw.writeStartElement(XMLTagNames.ONDEXMETADATA);

		// datasource list
		if (!dslist.isEmpty()) {
			buildDataSources(xmlw, dslist);
		}

		// units list
		if (!ulist.isEmpty()) {
			buildUnits(xmlw, ulist);
		}

		// attrnames list
		if (!alist.isEmpty()) {
			buildAttributeNames(xmlw, alist);
		}

		// evidences list
		if (!elist.isEmpty()) {
			buildEvidences(xmlw, elist, RefOrVal.VAL);
		}

		// conceptclasses list
		if (!cclist.isEmpty()) {
			buildConceptClasses(xmlw, cclist);
		}

		// relationtypes list
		if (!rtlist.isEmpty()) {
			buildRelationTypes(xmlw, rtlist);
		}

		xmlw.writeEndElement(); // end ondexmetadata tag
	}

	/**
	 * Writes a ONDEX Relation tag in a xml stream writer.
	 * 
	 * @param xmlw
	 *            the stream to write in
	 * @param r
	 *            ONDEX Relation data object
	 * @throws XMLStreamException
	 *             if xml writing fails
	 * @throws javax.xml.bind.JAXBException
	 *             if any Attribute values could not be serialized
	 */
	protected void buildRelation(XMLStreamWriter2 xmlw, ONDEXRelation r)
			throws XMLStreamException, JAXBException {

		ONDEXConcept from = r.getFromConcept();
		ONDEXConcept to = r.getToConcept();

		xmlw.writeStartElement(XMLTagNames.RELATION);

		xmlw.writeStartElement(XMLTagNames.FROMCONCEPT);
		xmlw.writeCharacters(String.valueOf(from.getId()));
		xmlw.writeEndElement();

		xmlw.writeStartElement(XMLTagNames.TOCONCEPT);
		xmlw.writeCharacters(String.valueOf(to.getId()));
		xmlw.writeEndElement();

		xmlw.writeStartElement(XMLTagNames.OFTYPE);
		buildRelationTypeContent(xmlw, r.getOfType(), nestedRule);
		xmlw.writeEndElement();

		// build evidence type list
		Set<EvidenceType> itEt = r.getEvidence();
		if (prettyPrint)
			xmlw.writeCharacters(newline);
		buildEvidences(xmlw, itEt, nestedRule);
		// end evidence type list

		// build relgds list
		Set<Attribute> itAttribute = r.getAttributes();
		if (prettyPrint)
			xmlw.writeCharacters(newline);
		buildRelationAttributes(xmlw, itAttribute);
		// end relgds list

		// build context list
		Set<ONDEXConcept> context = r.getTags();
		if (context.size() > 0) {
			if (prettyPrint)
				xmlw.writeCharacters(newline);
			buildContexts(xmlw, context);
		}
		// end context list

		xmlw.writeEndElement(); // end tag relation
	}

	/**
	 * Write a ONDEX RelationAttribute to XML.
	 * 
	 * @param xmlw
	 *            the stream to write in
	 * @param rgdslist
	 *            ONDEX RelationAttribute
	 * @throws XMLStreamException
	 *             if xml writing fails
	 * @throws javax.xml.bind.JAXBException
	 *             if any Attribute value could not be serialized
	 */
	protected void buildRelationAttributes(XMLStreamWriter2 xmlw,
			Set<Attribute> rgdslist) throws XMLStreamException, JAXBException {

		xmlw.writeStartElement(XMLTagNames.RELGDS);
		for (Attribute attribute : rgdslist) {
			buildAttribute(xmlw, attribute, XMLTagNames.RELATIONGDS);
		}
		xmlw.writeEndElement(); // end relgds
	}

	/**
	 * Builds a lists of relations within the related tag.
	 * 
	 * @param xmlw
	 *            the stream to write in
	 * @param rit
	 *            ONDEXRelations
	 * @throws XMLStreamException
	 *             if xml writing fails
	 * @throws javax.xml.bind.JAXBException
	 *             if the gds value could not be serialized
	 */
	protected void buildRelations(XMLStreamWriter2 xmlw, Set<ONDEXRelation> rit)
			throws XMLStreamException, JAXBException {

		// update max progress
		progress = 0;
		maxProgress = rit.size();

		NumberFormat formatter = new DecimalFormat(".00");
		NumberFormat format = NumberFormat.getInstance();

		// relations list
		if (prettyPrint)
			xmlw.writeCharacters(newline);
		xmlw.writeStartElement(XMLTagNames.RELATIONS);
		if (DEBUG)
			System.out.println("\nTotal relations to export: "
					+ format.format(rit.size()));
		int i = 0;
		for (ONDEXRelation relation : rit) {
			progress++;
			state = "Building relation " + progress + " of " + maxProgress;
			if (cancelled)
				break;
			if (prettyPrint)
				xmlw.writeCharacters(newline);
			buildRelation(xmlw, relation);
			i++;
			if (i % 5000 == 0) {
				System.out.println(format.format(i)
						+ " relations written ("
						+ (formatter.format((double) i / (double) rit.size()
								* 100)) + "%).");
			}
		}
		xmlw.writeEndElement(); // end relations
		if (prettyPrint)
			xmlw.writeCharacters(newline);
		// end relations list
	}

	/**
	 * Writes RelationType tag in a xml stream writer.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param rt
	 *            RelationType object
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	protected void buildRelationType(XMLStreamWriter2 xmlw, RelationType rt)
			throws XMLStreamException {

		xmlw.writeStartElement(XMLTagNames.RELATIONTYPE);
		buildRelationTypeContent(xmlw, rt, RefOrVal.VAL);
		xmlw.writeEndElement(); // end tag relation type
	}

	/**
	 * Writes RelationType Content tags in a xml stream writer.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param rt
	 *            RelationType object
	 * @param rov
	 *            write as a reference (idRef) or a full value
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	private void buildRelationTypeContent(XMLStreamWriter2 xmlw,
			RelationType rt, RefOrVal rov) throws XMLStreamException {

		switch (rov) {
		case REF:
			buildIdRef(xmlw, rt);
			break;
		case VAL:
			xmlw.writeStartElement(XMLTagNames.ID);
			xmlw.writeCharacters(rt.getId());
			xmlw.writeEndElement();

			xmlw.writeStartElement(XMLTagNames.FULLNAME);
			xmlw.writeCharacters(rt.getFullname());
			xmlw.writeEndElement();

			xmlw.writeStartElement(XMLTagNames.DESCRIPTION);
			xmlw.writeCharacters(rt.getDescription());
			xmlw.writeEndElement();

			xmlw.writeStartElement(XMLTagNames.INVERSENAME);
			xmlw.writeCharacters(rt.getInverseName());
			xmlw.writeEndElement();

			boolean isAntisymmetric = rt.isAntisymmetric();
			xmlw.writeStartElement(XMLTagNames.ISANTISYMMETRIC);
			xmlw.writeCharacters(String.valueOf(isAntisymmetric));
			xmlw.writeEndElement();

			boolean isReflexive = rt.isReflexive();
			xmlw.writeStartElement(XMLTagNames.ISREFLEXTIVE);
			xmlw.writeCharacters(String.valueOf(isReflexive));
			xmlw.writeEndElement();

			boolean isSymmetric = rt.isSymmetric();
			xmlw.writeStartElement(XMLTagNames.ISSYMMETRIC);
			xmlw.writeCharacters(String.valueOf(isSymmetric));
			xmlw.writeEndElement();

			boolean isTransitive = rt.isTransitiv();
			xmlw.writeStartElement(XMLTagNames.ISTRANSITIVE);
			xmlw.writeCharacters(String.valueOf(isTransitive));
			xmlw.writeEndElement();

			RelationType specialisationOf = rt.getSpecialisationOf();
			// specialisationOf is optional
			if (specialisationOf != null) {
				xmlw.writeStartElement(XMLTagNames.SPECIALISATIONOF);
				buildRelationTypeContent(xmlw, specialisationOf, nestedRule);
				xmlw.writeEndElement();
			}
		}
	}

	/**
	 * Builds lists of relationtypetype with related tag.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param rtlist
	 *            list of relationtypes
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	protected void buildRelationTypes(XMLStreamWriter2 xmlw,
			Set<RelationType> rtlist) throws XMLStreamException {

		if (prettyPrint)
			xmlw.writeCharacters(newline);
		xmlw.writeStartElement(XMLTagNames.RELATIONTYPES);
		for (RelationType rt : rtlist) {
			buildRelationType(xmlw, rt);
		}
		xmlw.writeEndElement(); // end rt
	}

	/**
	 * Builds a unittype xml tag with the content of the Unit object.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param unit
	 *            a Unit object
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	protected void buildUnit(XMLStreamWriter2 xmlw, Unit unit)
			throws XMLStreamException {

		xmlw.writeStartElement(XMLTagNames.UNIT);
		buildUnitContent(xmlw, unit, RefOrVal.VAL);
		xmlw.writeEndElement(); // end Unit tag
	}

	/**
	 * Builds the content of a unittype xml tag with the content of the Unit
	 * object.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param unit
	 *            a Unit object
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	private void buildUnitContent(XMLStreamWriter2 xmlw, Unit unit, RefOrVal rov)
			throws XMLStreamException {
		switch (rov) {
		case REF:
			buildIdRef(xmlw, unit);
			break;
		case VAL:
			xmlw.writeStartElement(XMLTagNames.ID);
			xmlw.writeCharacters(unit.getId());
			xmlw.writeEndElement();

			xmlw.writeStartElement(XMLTagNames.FULLNAME);
			xmlw.writeCharacters(unit.getFullname());
			xmlw.writeEndElement();

			xmlw.writeStartElement(XMLTagNames.DESCRIPTION);
			xmlw.writeCharacters(unit.getDescription());
			xmlw.writeEndElement();
		}

	}

	/**
	 * Builds lists of unittype with related tag.
	 * 
	 * @param xmlw
	 *            xml stream to write in
	 * @param ulist
	 *            list of units
	 * @throws javax.xml.stream.XMLStreamException
	 *             if xml writing fails
	 */
	protected void buildUnits(XMLStreamWriter2 xmlw, Set<Unit> ulist)
			throws XMLStreamException {

		if (prettyPrint)
			xmlw.writeCharacters(newline);
		xmlw.writeStartElement(XMLTagNames.UNITS);
		for (Unit u : ulist) {
			buildUnit(xmlw, u);
		}
		xmlw.writeEndElement(); // end units
	}

	/**
	 * Defines the arguments that can be passed to this export.
	 * 
	 * @return ArgumentDefinition[]
	 */
	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		ArgumentDefinition<?>[] options = getOptionArgumentDefinitions();
		ArgumentDefinition<?>[] outputs = getOutputArgumentDefinitions();
		ArgumentDefinition<?>[] combined = new ArgumentDefinition<?>[options.length
				+ outputs.length];
		for (int i = 0; i < options.length; i++) {
			combined[i] = options[i];
		}
		for (int i = 0; i < outputs.length; i++) {
			combined[i + options.length] = outputs[i];
		}
		return combined;
	}

	private ArgumentDefinition<?>[] getOptionArgumentDefinitions() {
		return new ArgumentDefinition[] {
				new StringArgumentDefinition(
						ArgumentNames.EXCLUDE_C_WITH_CC_ARG,
						ArgumentNames.EXCLUDE_C_WITH_CC_ARG_DESC, false, null,
						true),
				new StringArgumentDefinition(
						ArgumentNames.EXCLUDE_R_WITH_RT_ARG,
						ArgumentNames.EXCLUDE_R_WITH_RT_ARG_DESC, false, null,
						true),
				new StringArgumentDefinition(
						ArgumentNames.EXCLUDE_ATTRIBUTE_ARG,
						ArgumentNames.EXCLUDE_ATTRIBUTE_ARG_DESC, false, null,
						true),
				new StringArgumentDefinition(
						ArgumentNames.EXCLUSIVE_ATTRIBUTE_INCLUSION,
						ArgumentNames.EXCLUSIVE_ATTRIBUTE_INCLUSION_DESC,
						false, null, true),
				new StringArgumentDefinition(
						ArgumentNames.EXCLUSIVE_C_WITH_CC_INCLUSION,
						ArgumentNames.EXCLUSIVE_C_WITH_CC_INCLUSION_DESC,
						false, null, true),
				new StringArgumentDefinition(
						ArgumentNames.EXCLUSIVE_R_WITH_RT_INCLUSION,
						ArgumentNames.EXCLUSIVE_R_WITH_RT_INCLUSION_DESC,
						false, null, true),
				new BooleanArgumentDefinition(ArgumentNames.PRETTY_PRINTING,
						ArgumentNames.PRETTY_PRINTING_DESC, false, true),
				new BooleanArgumentDefinition(
						ArgumentNames.EXPORT_ISOLATED_CONCEPTS,
						ArgumentNames.EXPORT_ISOLATED_CONCEPTS_DESC, false,
						true) };
	}

	protected ArgumentDefinition<?>[] getOutputArgumentDefinitions() {
		return new ArgumentDefinition[] {
				new CompressResultsArguementDefinition(
						ArgumentNames.EXPORT_AS_ZIP_FILE,
						ArgumentNames.EXPORT_AS_ZIP_FILE_DESC, false, true),
				new FileArgumentDefinition(FileArgumentDefinition.EXPORT_FILE,
						"OXL file to export", true, false, false, false) };
	}

	/**
	 * Returns the name of this Export.
	 * 
	 * @return String
	 */
	@Override
	public String getName() {
		return "OXL Export";
	}

	/**
	 * Returns the version of this Export.
	 * 
	 * @return String
	 */
	@Override
	public String getVersion() {
		return "23/02/2011";
	}

	@Override
	public String getId() {
		return "oxl";
	}

	/**
	 * No indexed graph required.
	 * 
	 * @return false
	 */
	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	/**
	 * Builds a Document according to specifications in the ExportArguments
	 * 
	 * @throws IOException
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	@Override
	public void start() throws IOException, XMLStreamException, JAXBException,
			InvalidPluginArgumentException {

		setOptionalArguments();

		WstxOutputFactory xmlw = getXMLFactory();

		XMLStreamWriter2 xmlWriteStream = getXMLStreamWriter2(xmlw);

		fireEventOccurred(new GeneralOutputEvent("Ready to Export.",
				"[Export - start]"));

		try {
			// a graph overseeds list of concepts / relations
			if (graph != null) {
				concepts = graph.getConcepts();
				relations = graph.getRelations();
			}
			buildDocument(xmlWriteStream, concepts, relations);
		} catch (JAXBException e) {
			throw new XMLStreamException("Failed to write Attribute values", e);
		}

		xmlWriteStream.flush();
		xmlWriteStream.close();
		flushOutput();
		fireEventOccurred(new GeneralOutputEvent("Finished OXL Export.",
				"[Export - start]"));
	}

	/**
	 * Decide what concepts should be written.
	 * 
	 * @param concepts
	 *            Set<ONDEXConcept>
	 * @return filtered Set<ONDEXConcept>
	 */
	private Set<ONDEXConcept> filterConcepts(Set<ONDEXConcept> concepts) {
		Set<ONDEXConcept> filtered = new HashSet<ONDEXConcept>();

		for (ONDEXConcept c : concepts) {
			boolean writeConcept = true;

			if (excludeCCSet != null && excludeCCSet.size() > 0) {
				if (excludeCCSet.contains(c.getOfType().getId())) {
					writeConcept = false;
				}
			}

			if (writeConcept && !writeIsolatedConcepts) {
				if (conIdToRelationCounts.get(c.getId()) == 0) {
					writeConcept = false;
				}
			}

			if (writeConcept) {
				filtered.add(c);
			}
		}

		return filtered;
	}

	/**
	 * Decide what relations should be written.
	 * 
	 * @param relations
	 *            Set<ONDEXRelation>
	 * @return filtered Set<ONDEXRelation>
	 */
	private Set<ONDEXRelation> filterRelations(Set<ONDEXRelation> relations) {
		Set<ONDEXRelation> filtered = new HashSet<ONDEXRelation>();

		for (ONDEXRelation r : relations) {
			boolean writeRelation = true;

			ONDEXConcept from = r.getFromConcept();
			ONDEXConcept to = r.getToConcept();

			if (excludeCCSet != null && excludeCCSet.size() > 0) {
				if (excludeCCSet.contains(from.getOfType().getId())
						|| excludeCCSet.contains(to.getOfType().getId())) {
					writeRelation = false;
				}
			}

			if (excludeRt != null && excludeRt.size() > 0) {
				if (excludeRt.contains(r.getOfType().getId())) {
					writeRelation = false;
				}
			}

			if (writeRelation) {
				filtered.add(r);
			}
		}

		return filtered;
	}

	/**
	 * Builds a Document according to specifications in the ExportArguments
	 * 
	 * @throws IOException
	 * @throws XMLStreamException
	 *             if xml writing fails
	 */
	private void setOptionalArguments() throws IOException, XMLStreamException,
			JAXBException, InvalidPluginArgumentException {

		if (graph == null && (concepts == null || relations == null))
			throw new NullPointerException(
					"Error: Ondex graph not set for export");

		if (graph != null) {

			// get Attribute list to exclude
			List<String> attributeList = args.getObjectValueList(
					ArgumentNames.EXCLUDE_ATTRIBUTE_ARG, String.class);
			if (attributeList == null) {
				fireEventOccurred(new GeneralOutputEvent(
						"attributeList is null.",
						"[Export - setOptionalArguments]"));
			} else {
				fireEventOccurred(new GeneralOutputEvent(
						"set attributeList with size " + attributeList.size(),
						"[Export - setOptionalArguments]"));
			}

			if (attributeList != null) {
				for (String anAttributeList : attributeList) {
					excludeGDSSet.add(anAttributeList);
				}
			}

			// get CC list to exclude
			List<String> ccList = args.getObjectValueList(
					ArgumentNames.EXCLUDE_C_WITH_CC_ARG, String.class);
			if (ccList == null) {
				fireEventOccurred(new GeneralOutputEvent("ccList is null.",
						"[Export - setOptionalArguments]"));
			} else {
				fireEventOccurred(new GeneralOutputEvent(
						"set ccList with size " + ccList.size(),
						"[Export - setOptionalArguments]"));
			}

			if (ccList != null) {
				for (String aCcList : ccList) {
					excludeCCSet.add(aCcList);
				}
			}

			// get RelationType list to exclude
			List<String> rtList = args.getObjectValueList(
					ArgumentNames.EXCLUDE_R_WITH_RT_ARG, String.class);
			if (rtList == null) {
				fireEventOccurred(new GeneralOutputEvent("rtList is null.",
						"[Export - setOptionalArguments]"));
			} else {
				fireEventOccurred(new GeneralOutputEvent(
						"set rtList with size " + rtList.size(),
						"[Export - setOptionalArguments]"));
			}

			if (rtList != null) {
				for (String aRtList : rtList) {
					excludeRt.add(aRtList);
				}
			}

			// get Attribute list with exclusive inclusions
			List<String> inAtList = args.getObjectValueList(
					ArgumentNames.EXCLUSIVE_ATTRIBUTE_INCLUSION, String.class);
			if (inAtList == null) {
				fireEventOccurred(new GeneralOutputEvent("inAtList is null.",
						"[Export - setOptionalArguments]"));
			} else {
				fireEventOccurred(new GeneralOutputEvent(
						"set inAtList with size " + inAtList.size(),
						"[Export - setOptionalArguments]"));
			}

			if (inAtList != null && inAtList.size() > 0) {
				if (excludeGDSSet.size() > 0) {
					fireEventOccurred(new WrongParameterEvent(
							"Exclusion and Inclusion filters are exclusive: Ignored Attribute Attribute Exclusions",
							"[Export - setOptionalArguments]"));
				}

				fireEventOccurred(new GeneralOutputEvent(
						"Attribute Attribute exclusive inclusions on GDSs will be applied",
						"[Export - setOptionalArguments]"));

				excludeGDSSet.clear();
				for (AttributeName an : graph.getMetaData().getAttributeNames()) {
					String id = an.getId();
					if (!inAtList.contains(id)) {
						excludeGDSSet.add(id);
					}
				}
			}

			// get CC list with exclusive inclusions
			List<String> inCCList = args.getObjectValueList(
					ArgumentNames.EXCLUSIVE_C_WITH_CC_INCLUSION, String.class);
			if (inCCList == null) {
				fireEventOccurred(new GeneralOutputEvent("inCCList is null.",
						"[Export - setOptionalArguments]"));
			} else {
				fireEventOccurred(new GeneralOutputEvent(
						"set inCCList with size " + inCCList.size(),
						"[Export - setOptionalArguments]"));
			}

			if (inCCList != null && inCCList.size() > 0) {
				if (excludeCCSet.size() > 0) {
					fireEventOccurred(new WrongParameterEvent(
							"Exclusion and Inclusion filters are exclusive: Ignored Concept Class Exclusions",
							"[Export - setOptionalArguments]"));
				}

				fireEventOccurred(new GeneralOutputEvent(
						"ConceptClass exclusive inclusions on Concepts will be applied",
						"[Export - setOptionalArguments]"));

				excludeCCSet.clear();
				for (ConceptClass cc : graph.getMetaData().getConceptClasses()) {
					String id = cc.getId();
					if (!inCCList.contains(id)) {
						excludeCCSet.add(id);
					}
				}
			}

			// get RelationType list with exclusive inclusions
			List<String> inRTList = args.getObjectValueList(
					ArgumentNames.EXCLUSIVE_R_WITH_RT_INCLUSION, String.class);
			if (inRTList == null) {
				fireEventOccurred(new GeneralOutputEvent("inRTList is null.",
						"[Export - setOptionalArguments]"));
			} else {
				fireEventOccurred(new GeneralOutputEvent(
						"set inRTList with size " + ccList.size(),
						"[Export - setOptionalArguments]"));
			}

			if (inRTList != null && inRTList.size() > 0) {
				if (excludeRt.size() > 0) {
					fireEventOccurred(new WrongParameterEvent(
							"Exclusion and Inclusion filters are exclusive: Ignored RelationType Exclusions",
							"[Export - setOptionalArguments]"));
				}

				fireEventOccurred(new GeneralOutputEvent(
						"RelationType exclusive inclusions on Relations will be applied",
						"[Export - setOptionalArguments]"));

				excludeRt.clear();
				for (RelationType rt : graph.getMetaData().getRelationTypes()) {
					String id = rt.getId();
					if (!inRTList.contains(id)) {
						excludeRt.add(id);
					}
				}
			}

			// get write isolated concepts option
			writeIsolatedConcepts = (Boolean) args
					.getUniqueValue(ArgumentNames.EXPORT_ISOLATED_CONCEPTS);
			fireEventOccurred(new GeneralOutputEvent(
					"writeIsolatedConcept set to: " + writeIsolatedConcepts,
					"[Export - setOptionalArguments]"));

			if (!writeIsolatedConcepts) {
				conIdToRelationCounts = LazyMap.decorate(
						new HashMap<Integer, Integer>(),
						new Factory<Integer>() {
							@Override
							public Integer create() {
								return Integer.valueOf(0);
							}
						});
				fireEventOccurred(new GeneralOutputEvent(
						"Dont write isolate concepts!",
						"[Export - setOptionalArguments]"));
				for (ONDEXRelation r : graph.getRelations()) {

					// ignore excluded relation types
					if (excludeRt != null) {
						if (excludeRt.contains(r.getOfType().getId())) {
							continue;
						}
					}

					// from concept count
					int fc = r.getFromConcept().getId();
					conIdToRelationCounts.put(fc,
							conIdToRelationCounts.get(fc) + 1);

					// to concept count
					int tc = r.getToConcept().getId();
					conIdToRelationCounts.put(tc,
							conIdToRelationCounts.get(tc) + 1);
				}
			}
		}

		prettyPrint = ((Boolean) args
				.getUniqueValue(ArgumentNames.PRETTY_PRINTING));
		fireEventOccurred(new GeneralOutputEvent("prettyPrint set to: "
				+ prettyPrint, "[Export - setOptionalArguments]"));
	}

	protected WstxOutputFactory getXMLFactory() {
		// initialize outputs
		System.setProperty("javax.xml.stream.XMLOutputFactory",
				"com.ctc.wstx.stax.WstxOutputFactory");

		WstxOutputFactory xmlw = (WstxOutputFactory) WstxOutputFactory
				.newInstance();
		xmlw.configureForRobustness();

		xmlw.setProperty(XMLOutputFactory2.IS_REPAIRING_NAMESPACES, false);
		xmlw.setProperty(WstxOutputProperties.P_OUTPUT_FIX_CONTENT, true);
		xmlw.setProperty(WstxOutputProperties.P_OUTPUT_VALIDATE_CONTENT, true);
		return xmlw;
	}

	protected XMLStreamWriter2 getXMLStreamWriter2(WstxOutputFactory xmlw)
			throws InvalidPluginArgumentException, XMLStreamException,
			IOException {
		String fileName = ((String) args
				.getUniqueValue(FileArgumentDefinition.EXPORT_FILE)).trim();
		fireEventOccurred(new GeneralOutputEvent(
				"fileName set to: " + fileName, "[Export - start]"));

		boolean packed = ((Boolean) args
				.getUniqueValue(ArgumentNames.EXPORT_AS_ZIP_FILE));
		fireEventOccurred(new GeneralOutputEvent("packed set to: " + packed,
				"[Export - start]"));

		if (fileName.toLowerCase().endsWith(".oxl")) {
			// override packed option for .oxl
			packed = true;
		} else if (packed && !fileName.toLowerCase().endsWith(".gz")) {
			if (!fileName.toLowerCase().endsWith(".xml"))
				fileName += ".xml";
			fileName += ".gz";
		} else if (!packed && !fileName.toLowerCase().endsWith(".xml")) {
			fileName += ".xml";
		}
		File file = new File(fileName);
		if (packed) {
			// use gzip compression
			outStream = new GZIPOutputStream(new FileOutputStream(
					file.getAbsolutePath()));
		} else {
			// output file writer
			outStream = new FileOutputStream(file);
		}

		XMLStreamWriter2 xmlWriteStream = (XMLStreamWriter2) xmlw
				.createXMLStreamWriter(outStream, CharsetNames.CS_UTF8);

		return xmlWriteStream;
	}

	protected void flushOutput() throws IOException {
		outStream.flush();
		outStream.close();
	}

	/**
	 * Include by reference (id) or value (the complete XML)
	 * 
	 * @author Matthew Pocock
	 */
	protected enum RefOrVal {
		REF, VAL
	}

	@Override
	public int getMaxProgress() {
		return maxProgress;
	}

	@Override
	public int getMinProgress() {
		return minProgress;
	}

	@Override
	public int getProgress() {
		return progress;
	}

	@Override
	public String getState() {
		return state;
	}

	@Override
	public Throwable getUncaughtException() {
		return null;
	}

	@Override
	public boolean isAbortable() {
		return true;
	}

	@Override
	public boolean isIndeterminate() {
		return false;
	}

	@Override
	public void setCancelled(boolean c) {
		cancelled = c;
	}
}
