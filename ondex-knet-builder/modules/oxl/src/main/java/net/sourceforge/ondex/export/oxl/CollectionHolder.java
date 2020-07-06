package net.sourceforge.ondex.export.oxl;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;

import net.sourceforge.ondex.core.util.Holder;

/**
 * A collection holder for any collection with an empty constructor
 *
 * @author hindlem
 *         Created 29-Apr-2010 14:16:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class CollectionHolder<E> implements Holder<Collection<E>> {

    @XmlElementWrapper(name = "item")
    private Object[] values;

    @XmlAttribute
    private Class clazz;

    public CollectionHolder() {
    }

    public CollectionHolder(Collection<E> col) {
        setValue(col);
    }

    /**
     * @return the class that this Holds
     */
    public Class<? extends Collection> getClazz() {
        return clazz;
    }

    public Collection<E> getValue() throws JAXBException {
        try {
            Collection<E> newcollection = ((Collection<E>) clazz.getDeclaredConstructor().newInstance());
            for (Object value : values) {
                newcollection.add((E) value);
            }
            return newcollection;
        } 
        catch ( InstantiationException 
        					| IllegalAccessException
        					| IllegalArgumentException
        					| InvocationTargetException
        					| NoSuchMethodException
        					| SecurityException e) 
        {
            throw new JAXBException(e);
        } 
    }

    public void setValue(Collection<E> col) {
        values = col.toArray(); //this must be a copy not the origninal else nested collections are borked by reference
        clazz = col.getClass();
    }

}
