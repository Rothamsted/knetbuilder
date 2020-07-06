package net.sourceforge.ondex.export.oxl;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import net.sourceforge.ondex.core.util.Holder;

/**
 * Utility bean to wrap up map values.
 *
 * @author Matthew Pocock
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class MapHolder<K, V> implements Holder<Map<K, V>> {

    @XmlElementWrapper(name = "map")
    private Object[] keys;

    @XmlElementWrapper(name = "values")
    private Object[] values;

    @XmlAttribute
    private Class<? extends Map> clazz;

    public MapHolder() {
    }

    public MapHolder(Map<K, V> map) {
        setValue(map);
    }

    /**
     * @return the class that this Holds
     */
    public Class<? extends Map> getClazz() {
        return clazz;
    }

    public Map<K, V> getValue() throws JAXBException {
        try {
            Map<K, V> map = clazz.getDeclaredConstructor ().newInstance();
            for (int i = 0; i < keys.length; i++) {
                map.put((K) keys[i], (V) values[i]);
            }
            return map;
        } 
        catch (InstantiationException 
        			 | IllegalAccessException
        			 | IllegalArgumentException
        			 | InvocationTargetException
        			 | NoSuchMethodException
        			 | SecurityException e) 
        {
            throw new JAXBException(e);
        }
    }

    public void setValue(Map<K, V> map) {
        keys = new Object[map.size()];   //this must be a copy not the origninal else nested collections are borked by reference
        values = new Object[map.size()];
        Iterator<Map.Entry<K, V>> entit = map.entrySet().iterator();
        for (int i = 0; i < keys.length; i++) {
            Map.Entry<K, V> entry = entit.next();
            keys[i] = entry.getKey();
            values[i] = entry.getValue();
        }
        clazz = map.getClass();
    }
}
