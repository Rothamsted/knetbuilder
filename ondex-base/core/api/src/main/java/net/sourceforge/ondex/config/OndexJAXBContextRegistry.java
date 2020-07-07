package net.sourceforge.ondex.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.util.Holder;

/**
 * A central place where classes and adaptors are registered to the Ondex
 * JAXBContext and it's un/marshallers respectivly NB adaptors can only be set
 * for Un/Marshallers so if a
 * JAXBContext.createMarshaller()/JAXBContext.createUnmarshaller() is called
 * they will not have Ondex adaptors.
 * 
 * @author hindlem
 * @author Matthew Pocock
 * @see #createMarshaller()
 * @see #createMarshaller(ONDEXGraphMetaData)
 * @see #createUnmarshaller()
 * @see #createUnmarshaller(ONDEXGraphMetaData) <p/>
 *      Or to add Ondex adaptors to a custom JAXBContext
 * @see #addAdaptors(Marshaller)
 * @see #addAdaptors(Unmarshaller)
 */
public class OndexJAXBContextRegistry {

	/**
	 * Singleton instance
	 */
	private static OndexJAXBContextRegistry registry;

	private final Set<Class<?>> classesToBind = Collections
			.synchronizedSet(new HashSet<Class<?>>());

	private final Set<XmlAdapter<?, ?>> adaptors = Collections
			.synchronizedSet(new HashSet<XmlAdapter<?, ?>>());

	private final Map<Class<?>, Class<? extends Holder>> holders = Collections
			.synchronizedMap(new HashMap<Class<?>, Class<? extends Holder>>());

	private final Map<AttributeName, Class<?>> attributes = Collections
			.synchronizedMap(new HashMap<AttributeName, Class<?>>());

	/**
	 * Fetch the singleton instance.
	 * 
	 * @return the singleton instance
	 */
	public static OndexJAXBContextRegistry instance() {
		if (registry == null)
			registry = new OndexJAXBContextRegistry();
		return registry;
	}

	/**
	 * Associates a configured instance of {@link XmlAdapter} with all
	 * marshallers returned by getInstance.
	 * <p/>
	 * <p/>
	 * Every marshaller internally maintains a {@link java.util.Map}&lt;
	 * {@link Class},{@link XmlAdapter}>, which it uses for marshalling classes
	 * whose fields/methods are annotated with
	 * {@link javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter}.
	 * 
	 * @param adapter
	 *            The instance of the adapter to be used. If null, it will
	 *            un-register the current adapter set for this type.
	 */
	public synchronized <A extends XmlAdapter<?, ?>> void addAdapter(A adapter) {
		adaptors.add(adapter);
	}

	/**
	 * Registers holder to a class and replaces any existing holder for that
	 * class, removes holder for class if holder is null
	 * 
	 * @param holder
	 *            a holder class that wraps a java class to store
	 */
	public synchronized void addHolder(Class<?> c, Class<? extends Holder> holder) {
		holders.put(c, holder);
	}

	/**
	 * Gets a pre-registered holder for a specified class
	 * 
	 * @param c
	 *            the class to look for a holder on
	 * @return a registered holder on a class (null if not present)
	 */
	public Class<? extends Holder> getHolder(Class<?> c) {
		for (Class<?> classH : holders.keySet()) {
			if (classH.equals(c) || classH.isAssignableFrom(c)) {
				return holders.get(classH);
			}
		}
		return null;
	}

	/**
	 * Puts a given object into a new Holder object. The holder object is
	 * determined by the given class.
	 * 
	 * @param c
	 *            class for holder object
	 * @param applyTo
	 *            object to hold
	 * @return new holder
	 */
	@SuppressWarnings({ "unchecked" })
	public Object applyHolder(Class<?> c, Object applyTo) {
		Class<?> hc = getHolder(c);
		Object ho;
		if (hc != null) {
			Holder holder = null;
			try {
				holder = (Holder) hc.newInstance();
				holder.setValue(applyTo);
			} catch (Exception e) {
				throw new Error("Unable to create holder for object "
						+ applyTo.getClass(), e);
			}
			ho = holder;
		} else {
			ho = applyTo;
		}
		return ho;
	}

	/**
	 * @param classesToBeBound
	 *            list of java classes to be recognized by the new
	 *            {@link JAXBContext}. Can be empty, in which case a
	 *            {@link JAXBContext} that only knows about spec-defined classes
	 *            will be returned.
	 * @return if the classes to be bound changed as a result
	 */
	public synchronized boolean addClassBindings(Class<?>... classesToBeBound) {
		boolean added = false;
		for (Class<?> classTB : classesToBeBound) {
			if (classesToBind.add(classTB)) {
				added = true;
			}
		}

		return added;
	}

	/**
	 * Create a new context using the bound classes (@see getMarshaller
	 * adapters).
	 * 
	 * @return the configured context
	 * @throws JAXBException
	 *             if the marshaller could not be created
	 */
	public synchronized JAXBContext createContext() throws JAXBException {

		Set<Class<?>> classes = new HashSet<Class<?>>(classesToBind);
		classes.addAll(attributes.values());
		return JAXBContext
				.newInstance(classes.toArray(new Class[classes.size()]));
	}

	/**
	 * Create a new context using the bound classes and adapters, and also with
	 * all the types for all Attribute registered. NB class types in the meta
	 * data that already have adaptors will not be bound
	 * 
	 * @param md
	 *            graph metadata to use
	 * @return a configured JAXBContext
	 * @throws JAXBException
	 *             if the context could not be instantiated
	 */
	public synchronized JAXBContext createContext(ONDEXGraphMetaData md)
			throws JAXBException {

		for (AttributeName an : md.getAttributeNames()) {
			addAttribute(an);
		}

		return createContext();
	}

	/**
	 * @param attn
	 *            an attribute name to bind the class for
	 * @return if this contained a valid object for serialization
	 */
	public synchronized boolean addAttribute(AttributeName attn) {
		Class<?> dataType = attn.getDataType();
		if (!dataType.isInterface() // don't add interfaces
				&& getHolder(dataType) == null) {// or something that has a
													// holder
			attributes.put(attn, dataType);
			return true;
		}
		return false;
	}

	public boolean hasAttributeName(AttributeName attn) {
		return attributes.containsKey(attn);
	}

	/**
	 * Searches bound classes for a given class name
	 * 
	 * @param name
	 *            class name
	 * @return Class<?>
	 */
	public Class<?> getClassByName(String name) {
		for (Class<?> clazz : this.holders.keySet()) {
			if (clazz.getName().equals(name))
				return clazz;
		}
		return null;
	}

	/**
	 * @param md
	 *            the metadata to extract and add AttributeType Class data from,
	 *            and add to bound classes.
	 * @return marshaller with Ondex adaptors added
	 * @throws JAXBException
	 *             if the marshaller could not be generated
	 */
	public synchronized Marshaller createMarshaller(ONDEXGraphMetaData md)
			throws JAXBException {
		return addAdaptors(createContext(md).createMarshaller());
	}

	/**
	 * @return marshaller with Ondex adaptors added
	 * @throws JAXBException
	 *             if the marshaller could not be generated
	 */
	public synchronized Marshaller createMarshaller() throws JAXBException {
		return addAdaptors(createContext().createMarshaller());
	}

	/**
	 * @param md
	 *            the metadata to extract and add AttributeType Class data from,
	 *            and add to bound classes.
	 * @return unmarshaller with Ondex adaptors added
	 * @throws JAXBException
	 *             if the unmarshaller could not be generated
	 */
	public synchronized Unmarshaller createUnmarshaller(ONDEXGraphMetaData md)
			throws JAXBException {
		return addAdaptors(createContext(md).createUnmarshaller());
	}

	/**
	 * @return unmarshaller with Ondex adaptors added
	 * @throws JAXBException
	 *             if the unmarshaller could not be generated
	 */
	public synchronized Unmarshaller createUnmarshaller() throws JAXBException {
		return addAdaptors(createContext().createUnmarshaller());
	}

	/**
	 * Adds adaptors to marshaller
	 * 
	 * @param marshaller
	 *            the marshaller to add adaptors to
	 * @return the same marshaller with adaptors added
	 */
	public synchronized Marshaller addAdaptors(Marshaller marshaller) {
		for (XmlAdapter<?, ?> adaptor : adaptors) {
			marshaller.setAdapter(adaptor);
		}
		return marshaller;
	}

	/**
	 * Adds adaptors to unmarshaller
	 * 
	 * @param unmarshaller
	 *            the unmarshaller to add adaptors to
	 * @return the same unmarshaller with adaptors added
	 */
	public synchronized Unmarshaller addAdaptors(Unmarshaller unmarshaller) {
		for (XmlAdapter<?, ?> adaptor : adaptors) {
			unmarshaller.setAdapter(adaptor);
		}
		return unmarshaller;
	}

	/**
	 * Checks if a JAVA class is registered for JAXB.
	 * 
	 * @param c
	 *            Class<?>
	 * @return registered?
	 */
	public boolean isClassRegistered(Class<?> c) {
		if (classesToBind.contains(c) || attributes.values().contains(c)
				|| getHolder(c) != null) {
			return true;
		}
		return false;
	}
}
