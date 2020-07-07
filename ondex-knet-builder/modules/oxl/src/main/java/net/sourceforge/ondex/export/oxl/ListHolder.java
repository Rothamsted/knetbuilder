package net.sourceforge.ondex.export.oxl;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import net.sourceforge.ondex.core.util.Holder;

/**
 * Utility bean to wrap up list values.
 *
 * @author Matthew Pocock
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class ListHolder<E> implements Holder<List<E>> {
    @XmlElementWrapper(name = "item")
    private List<E> list;

    public ListHolder() {
    }

    public ListHolder(List<E> list) {
        this.list = list;
    }

    public List<E> getValue() {
        return list;
    }

    public void setValue(List<E> list) {
        this.list = list;
    }
}
